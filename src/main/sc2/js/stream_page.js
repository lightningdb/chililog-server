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
/**
 * Error messages
 */
App.ErrorMessage = SC.View.extend({
  messageBinding: 'App.pageController.errorMessage',
  isVisibleBinding: SC.Binding.from('App.pageController.errorMessage').oneWay().bool()
});

/**
 * Repository options
 */
App.RepositorySelectOption = App.SelectOption.extend({
  labelBinding: '*content.displayNameOrName',
  valueBinding: '*content.name'
});

/**
 * Repository
 */
App.RepositoryField = SC.View.extend({
  classNames: 'field'.w(),

  label: '_stream.repository'.loc(),

  Data : App.SelectView.extend({
    content: [],
    contentBinding: 'App.pageController.repositoryOptions',
    itemViewClass: App.RepositorySelectOption,
    valueBinding: 'App.pageController.repository',
    disabledBinding: SC.Binding.from('App.pageController.isStreaming').oneWay().bool()
  })
});

/**
 * Severity filter
 */
App.SeverityField = SC.View.extend({
  classNames: 'field'.w(),

  label: '_stream.severity'.loc(),

  Data : App.SelectView.extend({
    content: [],
    contentBinding: 'App.pageController.severityOptions',
    valueBinding: 'App.pageController.severity',
    disabledBinding: SC.Binding.from('App.pageController.isStreaming').oneWay().bool()
  })
});

/**
 * Button to start/stop streaming
 */
App.ActionButton = App.ButtonView.extend({
  label: '_start'.loc(),

  didStreamingChange: function() {
    if (App.pageController.get('isStreaming')) {
      this.set('label', '_stop'.loc());
      this.$().addClass('danger');
      this.$().removeClass('primary');
    } else {
      this.set('label', '_start'.loc());
      this.$().addClass('primary');
      this.$().removeClass('danger');
    }

  }.observes('App.pageController.isStreaming'),

  click: function() {
    if (App.pageController.get('isStreaming')) {
      App.pageController.set('isStreaming', NO);
      App.statechart.sendAction('doStop');
    } else {
      App.pageController.set('isStreaming', YES);
      App.statechart.sendAction('doStart');
    }
    return;
  }
});

/**
 * Button to clear the log entries on the page
 */
App.ClearButton = App.ButtonView.extend({
  label: '_clear'.loc(),

  /**
   * Remove all div in results and don't show the bottom buttons
   */
  click: function() {
    var rowCount = $('#results div').remove();
    App.pageController.set('showActionButton2', NO);
  }
});

/**
 * Click to send test messages to the server
 */
App.TestMessageButton = App.ButtonView.extend({
  label: '_stream.test'.loc(),

  click: function() {
    this.set('disabled', YES);

    var username = App.sessionEngine.getPath('loggedInUser.username');
    var scDate = SC.DateTime.create({ timezone: 0});
    var ts = scDate.toFormattedString('%Y-%m-%dT%H:%M:%S.%sZ');
    var request = {
      MessageType: 'PublicationRequest',
      MessageID: new Date().getTime() + '',
      RepositoryName: App.pageController.getPath('repository.name'),
      Username: App.sessionEngine.getPath('loggedInUser.username'),
      Password: 'token:' + App.sessionEngine.get('authenticationToken'),
      LogEntries: [
        { Timestamp: ts, Source: 'workbench', Host: 'local', Severity: '7', Message: 'Test DEBUG message sent from browser ' + navigator.userAgent},
        { Timestamp: ts, Source: 'workbench', Host: 'local', Severity: '4', Message: 'Test WARNING message with a timestamp ' + new Date() },
        { Timestamp: ts, Source: 'workbench', Host: 'local', Severity: '3', Message: 'Test ERROR message sent by ' + username}
      ]
    };

    try {
      var me = this;
      var webSocket = new WebSocket('ws://' + document.domain + ':61615/websocket');

      webSocket.onopen = function () {
        SC.Logger.log('Test Socket opening');
        var requestJSON = JSON.stringify(request);

        // Sent it
        webSocket.send(requestJSON);
      };

      webSocket.onmessage = function (evt) {
        SC.Logger.log('Socket received message: ' + evt.data);
        try {
          var response = JSON.parse(evt.data);
          if (!response.Success) {
            response.LogEntry = { Timestamp: '', Source: 'Chililog', Host: 'Chililog', Severity: '3', Message: response.ErrorMessage };
            App.pageController.writeLogEntry(response.LogEntry, true);
          }

          // Close after we get a response so that button is enabled and the user can send another message
          webSocket.close();
        }
        catch (exception) {
          SC.Logger.log('Error parsing log entry. ' + exception);
        }
      };

      webSocket.onclose = function (evt) {
        SC.Logger.log('Test Socket close');
        me.set('disabled', NO); //cannot use this because it is the websocket
      };

      webSocket.onerror = function (evt) {
        SC.Logger.log('Test Socket error: ' + evt.data);
        var logEntry = { Timestamp: '', Source: 'Chililog', Host: 'Chililog', Severity: '3', Message: evt.data };
        App.pageController.writeLogEntry(logEntry, true);
      };
    } catch (exception) {
      SC.Logger.log('Test Socket error: ' + evt.data);
      var logEntry = { Timestamp: '', Source: 'Chililog', Host: 'Chililog', Severity: '3', Message: exception };
      App.pageController.writeLogEntry(logEntry, true);
    }

    return;
  }
});

