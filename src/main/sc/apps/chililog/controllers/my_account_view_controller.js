// ==========================================================================
// Project:   Chililog.mainPaneController
// Copyright: ©2011 My Company, Inc.
// ==========================================================================

/**
 * Controller that for the mainPane. Mainly handles menu selection option and login/logout
 *
 * @extends SC.Object
 */
Chililog.myAccountViewController = SC.ObjectController.create(
/** @scope Chililog.mainPaneController.prototype */ {

  /**
   * The authenticated user details to edit
   * @type Object
   */
  content: null,

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
   * Flag to indicate if the user's profile can be saved.
   * Can only be saved if form is loaded and the data has changed
   *
   * @type Boolean
   */
  canSaveProfile: function() {
    var recordStatus = this.getPath('content.status');
    if (!SC.none(recordStatus) && recordStatus !== SC.Record.READY_CLEAN &&
      !this.get('isSavingProfile') && !this.get('isChangingPassword')) {
      return YES;
    }
    return NO;
  }.property('content.status', 'isSavingProfile', 'isChangingPassword').cacheable(),

  /**
   * Flag to indicate if we are in the middle of trying to save a profile
   */
  isSavingProfile: NO,

  /**
   * Trigger event to save the user's profile
   */
  saveProfile: function() {
    Chililog.statechart.sendEvent('saveProfile');
  },

  /**
   * Trigger event to discard changes to the user's profile
   */
  discardProfileChanges: function() {
    Chililog.statechart.sendEvent('discardProfileChanges');
  },

  /**
   * Show success message when profile successfully saved
   */
  showSaveProfileSuccess: function() {
    var view = Chililog.myAccountView.getPath('myProfile.successMessage');
    var field = Chililog.myAccountView.getPath('myProfile.username.field');

    if (!SC.none(view)) {
      // Have to invokeLater because of webkit
      // http://groups.google.com/group/sproutcore/browse_thread/thread/482740f497d80462/cba903f9cc6aadf8?lnk=gst&q=animate#cba903f9cc6aadf8
      view.adjust("opacity", 1);
      this.invokeLater(function() {
        view.animate("opacity", 0, { duration: 2, timing:'ease-in' });
      }, 10);
    }

    field.becomeFirstResponder();
  },

  /**
   * Show error message when error happened why trying to save profile
   * @param {SC.Error} error
   */
  showSaveProfileError: function(error) {
    if (SC.instanceOf(error, SC.Error)) {
      // Error
      var message = error.get('message');
      SC.AlertPane.error({ message: message });

      var label = error.get('label');
      if (SC.empty(label)) {
        label = 'username';
      }

      var fieldPath = 'myProfile.%@.field'.fmt(label);
      var field = Chililog.myAccountView.getPath(fieldPath);
      if (!SC.none(field)) {
        field.becomeFirstResponder();
      }
    } else {
      // Assume error message string
      SC.AlertPane.error(error);
    }
  },

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
  isChangingPassword: NO,

  /**
   * Trigger event to change the user's password
   */
  changePassword: function() {
    Chililog.statechart.sendEvent('changePassword');
  },

  /**
   * Show success message when password changed
   */
  showChangePasswordSuccess: function() {
    var view = Chililog.myAccountView.getPath('changePassword.successMessage');
    var field = Chililog.myAccountView.getPath('changePassword.oldPassword.field');

    if (!SC.none(view)) {
      // Have to invokeLater because of webkit
      // http://groups.google.com/group/sproutcore/browse_thread/thread/482740f497d80462/cba903f9cc6aadf8?lnk=gst&q=animate#cba903f9cc6aadf8
      view.adjust("opacity", 1);
      this.invokeLater(function() {
        view.animate("opacity", 0, { duration: 2, timing:'ease-in' });
      }, 10);
    }

    field.becomeFirstResponder();
  },

  /**
   * Show error if change password failed
   * @param {SC.Error} error
   */
  showChangePasswordError: function(error) {
    if (SC.instanceOf(error, SC.Error)) {
      // Error
      var message = error.get('message');
      SC.AlertPane.error({ message: message });

      var label = error.get('label');
      if (SC.empty(label)) {
        label = 'oldPassword';
      }

      var fieldPath = 'changePassword.%@.field'.fmt(label);
      var field = Chililog.myAccountView.getPath(fieldPath);
      if (!SC.none(field)) {
        field.becomeFirstResponder();
      }
    } else {
      // Assume error message string
      SC.AlertPane.error(error);
    }
  }

});

