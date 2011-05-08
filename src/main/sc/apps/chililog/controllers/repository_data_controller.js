// ==========================================================================
// Project:   Chililog
// Copyright: ©2011 My Company, Inc.
// ==========================================================================

sc_require('controllers/server_api_mixin');

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
   * @param {Function} [callbackFunction] Optional callback function in the callback object. Signature is: function(error) {}.
   */
  synchronizeWithServer: function(clearLocalData, synchronizeRepositoryInfo, callbackTarget, callbackFunction) {
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
      callbackTarget: callbackTarget, callbackFunction: callbackFunction
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
        params.callbackFunction.call(params.callbackTarget, error);
      }
    } else {
      // Chain sync
      Chililog.repositoryInfoDataController.synchronizeWithServer(params.clearLocalData, params.callbackTarget, params.callbackFunction);
    }

    // Return YES to signal handling of callback
    return YES;
  }

});
