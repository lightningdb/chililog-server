// ==========================================================================
// Project:   Chililog.mainPaneController
// Copyright: ©2011 My Company, Inc.
// ==========================================================================


/**
 * Controller that for the mainPane. Mainly handles menu selection option and login/logout
 *
 * @extends SC.Object
 */
Chililog.mainViewController = SC.Object.create(
/** @scope Chililog.mainPaneController.prototype */ {

  /**
   * Array of menu options items
   * @type Array
   */
  menuOptions: null,

  /**
   * Rebuild menu options if the logged in user changes
   */
  buildMenuOptions: function() {
    var values = [
      { value: 'search',
        title: '_mainPane.Search'.loc(),
        toolTip: '_mainPane.Search.ToolTip'.loc(),
        target: Chililog.mainViewController,
        action: 'showSearch'
      },
      { value: 'analyse',
        title: '_mainPane.Analyse'.loc(),
        toolTip: '_mainPane.Analyse.ToolTip'.loc(),
        target: Chililog.mainViewController,
        action: 'showAnalyse'
      }
    ];

    var isAdmin = Chililog.sessionDataController.get('isInAdministratorRole');
    if (isAdmin) {
      values.push({
        value: 'configure',
        title: '_mainPane.Configure'.loc(),
        toolTip: '_mainPane.Configure.ToolTip'.loc(),
        target: Chililog.mainViewController,
        action: 'showConfigure'
      });
    }

    values.push({
      value: 'about',
      title: '_mainPane.About'.loc(),
      toolTip: '_mainPane.About.ToolTip'.loc(),
      target: Chililog.mainViewController,
      action: 'showAbout'
    });

    // Set new menu options
    this.set('menuOptions', values);
  },

  /**
   * Show search view
   */
  showSearch: function() {
    Chililog.statechart.sendEvent('showSearch');
  },

  /**
   * Show analyse view
   */
  showAnalyse: function() {
    Chililog.statechart.sendEvent('showAnalyse');
  },

  /**
   * Show monitor view
   */
  showMonitor: function() {
    Chililog.statechart.sendEvent('showMonitor');
  },

  /**
   * Show configure views
   */
  showConfigure: function() {
    Chililog.statechart.sendEvent('showConfigure');
  },

  /**
   * Show user account view
   */
  showMyAccount: function() {
    Chililog.statechart.sendEvent('showMyAccount');
  },

  /**
   * Show about view
   */
  showAbout: function() {
    Chililog.statechart.sendEvent('showAbout');
  },

  /**
   * Handle the displaying of the correct
   */
  doShow: function(viewName) {
    var view = null;
    if (viewName === 'search') {
      if (Chililog.searchListView != null) {
        Chililog.searchListView.destroy();
      }
      Chililog.searchListView = Chililog.SearchListView.create();
      view = Chililog.searchListView;
    }
    else if (viewName === 'analyse') {
      //view = Chililog.searchListView;
    }
    else if (viewName === 'monitor') {
      //view = Chililog.searchListView;
    }
    else if (viewName === 'configure') {
      view = Chililog.configureView;
    }
    else if (viewName === 'about') {
      view = Chililog.aboutView;
    }
    else if (viewName === 'myAccount') {
      view = Chililog.myAccountView;
    }
    var body = Chililog.mainPage.getPath('mainPane.body');
    body.set('contentView', view);

    Chililog.mainPage.setPath('mainPane.toolBar.menuOptions.value', viewName);
    Chililog.mainPage.setPath('mainPane.toolBar.myProfileButton.value', (viewName === 'myAccount'));
  },

  /**
   * Logout
   */
  logout: function() {
    Chililog.statechart.sendEvent('logout');
  },

  /**
   * Show main page upon login
   */
  showMainPage: function () {
    Chililog.getPath('mainPage.mainPane').append();
  },

  /**
   * Hide main page upon logout
   */
  hideMainPage: function () {
    Chililog.getPath('mainPage.mainPane').remove();
  }
  
});

