// FixedHeader, a mixin for enabling iOS style list headers,
// wherein the firstmost group's header is fixed at the top until
// 'pushed' out of frame by the next group's header on scroll

// obviously, this should only be used within a scroll view
// and for simplicity's sake, *not* a nested scrollview

Endash.FixedListHeaders = {

	containerView: SC.View,

	reloadIfNeeded: function() {
		var ret = sc_super()
		
    var invalid = this._invalidIndexes;
    if (!invalid || !this.get('isVisibleInWindow'))
			return ret
			
		this.setFixedHeader()
		return ret
	}
	
	setFixedHeader: function() {
		var header, offset, content, idx,
			content, item, scrollView,
			nowShowing = this.get('nowShowing'),
			idx = nowShowing.get('min'),
			groups = this.get('_contentGroupIndexes'),
			isGroup = groups.contains(idx)
			
		if(!isGroup || idx == this._fixedHeaderIdx)
			return		
		
		content = this.get('content'),
		item = content.objectAt(idx)

		if(this._fixedHeader)
			this._fixedHeader.destroy()

		header = this.itemViewForContentIndex(idx)
		header.get('parentView').removeChild(header)
		header.set('layerId', this.get('layerId') + '-header')

		scrollView = this.getPath('.parentView.parentView')
		scrollView.appendChild(header)

		offset = scrollView.$().offset()
		header.$().css('position', 'fixed').css('top', offset.y).css('left', offset.x)

		this._fixedHeader = header
		this._fixedHeaderIdx = idx
	}
	
}
	