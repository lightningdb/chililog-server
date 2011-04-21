// ==========================================================================
// Project:   Chililog.mainPaneController
// Copyright: ©2011 My Company, Inc.
// ==========================================================================


/**
 * States of configure tree view
 */
Chililog.configureTreeViewStates = {
  UNLOADED: 'Unloaded',
  ADD_USER: 'AddUser',
  EDIT_USER: 'EditUser',
  USERS: 'Users',
  ADD_REPOSITORY: 'AddRepository',
  EDIT_REPOSITORY: 'EditRepository',
  REPOSITORIES: 'Repositories'
}

/**
 * States of configure user view
 */
Chililog.configureUserViewStates = {
  UNLOADED: 'Unloaded',
  ADDING: 'Adding',
  EDITING: 'Editing',
  SAVING: 'Saving',
  DELETING: 'Deleting'
}

/**
 * States of configure repository view
 */
Chililog.configureRepositoryViewStates = {
  UNLOADED: 'Unloaded',
  LOADED: 'Loaded',
  SAVING: 'Saving',
  DELETING: 'Deleting'
}

/**
 * Controls the data when configuring users
 */
Chililog.configureUserViewController = SC.ObjectController.create({

  /**
   * Determines the current state of the view
   * @type State
   */
  state: Chililog.configureUserViewStates.UNLOADED,

  /**
   * User record to display
   * @type Chililog.UserRecord
   */
  content: null,

  /**
   * Loads the data ready for editing. Sets the state to EDITING.
   */
  edit: function(documentID) {
    if (this.get('state') !== Chililog.configureUserViewStates.UNLOADED) {
      this.unload();
    }
    
    var record = Chililog.userDataController.editRecord(documentID);
    this.set('content', record);
    this.set('state', Chililog.configureUserViewStates.EDITING);
    return;
  },

  /**
   * Unloads the data when the view is no longer visible. Unsaved changes will be discarded.
   * Sets the state to UNLOADED.
   */
  unload: function() {
    var record = this.get('content');

    if (!SC.none(record)) {
      Chililog.userDataController.discardChanges(record);
    }

    this.set('state', Chililog.configureUserViewStates.UNLOADED);
    return;
  },

  /**
   * When the state changes for the tree view, then show the right view in the details pane
   */
  treeViewStateDidChange: function() {
    var state = Chililog.configureTreeViewController.get('state');

    if (state === Chililog.configureTreeViewStates.ADD_USER) {
    }
    else if (state === Chililog.configureTreeViewStates.EDIT_USER) {
      var selectedItem = Chililog.configureTreeViewController.get('selectedItem');
      this.edit(selectedItem.get(Chililog.DOCUMENT_ID_RECORD_FIELD_NAME));
    }
    else {
      this.unload();
    }

  }.observes('Chililog.configureTreeViewController.state')
});

/**
 * Controls the data when configuring repositories
 */
Chililog.configureRepositoryViewController = SC.ObjectController.create({

  /**
   * Determines the current state of the view
   * @type State
   */
  state: Chililog.configureRepositoryViewStates.UNLOADED,

  /**
   * Repository record to display
   * @type Chililog.RepositoryRecord
   */
  content: null,

  /**
   * Loads the data ready for editing. Sets the state to EDITING.
   */
  edit: function(documentID) {
    if (this.get('state') !== Chililog.configureRepositoryViewStates.UNLOADED) {
      this.unload();
    }

    var record = Chililog.repositoryDataController.editRecord(documentID);
    this.set('content', record);
    this.set('state', Chililog.configureRepositoryViewStates.EDITING);
    return;
  },

  /**
   * Unloads the data when the view is no longer visible. Unsaved changes will be discarded.
   * Sets the state to UNLOADED.
   */
  unload: function() {
    var record = this.get('content');

    if (!SC.none(record)) {
      Chililog.repositoryDataController.discardChanges(record);
    }

    this.set('state', Chililog.configureRepositoryViewStates.UNLOADED);
    return;
  }   
});

/**
 * Controller that for the mainPane. Mainly handles menu selection option and login/logout
 *
 * @extends SC.Object
 */
Chililog.configureTreeViewController = SC.TreeController.create({

  /**
   * Determines the current visible details view
   * @type String
   */
  state: Chililog.configureTreeViewStates.UNLOADED,

  /**
   * Poplulate our tree
   */
  populate: function() {
    var repositories = SC.Object.create({
      treeItemIsExpanded: YES,
      treeItemLabel: 'Repositories',
      treeItemIcon: sc_static('images/repositories.png'),
      treeItemChildren: function() {
        var repoInfoQuery = SC.Query.local(Chililog.RepositoryInfoRecord, { orderBy: 'name' });
        var repoInfo = Chililog.store.find(repoInfoQuery);
        return repoInfo;
      }.property()
    });

    var users = SC.Object.create({
      treeItemIsExpanded: YES,
      treeItemLabel: 'Users',
      treeItemIcon: sc_static('images/users.png'),
      treeItemChildren: function() {
        var userQuery = SC.Query.local(Chililog.UserRecord, { orderBy: 'username' });
        var users = Chililog.store.find(userQuery);
        return users;
      }.property()
    });

    var rootNode = SC.Object.create({
      treeItemIsExpanded: YES,
      treeItemLabel: 'Chililog',
      treeItemIcon: null,
      treeItemChildren: [repositories, users]
    });
    this.set('content', rootNode);
  },

  /**
   * Returns the selected item
   */
  selectedItem: function() {
    var selectionSet = this.get('selection');
    if (SC.none(selectionSet) || selectionSet.get('length') === 0) {
      return null;
    }
    var selection = selectionSet.get('firstObject');
    return selection;
  }.property('selection').cacheable(),

  /**
   * When selection changes, then we trigger a change in state
   */
  selectionDidChange: function() {
    var selectedItem = this.get('selectedItem');
    if (SC.none(selectedItem)) {
      return;
    }

    if (SC.instanceOf(selectedItem, Chililog.UserRecord)) {
      this.editUser();
    } else if (SC.instanceOf(selectedItem, Chililog.RepositoryInfoRecord)) {
      this.editRepository();
    } else {
      this.unload();
    }

    // Have to notify change because the user may move from EDIT_USER to another EDIT_USER
    this.notifyPropertyChange('state');
  }.observes('selection'),

  /**
   * Trigger a change of state to edit the selected user
   */
  editUser: function() {
    this.set('state', Chililog.configureTreeViewStates.EDIT_USER);
  },

  /**
   * Trigger a change of state to edit the selected repository
   */
  editRepository: function() {
    this.set('state', Chililog.configureTreeViewStates.EDIT_REPOSITORY);
  },

  /**
   * Trigger a change of state to unload the data
   */
  unload: function() {
    this.set('state', Chililog.configureTreeViewStates.UNLOADED);
  }

});

