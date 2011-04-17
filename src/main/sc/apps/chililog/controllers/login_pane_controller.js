// ==========================================================================
// Project:   Chililog.mainPaneController
// Copyright: ©2011 My Company, Inc.
// ==========================================================================


/**
 * States of the mainPane in the mainPage.
 */
Chililog.loginPaneStates = {
  EDIT: 'Edit',
  BUSY: 'Busy'
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
   * Flag to indicate if this pane is in the busy state
   * @return {Boolean}
   */
  isBusy: function() {
    return this.get('state') === Chililog.loginPaneStates.BUSY;
  }.property('state').cacheable(),

  /**
   * Flag to indicate if this pane is in the edit state
   * @return {Boolean}
   */
  isEdit: function() {
    return this.get('state') === Chililog.loginPaneStates.EDIT;
  }.property('state').cacheable(),

  /**
   * Flag indicating if this form is showing or not
   * @type {Boolean}
   */
  isShowing: null,
  
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
   * Start async login process
   *
   * @returns {Boolean} YES if ok, NO if error. Error object set to the 'error' property
   */
  login: function() {
    try {
      var username = this.get('username');
      var password = this.get('password');
      var rememberMe = this.get('rememberMe');

      this.set('error', null);

      Chililog.sessionDataController.login(username, password, rememberMe, YES, this, this.endLogin);

      // Say that we are busy
      this.set('state', Chililog.loginPaneStates.BUSY);

      // Finish
      return YES;
    }
    catch (err) {
      this.set('state', Chililog.loginPaneStates.EDIT);
      this.set('error', err);
      return NO;
    }
  },

  /**
   * Callback from login() after we get a response from the server to process
   * the returned login info.
   *
   * @param {SC.Error} error Error object or null if no error.
   */
  endLogin: function(error) {
    if (SC.none(error)) {
      // Clear error data and password
      this.set('password', '');
      this.set('error', null);
    } else {
      // Show error
      this.set('error', error);
    }

    // Return to edit mode from BUSY
    this.set('state', Chililog.loginPaneStates.EDIT);
  },

  /**
   * Reacts when the user logins and logsout
   */
  isLoggedInDidChange: function() {
    // Don't change screens if running unit tests
    if (Chililog.isRunningTests()) {
      return;
    }

    var isLoggedIn = Chililog.sessionDataController.get('isLoggedIn');
    var isShowing = this.get('isShowing');
    if (isShowing === null) {
      // When first loaded, need to work a little differently
      if (isLoggedIn) {
        // Show main form
        Chililog.getPath('mainPage.mainPane').append();
        this.set('isShowing', NO);
      } else {
        // Show login form
        Chililog.getPath('loginPage.loginPane').append();
        this.set('isShowing', YES);
      }
    } else {
      if (isLoggedIn && isShowing) {
        // Show main form
        Chililog.getPath('loginPage.loginPane').remove();
        Chililog.getPath('mainPage.mainPane').append();
        this.set('isShowing', NO);
      } else if (!isLoggedIn && !isShowing) {
        // Show login form
        Chililog.getPath('mainPage.mainPane').remove();
        Chililog.getPath('loginPage.loginPane').append();
        this.set('isShowing', YES);
      }
    }
  }.observes('Chililog.sessionDataController.isLoggedIn')
});

