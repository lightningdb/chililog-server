Endash.CollectionBlockView = SC.View.extend(Endash.CollectionBlockViewDelegate, {
  
  rowHeight: function() {
    var delegate = this.get('blockDelegate'),
      ret = delegate.get('rowHeight');

    return ret;
  }.propert('delegate').cacheable();
  
  // an array of child block views. these are kept even if they are no longer needed
  // because they might be if the content for this view is changed
  blockViews: null,
    
  // the content item for this block. either null (if root) or a treeitemobserver
  content: null,
  
  indexBinding: '*content.index',

  childrenBinding: '*content.children',
  
  lengthBinding: '*content.length',

  // the view for this item. This is so we can reuse a blockview for both a group view
  // or an item view if need be
  myView: null,
  
  // depending on how many states the view for this item can have we want to cache them all
  myViews: null,
  
  blockDelegate: function() {
    var del = this.get('delegate');
    return this.delegateFor('isBlockDelegate', del);
  }.property('delegate').cacheable(),
    
  indexes: function() {
    return SC.IndexSet.create(0, this.getPath('.content.children.length'));
  }.property('content').cacheable(),
  
  transform: function() {
    var children = this.get('children');
    if(!children) return;
    
    var indexes = this.get('indexes'),
      blockViews = this.get('blockViews'),
      children = this.get('children')
      blockView, child, i;
      
    if(!blockViews) this.set('blockViews', (blockViews = []));

    var blockIdx = 0;
    indexes.forEach(function(i) {
      child = children.objectAt(i);

      blockView = blockViews.objectAt(blockIdx++);
      if(!blockView) blockView = this._allocateBlockView();
      
      blockView.set('content', child);
      blockView.set('layout', this.layoutForBlockView(i));
      this.wakeView(blockView);
    }, this);
    
    //put any child views not being used to sleep
    for(var i = indexes.get('length'); i < blockViews.get('length'); i++) {
      this.sleepView(blockViews.objectAt(i))
    }
    
    
    this._setMyView();
  }.observes('content'),
  
  height: function() {
    var delegate = this.get('blockDelegate'),
      ret = delegate.get('rowHeight'),
      children = this.get('children');
    
    if(!children) return ret;
    
    var numChildren = children.get('length'),
      blockViews = this.get('blockViews'),
      view, i;
    
    for(i = 0; i < numChildren; i++) {
      view = blockViews.objectAt(i);
      ret += view.get('height');
    }
    
    return ret;
  }.property('content', 'length').cacheable(),
  
  layoutForBlockView: function(i) {
    return {
      top: this.get('rowHeight') * (i + ),
      left: 0,
      right: 0,
      height: 30
    };
  },
  
  _allocateBlockView: function() {
    var ret = Endash.CollectionBlockView.create({});
    this.appendChild(ret);
    return ret;
  },
  
  wakeView: function(view) {
    
  },
  
  sleepView: function(view) {
    view.adjust('left', '-9999');
  },
  
  
  

});