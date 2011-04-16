// ==========================================================================
// Project:   Chililog.mainPaneController
// Copyright: ©2011 My Company, Inc.
// ==========================================================================


/**
 * States of the mainPane in the mainPage.
 */
Chililog.mainPaneStates = {
  SEARCH: 'Search',
  ANALYSE: 'Analyse',
  MONITOR: 'Monitor',
  CONFIGURE: 'Configure',
  ABOUT: 'About',
  MY_ACCOUNT: 'MyAccount'
}

/**
 * Controller that for the mainPane. Mainly handles menu selection option and login/logout
 *
 * @extends SC.Object
 */
Chililog.mainPaneController = SC.Object.create(
/** @scope Chililog.mainPaneController.prototype */ {

  /**
   * Determines the current visible 'body' view
   * @type State
   */
  state: '',

  /**
   * Array of menu options items
   * @type Array
   */
  menuOptions: null,

  /**
   * Rebuild menu options if the logged in user changes
   */
  loggedInUserDidChange: function() {
    var values = [
      { value: Chililog.mainPaneStates.SEARCH,
        title: '_mainPane.Search'.loc(),
        toolTip: '_mainPane.Search.ToolTip'.loc(),
        target: Chililog.mainPaneController,
        action: 'showSearch'
      },
      { value: Chililog.mainPaneStates.ANALYSE,
        title: '_mainPane.Analyse'.loc(),
        toolTip: '_mainPane.Analyse.ToolTip'.loc(),
        target: Chililog.mainPaneController,
        action: 'showAnalysis'
      }
    ];

    var isAdmin = Chililog.sessionDataController.get('isInAdministratorRole');
    if (isAdmin) {
      values.push({
        value: Chililog.mainPaneStates.CONFIGURE,
        title: '_mainPane.Configure'.loc(),
        toolTip: '_mainPane.Configure.ToolTip'.loc(),
        target: Chililog.mainPaneController,
        action: 'showRepositories'
      });
    }

    values.push({
      value: Chililog.mainPaneStates.ABOUT,
      title: '_mainPane.About'.loc(),
      toolTip: '_mainPane.About.ToolTip'.loc(),
      target: Chililog.mainPaneController,
      action: 'showAbout'
    });

    // Set new menu options
    this.set('menuOptions', values);

    // Make views react to changes in menu options because after logging out,
    // menu option changes. So when the user logs back in, we have to select what was there before
    this.notifyPropertyChange('state');
  }.observes('Chililog.sessionDataController.loggedInUser'),

  /**
   * Show search view
   */
  showSearch: function() {
    this.set('state', Chililog.mainPaneStates.SEARCH);
  },

  /**
   * Show analysis view
   */
  showAnalysis: function() {
    this.set('state', Chililog.mainPaneStates.ANALYSE);
  },

  /**
   * Show monitors view
   */
  showMonitors: function() {
    this.set('state', Chililog.mainPaneStates.MONITOR);
  },

  /**
   * Show repository views
   */
  showRepositories: function() {
    this.set('state', Chililog.mainPaneStates.CONFIGURE);
  },

  /**
   * Show user account view
   */
  showMyAccount: function() {
    this.set('state', Chililog.mainPaneStates.MY_ACCOUNT);
  },

  /**
   * Show about view
   */
  showAbout: function() {
    this.set('state', Chililog.mainPaneStates.ABOUT);
  },

  /**
   * When the state in the controller changes, we change this pane to reflect it
   */
  stateDidChange: function() {
    var state = this.get('state');
    var body = Chililog.mainPage.getPath('mainPane.body');
    var nowShowing = body.get('nowShowing');

    if (state === Chililog.mainPaneStates.SEARCH && nowShowing !== 'Chililog.searchView') {
      body.set('nowShowing', 'Chililog.searchView');
    }
    else if (state === Chililog.mainPaneStates.ANALYSE && nowShowing !== 'Chililog.aboutView') {
      body.set('nowShowing', 'Chililog.aboutView');
    }
    else if (state === Chililog.mainPaneStates.ABOUT && nowShowing !== 'Chililog.aboutView') {
      body.set('nowShowing', 'Chililog.aboutView');
    }
    else if (state === Chililog.mainPaneStates.MY_ACCOUNT && nowShowing !== 'Chililog.myAccountView') {
      body.set('nowShowing', 'Chililog.myAccountView');
    }

    // Make sure that we sync buttons with state just in case someone changes the state
    Chililog.mainPage.setPath('mainPane.toolBar.menuOptions.value', state);
    Chililog.mainPage.setPath('mainPane.toolBar.myProfileButton.value', state === Chililog.mainPaneStates.MY_ACCOUNT);
  }.observes('state')


});

