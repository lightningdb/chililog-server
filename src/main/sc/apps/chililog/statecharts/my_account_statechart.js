// ==========================================================================
// Project:   Chililog
// Copyright: ©2011 My Company, Inc.
// ==========================================================================

/**
 * State chart for my account screens
 */
Chililog.MyAccountState = SC.State.extend({

  initialSubstate: 'editingMyAccount',

  /**
   * Show my profile page in the body
   */
  enterState: function() {
    Chililog.mainViewController.doShow('myAccount');
  },

  /**
   * Load user's profile and password field cleared for user to edit
   */
  editingMyAccount: SC.State.design({
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
     */
    save: function() {
      var ctrl = Chililog.myAccountViewController;
      try {
        Chililog.sessionDataController.saveProfile(ctrl.get('content'), this, this.endSave);
      }
      catch (error) {
        SC.Logger.error('savingMyAccountProfile.save: ' + error);
        ctrl.showError(error);
        this.gotoState('editingMyAccount', {loadData: NO});
      }
    },

    /**
     * Callback from save() after we get a response from the server to process the returned info.
     *
     * @param {Hash} params callback function parameters
     * @param {SC.Error} error Error object or null if no error.
     */
    endSave: function(params, error) {
      var ctrl = Chililog.myAccountViewController;
      if (SC.none(error)) {
        // Reload the data
        ctrl.showSaveProfileSuccess();
        this.gotoState('editingMyAccount', {loadData: YES});
      } else {
        // Show error
        ctrl.showError(error);
        this.gotoState('editingMyAccount', {loadData: NO});
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
      }
      catch (error) {
        SC.Logger.error('changingMyAccountPassword.changePassword: ' + error);
        ctrl.showError(error);
        this.gotoState('editingMyAccount', {loadData: NO});
      }

    },

    /**
     * Callback from changePassword() after we get a response from the server to process
     * the returned login info.
     *
     * @param {Hash} params callback function parameters
     * @param {SC.Error} error Error object or null if no error.
     */
    endChangePassword: function(params, error) {
      var ctrl = Chililog.myAccountViewController;
      if (SC.none(error)) {
        // Reload the data
        ctrl.showChangePasswordSuccess();
        this.gotoState('editingMyAccount', {loadData: YES});
      } else {
        // Show error
        error.set('label', 'oldPassword');
        ctrl.showError(error);
        this.gotoState('editingMyAccount', {loadData: NO});
      }
    }
  })

});