// ==========================================================================
// Project:   Chililog.sessionController
// Copyright: ©2011 My Company, Inc.
// ==========================================================================

/**
 * Name of Authentication header returned in API responses
 */
Chililog.AUTHENTICATION_HEADER_NAME = 'X-ChiliLog-Authentication';

/**
 * Name of Authentication header returned in API responses in lower case
 */
Chililog.AUTHENTICATION_HEADER_NAME_LCASE = 'x-chiliLog-authentication';


/**
 * All tokens to expire in 14 days
 */
Chililog.AUTHENTICATION_TOKEN_EXPIRY_SECONDS = 60 * 60 * 24 * 14;

/**
 * Mixin for heaping with server API comms
 */
Chililog.ServerApiMixin = {

  /**
   * Checks the return information from the server. Throws error if
   *
   * @param SC.Response HTTP response
   * @return YES if successful
   */
  checkResponse: function(response) {
    var responseStatus = response.get('status');
    if (!SC.ok(response)) {
      SC.Logger.info('HTTP status code: ' + responseStatus);
      if (responseStatus === 500) {
        var responseJson = response.get('body');
        SC.Logger.error('HTTP response ' + SC.json.encode(responseJson));
        throw SC.Error.desc(responseJson.Message);
      }
      throw SC.Error.desc('Unexpected HTTP error ' + status);
    }

    return YES;
  }


};