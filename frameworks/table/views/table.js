sc_require('views/table_header');
sc_require('views/data');

// ==========================================================================
// Project:   SproutCore - JavaScript Application Framework
// Copyright: ©2006-2009 Sprout Systems, Inc. and contributors.
//            Portions ©2008-2009 Apple Inc. All rights reserved.
// License:   Licened under MIT license (see license.js)
// ==========================================================================


/*globals SC Endash */

/**
  @class

  TableView

  @extends SC.View
  @author Christopher Swasey
*/

/*globals Endash */

SC.TableView = SC.View.extend({
  
  classNames: ['sc-table-view'],
  
  horizontalScrollOffset: 0,

  /**
    An array of content objects
    
    This array should contain the content objects you want the collection view 
    to display.  An item view (based on the exampleView view class) will be 
    created for each content object, in the order the content objects appear 
    in this array.
    
    If you make the collection editable, the collection view will also modify 
    this array using the observable array methods of SC.Array.
    
    Usually you will want to bind this property to a controller property 
    that actually contains the array of objects you to display.
    
    @type {SC.Array}
  */
  content: null,
  
  /** @private */
  contentBindingDefault: SC.Binding.multiple(),
  
  /**
    The columns array. Can be a regular array or can be bound to a controller.
    Must be an array of SC.TableColumn
    
    @type SC.Array
  */
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
  
  /**
     Equivalent of the orderBy property of an SC.ArrayController. It is actually bound to the content orderBy property

     @private
   */
  sortDescriptor: null,
  sortDescriptorBinding: '*content.orderBy',
  
  /**
    Indexes of selected content objects.  This SC.SelectionSet is modified 
    automatically by the collection view when the user changes the selection 
    on the collection.
    
    Any item views representing content objects in this set will have their 
    isSelected property set to YES automatically.
    
    @type {SC.SelectionSet}
  */
  selection: null,
  
  /** 
    Allow user to select content using the mouse and keyboard.
    
    Set this property to NO to disallow the user from selecting items. If you 
    have items in your selectedIndexes property, they will still be reflected
    visually.
    
    @type {Boolean}
  */
  isSelectable: YES,
  
  /** @private */
  isSelectableBindingDefault: SC.Binding.bool(),
  
  /**
    Enable or disable the view.  
    
    The collection view will set the isEnabled property of its item views to
    reflect the same view of this property.  Whenever isEnabled is false,
    the collection view will also be not selectable or editable, regardless of 
    the settings for isEditable & isSelectable.
    
    @type {Boolean}
  */
  isEnabled: YES,
  
  /** @private */
  isEnabledBindingDefault: SC.Binding.bool(),
  
  /**
    Allow user to edit content views.
    
    The collection view will set the isEditable property on its item views to
    reflect the same value of this property.  Whenever isEditable is false, 
    the user will not be able to reorder, add, or delete items regardless of 
    the canReorderContent and canDeleteContent and isDropTarget properties.
    
    @type {Boolean}
  */
  isEditable: YES,
  
  /** @private */
  isEditableBindingDefault: SC.Binding.bool(),
  
  /**
    Allow user to reorder items using drag and drop.
    
    If true, the user will can use drag and drop to reorder items in the list.
    If you also accept drops, this will allow the user to drop items into 
    specific points in the list.  Otherwise items will be added to the end.
    
    @type {Boolean}
  */
  canReorderContent: NO,
  
  /** @private */
  canReorderContentBindingDefault: SC.Binding.bool(),
  
  /**
    Allow the user to delete items using the delete key
    
    If true the user will be allowed to delete selected items using the delete
    key.  Otherwise deletes will not be permitted.
    
    @type {Boolean}
  */
  canDeleteContent: NO,
  
  /** @private */
  canDeleteContentBindingDefault: SC.Binding.bool(),
  
  /**
    Allow user to edit the content by double clicking on it or hitting return.
    This will only work if isEditable is YES and the item view implements 
    the beginEditing() method.
    
    @type {Boolean}
  */
  canEditContent: NO,
  
  /** @private */
  canEditContentBindingDefault: SC.Binding.bool(),
  
  /**
    Accept drops for data other than reordering.
    
    Setting this property to return true when the view is instantiated will 
    cause it to be registered as a drop target, activating the other drop 
    machinery.
    
    @type {Boolean}
  */
  isDropTarget: NO,
  
  /**
    Use toggle selection instead of normal click behavior.
    
    If set to true, then selection will use a toggle instead of the normal
    click behavior.  Command modifiers will be ignored and instead clicking
    once will select an item and clicking on it again will deselect it.
    
    @type {Boolean}
  */
  useToggleSelection: NO,
  
  /**
    Trigger the action method on a single click.
    
    Normally, clicking on an item view in a collection will select the content 
    object and double clicking will trigger the action method on the 
    collection view.
    
    If you set this property to YES, then clicking on a view will both select 
    it (if isSelected is true) and trigger the action method.  
    
    Use this if you are using the collection view as a menu of items.
    
    @property {Boolean}
  */  
  actOnSelect: NO,
  
  
  /**
    Select an item immediately on mouse down
    
    Normally as soon as you begin a click the item will be selected.
    
    In some UI scenarios, you might want to prevent selection until
    the mouse is released, so you can perform, for instance, a drag operation
    without actually selecting the target item.  
    
    @property {Boolean}
  */  
  selectOnMouseDown: YES,
  
  createChildViews: function() {
    var header, data;
    
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
        isDropTarget: this.get('isDropTarget'),
        isSelectable: this.get('isSelectable'),
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


  
  /**
    Changes the sort descriptor based on the column that is passed and the current sort state
  
    @param {SC.TableColumn} column The column to sort by
    @param {String} sortState The desired sort state (ASC|DESC)
  */
  sortByColumn: function(column, sortState) {
    if(sortState !== "ASC")
    {
      sortState = "ASC";
    }
    else
    {
      sortState = "DESC";
    }

    this.set('sortDescriptor', sortState + " " + column.get('key'));
  },

  /**
    Called by the TableHeaderView when a column is being dragged
    
    @param {SC.TableColumn} column the column being dragged
  */
  draggingColumn: function(column) {
    // this.$().addClass('reordering-columns');
    // this._dragging = column;
  },
  
  /** 
    Called by the TableHeaderView when a column is being dragged. Adjusts the offset of the ghost
    
    @param {Number} offset The offset by which the column has been dragged
  */
  columnDragged: function(offset) {
    // this._ghostLeft += offset;
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
  }
  
});
