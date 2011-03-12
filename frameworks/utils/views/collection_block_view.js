Endash.CollectionBlockView = SC.View.extend(/*Endash.CollectionBlockViewDelegate, */{
  
  // an array of child block views. these are kept even if they are no longer needed
  // because they might be if the content for this view is changed
  blockViews: null,
    
  // the content item for this block
  content: null,
  
  // contentIndex: null,
  
  children: null,

  childrenBinding: '*content.children',
  
  index: null,
  
  indexBinding: '*content.index',
    
  length: null,
    
  lengthBinding: '*content.length',

  // the view for this item. This is so we can reuse a blockview for both a group view
  // or an item view if need be
  myView: null,
  
  // depending on how many states the view for this item can have we want to cache them all
  myViews: null,
  
  // most probably the collectionview
  delegate: null,
  
  blockDelegate: function() {
    var del = this.get('delegate');
    return this.delegateFor('isBlockDelegate', del);
  }.property('delegate').cacheable(),
    
  indexes: function() {
    return SC.IndexSet.create(this.get('index'), this.get('length'));
  }.property('index', 'length').cacheable(),
  
  // rowHeight: function() {
  //   var delegate = this.get('blockDelegate'),
  //     ret = delegate.get('rowHeight');
  // 
  //   return ret;
  // }.property('delegate').cacheable(),
  
  transform: function() {
    var children = this.get('children');
    if(!children) return;
    
    var indexes = this.get('indexes'),
      blockViews = this.get('blockViews'),
      children = this.get('children'),
      branchIndexes = this.get('delegate').get('_contentBranchIndexes'),
      blockView, child, i;
      
    if(!blockViews) this.set('blockViews', (blockViews = []));

    var blockIdx = 0;
    indexes.forEach(function(i) {
      child = children.objectAt(i);

      if(branchIndex.contains(i)) { 
        blockView = blockViews.objectAt(blockIdx);
        if(!blockView) blockView = this._allocateBlockView();
      
        blockView.set('contentIndex', i);
        blockView.set('layout', this.layoutForBlockView(blockIdx));
        blockView.set('indexes', SC.IndexSet.create(i, child.get('length')));
        blockView.set('content', this.get('content'))
        this.wakeView(blockView);
      
        blockIdx++;
      }
    }, this);
    
    //put any child views not being used to sleep
    for(var i = indexes.get('length'); i < blockViews.get('length'); i++) {
      this.sleepView(blockViews.objectAt(i));
    }
    
    
    this._setMyView();
  }.observes('content'),
  
  _setMyView: function() {
    var delegate = this.get('blockDelegate'),
     _class = delegate.viewClassForContentIndex(this.get('index')),
     _guid = SC.guidFor(class);
     
    var myViews = this.get('myViews'),
      myView = this.get('myView');
      
    if(!myViews) this.set('myViews', myViews = {})

    if(myView) {
      this.sleepView(myView)
    }

    if(!(myView = myViews[_guid])) {
      myView = myViews[_guid] = _class.create();
      this.appendChild(myView);
    } else {
      this.wakeView(myView);
    }
    
    this.set('myView', myView);
  },
  
  // height: function() {
  //   var delegate = this.get('blockDelegate'),
  //     ret = delegate.get('rowHeight'),
  //     indexes = this.get('indexes');
  //   
  //   if(!indexes) return ret;
  //   
  //   var blockViews = this.get('blockViews'),
  //     view, i;
  //   
  //   indexes.forEach(function(i) {
  //     view = blockViews.objectAt(i);
  //     ret += view.get('height');
  //   }, this);
  //   
  //   return ret;
  // }.property('content', 'length').cacheable(),
  
  // layoutForBlockView: function(i) {
  //   return {
  //     top: this.get('rowHeight') * (i + ),
  //     left: 0,
  //     right: 0,
  //     height: 30
  //   };
  // },
  
  _allocateBlockView: function() {
    var ret = Endash.CollectionBlockView.create({});
    this.appendChild(ret);
    return ret;
  },
  
  wakeView: function(view) {
    viw.adjust('left', 0)
  },
  
  sleepView: function(view) {
    view.adjust('left', -9999);
  },
  
  
  

});