// ==========================================================================
// Project:   Chililog
// Copyright: ©2011 My Company, Inc.
// ==========================================================================

/**
 * This is the function that will start the app running.
 */
Chililog.main = function main() {
  try {
    // Don't need to execute anything below this line if running unit test cases
    if (Chililog.isRunningTests()) {
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

    // Hook up initial views
    Chililog.configureTreeViewController.populate();
  }
  catch (err) {
    // Show Error
    if (SC.instanceOf(err, SC.Error)) {
      // Error
      var message = err.get('message');
      SC.AlertPane.error({ message: message });
    } else {
      SC.AlertPane.error(err);
    }
    SC.Logger.error('main(): ' + err);
  }
};

function main() {
  Chililog.main();
}
