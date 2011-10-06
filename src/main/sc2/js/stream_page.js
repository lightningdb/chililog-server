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
 * @class
 * Error message view
 */
App.ErrorMessage = App.BlockMessageView.extend({
  messageType: 'error',
  messageBinding: 'App.pageController.errorMessage',
  isVisibleBinding: SC.Binding.from('App.pageController.errorMessage').oneWay().bool()
});

/**
 * @class
 * Specifies the properties of a SC.RepositoryStatusRecord that should be used for the select option label and value
 */
App.RepositorySelectOption = App.SelectOption.extend({
  labelBinding: '*content.displayNameOrName',
  valueBinding: '*content.name'
});

/**
 * @class
 * Repository field
 */
App.RepositoryField = App.StackedFieldView.extend({
  label: '_stream.repository'.loc(),

  DataView : App.SelectView.extend({
    content: [],
    contentBinding: 'App.pageController.repositoryOptions',
    itemViewClass: App.RepositorySelectOption,
    valueBinding: 'App.pageController.repository',
    disabledBinding: SC.Binding.from('App.pageController.isStreaming').oneWay().bool()
  })
});

/**
 * @class
 * Severity field
 */
App.SeverityField =App.StackedFieldView.extend({
  label: '_stream.severity'.loc(),

  DataView : App.SelectView.extend({
    content: [],
    contentBinding: 'App.pageController.severityOptions',
    valueBinding: 'App.pageController.severity',
    disabledBinding: SC.Binding.from('App.pageController.isStreaming').oneWay().bool()
  })
});

/**
 * @class
 * Source field
 */
App.SourceField = App.StackedFieldView.extend({
  label: '_stream.source'.loc(),

  DataView : App.TextBoxView.extend(App.CriteriaFieldDataMixin, {
    classNames: 'medium'.w(),
    valueBinding: 'App.pageController.source',
    name: 'source',
    disabledBinding: SC.Binding.from('App.pageController.isStreaming').oneWay().bool()
  })
});

/**
 * @class
 * Host field
 */
App.HostField = App.StackedFieldView.extend({
  label: '_stream.host'.loc(),

  DataView : App.TextBoxView.extend(App.CriteriaFieldDataMixin, {
    classNames: 'medium'.w(),
    valueBinding: 'App.pageController.host',
    name: 'host',
    disabledBinding: SC.Binding.from('App.pageController.isStreaming').oneWay().bool()
  })
});

/**
 * @class
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
 * @class
 * Button to clear the log entries on the page
 */
App.ClearButton = App.ButtonView.extend({
  label: '_clear'.loc(),

  /**
   * Remove all div in results and don't show the bottom buttons
   */
  click: function() {
    var rowCount = $('#results').find("div:gt(0)").remove();
    App.pageController.set('showActionButton2', NO);
  }
});

/**
 * @class
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
        { Timestamp: ts, Source: 'workbench', Host: 'local', Severity: '7', Message: 'Test DEBUG message sent from browser with all sorts of funny characters <test>!@#$%^&*()_+{}[]:";\'<>,.?/</test> ' + navigator.userAgent},
        { Timestamp: ts, Source: 'workbench', Host: 'local', Severity: '4', Message: 'Test WARNING message with a timestamp and a very long example of a java class path org.chililog.server.pubsub.websocket.AVeryLongClassName. The time is now ' + new Date() },
        { Timestamp: ts, Source: 'workbench', Host: 'local', Severity: '3', Message: 'Test ERROR message sent by ' + username}
      ]
    };

    try {
      var me = this;
      var webSocket = new App.WebSocket('ws://' + document.domain + ':61615/websocket');

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
 * @class
 * Mediates between state charts and views
 */
