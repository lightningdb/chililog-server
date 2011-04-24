// ==========================================================================
// Project:   Chililog.mainPaneController
// Copyright: ©2011 My Company, Inc.
// ==========================================================================

/**
 * Controls the data when configuring users
 */
Chililog.configureUserViewController = SC.ObjectController.create({

  /**
   * User record to display
   * @type Chililog.UserRecord
   */
  content: null,

  /**
   * Show the user details form
   */
  show: function() {
    Chililog.configureView.setPath('body.bottomRightView.contentView', Chililog.configureUserView);
  }
});

/**
 * Controls the data when configuring repositories
 */
Chililog.configureRepositoryViewController = SC.ObjectController.create({

  /**
   * Repository record to display
   * @type Chililog.RepositoryRecord
   */
  content: null,

  /**
   * Show the repository details form
   */
  show: function() {
    Chililog.configureView.setPath('body.bottomRightView.contentView', Chililog.configureRepositoryView);
  }
});

/**
 * Controller that for the mainPane. Mainly handles menu selection option and login/logout
 *
 * @extends SC.Object
 */
Chililog.configureTreeViewController = SC.TreeController.create({

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
   * When selection changes, then we send an event
   */
  selectionDidChange: function() {
    var selectedItem = this.get('selectedItem');
    if (SC.none(selectedItem)) {
      return;
    }

    if (SC.instanceOf(selectedItem, Chililog.UserRecord)) {
      Chililog.statechart.sendEvent('editUser', selectedItem.get(Chililog.DOCUMENT_ID_RECORD_FIELD_NAME));
    } else if (SC.instanceOf(selectedItem, Chililog.RepositoryInfoRecord)) {
      Chililog.statechart.sendEvent('editRepository', selectedItem.get(Chililog.DOCUMENT_ID_RECORD_FIELD_NAME));
    } 
  }.observes('selectedItem')

});

