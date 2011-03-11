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

Endash.DataView = SC.ListView.extend(SC.CollectionFastPath, {
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
    We override this b/c the base implementation grabs the column instead
    of the row from the layer ID
  */
  contentIndexForLayerId: function(id) {
    if (!id || !(id = id.toString())) return null ; // nothing to do
    
    var base = this._baseLayerId;
    if (!base) base = this._baseLayerId = SC.guidFor(this)+"-";
    
    // no match
    if ((id.length <= base.length) || (id.indexOf(base) !== 0)) return null ; 
    var ret = Number(id.split('-')[1]);
    return isNaN(ret) ? null : ret ;
  }
  

});
