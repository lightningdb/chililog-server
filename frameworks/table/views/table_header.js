sc_require('views/table_header_cell');

// ==========================================================================
// Project:   SproutCore - JavaScript Application Framework
// Copyright: ©2006-2010 Sprout Systems, Inc. and contributors.
//            Portions ©2008-2010 Apple, Inc. All rights reserved.
// License:   Licensed under MIT license (see license.js)
// ==========================================================================
/*globals SC Endash */

SC.TableHeaderView = SC.TableRowView.extend({
  
  /** @private */
  classNames: ['sc-table-header'],
  
  exampleView: SC.TableHeaderCellView,
  
  thumbView: Endash.ThumbView.extend({
    delegateBinding: '.parentView.parentView',
    columnBinding: '.parentView.column',
    layout: {
      top: 0, bottom: 0, right: 0, width: 15
    },
    isEnabledBinding: '.column.isResizable'
  }),
  
  widthsDidChange: function(object, key, value) {
    var columns = this.get('columns'),
      width = columns.get('@sum(width)'),
      idx;
      
    if(width == this._width && !this._dragging) return;
    this._width = width;

    if(key == '[]') {
      idx = 0;
    } else {
      idx = columns.objectAt(object);
    }
    
    this.widthDidChangeForIndex(idx);
    
    width = columns.get('@sum(width)');
    this.set('totalWidth', width);
    this.adjust('minWidth', width);
    this.set('calculatedWidth', width);
  }.observes('*columns.@each.width'),

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
      layout: layout,
      thumbView: this.get('thumbView')
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
  
  /** @private */
  adjustDrag: function(view, offset) {
    var direction = this.get('layoutDirection');
    
    var put = (this._totalOffset === 0 ? 0 : (this._totalOffset || this.offsetForView(view.get('columnIndex')))) + offset;

    this._totalOffset = put;
    this.set('_draggingOffset', put);
    
    var childViews = this._layoutViews;
    var idx = childViews.indexOf(view);
    var view2, idx2;
    var columns = this.get('columns');

    view.adjust('left', put);

  
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
  },
  
  mouseDown: function(evt) {
    var view = $(evt.target).view()[0];
    
    if(view.instanceOf(this.get('thumbView'))) {
      if (!view.get('isEnabled')) return NO ;
  
      var responder = this.getPath('pane.rootResponder') ;
      if (!responder) return NO ;
    
      this._offset = {x: 0, y: 0};
    
      responder.dragDidStart(this) ;
      
      this._thumbDragging = view;
    
      view.$().toggleClass('dragging', true);
    
      this._mouseDownX = this._lastX = evt.pageX ;
      this._mouseDownY = this._lastY = evt.pageY ;
  
      return YES ;
    } else {
      this._initialX = evt.pageX;
    }
  },
  
  
  mouseDragged: function(evt) {
    var view = $(evt.target).view()[0];
    
    if(this._thumbDragging) {
      view = this._thumbDragging;
      
      if (!view.get('isEnabled')) return NO ;
      var offset = this._offset;

      offset.x = evt.pageX - this._lastX;
      offset.y = evt.pageY - this._lastY;

      this._lastX = evt.pageX;
      this._lastY = evt.pageY;

      var column = view.get('column'),
        width = column.get('width') || 100,
        minWidth = column.get('minWidth') || 20,
        maxWidth = column.get('maxWidth'),
        newWidth;

      newWidth = Math.max(minWidth, width + offset.x);
      if(maxWidth)
      {
        newWidth = Math.min(maxWidth, newWidth);
      }

      column.set('width', newWidth);
      
      
      return YES;
    } else {
      
      var x = evt.pageX;

      if(!this._dragging) {
        if(Math.abs(this._initialX - x) < 6) {
          return;
        } else {
          while(!view.instanceOf(this.get('exampleView'))) {
            view = view.get('parentView');
          }
          
          if (!view.getPath('column.isReorderable')){
            return YES;
          }
          
          view._dragging = YES;
          view.set('dragging', YES);
          this._dragging = view;
          return YES;
        }
      }

      var lastX = this._lastX;
      if(SC.none(lastX)) {
        lastX = this._lastX = x;
      }

      offset = x - lastX;
      this._lastX = x;
      
      this.adjustDrag(this._dragging, offset);

      return YES;
    }
  },
  
  /** @private */
  mouseUp: function(evt) {
    var view = $(evt.target).view()[0];

    if(this._thumbDragging) {
      this._thumbDragging = NO;
      if (!view.get('isEnabled')) return NO ;
      this._lastX = this._lastY = this._offset = this._mouseDownX = this.mouseDownY = null;
      view.$().removeClass('dragging');
      return YES;
    } else {
      if(this._dragging) {
        this._dragging.set('dragging', NO);
        SC.$(this._dragging).removeClass('dragging');
        this._dragging = null;
        this._totalOffset = null;
        this.layoutViewsFrom(0);
      } else {
        while(!view.instanceOf(this.get('exampleView'))) {
          view = view.get('parentView');
        }
        this.get('table').sortByColumn(view.get('column'), view.get('sortState'));
      }
      this._lastX = null;
    }
  },
  
  
  
  
  // ..........................................................
   // touch support
   // 
   touchStart: function(evt){
     return this.mouseDown(evt);
   },

   touchEnd: function(evt){
     return this.mouseUp(evt);
   },

   touchesDragged: function(evt, touches) {
     return this.mouseDragged(evt);
   },

   touchEntered: function(evt){
     return this.mouseEntered(evt);
   },

   touchExited: function(evt){
     return this.mouseExited(evt);
   }
  
});