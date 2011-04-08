// ==========================================================================
// Project:   Chililog
// Copyright: ©2011 My Company, Inc.
// ==========================================================================

/**
 * Flag to indicate if we are running unit test cases or not. If YES, then we turn off page switching based on
 * login status.
 *
 * @type Boolean
 */
Chililog.RUNNING_UNIT_TESTS = NO;

/**
 * This is the function that will start the app running.
 */
Chililog.main = function main() {
  try {
    // Don't need to execute anything below this line if running unit test cases
    if (Chililog.RUNNING_UNIT_TESTS) {
      return;
    }

    // See if we can load the session (ignore errors)
    // Because binding do not work until after main(), we manually notify login page if user logged in or not
    Chililog.sessionDataController.load(NO);
    Chililog.loginPaneController.isLoggedInDidChange();

    // Setup poller to check for session expiry
    Chililog.sessionDataController.checkExpiry();

    // Set startup state (can be defined in query string
    var startUpState = Chililog.mainPaneStates.SEARCH;
    if (!SC.empty(window.location.search)) {
      var a = window.location.search.substr(1).split('&');
      for (var i = 0; i < a.length; ++i) {
        var p = a[i].split('=');
        var name = p[0];
        var value = p[1];
        if (name === 'StartState') {
          startUpState = value;
        }
      }
    }
    Chililog.mainPaneController.set('state', startUpState);

    // Manually fire event of state change because bindings do not fire until main finishes
    var mainPane = Chililog.mainPage.get('mainPane');
    mainPane.get('stateDidChange').call(mainPane);

    // TODO: Set the content property on your primary controller
    // ex: Chililog.contactsController.set('content',Chililog.contacts);

  }
  catch (err) {
    // Show Error
    SC.AlertPane.error(err);
  }
};

function main() {
  Chililog.main();
}
