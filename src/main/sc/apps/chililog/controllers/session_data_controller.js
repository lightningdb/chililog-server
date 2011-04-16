// ==========================================================================
// Project:   Chililog
// Copyright: ©2011 My Company, Inc.
// ==========================================================================

/**
 * Name of cookie where we store the auth token
 */
Chililog.AUTHENTICATION_TOKEN_LOCAL_STORE_KEY = 'ChiliLog.AuthenticationToken';

/**
 * Controller that manages our session and keeps track of the the user is logged in or not
 *
 * @extends SC.Object
 */
Chililog.sessionDataController = SC.Object.create(Chililog.ServerApiMixin,
/** @scope Chililog.sessionDataController.prototype */ {

  /**
   * Error returned from an asynchronous call
   *
   * @type SC.Error
   */
  error: '',

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
    return !SC.empty(this.get('authenticationToken'));
  }.property('authenticationToken').cacheable(),

  /**
   * Get the logged in user from the store.  There should only be 1 record if user is logged in.
   *
   * @type Chililog.AuthenticatedUserRecord
   */
  loggedInUser: function() {
    var userRecords = Chililog.store.find(Chililog.AuthenticatedUserRecord);
    if (userRecords.get('length') === 0) {
      return null;
    } else {
      return userRecords.objectAt(0);
    }
  }.property('authenticationToken').cacheable(),

  /**
   * Returns the display name of the logged in user. If not set, the username is returned.
   *
   * @type String
   */
  loggedInUserDisplayName: function() {
    var loggedInUser = this.get('loggedInUser');
    if (loggedInUser === null) {
      return '';
    }
    var displayName = loggedInUser.get('displayName');
    if (!SC.empty(displayName)) {
      return displayName;
    }
    return loggedInUser.get('username');
  }.property('loggedInUser').cacheable(),

  /**
   * Returns the display name of the logged in user. If not set, the username is returned.
   *
   * @type String
   */
  loggedInUserGravatarURL: function() {
    var loggedInUser = this.get('loggedInUser');
    if (loggedInUser === null) {
      return null;
    }
    var ghash = loggedInUser.get('gravatarMD5Hash');
    if (SC.empty(ghash)) {
      return null;
    }
    return 'http://www.gravatar.com/avatar/' + ghash + '.jpg?s=18&d=mm';
  }.property('loggedInUser').cacheable(),

  /**
   * YES if the user is an administrator
   *
   * @type Boolean
   */
  isInAdministratorRole: function() {
    var loggedInUser = this.get('loggedInUser');
    if (SC.none(loggedInUser)) {
      return NO;
    }
    var idx = jQuery.inArray('system.administrator', loggedInUser.get('roles'));
    return idx >= 0;
  }.property('loggedInUser').cacheable(),

  /**
   * Call this only once ... it will call itself every 5 minutes
   */
  checkExpiry: function() {
    var pollSeconds = 300000;
    if (this.get('isLoggedIn')) {
      var now = SC.DateTime.create();
      var expiry = this.get('authenticationTokenExpiry');

      // Bring forward pollSeconds so that we don't miss expiry
      expiry = expiry.advance({ second: -1 * pollSeconds });

      if (SC.DateTime.compare(now, expiry) > 0) {
        this.logout();
      }
    }

    setTimeout('Chililog.sessionDataController.checkExpiry()', pollSeconds)
  },

  /**
   * Load the details of the authentication token from cookies (if the user selected 'Remember Me')
   *
   * @param {Boolean} [isAsync] Optional flag to indicate if login is to be performed asynchronously or not. Defaults to YES.
   * @returns {Boolean} YES if successfully loaded, NO if token not loaded and the user has to sign in again
   */
  load: function(isAsync) {
    if (SC.none(isAsync)) {
      isAsync = YES;
    }

    // Get token from local store
    var token = Chililog.localStoreController.getItem(Chililog.AUTHENTICATION_TOKEN_LOCAL_STORE_KEY);
    if (SC.none(token)) {
      this.logout();
      return NO;
    }

    // Assumed logged out
    this.logout();

    // Decode token
    var delimiterIndex = token.indexOf('~~~');
    if (delimiterIndex < 0) {
      return NO;
    }
    var jsonString = token.substr(0, delimiterIndex);
    var json = SC.json.decode(jsonString);
    if (SC.none(json)) {
      return NO;
    }

    var expiryString = json.ExpiresOn;
    if (expiryString === null) {
      return NO;
    }
    var now = SC.DateTime.create();
    var expiry = SC.DateTime.parse(expiryString, SC.DATETIME_ISO8601);
    if (SC.DateTime.compare(now, expiry) > 0) {
      return NO;
    }

    // Get user from server
    var url = '/api/Authentication';
    var request = SC.Request.getUrl(url).async(isAsync).json(YES).header(Chililog.AUTHENTICATION_HEADER_NAME, token);
    var params = { expiry: expiry, token: token };
    if (isAsync) {
      request.notify(this, 'endLoad', params).send();
    } else {
      var response = request.send();
      this.endLoad(response, params);
    }

    return YES;
  },

  /**
   * Process data when user information returns
   *
   * @param {SC.Response} response
   * @param {Object} data hash of parameters pass edin via request.notify()
   */
  endLoad: function(response, params) {
    try {
      // Check status
      this.checkResponse(response);

      // Save authenticated user details
      var authenticatedUserAO = response.get('body');
      var authenticatedUserRecord = Chililog.store.createRecord(Chililog.AuthenticatedUserRecord, {}, authenticatedUserAO['DocumentID']);
      authenticatedUserRecord.fromApiObject(authenticatedUserAO);
      Chililog.store.commitRecords();

      // Save what we have so far
      this.set('authenticationTokenExpiry', params.expiry);
      this.set('authenticationToken', params.token);
      Chililog.localStoreController.setItem(Chililog.AUTHENTICATION_TOKEN_LOCAL_STORE_KEY, params.token);

      // Clear error data
      this.set('error', null);

      // Return YES to signal handling of callback
      return YES;
    }
    catch (err) {
      this.set('error', err);
      SC.Logger.info('Error in endLoad: ' + err.message);
    }
  },

  /**
   * Start async login process
   *
   * @param {String} username The username to use for login
   * @param {String} password The password to use for login
   * @param {Boolean} rememberMe If YES, then token is saved as a cookie.
   * @param {Boolean} [isAsync] Optional flag to indicate if login is to be performed asynchronously or not. Defaults to YES.
   * @param {Object} [callbackTarget] Optional callback object
   * @param {Function} [callbackFunction] Optional callback function in the callback object
   * will be placed in the 'error' property.
   */
  login: function(username, password, rememberMe, isAsync, callbackTarget, callbackFunction) {
    // Get our data from the properties using the SC 'get' methods
    // Need to do this because these properties have been bound/observed.
    if (SC.empty(username)) {
      throw Chililog.$error('_sessionDataController.UsernameRequiredError', null, 'username');
    }

    if (SC.empty(password)) {
      throw Chililog.$error('_sessionDataController.PasswordRequiredError', null, 'password');
    }

    if (SC.none(rememberMe)) {
      rememberMe = NO;
    }

    if (SC.none(isAsync)) {
      isAsync = YES;
    }

    // Assumes the user has logged out - if not force logout
    this.logout();

    // Clear error data
    this.set('error', null);

    var postData = {
      'Username': username,
      'Password': password,
      'ExpiryType': 'Absolute',
      'ExpirySeconds': Chililog.AUTHENTICATION_TOKEN_EXPIRY_SECONDS
    };

    // Simulate a HTTP call to check our data.
    // If the credentials not admin/admin, then get a bad url so we get 404 error
    var url = '/api/Authentication';
    var request = SC.Request.postUrl(url).async(isAsync).json(YES);
    var params = { rememberMe: rememberMe, callbackTarget: callbackTarget, callbackFunction: callbackFunction };
    if (isAsync) {
      request.notify(this, 'endLogin', params).send(postData);
    } else {
      var response = request.send(postData);
      this.endLogin(response, params);
    }

    return;
  },

  /**
   * Callback from beginLogin() after we get a response from the server to process
   * the returned login info.
   *
   * @param {SC.Response} response The HTTP response
   * @param {params} Hash of parameters passed into SC.Request.notify()
   * @returns {Boolean} YES if successful
   */
  endLogin: function(response, params) {
    try {
      // Check status
      this.checkResponse(response);

      // Figure out the expiry
      // Take off 1 hour so that we expire before the token does which means we have time to act
      var expiry = SC.DateTime.create();
      expiry = expiry.advance({ second: Chililog.AUTHENTICATION_TOKEN_EXPIRY_SECONDS });

      // Get the token
      var headers = response.get('headers');
      var token = headers[Chililog.AUTHENTICATION_HEADER_NAME];
      if (SC.none(token)) {
        token = headers[Chililog.AUTHENTICATION_HEADER_NAME_LCASE];
        if (SC.none(token)) {
          throw Chililog.$error('_sessionDataController.TokenNotFoundInResponseError');
        }
      }

      // Save token if rememeberMe
      if (params.rememberMe) {
        Chililog.localStoreController.setItem(Chililog.AUTHENTICATION_TOKEN_LOCAL_STORE_KEY, token);
      }

      // Save authenticated user details
      var authenticatedUserAO = response.get('body');
      var authenticatedUserRecord = Chililog.store.createRecord(Chililog.AuthenticatedUserRecord, {}, authenticatedUserAO['DocumentID']);
      authenticatedUserRecord.fromApiObject(authenticatedUserAO);
      Chililog.store.commitRecords();

      this.set('authenticationToken', token);
      this.set('authenticationTokenExpiry', expiry);

      // Clear error data
      this.set('error', null);
    }
    catch (err) {
      this.set('error', err);
      SC.Logger.info('Error in endLogin: ' + err.message);
    }

    // Callback
    if (!SC.none(params.callbackTarget) && !SC.none(params.callbackFunction)) {
      params.callbackFunction.call(params.callbackTarget, null);
    }

    // Return YES to signal handling of callback
    return YES;
  },

  /**
   * Remove authentication tokens
   */
  logout: function() {
    // Remove token from local store
    Chililog.localStoreController.removeItem(Chililog.AUTHENTICATION_TOKEN_LOCAL_STORE_KEY);

    // Delete authenticated user from store
    var authenticatedUserRecords = Chililog.store.find(Chililog.AuthenticatedUserRecord);
    authenticatedUserRecords.forEach(function(item, index, enumerable) {
      item.destroy();
    }, this);
    Chililog.store.commitRecords();

    // Clear cached token
    this.set('authenticationTokenExpiry', null);
    this.set('authenticationToken', null);
    this.notifyPropertyChange('authenticationToken');

    return;
  }


});
