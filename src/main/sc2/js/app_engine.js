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
 * Authentication token to expire in 14 days if remember me
 */
App.AUTHENTICATION_REMEMBER_ME_TOKEN_EXPIRY_SECONDS = 60 * 60 * 24 * 14;

/**
 * Authentication token to expire in 20 minutes if not remember me. We have job that will refresh this token as long as
 * browser is open. See App.sesisonEngine.checkExpiry().
 */
App.AUTHENTICATION_SESSION_TOKEN_EXPIRY_SECONDS = 60 * 20;

/**
 * We want to check token every minute
 */
App.AUTHENTICATION_SESSION_TOKEN_REFRESH_POLL_SECONDS = 60;

/**
 * Local store key for our authentication token
 */
App.AUTHENTICATION_TOKEN_LOCAL_STORE_KEY = 'AuthenticationToken';

/**
 * Local store key for flag that indicates if the user wishes to be remembered or logged out after session closes
 */
App.AUTHENTICATION_REMEMBER_ME_LOCAL_STORE_KEY = 'AuthenticationRememberMe';

/**
 * Local store key for the details of the logged in user
 */
App.AUTHENTICATED_USER_LOCAL_STORE_KEY = 'AuthenticatedUser';

/**
 * Local store key for repository status
 */
App.REPOSITORY_STATUS_LOCAL_STORE_KEY = 'RepositoryStatus';

/**
 * Local store key for repository status
 */
App.USERS_LOCAL_STORE_KEY = 'Users';

/**
 * Local store key for repository status
 */
App.REPOSITORY_CONFIG_LOCAL_STORE_KEY = 'RepositoryConfig';


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
  _ajaxError: function(jqXHR, textStatus, errorThrown) {

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

    // Put HTTP status in the error
    error.httpStatus = jqXHR.status;

    // Callback (this should be the context data)
    if (!SC.none(this.callbackFunction)) {
      if (!SC.none(this.documentID) && !SC.none(this.records)) {
        this.callbackFunction.call(this.callbackTarget, this.documentID, this.records, this.callbackParams, error);
      } else if (!SC.none(this.documentID)) {
        this.callbackFunction.call(this.callbackTarget, this.documentID, this.callbackParams, error);
      } else {
        this.callbackFunction.call(this.callbackTarget, this.callbackParams, error);
      }
    }

    return YES;
  },

  /**
   * Creates the standard request header loaded with our authentication token
   */
  _createAjaxRequestHeaders: function() {
    var headers = {};
    headers[App.AUTHENTICATION_HEADER_NAME] = App.sessionEngine.get('authenticationToken');
    return headers;
  },

  /**
   * Gets the named response header
   * @param {jQuery HttpRequest} jqXHR
   * @param {String} headerName name of header to retrieve
   * @return {String} value of the named header or null if not found
   */
  _getAjaxResponseHeader: function(jqXHR, headerName) {
    var value = jqXHR.getResponseHeader(headerName);
    if (SC.none(value)) {
      value = jqXHR.getResponseHeader(headerName.toLowerCase());
    }
    return value;
  },

  /**
   * Process AO and load them into the store
   *
   * @param {Array} aoArray array of API object returned from the server
   * @param {Class} recordType for example App.UserRecord
   */
  _convertApiObjectsToRecords: function(aoArray, recordType) {
    // Set data
    if (!SC.none(aoArray) && SC.isArray(aoArray)) {
      for (var i = 0; i < aoArray.length; i++) {
        var ao = aoArray[i];

        // If record exists, update it otherwise create new one
        var record = App.store.find(recordType, ao.DocumentID);
        if (SC.none(record) || (record.get('status') & SC.Record.DESTROYED)) {
          record = App.store.createRecord(recordType, {}, ao.DocumentID);
        }
        record.fromApiObject(ao);
      }
      App.store.commitRecords();
    }
  },

  /**
   * Converts records in the data store to an array of API objects
   * 
   * @param {Class} recordType type of record in the store to convert
   * @return {Array} Array of API objects
   */
  _convertRecordsToApiObjects: function (recordType) {
    var aoArray = [];
    var records = App.store.find(recordType);
    records.forEach(function(record) {
      aoArray.push(record.toApiObject());
    });

    return aoArray;
  },

  /**
   * Put API objects into local storage for caching
   *
   * If there are more than 1 entry, then we must have been called from load() so we can save the aoArray,
   * otherwise, we flush from the data store
   * @param {String} localStorageKey key to use for saving
   * @param {Array} aoArray array to save; null to flush from data store
   * @param {Class} recordType record type
   */
  _putApiObjectsIntoLocalStorage: function(localStorageKey, aoArray, recordType) {
    if (SC.none(aoArray) || aoArray.length === 1) {
      aoArray = [];
      var records = App.store.find(recordType);
      records.forEach(function(record) {
        aoArray.push(record.toApiObject());
      });
    }
    localStorage.setItem(localStorageKey, JSON.stringify(aoArray));
  },

  /**
   * Retrieves the records from the local storage
   * @param localStorageKey
   */
  _getApiObjectsFromLocalStorage: function(localStorageKey) {
    var aoArray = [];
    var jsonString = localStorage.getItem(localStorageKey);
    if (!SC.empty(jsonString)) {
      aoArray = JSON.parse(jsonString);
    }
    return aoArray;
  }

};

// --------------------------------------------------------------------------------------------------------------------
// userEngine
// --------------------------------------------------------------------------------------------------------------------
/** @class
 *
 * Manages user records and keeps them in sync with the server
 *
 * @extends SC.Object
 */
