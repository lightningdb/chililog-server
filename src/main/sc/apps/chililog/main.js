// ==========================================================================
// Project:   Chililog
// Copyright: ©2011 My Company, Inc.
// ==========================================================================

/**
 * This is the function that will start the app running.
 */
Chililog.main = function main() {

  // Load main form
  Chililog.getPath('mainPage.mainPane').append();

  // See if we have to login
  Chililog.mainPaneController.set('state', Chililog.mainPaneStates.LOGIN)

  

  // Step 2. Set the content property on your primary controller.
  // This will make your app come alive!

  // TODO: Set the content property on your primary controller
  // ex: Chililog.contactsController.set('content',Chililog.contacts);

} ;

function main() { Chililog.main(); }
