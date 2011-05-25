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
   * Flag to denote if we can delete this record or not
   * @type Boolean
   */
  canDelete: function() {
    var canSave = this.get('canSave');
    var isCreating = this.get('isCreating');

    return !canSave && !isCreating;
  }.property('canSave', 'isCreating').cacheable(),

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
   * Adjust height of body box depending on if we are adding or not
   */
  paneLayout: function() {
    if (this.get('isCreating')) {
      return { width:700, height:470, centerX:0, centerY:-50 };
    } else {
      return { width:700, height:370, centerX:0, centerY:-50 };
    }
  }.property('isCreating').cacheable(),

  /**
   * Show this modal form
   */
  show: function() {
    Chililog.configureUserDetailView.append();

    // Set focus on the username field
    this.setFocusOnField(Chililog.configureUserDetailView.getPath('contentView.body.username.field'), 100);
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
  }

});
