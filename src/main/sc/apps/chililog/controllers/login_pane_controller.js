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
  errorMessage: '',

  /**
   * Start async login process
   */
  beginLogin: function() {
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
  }.observes('Chililog.sessionController.isLoggedIn')
});

