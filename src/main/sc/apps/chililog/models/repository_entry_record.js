// ==========================================================================
// Project:   Chililog.UserRecord
// Copyright: ©2011 My Company, Inc.
// ==========================================================================

/** @class

  Repository Entry record

 @extends SC.Record
 @version 0.1
 */
Chililog.RepositoryEntryRecord = SC.Record.extend(
/** @scope Chililog.RepositoryEntryRecord.prototype */ {

  primaryKey: Chililog.DOCUMENT_ID_RECORD_FIELD_NAME,

  documentID: SC.Record.attr(String),
  repositoryInfoDocumentID: SC.Record.attr(String),
  timestamp: SC.Record.attr(SC.DateTime, { format: SC.DATETIME_ISO8601 }),
  savedTimestamp: SC.Record.attr(SC.DateTime, { format: SC.DATETIME_ISO8601 }),
  source: SC.Record.attr(String),
  host: SC.Record.attr(String),
  messages: SC.Record.attr(String),
  keywords: SC.Record.attr(Array),

  /**
   * Only need to map one way because we don't update entries
   *
   * @param {Object} repoEntryAO Entry API object returned from server
   * @param {Object} repositoryInfoDocumentID Repository to which this entry belong
   */
  fromApiObject: function(repoEntryAO, repositoryInfoDocumentID) {
    for (var i = 0; i < Chililog.REPOSITORY_INFO_RECORD_MAP.length; i++) {
      var map = Chililog.REPOSITORY_INFO_RECORD_MAP[i];
      var recordPropertyName = map[0];
      var apiObjectPropertyName = map[1];
      this.set(recordPropertyName, repoEntryAO[apiObjectPropertyName]);
    }
    this.set('repositoryInfoDocumentID', repositoryInfoDocumentID);
  }

});

/**
 * Maps Chililog.UserRecord property names to property names used by the server API objects
 */
Chililog.REPOSITORY_INFO_RECORD_MAP = [
  [Chililog.DOCUMENT_ID_RECORD_FIELD_NAME, '_id' ],
  ['timestamp' ,'c_ts'],
  ['savedTimestamp' ,'c_saved_ts'],
  ['source' ,'c_source'],
  ['host' ,'c_host'],
  ['severity', 'c_severity'],
  ['messages' ,'c_message'],
  ['keywords' ,'c_keywords']
];
