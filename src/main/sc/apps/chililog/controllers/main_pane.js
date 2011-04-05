// ==========================================================================
// Project:   Chililog.mainPaneController
// Copyright: ©2011 My Company, Inc.
// ==========================================================================


/**
 * States of the mainPane in the mainPage.
 */
Chililog.mainPaneStates = {
  SEARCH: 'Search',
  ANALYSIS: 'Analysis',
  MONITORS: 'Monitors',
  REPOSITORIES: 'Repositories',
  USERS: 'Users',
  ABOUT: 'About',
  LOGIN: 'Login'
}

/**
 * Controller that for the mainPane. Mainly handles menu selection option and login/logout
 *
 * @extends SC.Object
 */
Chililog.mainPaneController = SC.Object.create(
/** @scope Chililog.mainPaneController.prototype */ {

  /**
   * Determines the current visible 'body' view
   * @type State
   */
  state: Chililog.mainPaneStates.SEARCH,

  /**
   * Array of menu options items
   * @type Array
   */
  menuOptions: null,

  /**
   * Rebuild menu options if the logged in user changes
   */
  loggedInUserDidChange: function() {
    var values = [
      { value: Chililog.mainPaneStates.SEARCH,
        title: '_mainPane.Search'.loc(),
        toolTip: '_mainPane.Search.ToolTip'.loc(),
        target: Chililog.mainPaneController,
        action: 'showSearch'
      },
      { value: Chililog.mainPaneStates.ANALYSIS,
        title: '_mainPane.Analysis'.loc(),
        toolTip: '_mainPane.Analysis.ToolTip'.loc(),
        target: Chililog.mainPaneController,
        action: 'showAnalysis'
      }
    ];

    var isAdmin = Chililog.sessionController.get('isInAdministratorRole');
    if (isAdmin) {
      values.push({
        value: Chililog.mainPaneStates.REPOSITORIES,
        title: '_mainPane.Repositories'.loc(),
        toolTip: '_mainPane.Repositories.ToolTip'.loc(),
        target: Chililog.mainPaneController,
        action: 'showRepositories'
      });
      values.push({
        value: Chililog.mainPaneStates.USERS,
        title: '_mainPane.Users'.loc(),
        toolTip: '_mainPane.Users.ToolTip'.loc(),
        target: Chililog.mainPaneController,
        action: 'showUsers'
      });
    }

    values.push({
      value: Chililog.mainPaneStates.ABOUT,
      title: '_mainPane.About'.loc(),
      toolTip: '_mainPane.About.ToolTip'.loc(),
      target: Chililog.mainPaneController,
      action: 'showAbout'
    });

    this.set('menuOptions', values);

  }.observes('Chililog.sessionController.loggedInUser'),

  /**
   * Show search view
   */
  showSearch: function() {
    this.set('state', Chililog.mainPaneStates.SEARCH);
  },

  /**
   * Show analysis view
   */
  showAnalysis: function() {
    this.set('state', Chililog.mainPaneStates.ANALYSIS);
  },

  /**
   * Show monitors view
   */
  showMonitors: function() {
    this.set('state', Chililog.mainPaneStates.MONITORS);
  },

  /**
   * Show repository views
   */
  showRepositories: function() {
    this.set('state', Chililog.mainPaneStates.REPOSITORIES);
  },

  /**
   * Show users view
   */
  showUsers: function() {
    this.set('state', Chililog.mainPaneStates.USERS);
  },

  /**
   * Show about view
   */
  showAbout: function() {
    this.set('state', Chililog.mainPaneStates.ABOUT);
  }

});

