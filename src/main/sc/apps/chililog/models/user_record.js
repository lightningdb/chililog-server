// ==========================================================================
// Project:   Chililog.UserRecord
// Copyright: ©2011 My Company, Inc.
// ==========================================================================
/*globals Chililog */

/** @class

  User record

 @extends SC.Record
 @version 0.1
 */
Chililog.UserRecord = SC.Record.extend(
/** @scope Chililog.UserRecord.prototype */ {

  primaryKey: Chililog.DOCUMENT_ID_RECORD_FIELD_NAME,

  documentID: SC.Record.attr(String),
  documentVersion: SC.Record.attr(Number),
  username: SC.Record.attr(String),
  emailAddress: SC.Record.attr(String),
  password: SC.Record.attr(String),
  roles: SC.Record.attr(Array),
  status: SC.Record.attr(String),
  displayName: SC.Record.attr(String),
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

    for (var i = 0; i < Chililog.USER_RECORD_MAP.length; i++) {
      var map = Chililog.USER_RECORD_MAP[i];
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
      var map = Chililog.USER_RECORD_MAP[i];
      var recordPropertyName = map[0];
      var apiObjectPropertyName = map[1];
      apiObject[apiObjectPropertyName] = this.get(recordPropertyName);
    }
    return apiObject;
  },

  /**
   * Item display name in a tree view.  Just show the username
   */
  treeItemName: function() {
    return this.get('username');
  }.property('username').cacheable(),

  /**
   * Because there are no child items, there is no need to expand in a tree view
   */
  treeItemIsExpanded: NO,

  /**
   * There are no child items for display in a tree view under users
   */
  treeItemChildren: function(){
     return null;
  }.property().cacheable()
   
});

/**
 * Maps Chililog.UserRecord property names to property names used by the server API objects
 */
Chililog.USER_RECORD_MAP = [
  [Chililog.DOCUMENT_ID_RECORD_FIELD_NAME, Chililog.DOCUMENT_ID_AO_FIELD_NAME ],
  [Chililog.DOCUMENT_VERSION_RECORD_FIELD_NAME, Chililog.DOCUMENT_VERSION_AO_FIELD_NAME],
  ['username' ,'Username'],
  ['emailAddress' ,'EmailAddress'],
  ['password' ,'Password'],
  ['roles' ,'Roles'],
  ['status' ,'Status'],
  ['displayName' ,'DisplayName'],
  ['gravatarMD5Hash' ,'GravatarMD5Hash']
];
