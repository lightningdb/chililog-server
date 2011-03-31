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
 * @extends SC.ObjectController
 */
Chililog.mainPaneController = SC.ObjectController.create(
/** @scope Chililog.mainPaneController.prototype */ {

  state: Chililog.mainPaneStates.SEARCH,

  doAction: function(newState) {
    if (newState == Chililog.mainPaneStates.SEARCH) {
      this.doSearch();
    } else if (newState == Chililog.mainPaneStates.ANALYSIS) {
      this.doAnalysis();
    } else if (newState == Chililog.mainPaneStates.ABOUT) {
      this.doAbout();
    }
  },
  
  doSearch: function() {
    this.set('state', Chililog.mainPaneStates.SEARCH);
  },

  doAnalysis: function() {
    this.set('state', Chililog.mainPaneStates.ANALYSIS);
  },

  doAbout: function() {
    this.set('state', Chililog.mainPaneStates.ABOUT);
  }

});

