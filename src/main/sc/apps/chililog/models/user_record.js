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
Chililog.UserRecord = SC.Record.extend({

  primaryKey: Chililog.DOCUMENT_ID_RECORD_FIELD_NAME,

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

    // Parse roles
    var isSystemAdministrator = NO;
    var repositoryAccesses = [];
    var roles = this.get('roles');
    if (!SC.none(roles)) {
      for (var i=0; i<roles.length; i++) {
        var role = roles[i];
        if (role === Chililog.SYSTEM_ADMINISTRATOR_ROLE) {
          isSystemAdministrator = YES;
        }
        else if (role.indexOf('repo.') === 0) {
          var parts = role.split('.');
          var repoName = parts[1];
          var repoAccess = parts[2];
          repositoryAccesses.push( { repository: repoName, access: repoAccess } );
        }
      }
    }
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
      roles.push(Chililog.SYSTEM_ADMINISTRATOR_ROLE);
    }
    var repositoryAccesses = this.get('repositoryAccesses');
    if (!SC.none(repositoryAccesses)) {
      for (var i=0; i<repositoryAccesses.length; i++) {
        var repositoryAccess = repositoryAccesses[i];
        roles.push('repo.' + repositoryAccess.repository + '.' + repositoryAccess.access);
      }
    }
    this.set('roles', roles);

    // Map
    var apiObject = new Object();
    for (var i = 0; i < Chililog.USER_RECORD_MAP.length; i++) {
      var map = Chililog.USER_RECORD_MAP[i];
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
Chililog.USER_RECORD_MAP = [
  [Chililog.DOCUMENT_ID_RECORD_FIELD_NAME, Chililog.DOCUMENT_ID_AO_FIELD_NAME ],
  [Chililog.DOCUMENT_VERSION_RECORD_FIELD_NAME, Chililog.DOCUMENT_VERSION_AO_FIELD_NAME],
  ['username' ,'Username'],
  ['emailAddress' ,'EmailAddress'],
  ['password' ,'Password'],
  ['roles' ,'Roles'],
  ['currentStatus' ,'Status'],
  ['displayName' ,'DisplayName'],
  ['gravatarMD5Hash' ,'GravatarMD5Hash']
];
