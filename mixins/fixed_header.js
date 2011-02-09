// FixedHeader, a mixin for enabling iOS style list headers,
// wherein the firstmost group's header is fixed at the top until
// 'pushed' out of frame by the next group's header on scroll

// obviously, this should only be used within a scroll view
// and for simplicity's sake, *not* a nested scrollview

Endash.FixedListHeaders = {

  reloadIfNeeded: function(nowShowing, scrollOnly) {
    var skipHeader = false, ret, header, group;
    var invalid = this._invalidIndexes;

    if (!this.get('content')) {
      skipHeader = true;
    }

    if(!nowShowing) {
      nowShowing = this.get('nowShowing');
    }
    
    ret = sc_super();

    if(skipHeader) {
      return ret;
    }

    this.layoutHeader(nowShowing);

    return ret;
  },

  layoutHeader: function(nowShowing) {
    // the current group is ALWAYS the first index in nowShowing
    // there are three possibilities:
    // the second showing item (third index) is not a group
    // the second showing item is a group and we're scrolling downward
    // the second showing item is a group and we're scrolling upward

    // var nowShowing = this.get('nowShowing'),
    if(!nowShowing) {
      return;
    }

    nowShowing = nowShowing.toArray();

    var group = nowShowing.objectAt(0),
      groups = this.get('_contentGroupIndexes').toArray(),
      secondItem = nowShowing.objectAt(1),
      thirdItem = nowShowing.objectAt(2),
      oldGroup = this._group,
      scrollView = this.getPath('parentView.parentView'),
      oldHeader, currentHeader, layout, layout2, header, offset;

    // the second item being displayed is a group

    if(groups.contains(thirdItem)) {

      if(thirdItem > oldGroup) {
        // we're scrolling down

        oldHeader = this.itemViewForContentIndex(group);
        layout = this.layoutForContentIndex(secondItem);

        oldHeader.$().css('position', 'absolute');
        oldHeader.adjust(layout);

        this.get('layer').appendChild(oldHeader.get('layer'));
      } else {
        // we're scrolling up

        oldHeader = this.itemViewForContentIndex(oldGroup);
        layout = this.layoutForContentIndex(oldGroup);

        currentHeader = this.itemViewForContentIndex(group);
        layout2 = this.layoutForContentIndex(secondItem);

        oldHeader.$().css(layout).css('position', 'absolute').css('z-index', 5000);
        oldHeader.adjust(layout);
        currentHeader.adjust(layout2);

        this.get('layer').appendChild(oldHeader.get('layer'));
      }

      this._layoutHeader = true;
    } else {
      if((group !== oldGroup) || this._layoutHeader) {
        header = this.itemViewForContentIndex(group);

        if(!header.get('layer')) {
          this._layoutHeader = true;
        } else {
          this._layoutHeader = false;

          if(oldGroup && nowShowing.contains(oldGroup)) {
            oldHeader = this.itemViewForContentIndex(oldGroup);

            oldHeader.adjust(this.layoutForContentIndex(this._group));
            oldHeader.$().css('position', 'absolute');
          }

          this._group = group;

          layout = {top: 0, left: 0, width: this.get('frame').width};
          header.$().css(layout).css('z-index', '5000').addClass('sc-group-item');
          scrollView.get('layer').appendChild(header.get('layer'));
          header.adjust(layout);
        }

      }
    }
  },

  contentIndexesInRect: function(rect) {
    var ret = sc_super();
    var group = this.currentGroup(ret.get('min'));

    if(group !== false) {
      if(!ret.contains(group)) {
        ret.add(group);
      }
    }

    return ret;
  },

  currentGroup: function(idx) {
    if(!this.get('content')) {
      return false;
    }

     var groups = this.get('_contentGroupIndexes').toArray(),
      len = groups.get('length'),
      last, i;

    if(groups.contains(idx)) {
      return idx;
    }

    for(i = 0; i < len; i++) {
      if(groups.objectAt(i) > idx) {
        return last;
      }
      last = groups.objectAt(i);
    }

    return last;
  }

};