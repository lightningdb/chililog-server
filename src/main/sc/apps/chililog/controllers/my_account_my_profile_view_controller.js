// ==========================================================================
// Project:   Chililog.mainPaneController
// Copyright: ©2011 My Company, Inc.
// ==========================================================================

sc_require('controllers/view_controller_mixin');

/**
 * Controller for my profile view.
 *
 * @extends SC.Object
 */
Chililog.myAccountMyProfileViewController = SC.ObjectController.create(Chililog.ViewControllerMixin,
/** @scope Chililog.myAccountMyProfileViewController.prototype */ {

  /**
   * The authenticated user details to edit
   * @type Object
   */
  content: null,

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
    var rootView = Chililog.myProfileView;
    var result = this.findFieldAndValidate(rootView);
    if (result !== SC.VALIDATE_OK) {
      this.showError(result);
      return;
    }
    Chililog.statechart.sendEvent('save');
  },

  /**
   * Trigger event to discard changes to the user's profile
   */
  discardChanges: function() {
    Chililog.statechart.sendEvent('discardChanges');
  },

  /**
   * Show the my profile view to the user
   */
  show: function() {
    Chililog.myAccountView.setPath('right.nowShowing', 'Chililog.myProfileView');
    return;
  },

  /**
   * Show success message when profile successfully saved
   */
  showSaveProfileSuccess: function() {
    var view = Chililog.myProfileView.getPath('successMessage');
    var field = Chililog.myProfileView.getPath('username.field');

    if (!SC.none(view)) {
      // Have to invokeLater because of webkit
      // http://groups.google.com/group/sproutcore/browse_thread/thread/482740f497d80462/cba903f9cc6aadf8?lnk=gst&q=animate#cba903f9cc6aadf8
      view.adjust("opacity", 1);
      this.invokeLater(function() {
        view.animate("opacity", 0, { duration: 2, timing:'ease-in' });
      }, 10);
    }

    field.becomeFirstResponder();
  }

});

