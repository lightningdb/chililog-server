//
// Copyright 2010 Cinch Logic Pty Ltd.
//
// http://www.App.com
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//

//
// Engines do the grunt work
//

/**
 * Name of cookie where we store the auth token
 */
App.AUTHENTICATION_TOKEN_LOCAL_STORE_KEY = 'App.AuthenticationToken';

/**
 * Name of Authentication header returned in API responses
 */
App.AUTHENTICATION_HEADER_NAME = 'X-Chililog-Authentication';

/**
 * Name of app version header returned in API responses
 */
App.VERSION_HEADER_NAME = 'X-Chililog-Version';

/**
 * Name of app build timestamp header returned in API responses
 */
App.BUILD_TIMESTAMP_HEADER_NAME = 'X-Chililog-Build-Timestamp';

/**
 * All tokens to expire in 14 days
 */
App.AUTHENTICATION_TOKEN_EXPIRY_SECONDS = 60 * 60 * 24 * 14;


/**
 * Common engine methods
 */
App.EngineMixin = {

  /**
   * Checks the return information from the server. Throws error if
   *
   * @param {jQuery HttpRequest} jqXHR
   * @return YES if successful
   */
  ajaxError: function(jqXHR, textStatus, errorThrown) {

    var error = null;
    SC.Logger.error('HTTP error status code: ' + jqXHR.status);
    if (jqXHR.status === 500 || jqXHR.status === 400 || jqXHR.status === 401) {
      SC.Logger.error('HTTP response ' + jqXHR.responseText);

      if (!SC.empty(jqXHR.responseText) && jqXHR.responseText.charAt(0) === '{') {
        var responseJson = $.parseJSON(jqXHR.responseText);
        error = App.$error(responseJson.Message);
      } else {
        error = App.$error('Error connecting to server. ' + jqXHR.status + ' ' + jqXHR.statusText);
      }
    } else {
      error = App.$error('Unexpected HTTP error: ' + jqXHR.status + ' ' + jqXHR.statusText);
    }

    // Callback (this should be the context data)
    if (!SC.none(this.callbackFunction)) {
      this.callbackFunction.call(this.callbackTarget, this.callbackParams, error);
    }

    return YES;
  }

};


/** @class
 *
 * Manages repository meta information records and keeps them in sync with the server
 *
 * @extends SC.Object
 */
