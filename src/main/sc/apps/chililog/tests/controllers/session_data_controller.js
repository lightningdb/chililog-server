// ==========================================================================
// Project:   Chililog Unit Test
// Copyright: ©2011 My Company, Inc.
// ==========================================================================
/*globals Chililog module test ok equals same stop start */

module("Chililog.sessionDataController");

test("test successful login", function() {
  stop(2000);

  ok(Chililog.sessionDataController.login('admin', 'admin', NO), 'Successful login');
  
  setTimeout(checkLoginSuccess, 1000);
});
function checkLoginSuccess() {
  var errorMessage = Chililog.sessionDataController.get('errorMessage');
  ok(errorMessage.length == 0, 'No error messages');

  var loggedInUser = Chililog.sessionDataController.get('loggedInUser');
  ok(loggedInUser !== null, 'loggedInUser is not null');
  ok(loggedInUser.Username === 'admin', 'loggedInUser name is admin');

  var authenticationToken = Chililog.sessionDataController.get('authenticationToken');
  ok(authenticationToken !== null, 'authenticationToken is not null');

  var authenticationTokenExpiry = Chililog.sessionDataController.get('authenticationTokenExpiry');
  ok(authenticationTokenExpiry !== null, 'authenticationTokenExpiry is not null');

  var isLoggedIn = Chililog.sessionDataController.get('isLoggedIn');
  ok(isLoggedIn, 'is logged in');

  // Test logout
  Chililog.sessionDataController.logout();
  ok(!Chililog.sessionDataController.get('isLoggedIn'), 'Logged out');
  ok(Chililog.sessionDataController.get('loggedInUser') === null, 'Logged out no user');
  ok(Chililog.sessionDataController.get('authenticationToken') === null, 'Logged out no token');
  ok(Chililog.sessionDataController.get('authenticationTokenExpiry') === null, 'Logged out no expiry');

  start();
}


test("test successful save", function() {
  stop(2000);
  ok(Chililog.sessionDataController.login('admin', 'admin', YES, NO), 'Successful login');

  var loggedInUser = Chililog.sessionDataController.get('loggedInUser');
  ok(loggedInUser !== null, 'loggedInUser is not null');
  ok(loggedInUser.Username === 'admin', 'loggedInUser name is admin');

  var authenticationToken = Chililog.sessionDataController.get('authenticationToken');
  ok(authenticationToken !== null, 'authenticationToken is not null');

  var authenticationTokenExpiry = Chililog.sessionDataController.get('authenticationTokenExpiryExpiry');
  ok(authenticationTokenExpiry !== null, 'authenticationTokenExpiry is not null');

  Chililog.sessionDataController.load();

  ok(authenticationToken === Chililog.sessionDataController.get('authenticationToken'), 'authenticationToken is the same');

  ok(authenticationTokenExpiry === Chililog.sessionDataController.get('authenticationTokenExpiryExpiry'),
    'authenticationTokenExpiry is the same');

  setTimeout(checkLoadSuccess, 1000);
});
function checkLoadSuccess() {
  var errorMessage = Chililog.sessionDataController.get('errorMessage');
  ok(errorMessage.length == 0, 'No error messages');

  var loggedInUser = Chililog.sessionDataController.get('loggedInUser');
  ok(loggedInUser !== null, 'loggedInUser is not null');
  ok(loggedInUser.Username === 'admin', 'loggedInUser name is admin');

  start();
}


test("test bad password", function() {
  stop(2000);

  ok(Chililog.sessionDataController.login('admin', 'bad password', NO), 'Bad password');

  setTimeout(checkError, 1000);
});

test("test bad username", function() {
  stop(2000);

  ok(Chililog.sessionDataController.login('badusername', 'bad password', NO), 'Bad username');

  setTimeout(checkError, 1000);
});

function checkError() {
  var errorMessage = Chililog.sessionDataController.get('errorMessage');
  ok(errorMessage.length > 0, 'Error message is: ' + errorMessage);

  var isLoggedIn = Chililog.sessionDataController.get('isLoggedIn');
  ok(!isLoggedIn, 'is  not logged in');

  start();
}