// FixedHeader, a mixin for enabling iOS style list headers,
// wherein the firstmost group's header is fixed at the top until
// 'pushed' out of frame by the next group's header on scroll

// obviously, this should only be used within a scroll view
// and for simplicity's sake, *not* a nested scrollview

Endash.FixedListHeaders = {

	reloadIfNeeded: function() {
		var invalid = this._invalidIndexes;
		if (!invalid || !this.get('isVisibleInWindow'))
			var skipHeader = true

		var ret = sc_super()
		
		if(!skipHeader)
			this.setFixedHeader()

		return ret
	},
	
	setFixedHeader: function() {
		// console.log('setFixedHeader')
		if(!this.get('content'))
			return
			
		var header, offset, content, idx,
			content, item, scrollView,
			nowShowing = this.get('nowShowing'),
			idx = nowShowing.get('min'),
			groups = this.get('_contentGroupIndexes'),
			isGroup = groups.contains(idx),
			content = this.get('content'),
			item = content.objectAt(idx)
			
		if(idx === this._fixedHeaderIdx)
		 	return
		
		if(!isGroup) {
		 	if(item.get('parentItem') == content.objectAt(this._fixedHeaderIdx))
				return
			else 
				idx = content.indexOf(item.get('parentItem'))
		}
		
		if(this._fixedHeader)
			this._fixedHeader.destroy()

		header = this.itemViewForContentIndex(idx)

		delete this._sc_itemViews[idx]
		
		scrollView = this.getPath('parentView.parentView')
		offset = scrollView.$().offset()

		header.$().css('position', 'fixed').css('z-index', '5000').css('top', offset.top).css('left', offset.left + 1)
			.css('right', null).css('width', this.get('frame').width + 1)
			
		header.set('layerId', this.get('layerId') + '-header')

		this._fixedHeader = header
		this._fixedHeaderIdx = idx
	},
	
	layoutForContentIndex: function(idx) {
		var view = this.itemViewForContentIndex(idx)
		if(view.isGroup && view == this.get('currentGroup'))
			
		else
			return sc_super()
	},
	
	currentGroup: function() {
		
	}.property('nowShowing').cacheable(),
	
	frameDidChange: function() {
		this.setFixedHeader()
	}.observes('frame')
	
}
	