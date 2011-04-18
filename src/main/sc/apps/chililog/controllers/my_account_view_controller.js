// ==========================================================================
// Project:   Chililog.mainPaneController
// Copyright: ©2011 My Company, Inc.
// ==========================================================================


/**
 * States of the mainPane in the mainPage.
 */
Chililog.myAccountViewStates = {
  UNLOADED: 'Unloaded',
  LOADED: 'Loaded',
  SAVING: 'Saving',
  CHANGING_PASSWORD: 'ChangingPassword'
}

/**
 * Controller that for the mainPane. Mainly handles menu selection option and login/logout
 *
 * @extends SC.Object
 */
Chililog.myAccountViewController = SC.ObjectController.create(
/** @scope Chililog.mainPaneController.prototype */ {

  /**
   * Determines the current state of the view
   * @type State
   */
  state: Chililog.myAccountViewStates.UNLOADED,

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
   * When the state in the controller changes, we change this pane to reflect it
   */
  mainPaneStateDidChange: function() {
    var state = this.get('state');
    var mainPaneState = Chililog.mainPaneController.get('state');

    if (mainPaneState === Chililog.mainPaneStates.MY_ACCOUNT) {
      if (state === Chililog.myAccountViewStates.UNLOADED) {
        this.load();
      }
    }
    else if (mainPaneState !== Chililog.mainPaneStates.MY_ACCOUNT && state !== Chililog.myAccountViewStates.UNLOADED) {
      this.unload();
    }
  }.observes('Chililog.mainPaneController.state'),

  /**
   * Loads the data ready for editing. Sets the state to LOADED.
   */
  load: function() {
    if (this.get('state') !== Chililog.myAccountViewStates.UNLOADED) {
      throw Chililog.$error('_illegalStateError', [this.get('state')]);
    }

    var authenticatedUserRecord = Chililog.sessionDataController.editProfile();
    if (SC.none(authenticatedUserRecord)) {
      // Not logged in ... better unload
      this.unload();
      return;
    }

    this.set('oldPassword', null);
    this.set('newPassword', null);
    this.set('confirmNewPassword', null);

    this.set('content', authenticatedUserRecord);
    this.set('state', Chililog.myAccountViewStates.LOADED);
    return;
  },

  /**
   * Unloads the data when the view is no longer visible. Unsaved changes will be discarded.
   * Sets the state to UNLOADED.
   */
  unload: function() {
    var authenticatedUserRecord = this.get('content');
    Chililog.sessionDataController.discardProfileChanges(authenticatedUserRecord);
    this.set('state', Chililog.myAccountViewStates.UNLOADED);
    return;
  },

  /**
   * Flag to indicate if the user's profile can be saved.
   * Can only be saved if form is loaded and the data has changed
   *
   * @type Boolean
   */
  canSaveProfile: function() {
    var state = this.get('state');
    var recordStatus = this.getPath('content.status');

    if (!SC.none(state) && !SC.none(recordStatus)) {
      if (state === Chililog.myAccountViewStates.LOADED && recordStatus !== SC.Record.READY_CLEAN) {
        return YES;
      }
    }

    return NO;
  }.property('state', 'content.status').cacheable(),

  /**
   * Result of calling saveProfile(). YES if success, SC.Error if error, null if not set or executing.
   *
   * @type Object
   */
  saveProfileResult: null,

  /**
   * Saves the user's details
   *
   * @returns {Boolean} YES if ok, NO if error. Error object set to the 'error' property
   */
  saveProfile: function() {
    try {
      if (this.get('state') !== Chililog.myAccountViewStates.LOADED) {
        throw Chililog.$error('_illegalStateError', [this.get('state')]);
      }

      Chililog.sessionDataController.saveProfile(this.get('content'), this, this.endSaveProfile);

      this.set('saveProfileResult', null);
      this.set('state', Chililog.myAccountViewStates.SAVING);
      return YES;
    }
    catch (err) {
      SC.Logger.error('Chililog.myAccountViewController.save: ' + err);
      this.set('saveProfileResult', err);
      this.set('state', Chililog.myAccountViewStates.LOADED);
      return NO;
    }

  },

  /**
   * Callback from login() after we get a response from the server to process
   * the returned login info.
   *
   * @param {SC.Error} error Error object or null if no error.
   */
  endSaveProfile: function(error) {
    if (SC.none(error)) {
      // Reload the data
      var authenticatedUserRecord = Chililog.sessionDataController.editProfile();
      this.set('content', authenticatedUserRecord);
      this.set('saveProfileResult', YES);
    } else {
      // Show error
      this.set('saveProfileResult', error);
    }

    // Return to edit mode from BUSY
    this.set('state', Chililog.myAccountViewStates.LOADED);
  },

  /**
   * Restore previous values
   */
  discardProfileChanges: function() {
    this.unload();
    this.load();
  },

  /**
   * Flag to indicate if the user is able to change password
   * Only valid if old, new and confirm passwords have been entered
   *
   * @type Boolean
   */
  canChangePassword: function() {
    if (!SC.empty(this.get('oldPassword')) && !SC.empty(this.get('newPassword')) &&
      !SC.empty(this.get('confirmNewPassword'))) {
      return YES;
    }
    return NO;
  }.property('oldPassword', 'newPassword', 'confirmNewPassword').cacheable(),

  /**
   * Result of calling changePassword(). YES if success, SC.Error if error, null if not set or executing.
   *
   * @type Object
   */
  changePasswordResult: null,

  /**
   * Change the user's password
   */
  changePassword: function () {
    try {
      if (this.get('state') !== Chililog.myAccountViewStates.LOADED) {
        throw Chililog.$error('_illegalStateError', [this.get('state')]);
      }

      Chililog.sessionDataController.changePassword(this.get('oldPassword'), this.get('newPassword'),
        this.get('confirmNewPassword'), this, this.endChangePassword);

      this.set('changePasswordResult', null);
      this.set('state', Chililog.myAccountViewStates.CHANGING_PASSWORD);
      return YES;
    }
    catch (err) {
      SC.Logger.error('Chililog.myAccountViewController.changePassword: ' + err);
      this.set('changePasswordResult', err);
      this.set('state', Chililog.myAccountViewStates.LOADED);
      return NO;
    }

  },

  /**
   * Callback from login() after we get a response from the server to process
   * the returned login info.
   *
   * @param {SC.Error} error Error object or null if no error.
   */
  endChangePassword: function(error) {
    if (SC.none(error)) {
      // Reload the data
      var authenticatedUserRecord = Chililog.sessionDataController.editProfile();
      this.set('oldPassword', null);
      this.set('newPassword', null);
      this.set('confirmNewPassword', null);
      this.set('content', authenticatedUserRecord);
      this.set('changePasswordResult', YES);
    } else {
      // Show error
      error.set('label', 'oldPassword');
      this.set('changePasswordResult', error);
    }

    // Return to edit mode from BUSY
    this.set('state', Chililog.myAccountViewStates.LOADED);
  }


});

