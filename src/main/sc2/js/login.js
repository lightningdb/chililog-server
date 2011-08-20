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

// --------------------------------------------------------------------------------------------------------------------
// Views
// --------------------------------------------------------------------------------------------------------------------
App.UsernameTextField = App.TextField.extend({
  valueBinding: 'App.loginViewController.username',
  name: 'username',
  tabindex: '1',
  disabledBinding: SC.Binding.from('App.loginViewController.isLoggingIn').oneWay().bool()
});

App.PasswordTextField = App.TextField.extend({
  valueBinding: 'App.loginViewController.password',
  type: 'password',
  name: 'password',
  tabindex: '2',
  disabledBinding: SC.Binding.from('App.loginViewController.isLoggingIn').oneWay().bool()
});

App.LoginButton = JQ.Button.extend({

  disabledBinding: SC.Binding.from('App.loginViewController.isLoggingIn').oneWay().bool(),

  click: function() {
    App.statechart.sendAction('login');
    return false;
  }
});

App.WorkingImage = App.ImgView.extend({
  visible: NO,
  visibleBinding: SC.Binding.from('App.loginViewController.isLoggingIn').oneWay().bool()
});

App.loginViewController = SC.Object.create({
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

      login: function() {
        this.gotoState('loggingIn');
      }
    }),

    /**
     * Validate credentials with the server
     */
    loggingIn: SC.State.extend({
      enterState: function() {
        App.loginViewController.set('isLoggingIn', YES);

        var username =  App.loginViewController.get('username');
        var password =  App.loginViewController.get('password');

        return;
      },

      exitState: function() {
        App.loginViewController.set('isLoggingIn', NO);
      }
    })

  })

});


// --------------------------------------------------------------------------------------------------------------------
// Start
// --------------------------------------------------------------------------------------------------------------------
App.statechart.initStatechart();
