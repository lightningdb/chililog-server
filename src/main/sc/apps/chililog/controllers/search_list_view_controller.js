// ==========================================================================
// Project:   Chililog.mainPaneController
// Copyright: ©2011 My Company, Inc.
// ==========================================================================

sc_require('controllers/view_controller_mixin');

/**
 * Controller for searching for repository entries
 */
Chililog.searchListViewController = SC.ArrayController.create(Chililog.ViewControllerMixin, {

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
   * Array of repository name-value pairs to display in the drop down list
   * @type Array
   */
  repositories: null,

  /**
   * Keywords to use in the basic search
   * @type String
   */
  basicKeywords: '',

  /**
   * Repository to look in for basic search. One of the items listed in the repositories property.
   * @type String
   */
  basicRepository: '',

  /**
   * Number of past minutes to limit basic search
   * @type String
   */
  basicTimeSpan: '',

  /**
   * Repository to look in for advance search. One of the items listed in the repositories property.
   * @type String
   */
  advancedRepository: '',

  /**
   * Type of time period specification. In the past X minutes or In between specific times
   * @type String
   */
  advancedTimeType: 'InThePast',

  /**
   * Type of time period specification. In the past X minutes or In between specific times
   * @type Boolean
   */
  isInThePastTimeType: function() {
    return this.get('advancedTimeType') === 'InThePast';
  }.property('advancedTimeType').cacheable(),

  /**
   * Number of past minutes to limit advanced search
   * @type String
   */
  advancedTimeSpan: '',

  /**
   * Date and time to start search
   * @type SC.DateTime
   */
  advancedTimeFrom: null,

  /**
   * Date and time to end search
   * @type SC.DateTime
   */
  advancedTimeTo: null,

  /**
   * Severity level to filter search. E.g. '0' for Emergency
   * @type String
   */
  advancedSeverity: '',

  /**
   * Severity shifts to the left 1 spot if when specifying from and to time.
   * @type Object
   */
  advancedSeverityLayout: function() {
    var left = 495;
    if (!this.get('isInThePastTimeType')) {
      left = 675;
    }
    return { top: 10, left: left, width: 150, height: 50 };
  }.property('isInThePastTimeType').cacheable(),

  /**
   * Filter for the source
   * @type String
   */
  advancedSource: '',

  /**
   * Filter for the host
   * @type String
   */
  advancedHost: '',

  /**
   * Filter for the keywords
   * @type String
   */
  advancedKeywords: '',

  /**
   * Keywords is long to make fields line up when specifying from and to time.
   * @type Object
   */
  advancedKeywordsLayout: function() {
    var width = 310;
    if (!this.get('isInThePastTimeType')) {
      width = 490;
    }
    return  { top: 70, left: 335, width: width, height: 50 };
  }.property('isInThePastTimeType').cacheable(),

  /**
   * Conditions expressed as mongodb script
   * @type String
   */
  advancedConditions: '',

  /**
   * Conditions is long to make fields line up when specifying from and to time.
   * @type Object
   */
  advancedConditionsLayout: function() {
    var width = 630;
    if (!this.get('isInThePastTimeType')) {
      width = 810;
    }
    return  { top: 130, left: 15, width: width, height: 85 };
  }.property('isInThePastTimeType').cacheable(),

  /**
   * Advanced search button shifts to the left
   * @type Object
   */
  advancedSearchButtonLayout: function() {
    var left = 655;
    if (!this.get('isInThePastTimeType')) {
      left = 835;
    }
    return  { top: 30, left: left, width: 80 };
  }.property('isInThePastTimeType').cacheable(),

  /**
   * Advanced search button shifts to the left
   * @type Object
   */
  advancedSearchImageLayout: function() {
    var left = 745;
    if (!this.get('isInThePastTimeType')) {
      left = 925;
    }
    return  { top: 35, left: left, width: 16, height: 16 };
  }.property('isInThePastTimeType').cacheable(),

  /**
   * Flag to indicate if we are in the middle of a basic search.
   * This flag is set in the state chart
   * @type boolean
   */
  isSearching: NO,

  /**
   * Number of rows to display per search
   * @type Number
   */
  rowsPerSearch: 50,

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
      top = 300;
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
   * Do basic search
   */
  advancedSearch: function() {
    var result = this.findFieldAndValidate(Chililog.searchListView.get('advancedSearch'));
    if (result !== SC.VALIDATE_OK) {
      this.showError(result);
      return;
    }

    Chililog.statechart.sendEvent('advancedSearch');
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
    Chililog.statechart.sendEvent('toggleSearchMode');
  }
});
