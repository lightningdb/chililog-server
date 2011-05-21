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
  timestampString: SC.Record.attr(String),
  timestamp: SC.Record.attr(SC.DateTime, { format: '%Y-%m-%dT%H:%M:%S.%s%Z' }),
  savedTimestamp: SC.Record.attr(SC.DateTime, { format:'%Y-%m-%dT%H:%M:%S.%s%Z' }),
  source: SC.Record.attr(String),
  host: SC.Record.attr(String),
  severity: SC.Record.attr(Number),
  severityText: SC.Record.attr(String),
  message: SC.Record.attr(String),
  keywords: SC.Record.attr(Array),
  fields: SC.Record.attr(Array),

  /**
   * Only need to map one way because we don't update entries
   *
   * @param {Object} repoEntryAO Entry API object returned from server
   * @param {Object} repositoryInfoDocumentID Repository to which this entry belong
   */
  fromApiObject: function(repoEntryAO, repositoryInfoDocumentID) {
    var d = new Date();
    var timezoneOffsetMinutes = d.getTimezoneOffset();

    for (var i = 0; i < Chililog.REPOSITORY_ENTRY_RECORD_MAP.length; i++) {
      var map = Chililog.REPOSITORY_ENTRY_RECORD_MAP[i];
      var recordPropertyName = map[0];
      var apiObjectPropertyName = map[1];

      var apiObjectValue = repoEntryAO[apiObjectPropertyName];
      if (recordPropertyName === 'timestamp' || recordPropertyName === 'savedTimestamp') {
        apiObjectValue = SC.DateTime.parse(apiObjectValue, '%Y-%m-%dT%H:%M:%S.%s%Z');
        apiObjectValue.set('timezone', timezoneOffsetMinutes);
      }
      this.set(recordPropertyName, apiObjectValue);
    }

    // Set fields
    var a = [];
    for (var propertyName in repoEntryAO) {
      if (!SC.empty(propertyName) && typeof(propertyName) === 'string' && propertyName.indexOf('fld_') === 0) {
        a.push({ name: propertyName.substr(4), value: repoEntryAO[propertyName]});
      }
    }
    this.set('fields', a);

    // Set the severity text. Prepare map if first time because have to wait for strings to load before we can localize
    if (Chililog.REPOSITORY_ENTRY_SEVERITY_MAP == null) {
      Chililog.REPOSITORY_ENTRY_SEVERITY_MAP = [
        '_repositoryEntryRecord.Severity.Emergency'.loc(),
        '_repositoryEntryRecord.Severity.Action'.loc(),
        '_repositoryEntryRecord.Severity.Critical'.loc(),
        '_repositoryEntryRecord.Severity.Error'.loc(),
        '_repositoryEntryRecord.Severity.Warning'.loc(),
        '_repositoryEntryRecord.Severity.Notice'.loc(),
        '_repositoryEntryRecord.Severity.Information'.loc(),
        '_repositoryEntryRecord.Severity.Debug'.loc(),
        '_repositoryEntryRecord.Severity.Emergency'.loc()
      ];
    }
    this.set('severityText', Chililog.REPOSITORY_ENTRY_SEVERITY_MAP[this.get('severity')]);

    // Set repository id
    this.set('repositoryInfoDocumentID', repositoryInfoDocumentID);
  }

});

/**
 * Maps Chililog.RepositoryEntry property names to property names used by the server API objects
 */
Chililog.REPOSITORY_ENTRY_RECORD_MAP = [
  [Chililog.DOCUMENT_ID_RECORD_FIELD_NAME, '_id' ],
  ['timestampString' ,'ts'],
  ['timestamp' ,'ts'],
  ['savedTimestamp' ,'saved_ts'],
  ['source' ,'source'],
  ['host' ,'host'],
  ['severity', 'severity'],
  ['message' ,'message'],
  ['keywords' ,'keywords']
];

/**
 * Map of severity text for code
 */
Chililog.REPOSITORY_ENTRY_SEVERITY_MAP = null;