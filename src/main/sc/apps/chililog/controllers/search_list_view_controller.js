// ==========================================================================
// Project:   Chililog.mainPaneController
// Copyright: ©2011 My Company, Inc.
// ==========================================================================

sc_require('controllers/view_controller_mixin');

/**
 * Controller for searching for repository entries
 */
Chililog.searchListViewController = SC.ArrayController.create({

  /**
   * Selection set. Null if nothing selected
   *
   * @type SC.SelectionSet.
   */
  selection: null,

  /**
   * The selected record
   *
   * @type Chililog.UserRecord
   */
  selectedRecord: function() {
    var selectionSet = this.get('selection');
    if (SC.none(selectionSet) || selectionSet.get('length') === 0) {
      return null;
    }
    var record = selectionSet.get('firstObject');
    return record;
  }.property('selection').cacheable(),

  /**
   * Array of repostiry name-value pairs to display in the drop down list
   * @type Array
   */
  repositories: null,

  /**
   * Keywords to use in the basic search
   * @type String
   */
  basicKeywords: '',

  /**
   * Repository to look in for basic search
   * @type String
   */
  basicRepository: '',

  /**
   * Number of past minutes to limit basic search
   * @type String
   */
  basicTimeSpan: '',

  /**
   * Flag to indicate if we are in the middle of a basic search.
   * This flag is set in the state chart
   */
  isBasicSearching: NO,

  /**
   * Since this is a simple async call, skip the statechart and directly call the data controller
   */
  basicSearch: function() {
    Chililog.statechart.sendEvent('basicSearch');
  }

});
