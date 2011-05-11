// ==========================================================================
// Project:   Chililog
// Copyright: ©2011 My Company, Inc.
// ==========================================================================


/** @class

  Manages user records and keeps them in sync with the server

 @extends SC.Object
 */
Chililog.userDataController = SC.ObjectController.create(Chililog.DataControllerMixin,
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
   * @param {Object} [callbackTarget] Optional callback object
   * @param {Function} [callbackFunction] Optional callback function in the callback object.
   * Signature is: function(callbackParams, error) {}.
   * If there is no error, error will be set to null.
   * @param {Hash} [callbackParams] Optional Hash to pass into the callback function.
   */
  synchronizeWithServer: function(clearLocalData, callbackTarget, callbackFunction, callbackParams) {
    // If operation already under way, just exit
    var isSynchronizingWithServer = this.get('isSynchronizingWithServer');
    if (isSynchronizingWithServer) {
      return;
    }

    if (clearLocalData) {
      var records = Chililog.store.find(Chililog.UserRecord);
      records.forEach(function(record) {
        record.destroy()
      });
      Chililog.store.commitRecords();
    }

    // Not logged in, so cannot sync
    var authToken = Chililog.sessionDataController.get('authenticationToken');
    if (SC.empty(authToken)) {
      return;
    }

    // We are working
    this.set('isSynchronizingWithServer', YES);

    // Get data
    var params = { callbackTarget: callbackTarget, callbackFunction: callbackFunction, callbackParams: callbackParams };
    var url = '/api/Users';
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
      var userAOArray = response.get('body');
      if (!SC.none(userAOArray) && SC.isArray(userAOArray)) {
        for (var i = 0; i < userAOArray.length; i++) {
          var userAO = userAOArray[i];

          // See if user record exists
          var userRecord = Chililog.store.find(Chililog.UserRecord, userAO.DocumentID);
          if (SC.none(userRecord) || (userRecord.get('status') & SC.Record.DESTROYED)) {
            userRecord = Chililog.store.createRecord(Chililog.UserRecord, {}, userAO.DocumentID);
          }
          userRecord.fromApiObject(userAO);
        }
        Chililog.store.commitRecords();
      }

      // Delete records that have not been returned
      var records = Chililog.store.find(Chililog.UserRecord);
      records.forEach(function(record) {
        var doDelete = YES;
        if (!SC.none(userAOArray) && SC.isArray(userAOArray)) {
          for (var i = 0; i < userAOArray.length; i++) {
            var userAO = userAOArray[i];
            if (userAO[Chililog.DOCUMENT_ID_AO_FIELD_NAME] === record.get(Chililog.DOCUMENT_ID_RECORD_FIELD_NAME)) {
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
      
    }
    catch (err) {
      error = err;
      SC.Logger.error('userDataController.endSynchronizeWithServer: ' + err.message);
    }

    // Finish sync'ing
    this.set('isSynchronizingWithServer', NO);

    // Callback
    if (!SC.none(params.callbackFunction)) {
      params.callbackFunction.call(params.callbackTarget, params.callbackParams, error);
    }

    // Return YES to signal handling of callback
    return YES;
  },

  /**
   * Returns a new user record for editing
   *
   * @returns {Chililog.UserRecord}
   */
  create: function() {
    var nestedStore = Chililog.store.chain();
    var record = nestedStore.createRecord(Chililog.UserRecord, {});
    record.set(Chililog.DOCUMENT_VERSION_RECORD_FIELD_NAME,  0);
    record.set('currentStatus', 'Enabled');
    return record;
  },

  /**
   * Returns an existing the user record for editing
   *
   * @param {String} documentID Document ID of the user record to edit
   * @returns {Chililog.UserRecord}
   */
  edit: function(documentID) {
    var nestedStore = Chililog.store.chain();
    var record = nestedStore.find(Chililog.UserRecord, documentID);
    return record;
  },

  /**
   * Saves the user record to the server
   * @param {Chililog.UserRecord} record record to save
   * @param {Object} [callbackTarget] Optional callback object
   * @param {Function} [callbackFunction] Optional callback function in the callback object.
   * Signature is: function(documentID, callbackParams, error) {}.
   * The documentID will be set to the document ID of the saved record.
   * If there is no error, error will be set to null. 
   * @param {Hash} [callbackParams] Optional Hash to pass into the callback function.
   */
  save: function(record, callbackTarget, callbackFunction, callbackParams) {
    var documentID = record.get(Chililog.DOCUMENT_ID_RECORD_FIELD_NAME);
    var documentVersion = record.get(Chililog.DOCUMENT_VERSION_RECORD_FIELD_NAME);
    var isCreating = (SC.none(documentVersion) || documentVersion ===  0);

    var data = record.toApiObject();
    var authToken = Chililog.sessionDataController.get('authenticationToken');

    var request;
    if (isCreating) {
      var url = '/api/users/';
      request = SC.Request.postUrl(url).async(YES).json(YES).header(Chililog.AUTHENTICATION_HEADER_NAME, authToken);
    } else {
      var url = '/api/users/' + documentID;
      request = SC.Request.putUrl(url).async(YES).json(YES).header(Chililog.AUTHENTICATION_HEADER_NAME, authToken);
    }
    var params = { documentID: documentID, isCreating: isCreating,
      callbackTarget: callbackTarget, callbackFunction: callbackFunction, callbackParams: callbackParams 
    };
    request.notify(this, 'endSave', params).send(data);

    return;
  },

  /**
   * Callback from save() after we get a response from the server to process
   * the returned info.
   *
   * @param {SC.Response} response The HTTP response
   * @param {Hash} params Hash of parameters passed into SC.Request.notify()
   * @returns {Boolean} YES if successful
   */
  endSave: function(response, params) {
    var error = null;
    var documentID = params.documentID;
    try {
      // Check status
      this.checkResponse(response);

      // Remove temp record while creating/editing
      this.discardChanges(params.record);

      // Save user details returns from server
      var apiObject = response.get('body');
      if (documentID !== apiObject[Chililog.DOCUMENT_ID_AO_FIELD_NAME]) {
        throw Chililog.$error('_documentIDError', documentID, apiObject[Chililog.DOCUMENT_ID_AO_FIELD_NAME]);
      }

      var record = null;
      if (params.isCreating) {
        record = Chililog.store.createRecord(Chililog.UserRecord, {}, documentID);
      } else {
        record = Chililog.store.find(Chililog.UserRecord, documentID);
      }
      record.fromApiObject(apiObject);
      Chililog.store.commitRecords();

      // If we are editing the logged in user, then we better update the session data
      if (record.get(Chililog.DOCUMENT_ID_RECORD_FIELD_NAME) ===
          Chililog.sessionDataController.get('loggedInUser').get(Chililog.DOCUMENT_ID_RECORD_FIELD_NAME)) {
        //TODO
      }
    }
    catch (err) {
      error = err;
      SC.Logger.error('userDataController.endSaveRecord: ' + err);
    }

    // Callback
    if (!SC.none(params.callbackFunction)) {
      params.callbackFunction.call(params.callbackTarget, documentID, params.callbackParams, error);
    }

    // Return YES to signal handling of callback
    return YES;
  },

  /**
   * Discard changes by removing nested store
   * 
   * @param {Chililog.UserRecord} record User record to discard
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
    var authToken = Chililog.sessionDataController.get('authenticationToken');

    var url = '/api/users/' + documentID;
    var request = SC.Request.deleteUrl(url).async(YES).json(YES).header(Chililog.AUTHENTICATION_HEADER_NAME, authToken);
    var params = { documentID: documentID, callbackTarget: callbackTarget,
      callbackFunction: callbackFunction, callbackParams: callbackParams 
    };

    // For some reason, sc-server needs content otherwise Content-Length is not set to 0
    request.notify(this, 'endErase', params).send({dummy: 'data'});

    return;
  },

  /**
   * Callback from erase() after we get a response from the server to process
   * the returned info.
   *
   * @param {SC.Response} response The HTTP response
   * @param {Hash} params Hash of parameters passed into SC.Request.notify()
   * @returns {Boolean} YES if successful
   */
  endErase: function(response, params) {
    var error = null;
    try {
      // Check status
      this.checkResponse(response);

      var record = Chililog.store.find(Chililog.UserRecord, params.documentID);
      record.destroy();

      Chililog.store.commitRecords();
    }
    catch (err) {
      error = err;
      SC.Logger.error('userDataController.endDeleteRecord: ' + err);
    }

    // Callback
    if (!SC.none(params.callbackFunction)) {
      params.callbackFunction.call(params.callbackTarget, params.documentID, params.callbackParams, error);
    }

    // Return YES to signal handling of callback
    return YES;
  }

});
