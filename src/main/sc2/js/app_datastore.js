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

/**
 * Maps RepositoryConfigRecord.MaxMemoryPolicy codes to text
 */
App.REPOSITORY_MAX_MEMORY_POLICY_MAP = {
  'DROP': '_repositoryEntryRecord.MaxMemoryPolicy.Drop'.loc(),
  'PAGE': '_repositoryEntryRecord.MaxMemoryPolicy.Page'.loc(),
  'BLOCK': '_repositoryEntryRecord.MaxMemoryPolicy.Block'.loc()
};

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


/**
 * Code for the status of an online repository; i.e. repository can process log entries and is searchable
 */
App.REPOSITORY_STATUS_ONLINE = 'ONLINE';

/**
 * Code for the status of a readonly repository; i.e. repository can only be searched via the workbench
 */
App.REPOSITORY_STATUS_READONLY = 'READONLY';

/**
 * Code for the status of an offline repository. Read/write access to this repository is turned off.
 */
App.REPOSITORY_STATUS_OFFLINE = 'OFFLINE';

/**
 * Maps RepositoryStatusRecord.Status codes to text
 */
App.REPOSITORY_STATUS_MAP = {
  'ONLINE': '_repositoryStatusRecord.Status.Online'.loc(),
  'READONLY': '_repositoryStatusRecord.Status.ReadOnly'.loc(),
  'OFFLINE': '_repositoryStatusRecord.Status.Offline'.loc()
};

/**
 * User status of enabled - can login
 */
App.USER_STATUS_ENABLED = 'ENABLED';

/**
 * User status of disabled - cannot login
 */
App.USER_STATUS_DISABLED = 'DISABLED';

/**
 * User status of locked - too many failed logins
 */
App.USER_STATUS_LOCKED = 'LOCKED';

/**
 * Maps RepositoryStatusRecord.Status codes to text
 */
App.USER_STATUS_MAP = {
  'ENABLED': '_userRecord.currentStatus.enabled'.loc(),
  'DISABLED': '_userRecord.currentStatus.disabled'.loc(),
  'LOCKED': '_userRecord.currentStatus.locked'.loc()
};

/**
 * Maps User Role codes to text
 */
App.USER_ROLE_MAP = {
  'workbench': '_userRecord.role.workbench'.loc(),
  'publisher': '_userRecord.role.publisher'.loc(),
  'subscriber': '_userRecord.role.subscriber'.loc(),
  'system.administrator': '_userRecord.role.systemAdministrator'.loc()
};

// --------------------------------------------------------------------------------------------------------------------
// AuthenticatedUserRecord
// --------------------------------------------------------------------------------------------------------------------

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
   * Returns the display name of the logged in user. If not set, the username is returned.
   *
   * @type String
   */
  loggedInUserGravatarURL: function() {
    var ghash = this.get('gravatarMD5Hash');
    if (SC.empty(ghash)) {
      return null;
    }
    return 'http://www.gravatar.com/avatar/' + ghash + '.jpg?s=18&d=mm';
  }.property('gravatarMD5Hash').cacheable(),

  /**
   * YES if the user is a system administrator
   *
   * @type Boolean
   */
  isSystemAdministrator: function() {
    var idx = jQuery.inArray('system.administrator', this.get('roles'));
    return idx >= 0;
  }.property('roles').cacheable(),

  /**
   * YES if the user is an administrator for one or more repositories
   *
   * @type Boolean
   */
  isRepositoryAdministrator: function() {
    var roles = this.get('roles');
    if (!SC.none(roles)) {
      for (var i = 0; i < roles.length; i++) {
        var role = roles[i];
        if (role.indexOf('repo.' === 0) &&
          role.indexOf('.' + App.REPOSITORY_ADMINISTRATOR_ROLE) > 0) {
          return YES;
        }
      }
    }
    return NO;
  }.property('roles').cacheable(),

  /**
   * Checks if the logged in user is the administrator the specified repository
   * @param {App.RepositoryConfigRecord} repositoryRecord
   * @returns Boolean
   */
  isRepositoryAdministratorOf: function(repositoryRecord) {
    var roles = this.get('roles');
    if (!SC.none(roles)) {
      var roleName = 'repo.' + repositoryRecord.get('name') + '.' + App.REPOSITORY_ADMINISTRATOR_ROLE;
      for (var i = 0; i < roles.length; i++) {
        if (roles[i] === roleName) {
          return YES;
        }
      }
    }
    return NO;
  },
  
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


// --------------------------------------------------------------------------------------------------------------------
// RepositoryConfigRecord
// --------------------------------------------------------------------------------------------------------------------

/** @class
 *
 * Repository Meta Information record contains information that describes the repository and the parameters
 * under which it will operate.
 *
 * @extends SC.Record
 */
