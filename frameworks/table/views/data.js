// ==========================================================================
// Project:   SproutCore - JavaScript Application Framework
// Copyright: ©2006-2009 Sprout Systems, Inc. and contributors.
//            Portions ©2008-2009 Apple Inc. All rights reserved.
// License:   Licened under MIT license (see license.js)
// ==========================================================================
sc_require('views/table_row')
sc_require('views/table_cell')


Endash.DataView = SC.ListView.extend(Endash.CollectionFastPath, {
  rowHeight: 30,
  rowSpacing: 1,
  
  exampleView: SC.TableRowView,
  cellView: Endash.TableCellView,
  cellContentView: SC.LabelView.extend({
    isPoolable: YES,
    layerIsCacheable: YES,
    contentValueKeyBinding: '*column.key'
  }),

  
  init: function() {
    // sc_super doesnt work with mixins, so cache and then call separately
    this._allocateItemView = this.allocateItemView;
    this.allocateItemView = function(exampleView, attrs) {
      var ret = this._allocateItemView(exampleView, attrs),
        columns = this.get('columns'),
        column, cell, cells = [], cellViews;

      cellViews = ret._sc_cell_views = [];

      for(var i = 0, len = columns.get('length'); i < len; i++) {
        cell = this._createNewCellView(ret, i, attrs);
        cell.updateLayerLocation()
        cellViews[i] = cell;
        cells.push(cell);
      }

      ret.set('childViews', cells);
      
      // ret.layoutCells()
      return ret;
    }
    
    sc_super();

  },

  cellViewForColumn: function(col) {
    var columns = this.get('columns'),
      column = columns.objectAt(col),
      ret;

    if(ret = column.get('exampleView')) return ret;

    return this.get('cellContentView');
  },

  wakePooledView: function(view, attrs) {
    // configure
    this.configureItemView(view, attrs);

    var layer = view.get('layer')
    if (SC.platform.touch) {
      layer.style.top = ''
      this._repositionView(layer, attrs.layout)
    } else {
      layer.style.top = attrs.layout.top + "px"
    }
    
    attrs.parentView = view
    
    var columns = this.get('columns'),
      column, cell, E;
      
    for(var i = 0, len = columns.get('length'); i < len; i++) {
      cell = view._sc_cell_views[i];

      if (cell.isPoolable) {
        cell.beginPropertyChanges();
        cell.set('contentIndex', attrs.contentIndex);
        cell.set('layerId', view.get('layerId') + '-' + i)
        cell.endPropertyChanges();

        cell = cell.get('contentView')
        cell.beginPropertyChanges();
        cell.set('contentIndex', attrs.contentIndex);
        cell.set('content', attrs.content);
        cell.set('layerId', view.get('layerId') + '-' + i + '-content')
        cell.endPropertyChanges();
      } else {
        cell.destroy();
        cell = this._createNewCellView(view, i, attrs);
        view._sc_cell_views[i] = cell;
        view.appendChild(cell);
      }
    }
    
    // awake from the pool, etc.
    if (view.awakeFromPool) view.awakeFromPool(view.owningPool, this);
    
  },

  _createNewCellView: function(itemView, col, attrs) {
    var columns = this.get('columns'),
      column = columns.objectAt(col),
      E = this.cellViewForColumn(col),
      wrapper = this.get('cellView'),
      attrs = SC.clone(attrs);


    attrs.parentView = itemView;
    attrs.layerId = itemView.get('layerId') + '-' + col
    attrs.column = column;
    attrs.columnIndex = col;
    attrs.contentValueKey = column.get('key');
    attrs.layout = this.layoutForColumn(col);
    (attrs.classNames || (attrs.classNames = [])).push('column-' + col);

    return wrapper.create(attrs, {
      childViews: ['contentView'],
      contentView: E.extend(attrs, {parentView: null, layerId: attrs.layerId + '-content', layout: {left: 10, right: 10}})
    })
  },
  
  columnWidthsChanged: function(column) {
    this._columnOffsets = (this._columnOffsets || []).slice(0, column)
  },
  
  layoutForColumn: function(col) {
    var columns = this.get('columns'),
      column = columns.objectAt(col || 0),
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
  },
  
  _repositionView: function(layer, layout) {
    var transform = "";
    transform += 'translate3d(0px, ' + layout.top + 'px,0) ';
    if (layer) {
      layer.style.webkitTransform = transform;
      layer.style.webkitTransformOrigin = "top left";
    }
  },
  
  computeLayout: function() {
    var ret = sc_super();
    ret.minWidth = this.offsetForColumn((this.get('columns') || []).get('length'))
    this.set('calculatedWidth', ret.minWidth)
    this.set('calculatedHeight',ret.minHeight);
    return ret ;
  },
  
  /** @private */
  _sctv_contentDidChange: function() {
    this.reload(null);
  }.observes('*content.[]'),

  // /** @private */
  _sctv_columnsDidChange: function() {
    var columns = this.get('columns');
    if(SC.none(columns) || columns.get('length') < 1 || columns === this._columns)
    {
      return this;
    }

    var observer = this._sctv_columnsRangeObserver;
    var func = this._sctv_columnsRangeDidChange;

    if(this._columns)
    {
      this._columns.removeRangeObserver(observer);
    }

    observer = columns.addRangeObserver(null, this, func, null, true);      
    this._sctv_columnsRangeObserver = observer ;

    this._columns = columns;
    this._sctv_columnsRangeDidChange()
  }.observes('columns'),
  
  _sctv_columnsRangeDidChange: function(content, object, key, indexes) {
    if (!object && (key === '[]')) {
      this._columnsNeedReloading(indexes.get('min'))
      this.computeLayout();
    } else {
      this.contentPropertyDidChange(object, key, indexes);
    }
  },
  
  _columnsNeedReloading: function(col) {
    // redraw!!!
    
    this.reloadIfNeeded(SC.IndexSet.create())
    this.clearDOMPools()
    this.reloadIfNeeded()
  },

});
