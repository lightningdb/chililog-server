sc_require('views/table_row')

// ==========================================================================
// Project:   SproutCore - JavaScript Application Framework
// Copyright: ©2006-2009 Sprout Systems, Inc. and contributors.
//            Portions ©2008-2009 Apple Inc. All rights reserved.
// License:   Licened under MIT license (see license.js)
// ==========================================================================
/*globals SC Endash */

/**
  @class

  DataView handles the display of tableRow views.
  It extends CollectionFastPath to provide accelerated rendering.

  @extends SC.View
  @author Christopher Swasey
*/

/*globals Endash */

Endash.DataView = SC.ListView.extend(Endash.CollectionFastPath, {
  /**
    TableRow
    @property {SC.View}
  */
  exampleView: SC.TableRowView,
  
  /**
    The actual cell view
    @property {SC.View}
  */
  cellView: Endash.TableCellView,
  
  /**
    The cell content view, which gets placed inside a cell
    and actually displays the contents for the cell
    @property {SC.View}
  */
  cellContentView: SC.LabelView.extend({
    isPoolable: YES,
    layerIsCacheable: YES,
    contentValueKeyBinding: '*column.key',
    
    contentValueKeyDidChange: function() {
      this.updatePropertyFromContent('value', '*', 'contentValueKey');
    }.observes('contentValueKey')
  }),

  /**
    @private
    Gets the cell content class for a given column, defaults to our
    cellContentView
  */
  cellViewForColumn: function(col) {
    var columns = this.get('columns'),
      column = columns.objectAt(col),
      ret;

    if(ret = column.get('exampleView')) return ret;

    return this.get('cellContentView');
  },

  /**
    @private
    We override this to call our own repositionView method
  // */
  // wakePooledView: function(view, attrs) {
  //   // configure
  //   this.configureItemView(view, attrs);
  // 
  //   var layer = view.get('layer');
  //   this._repositionView(layer, attrs.layout);
  //   
  //   // awake from the pool, etc.
  //   if (view.awakeFromPool) view.awakeFromPool(view.owningPool, this);
  //   
  // },
  
  /**
    @private
    We handle repositioning the view specifically to avoid the overhead
    of using set layout or adjust
  */
  _repositionView: function(layer, layout) {
    if(SC.platform.touch) {
      var transform = 'translate3d(0px, ' + layout.top + 'px,0)';
      if (layer) {
        layer.style.webkitTransform = transform;
        layer.style.webkitTransformOrigin = "top left";
        layer.style.top = '';
      }
    } else {
      layer.style.top = layout.top + 'px';
    }
  },
  
  /**
    @private
    Sends a view to a DOM pool.
  */
  sendToDOMPool: function(view) {
    var pool = this.domPoolForExampleView(view.createdFromExampleView);
    pool.push(view);
    var f = view.get("frame");
    
    this._repositionView(view.get('layer'), {top: -(f.height + 2)});
    
    view.set("layerId", SC.guidFor(view));
    if (view.sleepInDOMPool) view.sleepInDOMPool();
  },
  
  /**
    @private
    This should completely reset the view, but we don't use it right now.
  */
  _reset: function() {
    this.reloadIfNeeded(SC.IndexSet.create(), true);
    delete this._viewMap;
    delete this._indexMap;
    var pools = this._domPools || (this._domPools = {});
    for (var p in pools) {
      for(var i = 0, len = pools[p].length; i < len; i++) {
        pools[p][i].destroy();
      }
      pools[p].length = 0;
    }
    
    this.reloadIfNeeded(null, true);
  },
  
  configureItemView: function(itemView, attrs) {
    itemView.beginPropertyChanges();
    itemView.setIfChanged('content', attrs.content);
    itemView.setIfChanged('contentIndex', attrs.contentIndex);
    itemView.setIfChanged('parentView', attrs.parentView);
    itemView.setIfChanged('layerId', attrs.layerId);
    itemView.setIfChanged('isEnabled', attrs.isEnabled);
    itemView.setIfChanged('isSelected', attrs.isSelected);
    itemView.setIfChanged('outlineLevel', attrs.outlineLevel);
    itemView.setIfChanged('disclosureState', attrs.disclosureState);
    itemView.setIfChanged('isVisibleInWindow', attrs.isVisibleInWindow);
    itemView.setIfChanged('isGroupView', attrs.isGroupView);
    itemView.setIfChanged('page', this.page);
    itemView.endPropertyChanges();
    
    this._repositionView(itemView.get('layer'), attrs.layout);
    itemView._updateCells();
  }
  

});
