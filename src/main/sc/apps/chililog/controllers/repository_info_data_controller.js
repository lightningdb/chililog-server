// ==========================================================================
// Project:   Chililog
// Copyright: ©2011 My Company, Inc.
// ==========================================================================

sc_require('controllers/server_api_mixin');


/** @class

  Manages repository information records and keeps them in sync with the server

 @extends SC.Object
 */
Chililog.repositoryInfoDataController = SC.ObjectController.create(Chililog.ServerApiMixin,
/** @scope Chililog.userDataController.prototype */ {

  /**
   * YEs if we are performing a server synchronization
   * @type Boolean
   */
  isSynchronizingWithServer: NO,

  /**
   * Synchronize data in the store with the data on the server
   * We sync repository status after we get all the repository info
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
      var records = Chililog.store.find(Chililog.RepositoryInfoRecord);
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
    var url = '/api/repository_info';
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
      var repoInfoAOArray = response.get('body');
      if (!SC.none(repoInfoAOArray) && SC.isArray(repoInfoAOArray)) {
        for (var i = 0; i < repoInfoAOArray.length; i++) {
          var repoInfoAO = repoInfoAOArray[i];

          // See if record exists
          var repoInfoRecord = Chililog.store.find(Chililog.RepositoryInfoRecord, repoInfoAO.DocumentID);
          if (SC.none(repoInfoRecord) || (repoInfoRecord.get('status') & SC.Record.DESTROYED)) {
            repoInfoRecord = Chililog.store.createRecord(Chililog.RepositoryInfoRecord, {}, repoInfoAO.DocumentID);
          }

          // Find corresponding repository record
          var query = SC.Query.local(Chililog.RepositoryRecord, 'name={name}', {name: repoInfoAO.Name});
          var repoRecord = Chililog.store.find(query);
          if (repoRecord.get('length') > 0) {
            repoInfoRecord.set('repository', repoRecord.objectAt(0));
          }
          repoInfoRecord.fromApiObject(repoInfoAO);
        }
        Chililog.store.commitRecords();
      }

      // Delete records that have not been returned
      var records = Chililog.store.find(Chililog.RepositoryInfoRecord);
      records.forEach(function(record) {
        var doDelete = YES;
        if (!SC.none(repoInfoAOArray) && SC.isArray(repoInfoAOArray)) {
          for (var i = 0; i < repoInfoAOArray.length; i++) {
            var repoInfoAO = repoInfoAOArray[i];
            if (repoInfoAO[Chililog.DOCUMENT_ID_AO_FIELD_NAME] === record.get(Chililog.DOCUMENT_ID_RECORD_FIELD_NAME)) {
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
      SC.Logger.error('repositoryInfoDataController.endSynchronizeWithServer: ' + err.message);
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
    var record = nestedStore.create(Chililog.RepositoryInfoRecord, {});
    record.set(Chililog.DOCUMENT_VERSION_RECORD_FIELD_NAME,  0);
    return record;
  },

  /**
   * Returns an existing the user record for editing
   *
   * @param {String} documentID Document ID of the user record to edit
   * @returns {Chililog.UserRecord}
   */
  editRecord: function(documentID) {
    var nestedStore = Chililog.store.chain();
    var record = nestedStore.find(Chililog.RepositoryInfoRecord, documentID);
    return record;
  },

  /**
   * Saves the user record to the server
   * @param {Chililog.RepositoryInfoRecord} record record to save
   * @param {Object} [callbackTarget] Optional callback object
   * @param {Function} [callbackFunction] Optional callback function in the callback object. Signature is: function(error) {}.
   */
  saveRecord: function(record, callbackTarget, callbackFunction) {
    // Get our data from the properties using the SC 'get' methods
    // Need to do this because these properties have been bound/observed.
    if (SC.empty(record.get('name'))) {
      throw Chililog.$error('_repositoryInfoDataController.NameRequiredError', null, 'username');
    }

    var data = record.toApiObject();
    var authToken = this.get('authenticationToken');

    var documentVersion = record.get(Chililog.DOCUMENT_VERSION_RECORD_FIELD_NAME);
    var isAdding = (!SC.none(documentVersion) && documentVersion >  0);
    var request;
    if (isAdding) {
      var url = '/api/repository_info/';
      request = SC.Request.postUrl(url).async(YES).json(YES).header(Chililog.AUTHENTICATION_HEADER_NAME, authToken);
    } else {
      var url = '/api/repository_info/' + record.get(Chililog.DOCUMENT_ID_RECORD_FIELD_NAME);
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
      var apiObject = response.get('body');

      var record = null;
      if (params.isAdding) {
        record = Chililog.store.createRecord(Chililog.RepositoryInfoRecord, {}, apiObject[Chililog.DOCUMENT_ID_AO_FIELD_NAME]);
      } else {
        record = Chililog.store.find(Chililog.RepositoryInfoRecord, apiObject[Chililog.DOCUMENT_ID_AO_FIELD_NAME]);
      }
      record.fromApiObject(apiObject);
      Chililog.store.commitRecords();
    }
    catch (err) {
      error = err;
      SC.Logger.error('repositoryInfoDataController.endSaveRecord: ' + err);
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
   * @param {Chililog.RepositoryInfoRecord} record record to discard
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
   * @param {Function} [callbackFunction] Optional callback function in the callback object. Signature is: function(error) {}.
   */
  deleteRecord: function(documentID, callbackTarget, callbackFunction) {
    var authToken = this.get('authenticationToken');

    var url = '/api/repository_info/' + documentID;
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

      var record = Chililog.store.find(Chililog.RepositoryInfoRecord, params.documentID);
      record.destroy();

      Chililog.store.commitRecords();
    }
    catch (err) {
      error = err;
      SC.Logger.error('repositoryInfoDataController.endDeleteRecord: ' + err);
    }

    // Callback
    if (!SC.none(params.callbackFunction)) {
      params.callbackFunction.call(params.callbackTarget, error);
    }

    // Return YES to signal handling of callback
    return YES;
  }



});
