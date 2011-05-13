// ==========================================================================
// Project:   Chililog.UserRecord
// Copyright: ©2011 My Company, Inc.
// ==========================================================================

/** @class

  Repository Information record

 @extends SC.Record
 @version 0.1
 */
Chililog.RepositoryInfoRecord = SC.Record.extend(
/** @scope Chililog.RepositoryInfoRecord.prototype */ {

  primaryKey: Chililog.DOCUMENT_ID_RECORD_FIELD_NAME,

  documentID: SC.Record.attr(String),
  documentVersion: SC.Record.attr(Number),
  name: SC.Record.attr(String, { defaultValue: '', isRequired: YES }),
  displayName: SC.Record.attr(String),
  description: SC.Record.attr(String),
  startupStatus: SC.Record.attr(String, { defaultValue: 'ONLINE', isRequired: YES }),

  readQueueDurable: SC.Record.attr(Boolean, { defaultValue: NO }),
  readQueuePassword: SC.Record.attr(String, { defaultValue: '' }),

  writeQueueDurable: SC.Record.attr(Boolean, { defaultValue: NO }),
  writeQueuePassword: SC.Record.attr(String),
  writeQueueWorkerCount: SC.Record.attr(Number),
  writeQueueMaxMemory: SC.Record.attr(Number),
  writeQueueMaxMemoryPolicy: SC.Record.attr(String, { defaultValue: 'DROP' }),
  writeQueuePageSize: SC.Record.attr(Number),
  writeQueuePageCountCache: SC.Record.attr(Number),

  maxKeywords: SC.Record.attr(Number),

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
    this.set('isOnline', s === 'ONLINE');
    this.set('currentStatus', s);

    var text = '_configureRepositoryInfoDetailView.Status.Offline'.loc();
    if (!SC.empty(s) && s === 'ONLINE') {
      text = '_configureRepositoryInfoDetailView.Status.Online'.loc();
    }
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

  ['readQueueDurable' ,'ReadQueueDurable'],
  ['readQueuePassword' ,'ReadQueuePassword'],

  ['writeQueueDurable' ,'WriteQueueDurable'],
  ['writeQueuePassword' ,'WriteQueuePassword'],
  ['writeQueueWorkerCount' ,'WriteQueueWorkerCount'],
  ['writeQueueMaxMemory' ,'WriteQueueMaxMemory'],
  ['writeQueueMaxMemoryPolicy' ,'WriteQueueMaxMemoryPolicy'],
  ['writeQueuePageSize' ,'WriteQueuePageSize'],
  ['writeQueuePageCountCache' ,'WriteQueuePageCountCache'],
  ['maxKeywords' ,'MaxKeywords']

];