App.repositoryMetaInfoEngine = SC.Object.create(App.EngineMixin, {

  /**
   * YES if we are performing a server synchronization
   * @type Boolean
   */
  isLoading: NO,

  /**
   * Removes all repository meta info records in the data store
   */
  clearServerData: function() {
    var records = App.store.find(App.RepositoryMetaInfoRecord);
    records.forEach(function(record) {
      record.destroy()
    });
    App.store.commitRecords();
  },

  /**
   * Synchronize data in the store with the data on the server
   * We sync repository status after we get all the repository info
   *
   * @param {Boolean} clearLocalData YES will delete data from local store before loading.
   * @param {Object} [callbackTarget] Optional callback object
   * @param {Function} [callbackFunction] Optional callback function in the callback object.
   * Signature is: function(callbackParams, error) {}.
   * If there is no error, error will be set to null.
   * @param {Hash} [callbackParams] Optional Hash to pass into the callback function.
   */
  load: function(callbackTarget, callbackFunction, callbackParams) {
    // If operation already under way, just exit
    var isLoading = this.get('isLoading');
    if (isLoading) {
      return;
    }

    // Remove all data
    this.clearServerData();

    // We are working
    this.set('isLoading', YES);

    // Get data
    var url = '/api/repository_info';
    var context = { callbackTarget: callbackTarget, callbackFunction: callbackFunction, callbackParams: callbackParams };
    var headers = {};
    headers[App.AUTHENTICATION_HEADER_NAME] = App.sessionEngine.get('authenticationToken');
    
    $.ajax({
      type: 'GET',
      url: '/api/repository_info',
      dataType: 'json',
      contentType: 'application/json; charset=utf-8',
      context: context,
      headers: headers, 
      error: this.ajaxError,
      success: this.endLoad
    });
  },

  /**
   * Process data when user information returns
   *
   * The 'this' object is the context data object.
   *
   * @param {Object} data Deserialized JSON returned form the server
   * @param {String} textStatus Hash of parameters passed into SC.Request.notify()
   * @param {jQueryXMLHttpRequest}  jQuery XMLHttpRequest object
   * @returns {Boolean} YES if successful and NO if not.
   */
  endLoad: function(data, textStatus, jqXHR) {
    var error = null;
    try {
      // Set data
      var repoMetaInfoAOArray = data;
      if (!SC.none(repoMetaInfoAOArray) && SC.isArray(repoMetaInfoAOArray)) {
        for (var i = 0; i < repoMetaInfoAOArray.length; i++) {
          var repoMetaInfoAO = repoMetaInfoAOArray[i];

          // See if record exists
          var repoMetaInfoRecord = App.store.find(App.RepositoryMetaInfoRecord, repoMetaInfoAO.DocumentID);
          if (SC.none(repoMetaInfoRecord) || (repoMetaInfoRecord.get('status') & SC.Record.DESTROYED)) {
            repoMetaInfoRecord = App.store.createRecord(App.RepositoryMetaInfoRecord, {}, repoMetaInfoAO.DocumentID);
          }

          // Find corresponding repository runtime record
          var query = SC.Query.local(App.RepositoryRuntimeInfoRecord, 'name={name}', {name: repoMetaInfoAO.Name});
          var repoRecords = App.store.find(query);
          if (repoRecords.get('length') > 0) {
            repoMetaInfoRecord.updateStatus(repoRecords.objectAt(0));
          }
          repoMetaInfoRecord.fromApiObject(repoMetaInfoAO);
        }
        App.store.commitRecords();
      }

      // Delete records that have not been returned
      var records = App.store.find(App.RepositoryMetaInfoRecord);
      records.forEach(function(record) {
        var doDelete = YES;
        if (!SC.none(repoMetaInfoAOArray) && SC.isArray(repoMetaInfoAOArray)) {
          for (var i = 0; i < repoMetaInfoAOArray.length; i++) {
            var repoMetaInfoAO = repoMetaInfoAOArray[i];
            if (repoMetaInfoAO[App.DOCUMENT_ID_AO_FIELD_NAME] === record.get(App.DOCUMENT_ID_RECORD_FIELD_NAME)) {
              doDelete = NO;
              break;
            }
          }
        }
        if (doDelete) {
          record.destroy()
        }
      });
      App.store.commitRecords();

    }
    catch (err) {
      error = err;
      SC.Logger.error('repositoryMetaInfoEngine.endLoad: ' + err.message);
    }

    // Finish sync'ing
    App.repositoryMetaInfoEngine.set('isLoading', NO);

    // Callback
    if (!SC.none(this.callbackFunction)) {
      this.callbackFunction.call(this.callbackTarget, this.callbackParams, error);
    }

    // Return YES to signal handling of callback
    return YES;
  },

  /**
   * Returns a new user record for editing
   *
   * @returns {App.UserRecord}
   */
  create: function(documentID) {
    var nestedStore = App.store.chain();
    var record = nestedStore.createRecord(App.RepositoryMetaInfoRecord, {});
    record.set(App.DOCUMENT_VERSION_RECORD_FIELD_NAME, 0);
    record.set('maxKeywords', 20);
    record.set('writeQueueWorkerCount', 1);
    record.set('writeQueueMaxMemory', 20971520); //20MB
    record.set('writeQueuePageSize', 4194304);  //4MB
    record.set('writeQueuePageCountCache', 3);
    return record;
  },

  /**
   * Returns an existing the user record for editing
   *
   * @param {String} documentID Document ID of the user record to edit
   * @returns {App.UserRecord}
   */
  edit: function(documentID) {
    var nestedStore = App.store.chain();
    var record = nestedStore.find(App.RepositoryMetaInfoRecord, documentID);
    return record;
  },

  /**
   * Saves the user record to the server
   * @param {App.RepositoryMetaInfoRecord} record record to save
   * @param {Object} [callbackTarget] Optional callback object
   * @param {Function} [callbackFunction] Optional callback function in the callback object.
   * Signature is: function(documentID, callbackParams, error) {}.
   * documentID will be set to the id of the document that was saved
   * If there is no error, error will be set to null.
   * @param {Hash} [callbackParams] Optional Hash to pass into the callback function.
   */
  save: function(record, callbackTarget, callbackFunction, callbackParams) {

    var documentID = record.get(App.DOCUMENT_ID_RECORD_FIELD_NAME);
    var documentVersion = record.get(App.DOCUMENT_VERSION_RECORD_FIELD_NAME);
    var isAdding = (SC.none(documentVersion) || documentVersion === 0);
    var data = record.toApiObject();
    var request;

    var url = '';
    var httpType = '';
    if (isAdding) {
      url = '/api/repository_info/';
      httpType = 'POST';
    } else {
      url = '/api/repository_info/' + documentID;
      httpType = 'PUT';
    }
    var context = { isAdding: isAdding, documentID: documentID,
      callbackTarget: callbackTarget, callbackFunction: callbackFunction, callbackParams: callbackParams
    };
    var headers = {};
    headers[App.AUTHENTICATION_HEADER_NAME] = App.sessionEngine.get('authenticationToken');
    
    // Call server
    $.ajax({
      type: httpType,
      url: url,
      data: JSON.stringify(data),
      dataType: 'json',
      contentType: 'application/json; charset=utf-8',
      context: context,
      headers: headers,
      error: this.ajaxError,
      success: this.endSave
    });
    
    return;
  },

  /**
   * Callback from save() after we get a response from the server to process
   * the returned info.
   *
   * The 'this' object is the context data object.
   *
   * @param {Object} data Deserialized JSON returned form the server
   * @param {String} textStatus Hash of parameters passed into SC.Request.notify()
   * @param {jQueryXMLHttpRequest}  jQuery XMLHttpRequest object
   * @returns {Boolean} YES if successful and NO if not.
   */
  endSave: function(data, textStatus, jqXHR) {
    var error = null;
    try {
      // Save new authenticated user details
      var apiObject = data;
      if (this.isAdding) {
        this.documentID = apiObject[App.DOCUMENT_ID_AO_FIELD_NAME];
      } else if (this.documentID !== apiObject[App.DOCUMENT_ID_AO_FIELD_NAME]) {
        throw App.$error('_documentIDError', [ this.documentID, apiObject[App.DOCUMENT_ID_AO_FIELD_NAME]]);
      }

      var record = null;
      if (this.isAdding) {
        record = App.store.createRecord(App.RepositoryMetaInfoRecord, {}, this.documentID);
      } else {
        record = App.store.find(App.RepositoryMetaInfoRecord, this.documentID);
      }
      record.fromApiObject(apiObject);
      App.store.commitRecords();
    }
    catch (err) {
      error = err;
      SC.Logger.error('repositoryInfoDataController.endSaveRecord: ' + err);
    }

    // Callback
    if (!SC.none(this.callbackFunction)) {
      this.callbackFunction.call(params.callbackTarget, this.documentID, this.callbackParams, error);
    }

    // Return YES to signal handling of callback
    return YES;
  },

  /**
   * Discard changes
   * @param {App.RepositoryMetaInfoRecord} record record to discard
   */
  discardChanges: function(record) {
    if (!SC.none(record)) {
      var nestedStore = record.get('store');
      nestedStore.destroy();
    }
    return;
  },

  /**
   * Deletes the repository info record on the server
   *
   * @param {String} documentID id of record to delete
   * @param {Object} [callbackTarget] Optional callback object
   * @param {Function} [callbackFunction] Optional callback function in the callback object.
   * Signature is: function(documentID, callbackParams, error) {}.
   * documentID will be set to the id of the document that was saved
   * If there is no error, error will be set to null.
   * @param {Hash} [callbackParams] Optional Hash to pass into the callback function.
   */
  erase: function(documentID, callbackTarget, callbackFunction, callbackParams) {
    var url = '/api/repository_info/' + documentID;
    var context = { documentID: documentID, callbackTarget: callbackTarget, callbackFunction: callbackFunction, callbackParams: callbackParams };
    var headers = {};
    headers[App.AUTHENTICATION_HEADER_NAME] = App.sessionEngine.get('authenticationToken');
    $.ajax({
      type: 'DELETE',
      url: url,
      dataType: 'json',
      contentType: 'application/json; charset=utf-8',
      context: context,
      headers: headers,
      error: this.ajaxError,
      success: this.endErase
    });

    return;
  },

  /**
   * Callback from save() after we get a response from the server to process
   * the returned info.
   *
   * The 'this' object is the context data object.
   *
   * @param {Object} data Deserialized JSON returned form the server
   * @param {String} textStatus Hash of parameters passed into SC.Request.notify()
   * @param {jQueryXMLHttpRequest}  jQuery XMLHttpRequest object
   * @returns {Boolean} YES if successful and NO if not.
   */
  endErase: function(data, textStatus, jqXHR) {
    var error = null;
    try {
      var record = App.store.find(App.RepositoryMetaInfoRecord, this.documentID);
      record.destroy();

      App.store.commitRecords();
    }
    catch (err) {
      error = err;
      SC.Logger.error('repositoryInfoDataController.endErase: ' + err);
    }

    // Callback
    if (!SC.none(this.callbackFunction)) {
      this.callbackFunction.call(this.callbackTarget, this.documentID, this.callbackParams, error);
    }

    // Return YES to signal handling of callback
    return YES;
  }
});


