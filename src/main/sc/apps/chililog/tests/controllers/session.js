// ==========================================================================
// Project:   Chililog.sessionController Unit Test
// Copyright: ©2011 My Company, Inc.
// ==========================================================================
/*globals Chililog module test ok equals same stop start */

module("Chililog.sessionController");

test("test successful login", function() {
  stop(3000);

  ok(Chililog.loginController.beginLogin('admin', 'admin'), 'Async call started');

  var isLoggingIn = Chililog.loginController.get('isLoggingIn');
  ok(isLoggingIn == YES, 'isLoginIn is YES');

  setTimeout(checkSuccess, 2000);
});


function checkSuccess() {
  var errorMessage = Chililog.loginController.get('errorMessage');
  ok(errorMessage.length == 0, 'No error messages');

  var isLoggingIn = Chililog.loginController.get('isLoggingIn');
  ok(isLoggingIn == NO, 'isLoginIn is NO');

  start();
}

function checkError() {
  var errorMessage = Chililog.loginController.get('errorMessage');
  ok(errorMessage.length > 0, 'Error message is: ' + errorMessage);

  var isLoggingIn = Chililog.loginController.get('isLoggingIn');
  ok(isLoggingIn == NO, 'isLoginIn is NO');

  start();
}