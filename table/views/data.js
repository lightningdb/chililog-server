// ==========================================================================
// Project:   SproutCore - JavaScript Application Framework
// Copyright: ©2006-2009 Sprout Systems, Inc. and contributors.
//            Portions ©2008-2009 Apple Inc. All rights reserved.
// License:   Licened under MIT license (see license.js)
// ==========================================================================

Endash.DataView = SC.ListView.extend(Endash.CollectionFastPath, {
  rowHeight: 30,
  
  backgroundColor: 'grey',
  
  exampleView: SC.View.extend({
    backgroundColor: 'white',
    isPoolable: YES,
    layerIsCacheable: YES,

    classNames: ['sc-dataview-row', 'sc-list-item-view'],
    
    sleepInDOMPool: function() {
      if(this._hasSlept)
        return
        
      this._sc_cell_views.forEach(function(c) {
        c.get('layer')
      }, this)
      
      this._hasSlept = YES
    },
    
    renderLayout: function(context, firstTime) {
      if(firstTime)
        sc_super()
    }
  }),

  cellView: SC.LabelView.extend({
    backgroundColor: 'white',
    isPoolable: YES,
    layerIsCacheable: YES,
    contentValueKeyBinding: '*column.key'
  }),
  
  init: function() {
    sc_super();

    this._allocateItemView = this.allocateItemView;
    
    this.allocateItemView = function(exampleView, attrs) {
      var ret = this._allocateItemView(exampleView, attrs),
        columns = this.get('columns'),
        column, cell, cells = [], cellViews;

      cellViews = ret._sc_cell_views = [];

      for(var i = 0, len = columns.get('length'); i < len; i++) {
        cell = this._createNewCellView(ret, i, attrs);
        cellViews[i] = cell;
        cells.push(cell);
      }

      ret.set('childViews', cells);
      // ret.replaceLayer();

      return ret;
    },
  },
  
  cellViewForColumn: function(col) {
    var columns = this.get('columns'),
      column = columns.objectAt(col),
      ret;

    if(ret = column.get('exampleView')) return ret;

    return this.get('cellView');
  },



  wakePooledView: function(view, attrs) {
    // configure
    this.configureItemView(view, attrs);

    // awake from the pool, etc.
    if (view.awakeFromPool) item.awakeFromPool(view.owningPool, this);
    
    var layer = view.get('layer')
    layer.style.top = attrs.layout.top + "px"
    
    return
    
    var columns = this.get('columns'),
      column, cell, E;
      
    for(var i = 0, len = columns.get('length'); i < len; i++) {
      cell = view._sc_cell_views[i];

      if (cell.isPoolable) {
        cell.beginPropertyChanges();
        cell.set('contentIndex', attrs.contentIndex);
        // cell.set('content', attrs.content);
        cell.set('layerId', view.get('layerId') + '-' + i)
        cell.endPropertyChanges();
      } else {
        cell.destroy();
        cell = this._createNewCellView(view, i, attrs);
        view._sc_cell_views[i] = cell;
        view.appendChild(cell);
      }
    }
  },

  _createNewCellView: function(itemView, col, attrs) {
    var columns = this.get('columns'),
      column = columns.objectAt(col),
      E = this.cellViewForColumn(col),
      attrs = SC.clone(attrs);

    attrs.parentView = itemView;
    attrs.layerId = itemView.get('layerId') + '-' + col
    attrs.column = column;
    attrs.columnIndex = col;
    attrs.contentValueKey = column.get('key');
    attrs.layout = this.layoutForColumn(col);
    (attrs.classNames || (attrs.classNames = [])).push('column-' + col);
    
    return E.create(attrs);
  },
  
  layoutForColumn: function(col) {
    var columns = this.get('columns'),
      column = columns.objectAt(col),
      width = column.get('width');
      
    return {
      left: this.offsetForColumn(col),
      width: this.widthForColumn(col)
    };
  },
  
  offsetForColumn: function(col) {
    if(col === 0) return 0;

    var offsets = (this._columnOffsets || (this._columnOffsets = []));
    
    offsets[col] = this.offsetForColumn(col - 1) + this.widthForColumn(col - 1);
    
    return offsets[col];
  },
  
  widthForColumn: function(col) {
    var columns = this.get('columns'),
      column = columns.objectAt(col),
      width = column.get('width');
      
    return width;
  }

});
