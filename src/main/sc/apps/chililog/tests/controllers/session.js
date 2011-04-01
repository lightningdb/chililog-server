// ==========================================================================
// Project:   Chililog.sessionController Unit Test
// Copyright: ©2011 My Company, Inc.
// ==========================================================================
/*globals Chililog module test ok equals same stop start */

module("Chililog.sessionController");

test("test successful login", function() {
  stop(2000);

  ok(Chililog.sessionController.login('admin', 'admin', NO), 'Successful login');

  var isBusy = Chililog.sessionController.get('isBusy');
  ok(isBusy == YES, 'isBusy is YES');

  setTimeout(checkLoginSuccess, 1000);
});
function checkLoginSuccess() {
  var errorMessage = Chililog.sessionController.get('errorMessage');
  ok(errorMessage.length == 0, 'No error messages');

  var isBusy = Chililog.sessionController.get('isBusy');
  ok(isBusy == NO, 'isBusy is NO');

  var loggedInUser = Chililog.sessionController.get('loggedInUser');
  ok(loggedInUser !== null, 'loggedInUser is not null');
  ok(loggedInUser.Username === 'admin', 'loggedInUser name is admin');

  var authenticationToken = Chililog.sessionController.get('authenticationToken');
  ok(authenticationToken !== null, 'authenticationToken is not null');

  var authenticationTokenExpiry = Chililog.sessionController.get('authenticationTokenExpiry');
  ok(authenticationTokenExpiry !== null, 'authenticationTokenExpiry is not null');

  var isLoggedIn = Chililog.sessionController.get('isLoggedIn');
  ok(isLoggedIn, 'is logged in');

  // Test logout
  Chililog.sessionController.logout();
  ok(!Chililog.sessionController.get('isLoggedIn'), 'Logged out');
  ok(Chililog.sessionController.get('loggedInUser') === null, 'Logged out no user');
  ok(Chililog.sessionController.get('authenticationToken') === null, 'Logged out no token');
  ok(Chililog.sessionController.get('authenticationTokenExpiry') === null, 'Logged out no expiry');

  start();
}


test("test successful save", function() {
  stop(2000);
  ok(Chililog.sessionController.login('admin', 'admin', YES, NO), 'Successful login');

  var loggedInUser = Chililog.sessionController.get('loggedInUser');
  ok(loggedInUser !== null, 'loggedInUser is not null');
  ok(loggedInUser.Username === 'admin', 'loggedInUser name is admin');

  var authenticationToken = Chililog.sessionController.get('authenticationToken');
  ok(authenticationToken !== null, 'authenticationToken is not null');

  var authenticationTokenExpiry = Chililog.sessionController.get('authenticationTokenExpiryExpiry');
  ok(authenticationTokenExpiry !== null, 'authenticationTokenExpiry is not null');

  Chililog.sessionController.load();

  ok(authenticationToken === Chililog.sessionController.get('authenticationToken'), 'authenticationToken is the same');

  ok(authenticationTokenExpiry === Chililog.sessionController.get('authenticationTokenExpiryExpiry'),
    'authenticationTokenExpiry is the same');

  setTimeout(checkLoadSuccess, 1000);
});
function checkLoadSuccess() {
  var errorMessage = Chililog.sessionController.get('errorMessage');
  ok(errorMessage.length == 0, 'No error messages');

  var isBusy = Chililog.sessionController.get('isBusy');
  ok(isBusy == NO, 'isBusy is NO');

  var loggedInUser = Chililog.sessionController.get('loggedInUser');
  ok(loggedInUser !== null, 'loggedInUser is not null');
  ok(loggedInUser.Username === 'admin', 'loggedInUser name is admin');

  start();
}


test("test bad password", function() {
  stop(2000);

  ok(Chililog.sessionController.login('admin', 'bad password', NO), 'Bad password');

  var isBusy = Chililog.sessionController.get('isBusy');
  ok(isBusy == YES, 'isBusy is YES');

  setTimeout(checkError, 1000);
});

test("test bad username", function() {
  stop(2000);

  ok(Chililog.sessionController.login('badusername', 'bad password', NO), 'Bad username');

  var isBusy = Chililog.sessionController.get('isBusy');
  ok(isBusy == YES, 'isBusy is YES');

  setTimeout(checkError, 1000);
});

function checkError() {
  var errorMessage = Chililog.sessionController.get('errorMessage');
  ok(errorMessage.length > 0, 'Error message is: ' + errorMessage);

  var isBusy = Chililog.sessionController.get('isBusy');
  ok(isBusy == NO, 'isBusy is NO');

  var isLoggedIn = Chililog.sessionController.get('isLoggedIn');
  ok(!isLoggedIn, 'is  not logged in');

  start();
}