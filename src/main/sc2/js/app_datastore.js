//
// Copyright 2010 Cinch Logic Pty Ltd.
//
// http://www.App.com
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//

//
// Sets up our records and the data store
//

/**
 * Name of the primary key field in Server API objects
 */
App.DOCUMENT_ID_AO_FIELD_NAME = 'DocumentID';

/**
 * Name of the primary key field our SC.Records
 */
App.DOCUMENT_ID_RECORD_FIELD_NAME = 'documentID';

/**
 * Name of the version field in Server API objects
 */
App.DOCUMENT_VERSION_AO_FIELD_NAME = 'DocumentVersion';

/**
 * Name of the version field in our SC.Records
 */
App.DOCUMENT_VERSION_RECORD_FIELD_NAME = 'documentVersion';

/**
 * User role representing a system administrator
 */
App.SYSTEM_ADMINISTRATOR_ROLE = 'system.administrator';

/**
 * User role representing a repository administrator
 */
App.REPOSITORY_ADMINISTRATOR_ROLE = 'administrator';

/**
 * User role representing a repository power user
 */
App.REPOSITORY_WORKBENCH_ROLE = 'workbench';

/**
 * User role representing a repository standard user
 */
App.REPOSITORY_PUBLISHER_ROLE = 'publisher';

/**
 * User role representing a repository standard user
 */
App.REPOSITORY_SUBSCRIBER_ROLE = 'subscriber';

/**
 * Code for the status of an online repository; i.e. one that is started and can process log entries
 */
App.REPOSITORY_ONLINE = 'ONLINE';

/**
 * Code for the status of an offline repository; i.e. one that is stopped and cannot process log entries
 */
App.REPOSITORY_OFFLINE = 'OFFLINE';

/**
 * If a repository reaches max memory, drop new messages
 */
App.REPOSITORY_MAX_MEMORY_POLICY_DROP = 'DROP';

/**
 * If a repository reaches max memory, page new messages to file
 */
App.REPOSITORY_MAX_MEMORY_POLICY_PAGE = 'PAGE';

/**
 * If a repository reaches max memory, make producers block (wait) until memory reduces.
 */
App.REPOSITORY_MAX_MEMORY_POLICY_BLOCK = 'BLOCK';


/** @class
 *
 * Authenticated User record containing user profile information that can be changed by the user
 *
 * @extends SC.Record
 */
