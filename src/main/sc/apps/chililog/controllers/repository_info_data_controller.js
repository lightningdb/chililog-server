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
  }

});
