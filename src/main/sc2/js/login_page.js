//
// Copyright 2010 Cinch Logic Pty Ltd.
//
// http://www.chililog.com
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//

//
// Javascript for login.html
//

// --------------------------------------------------------------------------------------------------------------------
// Views
// --------------------------------------------------------------------------------------------------------------------
App.ErrorMessage = SC.View.extend({
  messageBinding: 'App.pageController.errorMessage',
  isVisibleBinding: SC.Binding.from('App.pageController.errorMessage').oneWay().bool()
});

/**
 * Common functions for field data
 */
App.FieldDataMixin = {

  // Login when ENTER clicked
  insertNewline: function() {
    App.statechart.sendAction('doLogin');
    return;
  }
};

App.UsernameField = SC.View.extend({
  label: '_login.username'.loc(),

  Data : App.TextBoxView.extend(App.FieldDataMixin, {
    valueBinding: 'App.pageController.username',
    name: 'username',
    tabindex: '1',
    disabledBinding: SC.Binding.from('App.pageController.isLoggingIn').oneWay().bool(),
    didInsertElement: function() {
      this.$().focus();
    }

  })
});

App.PasswordField = SC.View.extend({
  label: '_login.password'.loc(),

  Data : App.TextBoxView.extend(App.FieldDataMixin, {
    valueBinding: 'App.pageController.password',
    type: 'password',
    name: 'password',
    tabindex: '1',
    disabledBinding: SC.Binding.from('App.pageController.isLoggingIn').oneWay().bool()
  })
});

App.RememberMeField = SC.View.extend({

  Data : App.CheckboxView.extend(App.FieldDataMixin, {
    title: '_login.rememberMe'.loc(),
    valueBinding: 'App.pageController.rememberMe',
    tabindex: '1',
    disabledBinding: SC.Binding.from('App.pageController.isLoggingIn').oneWay().bool()
  })
});

App.LoginButton = App.ButtonView.extend({
  label: '_login.login'.loc(),

  disabledBinding: SC.Binding.from('App.pageController.isLoggingIn').oneWay().bool(),

  click: function() {
    App.statechart.sendAction('doLogin');
    return;
  }
});

App.WorkingImage = App.ImgView.extend({
  src: 'images/working.gif',
  visible: NO,
  isVisibleBinding: SC.Binding.from('App.pageController.isLoggingIn').oneWay().bool()
});

App.pageController = SC.Object.create({
  /**
   * Value of the username text field
   */
  username: '',

  /**
   * Value of the password text field
   */
  password: '',

  /**
   * Remember the user on this computer for 14 days
   */
  rememberMe: NO,

  /**
   * Error message to display
   */
  errorMessage: '',

  /**
   * Indicates if we are currently logging in
   */
  isLoggingIn: NO
});

// --------------------------------------------------------------------------------------------------------------------
// States
// --------------------------------------------------------------------------------------------------------------------
App.statechart = SC.Statechart.create({

  rootState: SC.State.extend({

    initialSubstate: 'loggedOut',

    /**
     * Prompt the user to enter username and password
     */
    loggedOut: SC.State.extend({
      enterState: function() {
      },

      exitState: function() {
      },

      doLogin: function() {
        App.pageController.set('errorMessage', '');

        // Check data first
        var username = App.pageController.get('username');
        if (SC.empty(username)) {
          App.pageController.set('errorMessage', '_login.username.required'.loc());
          $('#usernameData').focus();
          return;
        }

        var password = App.pageController.get('password');
        if (SC.empty(password)) {
          App.pageController.set('errorMessage', '_login.password.required'.loc());
          $('#passwordData').focus();
          return;
        }

        // Let engine talk to server.
        this.gotoState('loggingIn');
      }
    }),

    /**
     * Lets the engine validates credentials with the server in an asynchronous manner.
     * The screen is disabled and a wait icon is displayed.
     */
    loggingIn: SC.State.extend({
      enterState: function() {
        App.pageController.set('isLoggingIn', YES);

        var username = App.pageController.get('username');
        var password = App.pageController.get('password');
        var rememberMe = App.pageController.get('rememberMe');

        App.sessionEngine.login(username, password, rememberMe, this, this.loginCallback, null);

        return;
      },

      /**
       * Asynchronous call back from sessionEngine.login().
       *
       * If successful, we redirect to the index.html page or the page specified in the returnTo query string
       * parameter.
       * 
       * @param {Object} callbackParams this is null because it is not set when login is called
       * @param {SC.Error} error object. null if no error
       */
      loginCallback: function(callbackParams, error) {
        if (SC.none(error)) {
          // Redirect
          var qs = App.getQueryStringHash();
          var returnTo = qs['returnTo'];
          if (SC.empty(returnTo)) {
            window.location = 'index.html';
          } else {
            window.location = returnTo;
          }
        } else {
          // Error so back to the logged out state to show error
          App.pageController.set('errorMessage', error.message);
          this.gotoState('loggedOut');
        }
      },

      exitState: function() {
        App.pageController.set('isLoggingIn', NO);
      }
    })

  })

});


// --------------------------------------------------------------------------------------------------------------------
// Start page processing
// --------------------------------------------------------------------------------------------------------------------
App.sessionEngine.load(NO);
App.statechart.initStatechart();
