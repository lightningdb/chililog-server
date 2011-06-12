// ==========================================================================
// Project:   Chililog - mainPage
// Copyright: ©2011 My Company, Inc.
// ==========================================================================

/**
 * The main page has 2 parts: the top toolbar and the body.
 */
Chililog.mainPage = SC.Page.design({

  mainPane: SC.MainPane.design({
    childViews: 'toolBar body'.w(),

    toolBar: SC.ToolbarView.design({
      layout: { top: 0, left: 0, right: 0, height: 40 },
      classNames: ['app-toolbar'],
      anchorLocation: SC.ANCHOR_TOP,
      childViews: 'title menuOptions myProfileButton logoutButton'.w(),

      title: SC.LabelView.design({
        layout: { top: 5, left: 10, width: 100, height: 40 },
        classNames: ['app-title'],
        tagName: 'h1',
        controlSize: SC.HUGE_CONTROL_SIZE,
        value: '_mainPane.Title',
        localize: YES
      }),

      menuOptions: SC.SegmentedView.design({
        layout: { top: 5, left: 10, width: 500 },
        align: SC.ALIGN_LEFT,
        controlSize: SC.LARGE_CONTROL_SIZE,
        itemsBinding: 'Chililog.mainViewController.menuOptions',
        itemValueKey: 'value',
        itemTitleKey: 'title',
        itemToolTipKey: 'toolTip',
        itemTargetKey: 'target',
        itemActionKey: 'action'
      }),

      myProfileButton: SC.ButtonView.design({
        layout: { top: 5, right: 120, width: 150 },
        classNames: ['bold'],
        controlSize: SC.HUGE_CONTROL_SIZE,
        align: SC.ALIGN_RIGHT,
        iconBinding: 'Chililog.sessionDataController.loggedInUserGravatarURL',
        titleBinding: 'Chililog.sessionDataController.loggedInUserDisplayName',
        toolTip: '_mainPane.MyProfile.ToolTip'.loc(),
        buttonBehavior: SC.TOGGLE_BEHAVIOR,
        value: NO,
        toggleOnValue: YES,
        toggleOffValue: NO,

        valueDidChange2: function() {
          if (this.get('value') === this.get('toggleOnValue')) {
            Chililog.mainViewController.showMyAccount();
          }
        }.observes('value')
      }),

      logoutButton: SC.ButtonView.design({
        layout: { top: 5, right: 10, width: 100 },
        controlSize: SC.HUGE_CONTROL_SIZE,
        align: SC.ALIGN_RIGHT,
        title: '_mainPane.Logout'.loc(),
        target: 'Chililog.mainViewController',
        action: 'logout'
      })
    }),

    body: SC.ContainerView.design({
      layout: { top: 40, left: 0, right: 0, bottom: 0 },
      classNames: ['app-body']
    })
  }) //mainPane

});
