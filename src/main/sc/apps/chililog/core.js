// ==========================================================================
// Project:   Chililog
// Copyright: ©2011 My Company, Inc.
// ==========================================================================
/*globals Chililog */

/** @namespace

  Chililog Workbench. UI for querying and configuring Chililog Server.
  
  @extends SC.Object
*/
Chililog = SC.Application.create(
  /** @scope Chililog.prototype */ {

  NAMESPACE: 'Chililog',
  VERSION: '0.1.0',

  store: SC.Store.create().from('Chililog.DataSource')
});

/**
 * Name of cookie where we store the auth token
 */
Chililog.AUTHENTICATION_TOKEN_LOCAL_STORE_KEY = 'ChiliLog.AuthenticationToken';

/**
 * Name of Authentication header returned in API responses
 */
Chililog.AUTHENTICATION_HEADER_NAME = 'X-ChiliLog-Authentication';

/**
 * Name of app version header returned in API responses
 */
Chililog.VERSION_HEADER_NAME = 'X-ChiliLog-Version';

/**
 * Name of app build timestamp header returned in API responses
 */
Chililog.BUILD_TIMESTAMP_HEADER_NAME = 'X-ChiliLog-Build-Timestamp';

/**
 * All tokens to expire in 14 days
 */
Chililog.AUTHENTICATION_TOKEN_EXPIRY_SECONDS = 60 * 60 * 24 * 14;

/**
 * Name of the primary key field in Server API objects
 */
Chililog.DOCUMENT_ID_AO_FIELD_NAME = 'DocumentID';

/**
 * Name of the primary key field our SC.Records
 */
Chililog.DOCUMENT_ID_RECORD_FIELD_NAME = 'documentID';

/**
 * Name of the version field in Server API objects
 */
Chililog.DOCUMENT_VERSION_AO_FIELD_NAME = 'DocumentVersion';

/**
 * Name of the version field in our SC.Records
 */
Chililog.DOCUMENT_VERSION_RECORD_FIELD_NAME = 'documentVersion';

/**
 * User role representing a system administrator
 */
Chililog.SYSTEM_ADMINISTRATOR_ROLE = 'system.administrator';

/**
 * User role representing a repository administrator
 */
Chililog.REPOSITORY_ADMINISTRATOR_ROLE = 'administrator';

/**
 * User role representing a repository power user
 */
Chililog.REPOSITORY_POWER_USER_ROLE = 'power';

/**
 * User role representing a repository standard user
 */
Chililog.REPOSITORY_STANDARD_USER_ROLE = 'standard';
