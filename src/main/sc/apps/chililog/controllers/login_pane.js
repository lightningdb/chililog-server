// ==========================================================================
// Project:   Chililog.mainPaneController
// Copyright: ©2011 My Company, Inc.
// ==========================================================================


/**
 * States of the mainPane in the mainPage.
 */
Chililog.loginPaneStates = {
  EDIT: 'Edit',
  BUSY: 'Busy',
  HIDDEN: 'Hidden'
}

/**
 * Controller that for the mainPane. Mainly handles menu selection option and login/logout
 *
 * @extends SC.Object
 */
Chililog.loginPaneController = SC.ObjectController.create(
/** @scope Chililog.loginPaneController.prototype */ {

  /**
   * Current state of the pane
   *
   * @type {String}
   */
  state: Chililog.loginPaneStates.EDIT,

  /**
   * Username to use for login
   *
   * @type {String}
   */
  username: null,

  /**
   * Password to use for login
   *
   * @type {String}
   */
  password: null,

  /**
   * Remember Me flag so the user does not have to login for 14 days
   *
   * @type {Boolean}
   */
  rememberMe: NO,

  /**
   * Error message to display to the user
   *
   * @type {String}
   */
  errorMessage: null,

  /**
   * Start async login process
   */
  login: function() {
    try {
      var username = this.get('username');
      var password = this.get('password');
      var rememberMe = this.get('rememberMe');

      Chililog.sessionController.login(username, password, rememberMe, YES, this, this.endLogin);
  
      // Say that we are busy
      this.set('state', Chililog.loginPaneStates.BUSY);

      // Finish
      return YES;
    }
    catch (err) {
      // Set Error
      this.set('errorMessage', err.message);
      return NO;
    }
  },

  /**
   * Callback from beginLogin() after we get a response from the server to process
   * the returned login info.
   *
   * @param {SC.Response} response The HTTP response
   * @param {params} Hash of parameters passed into SC.Request.notify
   * @returns {Boolean} YES if successful
   */
  endLogin: function() {
    var errorMessage = Chililog.sessionController.get('errorMessage');
    if (SC.empty(errorMessage)) {
      // Clear error data and password
      this.set('password', '');
      this.set('errorMessage', '');
    } else {
      // Show error
      this.set('errorMessage', errorMessage);
    }

    // Return to edit mode from BUSY
    this.set('state', Chililog.loginPaneStates.EDIT);
  },

  /**
   * Reacts when the user logins and logsout
   */
  isLoggedInDidChange: function() {
    // Don't change screens if running unit tests
    if (Chililog.RUNNING_UNIT_TESTS) {
      return;
    }

    var isLoggedIn = Chililog.sessionController.get('isLoggedIn');
    var state = this.get('state');
    if (isLoggedIn && state === Chililog.loginPaneStates.HIDDEN) {
      // Show main form
      Chililog.getPath('loginPage.loginPane').remove();
      Chililog.getPath('mainPage.mainPane').append();
      this.set('state', Chililog.loginPaneStates.HIDDEN);
    } else if (!isLoggedIn && state !== Chililog.loginPaneStates.HIDDEN) {
      // Show login form
      Chililog.getPath('mainPage.mainPane').remove();
      Chililog.getPath('loginPage.loginPane').append();
      this.set('state', Chililog.loginPaneStates.EDIT);
    }
  }.observes('Chililog.sessionController.isLoggedIn')
});

