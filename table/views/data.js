// ==========================================================================
// Project:   SproutCore - JavaScript Application Framework
// Copyright: ©2006-2009 Sprout Systems, Inc. and contributors.
//            Portions ©2008-2009 Apple Inc. All rights reserved.
// License:   Licened under MIT license (see license.js)
// ==========================================================================

Endash.DataView = SC.ListView.extend(Endash.CollectionFastPath, {

  rowHeight: 40,

  cellWrapperView: SC.View.extend(SC.Control, {
    isPoolable: YES,
    layerIsCacheable: YES,
    
    renderLayout: function(context, firstTime) {
      if(firstTime) {
        sc_super();
      }
    }
  }),

  exampleView: SC.LabelView.extend({
    contentValueKeyBinding: '*column.key'
  }),
  
  // @index should be the transformed index, so we can grab the column

  exampleViewForItem: function(item, index) {
    var columns = this.get('columns'),
      column = columns.objectAt(this.columnForIndex(index)),
      ret;
  
    if(ret = column.get('exampleView')) {
      return ret;
    }
  
    return this.get('exampleView');
  },
  
  exampleViewForItem: function() {
    var exampleViews = this._exampleViews;
    
    if(!exampleViews) {
      this._exampleViews = exampleViews = [];
    }
      
    if(!exampleViews[0]) {
      exampleViews[0] = this.get('cellWrapperView').extend({
        childViews: 'contentView'.w(),
        contentView: this.get('exampleView').extend({
          columnBinding: '.parentView*column',
          contentBinding: '.parentView*content'
        })
      });
    }
    
    return exampleViews[0];
  },
  
  /* 
    Transform the visible rows and columns into one set of indexes e.g, 
    for three columns:
    
    content[0] => [0, 1, 2, ...]
    content[1] => [..., 3, 4, 5, ...]
    
    These indexes can be translated back with integer division and modulus
    operations. Appropriate methods are provided.
    
    etc
  */
  
  computeNowShowing: function(rect) {
    var contentIndexes = this.contentIndexesInRect(rect || this.get('clippingFrame')),
    len = this.get('length'), 
    max = contentIndexes.get('max');
    
    if (max > len) contentIndexes = contentIndexes.copy().remove(len, max-len).freeze();

    var numColumns = this.getPath('columns.length'),
      ret = SC.IndexSet.create(),
      first = contentIndexes.get('min'),
      last = contentIndexes.get('max'),
      i, j;

    for(i = first; i <= last; i++) {
      if(this.contentIndexIsGroup(null, null, i)) {
        ret.add(i * numColumns);
      } else {
        ret.add(i * numColumns, numColumns);
      }
    }

    return ret;
  },

  setAttributesForItem: function(item, index, attrs) {
    var del = this.get('contentDelegate'), 
        isGroupView = this.contentIndexIsGroup(this, this.get('content'), this.contentIndexForIndex(index)),
        ExampleView = this.exampleViewForItem(item, index),
        content = this.get("content"),
        contentIndex = this.contentIndexForIndex(index),
        columns = this.get('columns'),
        column = columns.objectAt(this.columnForIndex(index));

    // 
    // FIGURE OUT "NORMAL" ATTRIBUTES
    //
    attrs.createdFromExampleView = ExampleView;
    attrs.parentView = this.get('containerView') || this;
    attrs.contentIndex = contentIndex;
    attrs.owner = attrs.displayDelegate = this;
    attrs.content = item;
    attrs.page = this.page;
    attrs.layerId = this.layerIdFor(index);
    attrs.isEnabled = del.contentIndexIsEnabled(this, content, contentIndex);
    attrs.isSelected = del.contentIndexIsSelected(this, content, contentIndex);
    attrs.outlineLevel = del.contentIndexOutlineLevel(this, content, contentIndex);
    attrs.disclosureState = del.contentIndexDisclosureState(this, content, contentIndex);
    attrs.isVisibleInWindow = this.get('isVisibleInWindow');
    attrs.isGroupView = isGroupView;
    attrs.column = column;
    attrs.layout = this.layoutForContentIndex(index);
    if (!attrs.layout) attrs.layout = ExampleView.prototype.layout;

    return attrs;
  },

  /* 
    These methods are for translating back from transformed indexes
    (i.e. [0, 1, 2] => content[0] for 3 columns).
  */  

  columnForIndex: function(idx) {
    var columns = this.get('columns'),
      num = columns.get('length');
      
    return idx % num;
  },
  
  contentItemForIndex: function(idx) {
    return this.get('content').objectAt(this.contentIndexForIndex(idx));
  },
  
  contentIndexForIndex: function(idx) {
    var numColumns = this.getPath('columns.length');
    return Math.floor(idx / numColumns);
  },
  
  
  /*
    Takes the transformed index and breaks out the row and column indexes,
    then spits out the layout
  */

  layoutForContentIndex: function(idx) {
    var ret = arguments.callee.base.call(this, this.contentIndexForIndex(idx)),
      columns = this.get('columns'),
      col = this.columnForIndex(idx);

    ret.left = this.offsetForColumnIndex(col);
    ret.width = columns.objectAt(col).get('width')

    return ret;
  },
  
  /*
    Basically does the same thing as #rowOffsetForContentIndex but for columns
  */
  
  offsetForColumnIndex: function(idx) {
    var offsets = this._colOffsets,
      offset, columns;
  
    if(!offsets) {
      offsets = this._colOffsets = [];
    }
    
    if(!(offset = offsets[idx])) {
      columns = this.get('columns');
  
      if(idx === 0) {
        offsets[idx] = 0;
      } else {
        offsets[idx] = this.offsetForColumnIndex(idx - 1) + columns.objectAt(idx - 1).get('width');
      }
    }
  
    return offsets[idx];
  }

});
