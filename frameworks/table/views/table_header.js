sc_require('views/table_header_cell');

// ==========================================================================
// Project:   SproutCore - JavaScript Application Framework
// Copyright: ©2006-2010 Sprout Systems, Inc. and contributors.
//            Portions ©2008-2010 Apple, Inc. All rights reserved.
// License:   Licensed under MIT license (see license.js)
// ==========================================================================
SC.TableHeaderView = SC.TableRowView.extend({
  
  /** @private */
  classNames: ['sc-table-header'],
  
  exampleView: SC.TableHeaderCellView,

  headerViewForColumn: function(col) {
    var columns = this.get('columns'),
      column = columns.objectAt(col),
      ret;

    if(ret = column.get('exampleHeaderView')) return ret;

    return this.get('exampleView');
  },
  
  _createNewCellView: function(col) {
    var columns = this.get('columns'),
      column = columns.objectAt(col),
      E = this.headerViewForColumn(col),
      layout = SC.clone(E.prototype.layout || {});
      
    layout.left = this.layoutForView(col).left;
    layout.width = this.layoutForView(col).width;

    return this.createChildView(E, {
      column: column,
      columnIndex: col,
      layoutDelegate: this,
      layoutIndex: col,
      delegate: this,
      first: col === 0,
      layout: layout
    });
  },
  
  shouldLayoutView: function(view, i) {
    if(this._dragging == view) {
      return NO;
    } else {
      return YES;
    }
  },
  
  _updateCell: function(idx, column) {},
  
  // drag to reorder
  /** @private */
  headerDidBeginDrag: function(view, offset, evt) {
    this._dragging = view;
    // this.get('table').draggingColumn(view.get('column'));
    // SC.$(view).addClass('dragging');
  },
  
  /** @private */
  headerWasDragged: function(view, offset, evt) {
    this.adjustDrag(view, offset);
    // this.get('table').columnDragged(offset);
  },
  
  /** @private */
  headerDidEndDrag: function(view, evt) {
    // this.get('table').endColumnDrag();
    console.log('ending drag')
    this._dragging = null;
    this._totalOffset = null;
    this.layoutViewsFrom(0);
    SC.$(view).removeClass('dragging');
  },
  
  // 
  /** @private */
  thumbWasDragged: function(view, offset, evt){
    // this._sl_layoutChildViews();
  },
  
  /** @private */
  adjustDrag: function(view, offset) {
    var direction = this.get('layoutDirection');
    
    var put = (this._totalOffset === 0 ? 0 : (this._totalOffset || this.offsetForView(view.get('columnIndex')))) + offset;
    // this.repositionView(view, {left: put});
    this._totalOffset = put;
    this.set('_draggingOffset', put);
    
    var childViews = this._layoutViews;
    var idx = childViews.indexOf(view);
    var view2, idx2;
    var columns = this.get('columns')

    var layout = {left: put, width: this.thicknessForView(idx)}
    view.adjust(layout);

  
    if(offset < 1 && idx > 0)
    {
      idx2 = idx - 1;
    }
    else 
    {
      if(offset > 1 && idx < childViews.get('length') - 1)
      {
        idx2 = idx + 1;
      }
    }

    view2 = childViews.objectAt(idx2);
    if(!view2 || view2.spacer || !columns.objectAt(idx2).get('isReorderable'))
    {
      return;
    }
      
    var centerPoint = this.offsetForView(idx2, view2) + (this.thicknessForView(idx2, view2) / 2);

    if(offset < 1 && (view.get('frame').x > centerPoint))
    {
      return;
    }
    else 
    {
      if(offset > 1 && (view.get('frame').x + view.get('frame').width < centerPoint))
      {
        return;
      }
    }
    
    this.swapViews(view, view2);
  },
  
  /** @private */
  swapViews: function(view1, view2) {
    var childViews = this._layoutViews;
    var columns = this.get('columns');
  
    var index1 = childViews.indexOf(view1);
    var index2 = childViews.indexOf(view2);
    var column1 = columns.objectAt(index1);
    var column2 = columns.objectAt(index2);
    
    view1.set('columnIndex', index2);
    view2.set('columnIndex', index1);
  
    childViews.beginPropertyChanges();
    columns.beginPropertyChanges();

    childViews.replace(index1, 1, view2);
    childViews.replace(index2, 1, view1);
    columns.replace(index1, 1, [ column2 ]);
    columns.replace(index2, 1, [ column1 ]);

    childViews.endPropertyChanges();
    columns.endPropertyChanges();
  }
});