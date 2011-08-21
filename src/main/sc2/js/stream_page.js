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
// Javascript for index.html
//

// --------------------------------------------------------------------------------------------------------------------
// Views
// --------------------------------------------------------------------------------------------------------------------
App.ErrorMessage = SC.View.extend({
  classNames: 'ui-state-error ui-corner-all error'.w(),
  messageBinding: 'App.pageData.errorMessage',
  isVisibleBinding: SC.Binding.from('App.pageData.errorMessage').oneWay().bool()
});

App.RepositorySelectOption = App.SelectOption.extend({
  labelBinding: '*content.displayNameOrName',
  valueBinding: '*content.name'
});

App.RepositoryField = SC.View.extend({
  classNames: 'field'.w(),

  label: '_stream.repository'.loc(),

  Data : App.SelectView.extend({
    content: [],
    contentBinding: 'App.pageData.repositoryOptions',
    itemViewClass: App.RepositorySelectOption,
    valueBinding: 'App.pageData.repository',
    disabledBinding: SC.Binding.from('App.pageData.isStreaming').oneWay().bool()
  })
});

App.SeverityField = SC.View.extend({
  classNames: 'field'.w(),

  label: '_stream.severity'.loc(),

  Data : App.SelectView.extend({
    content: [],
    contentBinding: 'App.pageData.severityOptions',
    valueBinding: 'App.pageData.severity',
    disabledBinding: SC.Binding.from('App.pageData.isStreaming').oneWay().bool()
  })
});

App.ActionButton = JQ.Button.extend({
  label: '_start'.loc(),

  didStreamingChange: function() {
    if (App.pageData.get('isStreaming')) {
      this.set('label', '_stop'.loc());
      this.$().addClass('ui-state-error');
    } else {
      this.set('label', '_start'.loc());
      this.$().removeClass('ui-state-error');
    }

  }.observes('App.pageData.isStreaming'),

  click: function() {
    if (App.pageData.get('isStreaming')) {
      App.pageData.set('isStreaming', NO);
      //App.statechart.sendAction('doStop');
    } else {
      App.pageData.set('isStreaming', YES);
      //App.statechart.sendAction('doStart');
    }
    return;
  }
});


App.pageData = SC.Object.create({
  /**
   * Value of the repository text field
   */
  repository: '',

  /**
   * Value of the severity text field
   */
  severity: '7',

  /**
   * Error message to display
   */
  errorMessage: '',

  /**
   * Indicates if we are currently streaming or not
   */
  isStreaming: NO,

  /**
   * Options for displaying in the repository dropdown
   */
  repositoryOptions: [],

  /**
   * Options for displaying in the severity dropdown
   */
  severityOptions: [
      SC.Object.create({label: '_repositoryEntryRecord.Severity.Emergency'.loc(), value: '0'}),
      SC.Object.create({label: '_repositoryEntryRecord.Severity.Action'.loc(), value: '1'}),
      SC.Object.create({label: '_repositoryEntryRecord.Severity.Critical'.loc(), value: '2'}),
      SC.Object.create({label: '_repositoryEntryRecord.Severity.Error'.loc(), value: '3'}),
      SC.Object.create({label: '_repositoryEntryRecord.Severity.Warning'.loc(), value: '4'}),
      SC.Object.create({label: '_repositoryEntryRecord.Severity.Notice'.loc(), value: '5'}),
      SC.Object.create({label: '_repositoryEntryRecord.Severity.Information'.loc(), value: '6'}),
      SC.Object.create({label: '_repositoryEntryRecord.Severity.Debug'.loc(), value: '7', selected: YES})
  ]

});


// --------------------------------------------------------------------------------------------------------------------
// States
// --------------------------------------------------------------------------------------------------------------------
App.statechart = SC.Statechart.create({

  rootState: SC.State.extend({

    initialSubstate: 'notStreaming',
  
    /**
     * Prompt the user to enter username and password
     */
    notStreaming: SC.State.extend({
      enterState: function() {
      },

      exitState: function() {
      },

      doStart: function() {
        App.pageData.set('errorMessage', '');
        this.gotoState('streaming');
      }
    }),

    /**
     * Lets the engine validates credentials with the server in an asynchronous manner.
     * The screen is disabled and a wait icon is displayed.
     */
    streaming: SC.State.extend({
      enterState: function() {
        App.pageData.set('isStreaming', YES);
        return;
      },

      /**
       * Asynchronous call back when we get a log entry
       *
       * @param {Object} callbackParams this is null because it is not set when login is called
       * @param {SC.Error} error object. null if no error
       */
      onReceiveLogEntry: function(logEntry, error) {
      },

      /**
       * Stop streaming
       */
      doStop: function() {
        App.pageData.set('errorMessage', '');
        this.gotoState('notStreaming');
      },

      exitState: function() {
        App.pageData.set('isStreaming', NO);
      }
    })
  })
});


// --------------------------------------------------------------------------------------------------------------------
// Start page processing
// --------------------------------------------------------------------------------------------------------------------
App.pageFileName = "stream.html";

if (App.sessionEngine.load()) {
  App.setupStandardPage(App.pageFileName);

  App.statechart.initStatechart();

  // Load repositories
  App.repositoryMetaInfoEngine.load();
  App.pageData.repositoryOptions = App.store.find(App.RepositoryMetaInfoRecord);
} else {
  // Not logged in so go to login page
  window.location = 'login.html?returnTo=' + encodeURIComponent(App.pageFileName);
}
