SC.TableRowView = SC.View.extend({
  isPoolable: YES,
  layerIsCacheable: YES,

  columnsBinding: '.parentView.columns',

  classNames: ['sc-dataview-row', 'sc-list-item-view'],

  render: function(context, firstTime) {
    var classArray = [];

    classArray.push((this.get('contentIndex') % 2 === 0) ? 'even' : 'odd');
    context.addClass(classArray);

    sc_super();
  },
  
  // we'll handle layout from here-on-out thank you
  renderLayout: function(context, firstTime) {
    if(firstTime)
      sc_super()
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
    if(this.get('columns'))
      this.layoutAllCells();
  }.observes('columns'),
  
  // reset classes
  awakeFromPool: function() {
    var layer = this.$();
    var eo = (this.get('contentIndex') % 2 === 0) ? 'even' : 'odd';
    layer.toggleClass('even', eo == 'even')
    layer.toggleClass('odd', eo == 'odd')
  },
  
  sleepInDOMPool: function() {
    if(this._hasSlept)
      return
      
    // why is the layer getting detached and why does this stop it?
    this._sc_cell_views.forEach(function(c) {
      c.get('contentView').get('layer')
    }, this)
    
    this._hasSlept = YES
  },

  setPositionForCell: function(i) {
    var layout = this.layoutForCell(i),
      view = this._sc_cell_views[i],
      layer = view.get('layer'),
      transform;

    layer = view.get('layer')
    if (layer) {
      if(SC.isTouch) {
        transform = 'translate3d(' + layout.left + 'px, 0px,0) ';
        layer.style.left = ''
        layer.style.webkitTransform = transform;
        layer.style.webkitTransformOrigin = "top left";
      } else {
        layer.style.left = layout.left + "px"
      }
      layer.style.width = layout.width + "px";
    }
  }

  
});