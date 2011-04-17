// ==========================================================================
// Project:   Chililog
// Copyright: ©2011 My Company, Inc.
// ==========================================================================

/**
 * Name of Authentication header returned in API responses
 */
Chililog.AUTHENTICATION_HEADER_NAME = 'X-ChiliLog-Authentication';

/**
 * Name of Authentication header returned in API responses in lower case
 */
Chililog.AUTHENTICATION_HEADER_NAME_LCASE = 'x-chililog-authentication';

/**
 * All tokens to expire in 14 days
 */
Chililog.AUTHENTICATION_TOKEN_EXPIRY_SECONDS = 60 * 60 * 24 * 14;

/**
 * Name of the primary key field in Server API objects
 */
Chililog.DOCUMENT_ID_AO_FIELD_NAME = 'DocumentID';

/**
 * Name of the primary key field our SC.Records
 */
Chililog.DOCUMENT_ID_RECORD_FIELD_NAME = 'documentID';

/**
 * Name of the version field in Server API objects
 */
Chililog.DOCUMENT_VERSION_AO_FIELD_NAME = 'DocumentVersion';

/**
 * Name of the version field in our SC.Records
 */
Chililog.DOCUMENT_VERSION_RECORD_FIELD_NAME = 'documentVersion';


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