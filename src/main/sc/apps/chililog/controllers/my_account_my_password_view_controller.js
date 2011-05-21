// ==========================================================================
// Project:   Chililog.mainPaneController
// Copyright: ©2011 My Company, Inc.
// ==========================================================================

sc_require('controllers/view_controller_mixin');

/**
 * Controller for change password view.
 *
 * @extends SC.Object
 */
Chililog.myAccountMyPasswordViewController = SC.ObjectController.create(Chililog.ViewControllerMixin,
/** @scope Chililog.myAccountChangePasswordViewController.prototype */ {

  /**
   * Current password
   * @type String
   */
  oldPassword: null,

  /**
   * New password
   * @type String
   */
  newPassword: null,

  /**
   * Confirmation of new password
   * @type String
   */
  confirmNewPassword: null,

  /**
   * Flag to indicate if the user is able to change password
   * Only valid if old, new and confirm passwords have been entered
   *
   * @type Boolean
   */
  canChangePassword: function() {
    if (!this.get('isSavingProfile') && !SC.empty(this.get('oldPassword')) && !SC.empty(this.get('newPassword')) &&
      !SC.empty(this.get('confirmNewPassword')) && !this.get('isChangingPassword')) {
      return YES;
    }
    return NO;
  }.property('oldPassword', 'newPassword', 'confirmNewPassword', 'isSavingProfile').cacheable(),

  /**
   * Flag to indicate if we are in the middle of trying to save a profile
   */
  isSaving: NO,

  /**
   * Trigger event to change the user's password
   */
  save: function() {
    // Check field values
    var rootView = Chililog.myPasswordView;
    var result = this.findFieldAndValidate(rootView);

    // Special cross field checks here
    if (result === SC.VALIDATE_OK) {
      if (this.get('newPassword') !== this.get('confirmNewPassword')) {
        result = Chililog.$error('_myAccountMyPasswordView.ConfirmNewPassword.Invalid'.loc(), null,
          Chililog.myPasswordView.getPath('password.field'));
      }
    }

    if (result !== SC.VALIDATE_OK) {
      this.showError(result);
      return;
    }
    Chililog.statechart.sendEvent('save');
  },

  /**
   * Show the my profile view to the user
   */
  show: function() {
    Chililog.myAccountView.setPath('right.nowShowing', 'Chililog.myPasswordView');
    return;
  },

  /**
   * Show success message when password changed
   */
  showChangePasswordSuccess: function() {
    var view = Chililog.myPasswordView.getPath('successMessage');
    var field = Chililog.myPasswordView.getPath('oldPassword.field');

    if (!SC.none(view)) {
      // Have to invokeLater because of webkit
      // http://groups.google.com/group/sproutcore/browse_thread/thread/482740f497d80462/cba903f9cc6aadf8?lnk=gst&q=animate#cba903f9cc6aadf8
      view.adjust("opacity", 1);
      this.invokeLater(function() {
        view.animate("opacity", 0, { duration: 2, timing:'ease-in' });
      }, 10);
    }

    this.setFocusOnField(field);
  }

});

