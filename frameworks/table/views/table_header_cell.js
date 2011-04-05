sc_require('views/thumb');

SC.TableHeaderCellView = SC.View.extend(SC.Button, {
  
  layout: {top:0, bottom:1},
  
  classNames: ['sc-table-cell'],

  tagName: 'div',
  
  column: null,
    
  // TableHeader will pass this along
  thumbView: null,
  
  displayProperties: ['column','title'],
  
  sortDescriptor: null,
  sortDescriptorBinding: '.parentView.sortDescriptor',
  
  // 
  // displayProperties: ['dragging', 'sortState'],
  //
  
  // childViews: 'sortStateView labelView thumbView'.w(),
  childViews: 'labelView thumbView'.w(),
    
  labelView: SC.View.extend({
    tagName: 'label',
    
    layout:{left:8,right:15,top:0,bottom:0},
    
    valueBinding: '.parentView.column.title',
    
    displayProperties: ['value'],
    render: function(context,firstTime){
      context.push(this.get('value'));
    }
  }),

  // We're going to disable this for now

  // /** 
  //   This View renders the arrow indicating sort state
  //   
  //   @private 
  // */
  // sortStateView: SC.View.extend({
  //   layout:{top: 17,height:8,right:17,width:9},
  //   sortStateBinding: '.parentView.sortState',
  //   sortStateDidChange: function(){
  //     switch (this.get('sortState')){
  //       case "ASC":
  //         this.set('classNames',['sc-sort-state-asc']);
  //       break;
  //       case "DESC":
  //         this.set('classNames',['sc-sort-state-desc']);
  //       break;
  //       default:
  //       this.set('classNames',[]);
  //       break;
  //     }
  //     this.displayDidChange();
  //   }.observes('sortState')
  // }),
  

  
  
  /** @private */
  sortState: function() {
    var key = this.get('sortDescriptor');
    if(!key || this.spacer)
    {
      return;
    }
    
    var descending = NO;
  
    if(SC.typeOf(key) === "array")
    {
      key = key[0];
    }
      
    if (key.indexOf('ASC') > -1) {
         key = key.split('ASC ')[1];
       } else if (key.indexOf('DESC') > -1) {
         key = key.split('DESC ')[1];
         descending = YES;
       }
    if(key === this.get('column').get('key'))
    {
      return descending ? "DESC" : "ASC";
    }
    
    return "none";
  }.property('sortDescriptor').cacheable(),

  sortStateBinding: '*column.sortState',
  // 
  // render: function(context, firstTime) {
  //   var href, toolTip, classes, theme;
  //   var sortState = this.get('sortState');
  //   
  //   context.setClass('first', this.get('first'));
  // 
  //   classes = this._TEMPORARY_CLASS_HASH || {};
  //   classes.asc = (sortState  === "ASC");
  //   classes.desc = (sortState === "DESC");
  //   classes.selected = !SC.none(sortState) && sortState !== "none";
  //   classes.draggging = this.get('dragging');
  //   classes.def = this.get('isDefault');
  //   classes.cancel = this.get('isCancel');
  //   
  //   classes.icon = !!this.get('icon');
  //   classes.dragging = this.get('dragging');
  //   
  //   context.attr('role', 'button').setClass(classes);
  //   theme = this.get('theme');
  //   if (theme) context.addClass(theme);
  //   if (firstTime)
  //   {
  //     this.renderChildViews(context, firstTime);
  //   }
  // },
  // 
  
  
  


});