// ==========================================================================
// Project:   Chililog.UserRecord
// Copyright: ©2011 My Company, Inc.
// ==========================================================================
/*globals Chililog */

/** @class

  Authenticated User record containing user profile information that can be changed by the user

 @extends SC.Record
 @version 0.1
 */
Chililog.AuthenticatedUserRecord = SC.Record.extend(
/** @scope Chililog.UserRecord.prototype */ {

  primaryKey: Chililog.DOCUMENT_ID_RECORD_FIELD_NAME,

  documentID: SC.Record.attr(String),
  documentVersion: SC.Record.attr(Number),
  username: SC.Record.attr(String),
  emailAddress: SC.Record.attr(String),
  displayName: SC.Record.attr(String),
  role: SC.Record.attr(Array),
  gravatarMD5Hash: SC.Record.attr(String),

  /**
   * Maps server api data into this user record
   *
   * @param {Object} userAO
   */
  fromApiObject: function(userAO) {
    // If version has not changed, then there's nothing to update
    var recordVersion = this.get(Chililog.DOCUMENT_VERSION_RECORD_FIELD_NAME);
    var apiObjectVersion = userAO[Chililog.DOCUMENT_VERSION_AO_FIELD_NAME ];
    if (recordVersion === apiObjectVersion) {
      return;
    }

    for (var i = 0; i < Chililog.AUTHENTICATED_USER_RECORD_MAP.length; i++) {
      var map = Chililog.AUTHENTICATED_USER_RECORD_MAP[i];
      var recordPropertyName = map[0];
      var apiObjectPropertyName = map[1];
      this.set(recordPropertyName, userAO[apiObjectPropertyName]);
    }
  },

  /**
   * Maps user record data to api object
   *
   * @returns {Object} userAO
   */
  toApiObject: function() {
    var apiObject = new Object();
    for (var i = 0; i < Chililog.USER_RECORD_MAP.length; i++) {
      var map = Chililog.AUTHENTICATED_USER_RECORD_MAP[i];
      var recordPropertyName = map[0];
      var apiObjectPropertyName = map[1];
      apiObject[apiObjectPropertyName] = this.get(recordPropertyName);
    }
    return apiObject;
  }
});

/**
 * Maps Chililog.UserRecord property names to property names used by the server API objects
 */
Chililog.AUTHENTICATED_USER_RECORD_MAP = [
  [Chililog.DOCUMENT_ID_RECORD_FIELD_NAME, Chililog.DOCUMENT_ID_AO_FIELD_NAME ],
  [Chililog.DOCUMENT_VERSION_RECORD_FIELD_NAME, Chililog.DOCUMENT_VERSION_AO_FIELD_NAME],
  ['username', 'Username'],
  ['emailAddress', 'EmailAddress'],
  ['displayName', 'DisplayName'],
  ['roles', 'Roles'],
  ['gravatarMD5Hash', 'GravatarMD5Hash']
];
