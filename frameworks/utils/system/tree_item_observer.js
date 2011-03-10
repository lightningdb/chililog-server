SC.TreeItemObserver = SC.TreeItemObserver.extend({
  objectAt: function(index) {
    var len   = this.get('length'),
        item  = this.get('item'), 
        cache = this._objectAtCache,
        cur   = index,
        loc   = 0,
        indexes, children, observer;
     
    if (index >= len) return undefined;
    if (this.get('isHeaderVisible')) {
      if (index === 0) return item;
      else cur--;
    }
    item = null; 

    if (!cache) cache = this._objectAtCache = [];
    if ((item = cache[index]) !== undefined) return item ;

    children = this.get('children');
    if (!children) return undefined; // no children - nothing to get
    
    // loop through branch indexes, reducing the offset until it matches 
    // something we might actually return.
    if (indexes = this.get('branchIndexes')) {
      indexes.forEach(function(i) {
        if (item || (i > cur)) return ; // past end - nothing to do

        observer = this.branchObserverAt(i);
        if (!observer) return ; // nothing to do

        // if cur lands inside of this observer's length, use objectAt to get
        // otherwise, just remove len from cur.
        len = observer.get('length') ;
        if (i+len > cur) {
          item = observer.objectAt(cur-i);
          cur  = -1;
        } else cur -= len-1 ;
        
      },this);
    }
    
    if (cur >= 0) {
      item = children.objectAt(cur); // get internal if needed
      item.set('parent', this);
    } else {
      item.set('parent', observer);
    }
    
    cache[index] = item ; // save in cache 

    return item ;
  }
});