// --------------------------------------------------------------------------------------------------------------------
// Controllers
// --------------------------------------------------------------------------------------------------------------------

/**
 * Mediates between state charts and views
 */
App.pageController = SC.Object.create({
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
  errorMessage: 'dasdfasfsdf',

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
  ],

  /**
   * Flag to indicate if we want the bottom bar to show or not
   */
  showActionButton2: NO,

  /**
   * Writes a log entry to the results area
   * @param {Object} logEntry data to write to page
   * @param {Boolean} doSeverityCheck YES to perform severity check to decide if log entry is to be dispalyed or not
   */
  writeLogEntry: function (logEntry, doSeverityCheck) {
    if (logEntry == null) {
      return;
    }

    var severity = parseInt(logEntry.Severity);
    var maxSeverity = parseInt(App.pageController.getPath('severity.value'));
    if (doSeverityCheck && severity > maxSeverity) {
      return;
    }

    var scDate = null;
    if (logEntry.Timestamp === '') {
      scDate = SC.DateTime.create();
    } else {
      var d = new Date();
      var timezoneOffsetMinutes = d.getTimezoneOffset();
      scDate = SC.DateTime.parse(logEntry.Timestamp, '%Y-%m-%dT%H:%M:%S.%s%Z');
      scDate.set('timezone', timezoneOffsetMinutes);
    }

    var severityClassName = 'severity';
    if (severity <= 3) {
      severityClassName = severityClassName + ' alert-message block-message error';
    } else if (severity == 4 || severity == 5) {
      severityClassName = severityClassName + ' alert-message block-message warning';
    }

    var newLogEntryHtml = '<div class="logEntry">' +
      '<div class="row">' +
        '<div class="left">' + scDate.toFormattedString('%Y-%m-%d %H:%M:%S %Z') + '</div>' +
        '<div class="right">' +
          logEntry.Message +
          '<div class="rightFooter">' +
            '<span class="' + severityClassName + '"><span class="label">severity:</span> ' + App.REPOSITORY_ENTRY_SEVERITY_MAP[severity] + '</span>' +
            '<span class="divider">|</span>' +
            '<span class="host"><span class="label">host:</span> ' + logEntry.Host + '</span>' +
            '<span class="divider">|</span>' +
            '<span class="source"><span class="label">source:</span> ' +logEntry.Source + '</span>' +
          '</div>' +
        '</div>' +
      '</div>';
    
    $('#results').append(newLogEntryHtml);
    window.scrollTo(0, document.body.scrollHeight);

    // Check if we want to show the bottom buttons ...
    var rowCount = $('#results div').length;
    if (!App.pageController.get('showActionButton2') && rowCount > 1) {
      App.pageController.set('showActionButton2', YES);
    }
  }
});

/**
 * Controls streaming.
 *
 * Rather than writing back to the page controller, write directly to DOM for speed.
 */
