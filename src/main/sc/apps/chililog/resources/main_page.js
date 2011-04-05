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
      childViews: 'menuOptions logoutButton'.w(),

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
        value: Chililog.mainPaneStates.SEARCH
      }),

      logoutButton: SC.ButtonView.design({
        layout: { top: 5, right: 8, width: 80 },
        controlSize: SC.HUGE_CONTROL_SIZE,
        align: SC.ALIGN_RIGHT,
        title: '_mainPane.Logout'.loc(),
        target: 'Chililog.sessionController',
        action: 'logout'
      })
    }),

    body: SC.ContainerView.design({
      layout: { top: 40, left: 0, right: 0, bottom: 0 },
      nowShowing: 'Chililog.searchView'
    }),

    /**
     * When the state in the controller changes, we change this pane to reflect it
     */
    stateDidChange: function() {
      var state = Chililog.mainPaneController.get('state');
      if (state === Chililog.mainPaneStates.SEARCH) {
        this.setPath('body.nowShowing', 'Chililog.searchView');
      }
      else if (state === Chililog.mainPaneStates.ABOUT) {
        this.setPath('body.nowShowing', 'Chililog.aboutView');
      }

      // Make sure that we sync with state just in case someone changes the state
      this.setPath('toolBar.menuOptions.value', state);
    }.observes('Chililog.mainPaneController.state')


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


