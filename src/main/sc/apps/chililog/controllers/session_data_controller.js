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
   * Chililog Version sourced from the server upon login/load
   *
   * @type String
   */
  chililogVersion: null,

  /**
   * Chililog build timestamp sourced from the server upon login/load
   *
   * @type String
   */
  chililogBuildTimestamp: null,

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
   * @returns {Boolean} YES if successfully loaded, NO if token not loaded and the user has to sign in again.
   */
  load: function() {
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

    // Synchronously get user from server
    var url = '/api/Authentication';
    var request = SC.Request.getUrl(url).async(NO).json(YES).header(Chililog.AUTHENTICATION_HEADER_NAME, token);
    var response = request.send();

    // Check status
    this.checkResponse(response);

    // Save authenticated user details
    var authenticatedUserAO = response.get('body');
    var authenticatedUserRecord = Chililog.store.createRecord(Chililog.AuthenticatedUserRecord, {},
      authenticatedUserAO[Chililog.DOCUMENT_ID_AO_FIELD_NAME]);
    authenticatedUserRecord.fromApiObject(authenticatedUserAO);
    Chililog.store.commitRecords();

    // Save what we have so far
    this.loadVersionAndBuildInfo(response.get('headers'));
    this.set('authenticationTokenExpiry', expiry);
    this.set('authenticationToken', token);
    Chililog.localStoreController.setItem(Chililog.AUTHENTICATION_TOKEN_LOCAL_STORE_KEY, token);

    // Get data from server
    this.synchronizeServerData(YES);

    // Return YES to signal handling of callback
    return YES;
  },

  /**
   * Start async login process
   *
   * @param {String} username The username to use for login
   * @param {String} password The password to use for login
   * @param {Boolean} rememberMe If YES, then token is saved as a cookie.
   * @param {Boolean} [isAsync] Optional flag to indicate if login is to be performed asynchronously or not. Defaults to YES.
   * @param {Object} [callbackTarget] Optional callback object
   * @param {Function} [callbackFunction] Optional callback function in the callback object. Signature is: function(error) {}.
   *
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

    var postData = {
      'Username': username,
      'Password': password,
      'ExpiryType': 'Absolute',
      'ExpirySeconds': Chililog.AUTHENTICATION_TOKEN_EXPIRY_SECONDS
    };

    // Call server
    var url = '/api/Authentication';
    var request = SC.Request.postUrl(url).async(isAsync).json(YES);
    var params = { rememberMe: rememberMe, callbackTarget: callbackTarget, callbackFunction: callbackFunction };
    if (isAsync) {
      request.notify(this, 'endLogin', params).send(postData);
    } else {
      // For sync logins, throw an error if no callback passed
      if (SC.none(callbackFunction)) {
        params.callbackTarget = this;
        params.callbackFunction = function(error) {
          if (!SC.none(error)) {
            throw error;
          }
        };
      }

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
   * @param {Hash} params Hash of parameters passed into SC.Request.notify()
   * @returns {Boolean} YES if successful
   */
  endLogin: function(response, params) {
    var error = null;
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
        token = headers[Chililog.AUTHENTICATION_HEADER_NAME.toLowerCase()];
        if (SC.none(token)) {
          throw Chililog.$error('_sessionDataController.TokenNotFoundInResponseError');
        }
      }
      this.loadVersionAndBuildInfo(headers);

      // Save token if rememeberMe
      if (params.rememberMe) {
        Chililog.localStoreController.setItem(Chililog.AUTHENTICATION_TOKEN_LOCAL_STORE_KEY, token);
      }

      // Save authenticated user details
      var authenticatedUserAO = response.get('body');
      var authenticatedUserRecord = Chililog.store.createRecord(Chililog.AuthenticatedUserRecord, {},
        authenticatedUserAO[Chililog.DOCUMENT_ID_AO_FIELD_NAME]);
      authenticatedUserRecord.fromApiObject(authenticatedUserAO);
      Chililog.store.commitRecords();

      this.set('authenticationToken', token);
      this.set('authenticationTokenExpiry', expiry);
    }
    catch (err) {
      error = err;
      SC.Logger.error('endLogin: ' + err.message);
    }

    // Get new data from server
    this.synchronizeServerData(YES);

    // Callback
    if (!SC.none(params.callbackFunction)) {
      params.callbackFunction.call(params.callbackTarget, error);
    }

    // Return YES to signal handling of callback
    return YES;
  },

  /**
   * Load version and build info from response headers
   * @param {Map} headers SC.Response header
   */
  loadVersionAndBuildInfo: function(headers) {
    var version = headers[Chililog.VERSION_HEADER_NAME];
    if (SC.none(version)) {
      version = headers[Chililog.VERSION_HEADER_NAME.toLowerCase()];
      if (SC.none(version)) {
        throw Chililog.$error('_sessionDataController.VersionNotFoundInResponseError');
      }
    }
    this.set('chililogVersion', version);

    var buildTimestamp = headers[Chililog.BUILD_TIMESTAMP_HEADER_NAME];
    if (SC.none(buildTimestamp)) {
      buildTimestamp = headers[Chililog.BUILD_TIMESTAMP_HEADER_NAME.toLowerCase()];
      if (SC.none(buildTimestamp)) {
        throw Chililog.$error('_sessionDataController.BuildTimestampNotFoundInResponseError');
      }
    }
    this.set('chililogBuildTimestamp', buildTimestamp);
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

    // Clear local data
    this.synchronizeServerData(YES);

    return;
  },

  /**
   * Returns the logged in user profile for editing. If not logged in, null is returned.
   * @returns {Chililog.AuthenticatedUserRecord}
   */
  editProfile: function() {
    var nestedStore = Chililog.store.chain();
    var authenticatedUserRecord = Chililog.sessionDataController.get('loggedInUser');
    if (SC.none(authenticatedUserRecord)) {
      // Not logged in ... better unload
      return null;
    }

    authenticatedUserRecord = nestedStore.find(authenticatedUserRecord);
    return authenticatedUserRecord;
  },

  /**
   * Saves a profile to the server
   * @param authenticatedUserRecord record to save
   * @param {Object} [callbackTarget] Optional callback object
   * @param {Function} [callbackFunction] Optional callback function in the callback object. Signature is: function(error) {}.
   */
  saveProfile: function(authenticatedUserRecord, callbackTarget, callbackFunction) {
    // Get our data from the properties using the SC 'get' methods
    // Need to do this because these properties have been bound/observed.
    if (SC.empty(authenticatedUserRecord.get('username'))) {
      throw Chililog.$error('_sessionDataController.UsernameRequiredError', null, 'username');
    }

    if (SC.empty(authenticatedUserRecord.get('emailAddress'))) {
      throw Chililog.$error('_sessionDataController.EmailAddressRequiredError', null, 'emailAddress');
    }

    if (SC.empty(authenticatedUserRecord.get('displayName'))) {
      throw Chililog.$error('_sessionDataController.DisplayNameRequiredError', null, 'displayName');
    }

    var postData = authenticatedUserRecord.toApiObject();

    var url = '/api/Authentication?action=update_profile';
    var authToken = this.get('authenticationToken');
    var request = SC.Request.putUrl(url).async(YES).json(YES).header(Chililog.AUTHENTICATION_HEADER_NAME, authToken);
    var params = { callbackTarget: callbackTarget, callbackFunction: callbackFunction };
    request.notify(this, 'endSaveProfile', params).send(postData);

    return;
  },

  /**
   * Callback from saveProfile() after we get a response from the server to process
   * the returned info.
   *
   * @param {SC.Response} response The HTTP response
   * @param {Hash} params Hash of parameters passed into SC.Request.notify()
   * @returns {Boolean} YES if successful
   */
  endSaveProfile: function(response, params) {
    var error = null;
    try {
      // Check status
      this.checkResponse(response);

      // Delete authenticated user from store
      var authenticatedUserRecords = Chililog.store.find(Chililog.AuthenticatedUserRecord);
      authenticatedUserRecords.forEach(function(item, index, enumerable) {
        item.destroy();
      }, this);
      Chililog.store.commitRecords();

      // Save new authenticated user details
      var authenticatedUserAO = response.get('body');
      var authenticatedUserRecord = Chililog.store.createRecord(Chililog.AuthenticatedUserRecord, {},
        authenticatedUserAO[Chililog.DOCUMENT_ID_AO_FIELD_NAME]);
      authenticatedUserRecord.fromApiObject(authenticatedUserAO);
      Chililog.store.commitRecords();

      // Update logged in user by simulating an authentication token change
      this.notifyPropertyChange('authenticationToken');
    }
    catch (err) {
      error = err;
      SC.Logger.error('endSaveProfile: ' + err);
    }

    // Callback
    if (!SC.none(params.callbackFunction)) {
      params.callbackFunction.call(params.callbackTarget, error);
    }

    // Sync user data
    Chililog.userDataController.synchronizeWithServer();

    // Return YES to signal handling of callback
    return YES;
  },

  /**
   * Discard changes
   * @param authenticatedUserRecord record to discard
   */
  discardProfileChanges: function(authenticatedUserRecord) {
    if (!SC.none(authenticatedUserRecord)) {
      var nestedStore = authenticatedUserRecord.get('store');
      nestedStore.destroy();
    }
    return;
  },

  /**
   *
   * @param oldPassword
   * @param newPassword
   * @param confirmNewPassword
   * @param {Object} [callbackTarget] Optional callback object
   * @param {Function} [callbackFunction] Optional callback function in the callback object. Signature is: function(error) {}.
   */
  changePassword: function(oldPassword, newPassword, confirmNewPassword, callbackTarget, callbackFunction) {
    // Get our data from the properties using the SC 'get' methods
    // Need to do this because these properties have been bound/observed.
    if (SC.empty(oldPassword)) {
      throw Chililog.$error('_sessionDataController.OldNewConfirmPasswordRequiredError', null, 'oldPassword');
    }

    if (SC.empty(newPassword)) {
      throw Chililog.$error('_sessionDataController.OldNewConfirmPasswordRequiredError', null, 'newPassword');
    }

    if (SC.empty(confirmNewPassword)) {
      throw Chililog.$error('_sessionDataController.OldNewConfirmPasswordRequiredError', null, 'confirmNewPassword');
    }

    if (newPassword !== confirmNewPassword) {
      throw Chililog.$error('_sessionDataController.ConfirmPasswordError', null, 'newPassword');
    }

    var postData = {
      'DocumentID': this.getPath('loggedInUser.documentID'),
      'OldPassword': oldPassword,
      'NewPassword': newPassword,
      'ConfirmNewPassword': confirmNewPassword
    };

    var url = '/api/Authentication?action=change_password';
    var authToken = this.get('authenticationToken');
    var request = SC.Request.putUrl(url).async(YES).json(YES).header(Chililog.AUTHENTICATION_HEADER_NAME, authToken);
    var params = { callbackTarget: callbackTarget, callbackFunction: callbackFunction };
    request.notify(this, 'endChangePassword', params).send(postData);

    return;
  },

  /**
   * Callback from saveProfile() after we get a response from the server to process
   * the returned info.
   *
   * @param {SC.Response} response The HTTP response
   * @param {Hash} params Hash of parameters passed into SC.Request.notify()
   * @returns {Boolean} YES if successful
   */
  endChangePassword: function(response, params) {
    var error = null;
    try {
      // Check status
      this.checkResponse(response);

      // Delete authenticated user from store
      var authenticatedUserRecords = Chililog.store.find(Chililog.AuthenticatedUserRecord);
      authenticatedUserRecords.forEach(function(item, index, enumerable) {
        item.destroy();
      }, this);
      Chililog.store.commitRecords();

      // Save new authenticated user details
      var authenticatedUserAO = response.get('body');
      var authenticatedUserRecord = Chililog.store.createRecord(Chililog.AuthenticatedUserRecord, {},
        authenticatedUserAO[Chililog.DOCUMENT_ID_AO_FIELD_NAME]);
      authenticatedUserRecord.fromApiObject(authenticatedUserAO);
      Chililog.store.commitRecords();

      // Update logged in user by simulating an authentication token change
      this.notifyPropertyChange('authenticationToken');
    }
    catch (err) {
      error = err;
      SC.Logger.error('endChangePassword: ' + err);
    }

    // Callback
    if (!SC.none(params.callbackFunction)) {
      params.callbackFunction.call(params.callbackTarget, error);
    }

    // Return YES to signal handling of callback
    return YES;
  },

  /**
   * Synchornizes the data in the local store with that on the server
   * 
   * @param {Boolean} clearLoadData YES if we want to clear the store of local data
   */
  synchronizeServerData: function(clearLoadData) {
    Chililog.userDataController.synchronizeWithServer(clearLoadData, null, null);

    // repositoryDataController will call repositoryInfoDataController
    Chililog.repositoryDataController.synchronizeWithServer(clearLoadData, null, null);
  }

});
