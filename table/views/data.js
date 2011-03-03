// ==========================================================================
// Project:   SproutCore - JavaScript Application Framework
// Copyright: ©2006-2009 Sprout Systems, Inc. and contributors.
//            Portions ©2008-2009 Apple Inc. All rights reserved.
// License:   Licened under MIT license (see license.js)
// ==========================================================================

Endash.DataView = SC.ListView.extend(Endash.CollectionFastPath, {
  rowHeight: 30,
  rowSpacing: 1,
  
  // reloadIfNeeded: function(nowShowing, scrollOnly) {
  
  exampleView: SC.View.extend({
    isPoolable: YES,
    layerIsCacheable: YES,

    classNames: ['sc-dataview-row', 'sc-list-item-view'],
    
    sleepInDOMPool: function() {
      if(this._hasSlept)
        return
        
      // why is the layer getting detached and why does this stop it?
      this._sc_cell_views.forEach(function(c) {
        c.get('contentView').get('layer')
      }, this)
      
      this._hasSlept = YES
    },
    
    // we'll handle layout from hereon out thank you
    renderLayout: function(context, firstTime) {
      if(firstTime)
        sc_super()
    },
    
    render: function(context, firstTime) {
      var classArray = [];

      classArray.push((this.get('contentIndex') % 2 === 0) ? 'even' : 'odd');
      context.addClass(classArray);

      sc_super();
    },
    
    // reset classes
    awakeFromPool: function() {
      var layer = this.$();
      var eo = (this.get('contentIndex') % 2 === 0) ? 'even' : 'odd';
      layer.toggleClass('even', eo == 'even')
      layer.toggleClass('odd', eo == 'odd')
    }
    
  }),
  
  cellView: SC.View.extend({
    isPoolable: YES,
    classNames: ['table-cell']
  }),

  cellContentView: SC.LabelView.extend({
    isPoolable: YES,
    layerIsCacheable: YES,
    contentValueKeyBinding: '*column.key'
  }),

  
  init: function() {
    sc_super();

    // sc_super doesnt work with mixins, so cache and then call separately
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
      return ret;
    },
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

    // awake from the pool, etc.
    if (view.awakeFromPool) view.awakeFromPool(view.owningPool, this);
    
    var layer = view.get('layer')
    if (SC.platform.touch) {
      layer.style.top = ''
      this._repositionView(layer, attrs.layout)
    } else {
      layer.style.top = attrs.layout.top + "px"
    }
    
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
  },

  _createNewCellView: function(itemView, col, attrs) {
    var columns = this.get('columns'),
      column = columns.objectAt(col),
      E = this.cellViewForColumn(col),
      wrapper = this.get('cellView'),
      attrs = SC.clone(attrs);


    // attrs.parentView = itemView;
    attrs.layerId = itemView.get('layerId') + '-' + col
    attrs.column = column;
    attrs.columnIndex = col;
    attrs.contentValueKey = column.get('key');
    attrs.layout = this.layoutForColumn(col);
    (attrs.classNames || (attrs.classNames = [])).push('column-' + col);

    return wrapper.create(attrs, {
      childViews: ['contentView'],
      contentView: E.extend(attrs, {layerId: attrs.layerId + '-content', layout: {left: 10, right: 10}})
    })

    // return E.create(attrs);
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
  },
  
  _repositionView: function(layer, layout) {
    var transform = "";
    transform += 'translate3d(0px, ' + layout.top + 'px,0) ';
    if (layer) {
      layer.style.webkitTransform = transform;
      layer.style.webkitTransformOrigin = "top left";
    }
  }

});
