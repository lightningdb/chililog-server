// ==========================================================================
// Project:   Chililog
// Copyright: ©2011 My Company, Inc.
// ==========================================================================

sc_require('statecharts/configure_repository_info_statechart');
sc_require('statecharts/configure_user_statechart');

/**
 * State chart for configure screens
 */
Chililog.ConfigureState = SC.State.extend({

  initialSubstate: 'repositoryInfo',

  /**
   * Base configuration page
   */
  enterState: function() {
    Chililog.mainViewController.doShow('configure');
  },

  exitState: function() {
    return;
  },

  /**
   * States for listing and CRUD of users
   */
  user: SC.State.plugin('Chililog.ConfigureUserState'),

  /**
   * States for listing and CRUD of repository info
   */
  repositoryInfo: SC.State.plugin('Chililog.ConfigureRepositoryInfoState'),

  /**
   * Event to view users
   */
  viewUsers: function() {
    this.gotoState('viewingUsers');
  },

  /**
   * Event to view repositories
   */
  viewRepositoryInfo: function() {
    this.gotoState('viewingRepositoryInfo');
  }
});