// ==========================================================================
// Project:   Chililog
// Copyright: ©2011 My Company, Inc.
// ==========================================================================

/**
 * Name of cookie where we store the auth token
 */
Chililog.AUTHENTICATION_TOKEN_LOCAL_STORE_KEY = 'Chililog.AuthenticationToken';

/**
 * Name of Authentication header returned in API responses
 */
Chililog.AUTHENTICATION_HEADER_NAME = 'X-Chililog-Authentication';

/**
 * Name of app version header returned in API responses
 */
Chililog.VERSION_HEADER_NAME = 'X-Chililog-Version';

/**
 * Name of app build timestamp header returned in API responses
 */
Chililog.BUILD_TIMESTAMP_HEADER_NAME = 'X-Chililog-Build-Timestamp';

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
Chililog.REPOSITORY_WORKBENCH_ROLE = 'workbench';

/**
 * User role representing a repository standard user
 */
Chililog.REPOSITORY_PUBLISHER_ROLE = 'publisher';

/**
 * User role representing a repository standard user
 */
Chililog.REPOSITORY_SUBSCRIBER_ROLE = 'subscriber';

/**
 * Code for the status of an online repository; i.e. one that is started and can process log entries
 */
Chililog.REPOSITORY_ONLINE = 'ONLINE';

/**
 * Code for the status of an offline repository; i.e. one that is stopped and cannot process log entries
 */
Chililog.REPOSITORY_OFFLINE = 'OFFLINE';

/**
 * If a repository reaches max memory, drop new messages
 */
Chililog.REPOSITORY_MAX_MEMORY_POLICY_DROP = 'DROP';

/**
 * If a repository reaches max memory, page new messages to file
 */
Chililog.REPOSITORY_MAX_MEMORY_POLICY_PAGE = 'PAGE';

/**
 * If a repository reaches max memory, make producers block (wait) until memory reduces.
 */
Chililog.REPOSITORY_MAX_MEMORY_POLICY_BLOCK = 'BLOCK';

