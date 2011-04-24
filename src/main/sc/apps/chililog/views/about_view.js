// ==========================================================================
// Project:   Chililog 
// Copyright: ©2011 My Company, Inc.
// ==========================================================================

/**
 * About view
 */
Chililog.AboutView = SC.TemplateView.design({
  layout: { top: 0, left: 0, width: 300, height: 300 },
  templateName: 'about'
});

/**
 * Instanced about view
 */
Chililog.aboutView = Chililog.AboutView.create();

/**
 * Template binding for app version
 */
Chililog.AboutVersionView = SC.TemplateView.extend({
  classNames: ['inline'],
  valueBinding: 'Chililog.sessionDataController.chililogVersion'
});

/**
 * Template binding for app build timetsamp
 */
Chililog.AboutBuildTimestampView = SC.TemplateView.extend({
  classNames: ['inline'],
  valueBinding: 'Chililog.sessionDataController.chililogBuildTimestamp'
});
