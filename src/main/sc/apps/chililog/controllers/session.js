// ==========================================================================
// Project:   Chililog.sessionController
// Copyright: ©2011 My Company, Inc.
// ==========================================================================

/**
 * Name of cookie where we store the auth token
 */
Chililog.AUTHENTICATION_TOKEN_COOKIE_NAME = 'ChiliLog.AuthenticationToken';

/**
 * Name of cookie where we store the auth token
 */
Chililog.AUTHENTICATION_TOKEN_EXPIRY_COOKIE_NAME = 'ChiliLog.AuthenticationTokenExpiry';


/**
 * @class
 *
 * Controller that manages our session and keeps track of the the user is logged in or not
 * 
 * @extends SC.Object
 */
Chililog.sessionController = SC.ObjectController.create(Chililog.ServerApiMixin, 
/** @scope Chililog.sessionController.prototype */ {

  /**
   * YES if async login taking place, NO otherwise.
   */
  isLoggingIn: NO,

  /**
   * Last error message
   *
   * @type String
   */
  errorMessage: '',

  /**
   * The logged in user
   *
   * @type User object hash
   */
  loggedInUser: null,

  /**
   * The authentication token
   *
   * @type String
   */
  authenticationToken: null,

  /**
   * Date at which the authentication token will expire
   *
   * @type Date
   */
  authenticationTokenExpiry: null,

  /**
   * YES if the user is logged in, NO if not.
   *
   * @type Boolean
   */
  isLoggedIn: function() {
    return this.get('loggedInUser') === null;
  }.property('loggedInUser').cacheable(),

  /**
   * Load the details of the authentication token from cookies (if the user selected 'Remember Me')
   *
   * @returns none
   */
  load: function() {
    // Assumed logged out
    this.set('authenticationTokenExpiry', null);
    this.set('authenticationToken', null);
    this.set('loggedInUser', null);

    // Get expiry
    var expiryCookie = SC.Cookie.find(Chililog.AUTHENTICATION_TOKEN_EXPIRY_COOKIE_NAME);
    if (expiryCookie === null) {
      return;
    }
    var expiry = new Date();
    expiry.setTime(parseInt(expiryCookie.get('value')));
    if (new Date() > expiry) {
      return;
    }

    // Get token
    var tokenCookie = SC.Cookie.find(Chililog.AUTHENTICATION_TOKEN_COOKIE_NAME);
    if (tokenCookie === null) {
      return;
    }

    // Get user from server TODO

    // Set
    this.set('authenticationTokenExpiry', expiry);
    this.set('authenticationToken', tokenCookie.get('value'));
    this.set('loggedInUser', null);
  },

  /**
   * Saves the current authentication details into cookies
   *
   * @returns none
   */
  save: function() {
    var expiry = this.get('authenticationTokenExpiry');
    
    var tokenCookie = SC.Cookie.create();
    tokenCookie.name = SC.AUTHENTICATION_TOKEN_COOKIE_NAME;
    tokenCookie.value = this.get('authenticationToken');
    tokenCookie.expires = expiry;
    tokenCookie.write();

    var expiryCookie = SC.Cookie.create();
    expiryCookie.name = SC.AUTHENTICATION_TOKEN_EXPIRY_COOKIE_NAME;
    expiryCookie.value = expiry.getTime();
    expiryCookie.expires = expiry;
    expiryCookie.write();
  },

  /**
   *  Start async login process
   *
   *  @returns {Boolean} YES if async call successfully started, NO if it failed. If error, the error message
   *  will be placed in the 'errorMessage' property.
   */
  beginLogin: function(username, password, rememberMe) {
    try {
      // Get our data from the properties using the SC 'get' methods
      // Need to do this because these properties have been bound/observed.
      if (username == null || username == '') {
        throw SC.Error.desc('Username is required');
      }

      if (password == null || password == '') {
        throw SC.Error.desc('Password is required');
      }

      // Clear error data
      this.set('errorMessage', '');

      // Start login
      this.set('isLoggingIn', YES);

      var postData = {
        'Username': username,
        'Password': password,
        'ExpiryType': 'Absolute',
        'ExpirySeconds': Chililog.AUTHENTICATION_TOKEN_EXPIRY_SECONDS
      };

      // Simulate a HTTP call to check our data.
      // If the credentials not admin/admin, then get a bad url so we get 404 error
      var url = '/api/Authentication';

      SC.Request.postUrl(url)
        .json(YES)
        .notify(this, 'endLogin', { rememberMe: rememberMe })
        .send(postData);

      return YES;
    }
    catch (err) {
      // Set Error
      this.set('errorMessage', err.message);

      // Finish login processing
      this.set('isLoggingIn', NO);

      return NO;
    }
  },

  /**
   * Callback from beginLogin() after we get a response from the server to process
   * the returned login info.
   *
   * @param {SC.Response} response The HTTP response
   * @param {params} Hash of parameters passed into SC.Request.notify
   * @returns {Boolean} YES if successful
   */
  endLogin: function(response, params) {
    try {
      // Flag finish login processing to unlock screen
      this.set('isLoggingIn', NO);

      // Check status
      this.checkResponse(response);

      // Figure out the expiry
      // Take off 1 hour so that we expire before the token does which means we have time to act
      var expiry = new Date();
      expiry.setTime(expiry.getTime() + ((Chililog.AUTHENTICATION_TOKEN_EXPIRY_SECONDS - 3600) * 1000))

      // Get the token
      var token = response.headers[Chililog.AUTHENTICATION_HEADER_NAME];
      if (token === null) {
        token = response.headers[Chililog.AUTHENTICATION_HEADER_NAME_LCASE];
        if (token === null) {
          throw SC.Error.desc('Token not found in authentication response');
        }
      }
      
      // Set data
      var userJson = response.get('body');
      this.set('loggedInUser', userJson);
      this.set('authenticationToken', token);
      this.set('authenticationTokenExpiry', expiry);

      // Clear error data
      this.set('errorMessage', '');

      // Return YES to signal handling of callback
      return YES;
    }
    catch (err) {
      this.set('errorMessage', err.message);
      SC.Logger.info('Error in endLogin: ' + err.message);
    }
  },

  /**
   * Remove authentication token cookie
   */
  logout: function() {
    var cookie = SC.Cookie.find(Chililog.AUTHENTICATION_TOKEN_COOKIE_NAME);
    if (cookie !== null) {
      cookie.destroy();
    }

    cookie = SC.Cookie.find(Chililog.AUTHENTICATION_TOKEN_EXPIRY_COOKIE_NAME);
    if (cookie !== null) {
      cookie.destroy();
    }

    this.set('authenticationTokenExpiry', null);
    this.set('authenticationToken', null);
    this.set('loggedInUser', null);

    return;
  }


}) ;
