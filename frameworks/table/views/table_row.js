sc_require('views/table_cell')

SC.TableRowView = SC.View.extend(SC.SimpleLayout, {
  isPoolable: YES,
  layerIsCacheable: YES,
  thicknessKey: 'width',

  columnsBinding: '.parentView.columns',

  classNames: ['sc-dataview-row'],
  
  isSelectedDidChange: function() {
    var isSelected = this.get('isSelected');
    if(isSelected) {
      this.$().addClass('sel');
    } else {
      this.$().removeClass('sel');
    }
  }.observes('isSelected'),
  
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
 
  /**
    @private
    Create the cell views when columns gets set
  */
  _trv_columnsDidChange: function() {
    this.beginPropertyChanges();
    var cellViews = this._sc_cell_views || (this._sc_cell_views = {}),
      columns = this.get('columns'),
      // numCells = cellViews.get('length'),
      numCells = this._layoutViews ? this._layoutViews.get('length') : 0,
      numCols = columns.get('length'),
      i, cell;
      
    if(!this.get('columns')) return;

    this.set('thicknesses', this.get('columns'));
    
    // for(i = numCols; i < numCells; i++) {
    //   cellViews[i].destroy();
    //   cellViews.removeAt(i);
    // }
    
    for(i = numCells; i < numCols; i++) {
      cell = this._createNewCellView(i);
      cellViews[SC.guidFor(columns.objectAt(i))] = cell;
      this.appendChild(cell);
    }
    
    this.endPropertyChanges();
    this._updateCells();
  }.observes('columns'),
  
  /**
    @private
    Ovveride from simpleLayout to map the index to the right view
  */
  
  viewForIndex: function(i) {
    var columns = this.get('columns'),
      column = columns.objectAt(i),
      views = this._sc_cell_views;
    return views[SC.guidFor(column)];
  },

  /**
    @private
    Looping through the columns and calling update for each
  */
  _updateCells: function() {
    var columns = this.get('columns'),
      column, cell, E;
      
    for(var i = 0, len = columns.get('length'); i < len; i++) {
      column = columns.objectAt(i);
      this._updateCell(i, column);
    }
  },
  
  /**
    @private
    Updating the cell for the given column with the new content
  */
  _updateCell: function(idx, column) {
    // this is faster than using bindings
    
    var cellView = this._sc_cell_views[SC.guidFor(column)];
    var contentView = cellView.get('contentView');
    
    var content = this.get('content')
    // var key = column.get('key')
    // if(key) content = content.get(column.get('key')) || content
    
    cellView.beginPropertyChanges();
    contentView.beginPropertyChanges();

    // column is the same, position might not be
    cellView.set('columnIndex', idx);
    contentView.set('columnIndex', idx);
    
    cellView.set('contentIndex', this.get('contentIndex'));
    contentView.set('contentIndex', this.get('contentIndex'));
    contentView.set('content', content);

    contentView.endPropertyChanges();
    cellView.endPropertyChanges();
    return;
  },
  
  /**
    @private
    Set our classnames
  */
  awakeFromPool: function() {
    // striping
    var eo = (this.get('contentIndex') % 2 === 0) ? 'even' : 'odd';
    this.get('layer').className = this.get('classNames').join(" ") + " " + eo;
    
    if(this.get('isSelected')) {
      this.$().addClass('sel');
    } else {
      this.$().removeClass('sel');
    }
  },
  
  /**
    @private
    Manual repositioning for speed
  */
  repositionView: function(view, layout) {
    if(!view) return
    
    var layer = view.get('layer'),
      transform;

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
    } else {
      view.adjust(layout);
    }
  },
  
  /**
    @private
    Creates the cell views
  */
  _createNewCellView: function(col) {
    var columns = this.get('columns'),
      column = columns.objectAt(col),
      E = this.get('parentView').cellViewForColumn(col),
      wrapper = this.get('parentView').get('cellView'),
      attrs = {};
      
      console.log(col, E)
      
    var content = this.get('content')
    // var key = column.get('key')
    // if(key) content = content.get(column.get('key')) || content

      
    attrs.parentView = this;
    attrs.column = column;
    attrs.columnIndex = col;
    attrs.content = content;
    attrs.contentIndex = this.get('contentIndex');
    attrs.contentValueKey = column.get('key');
    (attrs.classNames || (attrs.classNames = [])).push('column-' + col);

    return wrapper.create(attrs, {
      layoutDelegate: this,
      layoutIndex: col,
      childViews: ['contentView'],
      contentView: E.extend(attrs, {
        parentView: null, 
        layout: {left: 10, right: 10}
      })
    });
  }

  
});