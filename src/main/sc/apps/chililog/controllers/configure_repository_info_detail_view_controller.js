// ==========================================================================
// Project:   Chililog.mainPaneController
// Copyright: ©2011 My Company, Inc.
// ==========================================================================

sc_require('controllers/view_controller_mixin');

/**
 * Controller for CRUD of a repository info record
 */
Chililog.configureRepositoryInfoDetailViewController = SC.ObjectController.create(Chililog.ViewControllerMixin, {

  /**
   * Repository record to display
   * @type Chililog.RepositoryRecord
   */
  content: null,

  /**
   * Update our content when ever the content of the parent controller changes
   */
  contentDidChange: function() {
    var record = Chililog.configureRepositoryInfoDetailViewController.get('content');
    if (SC.none(record)) {
      this.setPath('repositoryAccessArrayController.content', null);
      return;
    }

    // Update the user access details
    var repositoryName = record.get('name');
    var users = Chililog.store.find(Chililog.UserRecord);
    var repositoryAccessArray = [];
    for (var i = 0; i < users.get('length'); i++) {
      var user = users.objectAt(i);
      var repositoryAccesses = user.get('repositoryAccesses');
      if (!SC.none(repositoryAccesses)) {
        for (var j = 0; j < repositoryAccesses.length; j++) {
          if (repositoryAccesses[j].repository === repositoryName) {
            repositoryAccessArray.push({ username: user.get('username'), userDisplayName: user.get('displayName'), role: repositoryAccesses[j].role});
          }
        }
      }
    }
    this.setPath('repositoryAccessArrayController.content', repositoryAccessArray);

  }.observes('content'),

  /**
   * Controller for repository access array
   */
  repositoryAccessArrayController: SC.ArrayController.create({

    /**
     * Sort by the repository name
     */
    orderBy: 'user',

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
    }.property('selection').cacheable()
  }),

  /**
   * Tab items to display on the left hand side list view
   */
  tabItems: [],

  /**
   * Update tab items when the logged in user changes
   */
  updateTabItems: function() {
    var isSystemAdministrator = Chililog.sessionDataController.get('isSystemAdministrator');
    var isRepositoryAdministrator = Chililog.sessionDataController.get('isRepositoryAdministrator');
    var tabItems = [
      { title: '_configureRepositoryInfoDetailView.GeneralAttributes'.loc(), value: 'Chililog.repositoryGeneralAttributesView'},
      { title: '_configureRepositoryInfoDetailView.PubSubAttributes'.loc(), value: 'Chililog.repositoryPubSubAttributesView'},
      { title: '_configureRepositoryInfoDetailView.StorageAttributes'.loc(), value: 'Chililog.repositoryStorageAttributesView'}
    ];

    if (isSystemAdministrator) {
      tabItems.push(
        { title: '_configureRepositoryInfoDetailView.RepositoryAccesses'.loc(), value: 'Chililog.repositoryAccessView'}
      );
    }

    this.set('tabItems', tabItems);
  }.observes('Chililog.sessionDataController.loggedInUser'),

  /**
   * Flag to denote if we can delete this record or not
   * @type Boolean
   */
  canDelete: function() {
    var canSave = this.get('canSave');
    var isCreating = this.get('isCreating');
    var isSaving = this.get('isSaving');
    return !canSave && !isCreating && !isSaving;
  }.property('canSave', 'isCreating').cacheable(),

  /**
   * Address of the write queue
   */
  address: function() {
    return 'repo.%@'.fmt(this.get('name'));
  }.property('name').cacheable(),

  /**
   * Flag to indicate if we are creating
   */
  title: function() {
    var record = this.get('content');
    if (SC.none(record)) {
      return '';
    }
    if (record.get(Chililog.DOCUMENT_VERSION_RECORD_FIELD_NAME) === 0) {
      return '_configureRepositoryInfoDetailView.CreateTitle'.loc();
    } else {
      return '_configureRepositoryInfoDetailView.EditTitle'.loc(record.get('name'));
    }
  }.property('content').cacheable(),

  /**
   * Flag to indicate if we are creating
   */
  isCreating: function() {
    var record = this.get('content');
    if (!SC.none(record) && record.get(Chililog.DOCUMENT_VERSION_RECORD_FIELD_NAME) === 0) {
      return YES;
    }
    return NO;
  }.property('content').cacheable(),

  /**
   * Show the modal details form
   */
  show: function() {
    Chililog.configureRepositoryInfoDetailView.setPath('contentView.body.nowShowing', 'Chililog.repositoryGeneralAttributesView');
    Chililog.configureRepositoryInfoDetailView.append();

    // What for form to show before setting focus
    this.setFocusOnField(Chililog.repositoryGeneralAttributesView.getPath('name.field'), 300);
  },

  /**
   * Hide this modal form
   */
  hide: function() {
    Chililog.configureRepositoryInfoDetailView.remove();
  },

  /**
   * Flag to indicate if the user's profile can be saved.
   * Can only be saved if form is loaded and the data has changed
   *
   * @type Boolean
   */
  canSave: function() {
    var recordStatus = this.getPath('content.status');
    if (!SC.none(recordStatus) && recordStatus !== SC.Record.READY_CLEAN && !this.get('isSaving')) {
      return YES;
    }
    return NO;
  }.property('content.status', 'isSaving').cacheable(),

  /**
   * Flag to indicate if we are in the middle of trying to save a profile.
   * This flag is set in by the state chart
   */
  isSaving: NO,

  /**
   * Trigger event to save the user's profile
   */
  save: function() {
    // Check field values
    var result = this.findFieldAndValidate(Chililog.repositoryGeneralAttributesView);
    if (result !== SC.VALIDATE_OK) {
      Chililog.configureRepositoryInfoDetailView.setPath('contentView.body.nowShowing', 'Chililog.repositoryGeneralAttributesView');
      this.showError(result);
      return;
    }
    result = this.findFieldAndValidate(Chililog.repositoryPubSubAttributesView);
    if (result !== SC.VALIDATE_OK) {
      Chililog.configureRepositoryInfoDetailView.setPath('contentView.body.nowShowing', 'Chililog.repositoryPubSubAttributesView');
      this.showError(result);
      return;
    }
    result = this.findFieldAndValidate(Chililog.repositoryStorageAttributesView);
    if (result !== SC.VALIDATE_OK) {
      Chililog.configureRepositoryInfoDetailView.setPath('contentView.body.nowShowing', 'Chililog.repositoryStorageAttributesView');
      this.showError(result);
      return;
    }

    // Check that page file size < max memory
    if (this.get('pageSize') > this.get('maxMemory')) {
      this.showError(Chililog.$error('_configureRepositoryInfoDetailView.PageSize.InvalidSize',
        [this.get('pageSize'), this.get('maxMemory')],
        Chililog.repositoryAddressAttributesView.getPath('contentView.pageSize.field')));
      return;
    }

    Chililog.statechart.sendEvent('save');
  },

  /**
   * Confirm erase
   */
  confirmErase: function() {
    SC.AlertPane.warn({
      message: '_configureRepositoryInfoDetailView.ConfirmDelete'.loc(this.getPath('content.name')),
      buttons: [
        {
          title: '_delete'.loc(),
          action: this.erase
        },
        {
          title: '_cancel'.loc()
        }
      ]
    });
  },

  /**
   * Trigger event to delete the user. This is called back from confirmErase
   */
  erase: function() {
    var record = Chililog.configureRepositoryInfoDetailViewController.get('content');
    Chililog.statechart.sendEvent('eraseRepositoryInfo', record.get(Chililog.DOCUMENT_ID_RECORD_FIELD_NAME));
  },

  /**
   * Trigger event to prompt to discard changes
   */
  discardChanges: function() {
    Chililog.statechart.sendEvent('discardChanges');
  },

  /**
   * Event handler to select previous item in the list
   */
  previous: function() {
    var tableDataView = Chililog.configureRepositoryInfoListView.getPath('table._dataView.contentView');
    tableDataView.selectPreviousItem();

    // Cannot use the view controller but must reference the view itself because the controller does not get
    // updated until the run loop finishes
    var selectedRecord = tableDataView.getPath('selection.firstObject');
    var id = selectedRecord.get(Chililog.DOCUMENT_ID_RECORD_FIELD_NAME);
    Chililog.statechart.sendEvent('editAnother', id);
  },

  /**
   * Event handler to select next item in the list
   */
  next: function() {
    var tableDataView = Chililog.configureRepositoryInfoListView.getPath('table._dataView.contentView');
    tableDataView.selectNextItem();

    // Cannot use the view controller but must reference the view itself because the controller does not get
    // updated until the run loop finishes
    var selectedRecord = tableDataView.getPath('selection.firstObject');
    var id = selectedRecord.get(Chililog.DOCUMENT_ID_RECORD_FIELD_NAME);
    Chililog.statechart.sendEvent('editAnother', id);
  }
});
