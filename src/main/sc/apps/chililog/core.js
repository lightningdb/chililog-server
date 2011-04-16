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
