// ==========================================================================
// Project:   Chililog - mainPage
// Copyright: ©2011 My Company, Inc.
// ==========================================================================

sc_require('views/label_mixin');

/**
 * The main account page has left hand side menu and right hand side body
 */
Chililog.MyAccountView = SC.View.design({
  layout: { top: 10, left: 10, bottom: 10, right: 10 },
  childViews: 'title left right'.w(),

  title: SC.LabelView.design({
    layout: { top: 0, left: 0, width: 200, height: 30 },
    tagName: 'h1',
    controlSize: SC.HUGE_CONTROL_SIZE,
    value: '_myAccountView.Title',
    localize: YES
  }),

  left: SC.ScrollView.design({
    layout: { top: 35, left: 0, bottom: 0, width: 200 },
    classNames: ['list-menu'],

    contentView: SC.ListView.design({
      layout: { top: 0, bottom: 0, left: 0, right: 0 },
      rowHeight: 40,
      isEditable: NO,
      actOnSelect: YES,
      content: [
        {
          id: 'MyProfile',
          label: '_myAccountView.MyProfile'.loc(),
          icon: sc_static('images/user.png')
        },
        {
          id: 'ChangePassword',
          label: '_myAccountView.ChangePassword'.loc(),
          icon: sc_static('images/password.png')
        }
      ],
      hasContentIcon: YES,
      contentValueKey: 'label',
      contentIconKey: 'icon',
      target: Chililog.myAccountViewController,
      action: 'onSelect'
    })
  }),

  right: SC.ContainerView.design({
    layout: { top: 35, bottom: 0, left: 208, right: 0 },
    classNames: ['box']
  })
});

/**
 * Instance the view
 */
Chililog.myAccountView = Chililog.MyAccountView.create();




