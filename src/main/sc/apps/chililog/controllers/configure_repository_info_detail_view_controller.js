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
   * Text to show in the success message text box
   */
  successMessage: null,

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
   * Show the details form
   */
  show: function() {
    Chililog.configureView.setPath('right.nowShowing', 'Chililog.configureRepositoryInfoDetailView');

    // Set scroller to top of page
    Chililog.configureRepositoryInfoDetailView.setPath('body.verticalScrollOffset', 0);
    
    // Need to delay setting focus because our scene view takes focus so we have to wait until that finishes first
    this.setFocusOnField(Chililog.configureRepositoryInfoDetailView.getPath('body.contentView.name.field'), 400);
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
   */
  isSaving: NO,

  /**
   * Trigger event to save the user's profile
   */
  save: function() {
    // Check field values
    var rootView = Chililog.configureRepositoryInfoDetailView.getPath('body.contentView');
    var result = this.findFieldAndValidate(rootView);
    if (result !== SC.VALIDATE_OK) {
      this.showError(result);
      return;
    }

    // Check that page file size < max memory
    if (this.get('writeQueuePageSize') > this.get('writeQueueMaxMemory')) {
      this.showError(Chililog.$error('_configureRepositoryInfoDetailView.WriteQueuePageSize.InvalidSize',
        [this.get('writeQueuePageSize'), this.get('writeQueueMaxMemory')],
        Chililog.configureRepositoryInfoDetailView.getPath('body.contentView.writeQueueAttributes.writeQueuePageSize.field')));
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
  },

  /**
   * Show success message when profile successfully saved
   */
  showSaveSuccess: function() {
    var view = Chililog.configureRepositoryInfoDetailView.get('successMessage');
    var field = Chililog.configureRepositoryInfoDetailView.getPath('body.contentView.name.field');
    this.set('successMessage', '_successMessage'.loc());

    if (!SC.none(view)) {
      // Have to invokeLater because of webkit
      // http://groups.google.com/group/sproutcore/browse_thread/thread/482740f497d80462/cba903f9cc6aadf8?lnk=gst&q=animate#cba903f9cc6aadf8
      view.adjust("opacity", 1);
      this.invokeLater(function() {
        view.animate("opacity", 0, { duration: 4, timing:'ease-in' });
      }, 10);
    }

    this.setFocusOnField(field);
  }

});
