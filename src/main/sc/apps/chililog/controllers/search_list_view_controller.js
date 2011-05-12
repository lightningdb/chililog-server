// ==========================================================================
// Project:   Chililog.mainPaneController
// Copyright: ©2011 My Company, Inc.
// ==========================================================================

sc_require('controllers/view_controller_mixin');

/**
 * Search listing
 */
Chililog.searchListViewController = SC.ArrayController.create({

  keywords: '',

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
   * Show list of repositories in the right hand side details pane
   */
  show: function() {
    Chililog.configureView.setPath('right.scenes', ['Chililog.configureRepositoryInfoListView', 'Chililog.configureRepositoryInfoDetailView']);
    Chililog.configureView.setPath('right.nowShowing', 'Chililog.configureRepositoryInfoListView');
    return;
  },

  /**
   * Since this is a simple async call, skip the statechart and directly call the data controller
   */
  search: function() {
    Chililog.statechart.sendEvent('search');
  }

});
