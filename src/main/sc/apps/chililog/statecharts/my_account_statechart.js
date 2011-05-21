// ==========================================================================
// Project:   Chililog
// Copyright: ©2011 My Company, Inc.
// ==========================================================================

sc_require('statecharts/my_account_my_password_statechart');
sc_require('statecharts/my_account_my_profile_statechart');

/**
 * State chart for my account screens
 */
Chililog.MyAccountState = SC.State.extend({

  initialSubstate: 'myProfile',

  /**
   * Base configuration page
   */
  enterState: function() {
    Chililog.mainViewController.doShow('myAccount');
  },

  exitState: function() {
    return;
  },

  /**
   * States for editing the logged in user's profile
   */
  myProfile: SC.State.plugin('Chililog.MyProfileState'),

  /**
   * States for editing the logged in user's password
   */
  myPassword: SC.State.plugin('Chililog.MyPasswordState'),

  /**
   * Event to edit the logged in user's profile
   */
  editMyProfile: function() {
    this.gotoState('editingMyProfile');
  },

  /**
   * Event to change password
   */
  changeMyPassword: function() {
    this.gotoState('changingMyPassword');
  }

});