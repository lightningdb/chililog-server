// ==========================================================================
// Project:   Chililog
// Copyright: ©2011 My Company, Inc.
// ==========================================================================

/**
 * Root state chart for the application dealing with login/logout
 */
Chililog.statechart = SC.Statechart.create({

  rootState: SC.State.design({

    initialSubstate: 'startup',

    /**
     * A nothing state to handle starting up
     */
    startup: SC.State.design({

    }),

    /**
     * Handles the login page
     */
    loggedOut: SC.State.design({

      enterState: function() {
        var ctrl = Chililog.loginViewController;
        ctrl.set('isLoggingIn', NO);

        Chililog.getPath('loginPage.loginPane').append();
      },

      exitState: function() {
        Chililog.getPath('loginPage.loginPane').remove();
      },

      login: function() {
        this.gotoState('loggingIn');
      }
    }),

    /**
     * Transitional state while we are logging in
     */
    loggingIn: SC.State.design({

      enterState: function() {
        Chililog.loginViewController.set('isLoggingIn', YES);
        this.login();
      },

      exitState: function() {
        Chililog.loginViewController.set('isLoggingIn', NO);
      },

      login: function() {
        var ctrl = Chililog.loginViewController;
        try {
          var username = ctrl.get('username');
          var password = ctrl.get('password');
          var rememberMe = ctrl.get('rememberMe');

          ctrl.set('isLoggingIn', YES);
          ctrl.set('error', null);

          Chililog.sessionDataController.login(username, password, rememberMe, YES, this, this.endLogin);
        }
        catch (err) {
          ctrl.set('error', err);
          this.gotoState('loggedOut');
        }
      },

      endLogin: function(error) {
        var ctrl = Chililog.loginViewController;

        // Finish login
        ctrl.set('isLoggingIn', NO);

        if (SC.none(error)) {
          // Clear error data and password
          ctrl.set('password', '');
          ctrl.set('error', null);

          // Show main page
          this.gotoState('loggedIn');
        } else {
          // Show error
          ctrl.set('error', error);

          // Go back to logged out
          this.gotoState('loggedOut');
        }
      }

    }),

    /**
     * Handles the main page
     */
    loggedIn: SC.State.design({

      initialSubstate: 'search',

      enterState: function() {
        Chililog.mainViewController.buildMenuOptions();
        Chililog.getPath('mainPage.mainPane').append();
      },

      exitState: function() {
        Chililog.getPath('mainPage.mainPane').remove();
      },

      search: SC.State.design({
        enterState: function() {
          Chililog.mainPage.setPath('mainPane.toolBar.menuOptions.value', 'search');
          var body = Chililog.mainPage.getPath('mainPane.body');
          body.set('nowShowing', 'Chililog.searchView');
        }
      }),

      analyse: SC.State.design({
        enterState: function() {
          Chililog.mainPage.setPath('mainPane.toolBar.menuOptions.value', 'analyse');
          var body = Chililog.mainPage.getPath('mainPane.body');
          body.set('nowShowing', 'Chililog.searchView');
        }
      }),

      monitor: SC.State.design({
        enterState: function() {
          Chililog.mainPage.setPath('mainPane.toolBar.menuOptions.value', 'monitor');
          var body = Chililog.mainPage.getPath('mainPane.body');
          body.set('nowShowing', 'Chililog.searchView');
        }
      }),

      configure: SC.State.design({
        enterState: function() {
          Chililog.mainPage.setPath('mainPane.toolBar.menuOptions.value', 'configure');
          var body = Chililog.mainPage.getPath('mainPane.body');
          body.set('nowShowing', 'Chililog.configureView');
        }
      }),

      about: SC.State.design({
        enterState: function() {
          Chililog.mainPage.setPath('mainPane.toolBar.menuOptions.value', 'about');
          var body = Chililog.mainPage.getPath('mainPane.body');
          body.set('nowShowing', 'Chililog.aboutView');
        }
      }),

      myAccount: SC.State.plugin('Chililog.MyAccountState'),
      
      showSearch: function() {
        this.gotoState('search');
      },

      showAnalysis: function() {
        this.gotoState('analyse');
      },

      showMonitors: function() {
        this.gotoState('monitor');
      },

      showConfigure: function() {
        this.gotoState('configure');
      },

      showMyAccount: function() {
        this.gotoState('myAccount');
      },

      showAbout: function() {
        this.gotoState('about');
      },

      logout: function() {
        Chililog.sessionDataController.logout();
        this.gotoState('loggedOut');
      }

    })

  })

});