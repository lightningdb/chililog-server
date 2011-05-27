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
  basicRowsPerSearch: 50,
  
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
   * The search mode basic or advanced
   * @type boolean
   */
  isBasicSearchMode: YES,

  /**
   * Text to display on the toggle search mode button
   */
  toggleSearchModeButtonTitle: function() {
    var isBasicSearchMode = this.get('isBasicSearchMode');
    return isBasicSearchMode ? '_searchListView.AdvancedSearchMode' : '_searchListView.BasicSearchMode';
  }.property('isBasicSearchMode').cacheable(),

  /**
   * Dynamically calculated table layout based on basic/advanced mode and if there are more records to retrieve
   */
  tableLayout: function() {
    var top = 122;
    var isBasicSearchMode = this.get('isBasicSearchMode');
    if (!isBasicSearchMode) {
      top = 272;
    }
    
    var bottom = 10;
    var canShowMore = this.get('canShowMore');
    if (canShowMore) {
      bottom = 50;
    }

    return { top: top, left: 10, right: 10, bottom: bottom };

  }.property('isBasicSearchMode', 'canShowMore').cacheable(),

  /**
   * Where the no rows found message is displayed is also dependent on basic/advanced search
   */
  noRowsFoundMessageLayout: function () {
    var top = 155;
    var isBasicSearchMode = this.get('isBasicSearchMode');
    if (!isBasicSearchMode) {
      top = 230;
    }

    return { top: top, left: 25, width: 200, height: 25 };
  }.property('isBasicSearchMode').cacheable(),

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
  },

  /**
   * Toggle between advanced and basic search modes
   */
  toggleSearchMode: function() {
    var isBasicSearchMode = this.get('isBasicSearchMode');
    this.set('isBasicSearchMode', !isBasicSearchMode);
  }
});
