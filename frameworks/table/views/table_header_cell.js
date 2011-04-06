sc_require('views/thumb');

SC.TableHeaderCellView = SC.View.extend({
  
  layout: {top:0, bottom:1},
  
  classNames: ['sc-table-cell'],

  tagName: 'div',
  
  column: null,
    
  // TableHeader will pass this along
  thumbView: null,
  
  displayProperties: ['column','title'],
  
  sortDescriptor: null,
  sortDescriptorBinding: '.parentView.sortDescriptor',
  
  sortStateBinding: '*column.sortState'  ,
  
  childViews: 'labelView thumbView sortStateView'.w(),
    
  labelView: SC.View.extend({
    tagName: 'label',
    
    layout:{left:8,right:28,top:0,bottom:0},
    
    valueBinding: '.parentView.column.title',
    
    displayProperties: ['value'],
    render: function(context,firstTime){
      context.push(this.get('value'));
    }
  }),

  // We're going to disable this for now

  /** 
    This View renders the arrow indicating sort state
    
    @private 
  */
  sortStateView: SC.View.extend({
    layout:{top: 0, bottom: 0, right:17,width:11},
    sortStateBinding: '.parentView.sortState',
    sortStateDidChange: function(){
      switch (this.get('sortState')){
        case "ASC":
          this.set('classNames',['sc-view sc-sort-state-asc']);
        break;
        case "DESC":
          this.set('classNames',['sc-view sc-sort-state-desc']);
        break;
        default:
        this.set('classNames',['sc-view']);
        break;
      }
      this.displayDidChange();
    }.observes('sortState')
  }),
  

  
  
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
  }.property('sortDescriptor').cacheable()

});