// ==========================================================================
// Project:   Chililog.mainPaneController
// Copyright: ©2011 My Company, Inc.
// ==========================================================================

sc_require('controllers/view_controller_mixin');

/**
 * Controller for listing and searching for repositories
 */
Chililog.configureRepositoryInfoListViewController = SC.ArrayController.create(Chililog.ViewControllerMixin, {

  /**
   * Selection set. Null if nothing selected
   *
   * @type SC.SelectionSet.
   */
  selection: null,

  /**
   * The selected record
   *
   * @type Chililog.RepositoryInfoRecord
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
   * Title of the toggle button.
   */
  toggleStartStopButtonTitle: '',
  
   /**
   * Flag to indicate if this repository is online
   */
  currentStatusDidChange: function() {
    var record = this.get('selectedRecord');
    if (SC.none(record)) {
      return '';
    }
    this.set ('toggleStartStopButtonTitle', record.get('isOnline') ? '_stop'.loc() : '_start'.loc());
  }.observes('*selectedRecord.currentStatus').cacheable(),

  /**
   * Show list of repositories in the right hand side details pane
   */
  show: function() {
    // Because of a bug in table view, we need to re-create the view everytime
    var oldView = Chililog.configureView.getPath('right.contentView');
    if (!SC.none(oldView)) {
      oldView.destroy();
    }
    Chililog.configureView.setPath('right.nowShowing', null);

    if (Chililog.configureRepositoryInfoListView != null && !Chililog.configureRepositoryInfoListView.get('isDestroyed')) {
      Chililog.configureRepositoryInfoListView.destroy();
    }
    Chililog.configureRepositoryInfoListView = Chililog.ConfigureRepositoryInfoListView.create();

    Chililog.configureView.setPath('right.nowShowing', 'Chililog.configureRepositoryInfoListView');
    return;
  },

  /**
   * Trigger event to create a new user
   */
  create: function() {
    Chililog.statechart.sendEvent('createRepositoryInfo');
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
    Chililog.statechart.sendEvent('editRepositoryInfo', id);
  },

  /**
   * Since this is a simple async call, skip the statechart and directly call the data controller
   */
  refresh: function() {
    Chililog.statechart.sendEvent('refresh');
  },

  /**
   * Start or stop the selected repository
   */
  toggleStartStop: function() {
    var record = this.get('selectedRecord');
    if (SC.none(record)) {
      return null;
    }

    var documentID = record.get(Chililog.DOCUMENT_ID_RECORD_FIELD_NAME);
    if (record.get('isOnline')) {
      Chililog.statechart.sendEvent('stop', documentID);
    } else {
      Chililog.statechart.sendEvent('start', documentID);
    }
  }
});

