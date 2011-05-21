// ==========================================================================
// Project:   Chililog
// Copyright: ©2011 My Company, Inc.
// ==========================================================================

/**
 * State chart for my account my profile screens
 */
Chililog.MyProfileState = SC.State.extend({

  initialSubstate: 'editingMyProfile',

  /**
   * Makes sure that the menu is placed on My Profile
   */
  enterState: function() {
    Chililog.myAccountViewController.selectMyProfileMenuItem();
    Chililog.myAccountMyProfileViewController.show();
  },

  /**
   * Load user's profile and password field cleared for user to edit
   */
  editingMyProfile: SC.State.design({
    /**
     * Loads our data if required.
     *
     * @param {Hash} [context] Supports 'loadData' parameter. If YES (default, then data is loaded). If NO, then
     * screen data kept as is.
     */
    enterState: function(context) {
      var ctrl = Chililog.myAccountMyProfileViewController;
      
      var loadData = YES;
      if (!SC.none(context) && !SC.none(context['loadData'])) {
        loadData = context['loadData'];
      }

      if (loadData || SC.none(ctrl.get('content'))) {
        var authenticatedUserRecord = Chililog.sessionDataController.editProfile();
        if (SC.none(authenticatedUserRecord)) {
          // Not logged in!
          this.gotoState('loggedOut');
          return;
        }
        ctrl.set('content', authenticatedUserRecord);
      }

      return;
    },

    /**
     * Saves the user's details
     */
    save: function() {
      this.gotoState('savingMyProfile');
    },

    /**
     * Restore previous values
     */
    discardChanges: function() {
      var ctrl = Chililog.myAccountMyProfileViewController;
      
      // Discard
      var authenticatedUserRecord = ctrl.get('content');
      Chililog.sessionDataController.discardProfileChanges(authenticatedUserRecord);

      // Reload
      authenticatedUserRecord = Chililog.sessionDataController.editProfile();
      if (SC.none(authenticatedUserRecord)) {
        // Not logged in!
        this.gotoState('loggedOut');
        return;
      }
      ctrl.set('content', authenticatedUserRecord);
    }
  }),

  /**
   * We trigger async saving of the user's profile
   */
  savingMyProfile: SC.State.design({
    
    enterState: function() {
      Chililog.myAccountMyProfileViewController.set('isSaving', YES);
      this.save();
    },

    exitState: function() {
      Chililog.myAccountMyProfileViewController.set('isSaving', NO);
    },

    /**
     * Saves the user's details
     */
    save: function() {
      var ctrl = Chililog.myAccountMyProfileViewController;
      try {
        Chililog.sessionDataController.saveProfile(ctrl.get('content'), this, this.endSave);
      }
      catch (error) {
        SC.Logger.error('savingMyProfile.save: ' + error);
        ctrl.showError(error);
        this.gotoState('editingMyProfile', {loadData: NO});
      }
    },

    /**
     * Callback from save() after we get a response from the server to process the returned info.
     *
     * @param {Hash} params callback function parameters
     * @param {SC.Error} error Error object or null if no error.
     */
    endSave: function(params, error) {
      var ctrl = Chililog.myAccountMyProfileViewController;
      if (SC.none(error)) {
        // Reload the data
        ctrl.showSaveProfileSuccess();
        this.gotoState('editingMyProfile', {loadData: YES});
      } else {
        // Show error
        ctrl.showError(error);
        this.gotoState('editingMyProfile', {loadData: NO});
      }
    }
  })

});