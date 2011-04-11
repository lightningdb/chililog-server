// ==========================================================================
// Project:   Chililog
// Copyright: ©2011 My Company, Inc.
// ==========================================================================

/**
 * Maps Chililog.UserRecord property names to property names used by the server
 */
Chililog.userDataMap = [
  ['username' ,'Username'],
  ['emailAddress' ,'EmailAddress'],
  ['password' ,'Password'],
  ['roles' ,'Roles'],
  ['status' ,'Status'],
  ['displayName' ,'DisplayName'],
  ['gravatarMD5Hash' ,'GravatarMD5Hash']
];


/** @class

  Manages user records and keeps them in sync with the server

 @extends SC.Object
 */
Chililog.userDataController = SC.ObjectController.create(Chililog.ServerApiMixin,
/** @scope Chililog.userDataController.prototype */ {

  /**
   * Error returned from an asynchronous call
   *
   * @type SC.Error
   */
  error: '',

  /**
   * Synchronize data in the store with the data on the server
   *
   * @param {Object} [callbackTarget] Optional callback object
   * @param {Function} [callbackFunction] Optional callback function in the callback object
   */
  synchronizeWithServer: function(callbackTarget, callbackFunction) {
    var authToken = Chililog.sessionDataController.get('authenticationToken');
    var loggedInUser = Chililog.sessionDataController.get('loggedInUser');

    var isBusy = this.get('isBusy');
    if (isBusy) {
      return;
    }

    // Not logged in, so cannot sync
    if (SC.none(loggedInUser)) {
      return;
    }

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
    try {
      // Check status
      this.checkResponse(response);

      // Set data
      var users = response.get('body');
      if (!SC.none(users) && SC.isArray(users)) {
        for (var i = 0; i < users.length; i++) {
          var user = users[i];

          // See if user record exists
          var userRecord = Chililog.store.find(Chililog.UserRecord, user.DocumentID);
          if (SC.none(userRecord)) {
            userRecord = Chililog.store.createRecord(Chililog.UserRecord, {}, user.DocumentID);
          }
          this.serverDataToRecord(user, userRecord);
        }
      }

      // Clear error data
      this.set('error', null);
    }
    catch (err) {
      this.set('error', err);
      SC.Logger.info('Error in endSynchronizeWithServer: ' + err.message);
    }

    // Callback
    if (!SC.none(params.callbackTarget) && !SC.none(params.callbackFunction)) {
      params.callbackFunction.call(params.callbackTarget, null);
    }

    // Return YES to signal handling of callback
    return YES;
  },

  /**
   * Maps server data to a user record
   * @param {Object} serverData
   * @param {Chililog.UserRecord} record
   */
  serverDataToRecord: function(serverData, record) {
    // If version has not changed, then there's nothing to update
    var recordVersion = record.get('documentVersion');
    var serverDataVersion = serverData.DocumentVersion;
    if (recordVersion === serverDataVersion) {
      return;
    }

    record.set('documentVersion', serverDataVersion);
    for (var i = 0; i < Chililog.userDataMap.length; i++) {
      var map = Chililog.userDataMap[i];
      var recordPropertyName = map[0];
      var serverDataPropertyName = map[1];
      record.set(recordPropertyName, serverData[serverDataPropertyName]);
    }
  },

  /**
   * Maps user record data to server data
   * @param {Chililog.UserRecord} record
   * @param {Object} serverData
   */
  recordToServerData: function(record, serverData) {
    for (var i = 0; i < Chililog.userDataMap.length; i++) {
      var map = Chililog.userDataMap[i];
      var recordPropertyName = map[0];
      var serverDataPropertyName = map[1];
      serverData[serverDataPropertyName] = record.get(recordPropertyName);
    }
  }

});