App.RepositoryConfigRecord = SC.Record.extend({

  primaryKey: App.DOCUMENT_ID_RECORD_FIELD_NAME,

  documentID: SC.Record.attr(String),
  documentVersion: SC.Record.attr(Number, { defaultValue: 0 }),
  name: SC.Record.attr(String, { defaultValue: '', isRequired: YES }),
  displayName: SC.Record.attr(String, { defaultValue: '' }),
  description: SC.Record.attr(String, { defaultValue: '' }),
  startupStatus: SC.Record.attr(String, { defaultValue: App.REPOSITORY_STATUS_ONLINE, isRequired: YES }),

  storeEntriesIndicator: SC.Record.attr(Boolean, { defaultValue: YES, isRequired: YES }),
  storageQueueDurableIndicator: SC.Record.attr(Boolean, { defaultValue: NO, isRequired: YES }),
  storageQueueWorkerCount: SC.Record.attr(Number, { defaultValue: 1, isRequired: YES }),
  storageMaxKeywords: SC.Record.attr(Number, { defaultValue: 50, isRequired: YES }),

  maxMemory: SC.Record.attr(Number, { defaultValue: 10485760, isRequired: YES }),  //10MB
  maxMemoryPolicy: SC.Record.attr(String, { defaultValue: App.REPOSITORY_MAX_MEMORY_POLICY_PAGE }),
  pageSize: SC.Record.attr(Number, { defaultValue: 1048576, isRequired: YES }),    //1MB
  pageCountCache: SC.Record.attr(Number, { defaultValue: 3, isRequired: YES }),    //3

  /**
   * Readonly details of users who can access this repository. Each object in the array contains properties: username,
   * role.
   *
   * @type Array of objects
   */
  users: [],

  /**
   * Returns the display name if not blank, else returns the name
   * @type String
   */
  displayNameOrName: function() {
    var displayName = this.get('displayName');
    if (SC.empty(displayName)) {
      displayName = this.get('name');
    }
    return displayName;
  }.property('name', 'displayName').cacheable(),

  /**
   * The current status code of this repository: ONLINE or OFFLINE
   * @type String
   */
  currentStatus: SC.Record.attr(String, { defaultValue: App.REPOSITORY_STATUS_OFFLINE }),

  /**
   * The current status description of this repository: ONLINE or OFFLINE
   * @type String
   */
  currentStatusText: function() {
    var currentStatus = this.get('currentStatus');
    return App.REPOSITORY_STATUS_MAP[currentStatus];
  }.property('currentStatus'),
  
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

    for (var i = 0; i < App.REPOSITORY_CONFIG_RECORD_MAP.length; i++) {
      var map = App.REPOSITORY_CONFIG_RECORD_MAP[i];
      var recordPropertyName = map[0];
      var apiObjectPropertyName = map[1];
      this.set(recordPropertyName, repoInfoAO[apiObjectPropertyName]);
    }

    // Parse the users with access
    var users = repoInfoAO['Users'];
    var parsedUsers = [];
    if (!SC.none(users)) {
      users.forEach(function(item) {
        var idx = item.indexOf('=');
        var username = item.substring(0, idx);
        var role = item.substring(idx + 1);
        var roleCode = role;
        if (role != App.SYSTEM_ADMINISTRATOR_ROLE) {
          idx = role.lastIndexOf('.');
          roleCode = role.substr(idx + 1);
        }
        var label = username + ' (' + App.USER_ROLE_MAP[roleCode] + ')';

        var u = {
          username: username,
          role: role,
          label: label,
          value: item
        };
        parsedUsers.push(u);
      });
    }
    this.set('users', parsedUsers)

    // Update the status
    this.syncCurrentStatus();
  },

  /**
   * Maps record data to api object
   *
   * @returns {Object} repoInfoAO
   */
  toApiObject: function() {
    var apiObject = new Object();
    for (var i = 0; i < App.REPOSITORY_CONFIG_RECORD_MAP.length; i++) {
      var map = App.REPOSITORY_CONFIG_RECORD_MAP[i];
      var recordPropertyName = map[0];
      var apiObjectPropertyName = map[1];
      apiObject[apiObjectPropertyName] = this.get(recordPropertyName);
    }
    return apiObject;
  },

  /**
   * Synchronise the current status with what is in the repository
   */
  syncCurrentStatus: function() {
    var statusRecord = App.store.find(App.RepositoryStatusRecord, this.get(App.DOCUMENT_ID_RECORD_FIELD_NAME));
    if (SC.none(statusRecord)) {
      this.set('currentStatus', App.REPOSITORY_STATUS_OFFLINE);
    } else {
      this.set('currentStatus', statusRecord.get('currentStatus'));
    }
    this.commitRecord();
  }


});

