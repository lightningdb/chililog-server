// ==========================================================================
// Project:   Chililog Unit Test
// Copyright: ©2011 My Company, Inc.
// ==========================================================================
/*globals Chililog module test ok equals same stop start */

module("Chililog.sessionDataController");

var loginError = null;
test("test successful login", function() {
  stop(2000);

  Chililog.sessionDataController.login('admin', 'admin', NO, YES, null, function(error) {
    loginError = error;
  });
  
  setTimeout(checkLoginSuccess, 1000);
});
function checkLoginSuccess() {
  ok(SC.none(loginError), 'No error messages');

  var loggedInUser = Chililog.sessionDataController.get('loggedInUser');
  ok(loggedInUser !== null, 'loggedInUser is not null');
  ok(loggedInUser.get('username') === 'admin', 'loggedInUser name is admin');

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

var loadAuthenticationToken;
var loadAuthenticationTokenExpiry;
test("test successful load", function() {
  SC.RunLoop.begin();
  Chililog.sessionDataController.login('admin', 'admin', YES, NO);
  SC.RunLoop.end();

  var loggedInUser = Chililog.sessionDataController.get('loggedInUser');
  ok(loggedInUser !== null, 'loggedInUser is not null');
  ok(loggedInUser.get('username') === 'admin', 'loggedInUser name is admin');

  loadAuthenticationToken = Chililog.sessionDataController.get('authenticationToken');
  ok(loadAuthenticationToken !== null, 'authenticationToken is not null');

  loadAuthenticationTokenExpiry = Chililog.sessionDataController.get('authenticationTokenExpiryExpiry');
  ok(loadAuthenticationTokenExpiry !== null, 'authenticationTokenExpiry is not null');

  // this is a synchronous call
  Chililog.sessionDataController.load();

  ok(loadAuthenticationToken === Chililog.sessionDataController.get('authenticationToken'), 'authenticationToken is the same');

  ok(loadAuthenticationTokenExpiry === Chililog.sessionDataController.get('authenticationTokenExpiryExpiry'),
    'authenticationTokenExpiry is the same');

  var loggedInUser = Chililog.sessionDataController.get('loggedInUser');
  ok(loggedInUser !== null, 'loggedInUser is not null');
  ok(loggedInUser.get('username'), 'loggedInUser name is admin');
});


test("test bad password", function() {
  stop(2000);

  Chililog.sessionDataController.login('admin', 'bad password', NO, YES, null, function(error) {
    loginError = error;
  });

  setTimeout(checkError, 1000);
});

test("test bad username", function() {
  stop(2000);

  Chililog.sessionDataController.login('badusername', 'admin', NO, YES, null, function(error) {
    loginError = error;
  });

  setTimeout(checkError, 1000);
});

function checkError() {
  ok(!SC.none(loginError), 'Error message is: ' + loginError['message']);

  var isLoggedIn = Chililog.sessionDataController.get('isLoggedIn');
  ok(!isLoggedIn, 'is  not logged in');

  start();
}