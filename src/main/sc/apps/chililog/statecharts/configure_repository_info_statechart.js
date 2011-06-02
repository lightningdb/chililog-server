// ==========================================================================
// Project:   Chililog
// Copyright: ©2011 My Company, Inc.
// ==========================================================================

/**
 * State chart for configure screens
 */
Chililog.ConfigureRepositoryInfoState = SC.State.extend({

  initialSubstate: 'viewingRepositoryInfo',

  enterState: function() {
    // Make sure that we are selected on the left hand side tree menu
    // This can get out of sync if we navigate from another top level menu like search
    Chililog.configureViewController.selectRepositoriesMenuItem();

    // Setup and show the list view
    var ctrl = Chililog.configureRepositoryInfoListViewController;
    if (SC.none(ctrl.get('content'))) {
      var repoInfoQuery = SC.Query.local(Chililog.RepositoryInfoRecord, { orderBy: 'name' });
      var repoInfo = Chililog.store.find(repoInfoQuery);
      ctrl.set('content', repoInfo);
    }
    ctrl.show();
  },

  /**
   * List repositories in table view
   */
  viewingRepositoryInfo: SC.State.design({

    initialSubstate: 'viewingRepositoryInfo_Idle',

    enterState: function() {
      // Hide modal form if it is showing
      Chililog.configureRepositoryInfoDetailViewController.hide();
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
        // Load record
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
    },

    /**
     * Load another record for editing
     *
     * @param {String} documentID id of record to edit
     */
    editAnother: function (documentID) {
      var ctrl = Chililog.configureRepositoryInfoDetailViewController;

      // Discard changes
      var record = ctrl.get('content');
      Chililog.repositoryInfoDataController.discardChanges(record);

      // Load next record
      record = Chililog.repositoryInfoDataController.edit(documentID);
      ctrl.set('content', record);
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
        //this.gotoState('editingRepositoryInfo', {documentID: documentID});
        this.gotoState('viewingRepositoryInfo_Idle');
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