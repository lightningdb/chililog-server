// ==========================================================================
// Project:   Chililog.mainPaneController
// Copyright: ©2011 My Company, Inc.
// ==========================================================================


/**
 * States of the mainPane in the mainPage.
 */

/**
 * Controller that for the mainPane. Mainly handles menu selection option and login/logout
 *
 * @extends SC.Object
 */
Chililog.configureViewTreeController = SC.TreeController.create({

  /**
   * Poplulate our tree
   */
  populate: function() {
    var repositories = SC.Object.create({
      treeItemIsExpanded: YES,
      treeItemLabel: 'Repositories',
      treeItemChildren: function() {
        var repoInfoQuery = SC.Query.local(Chililog.RepositoryInfoRecord, { orderBy: 'name' });
        var repoInfo = Chililog.store.find(repoInfoQuery);
        return repoInfo;
      }.property()
    });

    var users = SC.Object.create({
      treeItemIsExpanded: YES,
      treeItemLabel: 'Users',
      treeItemChildren: function() {
        var userQuery = SC.Query.local(Chililog.UserRecord, { orderBy: 'username' });
        var users = Chililog.store.find(userQuery);
        return users;
      }.property()
    });

    var rootNode = SC.Object.create({
      treeItemIsExpanded: YES,
      treeItemLabel: 'Chililog',
      treeItemChildren: [repositories, users]
    });
    this.set('content', rootNode);
  }

});