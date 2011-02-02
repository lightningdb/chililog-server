// ==========================================================================
// Project:   SproutCore - JavaScript Application Framework
// Copyright: ©2006-2009 Sprout Systems, Inc. and contributors.
//            Portions ©2008-2009 Apple Inc. All rights reserved.
// License:   Licened under MIT license (see license.js)
// ==========================================================================

SC.DataView = SC.ListView.extend({
  isEditable: YES,
  canEditContent: YES,
  //
  //   /**
  //     Setting this to 'undefined' lets the 'contentValueKey' property
  //     be determined by SC.ListItemView instead of SC.ListItem.  It forces
  //
  //       del.getDelegateProperty('contentValueKey', del)
  //
  //     in SC.ListItemView.displayValue() to ask itself instead of 'del' for
  //     the 'contentValueKey'.
  //   */
  //   contentValueKey: undefined,
  //
  //
  //   /**
  //     The view that will wrap the cells of a row
  //   */
  exampleView: SC.View.extend(SC.Control, {
    isReusableInCollections: YES,

    classNames: ['sc-dataview-row', 'sc-list-item-view'],
    //
    // render: function(context, firstTime) {
    //   var content = this.get('content'),
    //     classArray = [];
    //
    //   // add alternating row classes
    //   classArray.push((this.get('contentIndex') % 2 === 0) ? 'even' : 'odd');
    //   context.addClass(classArray);
    //   sc_super();
    // }
  }),

  cellView: SC.ListItemView.extend({
    outlineLevel: -1,
    contentIsEditable: YES
    // isReusableInCollections: YES
  }),

  cellViewForColumn: function(col) {
    var columns = this.get('columns'),
      column = columns.objectAt(col),
      ret

    if(ret = column.get('exampleView'))
      return ret

    return this.get('cellView')
  },

  _createNewItemView: function(E, idx, item, attrs){
    var ret = sc_super(),
      columns = this.get('columns'),
      column,
      cell,
      cells = []

    var cellViews = ret._sc_cell_views = []

    for(var i = 0, len = columns.get('length'); i < len; i++)   {
      cell = this._createNewCellView(idx, i, {parentView: ret})
      cellViews[i] = cell
      cells.push(cell)
    }

    ret.set('childViews', cells)
    ret.replaceLayer();

    return ret
  },

  _retrieveViewFromPool: function(viewPoolKey, idx, attrs){
    var ret = sc_super(),
      content = this.get('content'),
      item = content.objectAt(idx),
      columns = this.get('columns'),
      column,
      cell,
      attrs = {},
      E,
      view;

    if(!ret)
      return ret

    for(var i = 0, len = columns.get('length'); i < len; i++)   {

      cell = ret._sc_cell_views[i]
      E = this.cellViewForColumn(i)

      if (E  &&  E.prototype.isReusableInCollections) {

        // Tell the view it's about to be re-used.
        reuseFunc = cell.prepareForReuse;
        if (reuseFunc) reuseFunc.call(ret);

        // Set the new state.  We'll set content last, because it's the most
        // likely to have observers.
        cell.beginPropertyChanges();
        cell.set('contentIndex', idx);
        cell.set('content', item)
        cell.endPropertyChanges()

      } else {
        ret.removeChild(cell)
        cell = this._createNewCellView(idx, i)
        ret._sc_cell_views[i] = cell
        ret.appendChild(cell)
      }
    }

    return ret


  },

  _createNewCellView: function(idx, col, attrs) {
    var columns = this.get('columns'),
      content = this.get('content'),
      item = content.objectAt(idx),
      column = columns.objectAt(col),
      E = this.cellViewForColumn(col),
      cellViews

    if(!attrs)
      attrs = {}

    attrs.classNames = 'column-' + col
    attrs.contentIndex = idx
    attrs.content = item
    attrs.contentValueKey = column.get('key')


    ret = this.createItemView(E, idx, item, attrs);

    console.log(ret.get('outlineLevel'))

    return ret;
  }

});
