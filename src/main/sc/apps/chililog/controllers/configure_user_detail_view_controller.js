// ==========================================================================
// Project:   Chililog.mainPaneController
// Copyright: ©2011 My Company, Inc.
// ==========================================================================

sc_require('controllers/view_controller_mixin');

/**
 * Controller for performing CRUD on user records
 */
Chililog.configureUserDetailViewController = SC.ObjectController.create(Chililog.ViewControllerMixin, {

  /**
   * User record to display
   * @type Chililog.UserRecord
   */
  content: null,

  /**
   * Controller for repository access array
   */
  repositoryAccessArrayController: SC.ArrayController.create({

    /**
     * Bind to the user's repository access
     */
    contentBinding: 'Chililog.configureUserDetailViewController.repositoryAccesses',

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
   * Map between user repository access code and it's display text
   */
  repositoryAccessRoles: function() {
    return [
      { displayText:'_configureUserDetailView.repositoryAccesses.AdminRole'.loc(), code: Chililog.REPOSITORY_ADMINISTRATOR_ROLE },
      { displayText:'_configureUserDetailView.repositoryAccesses.PowerRole'.loc(), code: Chililog.REPOSITORY_POWER_USER_ROLE },
      { displayText:'_configureUserDetailView.repositoryAccesses.StandardRole'.loc(), code:Chililog.REPOSITORY_STANDARD_USER_ROLE }
    ];
  }.property().cacheable(),

  /**
   * Flag to denote if we can delete this record or not
   * @type Boolean
   */
  canDelete: function() {
    var canSave = this.get('canSave');
    var isCreating = this.get('isCreating');
    var isSaving = this.get('isSaving');
    return !canSave && !isCreating && !isSaving;
  }.property('canSave', 'isCreating', 'isSaving').cacheable(),

  /**
   * Flag to indicate if we are creating
   */
  title: function() {
    var record = this.get('content');
    if (SC.none(record)) {
      return '';
    }
    if (record.get(Chililog.DOCUMENT_VERSION_RECORD_FIELD_NAME) === 0) {
      return '_configureUserDetailView.CreateTitle'.loc();
    } else {
      return '_configureUserDetailView.EditTitle'.loc(record.get('username'));
    }
  }.property('content').cacheable(),

  /**
   * label for password. Not mandatory for edit mode
   */
  passwordLabelValue: function() {
    var isCreating = this.get('isCreating');
    return '_configureUserDetailView.Password'.loc() + (isCreating ? '*' : '');
  }.property('isCreating').cacheable(),

  /**
   * Label for confirm password. Not mandatory for edit mode
   */
  confirmPasswordLabelValue: function() {
    var isCreating = this.get('isCreating');
    return '_configureUserDetailView.ConfirmPassword'.loc() + (isCreating ? '*' : '');
  }.property('isCreating').cacheable(),

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
   * Show this modal form
   */
  show: function() {
    Chililog.configureUserDetailView.setPath('contentView.body.nowShowing', 'Chililog.userAttributesView');

    // Show modal
    Chililog.configureUserDetailView.append();

    // Set focus on the username field
    this.setFocusOnField(Chililog.userAttributesView.getPath('username.field'), 100);
  },

  /**
   * Hide this modal form
   */
  hide: function() {
    Chililog.configureUserDetailView.remove();
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
   * Flag to indicate if we are in the middle of trying to save a profile
   * This flag is set in by the state chart
   */
  isSaving: NO,

  /**
   * Trigger event to save the user's profile
   */
  save: function() {
    // Check field values
    var rootView = Chililog.configureUserDetailView.getPath('contentView.body');
    var result = this.findFieldAndValidate(rootView);

    // Special cross field checks here
    if (result === SC.VALIDATE_OK) {
      if (this.get('isCreating')) {
        if (this.get('password') !== this.get('confirmPassword')) {
          result = Chililog.$error('_configureUserDetailView.ConfirmPassword.Invalid'.loc(), null,
            Chililog.configureUserDetailView.getPath('body.contentView.password.field'));
        }
      }
    }

    if (result !== SC.VALIDATE_OK) {
      this.showError(result);
      return;
    }

    Chililog.statechart.sendEvent('save');
  },

  /**
   * Confirm erase
   */
  confirmErase: function() {
    SC.AlertPane.warn({
      message: '_configureUserDetailView.ConfirmDelete'.loc(this.getPath('content.username')),
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
    var record = Chililog.configureUserDetailViewController.get('content');
    Chililog.statechart.sendEvent('eraseUser', record.get(Chililog.DOCUMENT_ID_RECORD_FIELD_NAME));
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
    var tableDataView = Chililog.configureUserListView.getPath('table._dataView.contentView');
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
    var tableDataView = Chililog.configureUserListView.getPath('table._dataView.contentView');
    tableDataView.selectNextItem();

    // Cannot use the view controller but must reference the view itself because the controller does not get
    // updated until the run loop finishes
    var selectedRecord = tableDataView.getPath('selection.firstObject');
    var id = selectedRecord.get(Chililog.DOCUMENT_ID_RECORD_FIELD_NAME);
    Chililog.statechart.sendEvent('editAnother', id);
  },

  /**
   * Item int he the repositoryAccess array to delete
   * @param {Object} repositoryAccess
   */
  deleteRepositoryAccess: function(repositoryAccess) {

    // To get deletes the work, we need to create a new array with all the elements we want to keep and
    // update the user record with the new array. For whatever reason, the new array forces the screen
    // to update. Removing an item from the existing array does not update the screen.
    var array = this.getPath('content.repositoryAccesses');
    var newArray = [];
    for (var i = 0; i < array.length; i++) {
      if (array[i] !== repositoryAccess) {
        newArray.push(array[i]);
      }
    }

    newArray.sort(function(a, b) {
      var nameA = a.repository, nameB = b.repository;
      if (nameA < nameB) return -1;
      if (nameA > nameB) return 1;
      return 0;
    });

    this.setPath('content.repositoryAccesses', newArray);
    this.set('repositoryAccessesChanged', YES);
  },

  /**
   * Adds the new repository access using the selected repository and role in the add box.
   */
  addRepositoryAccess: function() {
    // Check if it already exists
    var repository = this.getPath('repositoryAccessArrayController.repositoryToAdd');
    var role = this.getPath('repositoryAccessArrayController.roleToAdd');
    var repositoryAccesses = this.getPath('content.repositoryAccesses');
    if (!SC.none(repositoryAccesses)) {
      for (var i = 0; i < repositoryAccesses.length; i++) {
        var ra = repositoryAccesses[i];
        if (ra.repository === repository) {
          var msg =  '_configureUserDetailView.repositoryAccesses.AlreadyExists'.loc(repository, role);
          SC.AlertPane.info( { message: msg });
          return;
        }
      }
    }

    var newArray = [].concat(repositoryAccesses);
    newArray.push({
      repository: repository,
      role: role
    });

    newArray.sort(function(a, b) {
      var nameA = a.repository, nameB = b.repository;
      if (nameA < nameB) return -1;
      if (nameA > nameB) return 1;
      return 0;
    });

    this.setPath('content.repositoryAccesses', newArray);
    this.set('repositoryAccessesChanged', YES);
  }

});
