// ==========================================================================
// Project:   SproutCore - JavaScript Application Framework
// Copyright: ©2006-2010 Sprout Systems, Inc. and contributors.
//            Portions ©2008-2010 Apple, Inc. All rights reserved.
// License:   Licensed under MIT license (see license.js)
// ==========================================================================

// sc_require('views/table_header');
// sc_require('views/data');

SC.TableView = SC.View.extend({
  
  classNames: ['sc-table-view'],
  
  horizontalScrollOffset: 0,

  /**
    The content object the table view will display. Usually an SC.ArrayController's content
    
    @type SC.Array
  */
  content: null,
  columns: null,
  
  /**
    The height of each row in the TableView
    
    @property {Number}
  */
  rowHeight:30,
  
  /**
    Spacing between rows (for borders or such)
    @property {Number}
  */
  rowSpacing: 1,
  
  selection:null,
  target:null,
  action:null,
  
  /**
    IF YES, a table header will be rendered. Note that if a table header is not rendered, 
    there will be no way to resize columns, reorder rows or sort from the TableView UI.
    
    @property {Boolean}
  */
  useHeaders: YES,
  
  /**
    The height of the header row
    
    @property {Number}
  */
  headerHeight:30,
  
  /**
    If set to NO the horizontal scroller will be suppressed.
    
    @property {Boolean}
  */
  hasHorizontalScroller:YES,
  
  /**
    An example ScrollView that will be used to paint the scrollpane of the tableView. 
    This is useful if your app has custom scrollbars.
    
    @property {SC.ScrollView}
  */
  exampleScrollView: SC.ScrollView,
  
  isSelectable: YES,
  isEditable: YES,
  // canEditContent: YES,
  
  /**
     Equivalent of the orderBy property of an SC.ArrayController. It is actually bound to the content orderBy property

     @private
   */
  sortDescriptor: null,
  sortDescriptorBinding: '*content.orderBy',
  
  createChildViews: function() {
    
    var header, data
    
    this._tableHeaderView = header = this.createChildView(SC.ScrollView.design({
      isVisibleBinding: SC.Binding.from('.useHeaders', this),
      headerHeightBinding: SC.Binding.from('.headerHeight',this),

      headerHeightDidChange: function(){
        var height = this.get('headerHeight'),
            layout = this.get('layout');
        if (height && height!==layout.height){
          layout.height=height;
        }
      }.observes('headerHeight'),

      layout: {
        left: 0,
        right: 0,
        bottom: 0,
        top: 0,
        height: this.get('headerHeight')
      },
      
      hasHorizontalScroller: NO,
      canScrollHorizontal: function() {
        return YES;
      }.property().cacheable(),
      horizontalScrollOffsetBinding: SC.Binding.from('.horizontalScrollOffset',this),
      
      borderStyle: SC.BORDER_NONE,
      contentView: SC.TableHeaderView.extend({
        layout:{top:0,left:0,right:0,bottom:0},
        table: this,
        columnsBinding: SC.Binding.from('.columns',this).oneWay(),
        sortDescriptorBinding: SC.Binding.from('.sortDescriptor',this)
       })
    }));
    
    this._dataView = data = this.createChildView(this.get('exampleScrollView').design({
      isVisible: YES,
      layout: {
        left:   0,
        right:  0,
        bottom: 0,
        top:    this.get('useHeaders')?this.get('headerHeight'):0
      },
      hasHorizontalScrollerBinding: SC.Binding.from('hasHorizontalScroller', this),
      borderStyle: SC.BORDER_NONE,
      contentView: Endash.DataView.design({
        classNames: ['sc-table-data-view'],
        table: this,
        rowHeight: this.get('rowHeight'),
        rowSpacing: this.get('rowSpacing'),
        isEditableBinding: SC.Binding.from('.isEditable',this),
        canEditContentBinding: SC.Binding.from('.canEditContent',this),
        targetBinding: SC.Binding.from('.target',this),
        actionBinding: SC.Binding.from('.action',this),
        canReorderContentBinding: SC.Binding.from('.canReorderContent',this),
        selectionBinding: SC.Binding.from('.selection',this),
        sortDescriptorBinding: SC.Binding.from('.sortDescriptor',this),
        columnsBinding: SC.Binding.from('.columns',this).oneWay(),
        contentBinding: SC.Binding.from('.content',this),
        delegate: this.get('delegate'),
        // isDropTarget: this.get('isDropTarget'),
        // isSelectable: this.get('isSelectable'),
        
        // exampleView: this.get('exampleView')
      }),

      autohidesVerticalScroller: NO,
      horizontalScrollOffsetBinding: SC.Binding.from('.horizontalScrollOffset',this)
    }));
    
    this.set('childViews', [header, data]);
    
    if(!this.columnsBinding)
    {
      this.notifyPropertyChange('columns');
    }
  },


  
  // /**
  //   Changes the sort descriptor based on the column that is passed and the current sort state
  // 
  //   @param {SC.TableColumn} column The column to sort by
  //   @param {String} sortState The desired sort state (ASC|DESC)
  // */
  sortByColumn: function(column, sortState) {
    if(sortState !== "ASC")
    {
      sortState = "ASC";
    }
    else
    {
      sortState = "DESC";
    }
    
    console.log('sortbycolumn')
    
    this.set('sortDescriptor', sortState + " " + column.get('key'));
  },

  /**
    Called by the TableHeaderView when a column is being dragged
    
    @param {SC.TableColumn} column the column being dragged
  */
  draggingColumn: function(column) {
    return
    this.$().addClass('reordering-columns');
    this._dragging = column;
  },
  
  /** 
    Called by the TableHeaderView when a column is being dragged. Adjusts the offset of the ghost
    
    @param {Number} offset The offset by which the column has been dragged
  */
  columnDragged: function(offset) {
    return
    this._ghostLeft += offset;
  },
  
  /** 
    Called by the TableHeaderView when a column has stopped dragging.
   */
  endColumnDrag: function() {
    this.$().removeClass('reordering-columns');
    if (!SC.none(this._ghost))
    {
      this.get('layer').removeChild(this._ghost);
    }
    this._ghost = this._blocker = null;
    this._ghostLeft = null;
    this.get('columns').notifyPropertyChange('[]');
  },
  
});
