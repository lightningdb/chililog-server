// ==========================================================================
// Project:   Chililog
// Copyright: ©2011 My Company, Inc.
// ==========================================================================


/** @class

  Manages user records and keeps them in sync with the server

 @extends SC.Object
 */
Chililog.userDataController = SC.ObjectController.create(Chililog.ServerApiMixin,
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
   * @param {Function} [callbackFunction] Optional callback function in the callback object. Signature is: function(error) {}.
   */
  synchronizeWithServer: function(clearLocalData, callbackTarget, callbackFunction) {
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
    var params = { callbackTarget: callbackTarget, callbackFunction: callbackFunction };
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
      params.callbackFunction.call(params.callbackTarget, error);
    }

    // Return YES to signal handling of callback
    return YES;
  },

  /**
   * Returns a new user record for editing
   *
   * @returns {Chililog.UserRecord}
   */
  createRecord: function(documentID) {
    var nestedStore = Chililog.store.chain();
    userRecord = nestedStore.create(Chililog.UserRecord, {});
    userRecord.set(Chililog.DOCUMENT_VERSION_RECORD_FIELD_NAME,  0);
    return userRecord;
  },

  /**
   * Returns an existing the user record for editing
   *
   * @param {String} documentID Document ID of the user record to edit
   * @returns {Chililog.UserRecord}
   */
  editRecord: function(documentID) {
    var nestedStore = Chililog.store.chain();
    userRecord = nestedStore.find(Chililog.UserRecord, documentID);
    return userRecord;
  },

  /**
   * Saves the user record to the server
   * @param {Chililog.UserRecord} userRecord record to save
   * @param {Object} [callbackTarget] Optional callback object
   * @param {Function} [callbackFunction] Optional callback function in the callback object. Signature is: function(error) {}.
   */
  saveRecord: function(userRecord, callbackTarget, callbackFunction) {
    // Get our data from the properties using the SC 'get' methods
    // Need to do this because these properties have been bound/observed.
    if (SC.empty(userRecord.get('username'))) {
      throw Chililog.$error('_sessionDataController.UsernameRequiredError', null, 'username');
    }

    if (SC.empty(userRecord.get('emailAddress'))) {
      throw Chililog.$error('_sessionDataController.EmailAddressRequiredError', null, 'emailAddress');
    }

    if (SC.empty(userRecord.get('displayName'))) {
      throw Chililog.$error('_sessionDataController.DisplayNameRequiredError', null, 'displayName');
    }

    var data = userRecord.toApiObject();
    var authToken = this.get('authenticationToken');

    var documentVersion = userRecord.get(Chililog.DOCUMENT_VERSION_RECORD_FIELD_NAME);
    var isAdding = (!SC.none(documentVersion) && documentVersion >  0);
    var request;
    if (isAdding) {
      var url = '/api/users/';
      request = SC.Request.postUrl(url).async(YES).json(YES).header(Chililog.AUTHENTICATION_HEADER_NAME, authToken);
    } else {
      var url = '/api/users/' + userRecord.get(Chililog.DOCUMENT_ID_RECORD_FIELD_NAME);
      request = SC.Request.putUrl(url).async(YES).json(YES).header(Chililog.AUTHENTICATION_HEADER_NAME, authToken);
    }
    var params = { isAdding: isAdding, callbackTarget: callbackTarget, callbackFunction: callbackFunction };
    request.notify(this, 'endSaveRecord', params).send(data);

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
  endSaveRecord: function(response, params) {
    var error = null;
    try {
      // Check status
      this.checkResponse(response);

      // Save new authenticated user details
      var userAO = response.get('body');

      var userRecord = null;
      if (param.isAdding) {
        userRecord = Chililog.store.createRecord(Chililog.UserRecord, {}, userAO[Chililog.DOCUMENT_ID_AO_FIELD_NAME]);
      } else {
        userRecord = Chililog.store.find(Chililog.UserRecord, userAO[Chililog.DOCUMENT_ID_AO_FIELD_NAME]);
      }
      userRecord.fromApiObject(userAO);
      Chililog.store.commitRecords();
    }
    catch (err) {
      error = err;
      SC.Logger.error('userDataController.endSaveRecord: ' + err);
    }

    // Callback
    if (!SC.none(params.callbackFunction)) {
      params.callbackFunction.call(params.callbackTarget, error);
    }

    // Return YES to signal handling of callback
    return YES;
  },

  /**
   * Discard changes
   * @param userRecord record to discard
   */
  discardChanges: function(userRecord) {
    if (!SC.none(userRecord)) {
      var nestedStore = userRecord.get('store');
      nestedStore.destroy();
    }
    return;
  },

  /**
   * Deletes the user record on the server
   *
   * @param {String} documentID id of record to delete
   * @param {Object} [callbackTarget] Optional callback object
   * @param {Function} [callbackFunction] Optional callback function in the callback object. Signature is: function(error) {}.
   */
  deleteRecord: function(documentID, callbackTarget, callbackFunction) {
    var authToken = this.get('authenticationToken');

    var url = '/api/users/' + documentID;
    var request = SC.Request.deleteUrl(url).async(YES).json(YES).header(Chililog.AUTHENTICATION_HEADER_NAME, authToken);
    var params = { documentID: documentID, callbackTarget: callbackTarget, callbackFunction: callbackFunction };
    request.notify(this, 'endDeleteRecord', params).send(data);

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
  endDeleteRecord: function(response, params) {
    var error = null;
    try {
      // Check status
      this.checkResponse(response);

      var userRecord = Chililog.store.find(Chililog.UserRecord, params.documentID);
      userRecord.destroy();

      Chililog.store.commitRecords();
    }
    catch (err) {
      error = err;
      SC.Logger.error('userDataController.endDeleteRecord: ' + err);
    }

    // Callback
    if (!SC.none(params.callbackFunction)) {
      params.callbackFunction.call(params.callbackTarget, error);
    }

    // Return YES to signal handling of callback
    return YES;
  }



});