App.streamingController = SC.Object.create({
  /**
   * Websocket that we use to talk to the server
   */
  webSocket: null,

  /**
   * Make web socket connection and wait for log entries to come down
   */
  startStreaming: function() {
    try {
      $('#results').css('display', 'block');
      var webSocket = new WebSocket('ws://' + document.domain + ':61615/websocket');

      webSocket.onopen = function () {
        SC.Logger.log('Socket opening');

        var request = {
          MessageType: 'SubscriptionRequest',
          MessageID: new Date().getTime() + '',
          RepositoryName: App.pageController.getPath('repository.name'),
          Username: App.sessionEngine.getPath('loggedInUser.username'),
          Password: 'token:' + App.sessionEngine.get('authenticationToken')
        };
        var requestJSON = JSON.stringify(request);
        webSocket.send(requestJSON);

        App.pageController.writeLogEntry({
          Timestamp: '',
          Source: 'workbench',
          Host: 'local',
          Severity: '6',
          Message: 'Started.  Waiting for messages ...'
        }, false);
      };

      webSocket.onmessage = function (evt) {
        SC.Logger.log('Socket received message: ' + evt.data);
        try {
          var response = JSON.parse(evt.data);
          if (!response.Success) {
            // Turn our error into a message for display
            response.LogEntry = {
              Timestamp: '',
              Source: 'Chililog',
              Host: 'Chililog',
              Severity: '3',
              Message: response.ErrorMessage
            };
          }
          App.pageController.writeLogEntry(response.LogEntry, true);
        }
        catch (exception) {
          SC.Logger.log('Error parsing log entry. ' + exception);
        }
      };

      webSocket.onclose = function (evt) {
        SC.Logger.log('Socket close');
        App.pageController.writeLogEntry({
          Timestamp: '',
          Source: 'workbench',
          Host: 'local',
          Severity: '6',
          Message: 'Stopped'
        }, false);
      };

      webSocket.onerror = function (evt) {
        SC.Logger.log('Socket error: ' + evt.data);
      };

      App.streamingController.set('webSocket', webSocket);
    } catch (exception) {
      App.pageController.errorMessage = exception;
      this.gotoState('notStreaming');
    }
  },

  /**
   * Stop streaming
   */
  stopStreaming: function() {
    try {
      App.streamingController.get('webSocket').close();
    } catch (exception) {
      SC.Logger.log('Error closing web socket: ' + exception);
    }
  }
});

// --------------------------------------------------------------------------------------------------------------------
// States
// --------------------------------------------------------------------------------------------------------------------
App.statechart = SC.Statechart.create({

  rootState: SC.State.extend({

    initialSubstate: 'notStreaming',

    /**
     * Prompt the user for criteria
     */
    notStreaming: SC.State.extend({
      enterState: function() {
      },

      exitState: function() {
      },

      doStart: function() {
        App.pageController.set('errorMessage', '');
        this.gotoState('streaming');
      }
    }),

    /**
     * Startup web socket and wait for incoming messages
     */
    streaming: SC.State.extend({
      enterState: function() {
        App.pageController.set('isStreaming', YES);
        App.streamingController.startStreaming();
      },

      /**
       * Stop streaming
       */
      doStop: function() {
        App.pageController.set('errorMessage', '');
        this.gotoState('notStreaming');
      },

      exitState: function() {
        App.streamingController.stopStreaming();
        App.pageController.set('isStreaming', NO);
      }
    })
  })
});

// --------------------------------------------------------------------------------------------------------------------
// Start page processing
// --------------------------------------------------------------------------------------------------------------------
App.pageFileName = "stream.html";

if (!window.WebSocket) {
  App.pageController.set('errorMessage', 'Your browser does not support web sockets :-( Try using the latest version of Chrome or Safari');
}

if (App.sessionEngine.load()) {
  App.setupStandardPage(App.pageFileName);

  App.statechart.initStatechart();

  // Load repositories
  App.repositoryMetaInfoEngine.load(this, function() {
    var query = SC.Query.local(App.RepositoryMetaInfoRecord, {
      conditions: 'name != "chililog"',
      orderBy: 'name'
    });
    var arrayProxy = App.store.find(query);
    App.pageController.set('repositoryOptions', arrayProxy);
  }, null);

} else {
  // Not logged in so go to login page
  window.location = 'login.html?returnTo=' + encodeURIComponent(App.pageFileName);
}
