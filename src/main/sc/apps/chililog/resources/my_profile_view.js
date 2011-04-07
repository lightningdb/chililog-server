// ==========================================================================
// Project:   Chililog - mainPage
// Copyright: ©2011 My Company, Inc.
// ==========================================================================

/**
 * My profile view
 */

Chililog.myProfileView = SC.View.design({
  layout: { top: 10, left: 10, bottom: 10, right: 10 },
  childViews: 'title data'.w(),

  title: SC.LabelView.design({
    layout: { top: 0, left: 0, width: 200, height: 30 },
    tagName: 'h1',
    controlSize: SC.HUGE_CONTROL_SIZE,
    value: 'My Profile'
  }),

  data: SC.View.design({
    layout: { top: 35, left: 0, right: 0, height: 400 },
    classNames: ['box'],
    childViews: 'username'.w(),

    username: SC.View.design({
      layout: { left: 10, right: 10, top: 10, height: 40 },
      childViews: 'label field'.w(),

      label: SC.LabelView.design({
        layout: { top: 0, left: 0, width: 107, height: 18, centerY: 0 },
        value: '_loginPane.Username',
        localize: YES
      }),

      field: SC.TextFieldView.design({
        layout: { top: 20, left: 0, width: 107, height: 18, centerY: 0 },
        controlSize: SC.HUGE_CONTROL_SIZE,
        value: ''
      })
    })
  })
});
