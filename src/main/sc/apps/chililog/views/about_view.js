// ==========================================================================
// Project:   Chililog 
// Copyright: ©2011 My Company, Inc.
// ==========================================================================

/**
 * About view
 */
Chililog.aboutView = SC.TemplateView.design({
  layout: { top: 0, left: 0, width: 300, height: 300 },
  templateName: 'about'
});

Chililog.aboutVersionView = SC.TemplateView.extend({
  classNames: ['inline'],
  valueBinding: 'Chililog.sessionDataController.chililogVersion'
});

Chililog.aboutBuildTimestampView = SC.TemplateView.extend({
  classNames: ['inline'],
  valueBinding: 'Chililog.sessionDataController.chililogBuildTimestamp'
});
