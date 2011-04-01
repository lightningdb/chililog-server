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
Chililog.RUNNING_UNIT_TESTS = YES;

/**
 * This is the function that will start the app running.
 */
Chililog.main = function main() {

  // Don't need to execute anything below this line if running unit test cases
  if (Chililog.RUNNING_UNIT_TESTS) {
    return;
  }

  // See if we can load the session (ignore errors)
  Chililog.sessionController.load(YES);
  Chililog.loginPaneController.isLoggedInDidChange();

  // Setup poller to check for session expiry
  Chililog.sessionController.checkExpiry();

  // Step 2. Set the content property on your primary controller.
  // This will make your app come alive!

  // TODO: Set the content property on your primary controller
  // ex: Chililog.contactsController.set('content',Chililog.contacts);

} ;

function main() { Chililog.main(); }
