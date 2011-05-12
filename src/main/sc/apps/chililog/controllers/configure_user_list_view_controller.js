// ==========================================================================
// Project:   Chililog.mainPaneController
// Copyright: ©2011 My Company, Inc.
// ==========================================================================

sc_require('controllers/view_controller_mixin');

/**
 * Controller for listing and searching for users
 */
Chililog.configureUserListViewController = SC.ArrayController.create({

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
   * Show list of users in the right hand side details pane
   */
  show: function() {
    Chililog.configureView.setPath('right.contentView.nowShowing', 'Chililog.configureUserListView');
    return;
  },

  /**
   * Trigger event to create a new user
   */
  create: function() {
    Chililog.statechart.sendEvent('createUser');
  },

  /**
   * User double clicked on record so edit it
   */
  edit: function() {
    var record = this.get('selectedRecord');
    if (SC.none(record)) {
      return null;
    }

    var id = record.get(Chililog.DOCUMENT_ID_RECORD_FIELD_NAME);
    Chililog.statechart.sendEvent('editUser', id);
  },

  /**
   * Since this is a simple async call, skip the statechart and directly call the data controller
   */
  refresh: function() {
    Chililog.statechart.sendEvent('refresh');
  }

});
