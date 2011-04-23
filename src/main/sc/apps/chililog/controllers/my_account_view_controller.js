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
    if (!SC.none(recordStatus) && recordStatus !== SC.Record.READY_CLEAN && !this.get('isSavingProfile')) {
      return YES;
    }
    return NO;
  }.property('content.status', 'isSavingProfile').cacheable(),

  /**
   * Flag to indicate if we are in the middle of trying to save a profile
   */
  isSavingProfile: NO,
  
  /**
   * Result of calling saveProfile(). YES if success, SC.Error if error, null if not set or executing.
   *
   * @type Object
   */
  saveProfileResult: null,

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
   * Flag to indicate if the user is able to change password
   * Only valid if old, new and confirm passwords have been entered
   *
   * @type Boolean
   */
  canChangePassword: function() {
    if (!SC.empty(this.get('oldPassword')) && !SC.empty(this.get('newPassword')) &&
      !SC.empty(this.get('confirmNewPassword')) && !this.get('isChangingPassword')) {
      return YES;
    }
    return NO;
  }.property('oldPassword', 'newPassword', 'confirmNewPassword').cacheable(),

  /**
   * Flag to indicate if we are in the middle of trying to save a profile
   */
  isChangingPassword: NO,

  /**
   * Result of calling changePassword(). YES if success, SC.Error if error, null if not set or executing.
   *
   * @type Object
   */
  changePasswordResult: null,

  /**
   * Trigger event to change the user's password
   */
  changePassword: function() {
    Chililog.statechart.sendEvent('changePassword');
  }

});

