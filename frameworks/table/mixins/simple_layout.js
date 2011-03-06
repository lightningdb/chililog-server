SC.SimpleLayout = {
  isLayout: YES,
  isDividedLayout: YES,

  layoutDirection: SC.LAYOUT_HORIZONTAL,

  thicknesses: null,
  thicknessesBindingDefault: SC.Binding.multiple(),

  totalThickness: 0,
  
  widthDelta: null,
  offsetDelta: null,
  
  thicknessKey: null,
  
  childViewsDidChange: function() {
    this._sl_layoutChildViews();
  }.observes('childViews'),
  
  _sl_layoutChildViews: function() {
    var layoutViews = (this._layoutViews || (this._layoutViews = []))
      childViews = this.get('childViews');
      
    childViews.forEach(function(c) {
      if(c.layoutDelegate == this)
        layoutViews[c.layoutIndex] = c
    }, this);
    
    this.layoutViewsFrom(0)
  },

  layoutForView: function(idx, view) {
    var ret = {top: 0, left: 0, right: 0, bottom: 0}
    var direction = this.get('layoutDirection')
    if((direction == SC.LAYOUT_HORIZONTAL))
      delete ret['right']
    else
      delete ret['bottom']
    
    ret[(direction == SC.LAYOUT_HORIZONTAL) ? 'left' : 'top'] = this.offsetForView(idx, view)

    // if(view.get('spacer'))
      // ret[(direction == SC.LAYOUT_HORIZONTAL) ? 'right' : 'bottom'] = 0
    // else
      ret[(direction == SC.LAYOUT_HORIZONTAL) ? 'width' : 'height'] = this.thicknessForView(idx, view)
      
    return ret
  },

  thicknessForView: function(idx, view) {
    var thicknesses = this.get('thicknesses');
    return thicknesses.objectAt(idx).get(this.get('thicknessKey')) + (this.widthDelta || 0);
  },

  offsetForView: function(idx, view) {
    var cache = this._offsetCache;
    if (!cache)
      cache = this._offsetCache = [];

    if(SC.none(this._offsetCache[idx])) {
      if(idx > 0)
        this._offsetCache[idx] = this.offsetForView(idx - 1) + this.thicknessForView(idx - 1)
      else
        this._offsetCache[idx] = this.startOffset || 0
    }
  
    return this._offsetCache[idx] + (this.offsetDelta || 0)
  },

  thicknessesDidChange: function() {
    var thicknesses = this.get('thicknesses')
    if (SC.none(thicknesses) || thicknesses === this._thicknesses) return this; // nothing to do
  
    var observer   = this._dv_thicknessesRangeObserver
    var func = this.thicknessesRangeDidChange;
  
      // cleanup old content
    if(this._thicknesses)
      this._thicknesses.removeRangeObserver(observer)
  
    observer = thicknesses.addRangeObserver(null, this, func, null, YES);      
    this._dv_thicknessesRangeObserver = observer ;
  
    this._thicknesses = thicknesses

    this.expireLayoutFrom(0);
    this._sl_layoutChildViews(0);
  }.observes('thicknesses'),

  thicknessesRangeDidChange: function(content, object, key, indexes) {
    if (!object && (key === '[]')) {
      this.layoutViewsFrom(0);
      this._updateCells();
    } else {
      if(this.contentPropertyDidChange) this.contentPropertyDidChange(object, key, indexes);
    }
  },
  
  contentPropertyDidChange: function(object, key, indexes) {
    if(key == this.get('thicknessKey')) {
      this.layoutViewsFrom(indexes ? indexes.get('min') : 0)
    }
  },
  
  expireLayoutFrom: function(index) {
    this._offsetCache = this._offsetCache ? this._offsetCache.slice(0, index) : null;
    // this.layoutViewsFrom(index)
  },
  
  layoutViewsFrom: function(index) {
    if(!index) index = 0
    this.expireLayoutFrom(index)
  
    var thicknesses = this.get('thicknesses'), views = this._layoutViews;
    
    if(!views) return
    
    for(var i = index, len = thicknesses.get('length'); i < len; i++) {
      if(!this.shouldLayoutView || this.shouldLayoutView(views[i], i)) {
        this.repositionView(views[i], this.layoutForView(i, views[i]))
      }
    }
    
    this.set('totalThickness', this.offsetForView(len));
    this.adjust('minWidth', this.get('totalThickness'))
    this.set('calculatedWidth', this.get('totalThickness'))
  },
  
  repositionView: function(view, layout) {
    if(view) {
      view.adjust(layout);
    }
  }
}