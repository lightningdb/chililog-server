// ==========================================================================
// Project:   Chililog.UserRecord
// Copyright: ©2011 My Company, Inc.
// ==========================================================================

/** @class

  Repository record

 @extends SC.Record
 @version 0.1
 */
Chililog.RepositoryRecord = SC.Record.extend(
/** @scope Chililog.RepositoryRecord.prototype */ {

  primaryKey: Chililog.DOCUMENT_ID_RECORD_FIELD_NAME,

  documentID: SC.Record.attr(String),
  documentVersion: SC.Record.attr(Number),
  name: SC.Record.attr(String),
  status: SC.Record.attr(String),

  /**
   * Maps server api data into this record
   *
   * @param {Object} repoInfoAO
   */
  fromApiObject: function(repoInfoAO) {
    // If version has not changed, then there's nothing to update
    var recordVersion = this.get(Chililog.DOCUMENT_VERSION_RECORD_FIELD_NAME);
    var apiObjectVersion = repoInfoAO[Chililog.DOCUMENT_VERSION_AO_FIELD_NAME ];
    if (recordVersion === apiObjectVersion) {
      return;
    }

    for (var i = 0; i < Chililog.REPOSITORY_INFO_RECORD_MAP.length; i++) {
      var map = Chililog.REPOSITORY_INFO_RECORD_MAP[i];
      var recordPropertyName = map[0];
      var apiObjectPropertyName = map[1];
      this.set(recordPropertyName, repoInfoAO[apiObjectPropertyName]);
    }
  },

  /**
   * Maps record data to api object
   * 
   * @returns {Object} repoInfoAO
   */
  toApiObject: function() {
    var apiObject = new Object();
    for (var i = 0; i < Chililog.REPOSITORY_INFO_RECORD_MAP.length; i++) {
      var map = Chililog.REPOSITORY_INFO_RECORD_MAP[i];
      var recordPropertyName = map[0];
      var apiObjectPropertyName = map[1];
      apiObject[apiObjectPropertyName] = this.get(recordPropertyName);
    }
    return apiObject;
  },

  /**
   * Item display name in a tree view.  Just show the username
   */
  treeItemLabel: function() {
    return this.get('name');
  }.property('name').cacheable(),

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
Chililog.REPOSITORY_INFO_RECORD_MAP = [
  [Chililog.DOCUMENT_ID_RECORD_FIELD_NAME, Chililog.DOCUMENT_ID_AO_FIELD_NAME ],
  [Chililog.DOCUMENT_VERSION_RECORD_FIELD_NAME, Chililog.DOCUMENT_VERSION_AO_FIELD_NAME],
  ['name' ,'Name'],
  ['status' ,'Status']
];
