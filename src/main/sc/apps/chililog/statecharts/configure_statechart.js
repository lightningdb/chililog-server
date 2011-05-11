// ==========================================================================
// Project:   Chililog
// Copyright: ©2011 My Company, Inc.
// ==========================================================================

/**
 * State chart for configure screens
 */
Chililog.ConfigureState = SC.State.extend({

  initialSubstate: 'viewingRepositoryInfo',

  /**
   * Show my profile page in the body
   */
  enterState: function() {
    Chililog.mainViewController.doShow('configure');

  },

/*********************************************************************************************************************
 * User States
 ********************************************************************************************************************/

  /**
   * List users in table view
   */
  viewingUsers: SC.State.design({

    initialSubstate: 'viewingUsers_Idle',

    enterState: function() {
      var ctrl = Chililog.configureUserListViewController;
      if (SC.none(ctrl.get('content'))) {
        var userQuery = SC.Query.local(Chililog.UserRecord, { orderBy: 'username' });
        var users = Chililog.store.find(userQuery);
        ctrl.set('content', users);
      }
      ctrl.show();
    },

    exitState: function() {
      return;
    },

    /**
     * Let the user browser repositories
     */
    viewingUsers_Idle: SC.State.design({
      enterState: function() {
        return;
      },
      exitState: function() {
        return;
      }
    }),

    /**
     * Trigger refreshing of repository data
     */
    viewingUsers_Refreshing: SC.State.design({
      enterState: function() {
        this.doRefresh();
      },

      exitState: function() {
      },

      doRefresh: function() {
        Chililog.progressPane.show('_refreshing'.loc());
        Chililog.userDataController.synchronizeWithServer(NO, this, this.endRefresh);
      },

      endRefresh: function(params, error) {
        var ctrl = Chililog.configureUserListViewController;
        if (SC.none(error)) {
          Chililog.progressPane.hideWithoutFlashing();
        } else {
          Chililog.progressPane.hide();
          ctrl.showError(error);
        }
        this.gotoState('viewingUsers_Idle');
      }
    }),

    /**
     * Refresh event
     */
    refresh: function() {
      this.gotoState('viewingUsers_Refreshing');
    }

  }),

  /**
   * Show blank user details page for adding a new user
   */
  creatingUser: SC.State.design({
    /**
     * Load user record via the data controller and put it in the view controller
     *
     * @param {Hash} context Data hash with 'documentID' set to the document id f the user to edit. Alternatively,
     *  set 'reedit' to YES, then data will be left as is.
     *
     */
    enterState: function(context) {
      var isReedit = !SC.none(context) && context['isReedit'];
      if (!isReedit) {
        var record = Chililog.userDataController.create();
        var ctrl = Chililog.configureUserDetailViewController;
        ctrl.set('content', record);
        ctrl.set('isSaving', NO);
        ctrl.show();
      }
    },

    /**
     * Discard changes unless we are saving or re-editing
     *
     * @param {Hash} context Data hash with 'isSaving' flag to indicate if we are moving to the save
     */
    exitState: function(context) {
      var isSaving = !SC.none(context) && context['isSaving'];
      var isReedit = !SC.none(context) && context['isReedit'];
      if (!isSaving && !isReedit) {
        var record = Chililog.configureUserDetailViewController.get('content');
        Chililog.userDataController.discardChanges(record);
      }
    },

    /**
     * Save changes
     */
    save: function() {
      this.gotoState('savingUser', {isSaving: YES});
    },

    /**
     * Discard changes and reload our data to the
     */
    discardChanges: function() {
      this.gotoState('viewingUsers');
    }
  }),

  /**
   * Load existing user for the user to edit
   */
  editingUser: SC.State.design({
    /**
     * Load user record via the data controller and put it in the view controller
     *
     * @param {Hash} context Data hash with 'documentID' set to the document id f the user to edit. Alternatively,
     *  set 'reedit' to YES, then data will be left as is.
     *
     */
    enterState: function(context) {
      var isReedit = !SC.none(context) && context['isReedit'];
      if (!isReedit) {
        var record = Chililog.userDataController.edit(context['documentID']);
        var ctrl = Chililog.configureUserDetailViewController;
        ctrl.set('content', record);
        ctrl.set('isSaving', NO);
        ctrl.show();
      }
    },

    /**
     * Discard changes unless we are saving or re-editing
     *
     * @param {Hash} context Data hash with 'isSaving' flag to indicate if we are moving to the save
     */
    exitState: function(context) {
      var isSaving = !SC.none(context) && context['isSaving'];
      var isReedit = !SC.none(context) && context['isReedit'];
      if (!isSaving && !isReedit) {
        var record = Chililog.configureUserDetailViewController.get('content');
        Chililog.userDataController.discardChanges(record);
      }
    },

    /**
     * Save changes
     */
    save: function() {
      this.gotoState('savingUser', {isSaving: YES});
    },

    /**
     * Discard changes and reload our data to the
     */
    discardChanges: function() {
      this.gotoState('viewingUsers');
      //var record = Chililog.configureUserDetailViewController.get('content');
      //this.gotoState('editingUser', {documentID: record.get(Chililog.DOCUMENT_ID_RECORD_FIELD_NAME)});
    }
  }),

  /**
   * Asynchronous call triggered to save user details and wait for server to response
   */
  savingUser: SC.State.design({
    enterState: function() {
      Chililog.configureUserDetailViewController.set('isSaving', YES);
      this.save();
    },

    exitState: function() {
      Chililog.configureUserDetailViewController.set('isSaving', NO);
    },

    /**
     * Saves the user's details
     */
    save: function() {
      var ctrl = Chililog.configureUserDetailViewController;
      try {
        Chililog.userDataController.save(ctrl.get('content'), this, this.endSave);
      }
      catch (error) {
        SC.Logger.error('savingUser.save: ' + error);
        ctrl.showError(error);
        var stateToGoTo = ctrl.get('isCreating') ? 'creatingUser' : 'editingUser';
        this.gotoState(stateToGoTo, {isReedit: YES});
      }
    },

    /**
     * Callback from save() after we get a response from the server to process the returned info.
     *
     * @param {String} document id of the saved record. Null if error.
     * @param {Hash} params callback function parameters
     * @param {SC.Error} error Error object or null if no error.
     */
    endSave: function(documentID, params, error) {
      var ctrl = Chililog.configureUserDetailViewController;
      if (SC.none(error)) {
        // Show saved record
        ctrl.showSaveSuccess();
        this.gotoState('editingUser', {documentID: documentID});
      } else {
        // Show error
        ctrl.showError(error);
        var stateToGoTo = ctrl.get('isCreating') ? 'creatingUser' : 'editingUser';
        this.gotoState(stateToGoTo, {isReedit: YES});
      }
    }
  }),

  /**
   * Asynchronous call triggered to delete the user
   */
  erasingUser: SC.State.design({
    enterState: function(context) {
      Chililog.configureUserDetailViewController.set('isErasing', YES);
      this.erase(context);
    },

    exitState: function() {
      Chililog.configureUserDetailViewController.set('isErasing', NO);
    },

    /**
     * Delete the user
     */
    erase: function(context) {
      var ctrl = Chililog.configureUserDetailViewController;
      try {
        Chililog.userDataController.erase(context['documentID'], this, this.endErase);
      }
      catch (error) {
        SC.Logger.error('erasingUser.erase: ' + error);
        ctrl.showError(error);
        this.gotoState('editingUser', {documentID: context['documentID']});
      }
    },

    /**
     * Callback from save() after we get a response from the server to process the returned info.
     *
     * @param {String} document id of the saved record. Null if error.
     * @param {Hash} params callback function parameters
     * @param {SC.Error} error Error object or null if no error.
     */
    endErase: function(documentID, params, error) {
      var ctrl = Chililog.configureUserDetailViewController;
      if (SC.none(error)) {
        this.gotoState('viewingUsers');
      } else {
        // Show error
        ctrl.showError(error);
        this.gotoState('editingUser', {documentID: documentID});
      }
    }
  }),

/*********************************************************************************************************************
 * Repositories State
 ********************************************************************************************************************/

  /**
   * List repositories in table view
   */
  viewingRepositoryInfo: SC.State.design({

    initialSubstate: 'viewingRepositoryInfo_Idle',

    enterState: function() {
      var ctrl = Chililog.configureRepositoryInfoListViewController;
      if (SC.none(ctrl.get('content'))) {
        var repoInfoQuery = SC.Query.local(Chililog.RepositoryInfoRecord, { orderBy: 'name' });
        var repoInfo = Chililog.store.find(repoInfoQuery);
        ctrl.set('content', repoInfo);
      }
      ctrl.show();
    },

    exitState: function() {
      return;
    },

    /**
     * Let the user browser repositories
     */
    viewingRepositoryInfo_Idle: SC.State.design({
      enterState: function() {
        return;
      },
      exitState: function() {
        return;
      }
    }),

    /**
     * Trigger refreshing of repository data
     */
    viewingRepositoryInfo_Refreshing: SC.State.design({
      enterState: function() {
        this.doRefresh();
      },

      exitState: function() {
      },

      doRefresh: function() {
        Chililog.progressPane.show('_refreshing'.loc());
        Chililog.repositoryDataController.synchronizeWithServer(NO, YES, this, this.endRefresh);
      },

      endRefresh: function(params, error) {
        var ctrl = Chililog.configureRepositoryInfoListViewController;
        if (SC.none(error)) {
          Chililog.progressPane.hideWithoutFlashing();
        } else {
          Chililog.progressPane.hide();
          ctrl.showError(error);
        }
        this.gotoState('viewingRepositoryInfo_Idle');
      }
    }),

    /**
     * Trigger starting a repository
     */
    viewingRepositoryInfo_Starting: SC.State.design({
      enterState: function(context) {
        var documentID = context['documentID'];
        this.doStart(documentID);
      },

      exitState: function() {
      },

      doStart: function(documentID) {
        Chililog.progressPane.show('_starting'.loc());
        Chililog.repositoryDataController.start(documentID, this, this.endStart);
      },

      endStart: function(documentID, params, error) {
        var ctrl = Chililog.configureRepositoryInfoListViewController;
        if (SC.none(error)) {
          Chililog.progressPane.hideWithoutFlashing();
        } else {
          Chililog.progressPane.hide();
          ctrl.showError(error);
        }
        this.gotoState('viewingRepositoryInfo_Idle');
      }
    }),

    /**
     * Trigger stopping a repository
     */
    viewingRepositoryInfo_Stopping: SC.State.design({
      enterState: function(context) {
        var documentID = context['documentID'];
        this.doStop(documentID);
      },

      exitState: function() {
      },

      doStop: function(documentID) {
        Chililog.progressPane.show('_stopping'.loc());
        Chililog.repositoryDataController.stop(documentID, this, this.endStop);
      },

      endStop: function(documentID, params, error) {
        var ctrl = Chililog.configureRepositoryInfoListViewController;
        if (SC.none(error)) {
          Chililog.progressPane.hideWithoutFlashing();
        } else {
          Chililog.progressPane.hide();
          ctrl.showError(error);
        }
        this.gotoState('viewingRepositoryInfo_Idle');
      }
    }),

    /**
     * Refresh event
     */
    refresh: function() {
      this.gotoState('viewingRepositoryInfo_Refreshing');
    },

    /**
     * Start repository event
     *
     * @param {String} documentID id of repository to start
     */
    start: function(documentID) {
      this.gotoState('viewingRepositoryInfo_Starting', {documentID: documentID});
    },

    /**
     * Start repository event
     *
     * @param {String} documentID id of repository to start
     */
    stop: function(documentID) {
      this.gotoState('viewingRepositoryInfo_Stopping', {documentID: documentID});
    }

  }),

  /**
   * Blank repository details view for user to add
   */
  creatingRepositoryInfo: SC.State.design({
    /**
     * Load repository info record via the data controller and put it in the view controller
     *
     * @param {Hash} context Data hash with 'documentID' set to the document id f the user to edit. Alternatively,
     *  set 'reedit' to YES, then data will be left as is.
     *
     */
    enterState: function(context) {
      var isReedit = !SC.none(context) && context['isReedit'];
      if (!isReedit) {
        var record = Chililog.repositoryInfoDataController.create();
        var ctrl = Chililog.configureRepositoryInfoDetailViewController;
        ctrl.set('content', record);
        ctrl.set('isSaving', NO);
        ctrl.show();
      }
    },

    /**
     * Discard changes unless we are saving or re-editing
     *
     * @param {Hash} context Data hash with 'isSaving' flag to indicate if we are moving to the save
     */
    exitState: function(context) {
      var isSaving = !SC.none(context) && context['isSaving'];
      var isReedit = !SC.none(context) && context['isReedit'];
      if (!isSaving && !isReedit) {
        var record = Chililog.configureRepositoryInfoDetailViewController.get('content');
        Chililog.repositoryInfoDataController.discardChanges(record);
      }
    },

    /**
     * Save changes
     */
    save: function() {
      this.gotoState('savingRepositoryInfo', {isSaving: YES});
    },

    /**
     * Discard changes and reload our data to the
     */
    discardChanges: function() {
      this.gotoState('viewingRepositoryInfo');
    }

  }),

  /**
   * Load existing repository details view for user to edit
   */
  editingRepositoryInfo: SC.State.design({
    /**
     * Load repository info record via the data controller and put it in the view controller
     *
     * @param {Hash} context Data hash with 'documentID' set to the document id f the user to edit. Alternatively,
     *  set 'reedit' to YES, then data will be left as is.
     *
     */
    enterState: function(context) {
      var isReedit = !SC.none(context) && context['isReedit'];
      if (!isReedit) {
        var record = Chililog.repositoryInfoDataController.edit(context['documentID']);
        var ctrl = Chililog.configureRepositoryInfoDetailViewController;
        ctrl.set('content', record);
        ctrl.set('isSaving', NO);
        ctrl.show();
      }
    },

    /**
     * Discard changes unless we are saving or re-editing
     *
     * @param {Hash} context Data hash with 'isSaving' flag to indicate if we are moving to the save
     */
    exitState: function(context) {
      var isSaving = !SC.none(context) && context['isSaving'];
      var isReedit = !SC.none(context) && context['isReedit'];
      if (!isSaving && !isReedit) {
        var record = Chililog.configureRepositoryInfoDetailViewController.get('content');
        Chililog.repositoryInfoDataController.discardChanges(record);
      }
    },

    /**
     * Save changes
     */
    save: function() {
      this.gotoState('savingRepositoryInfo', {isSaving: YES});
    },

    /**
     * Discard changes and reload our data to the
     */
    discardChanges: function() {
      this.gotoState('viewingRepositoryInfo');
    }
  }),

  /**
   * Asynchronous call triggered to save repository details and wait for server to response
   */
  savingRepositoryInfo: SC.State.design({
    enterState: function() {
      Chililog.configureRepositoryInfoDetailViewController.set('isSaving', YES);
      this.save();
    },

    exitState: function() {
      Chililog.configureRepositoryInfoDetailViewController.set('isSaving', NO);
    },

    /**
     * Saves the repository's details
     */
    save: function() {
      var ctrl = Chililog.configureRepositoryInfoDetailViewController;
      try {
        Chililog.repositoryInfoDataController.save(ctrl.get('content'), this, this.endSave);
      }
      catch (error) {
        SC.Logger.error('savingRepositoryInfo.save: ' + error);
        ctrl.showError(error);
        var stateToGoTo = ctrl.get('isCreating') ? 'creatingUser' : 'editingUser';
        this.gotoState(stateToGoTo, {isReedit: YES});
      }
    },

    /**
     * Callback from save() after we get a response from the server to process the returned info.
     *
     * @param {String} documentID of the saved record. Null if error.
     * @param {Hash} params callback function parameters
     * @param {SC.Error} error Error object or null if no error.
     */
    endSave: function(documentID, params, error) {
      var ctrl = Chililog.configureRepositoryInfoDetailViewController;
      if (SC.none(error)) {
        // Show saved record
        ctrl.showSaveSuccess();
        this.gotoState('editingRepositoryInfo', {documentID: documentID});
      } else {
        // Show error
        ctrl.showError(error);
        var stateToGoTo = ctrl.get('isCreating') ? 'creatingRepositoryInfo' : 'editingRepositoryInfo';
        this.gotoState(stateToGoTo, {isReedit: YES});
      }
    }
  }),

  /**
   * Asynchronous call triggered to delete the repository
   */
  erasingRepositoryInfo: SC.State.design({
    enterState: function(context) {
      Chililog.configureRepositoryInfoDetailViewController.set('isErasing', YES);
      this.erase(context);
    },

    exitState: function() {
      Chililog.configureRepositoryInfoDetailViewController.set('isErasing', NO);
    },

    /**
     * Delete the repository
     */
    erase: function(context) {
      var ctrl = Chililog.configureRepositoryInfoDetailViewController;
      try {
        Chililog.repositoryInfoDataController.erase(context['documentID'], this, this.endErase);
      }
      catch (error) {
        SC.Logger.error('erasingRepositoryInfo.erase: ' + error);
        ctrl.showError(error);
        this.gotoState('editingRepositoryInfo', {documentID: context['documentID']});
      }
    },

    /**
     * Callback from save() after we get a response from the server to process the returned info.
     *
     * @param {String} document id of the saved record. Null if error.
     * @param {Hash} params callback function parameters
     * @param {SC.Error} error Error object or null if no error.
     */
    endErase: function(documentID, params, error) {
      var ctrl = Chililog.configureRepositoryInfoDetailViewController;
      if (SC.none(error)) {
        this.gotoState('viewingRepositoryInfo');
      } else {
        // Show error
        ctrl.showError(error);
        this.gotoState('editingRepositoryInfo', {documentID: documentID});
      }
    }
  }),

/*********************************************************************************************************************
 * Events
 ********************************************************************************************************************/
  /**
   * Event to view users
   */
  viewUsers: function() {
    this.gotoState('viewingUsers');
  },

  /**
   * Event to start adding a new user
   */
  createUser: function() {
    this.gotoState('creatingUser');
  },

  /**
   * Event to start editing users
   *
   * @param {String} documentID unique id for the user record
   */
  editUser: function(documentID) {
    this.gotoState('editingUser', {documentID: documentID});
  },

  /**
   * Event to start deleting user
   *
   * @param {String} documentID unique id for the user record
   */
  eraseUser: function(documentID) {
    this.gotoState('erasingUser', {documentID: documentID});
  },

  /**
   * Event to view repositories
   */
  viewRepositoryInfo: function() {
    this.gotoState('viewingRepositoryInfo');
  },

  /**
   * Event to create repositories
   */
  createRepositoryInfo: function() {
    this.gotoState('creatingRepositoryInfo');
  },

  /**
   * Event to start editing repository info
   *
   * @param {String} documentID unique id for the user record
   */
  editRepositoryInfo: function(documentID) {
    this.gotoState('editingRepositoryInfo', {documentID: documentID});
  },

  /**
   * Event to start delete repository info
   *
   * @param {String} documentID unique id for the user record
   */
  eraseRepositoryInfo: function(documentID) {
    this.gotoState('erasingRepositoryInfo', {documentID: documentID});
  }


});