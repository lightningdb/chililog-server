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
      anchorLocation: SC.ANCHOR_TOP,
      childViews: 'menuOptions myProfileButton logoutButton'.w(),

      menuOptions: SC.SegmentedView.design({
        layout: { top: 5, left: 8 },
        align: SC.ALIGN_LEFT,
        controlSize: SC.LARGE_CONTROL_SIZE,
        itemsBinding: 'Chililog.mainPaneController.menuOptions',
        itemValueKey: 'value',
        itemTitleKey: 'title',
        itemToolTipKey: 'toolTip',
        itemTargetKey: 'target',
        itemActionKey: 'action',

        valueDidChange2: function() {
          return;
        }.observes('value')
      }),

      myProfileButton: SC.ButtonView.design({
        layout: { top: 5, right: 100, width: 150 },
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
            Chililog.mainPaneController.showMyAccount();
          }
        }.observes('value')
      }),

      logoutButton: SC.ButtonView.design({
        layout: { top: 5, right: 8, width: 80 },
        controlSize: SC.HUGE_CONTROL_SIZE,
        align: SC.ALIGN_RIGHT,
        title: '_mainPane.Logout'.loc(),
        target: 'Chililog.sessionDataController',
        action: 'logout'
      })
    }),

    body: SC.ContainerView.design({
      layout: { top: 40, left: 0, right: 0, bottom: 0 }
    })
    
  })  //mainPane

});

Chililog.searchView = SC.LabelView.design( {
  layout: { top: 0, left: 0, width: 200, height: 18 },
  textAlign: SC.ALIGN_CENTER,
  tagName: 'h1',
  value: 'Welcome to Search!'
});


Chililog.aboutView =  SC.TemplateView.design({
  layout: { top: 0, left: 0, width: 200, height: 18 },
    templateName: 'about'
  });

