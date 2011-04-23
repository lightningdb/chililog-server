// ==========================================================================
// Project:   Chililog
// Copyright: ©2011 My Company, Inc.
// ==========================================================================

Chililog.MyAccountState = SC.State.extend({

  initialSubstate: 'myAccountLoaded',

  /**
   * Show my profile page in the body
   */
  enterState: function() {
    Chililog.mainPage.setPath('mainPane.toolBar.menuOptions.value', '');
    Chililog.mainPage.setPath('mainPane.toolBar.myProfileButton.value', YES);
    var body = Chililog.mainPage.getPath('mainPane.body');
    body.set('nowShowing', 'Chililog.myAccountView');
  },

  /**
   * Make sure the toggle button for my profile is not on
   */
  exitState: function() {
    Chililog.mainPage.setPath('mainPane.toolBar.myProfileButton.value', NO);
  },

  /**
   * User's profile and password field cleared for user to fill in
   */
  myAccountLoaded: SC.State.design({
    /**
     * Loads our data if required.
     *
     * @param {Hash} [context] Supports 'loadData' parameter. If YES (default, then data is loaded). If NO, then
     * screen data kept as is.
     */
    enterState: function(context) {
      var ctrl = Chililog.myAccountViewController;
      var loadData = YES;
      if (!SC.none(context) && !SC.none(context['loadData'])) {
        loadData = context['loadData'];
      }

      if (loadData || SC.none(ctrl.get('content'))) {
        this._load();
      }
      return;
    },

    /**
     * Saves the user's details
     */
    saveProfile: function() {
      this.gotoState('savingMyAccountProfile');
    },

    /**
     * Restore previous values
     */
    discardProfileChanges: function() {
      this._load();
    },

    /**
     * Saves the user new password
     */
    changePassword: function() {
      this.gotoState('changingMyAccountPassword');
    },

    /**
     * @private
     *
     * Loads our view controller with data
     */
    _load: function() {
      var ctrl = Chililog.myAccountViewController;
      var authenticatedUserRecord = Chililog.sessionDataController.editProfile();
      if (SC.none(authenticatedUserRecord)) {
        // Not logged in ... better unload
        this.unload();
        return;
      }

      ctrl.set('oldPassword', null);
      ctrl.set('newPassword', null);
      ctrl.set('confirmNewPassword', null);
      ctrl.set('content', authenticatedUserRecord);
    }
  }),

  /**
   * We trigger async saving of the user's profile
   */
  savingMyAccountProfile: SC.State.design({
    
    enterState: function() {
      Chililog.myAccountViewController.set('isSavingProfile', YES);
      this.save();
    },

    exitState: function() {
      Chililog.myAccountViewController.set('isSavingProfile', NO);
    },

    /**
     * Saves the user's details
     *
     * @returns {Boolean} YES if ok, NO if error. Error object set to the 'error' property
     */
    save: function() {
      var ctrl = Chililog.myAccountViewController;
      try {
        Chililog.sessionDataController.saveProfile(ctrl.get('content'), this, this.endSave);
        ctrl.set('saveProfileResult', null);
      }
      catch (err) {
        SC.Logger.error('savingMyAccountProfile.save: ' + err);
        ctrl.set('saveProfileResult', err);
        this.gotoState('myAccountLoaded', {loadData: NO});
      }
    },

    /**
     * Callback from save() after we get a response from the server to process the returned info.
     *
     * @param {SC.Error} error Error object or null if no error.
     */
    endSave: function(error) {
      var ctrl = Chililog.myAccountViewController;
      if (SC.none(error)) {
        // Reload the data
        ctrl.set('saveProfileResult', YES);
        this.gotoState('myAccountLoaded', {loadData: YES});
      } else {
        // Show error
        ctrl.set('saveProfileResult', error);
        this.gotoState('myAccountLoaded', {loadData: NO});
      }
    }
  }),

  /**
   * We trigger async changing of password
   */
  changingMyAccountPassword: SC.State.design({

    enterState: function() {
      Chililog.myAccountViewController.set('isChangingPassword', YES);
      this.changePassword();
    },

    exitState: function() {
      Chililog.myAccountViewController.set('isChangingPassword', NO);
    },

    /**
     * Change the user's password
     */
    changePassword: function () {
      var ctrl = Chililog.myAccountViewController;
      try {
        Chililog.sessionDataController.changePassword(ctrl.get('oldPassword'), ctrl.get('newPassword'),
          ctrl.get('confirmNewPassword'), this, this.endChangePassword);

        this.set('changePasswordResult', null);
      }
      catch (err) {
        SC.Logger.error('changingMyAccountPassword.changePassword: ' + err);
        this.set('changePasswordResult', err);
        this.gotoState('myAccountLoaded', {loadData: NO});
      }

    },

    /**
     * Callback from login() after we get a response from the server to process
     * the returned login info.
     *
     * @param {SC.Error} error Error object or null if no error.
     */
    endChangePassword: function(error) {
      var ctrl = Chililog.myAccountViewController;
      if (SC.none(error)) {
        // Reload the data
        ctrl.set('changePasswordResult', YES);
        this.gotoState('myAccountLoaded', {loadData: YES});
      } else {
        // Show error
        error.set('label', 'oldPassword');
        ctrl.set('changePasswordResult', error);
        this.gotoState('myAccountLoaded', {loadData: NO});
      }
    }
  })

});