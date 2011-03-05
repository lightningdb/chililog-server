// sc_require('views/table_cell')

SC.TableRowView = SC.View.extend({
  backgroundColor: 'white',
  isPoolable: YES,
  layerIsCacheable: YES,

  columnsBinding: '.parentView.columns',

  classNames: ['sc-dataview-row', 'sc-list-item-view'],
  
  init: function() {
    sc_super();
    
    var columns = this.getPath('parentView.columns'),
      column, cell, cells = [], cellViews;

    cellViews = this._sc_cell_views = [];

    for(var i = 0, len = columns.get('length'); i < len; i++) {
      cell = this._createNewCellView(i);
      cell.updateLayerLocation();
      cellViews[i] = cell;
      cells.push(cell);
    }

    this.set('childViews', cells);
  },

  render: function(context, firstTime) {
    if(firstTime) {
      var classArray = [];

      classArray.push((this.get('contentIndex') % 2 === 0) ? 'even' : 'odd');
      context.addClass(classArray);
    }
    sc_super();
  },
  
  // we'll handle layout from here-on-out thank you
  renderLayout: function(context, firstTime) {
    if(firstTime) sc_super();
  },
  
  layoutForCell: function(col) {
    return this.get('parentView').layoutForColumn(col);
  },

  layoutAllCells: function() {
    this.columnWidthDidChange(0);
  },

  columnWidthDidChange: function(idx) {
    var cells = (this._sc_cell_views || []);
    for(var i = idx; i < cells.length; i++) {
      this.setPositionForCell(i);
    }
  },
  
  columnsDidChange: function() {
    if(this.get('columns')) this.layoutAllCells();
  }.observes('columns'),
  
  awakeFromPool: function() {
    // striping
    var eo = (this.get('contentIndex') % 2 === 0) ? 'even' : 'odd';
    this.get('layer').className = this.get('classNames').join(" ") + " " + eo;

    this.updateCells();
  },
  
  updateCells: function() {
    // cell updating
    var columns = this.getPath('parentView.columns'),
      column, cell, E;
      
    for(var i = 0, len = columns.get('length'); i < len; i++) {
      cell = this._sc_cell_views[i];
      if (cell.isPoolable) {
        column = columns.objectAt(i)
        this.setPositionForCell(i)
        this.updateCell(i, column);
      } else {
        cell.destroy();
        cell = this._createNewCellView(i);
        this._sc_cell_views[i] = cell;
        this.appendChild(cell);
      }
    }
  },
  
  updateCell: function(idx, column) {
    // this is faster than using bindings
    
    var cellView = this._sc_cell_views[idx];
    var contentView = cellView.get('contentView');
    
    cellView.beginPropertyChanges();
    contentView.beginPropertyChanges();
    
    if(cellView.get('column') != column) {
      cellView.set('column', column);
      cellView.set('columnIndex', idx);
      contentView.set('column', column);
      contentView.set('columnIndex', idx);
    }
    
    cellView.set('contentIndex', this.get('contentIndex'));
    cellView.set('layerId', this.get('layerId') + '-' + idx);
    contentView.set('contentIndex', this.get('contentIndex'));
    contentView.set('content', this.get('content'));
    contentView.set('layerId', this.get('layerId') + '-' + idx + '-content');

    contentView.endPropertyChanges();
    cellView.endPropertyChanges();
    return;
  },
  
  sleepInDOMPool: function() {
    if(this._hasSlept) return
      
    // why is the layer getting detached and why does this stop it?
    this._sc_cell_views.forEach(function(c) {
      c.get('contentView').get('layer');
    }, this);
    
    this._hasSlept = YES;
  },

  setPositionForCell: function(i) {
    var layout = this.layoutForCell(i),
      view = this._sc_cell_views[i],
      layer = view.get('layer'),
      transform;

    layer = view.get('layer');
    if (layer) {
      if(SC.isTouch) {
        transform = 'translate3d(' + layout.left + 'px, 0px,0) ';
        layer.style.left = '';
        layer.style.webkitTransform = transform;
        layer.style.webkitTransformOrigin = "top left";
      } else {
        layer.style.left = layout.left + "px";
      }
      layer.style.width = layout.width + "px";
    }
  },
  
  _createNewCellView: function(col) {
    var columns = this.getPath('parentView.columns'),
      column = columns.objectAt(col),
      E = this.get('parentView').cellViewForColumn(col),
      wrapper = this.get('parentView').get('cellView'),
      attrs = {};
    
    attrs.parentView = this;
    attrs.layerId = this.get('layerId') + '-' + col;
    
    attrs.column = column;
    attrs.columnIndex = col;
    attrs.content = this.get('content');
    attrs.contentIndex = this.get('contentIndex');
    
    attrs.contentValueKey = column.get('key');
    attrs.layout = this.layoutForCell(col);
    (attrs.classNames || (attrs.classNames = [])).push('column-' + col);

    return wrapper.create(attrs, {
      childViews: ['contentView'],
      contentView: E.extend(attrs, {
        parentView: null, 
        layerId: attrs.layerId + '-content', 
        layout: {left: 10, right: 10}
      })
    });
  }

  
});