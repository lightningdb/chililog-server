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
  classNames: 'ui-state-error ui-corner-all error'.w(),
  messageBinding: 'App.pageData.errorMessage',
  isVisibleBinding: SC.Binding.from('App.pageData.errorMessage').oneWay().bool()
});

App.UsernameField = SC.View.extend({
  label: '_login.username'.loc(),

  Data : App.TextBoxView.extend({
    id: 'usernameData',
    valueBinding: 'App.pageData.username',
    name: 'username',
    tabindex: '1',
    disabledBinding: SC.Binding.from('App.pageData.isLoggingIn').oneWay().bool()
  })
});

App.PasswordField = SC.View.extend({
  label: '_login.password'.loc(),

  Data : App.TextBoxView.extend({
    valueBinding: 'App.pageData.password',
    type: 'password',
    name: 'password',
    tabindex: '2',
    disabledBinding: SC.Binding.from('App.pageData.isLoggingIn').oneWay().bool()
  })
});

App.LoginButton = JQ.Button.extend({
  label: '_login.login'.loc(),

  disabledBinding: SC.Binding.from('App.pageData.isLoggingIn').oneWay().bool(),

  click: function() {
    App.statechart.sendAction('doLogin');
    return;
  }
});

App.WorkingImage = App.ImgView.extend({
  src: 'images/working.gif',
  visible: NO,
  isVisibleBinding: SC.Binding.from('App.pageData.isLoggingIn').oneWay().bool()
});

App.pageData = SC.Object.create({
  /**
   * Value of the username text field
   */
  username: '',

  /**
   * Value of the password text field
   */
  password: '',

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
        App.pageData.set('errorMessage', '');

        // Check data first
        var username = App.pageData.get('username');
        if (SC.empty(username)) {
          App.pageData.set('errorMessage', '_login.username.required'.loc());
          $('#usernameData').focus();
          return;
        }

        var password = App.pageData.get('password');
        if (SC.empty(password)) {
          App.pageData.set('errorMessage', '_login.password.required'.loc());
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
        App.pageData.set('isLoggingIn', YES);

        var username = App.pageData.get('username');
        var password = App.pageData.get('password');

        App.sessionEngine.login(username, password, true, true,
          this, this.loginCallback, null);

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
          App.pageData.set('errorMessage', error.message);
          this.gotoState('loggedOut');
        }
      },

      exitState: function() {
        App.pageData.set('isLoggingIn', NO);
      }
    })

  })

});


// --------------------------------------------------------------------------------------------------------------------
// Start page processing
// --------------------------------------------------------------------------------------------------------------------
App.sessionEngine.load();
App.statechart.initStatechart();
