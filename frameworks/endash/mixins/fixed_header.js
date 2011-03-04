// FixedHeader, a mixin for enabling iOS style list headers,
// wherein the firstmost group's header is fixed at the top until
// 'pushed' out of frame by the next group's header on scroll

// obviously, this should only be used within a scroll view
// and for simplicity's sake, *not* a nested scrollview

// (c) 2011 Christopher Swasey (Endash)
// christopher.swasey@gmail.com

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

    // If the second item being displayed is a group,
    // then we position the two headers 'normally'
    // to allowing them to scroll into position/off the screen
    // note that the second item in the display is the
    // third item in the nowShowing array

    if(groups.contains(thirdItem)) {

      if(thirdItem > oldGroup) {
        // we're scrolling down, so the next group is moving upward
        // visually speaking. we position the current header
        // over the last item in its group, so that the new header
        // appears to be pushing it out of the way

        oldHeader = this.itemViewForContentIndex(group);
        layout = this.layoutForContentIndex(secondItem);

        oldHeader.adjust(layout);

        this.get('layer').appendChild(oldHeader.get('layer'));
      } else {
        // we're scrolling up, so the next header is moving downward
        // visually. We reposition the old header to its 'proper'
        // position

        oldHeader = this.itemViewForContentIndex(oldGroup);
        layout = this.layoutForContentIndex(oldGroup);

        currentHeader = this.itemViewForContentIndex(group);
        layout2 = this.layoutForContentIndex(secondItem);

        oldHeader.adjust(layout);
        currentHeader.adjust(layout2);

        this.get('layer').appendChild(oldHeader.get('layer'));
      }

      this._layoutHeader = true;
    } else {

      // Either the group has changed, or the current header
      // needs to be laid out

      if((group !== oldGroup) || this._layoutHeader) {
        header = this.itemViewForContentIndex(group);

        if(!header.get('layer')) {
          // the header will be laid out next time since
          // we don't have the DOM element yet

          this._layoutHeader = true;
        } else {
          this._layoutHeader = false;
          this._group = group;

          // Move the header to the scrollView, so we can position it
          // and set the layout 
          layout = {top: 0, left: 0, width: this.get('frame').width};
          scrollView.get('layer').appendChild(header.get('layer'));
          
          // HAAAACK
          header.$().addClass('sc-group-item');
          header.adjust(layout);
        }

      }
    }
  },

  // Put the current group at the beginning of the nowShwowing

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
  
  // Get the current group, by stepping through the groups
  // and getting the largest groupId without going over
  // the first index showing

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