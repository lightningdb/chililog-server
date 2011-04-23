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
        action: 'showAnalysis'
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
   * Show analysis view
   */
  showAnalysis: function() {
    Chililog.statechart.sendEvent('showAnalysis');
  },

  /**
   * Show monitors view
   */
  showMonitors: function() {
    Chililog.statechart.sendEvent('showMonitors');
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
   * Logout
   */
  logout: function() {
    Chililog.statechart.sendEvent('logout');
  }


});

