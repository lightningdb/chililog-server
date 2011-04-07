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

  // Don't need to execute anything below this line if running unit test cases
  if (Chililog.RUNNING_UNIT_TESTS) {
    return;
  }

  // See if we can load the session (ignore errors)
  Chililog.sessionController.load(NO);
  Chililog.loginPaneController.isLoggedInDidChange();

  // Setup poller to check for session expiry
  Chililog.sessionController.checkExpiry();

  // Check for startup page on the URL querystring
  if (!SC.empty(window.location.search))
  {
    var a = window.location.search.substr(1).split('&');
    for (var i = 0; i < a.length; ++i)
    {
      var p=a[i].split('=');
      var name = p[0];
      var value = p[1];
      if (name === 'StartPage')
      {
        // Init state to force an DidChange event
        Chililog.mainPaneController.set('state', '');
        // Set state to the new value that is specified
        Chililog.mainPaneController.set('state', value);
      }
    }
  }

  // TODO: Set the content property on your primary controller
  // ex: Chililog.contactsController.set('content',Chililog.contacts);

} ;

function main() { Chililog.main(); }
