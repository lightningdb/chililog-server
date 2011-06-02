// ==========================================================================
// Project:   Chililog
// Copyright: ©2011 My Company, Inc.
// ==========================================================================

/**
 * Mixin for heaping with server API comms
 */
Chililog.DataControllerMixin = {

  /**
   * Checks the return information from the server. Throws error if
   *
   * @param SC.Response HTTP response
   * @return YES if successful
   */
  checkResponse: function(response) {
    var responseStatus = response.get('status');
    if (!SC.ok(response)) {
      SC.Logger.error('HTTP error status code: ' + responseStatus);
      if (responseStatus === 500 || responseStatus === 400 || responseStatus === 401) {
        var responseString = response.get('encodedBody');
        SC.Logger.error('HTTP response ' + responseString);

        if (!SC.empty(responseString) && response.get('isJSON') && responseString.charAt(0) === '{') {
          var responseJson = response.get('body');
          throw SC.Error.desc(responseJson.Message);
        } else {
          throw SC.Error.desc('Error connecting to server.');
        }
      }
      throw SC.Error.desc('Unexpected HTTP error ' + status);
    }

    return YES;
  }

};