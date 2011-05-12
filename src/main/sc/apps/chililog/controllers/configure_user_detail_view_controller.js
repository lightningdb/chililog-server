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
  bodyLayout: function() {
    if (this.get('isCreating')) {
      return { top: 0, left: 0, right: 0, height: 450 };
    } else {
      return { top: 0, left: 0, right: 0, height: 350 };
    }
  }.property('isCreating').cacheable(),

  /**
   * Adjust height of buttons depending on if we are adding or not
   */
  buttonsLayout: function() {
    if (this.get('isCreating')) {
      return {top: 370, left: 0, right: 0, height: 50 };
    } else {
      return {top: 270, left: 0, right: 0, height: 50 };
    }
  }.property('isCreating').cacheable(),

  /**
   * Show the user details form
   */
  show: function() {
    Chililog.configureView.setPath('right.contentView.nowShowing', 'Chililog.configureUserDetailView');

    // Set scroller to top of page
    Chililog.configureUserDetailView.setPath('body.verticalScrollOffset', 0);

    // Need to delay setting focus because our scene view takes focus so we have to wait until that finishes first
    var field = Chililog.configureUserDetailView.getPath('body.contentView.username.field');
    this.invokeLater(function() {
      field.becomeFirstResponder();
    }, 400);
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
    var rootView = Chililog.configureUserDetailView.getPath('body.contentView');
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
   * Trigger event to discard changes and go back to the view page
   */
  back: function() {
    Chililog.statechart.sendEvent('discardChanges');
  },

  /**
   * Show success message when profile successfully saved
   */
  showSaveSuccess: function() {
    var view = Chililog.configureUserDetailView.get('successMessage');
    var field = Chililog.configureUserDetailView.getPath('body.contentView.username.field');

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
