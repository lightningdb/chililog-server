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
   * Address of the write queue
   */
  writeQueueAddress: function() {
    return 'repository.%@.write'.fmt(this.get('name'));
  }.property('name').cacheable(),

  /**
   * Login username to access the write queue
   */
  writeQueueUsername: function() {
    return this.get('name');
  }.property('name').cacheable(),

  /**
   * Address of the read queue
   */
  readQueueAddress: function() {
    return 'repository.%@.read'.fmt(this.get('name'));
  }.property('name').cacheable(),

  /**
   * Login username of the read queue
   */
  readQueueUsername: function() {
    return this.get('name');
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
    Chililog.configureRepositoryInfoDetailView.setPath('contentView.body.nowShowing', 'Chililog.repositoryAttributesView');
    Chililog.configureRepositoryInfoDetailView.append();

    // What for form to show before setting focus
    this.setFocusOnField(Chililog.repositoryAttributesView.getPath('name.field'), 100);
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
    var result = this.findFieldAndValidate(Chililog.repositoryAttributesView);
    if (result !== SC.VALIDATE_OK) {
      Chililog.configureRepositoryInfoDetailView.setPath('contentView.body.nowShowing', 'Chililog.repositoryAttributesView');
      this.showError(result);
      return;
    }
    result = this.findFieldAndValidate(Chililog.writeQueueAttributesView);
    if (result !== SC.VALIDATE_OK) {
      Chililog.configureRepositoryInfoDetailView.setPath('contentView.body.nowShowing', 'Chililog.writeQueueAttributesView');
      this.showError(result);
      return;
    }
    result = this.findFieldAndValidate(Chililog.readQueueAttributesView);
    if (result !== SC.VALIDATE_OK) {
      Chililog.configureRepositoryInfoDetailView.setPath('contentView.body.nowShowing', 'Chililog.readQueueAttributesView');
      this.showError(result);
      return;
    }

    // Check that page file size < max memory
    if (this.get('writeQueuePageSize') > this.get('writeQueueMaxMemory')) {
      this.showError(Chililog.$error('_configureRepositoryInfoDetailView.WriteQueuePageSize.InvalidSize',
        [this.get('writeQueuePageSize'), this.get('writeQueueMaxMemory')],
        Chililog.writeQueueAttributesView.getPath('contentView.writeQueuePageSize.field')));
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
   * Trigger event to discard changes and go back to the view page
   */
  back: function() {
    Chililog.statechart.sendEvent('discardChanges');
  }
});