App.AuthenticatedUserRecord = SC.Record.extend({

  primaryKey: App.DOCUMENT_ID_RECORD_FIELD_NAME,

  documentID: SC.Record.attr(String),
  documentVersion: SC.Record.attr(Number),
  username: SC.Record.attr(String),
  emailAddress: SC.Record.attr(String),
  displayName: SC.Record.attr(String),
  role: SC.Record.attr(),
  gravatarMD5Hash: SC.Record.attr(String),

  displayNameOrUsername: function() {
    var displayName = this.get('displayName');
    if (SC.none(displayName)) {
      displayName = this.get('username');
    }
    return displayName;
  }.property('username', 'displayName').cacheable(),
  
  /**
   * Maps server api data into this user record
   *
   * @param {Object} userAO
   */
  fromApiObject: function(userAO) {
    // If version has not changed, then there's nothing to update
    var recordVersion = this.get(App.DOCUMENT_VERSION_RECORD_FIELD_NAME);
    var apiObjectVersion = userAO[App.DOCUMENT_VERSION_AO_FIELD_NAME ];
    if (recordVersion === apiObjectVersion) {
      return;
    }

    for (var i = 0; i < App.AUTHENTICATED_USER_RECORD_MAP.length; i++) {
      var map = App.AUTHENTICATED_USER_RECORD_MAP[i];
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
    for (var i = 0; i < App.AUTHENTICATED_USER_RECORD_MAP.length; i++) {
      var map = App.AUTHENTICATED_USER_RECORD_MAP[i];
      var recordPropertyName = map[0];
      var apiObjectPropertyName = map[1];
      apiObject[apiObjectPropertyName] = this.get(recordPropertyName);
    }
    return apiObject;
  }
});

/**
 * Maps App.AuthenticatedUserRecord property names to property names used by the server API objects
 */
App.AUTHENTICATED_USER_RECORD_MAP = [
  [App.DOCUMENT_ID_RECORD_FIELD_NAME, App.DOCUMENT_ID_AO_FIELD_NAME ],
  [App.DOCUMENT_VERSION_RECORD_FIELD_NAME, App.DOCUMENT_VERSION_AO_FIELD_NAME],
  ['username', 'Username'],
  ['emailAddress', 'EmailAddress'],
  ['displayName', 'DisplayName'],
  ['roles', 'Roles'],
  ['gravatarMD5Hash', 'GravatarMD5Hash']
];

/**
 * Map of severity text for code
 */
App.REPOSITORY_ENTRY_SEVERITY_MAP = [
  '_repositoryEntryRecord.Severity.Emergency'.loc(),
  '_repositoryEntryRecord.Severity.Action'.loc(),
  '_repositoryEntryRecord.Severity.Critical'.loc(),
  '_repositoryEntryRecord.Severity.Error'.loc(),
  '_repositoryEntryRecord.Severity.Warning'.loc(),
  '_repositoryEntryRecord.Severity.Notice'.loc(),
  '_repositoryEntryRecord.Severity.Information'.loc(),
  '_repositoryEntryRecord.Severity.Debug'.loc()
];

/** @class
 *
 * Repository Entry record contains information about a log entry
 *
 * @extends SC.Record
 */
App.RepositoryEntryRecord = SC.Record.extend({

  primaryKey: App.DOCUMENT_ID_RECORD_FIELD_NAME,

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
  messageWithKeywordsHilighted: SC.Record.attr(String),
  keywords: SC.Record.attr(Array),
  fields: SC.Record.attr(Array),

  localTimestampString: function() {
    return this.get('timestamp').toFormattedString('%Y-%m-%d %H:%M:%S.%s %Z');
  }.property('timestamp'),

  /**
   * Only need to map one way because we don't update entries
   *
   * @param {Object} repoEntryAO Entry API object returned from server
   * @param {Object} repositoryInfoDocumentID Repository to which this entry belong
   * @param {Array} keywordsRegexArray Array of keywords regular expression to match so that we can highlight them
   */
  fromApiObject: function(repoEntryAO, repositoryInfoDocumentID, keywordsRegexArray) {
    var d = new Date();
    var timezoneOffsetMinutes = d.getTimezoneOffset();

    for (var i = 0; i < App.REPOSITORY_ENTRY_RECORD_MAP.length; i++) {
      var map = App.REPOSITORY_ENTRY_RECORD_MAP[i];
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
    this.set('severityText', App.REPOSITORY_ENTRY_SEVERITY_MAP[this.get('severity')]);

    // Set repository id
    this.set('repositoryInfoDocumentID', repositoryInfoDocumentID);

    // Highlight keywords
    var msg = repoEntryAO['message'];
    if (SC.empty(msg)) {
      this.set('messageWithKeywordsHilighted', '');
    } else {
      //Clone to protect original
      msg = new String(msg);

      // Replace keywords to tokens
      for (var i = 0; i < keywordsRegexArray.length; i++) {
        var keywordsRegex = keywordsRegexArray[i];
        msg = msg.replace(keywordsRegex, '~~~Chililog~~~$1###Chililog###');
      }

      // Markup and then replace tokens with tags (so that injected tags don't get marked up)
      var highlightedMsg = msg; //SC.RenderContext.escapeHTML(msg);

      if (keywordsRegexArray.length > 0) {
        highlightedMsg = highlightedMsg.replace(/~~~Chililog~~~/g, '<span class="keyword">');
        highlightedMsg = highlightedMsg.replace(/###Chililog###/g, '</span>');
      }

      this.set('messageWithKeywordsHilighted', highlightedMsg);
    }
  }

});

/**
 * Maps App.RepositoryEntry property names to property names used by the server API objects
 */
App.REPOSITORY_ENTRY_RECORD_MAP = [
  [App.DOCUMENT_ID_RECORD_FIELD_NAME, '_id' ],
  ['timestampString' ,'ts'],
  ['timestamp' ,'ts'],
  ['savedTimestamp' ,'saved_ts'],
  ['source' ,'source'],
  ['host' ,'host'],
  ['severity', 'severity'],
  ['message' ,'message'],
  ['keywords' ,'keywords']
];


/** @class
 *
 * Repository Meta Information record contains information that describes the repository and the parameters
 * under which it will operate.
 *
 * @extends SC.Record
 */
App.RepositoryMetaInfoRecord = SC.Record.extend({

  primaryKey: App.DOCUMENT_ID_RECORD_FIELD_NAME,

  documentID: SC.Record.attr(String),
  documentVersion: SC.Record.attr(Number),
  name: SC.Record.attr(String, { defaultValue: '', isRequired: YES }),
  displayName: SC.Record.attr(String),
  description: SC.Record.attr(String),
  startupStatus: SC.Record.attr(String, { defaultValue: App.REPOSITORY_ONLINE, isRequired: YES }),

  storeEntriesIndicator: SC.Record.attr(Boolean, { defaultValue: NO }),
  storageQueueDurableIndicator: SC.Record.attr(Boolean, { defaultValue: NO }),
  storageQueueWorkerCount: SC.Record.attr(Number),
  storageMaxKeywords: SC.Record.attr(Number),

  maxMemory: SC.Record.attr(Number),
  maxMemoryPolicy: SC.Record.attr(String, { defaultValue: App.REPOSITORY_MAX_MEMORY_POLICY_DROP }),
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
   * @param {App.RepositoryRecord} repositoryRecord
   */
  updateStatus: function(repositoryRecord) {
    var s = repositoryRecord.get('currentStatus');
    var isOnline = (!SC.empty(s) && s === App.REPOSITORY_ONLINE);
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
    var recordVersion = this.get(App.DOCUMENT_VERSION_RECORD_FIELD_NAME);
    var apiObjectVersion = repoInfoAO[App.DOCUMENT_VERSION_AO_FIELD_NAME ];
    if (recordVersion === apiObjectVersion) {
      return;
    }

    for (var i = 0; i < App.REPOSITORY_META_INFO_RECORD_MAP.length; i++) {
      var map = App.REPOSITORY_META_INFO_RECORD_MAP[i];
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
    for (var i = 0; i < App.REPOSITORY_META_INFO_RECORD_MAP.length; i++) {
      var map = App.REPOSITORY_META_INFO_RECORD_MAP[i];
      var recordPropertyName = map[0];
      var apiObjectPropertyName = map[1];
      apiObject[apiObjectPropertyName] = this.get(recordPropertyName);
    }
    return apiObject;
  }

});

/**
 * Maps App.UserRecord property names to property names used by the server API objects
 */
App.REPOSITORY_META_INFO_RECORD_MAP = [
  [App.DOCUMENT_ID_RECORD_FIELD_NAME, App.DOCUMENT_ID_AO_FIELD_NAME ],
  [App.DOCUMENT_VERSION_RECORD_FIELD_NAME, App.DOCUMENT_VERSION_AO_FIELD_NAME],
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


/** @class
 *
 *  Repository runtime record contains run time information on a repository
 *
 *  @extends SC.Record
 */
App.RepositoryRuntimeInfoRecord = SC.Record.extend({

  primaryKey: App.DOCUMENT_ID_RECORD_FIELD_NAME,

  documentID: SC.Record.attr(String),
  documentVersion: SC.Record.attr(Number),
  name: SC.Record.attr(String),
  currentStatus: SC.Record.attr(String),

  /**
   * Maps server api data into this record
   *
   * @param {Object} repoInfoAO
   */
  fromApiObject: function(repoInfoAO) {
    // Ignore version check because this is a status update. We always want the latest status.
    // The documentID and documentVersion is that of the repo info record
    for (var i = 0; i < App.REPOSITORY_RUNTIME_RECORD_MAP.length; i++) {
      var map = App.REPOSITORY_RUNTIME_RECORD_MAP[i];
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
    for (var i = 0; i < App.REPOSITORY_RUNTIME_RECORD_MAP.length; i++) {
      var map = App.REPOSITORY_RUNTIME_RECORD_MAP[i];
      var recordPropertyName = map[0];
      var apiObjectPropertyName = map[1];
      apiObject[apiObjectPropertyName] = this.get(recordPropertyName);
    }
    return apiObject;
  }

});

/**
 * Maps App.UserRecord property names to property names used by the server API objects
 */
App.REPOSITORY_RUNTIME_RECORD_MAP = [
  [App.DOCUMENT_ID_RECORD_FIELD_NAME, App.DOCUMENT_ID_AO_FIELD_NAME ],
  [App.DOCUMENT_VERSION_RECORD_FIELD_NAME, App.DOCUMENT_VERSION_AO_FIELD_NAME],
  ['name' ,'Name'],
  ['currentStatus' ,'Status']
];

/** @class
 *
 * User record contains information on a user of chililogs
 *
 * @extends SC.Record
 */
App.UserRecord = SC.Record.extend({

  primaryKey: App.DOCUMENT_ID_RECORD_FIELD_NAME,

  documentID: SC.Record.attr(String),
  documentVersion: SC.Record.attr(Number),
  username: SC.Record.attr(String),
  emailAddress: SC.Record.attr(String),
  password: SC.Record.attr(String),
  roles: SC.Record.attr(Array),
  currentStatus: SC.Record.attr(String),
  displayName: SC.Record.attr(String),
  gravatarMD5Hash: SC.Record.attr(String),

  /**
   * Cached system.administrator role
   */
  isSystemAdministrator: SC.Record.attr(Boolean),

  /**
   * Cached repository access roles
   */
  repositoryAccesses: SC.Record.attr(Array),

  /**
   * Flag to be set so that we can trigger saving when adding/deleting repository access items to the array
   */
  repositoryAccessesChanged: SC.Record.attr(Boolean),

  /**
   * Maps server api data into this user record
   *
   * @param {Object} userAO
   */
  fromApiObject: function(userAO) {
    // If version has not changed, then there's nothing to update
    var recordVersion = this.get(App.DOCUMENT_VERSION_RECORD_FIELD_NAME);
    var apiObjectVersion = userAO[App.DOCUMENT_VERSION_AO_FIELD_NAME ];
    if (recordVersion === apiObjectVersion) {
      return;
    }

    for (var i = 0; i < App.USER_RECORD_MAP.length; i++) {
      var map = App.USER_RECORD_MAP[i];
      var recordPropertyName = map[0];
      var apiObjectPropertyName = map[1];
      this.set(recordPropertyName, userAO[apiObjectPropertyName]);
    }

    // Parse roles
    var isSystemAdministrator = NO;
    var repositoryAccesses = [];
    var roles = this.get('roles');
    if (!SC.none(roles)) {
      for (var i = 0; i < roles.length; i++) {
        var role = roles[i];
        if (role === App.SYSTEM_ADMINISTRATOR_ROLE) {
          isSystemAdministrator = YES;
        }
        else if (role.indexOf('repo.') === 0) {
          var parts = role.split('.');
          var repoName = parts[1];
          var repoRole = parts[2];
          repositoryAccesses.push({ repository: repoName, role: repoRole });
        }
      }
    }
    repositoryAccesses.sort(function(a, b) {
      var nameA = a.repository, nameB = b.repository;
      if (nameA < nameB) {
        return -1;
      }
      if (nameA > nameB) {
        return 1;
      }
      return 0;
    });

    this.set('isSystemAdministrator', isSystemAdministrator);
    this.set('repositoryAccesses', repositoryAccesses);
    this.set('repositoryAccessesChanged', NO);
  },

  /**
   * Maps user record data to api object
   *
   * @returns {Object} userAO
   */
  toApiObject: function() {
    // Re-create roles
    var roles = [];
    if (this.get('isSystemAdministrator')) {
      roles.push(App.SYSTEM_ADMINISTRATOR_ROLE);
    }
    var repositoryAccesses = this.get('repositoryAccesses');
    if (!SC.none(repositoryAccesses)) {
      for (var i = 0; i < repositoryAccesses.length; i++) {
        var repositoryAccess = repositoryAccesses[i];
        roles.push('repo.' + repositoryAccess.repository + '.' + repositoryAccess.role);
      }
    }
    this.set('roles', roles);

    // Map
    var apiObject = new Object();
    for (var i = 0; i < App.USER_RECORD_MAP.length; i++) {
      var map = App.USER_RECORD_MAP[i];
      var recordPropertyName = map[0];
      var apiObjectPropertyName = map[1];
      apiObject[apiObjectPropertyName] = this.get(recordPropertyName);
    }
    return apiObject;
  }

});

/**
 * Maps App.UserRecord property names to property names used by the server API objects
 */
App.USER_RECORD_MAP = [
  [App.DOCUMENT_ID_RECORD_FIELD_NAME, App.DOCUMENT_ID_AO_FIELD_NAME ],
  [App.DOCUMENT_VERSION_RECORD_FIELD_NAME, App.DOCUMENT_VERSION_AO_FIELD_NAME],
  ['username' ,'Username'],
  ['emailAddress' ,'EmailAddress'],
  ['password' ,'Password'],
  ['roles' ,'Roles'],
  ['currentStatus' ,'Status'],
  ['displayName' ,'DisplayName'],
  ['gravatarMD5Hash' ,'GravatarMD5Hash']
];

/**
 * Declare store
 */
App.store = SC.Store.create().from('App.DataSource');


/** @class
 *
 * The Chililog data source is a bit like Seinfeld show. It is a data source that does NOTHING.
 *
 * This is because it is a read only copy of the data that is on the server.  It is the responsibility of the engines
 * to sync the data source with the data on the server.
 *
 * This data source is a big copy of SC.FixturesDataSource.
 *
 * @extends SC.DataSource
 */
App.DataSource = SC.DataSource.extend({

  /**
   We are not going to do server lookups this because it is done via our data controllers
   We only perform local queries on the store.

   @param {SC.Store} store the requesting store
   @param {SC.Query} query query describing the request
   @returns {Boolean} YES if you can handle fetching the query, NO otherwise
   */
  fetch: function(store, query) {
    return NO; // return YES if you handled the query
  },

  /**
   We are not going to do server lookups this because it is done via our data controllers.
   We only perform local queries on the store.

   @param {SC.Store} store the requesting store
   @param {SC.Query} query query describing the request
   @returns {Boolean} YES if you can handle fetching the query, NO otherwise
   */
  retrieveRecord: function(store, storeKey) {
    return NO; // return YES if you handled the storeKey
  },

  /**
   Called from `createdRecords()` to created a single record.  This is the
   most basic primitive to can implement to support creating a record.

   To support cascading data stores, be sure to return `NO` if you cannot
   handle the passed storeKey or `YES` if you can.

   @param {SC.Store} store the requesting store
   @param {Array} storeKey key to update
   @param {Hash} params to be passed down to data source. originated
   from the commitRecords() call on the store
   @returns {Boolean} YES if handled
   */
  createRecord: function(store, storeKey, params) {
    var id = store.idFor(storeKey);
    var recordType = store.recordTypeFor(storeKey);
    this._invalidateCachesFor(recordType, storeKey, id);

    store.dataSourceDidComplete(storeKey);

    return YES; // return YES if you handled the storeKey
  },

  /**
   Called from `updatesRecords()` to update a single record.  This is the
   most basic primitive to can implement to support updating a record.

   To support cascading data stores, be sure to return Data store`NO` if you cannot
   handle the passed storeKey or `YES` if you can.

   @param {SC.Store} store the requesting store
   @param {Array} storeKey key to update
   @param {Hash} params to be passed down to data source. originated
   from the commitRecords() call on the store
   @returns {Boolean} YES if handled
   */
  updateRecord: function(store, storeKey, params) {
    var id = store.idFor(storeKey);
    var recordType = store.recordTypeFor(storeKey);
    this._invalidateCachesFor(recordType, storeKey, id);

    store.dataSourceDidComplete(storeKey);

    return YES; // return YES if you handled the storeKey
  },

  /**
   Called from `destroyRecords()` to destroy a single record.  This is the
   most basic primitive to can implement to support destroying a record.

   To support cascading data stores, be sure to return `NO` if you cannot
   handle the passed storeKey or `YES` if you can.

   @param {SC.Store} store the requesting store
   @param {Array} storeKey key to update
   @param {Hash} params to be passed down to data source. originated
   from the commitRecords() call on the store
   @returns {Boolean} YES if handled
   */
  destroyRecord: function(store, storeKey, params) {
    var id = store.idFor(storeKey);
    var recordType = store.recordTypeFor(storeKey);
    this._invalidateCachesFor(recordType, storeKey, id);

    store.dataSourceDidDestroy(storeKey);

    return YES;  // return YES if you handled the storeKey
  },

  /** @private
   Invalidates any internal caches based on the recordType and optional
   other parameters.  Currently this only invalidates the storeKeyCache used
   for fetch, but it could invalidate others later as well.

   @param {SC.Record} recordType the type of record modified
   @param {Number} storeKey optional store key
   @param {String} id optional record id
   @returns {SC.FixturesDataSource} receiver
   */
  _invalidateCachesFor: function(recordType, storeKey, id) {
    var cache = this._storeKeyCache;
    if (cache) {
      delete cache[SC.guidFor(recordType)];
    }
    return this;
  }

});
