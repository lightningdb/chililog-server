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
Chililog.UserRecord = SC.Record.extend(
/** @scope Chililog.UserRecord.prototype */ {

  primaryKey: 'documentID',
  
  documentID: SC.Record.attr(String),
  documentVersion: SC.Record.attr(Number),
  username: SC.Record.attr(String),
  emailAddress: SC.Record.attr(String),
  password: SC.Record.attr(String),
  roles: SC.Record.attr(Array),
  status: SC.Record.attr(String),
  displayName: SC.Record.attr(String),
  gravatarMD5Hash: SC.Record.attr(String)
});
