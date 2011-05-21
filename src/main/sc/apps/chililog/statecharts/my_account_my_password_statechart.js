// ==========================================================================
// Project:   Chililog
// Copyright: ©2011 My Company, Inc.
// ==========================================================================

/**
 * State chart for my account change password screens
 */
Chililog.MyPasswordState = SC.State.extend({

  initialSubstate: 'changingMyPassword',

  /**
   * Makes sure that the menu is placed on Change Password
   */
  enterState: function() {
    Chililog.myAccountViewController.selectChangePasswordMenuItem();
    Chililog.myAccountMyPasswordViewController.show();
  },

  /**
   * Load user's profile and password field cleared for user to edit
   */
  changingMyPassword: SC.State.design({
    /**
     * Loads our data if required.
     *
     * @param {Hash} [context] Supports 'loadData' parameter. If YES (default, then data is loaded). If NO, then
     * screen data kept as is.
     */
    enterState: function(context) {
      var ctrl = Chililog.myAccountMyPasswordViewController;

      var loadData = YES;
      if (!SC.none(context) && !SC.none(context['loadData'])) {
        loadData = context['loadData'];
      }

      if (loadData) {
        ctrl.set('oldPassword', null);
        ctrl.set('newPassword', null);
        ctrl.set('confirmNewPassword', null);
      }

      return;
    },

    /**
     * Saves the password details
     */
    save: function() {
      this.gotoState('savingMyPassword');
    },

    /**
     * Restore previous values
     */
    discardChanges: function() {
      var ctrl = Chililog.myAccountMyPasswordViewController;
      ctrl.set('oldPassword', null);
      ctrl.set('newPassword', null);
      ctrl.set('confirmNewPassword', null);
    }

  }),


  /**
   * We trigger async changing of password
   */
  savingMyPassword: SC.State.design({

    enterState: function() {
      Chililog.myAccountMyPasswordViewController.set('isSaving', YES);
      this.changePassword();
    },

    exitState: function() {
      Chililog.myAccountMyPasswordViewController.set('isSaving', NO);
    },

    /**
     * Change the user's password
     */
    changePassword: function () {
      var ctrl = Chililog.myAccountMyPasswordViewController;
      try {
        Chililog.sessionDataController.changePassword(ctrl.get('oldPassword'), ctrl.get('newPassword'),
          ctrl.get('confirmNewPassword'), this, this.endChangePassword);
      }
      catch (error) {
        SC.Logger.error('savingMyPassword.changePassword: ' + error);
        ctrl.showError(error);
        this.gotoState('changingMyPassword', {loadData: NO});
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
      var ctrl = Chililog.myAccountMyPasswordViewController;
      if (SC.none(error)) {
        // Reload the data
        ctrl.showChangePasswordSuccess();
        this.gotoState('changingMyPassword', {loadData: YES});
      } else {
        // Show error
        error.set('label', 'oldPassword');
        ctrl.showError(error);
        this.gotoState('changingMyPassword', {loadData: NO});
      }
    }
  })

});