/**
 * Maps App.UserRecord property names to property names used by the server API objects
 */
App.REPOSITORY_CONFIG_RECORD_MAP = [
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

// --------------------------------------------------------------------------------------------------------------------
// RepositoryRuntimeInfoRecord
// --------------------------------------------------------------------------------------------------------------------

/** @class
 *
 *  Repository runtime record contains run time information on a repository
 *
 *  @extends SC.Record
 */
App.RepositoryStatusRecord = SC.Record.extend({

  primaryKey: App.DOCUMENT_ID_RECORD_FIELD_NAME,

  documentID: SC.Record.attr(String),
  documentVersion: SC.Record.attr(Number),
  name: SC.Record.attr(String),
  displayName: SC.Record.attr(String),
  currentStatus: SC.Record.attr(String),

  /**
   * The current status description of this repository: ONLINE or OFFLINE
   * @type String
   */
  currentStatusText: function() {
    var currentStatus = this.get('currentStatus');
    return App.REPOSITORY_STATUS_MAP[currentStatus];
  }.property('currentStatus').cacheable(),

  /**
   * Display name or name if display name is blank
   * @type String
   */
  displayNameOrName: function() {
    var displayName = this.get('displayName');
    if (SC.empty(displayName)) {
      displayName = this.get('name');
    }
    return displayName;
  }.property('name', 'displayName').cacheable(),

  /**
   * Maps server api data into this record
   *
   * @param {Object} repoInfoAO
   */
  fromApiObject: function(repoInfoAO) {
    // Ignore version check because this is a status update. We always want the latest status.
    // The documentID and documentVersion is that of the repo info record
    for (var i = 0; i < App.REPOSITORY_STATUS_RECORD_MAP.length; i++) {
      var map = App.REPOSITORY_STATUS_RECORD_MAP[i];
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
    for (var i = 0; i < App.REPOSITORY_STATUS_RECORD_MAP.length; i++) {
      var map = App.REPOSITORY_STATUS_RECORD_MAP[i];
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
App.REPOSITORY_STATUS_RECORD_MAP = [
  [App.DOCUMENT_ID_RECORD_FIELD_NAME, App.DOCUMENT_ID_AO_FIELD_NAME ],
  [App.DOCUMENT_VERSION_RECORD_FIELD_NAME, App.DOCUMENT_VERSION_AO_FIELD_NAME],
  ['name' ,'Name'],
  ['displayName' ,'DisplayName'],
  ['currentStatus' ,'Status']
];


// --------------------------------------------------------------------------------------------------------------------
// UserRecord
// --------------------------------------------------------------------------------------------------------------------

/** @class
 *
 * User record contains information on a user of chililogs
 *
 * @extends SC.Record
 */
App.UserRecord = SC.Record.extend({

  primaryKey: App.DOCUMENT_ID_RECORD_FIELD_NAME,

  documentID: SC.Record.attr(String),
  documentVersion: SC.Record.attr(Number, { defaultValue: 0 }),
  username: SC.Record.attr(String, { defaultValue: '', isRequired: YES }),
  emailAddress: SC.Record.attr(String, { defaultValue: '' }),
  password: SC.Record.attr(String, { defaultValue: '' }),
  roles: SC.Record.attr(Array, { defaultValue: [] }),
  currentStatus: SC.Record.attr(String, { defaultValue: App.USER_STATUS_ENABLED }),
  displayName: SC.Record.attr(String, { defaultValue: '' }),
  gravatarMD5Hash: SC.Record.attr(String),

  /**
   * Returns the text description of the current status
   * @type String
   */
  currentStatusText: function() {
    var currentStatus = this.get('currentStatus');
    return App.USER_STATUS_MAP[currentStatus];
  }.property('currentStatus').cacheable(),

  /**
   * Cached system.administrator role
   */
  isSystemAdministrator: SC.Record.attr(Boolean, { defaultValue: NO }),

  /**
   * Cached array of data hash {repository: repoName, role: roleName} representing the repositories that can be
   * accessed by this user and the role
   */
  repositoryAccesses: SC.Record.attr(Array, { defaultValue: [] }),

  /**
   * Flag to be set so that we can trigger saving when adding/deleting repository access items to the array
   * This affects our flagging of if a record has changed
   */
  repositoryAccessesChanged: SC.Record.attr(Boolean, { defaultValue: NO }),

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
          var roleLabel = App.USER_ROLE_MAP[repoRole];
          repositoryAccesses.push({ repository: repoName, role: repoRole, label: repoName + ' (' + roleLabel + ')', value: role });
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


// --------------------------------------------------------------------------------------------------------------------
// Store and dummy data source
// --------------------------------------------------------------------------------------------------------------------

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

