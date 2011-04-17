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
   * When the state in the controller changes, we change this pane to reflect it
   */
  mainPaneStateDidChange: function() {
    var state = this.get('state');
    var mainPaneState = Chililog.mainPaneController.get('state');

    if (mainPaneState === Chililog.mainPaneStates.MY_ACCOUNT && state !== Chililog.myAccountViewStates.LOADED) {
      this.load();
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

    var nestedStore = Chililog.store.chain();
    var authenticatedUserRecord = Chililog.sessionDataController.get('loggedInUser');
    if (SC.none(authenticatedUserRecord)) {
      // Not logged in ... better unload
      this.unload();
      return;
    }

    authenticatedUserRecord = nestedStore.find(authenticatedUserRecord);

    this.set('content', authenticatedUserRecord);
    this.set('state', Chililog.myAccountViewStates.LOADED);
  },

  /**
   * Unloads the data when the view is no longer visible. Unsaved changes will be discarded.
   * Sets the state to UNLOADED.
   */
  unload: function() {
    var authenticatedUserRecord = this.get('content');
    if (!SC.none(authenticatedUserRecord)) {
      var nestedStore = authenticatedUserRecord.get('store');
      nestedStore.destroy();
    }
    
    this.set('state', Chililog.myAccountViewStates.UNLOADED);
  },

  /**
   * Saves the user's details
   */
  save: function() {
    if (this.get('state') !== Chililog.myAccountViewStates.LOADED) {
      throw Chililog.$error('_illegalStateError', [this.get('state')]);
    }

    alert('save')
    //this.set('state', Chililog.myAccountViewStates.SAVING);
  },

  /**
   * Change the user's password
   */
  changePassword: function () {
    if (this.get('state') !== Chililog.myAccountViewStates.LOADED) {
      throw Chililog.$error('_illegalStateError', [this.get('state')]);
    }

    alert('change password')
    //this.set('state', Chililog.myAccountViewStates.CHANGING_PASSWORD);
  }


});

