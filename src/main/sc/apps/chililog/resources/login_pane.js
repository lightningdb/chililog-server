// ==========================================================================
// Project:   Chililog - mainPage
// Copyright: ©2011 My Company, Inc.
// ==========================================================================

/**
 * The login page
 */
Chililog.loginPage = SC.Page.design({

  loginPane: SC.MainPane.design({
    childViews: 'test'.w(),

    test: SC.LabelView.design({
      layout: { centerX: 0, centerY: 0, width: 200, height: 18 },
      textAlign: SC.ALIGN_CENTER,
      tagName: 'h1',
      value: 'Welcome to Login'
    })
  })
});