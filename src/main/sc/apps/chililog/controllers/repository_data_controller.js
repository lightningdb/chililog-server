// ==========================================================================
// Project:   Chililog
// Copyright: ©2011 My Company, Inc.
// ==========================================================================

sc_require('controllers/data_controller_mixin');

/** @class

  Manages repository records and keeps them in sync with the server

 @extends SC.Object
 */
Chililog.repositoryDataController = SC.ObjectController.create(Chililog.DataControllerMixin,
/** @scope Chililog.userDataController.prototype */ {

  /**
   * YEs if we are performing a server synchronization
   * @type Boolean
   */
  isSynchronizingWithServer: NO,

  /**
   * Synchronize data in the store with the data on the server
   *
   * @param {Boolean} clearLocalData YES will delete data from local store before loading.
   * @param {Boolean} synchronizeRepositoryInfo YES will chain a repository info refresh after this sync has finished.
   * In this event, callback will only be called after synchronization with RepositoryInfo has finished.
   * @param {Object} [callbackTarget] Optional callback object
   * @param {Function} [callbackFunction] Optional callback function in the callback object.
   * Signature is: function(callbackParams, error) {}.
   * If there is no error, error will be set to null.
   * @param {Hash} [callbackParams] Optional Hash to pass into the callback function.
   */
  synchronizeWithServer: function(clearLocalData, synchronizeRepositoryInfo, callbackTarget, callbackFunction, callbackParams) {
    // If operation already under way, just exit
    var isSynchronizingWithServer = this.get('isSynchronizingWithServer');
    if (isSynchronizingWithServer) {
      return;
    }

    if (clearLocalData) {
      var records = Chililog.store.find(Chililog.RepositoryRecord);
      records.forEach(function(record) {
        record.destroy()
      });
      Chililog.store.commitRecords();
    }

    // Not logged in, so cannot sync
    var authToken = Chililog.sessionDataController.get('authenticationToken');
    if (SC.empty(authToken)) {
      Chililog.repositoryInfoDataController.synchronizeWithServer(clearLocalData);
      return;
    }

    // We are working
    this.set('isSynchronizingWithServer', YES);

    // Get data
    var params = {
      clearLocalData: clearLocalData, synchronizeRepositoryInfo: synchronizeRepositoryInfo,
      callbackTarget: callbackTarget, callbackFunction: callbackFunction, callbackParams: callbackParams
    };
    var url = '/api/repositories';
    var request = SC.Request.getUrl(url).async(YES).json(YES).header(Chililog.AUTHENTICATION_HEADER_NAME, authToken);
    request.notify(this, 'endSynchronizeWithServer', params).send();
  },

  /**
   * Process data when user information returns
   *
   * @param {SC.Response} response
   */
  endSynchronizeWithServer: function(response, params) {
    var error = null;
    try {
      // Check status
      this.checkResponse(response);

      // Set data
      var repoAOArray = response.get('body');
      this._processAOArray(repoAOArray);
    }
    catch (err) {
      error = err;
      SC.Logger.error('repositoryDataController.endSynchronizeWithServer: ' + err.message);
    }

    // Finish sync'ing
    this.set('isSynchronizingWithServer', NO);

    // Chain sync or not
    if (SC.none(params.synchronizeRepositoryInfo)) {
      // Callback
      if (!SC.none(params.callbackFunction)) {
        params.callbackFunction.call(params.callbackTarget, params.callbackParams, error);
      }
    } else {
      // Chain sync
      Chililog.repositoryInfoDataController.synchronizeWithServer(params.clearLocalData, params.callbackTarget,
        params.callbackFunction, params.callbackParams);
    }

    // Return YES to signal handling of callback
    return YES;
  },

  /**
   * Merge new AO array status with the current data in the store
   *
   * @param repoAOArray
   */
  _processAOArray: function (repoAOArray) {
    // Set data
    if (!SC.none(repoAOArray) && SC.isArray(repoAOArray)) {
      for (var i = 0; i < repoAOArray.length; i++) {
        var repoAO = repoAOArray[i];

        // See if record exists
        var repoRecord = Chililog.store.find(Chililog.RepositoryRecord, repoAO.DocumentID);
        if (SC.none(repoRecord) || (repoRecord.get('status') & SC.Record.DESTROYED)) {
          repoRecord = Chililog.store.createRecord(Chililog.RepositoryRecord, {}, repoAO.DocumentID);
        }
        repoRecord.fromApiObject(repoAO);
      }
      Chililog.store.commitRecords();
    }

    // Delete records that have not been returned
    var records = Chililog.store.find(Chililog.RepositoryRecord);
    records.forEach(function(record) {
      var doDelete = YES;
      if (!SC.none(repoAOArray) && SC.isArray(repoAOArray)) {
        for (var i = 0; i < repoAOArray.length; i++) {
          var repoAO = repoAOArray[i];
          if (repoAO[Chililog.DOCUMENT_ID_AO_FIELD_NAME] === record.get(Chililog.DOCUMENT_ID_RECORD_FIELD_NAME)) {
            doDelete = NO;
            break;
          }
        }
      }
      if (doDelete) {
        record.destroy()
      }
    });
    Chililog.store.commitRecords();

    // Make sure that all repository info records have the correct repository record
    var repoInfoRecords = Chililog.store.find(Chililog.RepositoryInfoRecord);
    repoInfoRecords.forEach(function(repoInfoRecord) {
      // Find corresponding repository record
      var query = SC.Query.local(Chililog.RepositoryRecord, 'name={name}', {name: repoInfoRecord.get('name')});
      var repoRecords = Chililog.store.find(query);
      if (repoRecords.get('length') > 0) {
        repoInfoRecord.set('repository', repoRecords.objectAt(0));
      }
    });
    Chililog.store.commitRecords();

  },

  /**
   * YEs if we are performing a server synchronization
   * @type Boolean
   */
  isStartingOrStopping: NO,

  /**
   * Starts a specific repository
   * @param {String} documentID record to start
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

    var authToken = Chililog.sessionDataController.get('authenticationToken');

    var url = '/api/repository/' + documentID + '?action=start';
    var request = SC.Request.postUrl(url).async(YES).json(YES).header(Chililog.AUTHENTICATION_HEADER_NAME, authToken);
    var params = { documentID: documentID, callbackTarget: callbackTarget,
      callbackFunction: callbackFunction, callbackParams: callbackParams 
    };
    request.notify(this, 'endStart', params).send({dummy: 'data'});

    return;
  },

  /**
   * Callback from start() after we get a response from the server to process
   * the returned info.
   *
   * @param {SC.Response} response The HTTP response
   * @param {Hash} params Hash of parameters passed into SC.Request.notify()
   * @returns {Boolean} YES if successful
   */
  endStart: function(response, params) {
    var error = null;
    try {
      // Check status
      this.checkResponse(response);

      // Get and check data
      var repoAO = response.get('body');
      if (params.documentID !== repoAO[Chililog.DOCUMENT_ID_AO_FIELD_NAME]) {
        throw Chililog.$error('_documentIDError', [ params.documentID, repoAO[Chililog.DOCUMENT_ID_AO_FIELD_NAME]]);
      }

      // Process response
      this._processAOArray([repoAO]);
    }
    catch (err) {
      error = err;
      SC.Logger.error('repositoryDataController.endStart: ' + err);
    }

    this.set('isStartingOrStopping', NO);

    // Callback
    if (!SC.none(params.callbackFunction)) {
      params.callbackFunction.call(params.callbackTarget, params.documentID, params.callbackParams, error);
    }

    // Return YES to signal handling of callback
    return YES;
  },

  /**
   * Stops a specific repository
   * @param {String} documentID record to stop
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

    var authToken = Chililog.sessionDataController.get('authenticationToken');

    var url = '/api/repository/' + documentID + '?action=stop';
    var request = SC.Request.postUrl(url).async(YES).json(YES).header(Chililog.AUTHENTICATION_HEADER_NAME, authToken);
    var params = { documentID: documentID, callbackTarget: callbackTarget,
      callbackFunction: callbackFunction, callbackParams: callbackParams 
    };
    request.notify(this, 'endStop', params).send({dummy: 'data'});

    return;
  },

  /**
   * Callback from start() after we get a response from the server to process
   * the returned info.
   *
   * @param {SC.Response} response The HTTP response
   * @param {Hash} params Hash of parameters passed into SC.Request.notify()
   * @returns {Boolean} YES if successful
   */
  endStop: function(response, params) {
    var error = null;
    try {
      // Check status
      this.checkResponse(response);

      // Get and check data
      var repoAO = response.get('body');
      if (params.documentID !== repoAO[Chililog.DOCUMENT_ID_AO_FIELD_NAME]) {
        throw Chililog.$error('_documentIDError', [ params.documentID, repoAO[Chililog.DOCUMENT_ID_AO_FIELD_NAME]]);
      }

      // Merge the data
      this._processAOArray([repoAO]);
    }
    catch (err) {
      error = err;
      SC.Logger.error('repositoryDataController.endStop: ' + err);
    }

    this.set('isStartingOrStopping', NO);

    // Callback
    if (!SC.none(params.callbackFunction)) {
      params.callbackFunction.call(params.callbackTarget, params.documentID, params.callbackParams, error);
    }

    // Return YES to signal handling of callback
    return YES;
  },

  /**
   * Starts all repositories
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

    var authToken = Chililog.sessionDataController.get('authenticationToken');

    var url = '/api/repository?action=start';
    var request = SC.Request.postUrl(url).async(YES).json(YES).header(Chililog.AUTHENTICATION_HEADER_NAME, authToken);
    var params = { callbackTarget: callbackTarget, callbackFunction: callbackFunction, callbackParams: callbackParams };
    request.notify(this, 'endStartAll', params).send({dummy: 'data'});

    return;
  },

  /**
   * Callback from start() after we get a response from the server to process
   * the returned info.
   *
   * @param {SC.Response} response The HTTP response
   * @param {Hash} params Hash of parameters passed into SC.Request.notify()
   * @returns {Boolean} YES if successful
   */
  endStartAll: function(response, params) {
    var error = null;
    try {
      // Check status
      this.checkResponse(response);

      // Set data
      var repoAOArray = response.get('body');
      this._processAOArray(repoAOArray);
    }
    catch (err) {
      error = err;
      SC.Logger.error('repositoryDataController.endStartAll: ' + err);
    }

    this.set('isStartingOrStopping', NO);

    // Callback
    if (!SC.none(params.callbackFunction)) {
      params.callbackFunction.call(params.callbackTarget, params.callbackParams, error);
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

    var authToken = Chililog.sessionDataController.get('authenticationToken');

    var url = '/api/repository?action=stop';
    var request = SC.Request.postUrl(url).async(YES).json(YES).header(Chililog.AUTHENTICATION_HEADER_NAME, authToken);
    var params = { callbackTarget: callbackTarget, callbackFunction: callbackFunction, callbackParams: callbackParams };
    request.notify(this, 'endStopAll', params).send({dummy: 'data'});

    return;
  },

  /**
   * Callback from start() after we get a response from the server to process
   * the returned info.
   *
   * @param {SC.Response} response The HTTP response
   * @param {Hash} params Hash of parameters passed into SC.Request.notify()
   * @returns {Boolean} YES if successful
   */
  endStopAll: function(response, params) {
    var error = null;
    try {
      // Check status
      this.checkResponse(response);

      // Set data
      var repoAOArray = response.get('body');
      this._processAOArray(repoAOArray);
    }
    catch (err) {
      error = err;
      SC.Logger.error('repositoryDataController.endStop: ' + err);
    }

    this.set('isStartingOrStopping', NO);

    // Callback
    if (!SC.none(params.callbackFunction)) {
      params.callbackFunction.call(params.callbackTarget, params.callbackParams, error);
    }

    // Return YES to signal handling of callback
    return YES;
  }

});