/** @class
 *
 * Handles session work - authentication and logged in user's profile
 */
App.sessionEngine = SC.Object.create(App.EngineMixin, {

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
   * @type App.AuthenticatedUserRecord
   */
  loggedInUser: function() {
    var userRecords = App.store.find(App.AuthenticatedUserRecord);
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
   * YES if the user is a system administrator
   *
   * @type Boolean
   */
  isSystemAdministrator: function() {
    var loggedInUser = this.get('loggedInUser');
    if (SC.none(loggedInUser)) {
      return NO;
    }
    var idx = jQuery.inArray('system.administrator', loggedInUser.get('roles'));
    return idx >= 0;
  }.property('loggedInUser').cacheable(),

  /**
   * YES if the user is an administrator for one or more repositories
   *
   * @type Boolean
   */
  isRepositoryAdministrator: function() {
    var loggedInUser = this.get('loggedInUser');
    if (SC.none(loggedInUser)) {
      return NO;
    }
    var roles = loggedInUser.get('roles');
    if (!SC.none(roles)) {
      for (var i = 0; i < roles.length; i++) {
        var role = roles[i];
        if (role.indexOf('repo.' === 0) &&
          role.indexOf('.' + App.REPOSITORY_ADMINISTRATOR_ROLE) > 0) {
          return YES;
        }
      }
    }
    return NO;
  }.property('loggedInUser').cacheable(),

  /**
   * Checks if the logged in user is the administrator the specified repository
   * @param {App.RepositoryMetaInfoRecord} repositoryRecord
   * @returns Boolean
   */
  isRepositoryAdministratorOf: function(repositoryRecord) {
    var loggedInUser = this.get('loggedInUser');
    if (SC.none(loggedInUser)) {
      return NO;
    }
    var roles = loggedInUser.get('roles');
    if (!SC.none(roles)) {
      var roleName = 'repo.' + repositoryRecord.get('name') + '.' + App.REPOSITORY_ADMINISTRATOR_ROLE;
      for (var i = 0; i < roles.length; i++) {
        if (roles[i] === roleName) {
          return YES;
        }
      }
    }
    return NO;
  },

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

    setTimeout('App.sessionEngine.checkExpiry()', pollSeconds)
  },

  /**
   * Load the details of the authentication token from cookies (if the user selected 'Remember Me')
   *
   * @returns {Boolean} YES if authenticated user details successfully loaded, NO if token not loaded and the user has to sign in again.
   */
  load: function() {
    // Get token from local store
    var token = localStorage.getItem(App.AUTHENTICATION_TOKEN_LOCAL_STORE_KEY);
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
    var json = JSON.parse(jsonString);
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
    var headers = {};
    headers[App.AUTHENTICATION_HEADER_NAME] = token;
    var authenticatedUserAO = null;
    var responseJqXHR = null;

    $.ajax({
      url: '/api/Authentication',
      type: 'GET',
      async: false,
      dataType: 'json',
      headers: headers,
      error: this.ajaxError,
      success: function (data, textStatus, jqXHR) {
        authenticatedUserAO = data;
        responseJqXHR = jqXHR;
      }
    });

    // Save authenticated user details
    var authenticatedUserRecord = App.store.createRecord(App.AuthenticatedUserRecord, {},
      authenticatedUserAO[App.DOCUMENT_ID_AO_FIELD_NAME]);
    authenticatedUserRecord.fromApiObject(authenticatedUserAO);
    App.store.commitRecords();

    // Save what we have so far
    this.loadVersionAndBuildInfo(responseJqXHR);
    this.set('authenticationTokenExpiry', expiry);
    this.set('authenticationToken', token);
    localStorage.setItem(App.AUTHENTICATION_TOKEN_LOCAL_STORE_KEY, token);

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
   * @param {Function} [callbackFunction] Optional callback function in the callback object.
   * Signature is: function(callbackParams, error) {}.
   * If there is no error, error will be set to null.
   * @param {Hash} [callbackParams] Optional Hash to pass into the callback function.
   */
  login: function(username, password, rememberMe, isAsync, callbackTarget, callbackFunction, callbackParams) {
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
      'ExpirySeconds': App.AUTHENTICATION_TOKEN_EXPIRY_SECONDS
    };

    // Call server
    var url = '/api/Authentication';
    var context = { rememberMe: rememberMe, callbackTarget: callbackTarget, callbackFunction: callbackFunction, callbackParams: callbackParams };

    $.ajax({
      type: 'POST',
      url: url,
      data: JSON.stringify(postData),
      dataType: 'json',
      contentType: 'application/json; charset=utf-8',
      context: context,
      error: this.ajaxError,
      success: this.endLogin,
      async: isAsync
    });

    return;
  },

  /**
   * Callback from beginLogin() after we get a response from the server to process
   * the returned login info.  'this' is set to the context object.
   *
   * The 'this' object is the context data object.
   *
   * @param {Object} data Deserialized JSON returned form the server
   * @param {String} textStatus Hash of parameters passed into SC.Request.notify()
   * @param {jQueryXMLHttpRequest}  jQuery XMLHttpRequest object
   * @returns {Boolean} YES if successful and NO if not.
   */
  endLogin: function(data, textStatus, jqXHR) {
    var error = null;
    try {
      // Figure out the expiry
      // Take off 1 hour so that we expire before the token does which means we have time to act
      var expiry = SC.DateTime.create();
      expiry.advance({ second: App.AUTHENTICATION_TOKEN_EXPIRY_SECONDS });

      // Get the token
      var token = jqXHR.getResponseHeader(App.AUTHENTICATION_HEADER_NAME);
      if (SC.none(token)) {
        token = jqXHR.getResponseHeader(App.AUTHENTICATION_HEADER_NAME.toLowerCase());
        if (SC.none(token)) {
          throw App.$error('_sessionEngine.TokenNotFoundInResponseError');
        }
      }
      App.sessionEngine.loadVersionAndBuildInfo(jqXHR);

      // Save token if rememeberMe
      if (this.rememberMe) {
        localStorage.setItem(App.AUTHENTICATION_TOKEN_LOCAL_STORE_KEY, token);
      }

      // Save authenticated user details
      var authenticatedUserAO = jqXHR.responseText;
      var authenticatedUserRecord = App.store.createRecord(App.AuthenticatedUserRecord, {},
        authenticatedUserAO[App.DOCUMENT_ID_AO_FIELD_NAME]);
      authenticatedUserRecord.fromApiObject(authenticatedUserAO);
      App.store.commitRecords();

      App.sessionEngine.set('authenticationToken', token);
      App.sessionEngine.set('authenticationTokenExpiry', expiry);
    }
    catch (err) {
      error = err;
      SC.Logger.error('endLogin: ' + err.message);
    }

    // Callback
    if (!SC.none(this.callbackFunction)) {
      this.callbackFunction.call(this.callbackTarget, this.callbackParams, error);
    }

    // Return YES to signal handling of callback
    return YES;
  },

  /**
   * Load version and build info from response headers.
   *
   * Cannot use 'this' to represent App.sessionEngine because in a callback, this is the context object.
   *
   * @param {JQuery Xml Http Request object} jqXHR SC.Response header
   */
  loadVersionAndBuildInfo: function(jqXHR) {
    var version = jqXHR.getResponseHeader(App.VERSION_HEADER_NAME);
    if (SC.none(version)) {
      version = jqXHR.getResponseHeader(App.VERSION_HEADER_NAME.toLowerCase());
      if (SC.none(version)) {
        throw App.$error('_sessionEngine.VersionNotFoundInResponseError');
      }
    }
    App.sessionEngine.set('chililogVersion', version);

    var buildTimestamp = jqXHR.getResponseHeader(App.BUILD_TIMESTAMP_HEADER_NAME);
    if (SC.none(buildTimestamp)) {
      buildTimestamp = jqXHR.getResponseHeader(App.BUILD_TIMESTAMP_HEADER_NAME.toLowerCase());
      if (SC.none(buildTimestamp)) {
        throw App.$error('_sessionEngine.BuildTimestampNotFoundInResponseError');
      }
    }
    App.sessionEngine.set('chililogBuildTimestamp', buildTimestamp);
  },

  /**
   * Remove authentication tokens
   */
  logout: function() {
    // Remove token from local store
    localStorage.removeItem(App.AUTHENTICATION_TOKEN_LOCAL_STORE_KEY);

    // Delete authenticated user from store
    var authenticatedUserRecords = App.store.find(App.AuthenticatedUserRecord);
    authenticatedUserRecords.forEach(function(item, index, enumerable) {
      item.destroy();
    }, this);
    App.store.commitRecords();

    // Clear cached token
    this.set('authenticationTokenExpiry', null);
    this.set('authenticationToken', null);
    this.notifyPropertyChange('authenticationToken');

    // Clear local data
    this.clearServerData();

    return;
  },

  /**
   * Returns the logged in user profile for editing. If not logged in, null is returned.
   * @returns {App.AuthenticatedUserRecord}
   */
  editProfile: function() {
    var nestedStore = App.store.chain();
    var authenticatedUserRecord = App.sessionEngine.get('loggedInUser');
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
   * @param {Function} [callbackFunction] Optional callback function in the callback object.
   * Signature is: function(callbackParams, error) {}.
   * If there is no error, error will be set to null.
   * @param {Hash} [callbackParams] Optional Hash to pass into the callback function.
   */
  saveProfile: function(authenticatedUserRecord, callbackTarget, callbackFunction, callbackParams) {
    var postData = authenticatedUserRecord.toApiObject();

    var url = '/api/Authentication?action=update_profile';
    var authToken = this.get('authenticationToken');
    var request = SC.Request.putUrl(url).async(YES).json(YES).header(App.AUTHENTICATION_HEADER_NAME, authToken);
    var params = { callbackTarget: callbackTarget, callbackFunction: callbackFunction, callbackParams: callbackParams };
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
      var authenticatedUserRecords = App.store.find(App.AuthenticatedUserRecord);
      authenticatedUserRecords.forEach(function(item, index, enumerable) {
        item.destroy();
      }, this);
      App.store.commitRecords();

      // Save new authenticated user details
      var authenticatedUserAO = response.get('body');
      var authenticatedUserRecord = App.store.createRecord(App.AuthenticatedUserRecord, {},
        authenticatedUserAO[App.DOCUMENT_ID_AO_FIELD_NAME]);
      authenticatedUserRecord.fromApiObject(authenticatedUserAO);
      App.store.commitRecords();

      // Update logged in user by simulating an authentication token change
      this.notifyPropertyChange('authenticationToken');
    }
    catch (err) {
      error = err;
      SC.Logger.error('endSaveProfile: ' + err);
    }

    // Callback
    if (!SC.none(params.callbackFunction)) {
      params.callbackFunction.call(params.callbackTarget, params.callbackParams, error);
    }

    // Sync user data
    App.userDataController.synchronizeWithServer(NO);

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
   * @param {Function} [callbackFunction] Optional callback function in the callback object.
   * Signature is: function(callbackParams, error) {}.
   * If there is no error, error will be set to null.
   * @param {Hash} [callbackParams] Optional Hash to pass into the callback function.
   */
  changePassword: function(oldPassword, newPassword, confirmNewPassword, callbackTarget, callbackFunction, callbackParams) {
    var postData = {
      'DocumentID': this.getPath('loggedInUser.documentID'),
      'OldPassword': oldPassword,
      'NewPassword': newPassword,
      'ConfirmNewPassword': confirmNewPassword
    };

    var url = '/api/Authentication?action=change_password';
    var authToken = this.get('authenticationToken');
    var request = SC.Request.putUrl(url).async(YES).json(YES).header(App.AUTHENTICATION_HEADER_NAME, authToken);
    var params = { callbackTarget: callbackTarget, callbackFunction: callbackFunction, callbackParams: callbackParams };
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
      var authenticatedUserRecords = App.store.find(App.AuthenticatedUserRecord);
      authenticatedUserRecords.forEach(function(item, index, enumerable) {
        item.destroy();
      }, this);
      App.store.commitRecords();

      // Save new authenticated user details
      var authenticatedUserAO = response.get('body');
      var authenticatedUserRecord = App.store.createRecord(App.AuthenticatedUserRecord, {},
        authenticatedUserAO[App.DOCUMENT_ID_AO_FIELD_NAME]);
      authenticatedUserRecord.fromApiObject(authenticatedUserAO);
      App.store.commitRecords();

      // Update logged in user by simulating an authentication token change
      this.notifyPropertyChange('authenticationToken');
    }
    catch (err) {
      error = err;
      SC.Logger.error('endChangePassword: ' + err);
    }

    // Callback
    if (!SC.none(params.callbackFunction)) {
      params.callbackFunction.call(params.callbackTarget, params.callbackParams, error);
    }

    // Return YES to signal handling of callback
    return YES;
  },

  clearServerData: function() {

  }

});

