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
   * Number of rows to display per search
   * @type Number
   */
  basicRowsPerSearch: 100,
  
  /**
   * Flag to indicate if we are in the middle of a basic search.
   * This flag is set in the state chart
   * @type boolean
   */
  isSearching: NO,

  /**
   * Flag to control showing the "no rows found" message.
   * Initially set to YES so that message don't show until search is clicked.
   * @type boolean
   */
  rowsFoundAfterSearch: YES,

  /**
   * Flag to control showing the "no rows found" message.
   * Init to NO because there are no records to show
   * @type boolean
   */
  canShowMore: NO,

  /**
   * Do basic search
   */
  basicSearch: function() {
    Chililog.statechart.sendEvent('basicSearch');
  },
  
  /**
   * Show more records
   */
  showMore: function() {
    Chililog.statechart.sendEvent('showMore');
  },

  /**
   * Popup pane to show record in detail
   */
  view: function() {
    Chililog.statechart.sendEvent('viewEntry');
  }

});
