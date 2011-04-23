// ==========================================================================
// Project:   Chililog.mainPaneController
// Copyright: ©2011 My Company, Inc.
// ==========================================================================


/**
 * Controller that for the mainPane. Mainly handles menu selection option and login/logout
 *
 * @extends SC.Object
 */
Chililog.loginViewController = SC.ObjectController.create(
/** @scope Chililog.loginPaneController.prototype */ {

  /**
   * Username to use for login
   *
   * @type String
   */
  username: null,

  /**
   * Password to use for login
   *
   * @type String
   */
  password: null,

  /**
   * Remember Me flag so the user does not have to login for 14 days
   *
   * @type Boolean
   */
  rememberMe: NO,

  /**
   * Error object
   *
   * @type SC.Error
   */
  error: null,

  /**
   * Flag to indicate if we are logging or not.
   * This is driven by our statechart
   */
  isLoggingIn: NO,

  /**
   * Handles the login action
   */
  login: function() {
    Chililog.statechart.sendEvent('login');
  }

});

