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
   * Synchronize data in the store with the data on the server
   *
   * @param {Object} [callbackTarget] Optional callback object
   * @param {Function} [callbackFunction] Optional callback function in the callback object. Signature is: function(error) {}.
   */
  synchronizeWithServer: function(callbackTarget, callbackFunction) {
    var authToken = Chililog.sessionDataController.get('authenticationToken');
    var isLoggedIn = Chililog.sessionDataController.get('isLoggedIn');

    var isBusy = this.get('isBusy');
    if (isBusy) {
      return;
    }

    // Not logged in, so cannot sync
    if (!isLoggedIn) {
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
          if (SC.none(userRecord)) {
            userRecord = Chililog.store.createRecord(Chililog.UserRecord, {}, userAO.DocumentID);
          }
          userRecord.fromApiObject(userAO);
        }
      }
    }
    catch (err) {
      error = err;
      SC.Logger.error('endSynchronizeWithServer: ' + err.message);
    }

    // Callback
    if (!SC.none(params.callbackFunction)) {
      params.callbackFunction.call(params.callbackTarget, error);
    }

    // Return YES to signal handling of callback
    return YES;
  }

});