App.userEngine = SC.Object.create(App.EngineMixin, {

  /**
   * YEs if we are performing a server synchronization
   * @type Boolean
   */
  isLoading: NO,

  /**
   * Removes all repository meta info records in the data store
   */
  clearData: function() {
    var records = App.store.find(App.UserRecord);
    records.forEach(function(record) {
      record.destroy()
    });
    App.store.commitRecords();

    localStorage.removeItem(App.USERS_LOCAL_STORE_KEY);
  },

  /**
   * Synchronize data in the store with the data on the server
   *
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

    // Can we load from local storage?
    var aoArray = this._getApiObjectsFromLocalStorage(App.USERS_LOCAL_STORE_KEY);
    if (!SC.none(aoArray) && aoArray.length > 0) {
      App.userEngine.clearData();
      App.userEngine._convertApiObjectsToRecords(aoArray, App.UserRecord);
      if (!SC.none(callbackFunction)) {
        callbackFunction.call(callbackTarget, callbackParams, null);
      }
      return;
    }

    // We are working
    this.set('isLoading', YES);

    // Get data
    var context = { callbackTarget: callbackTarget, callbackFunction: callbackFunction, callbackParams: callbackParams };
    $.ajax({
      type: 'GET',
      url: '/api/users',
      dataType: 'json',
      contentType: 'application/json; charset=utf-8',
      context: context,
      headers: this._createAjaxRequestHeaders(),
      error: this._ajaxError,
      success: this._endLoad
    });
  },

  /**
   * Process data that the server returns
   *
   * The 'this' object is the context data object.
   *
   * @param {Object} data Deserialized JSON returned form the server
   * @param {String} textStatus Hash of parameters passed into SC.Request.notify()
   * @param {jQueryXMLHttpRequest}  jQuery XMLHttpRequest object
   * @returns {Boolean} YES if successful and NO if not.
   */
  _endLoad: function(data, textStatus, jqXHR) {
    var error = null;
    try {
      App.userEngine.clearData();
      App.userEngine._convertApiObjectsToRecords(data, App.UserRecord);
      App.userEngine._putApiObjectsIntoLocalStorage(App.USERS_LOCAL_STORE_KEY, data, App.UserRecord);
    }
    catch (err) {
      error = err;
      SC.Logger.error('userEngine.endLoad: ' + err.message);
    }

    // Finish loading
    App.userEngine.set('isLoading', NO);

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
  create: function() {
    var nestedStore = App.store.chain();
    var record = nestedStore.createRecord(App.UserRecord, {});
    record.set(App.DOCUMENT_VERSION_RECORD_FIELD_NAME, 0);
    record.set('currentStatus', 'Enabled');
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
    var record = nestedStore.find(App.UserRecord, documentID);
    return record;
  },

  /**
   * Saves the user record to the server
   * @param {App.UserRecord} record record to save
   * @param {Object} [callbackTarget] Optional callback object
   * @param {Function} [callbackFunction] Optional callback function in the callback object.
   * Signature is: function(documentID, callbackParams, error) {}.
   * The documentID will be set to the document ID of the saved record.
   * If there is no error, error will be set to null.
   * @param {Hash} [callbackParams] Optional Hash to pass into the callback function.
   */
  save: function(record, callbackTarget, callbackFunction, callbackParams) {
    var documentID = record.get(App.DOCUMENT_ID_RECORD_FIELD_NAME);
    var documentVersion = record.get(App.DOCUMENT_VERSION_RECORD_FIELD_NAME);
    var isAdding = (SC.none(documentVersion) || documentVersion === 0);
    var data = record.toApiObject();

    var url = '';
    var httpType = '';
    if (isAdding) {
      url = '/api/users/';
      httpType = 'POST';
    } else {
      url = '/api/users/' + documentID;
      httpType = 'PUT';
    }
    var context = { isAdding: isAdding, documentID: documentID, record: record,
      callbackTarget: callbackTarget, callbackFunction: callbackFunction, callbackParams: callbackParams
    };

    // Call server
    $.ajax({
      type: httpType,
      url: url,
      data: JSON.stringify(data),
      dataType: 'json',
      contentType: 'application/json; charset=utf-8',
      context: context,
      headers: this._createAjaxRequestHeaders(),
      error: this._ajaxError,
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
      // Check
      var apiObject = data;
      if (!this.isAdding) {
        this.documentID = apiObject[App.DOCUMENT_ID_AO_FIELD_NAME];
      } else if (this.documentID !== apiObject[App.DOCUMENT_ID_AO_FIELD_NAME]) {
        throw App.$error('_documentIDError', [ this.documentID, apiObject[App.DOCUMENT_ID_AO_FIELD_NAME]]);
      }

      // Remove temp record while creating/editing
      App.userEngine.discardChanges(this.record);

      // Save user details returned from server
      App.userEngine._convertApiObjectsToRecords([data], App.UserRecord);
      App.userEngine._putApiObjectsIntoLocalStorage(App.USERS_LOCAL_STORE_KEY, null, App.UserRecord);

      // If we are editing the logged in user, then we better update the session data
      if (this.record.get(App.DOCUMENT_ID_RECORD_FIELD_NAME) ===
        App.sessionEngine.get('loggedInUser').get(App.DOCUMENT_ID_RECORD_FIELD_NAME)) {
        //TODO
      }
    }
    catch (err) {
      error = err;
      SC.Logger.error('userDataController.endSaveRecord: ' + err);
    }

    // Callback
    if (!SC.none(this.callbackFunction)) {
      this.callbackFunction.call(this.callbackTarget, this.documentID, this.callbackParams, error);
    }

    // Return YES to signal handling of callback
    return YES;
  },

  /**
   * Discard changes by removing nested store
   *
   * @param {App.UserRecord} record User record to discard
   */
  discardChanges: function(record) {
    if (!SC.none(record)) {
      var store = record.get('store');
      store.destroy();
    }
    return;
  },

  /**
   * Deletes the user record on the server
   *
   * @param {String} documentID id of record to delete
   * @param {Object} [callbackTarget] Optional callback object
   * @param {Function} [callbackFunction] Optional callback function in the callback object.
   * Signature is: function(documentID, callbackParams, error) {}.
   * documentId is set to the id of the user to be deleted.
   * If there is no error, error will be set to null.
   * @param {Hash} [callbackParams] Optional Hash to pass into the callback function.
   */
  erase: function(documentID, callbackTarget, callbackFunction, callbackParams) {
    var url = '/api/users/' + documentID;
    var context = { documentID: documentID, callbackTarget: callbackTarget, callbackFunction: callbackFunction, callbackParams: callbackParams
    };

    $.ajax({
      type: 'DELETE',
      url: url,
      dataType: 'json',
      contentType: 'application/json; charset=utf-8',
      context: context,
      headers: this._createAjaxRequestHeaders(),
      error: this._ajaxError,
      success: this.endErase
    });

    return;
  },

  /**
   * Callback from erase()
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
      var record = App.store.find(App.UserRecord, this.documentID);
      record.destroy();
      App.store.commitRecords();

      App.userEngine._putApiObjectsIntoLocalStorage(App.USERS_LOCAL_STORE_KEY, null, App.UserRecord);
    }
    catch (err) {
      error = err;
      SC.Logger.error('userEngine.endErase: ' + err);
    }

    // Callback
    if (!SC.none(this.callbackFunction)) {
      this.callbackFunction.call(this.callbackTarget, this.documentID, this.callbackParams, error);
    }

    // Return YES to signal handling of callback
    return YES;
  }

});


// --------------------------------------------------------------------------------------------------------------------
// repositoryRuntimeInfoEngine
// --------------------------------------------------------------------------------------------------------------------
/** @class
 *
 * Manages repository records and keeps them in sync with the server
 *
 * @extends SC.Object
 */
App.repositoryRuntimeEngine = SC.Object.create(App.EngineMixin, {
  /**
   * YEs if we are performing a server synchronization
   * @type Boolean
   */
  isLoading: NO,

  clearData: function() {
    var records = App.store.find(App.RepositoryStatusRecord);
    records.forEach(function(record) {
      record.destroy()
    });
    App.store.commitRecords();

    localStorage.removeItem(App.REPOSITORY_STATUS_LOCAL_STORE_KEY);
  },

  /**
   * Synchronize data in the store with the data on the server
   *
   * @param {Boolean} loadRepositoryMetaInfo YES will chain a repository meta info refresh after this sync has finished.
   * In this event, callback will only be called after synchronization with RepositoryInfo has finished.
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

    // Can we load from local storage?
    var aoArray = this._getApiObjectsFromLocalStorage(App.REPOSITORY_STATUS_LOCAL_STORE_KEY);
    if (!SC.none(aoArray) && aoArray.length > 0) {
      App.repositoryRuntimeEngine.clearData();
      App.repositoryRuntimeEngine._convertApiObjectsToRecords(aoArray, App.RepositoryStatusRecord);
      if (!SC.none(callbackFunction)) {
        callbackFunction.call(callbackTarget, callbackParams, null);
      }
      return;
    }

    // We are working
    this.set('isLoading', YES);

    // Get data
    var context = {
      callbackTarget: callbackTarget, callbackFunction: callbackFunction, callbackParams: callbackParams
    };

    $.ajax({
      type: 'GET',
      url: '/api/repository_runtime',
      dataType: 'json',
      contentType: 'application/json; charset=utf-8',
      context: context,
      headers: this._createAjaxRequestHeaders(),
      error: this._ajaxError,
      success: this._endLoad
    });
  },


  /**
   * Process data when repository run time information returns
   *
   * The 'this' object is the context data object.
   *
   * @param {Object} data Deserialized JSON returned form the server
   * @param {String} textStatus Hash of parameters passed into SC.Request.notify()
   * @param {jQueryXMLHttpRequest}  jQuery XMLHttpRequest object
   * @returns {Boolean} YES if successful and NO if not.
   */
  _endLoad: function(data, textStatus, jqXHR) {
    var error = null;
    try {
      App.repositoryRuntimeEngine.clearData();
      App.repositoryRuntimeEngine._convertApiObjectsToRecords(data, App.RepositoryStatusRecord);
      App.repositoryRuntimeEngine._putApiObjectsIntoLocalStorage(App.REPOSITORY_STATUS_LOCAL_STORE_KEY, data, App.RepositoryStatusRecord);
    }
    catch (err) {
      error = err;
      SC.Logger.error('repositoryRuntimeEngine.endLoad: ' + err.message);
    }

    // Finish sync'ing
    App.repositoryRuntimeEngine.set('isLoading', NO);

    // Callback
    if (!SC.none(this.callbackFunction)) {
      this.callbackFunction.call(this.callbackTarget, this.callbackParams, error);
    }

    // Return YES to signal handling of callback
    return YES;
  },

  /**
   * YEs if we are performing a server synchronization
   * @type Boolean
   */
  isStartingOrStopping: NO,

  /**
   * Starts a specific repository
   * @param {String} documentID of repository record to start
   * @param {Object} [callbackTarget] Optional callback object
   * @param {Function} [callbackFunction] Optional callback function in the callback object.
   * Signature is: function(documentID, callbackParams, error) {}.
   * documentID will be set to the id of the document that was to be started.
   * If there is no error, error will be set to null.
   * @param {Hash} [callbackParams] Optional Hash to pass into the callback function.
   */
  start: function(documentID, callbackTarget, callbackFunction, callbackParams) {
    // Don't run if we are doing stuff
    if (this.get('isStartingOrStopping')) {
      return;
    }
    this.set('isStartingOrStopping', YES);

    var context = { documentID: documentID, callbackTarget: callbackTarget,
      callbackFunction: callbackFunction, callbackParams: callbackParams
    };

    $.ajax({
      type: 'POST',
      url: '/api/repository_runtime/' + documentID + '?action=start',
      dataType: 'json',
      contentType: 'application/json; charset=utf-8',
      context: context,
      headers: this._createAjaxRequestHeaders(),
      error: this._ajaxError,
      success: this._endStart
    });

    return;
  },

  /**
   * Callback from start() after we get a response from the server to process
   * the returned info.
   *
   * The 'this' object is the context data object.
   *
   * @param {Object} data Deserialized JSON returned form the server
   * @param {String} textStatus Hash of parameters passed into SC.Request.notify()
   * @param {jQueryXMLHttpRequest}  jQuery XMLHttpRequest object
   * @returns {Boolean} YES if successful and NO if not.
   */
  _endStart: function(data, textStatus, jqXHR) {
    var error = null;
    try {
      // Get and check data
      var repoAO = data;
      if (this.documentID !== repoAO[App.DOCUMENT_ID_AO_FIELD_NAME]) {
        throw App.$error('_documentIDError', [ this.documentID, repoAO[App.DOCUMENT_ID_AO_FIELD_NAME]]);
      }

      // Process response
      App.repositoryRuntimeEngine._convertApiObjectsToRecords([repoAO], App.RepositoryStatusRecord);
      App.repositoryRuntimeEngine._putApiObjectsIntoLocalStorage(App.REPOSITORY_STATUS_LOCAL_STORE_KEY, null, App.RepositoryStatusRecord);
    }
    catch (err) {
      error = err;
      SC.Logger.error('repositoryRuntimeEngine.endStart: ' + err);
    }

    App.repositoryRuntimeEngine.set('isStartingOrStopping', NO);

    // Callback
    if (!SC.none(this.callbackFunction)) {
      this.callbackFunction.call(this.callbackTarget, this.documentID, this.callbackParams, error);
    }

    // Return YES to signal handling of callback
    return YES;
  },

  /**
   * Stops a specific repository
   * @param {String} documentID of repository record to stop
   * @param {Object} [callbackTarget] Optional callback object
   * @param {Function} [callbackFunction] Optional callback function in the callback object.
   * Signature is: function(documentID, callbackParams, error) {}.
   * documentID will be set to the id of the repository to be stopped.
   * If there is no error, error will be set to null.
   * @param {Hash} [callbackParams] Optional Hash to pass into the callback function.
   */
  stop: function(documentID, callbackTarget, callbackFunction, callbackParams) {
    // Don't run if we are doing stuff
    if (this.get('isStartingOrStopping')) {
      return;
    }
    this.set('isStartingOrStopping', YES);

    var context = { documentID: documentID, callbackTarget: callbackTarget,
      callbackFunction: callbackFunction, callbackParams: callbackParams
    };

    $.ajax({
      type: 'POST',
      url: '/api/repository_runtime/' + documentID + '?action=stop',
      dataType: 'json',
      contentType: 'application/json; charset=utf-8',
      context: context,
      headers: this._createAjaxRequestHeaders(),
      error: this._ajaxError,
      success: this._endStop
    });

    return;
  },

  /**
   * Callback from stop() after we get a response from the server to process
   * the returned info.
   *
   * The 'this' object is the context data object.
   *
   * @param {Object} data Deserialized JSON returned form the server
   * @param {String} textStatus Hash of parameters passed into SC.Request.notify()
   * @param {jQueryXMLHttpRequest}  jQuery XMLHttpRequest object
   * @returns {Boolean} YES if successful and NO if not.
   */
  _endStop: function(data, textStatus, jqXHR) {
    var error = null;
    try {
      // Get and check data
      var repoAO = data;
      if (this.documentID !== repoAO[this.DOCUMENT_ID_AO_FIELD_NAME]) {
        throw App.$error('_documentIDError', [ this.documentID, repoAO[App.DOCUMENT_ID_AO_FIELD_NAME]]);
      }

      // Merge the data
      App.repositoryRuntimeEngine._convertApiObjectsToRecords([repoAO], App.RepositoryStatusRecord);
      App.repositoryRuntimeEngine._putApiObjectsIntoLocalStorage(App.REPOSITORY_STATUS_LOCAL_STORE_KEY, null, App.RepositoryStatusRecord);
    }
    catch (err) {
      error = err;
      SC.Logger.error('repositoryRuntimeEngine.endStop: ' + err);
    }

    App.repositoryRuntimeEngine.set('isStartingOrStopping', NO);

    // Callback
    if (!SC.none(this.callbackFunction)) {
      this.callbackFunction.call(this.callbackTarget, this.documentID, this.callbackParams, error);
    }

    // Return YES to signal handling of callback
    return YES;
  },

  /**
   * Starts all repositories
   *
   * @param {Object} [callbackTarget] Optional callback object
   * @param {Function} [callbackFunction] Optional callback function in the callback object.
   * Signature is: function(callbackParams, error) {}.
   * If there is no error, error will be set to null.
   * @param {Hash} [callbackParams] Optional Hash to pass into the callback function.
   */
  startAll: function(callbackTarget, callbackFunction, callbackParams) {
    // Don't run if we are doing stuff
    if (this.get('isStartingOrStopping')) {
      return;
    }
    this.set('isStartingOrStopping', YES);

    var context = { callbackTarget: callbackTarget, callbackFunction: callbackFunction, callbackParams: callbackParams };

    $.ajax({
      type: 'POST',
      url: '/api/repositories?action=start',
      dataType: 'json',
      contentType: 'application/json; charset=utf-8',
      context: context,
      headers: this._createAjaxRequestHeaders(),
      error: this._ajaxError,
      success: this._endStartAll
    });

    return;
  },

  /**
   * Process data when repository meta information returns
   *
   * The 'this' object is the context data object.
   *
   * @param {Object} data Deserialized JSON returned form the server
   * @param {String} textStatus Hash of parameters passed into SC.Request.notify()
   * @param {jQueryXMLHttpRequest}  jQuery XMLHttpRequest object
   * @returns {Boolean} YES if successful and NO if not.
   */
  _endStartAll: function(data, textStatus, jqXHR) {
    var error = null;
    try {
      App.repositoryRuntimeEngine.clearData();
      App.repositoryRuntimeEngine._convertApiObjectsToRecords(data, App.RepositoryStatusRecord);
      App.repositoryRuntimeEngine._putApiObjectsIntoLocalStorage(App.REPOSITORY_STATUS_LOCAL_STORE_KEY, data, App.RepositoryStatusRecord);
    }
    catch (err) {
      error = err;
      SC.Logger.error('repositoryRuntimeEngine.endStartAll: ' + err);
    }

    App.repositoryRuntimeEngine.set('isStartingOrStopping', NO);

    // Callback
    if (!SC.none(this.callbackFunction)) {
      this.callbackFunction.call(this.callbackTarget, this.callbackParams, error);
    }

    // Return YES to signal handling of callback
    return YES;
  },

  /**
   * Stops all repositories
   * @param {Object} [callbackTarget] Optional callback object
   * @param {Function} [callbackFunction] Optional callback function in the callback object.
   * Signature is: function(callbackParams, error) {}.
   * If there is no error, error will be set to null.
   * @param {Hash} [callbackParams] Optional Hash to pass into the callback function.
   */
  stopAll: function(callbackTarget, callbackFunction, callbackParams) {
    // Don't run if we are doing stuff
    if (this.get('isStartingOrStopping')) {
      return;
    }
    this.set('isStartingOrStopping', YES);

    var context = { callbackTarget: callbackTarget, callbackFunction: callbackFunction, callbackParams: callbackParams };

    $.ajax({
      type: 'POST',
      url: '/api/repositories?action=stop',
      dataType: 'json',
      contentType: 'application/json; charset=utf-8',
      context: context,
      headers: this._createAjaxRequestHeaders(),
      error: this._ajaxError,
      success: this._endStopAll
    });

    return;
  },

  /**
   * Process data when return from startStopAll
   *
   * The 'this' object is the context data object.
   *
   * @param {Object} data Deserialized JSON returned form the server
   * @param {String} textStatus Hash of parameters passed into SC.Request.notify()
   * @param {jQueryXMLHttpRequest}  jQuery XMLHttpRequest object
   * @returns {Boolean} YES if successful and NO if not.
   */
  _endStopAll: function(data, textStatus, jqXHR) {
    var error = null;
    try {
      App.repositoryRuntimeEngine.clearData();
      App.repositoryRuntimeEngine._convertApiObjectsToRecords(data, App.RepositoryStatusRecord);
      App.repositoryRuntimeEngine._putApiObjectsIntoLocalStorage(App.REPOSITORY_STATUS_LOCAL_STORE_KEY, data, App.RepositoryStatusRecord);
    }
    catch (err) {
      error = err;
      SC.Logger.error('repositoryRuntimeEngine.endStopAll: ' + err);
    }

    App.repositoryRuntimeEngine.set('isStartingOrStopping', NO);

    // Callback
    if (!SC.none(this.callbackFunction)) {
      this.callbackFunction.call(this.callbackTarget, this.callbackParams, error);
    }

    // Return YES to signal handling of callback
    return YES;
  },

  /**
   * Find entries in the specified repository and returns it in the callback
   *
   * @param {Hash} criteria Hash containing the following values
   *  - documentID: unique id of repository info to find entries in
   *  - keywordUsage: 'All' to match entries with all of the listed keywords; or 'Any' to match entries with any of the listed keywords
   *  - keywords: list of keywords to entries must contain
   *  - conditions: hash of mongodb criteria
   *  -
   * @param {Object} [callbackTarget] Optional callback object
   * @param {Function} [callbackFunction] Optional callback function in the callback object.
   * Signature is: function(documentId, records, callbackParams, error) {}.
   * @param {Hash} [callbackParams] Optional Hash to pass into the callback function.
   */
  findLogEntries: function(criteria, callbackTarget, callbackFunction, callbackParams) {
    var conditionsJson = '';
    if (!SC.empty(criteria.conditions)) {
      conditionsJson = JSON.stringify(criteria.conditions);
    }

    var headers = this._createAjaxRequestHeaders();
    headers['X-Chililog-Query-Type'] = 'Find';
    headers['X-Chililog-Conditions'] = conditionsJson;
    headers['X-Chililog-Keywords-Usage'] = criteria.keywordUsage;
    headers['X-Chililog-Keywords'] = criteria.keywords;
    headers['X-Chililog-Start-Page'] = criteria.startPage + '';
    headers['X-Chililog-Records-Per-Page'] = criteria.recordsPerPage + '';
    headers['X-Chililog-Do-Page-Count'] = 'false';

    var context = { criteria: criteria, callbackTarget: callbackTarget,
      callbackFunction: callbackFunction, callbackParams: callbackParams
    };

    $.ajax({
      type: 'GET',
      url: '/api/repository_runtime/' + criteria.documentID + '/entries',
      dataType: 'json',
      contentType: 'application/json; charset=utf-8',
      context: context,
      headers: headers,
      error: this._ajaxError,
      success: this._endFindLogEntries
    });

    return;
  },

  /**
   * Process data when startFind() returns.
   *
   * The 'this' object is the context data object.
   *
   * @param {Object} data Deserialized JSON returned form the server
   * @param {String} textStatus Hash of parameters passed into SC.Request.notify()
   * @param {jQueryXMLHttpRequest}  jQuery XMLHttpRequest object
   * @returns {Boolean} YES if successful and NO if not.
   */
  _endFindLogEntries: function(data, textStatus, jqXHR) {
    var error = null;
    var repoEntryAOArray = [];
    var recordCount = 0;
    try {
      // Fill with new data
      if (!SC.none(data)) {
        repoEntryAOArray = data['find'];
        if (!SC.none(repoEntryAOArray) && SC.isArray(repoEntryAOArray)) {
          
          // Make keywords into an array of text to highlight
          var keywordsRegexArray = [];
          if (!SC.empty(this.criteria.keywords)) {
            var tempArray = this.criteria.keywords.w();
            for (var i = 0; i < tempArray.length; i++) {
              var keyword = tempArray[i];
              if (!SC.empty(keyword)) {
                keywordsRegexArray.push(new RegExp('(' + keyword + ')', 'gi'));
              }
            }
          }

          // Inject keywords hilighting
          recordCount = repoEntryAOArray.length;
          for (var i = 0; i < recordCount; i++) {
            var repoEntryAO = repoEntryAOArray[i];
            App.repositoryRuntimeEngine._injectKeywordHilighting(keywordsRegexArray, repoEntryAO);
          }
        }
      }
    }
    catch (err) {
      error = err;
      SC.Logger.error('repositoryRuntimeEngine._endFindLogEntries: ' + err);
    }

    // Callback
    if (!SC.none(this.callbackFunction)) {
      this.callbackFunction.call(this.callbackTarget, this.documentID, repoEntryAOArray, this.callbackParams, error);
    }

    // Return YES to signal handling of callback
    return YES;
  },

  /**
   * Hilight keywords
   * @param keywordsRegexArray
   * @param repoEntryAO
   */
  _injectKeywordHilighting: function(keywordsRegexArray, repoEntryAO) {
    // Highlight keywords
    var msg = repoEntryAO['message'];
    if (SC.empty(msg)) {
      repoEntryAO['messageWithKeywordsHilighted'] = '';
    } else {
      //Clone to protect original
      msg = new String(msg);

      // Replace keywords to tokens
      for (var i = 0; i < keywordsRegexArray.length; i++) {
        var keywordsRegex = keywordsRegexArray[i];
        msg = msg.replace(keywordsRegex, '~~~Chililog~~~$1###Chililog###');
      }

      // Markup and then replace tokens with tags (so that injected tags don't get marked up)
      var highlightedMsg = msg; //SC.RenderContext.escapeHTML(msg);

      if (keywordsRegexArray.length > 0) {
        highlightedMsg = highlightedMsg.replace(/~~~Chililog~~~/g, '<span class="keyword">');
        highlightedMsg = highlightedMsg.replace(/###Chililog###/g, '</span>');
      }

      repoEntryAO['messageWithKeywordsHilighted'] = highlightedMsg;
    }
    return;
  },

  /**
   * Returns
   * @param [conditions] Optional conditions
   * @param [orderBy] Optional order by property names. Defaults to 'name' if not supplied.
   * @returns {SC.RecordArray} Returns an array of matching records
   */
  getRecords: function(conditions, orderBy) {
    var params = {};
    if (!SC.empty(conditions)) {
      params['conditions'] = conditions;
    }
    if (SC.empty(orderBy)) {
      orderBy = 'name';
    }
    params['orderBy'] = orderBy;

    var query = SC.Query.local(App.RepositoryStatusRecord, params);
    return App.store.find(query);
  }


});

// --------------------------------------------------------------------------------------------------------------------
// repositoryConfigEngine
// --------------------------------------------------------------------------------------------------------------------

/** @class
 *
 * Manages repository meta information records and keeps them in sync with the server
 *
 * @extends SC.Object
 */
App.repositoryConfigEngine = SC.Object.create(App.EngineMixin, {

  /**
   * YES if we are performing a server synchronization
   * @type Boolean
   */
  isLoading: NO,

  /**
   * Removes all repository meta info records in the data store
   */
  clearData: function() {
    var records = App.store.find(App.RepositoryConfigRecord);
    records.forEach(function(record) {
      record.destroy()
    });
    App.store.commitRecords();

    localStorage.removeItem(App.REPOSITORY_CONFIG_LOCAL_STORE_KEY);
  },

  /**
   * Synchronize data in the store with the data on the server
   * We sync repository status after we get all the repository info
   *
   * @param {Boolean} clearData YES will delete data from local store before loading.
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

    // Can we load from local storage?
    var aoArray = this._getApiObjectsFromLocalStorage(App.REPOSITORY_CONFIG_LOCAL_STORE_KEY);
    if (!SC.none(aoArray) && aoArray.length > 0) {
      App.repositoryConfigEngine.clearData();
      App.repositoryConfigEngine._convertApiObjectsToRecords(aoArray, App.RepositoryConfigRecord);
      if (!SC.none(callbackFunction)) {
        callbackFunction.call(callbackTarget, callbackParams, null);
      }
      return;
    }
    
    // We are working
    this.set('isLoading', YES);

    // Get data
    var context = { callbackTarget: callbackTarget, callbackFunction: callbackFunction, callbackParams: callbackParams };
    $.ajax({
      type: 'GET',
      url: '/api/repository_config',
      dataType: 'json',
      contentType: 'application/json; charset=utf-8',
      context: context,
      headers: this._createAjaxRequestHeaders(),
      error: this._ajaxError,
      success: this._endLoad
    });
  },

  /**
   * Process data that the server returns
   *
   * The 'this' object is the context data object.
   *
   * @param {Object} data Deserialized JSON returned form the server
   * @param {String} textStatus Hash of parameters passed into SC.Request.notify()
   * @param {jQueryXMLHttpRequest}  jQuery XMLHttpRequest object
   * @returns {Boolean} YES if successful and NO if not.
   */
  _endLoad: function(data, textStatus, jqXHR) {
    var error = null;
    try {
      App.repositoryConfigEngine.clearData();
      App.repositoryConfigEngine._convertApiObjectsToRecords(data, App.RepositoryConfigRecord);
      App.repositoryConfigEngine._putApiObjectsIntoLocalStorage(App.REPOSITORY_CONFIG_LOCAL_STORE_KEY, data, App.RepositoryConfigRecord);
    }
    catch (err) {
      error = err;
      SC.Logger.error('repositoryConfigEngine.endLoad: ' + err.message);
    }

    // Finish sync'ing
    App.repositoryConfigEngine.set('isLoading', NO);

    // Callback
    if (!SC.none(this.callbackFunction)) {
      this.callbackFunction.call(this.callbackTarget, this.callbackParams, error);
    }

    // Return YES to signal handling of callback
    return YES;
  },

  /**
   * Returns a new record for editing
   *
   * @returns {App.UserRecord}
   */
  create: function(documentID) {
    var nestedStore = App.store.chain();
    var record = nestedStore.createRecord(App.RepositoryConfigRecord, {});
    record.set(App.DOCUMENT_VERSION_RECORD_FIELD_NAME, 0);
    record.set('maxKeywords', 20);
    record.set('writeQueueWorkerCount', 1);
    record.set('writeQueueMaxMemory', 20971520); //20MB
    record.set('writeQueuePageSize', 4194304);  //4MB
    record.set('writeQueuePageCountCache', 3);
    return record;
  },

  /**
   * Returns an existing the record for editing
   *
   * @param {String} documentID Document ID of the user record to edit
   * @returns {App.UserRecord}
   */
  edit: function(documentID) {
    var nestedStore = App.store.chain();
    var record = nestedStore.find(App.RepositoryConfigRecord, documentID);
    return record;
  },

  /**
   * Saves the record to the server
   * @param {App.RepositoryConfigRecord} record record to save
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

    var url = '';
    var httpType = '';
    if (isAdding) {
      url = '/api/repository_config/';
      httpType = 'POST';
    } else {
      url = '/api/repository_config/' + documentID;
      httpType = 'PUT';
    }
    var context = { isAdding: isAdding, documentID: documentID,
      callbackTarget: callbackTarget, callbackFunction: callbackFunction, callbackParams: callbackParams
    };

    // Call server
    $.ajax({
      type: httpType,
      url: url,
      data: JSON.stringify(data),
      dataType: 'json',
      contentType: 'application/json; charset=utf-8',
      context: context,
      headers: this._createAjaxRequestHeaders(),
      error: this._ajaxError,
      success: this._endSave
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
  _endSave: function(data, textStatus, jqXHR) {
    var error = null;
    try {
      // Check
      var apiObject = data;
      if (this.isAdding) {
        this.documentID = apiObject[App.DOCUMENT_ID_AO_FIELD_NAME];
      } else if (this.documentID !== apiObject[App.DOCUMENT_ID_AO_FIELD_NAME]) {
        throw App.$error('_documentIDError', [ this.documentID, apiObject[App.DOCUMENT_ID_AO_FIELD_NAME]]);
      }

      // Remove temp record while creating/editing
      App.repositoryConfigEngine.discardChanges(this.record);

      // Save
      App.repositoryConfigEngine._convertApiObjectsToRecords([data], App.RepositoryConfigRecord);
      App.repositoryConfigEngine._putApiObjectsIntoLocalStorage(App.REPOSITORY_CONFIG_LOCAL_STORE_KEY, null, App.RepositoryConfigRecord);
    }
    catch (err) {
      error = err;
      SC.Logger.error('repositoryInfoDataController.endSaveRecord: ' + err);
    }

    // Callback
    if (!SC.none(this.callbackFunction)) {
      this.callbackFunction.call(this.callbackTarget, this.documentID, this.callbackParams, error);
    }

    // Return YES to signal handling of callback
    return YES;
  },

  /**
   * Discard changes
   * @param {App.RepositoryConfigRecord} record record to discard
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
    var url = '/api/repository_config/' + documentID;
    var context = { documentID: documentID, callbackTarget: callbackTarget, callbackFunction: callbackFunction, callbackParams: callbackParams };

    $.ajax({
      type: 'DELETE',
      url: url,
      dataType: 'json',
      contentType: 'application/json; charset=utf-8',
      context: context,
      headers: this._createAjaxRequestHeaders(),
      error: this._ajaxError,
      success: this._endErase
    });

    return;
  },

  /**
   * Callback from erase()
   *
   * The 'this' object is the context data object.
   *
   * @param {Object} data Deserialized JSON returned form the server
   * @param {String} textStatus Hash of parameters passed into SC.Request.notify()
   * @param {jQueryXMLHttpRequest}  jQuery XMLHttpRequest object
   * @returns {Boolean} YES if successful and NO if not.
   */
  _endErase: function(data, textStatus, jqXHR) {
    var error = null;
    try {
      var record = App.store.find(App.RepositoryConfigRecord, this.documentID);
      record.destroy();
      App.store.commitRecords();

      App.repositoryConfigEngine._putApiObjectsIntoLocalStorage(App.REPOSITORY_CONFIG_LOCAL_STORE_KEY, null, App.RepositoryConfigRecord);
    }
    catch (err) {
      error = err;
      SC.Logger.error('repositoryInfoEngine.endErase: ' + err);
    }

    // Callback
    if (!SC.none(this.callbackFunction)) {
      this.callbackFunction.call(this.callbackTarget, this.documentID, this.callbackParams, error);
    }

    // Return YES to signal handling of callback
    return YES;
  }
});

// --------------------------------------------------------------------------------------------------------------------
// sessionEngine
// --------------------------------------------------------------------------------------------------------------------

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
   * Flag to indicate if the user wants to be remembered so they don't have to login
   *
   * @type Boolean
   */
  authenticationRememberMe: NO,

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
   * Removes all saved data
   */
  clearData: function() {
    // Delete authenticated user from store
    var authenticatedUserRecords = App.store.find(App.AuthenticatedUserRecord);
    authenticatedUserRecords.forEach(function(item, index, enumerable) {
      item.destroy();
    }, this);
    App.store.commitRecords();

    // Clear cached token
    this.set('authenticationTokenExpiry', null);
    this.set('authenticationToken', null);

    localStorage.removeItem(App.AUTHENTICATION_TOKEN_LOCAL_STORE_KEY);
    localStorage.removeItem(App.AUTHENTICATION_REMEMBER_ME_LOCAL_STORE_KEY);
    localStorage.removeItem(App.AUTHENTICATED_USER_LOCAL_STORE_KEY);

    return;
  },

  /**
   * Load the details of the authentication token from cookies (if the user selected 'Remember Me')
   *
   * @param {Boolean} autoCheckExpiry YES to check expiry automatically every 5 minutes
   * @returns {Boolean} YES if authenticated user details successfully loaded, NO if token not loaded and the user has to sign in again.
   */
  load: function(autoCheckExpiry) {
    // Get token from local store
    var token = localStorage.getItem(App.AUTHENTICATION_TOKEN_LOCAL_STORE_KEY);
    if (SC.none(token)) {
      this.logout();
      return NO;
    }

    // Decode token
    var delimiterIndex = token.indexOf('~~~');
    if (delimiterIndex < 0) {
      return NO;
    }
    var tokenInfoString = token.substr(0, delimiterIndex);
    var tokenInfo = JSON.parse(tokenInfoString);
    if (SC.none(tokenInfo)) {
      return NO;
    }

    var expiryString = tokenInfo.ExpiresOn;
    if (expiryString === null) {
      return NO;
    }
    var now = SC.DateTime.create();
    var expiry = SC.DateTime.parse(expiryString, SC.DATETIME_ISO8601);
    if (SC.DateTime.compare(now, expiry) > 0) {
      return NO;
    }

    // Can we load from local storage?
    var aoArray = this._getApiObjectsFromLocalStorage(App.AUTHENTICATED_USER_LOCAL_STORE_KEY);
    if (SC.none(aoArray) || aoArray.length === 0) {
      // Synchronously get user from server if the user details not previously saved
      var responseJqXHR = null;
      var headers = {};
      headers[App.AUTHENTICATION_HEADER_NAME] = token;

      $.ajax({
        url: '/api/Authentication',
        type: 'GET',
        async: false,
        dataType: 'json',
        headers: headers,
        error: this._ajaxError,
        success: function (data, textStatus, jqXHR) {
          aoArray = [data];
          responseJqXHR = jqXHR;
        }
      });

      App.sessionEngine._putApiObjectsIntoLocalStorage(App.AUTHENTICATED_USER_LOCAL_STORE_KEY, aoArray, App.AuthenticatedUserRecord);
    }
    App.sessionEngine._convertApiObjectsToRecords(aoArray, App.AuthenticatedUserRecord);

    // Load what we have so far
    this.set('authenticationToken', token);
    this.set('authenticationTokenExpiry', expiry);

    // Check expiry if requested
    if (SC.none(autoCheckExpiry) || autoCheckExpiry) {
      App.sessionEngine.checkExpiry();
    }

    // Return YES to signal handling of callback
    return YES;
  },

  /**
   * Checks to see if the session has expired
   * Call this only once ... it will call itself periodically as set in App.AUTHENTICATION_SESSION_TOKEN_REFRESH_POLL_SECONDS
   */
  checkExpiry: function() {
    var pollSeconds = App.AUTHENTICATION_SESSION_TOKEN_REFRESH_POLL_SECONDS;
    if (this.get('isLoggedIn')) {
      var now = SC.DateTime.create();
      var expiry = App.sessionEngine.get('authenticationTokenExpiry');

      // Bring forward pollSeconds so that we don't miss expiry
      expiry = expiry.advance({ second: -1 * pollSeconds });
      if (SC.DateTime.compare(now, expiry) > 0) {
        this.logout();
        window.location = 'login.html';
      } else {
        // Bring forward again to see if we need to refresh the token
        var authenticationRememberMe = App.sessionEngine.get('authenticationRememberMe');
        if (!authenticationRememberMe) {
          expiry = expiry.advance({ second: -1 * pollSeconds });
          if (SC.DateTime.compare(now, expiry) > 0) {
            App.sessionEngine.refreshToken();
          }
        }
      }
    }

    setTimeout('App.sessionEngine.checkExpiry()', pollSeconds * 1000)
  },

  /**
   * Refreshes the authentication token. Used for when the user does not want to be remembered.
   *
   * @param {Object} [callbackTarget] Optional callback object
   * @param {Function} [callbackFunction] Optional callback function in the callback object.
   * Signature is: function(callbackParams, error) {}.
   * If there is no error, error will be set to null.
   * @param {Hash} [callbackParams] Optional Hash to pass into the callback function.
   */
  refreshToken: function(callbackTarget, callbackFunction, callbackParams) {
    var postData = {
      'Username': App.sessionEngine.getPath('loggedInUser.username'),
      'Password': '',
      'ExpiryType': 'Absolute',
      'ExpirySeconds': App.AUTHENTICATION_SESSION_TOKEN_EXPIRY_SECONDS
    };

    // Call server
    var url = '/api/Authentication';
    var context = { rememberMe: NO, postData: postData,
      callbackTarget: callbackTarget, callbackFunction: callbackFunction, callbackParams: callbackParams };

    $.ajax({
      type: 'POST',
      url: url,
      data: JSON.stringify(postData),
      dataType: 'json',
      contentType: 'application/json; charset=utf-8',
      context: context,
      headers: this._createAjaxRequestHeaders(),
      error: this._ajaxError,
      success: this._endLogin
    });

    return;
  },

  /**
   * Start login process
   *
   * @param {String} username The username to use for login
   * @param {String} password The password to use for login
   * @param {Boolean} rememberMe If YES, then token is saved as a cookie.
   * @param {Object} [callbackTarget] Optional callback object
   * @param {Function} [callbackFunction] Optional callback function in the callback object.
   * Signature is: function(callbackParams, error) {}.
   * If there is no error, error will be set to null.
   * @param {Hash} [callbackParams] Optional Hash to pass into the callback function.
   */
  login: function(username, password, rememberMe, callbackTarget, callbackFunction, callbackParams) {
    if (SC.none(rememberMe)) {
      rememberMe = NO;
    }

    // Assumes the user has logged out - if not force logout
    this.logout();

    var postData = {
      'Username': username,
      'Password': password,
      'ExpiryType': 'Absolute',
      'ExpirySeconds': rememberMe ? App.AUTHENTICATION_REMEMBER_ME_TOKEN_EXPIRY_SECONDS : App.AUTHENTICATION_SESSION_TOKEN_EXPIRY_SECONDS
    };

    // Call server
    var url = '/api/Authentication';
    var context = { rememberMe: rememberMe, postData: postData,
      callbackTarget: callbackTarget, callbackFunction: callbackFunction, callbackParams: callbackParams };

    $.ajax({
      type: 'POST',
      url: url,
      data: JSON.stringify(postData),
      dataType: 'json',
      contentType: 'application/json; charset=utf-8',
      context: context,
      error: this._ajaxError,
      success: this._endLogin
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
  _endLogin: function(data, textStatus, jqXHR) {
    var error = null;
    try {
      // Get the token
      var token = App.sessionEngine._getAjaxResponseHeader(jqXHR, App.AUTHENTICATION_HEADER_NAME);
      if (SC.none(token)) {
        throw App.$error('_sessionEngine.TokenNotFoundInResponseError');
      }

      // Put authenticated user details into the store
      App.sessionEngine._convertApiObjectsToRecords([data], App.AuthenticatedUserRecord);
      App.sessionEngine._putApiObjectsIntoLocalStorage(App.AUTHENTICATED_USER_LOCAL_STORE_KEY, null, App.AuthenticatedUserRecord);

      // Cache authentication token in this engine
      App.sessionEngine.set('authenticationToken', token);
      localStorage.setItem(App.AUTHENTICATION_TOKEN_LOCAL_STORE_KEY, token);

      // Figure out the expiry for caching in this engine
      // To save time parsing, we just guess that the ajax call cannot taken more than 1 seconds
      var expiry = SC.DateTime.create();
      expiry = expiry.advance({second: this.postData.ExpirySeconds - 1});
      App.sessionEngine.set('authenticationTokenExpiry', expiry);
      localStorage.setItem(App.AUTHENTICATION_REMEMBER_ME_LOCAL_STORE_KEY, this.rememberMe ? 'YES' : 'NO');
    }
    catch (err) {
      error = err;
      SC.Logger.error('_endLogin: ' + err.message);
    }

    // Callback
    if (!SC.none(this.callbackFunction)) {
      this.callbackFunction.call(this.callbackTarget, this.callbackParams, error);
    }

    // Return YES to signal handling of callback
    return YES;
  },

  /**
   * Remove authentication tokens
   */
  logout: function() {
    this.clearData();
    App.userEngine.clearData();
    App.repositoryConfigEngine.clearData();
    App.repositoryRuntimeEngine.clearData();
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
   * Signature is: function(documentID, callbackParams, error) {}.
   * If there is no error, error will be set to null.
   * @param {Hash} [callbackParams] Optional Hash to pass into the callback function.
   */
  saveProfile: function(authenticatedUserRecord, callbackTarget, callbackFunction, callbackParams) {
    var data = authenticatedUserRecord.toApiObject();
    var context = {
      record: authenticatedUserRecord, callbackTarget: callbackTarget, callbackFunction: callbackFunction, callbackParams: callbackParams
    };

    // Call server
    $.ajax({
      type: 'PUT',
      url: '/api/Authentication?action=update_profile',
      data: JSON.stringify(data),
      dataType: 'json',
      contentType: 'application/json; charset=utf-8',
      context: context,
      headers: this._createAjaxRequestHeaders(),
      error: this._ajaxError,
      success: this._endSaveProfile
    });

    return;
  },

  /**
   * Callback from saveProfile() after we get a response from the server to process
   * the returned info.
   *
   * @param {Object} data Deserialized JSON returned form the server
   * @param {String} textStatus Hash of parameters passed into SC.Request.notify()
   * @param {jQueryXMLHttpRequest}  jQuery XMLHttpRequest object
   * @returns {Boolean} YES if successful and NO if not.
   */
  _endSaveProfile: function(data, textStatus, jqXHR) {
    var error = null;
    try {
      // Discard changes
      App.sessionEngine.discardProfileChanges(this.record);

      // Put authenticated user details into the store
      App.sessionEngine._convertApiObjectsToRecords([data], App.AuthenticatedUserRecord);
      App.sessionEngine._putApiObjectsIntoLocalStorage(App.AUTHENTICATED_USER_LOCAL_STORE_KEY, null, App.AuthenticatedUserRecord);

      // Update logged in user by simulating an authentication token change
      App.sessionEngine.notifyPropertyChange('authenticationToken');
    }
    catch (err) {
      error = err;
      SC.Logger.error('endSaveProfile: ' + err);
    }

    // Callback
    if (!SC.none(this.callbackFunction)) {
      this.callbackFunction.call(this.callbackTarget, this.documentID, this.callbackParams, error);
    }

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
   * Changes the authenticated user's password
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
    var data = {
      'DocumentID': this.getPath('loggedInUser.documentID'),
      'OldPassword': oldPassword,
      'NewPassword': newPassword,
      'ConfirmNewPassword': confirmNewPassword
    };

    var context = {
      callbackTarget: callbackTarget, callbackFunction: callbackFunction, callbackParams: callbackParams
    };

    // Call server
    $.ajax({
      type: 'PUT',
      url: '/api/Authentication?action=change_password',
      data: JSON.stringify(data),
      dataType: 'json',
      contentType: 'application/json; charset=utf-8',
      context: context,
      headers: this._createAjaxRequestHeaders(),
      error: this._ajaxError,
      success: this._endChangePassword
    });

    return;
  },

  /**
   * Callback from saveProfile() after we get a response from the server to process
   * the returned info.
   *
   * @param {Object} data Deserialized JSON returned form the server
   * @param {String} textStatus Hash of parameters passed into SC.Request.notify()
   * @param {jQueryXMLHttpRequest}  jQuery XMLHttpRequest object
   * @returns {Boolean} YES if successful and NO if not.
   */
  _endChangePassword: function(data, textStatus, jqXHR) {
    var error = null;
    try {
      // Put authenticated user details into the store
      App.sessionEngine._convertApiObjectsToRecords([data], App.AuthenticatedUserRecord);
      App.sessionEngine._putApiObjectsIntoLocalStorage(App.AUTHENTICATED_USER_LOCAL_STORE_KEY, null, App.AuthenticatedUserRecord);
    }
    catch (err) {
      error = err;
      SC.Logger.error('endChangePassword: ' + err);
    }

    // Callback
    if (!SC.none(this.callbackFunction)) {
      this.callbackFunction.call(this.callbackTarget, this.documentID, this.callbackParams, error);
    }

    // Return YES to signal handling of callback
    return YES;
  }

});

