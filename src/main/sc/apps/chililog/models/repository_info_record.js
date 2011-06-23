// ==========================================================================
// Project:   Chililog.UserRecord
// Copyright: ©2011 My Company, Inc.
// ==========================================================================

/** @class

  Repository Information record

 @extends SC.Record
 @version 0.1
 */
Chililog.RepositoryInfoRecord = SC.Record.extend({

  primaryKey: Chililog.DOCUMENT_ID_RECORD_FIELD_NAME,

  documentID: SC.Record.attr(String),
  documentVersion: SC.Record.attr(Number),
  name: SC.Record.attr(String, { defaultValue: '', isRequired: YES }),
  displayName: SC.Record.attr(String),
  description: SC.Record.attr(String),
  startupStatus: SC.Record.attr(String, { defaultValue: Chililog.REPOSITORY_ONLINE, isRequired: YES }),

  storeEntriesIndicator: SC.Record.attr(Boolean, { defaultValue: NO }),
  storageQueueDurableIndicator: SC.Record.attr(Boolean, { defaultValue: NO }),
  storageQueueWorkerCount: SC.Record.attr(Number),
  storageMaxKeywords: SC.Record.attr(Number),

  maxMemory: SC.Record.attr(Number),
  maxMemoryPolicy: SC.Record.attr(String, { defaultValue: Chililog.REPOSITORY_MAX_MEMORY_POLICY_DROP }),
  pageSize: SC.Record.attr(Number),
  pageCountCache: SC.Record.attr(Number),

  displayNameOrName: function() {
    var displayName = this.get('displayName');
    if (SC.none(displayName)) {
      displayName = this.get('name');
    }
    return displayName;
  }.property('name', 'displayName').cacheable(),

  /**
   * Flag to indicate if this repository is online or not
   * This is set during the SYNC and is not sent to the server for saving.
   * @type boolean
   */
  isOnline: SC.Record.attr(Boolean),

  /**
   * Current status code: ONLINE or OFFLINE
   * This is set during the SYNC and is not sent to the server for saving.
   * @type String
   */
  currentStatus: SC.Record.attr(String),

  /**
   * Localized text description of the status
   * This is set during the SYNC and is not sent to the server for saving.
   * @type String
   */
  currentStatusText: SC.Record.attr(String),

  /**
   * Function called to update the status using the repository record returned from the server
   * @param {Chililog.RepositoryRecord} repositoryRecord
   */
  updateStatus: function(repositoryRecord) {
    var s = repositoryRecord.get('currentStatus');
    var isOnline = (!SC.empty(s) && s === Chililog.REPOSITORY_ONLINE);
    this.set('isOnline', isOnline);
    this.set('currentStatus', s);

    var text = isOnline ? '_configureRepositoryInfoDetailView.Status.Online'.loc() :
      '_configureRepositoryInfoDetailView.Status.Offline'.loc();
    this.set('currentStatusText', text);
  },

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
  }

});

/**
 * Maps Chililog.UserRecord property names to property names used by the server API objects
 */
Chililog.REPOSITORY_INFO_RECORD_MAP = [
  [Chililog.DOCUMENT_ID_RECORD_FIELD_NAME, Chililog.DOCUMENT_ID_AO_FIELD_NAME ],
  [Chililog.DOCUMENT_VERSION_RECORD_FIELD_NAME, Chililog.DOCUMENT_VERSION_AO_FIELD_NAME],
  ['name' ,'Name'],
  ['displayName' ,'DisplayName'],
  ['description' ,'Description'],
  ['startupStatus' ,'StartupStatus'],

  ['storeEntriesIndicator' ,'StoreEntriesIndicator'],
  ['storageQueueDurableIndicator' ,'StorageQueueDurableIndicator'],
  ['storageQueueWorkerCount' ,'StorageQueueWorkerCount'],
  ['storageMaxKeywords' ,'StorageMaxKeywords'],

  ['maxMemory' ,'MaxMemory'],
  ['maxMemoryPolicy' ,'MaxMemoryPolicy'],
  ['pageSize' ,'PageSize'],
  ['pageCountCache' ,'PageCountCache']
];
