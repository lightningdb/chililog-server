// ==========================================================================
// Project:   Chililog.mainPaneController
// Copyright: ©2011 My Company, Inc.
// ==========================================================================

sc_require('controllers/view_controller_mixin');

/**
 * Controller that for the mainPane. Mainly handles menu selection option and login/logout
 *
 * @extends SC.Object
 */
Chililog.loginViewController = SC.ObjectController.create(Chililog.ViewControllerMixin, 
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
    // Check field values
    var rootView = Chililog.loginPage.getPath('loginPane.boxView');
    var result = this.findFieldAndValidate(rootView);
    if (result !== SC.VALIDATE_OK) {
      this.showError(result);
      return;
    }
    
    Chililog.statechart.sendEvent('login');
  },

  showLoginPage: function () {
    Chililog.getPath('loginPage.loginPane').append();

    var field = Chililog.getPath('loginPage.loginPane.boxView.username.field');
    this.setFocusOnField(field, 200);
  },

  hideLoginPage: function () {
    Chililog.getPath('loginPage.loginPane').remove();
  }


});