App.pageController = SC.Object.create({
  /**
   * Selected item of the repository field
   *
   * @type App.RepositoryStatusRecord
   */
  repository: null,

  /**
   * Selected item of the severity field
   *
   * @type SC.Object
   */
  severity: null,

  /**
   * Value of the source field
   *
   * @type String
   */
  source: '',

  /**
   * Value of the host field
   *
   * @type String
   */
  host: '',

  /**
   * Error message to display
   *
   * @type String
   */
  errorMessage: '',

  /**
   * Indicates if we are currently streaming or not
   *
   * @type Boolean
   */
  isStreaming: NO,

  /**
   * Maximum number of log entries displayed. If this is exceeded, the earliest entries are deleted
   *
   * @type int
   */
  maxRowsToDisplay: 1000,

  /**
   * Options for displaying in the repository dropdown
   *
   * @type SC.ArrayProxy of SC.RepositoryStatusRecord
   */
  repositoryOptions: SC.ArrayProxy.create(),
  
  /**
   * Options for displaying in the severity dropdown
   *
   * @type Array of SC.Object
   */
  severityOptions: [
    SC.Object.create({label: '_repositoryEntryRecord.Severity.Emergency'.loc(), value: '0'}),
    SC.Object.create({label: '_repositoryEntryRecord.Severity.Action'.loc(), value: '1'}),
    SC.Object.create({label: '_repositoryEntryRecord.Severity.Critical'.loc(), value: '2'}),
    SC.Object.create({label: '_repositoryEntryRecord.Severity.Error'.loc(), value: '3'}),
    SC.Object.create({label: '_repositoryEntryRecord.Severity.Warning'.loc(), value: '4'}),
    SC.Object.create({label: '_repositoryEntryRecord.Severity.Notice'.loc(), value: '5'}),
    SC.Object.create({label: '_repositoryEntryRecord.Severity.Information'.loc(), value: '6'}),
    SC.Object.create({label: '_repositoryEntryRecord.Severity.Debug'.loc(), value: '7', selected: YES })
  ],

  /**
   * Flag to indicate if we want the bottom bar to show or not
   *
   * @type Boolean
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

    var scDate = null;
    if (SC.empty(logEntry.Timestamp)) {
      scDate = SC.DateTime.create();
    } else {
      scDate = App.DateTime.parseChililogServerDateTime(logEntry.Timestamp);
    }

    var severity = parseInt(logEntry.Severity);
    var severityClassName = '';
    if (severity <= 3) {
      severityClassName = 'important';
    } else if (severity == 4 || severity == 5) {
      severityClassName = 'warning';
    }

    var formattedMessage = logEntry.Message;
    if (SC.empty(formattedMessage)) {
      formattedMessage = '&nbsp;';
    } else {
      // Markup
      formattedMessage = formattedMessage.replace(/&/g, '&amp;');
      formattedMessage = formattedMessage.replace(/</g, '&lt;');
      formattedMessage = formattedMessage.replace(/>/g, '&gt;');

      // Add spaces to break long lines
      if (logEntry.Message.length > 100) {
        formattedMessage = formattedMessage.replace(/([\.;:,_-])/g, '$1<wbr/>');
      }

      // Convert new lines
      formattedMessage = formattedMessage.replace(/\n/g, '<br/>');
    }

    var newLogEntryHtml = '<div class="logEntry">' +
      '<div class="row">' +
        '<div class="left">' + App.DateTime.toChililogLocalDateTime(scDate) + '</div>' +
        '<div class="right">' +
          formattedMessage +
          '<div class="rightFooter">' +
            '<span class="severity"><span class="label ' + severityClassName + '">severity:</span> ' + App.REPOSITORY_ENTRY_SEVERITY_MAP[severity] + '</span>' +
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
    var rows = $('#results > div');
    var rowCount = rows.length;
    var maxRowsToDisplay = App.pageController.get('maxRowsToDisplay');
    if (!App.pageController.get('showActionButton2') && rowCount > 1) {
      App.pageController.set('showActionButton2', YES);
    }
    if (rowCount > maxRowsToDisplay)
    {
      rows.slice(1, 11).remove();
    }
  }
});

/**
 * @class
 * Controls streaming.
 *
 * Rather than writing back to the page controller, write directly to DOM for speed.  We found that over > 1000 items,
 * binding slows everything down.
 *
 * Log entries are read-only data so binding is not so important because the data wont be changing
 */
App.streamingController = SC.Object.create({
  /**
   * Current Websocket being used to talk to the server
   *
   * @type WebSocket
   */
  webSocket: null,

  /**
   * Make web socket connection and wait for log entries to come down
   */
  startStreaming: function() {
    try {
      $('#results').css('display', 'block');
      var webSocket = new App.WebSocket('ws://' + document.domain + ':61615/websocket');

      webSocket.onopen = function () {
        SC.Logger.log('Socket opening');

        var request = {
          MessageType: 'SubscriptionRequest',
          MessageID: new Date().getTime() + '',
          RepositoryName: App.pageController.getPath('repository.name'),
          Source: App.pageController.getPath('source'),
          Host: App.pageController.getPath('host'),
          Severity: App.pageController.getPath('severity.value'),
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

if (!App.WebSocket) {
  App.pageController.set('errorMessage', 'Your browser does not support web sockets :-( Try using the latest version of Chrome or Safari');
}

if (App.sessionEngine.load()) {
  App.viewUtils.setupStandardPage(App.pageFileName);

  App.statechart.initStatechart();

  // Load repository status so as to setup the dropdown list
  App.repositoryRuntimeEngine.load(this, function() {
    var recordArray = App.repositoryRuntimeEngine.getRecords('currentStatus = "' + App.REPOSITORY_STATUS_ONLINE  +'"');
    App.pageController.get('repositoryOptions').set('content', recordArray);
    App.pageController.set('repository', App.pageController.get('repositoryOptions').get('firstObject'));
  }, null);

} else {
  // Not logged in so go to login page
  window.location = 'login.html?returnTo=' + encodeURIComponent(App.pageFileName);
}

   $('#dialog').dialog({
        autoOpen: false,
        width: 600,
        buttons: {
            "Ok": function() {
                $(this).dialog("close");
            },
            "Cancel": function() {
                $(this).dialog("close");
            }
        }
    });