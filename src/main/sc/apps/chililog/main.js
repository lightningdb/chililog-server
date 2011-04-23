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

    // Init state chart
    Chililog.statechart.initStatechart();

    // See if we can load the session (ignore errors)
    Chililog.sessionDataController.load(NO);

    // Setup poller to check for session expiry
    Chililog.sessionDataController.checkExpiry();

    // Set startup state (can be defined in query string
    var startState = 'loggedIn';
    if (!SC.empty(window.location.search)) {
      var a = window.location.search.substr(1).split('&');
      for (var i = 0; i < a.length; ++i) {
        var p = a[i].split('=');
        var name = p[0];
        var value = p[1];
        if (name === 'StartState') {
          startState = value;
        }
      }
    }

    // Hook up initial views
    Chililog.configureTreeViewController.populate();

    // Now go to the correct state depending on if we are logged in or not
    var isLoggedIn = Chililog.sessionDataController.get('isLoggedIn');
    Chililog.statechart.gotoState(isLoggedIn ? startState : 'loggedOut');
    
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
