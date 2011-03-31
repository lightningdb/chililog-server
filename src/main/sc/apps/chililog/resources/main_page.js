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
        items: [
          { value: Chililog.mainPaneStates.SEARCH, title: '_topBar.Search'.loc() },
          { value: Chililog.mainPaneStates.ANALYSIS, title: '_topBar.Analysis'.loc() },
          { value: Chililog.mainPaneStates.MONITORS, title: '_topBar.Monitors'.loc() },
          { value: Chililog.mainPaneStates.REPOSITORIES, title: '_topBar.Repositories'.loc() },
          { value: Chililog.mainPaneStates.USERS, title: '_topBar.Users'.loc() },
          { value: Chililog.mainPaneStates.ABOUT, title: '_topBar.About'.loc() }
        ],
        itemValueKey: 'value',
        itemTitleKey: 'title',
        value: Chililog.mainPaneStates.SEARCH,

        valueDidChange: function() {
          // Call sc_super to hi-light selected option
          sc_super();
          Chililog.mainPaneController.doAction(this.get('value'));
        }.observes('value')
      }),

      logoutButton: SC.ButtonView.design({
        layout: { top: 5, right: 8, width: 80 },
        controlSize: SC.HUGE_CONTROL_SIZE,
        align: SC.ALIGN_RIGHT,
        title: '_topBar.Logout'.loc()
      })
    }),

    body: SC.ContainerView.design({
      nowShowing: 'Chililog.searchPane'
    }),

    /**
     * When the state in the controller changes, we change this pane to reflect it
     */
    stateDidChange: function() {
      var state = Chililog.mainPaneController.get('state');
      if (state === Chililog.mainPaneStates.SEARCH) {
        this.setPath('body.nowShowing', 'Chililog.searchPane');
      }
      else if (state === Chililog.mainPaneStates.ABOUT) {
        this.setPath('body.nowShowing', 'Chililog.aboutPane');
      }

      // Make sure that we sync with state just in case someone changes the state
      this.setPath('toolBar.menuOptions.value', state);
    }.observes('Chililog.mainPaneController.state')


  })

});

Chililog.searchPane = SC.LabelView.design({
  layout: { centerX: 0, centerY: 0, width: 200, height: 18 },
  textAlign: SC.ALIGN_CENTER,
  tagName: 'h1',
  value: 'Welcome to Search!'
});


Chililog.aboutPane = SC.LabelView.design({
  layout: { centerX: 0, centerY: 0, width: 200, height: 18 },
  textAlign: SC.ALIGN_CENTER,
  tagName: 'h1',
  value: 'Welcome to About!'
});

Chililog.loginPane = SC.LabelView.design({
  layout: { centerX: 0, centerY: 0, width: 200, height: 18 },
  textAlign: SC.ALIGN_CENTER,
  tagName: 'h1',
  value: 'Welcome to Login'
});