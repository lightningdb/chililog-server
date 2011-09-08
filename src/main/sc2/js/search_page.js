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
App.ErrorMessage = SC.View.extend({
  messageBinding: 'App.pageController.errorMessage',
  isVisibleBinding: SC.Binding.from('App.pageController.errorMessage').oneWay().bool()
});

/**
 * @class
 * Common functions for field data
 */
App.FieldDataMixin = {

  // Search when ENTER clicked
  insertNewline: function() {
    App.statechart.sendAction('startSearch');
    return;
  }
};

/**
 * @class
 * Specifies the properties of a SC.RepositoryStatusRecord that should be used for the select option label and value
 */
App.RepositorySelectOption = App.SelectOption.extend({
  labelBinding: '*content.displayNameOrName',
  valueBinding: '*content.documentID'
});

/**
 * @class
 * Repository selection field
 */
App.RepositoryField = SC.View.extend({
  classNames: 'field'.w(),

  label: '_search.repository'.loc(),

  Data : App.SelectView.extend({
    content: [],
    contentBinding: 'App.pageController.repositoryOptions',
    itemViewClass: App.RepositorySelectOption,
    valueBinding: 'App.pageController.repository',
    disabledBinding: SC.Binding.from('App.pageController.isSearching').oneWay().bool()
  })
});

/**
 * @class
 * From date field.
 */
App.FromDateField = SC.View.extend({
  classNames: 'field'.w(),

  label: '_search.fromDate'.loc(),

  Data : App.TextBoxView.extend(App.FieldDataMixin, {
    valueBinding: 'App.pageController.fromDate',
    name: 'fromDate',
    placeholder: 'yyyy-mm-dd',
    disabledBinding: SC.Binding.from('App.pageController.isSearching').oneWay().bool(),

    /**
     * Attach date picker to text box
     */
    didInsertElement: function() {
      this._super();
      this.$().datepicker({ dateFormat: 'yy-mm-dd' });
    }
  })
});

/**
 * @class
 * From time field
 */
App.FromTimeField = SC.View.extend({
  classNames: 'field'.w(),

  label: '_search.fromTime'.loc(),

  Data : App.TextBoxView.extend(App.FieldDataMixin, {
    valueBinding: 'App.pageController.fromTime',
    name: 'fromTime',
    placeholder: 'hh:mm:ss',
    disabledBinding: SC.Binding.from('App.pageController.isSearching').oneWay().bool()
  })
});

/**
 * @class
 * To date field
 */
App.ToDateField = SC.View.extend({
  classNames: 'field'.w(),

  label: '_search.toDate'.loc(),

  Data : App.TextBoxView.extend(App.FieldDataMixin, {
    valueBinding: 'App.pageController.toDate',
    name: 'toDate',
    placeholder: 'yyyy-mm-dd',
    disabledBinding: SC.Binding.from('App.pageController.isSearching').oneWay().bool(),

    /**
     * Attach date picker to text box
     */
    didInsertElement: function() {
      this._super();
      this.$().datepicker({ dateFormat: 'yy-mm-dd' });
    }
  })
});

/**
 * @class
 * To time field.
 */
App.ToTimeField = SC.View.extend({
  classNames: 'field'.w(),

  label: '_search.toTime'.loc(),

  Data : App.TextBoxView.extend(App.FieldDataMixin, {
    valueBinding: 'App.pageController.toTime',
    name: 'toTime',
    placeholder: 'hh:mm:ss',
    disabledBinding: SC.Binding.from('App.pageController.isSearching').oneWay().bool()
  })
});

/**
 * @class
 * Source field
 */
App.SourceField = SC.View.extend({
  classNames: 'field'.w(),

  label: '_search.source'.loc(),

  Data : App.TextBoxView.extend(App.FieldDataMixin, {
    valueBinding: 'App.pageController.source',
    name: 'source',
    disabledBinding: SC.Binding.from('App.pageController.isSearching').oneWay().bool()
  })
});

/**
 * @class
 * Host field
 */
App.HostField = SC.View.extend({
  classNames: 'field'.w(),

  label: '_search.host'.loc(),

  Data : App.TextBoxView.extend(App.FieldDataMixin, {
    valueBinding: 'App.pageController.host',
    name: 'host',
    disabledBinding: SC.Binding.from('App.pageController.isSearching').oneWay().bool()
  })
});

/**
 * @class
 * Severity field
 */
App.SeverityField = SC.View.extend({
  classNames: 'field'.w(),

  label: '_search.severity'.loc(),

  Data : App.SelectView.extend({
    content: [],
    contentBinding: 'App.pageController.severityOptions',
    valueBinding: 'App.pageController.severity',
    disabledBinding: SC.Binding.from('App.pageController.isSearching').oneWay().bool()
  })
});

/**
 * @class
 * Timespan field
 */
App.TimespanField = SC.View.extend({
  classNames: 'field'.w(),

  label: '_search.timespan'.loc(),

  Data : App.SelectView.extend({
    content: [],
    contentBinding: 'App.pageController.timespanOptions',
    valueBinding: 'App.pageController.timespan',
    disabledBinding: SC.Binding.from('App.pageController.isSearching').oneWay().bool()
  })
});

/**
 * @class
 * Keywords field
 */
App.KeywordsField = SC.View.extend({
  classNames: 'field'.w(),

  label: '_search.keywords'.loc(),

  Data : App.TextBoxView.extend(App.FieldDataMixin, {
    valueBinding: 'App.pageController.keywords',
    name: 'keywords',
    disabledBinding: SC.Binding.from('App.pageController.isSearching').oneWay().bool()
  })
});

/**
 * @class
 * Condition
 */
App.ConditionField = SC.View.extend({
  classNames: 'field'.w(),

  label: '_search.condition'.loc(),

  Data : App.TextAreaView.extend({
    valueBinding: 'App.pageController.condition',
    disabledBinding: SC.Binding.from('App.pageController.isSearching').oneWay().bool()
  })
});

/**
 * @class
 * Button to start search
 */
App.SearchButton = App.ButtonView.extend({
  disabledBinding: SC.Binding.from('App.pageController.isSearching').oneWay().bool(),

  label: '_search'.loc(),

  click: function() {
    App.statechart.sendAction('startSearch');
    return;
  }
});

/**
 * @class
 * Button to retrieve more rows from the server
 */
App.ShowMoreButton = App.ButtonView.extend({
  disabledBinding: SC.Binding.from('App.pageController.isSearching').oneWay().bool(),

  label: '_search.showMore'.loc(),

  click: function() {
    App.statechart.sendAction('doShowMore');
    return;
  }
});

/**
 * @class
 * Button to show advanced criteria
 */
App.AdvancedButton = App.ButtonView.extend({
  disabledBinding: SC.Binding.from('App.pageController.isSearching').oneWay().bool(),
  isVisibleBinding: SC.Binding.from('App.pageController.showAdvancedCriteria').oneWay().bool().not(),

  label: '_search.advancedCriteria'.loc(),

  click: function() {
    App.pageController.set('showAdvancedCriteria', YES);
    //App.pageController.set('timespan', App.pageController.get('timespanOptions')[0]);
    return;
  }
});

/**
 * @class
 * DIV for advanced criteria
 */
App.AdvancedCriteria = SC.View.extend({
  isVisibleBinding: SC.Binding.from('App.pageController.showAdvancedCriteria').oneWay().bool()
});

/**
 * @class
 * Spinner displayed while searching
 */
App.WorkingImage = App.ImgView.extend({
  src: 'images/working.gif',
  visible: NO,
  isVisibleBinding: SC.Binding.from('App.pageController.isSearching').oneWay().bool()
});

/**
 * @class
 * View for displaying log entries returned from search
 */
App.LogEntryCollectionView = SC.CollectionView.extend({
  contentBinding: 'App.pageController.logEntries',

  didInsertElement: function() {
    this._super();
    this.$("div:even").addClass("odd");
  },

  itemViewClass: SC.View.extend({
    classNames: 'logEntry'.w()
  })
});

/**
 * @class
 * Container view for the ShowMore button
 */
App.BottomBar = SC.View.extend({
  isVisibleBinding: SC.Binding.from('App.pageController.canShowMore').oneWay().bool()
});

/**
 * @class
 * View displayed when when on rows found
 */
App.NoRowsView = SC.View.extend({
  isVisibleBinding: SC.Binding.from('App.pageController.rowsFound').oneWay().bool().not()
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
   * Value of the from date field
   *
   * @type String
   */
  fromDate: '',

  /**
   * Value of the from time field
   *
   * @type String
   */
  fromTime: '',

  /**
   * FROM Date and time combined
   *
   * @type String
   */
  fromDateTime: function() {
    var d = this.get('fromDate');
    if (SC.empty(d)) {
      return '';
    }
    var t = this.get('fromTime');
    if (SC.empty(t)) {
      t = "00:00:00";
    }
    return d + ' ' + t;
  }.property('fromDate, fromTime'),

  /**
   * TO Date and time combined
   *
   * @type String
   */
  toDateTime: function() {
    var d = this.get('toDate');
    if (SC.empty(d)) {
      return '';
    }
    var t = this.get('toTime');
    if (SC.empty(t)) {
      t = "23:59:59";
    }
    return d + ' ' + t;
  }.property('toDate, toTime'),

  /**
   * Value of the to date field
   *
   * @type String
   */
  toDate: '',

  /**
   * Value of the to time field
   *
   * @type String
   */
  toTime: '',

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
   * Selected item of the severity field
   *
   * @type SC.Object
   */
  severity: null,

  /**
   * Selected item of the timespan field
   *
   * @type SC.Object
   */
  timespan: null,

  /**
   * Value of the keywords field
   *
   * @type String
   */
  keywords: '',

  /**
   * JSON conditions
   *
   * @type String
   */
  condition: '',

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
  isSearching: NO,

  /**
   * Flag to indicate if row found or not
   *
   * @type Boolean
   */
  rowsFound: YES,

  /**
   * Flag to indicate if there are more rows to show
   *
   * @type Boolean
   */
  canShowMore: NO,

  /**
   * Options for displaying in the repository dropdown\
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
    SC.Object.create({label: '_repositoryEntryRecord.Severity.Debug'.loc(), value: '7', selected: YES})
  ],

  /**
   * Options for displaying in the timespan dropdown
   *
   * @type Array of SC.Object
   */
  timespanOptions: [
    SC.Object.create({label: '', value: ''}),
    SC.Object.create({label: '_search.timespan.5'.loc(), value: '5'}),
    SC.Object.create({label: '_search.timespan.15'.loc(), value: '15'}),
    SC.Object.create({label: '_search.timespan.30'.loc(), value: '30'}),
    SC.Object.create({label: '_search.timespan.60'.loc(), value: '60', selected: YES}),
    SC.Object.create({label: '_search.timespan.1440'.loc(), value: '1440'}),
    SC.Object.create({label: '_search.timespan.10080'.loc(), value: '10080'}),
    SC.Object.create({label: '_search.timespan.20160'.loc(), value: '20160'}),
    SC.Object.create({label: '_search.timespan.43200'.loc(), value: '43200'})
  ],

  /**
   * Number of rows to return per search
   *
   * @type int
   */
  rowsPerSearch: 30,

  /**
   * Maximum number of log entries to display
   *
   * @type int
   */
  maxRowsToDisplay: 1000,

  /**
   * Flag to indicate if we want to show advanced criteria
   *
   * @type Boolean
   */
  showAdvancedCriteria: NO,

  /**
   * Saved page criteria used when we show more records
   *
   * @type  String
   */
  previousSearchCriteria: null,

  /**
   * Arrary of log entry objects that have been displayed on the screen
   *
   * @type  Array of Objects
   */
  displayedLogEntries: [],

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
    if (logEntry.Timestamp === '') {
      scDate = SC.DateTime.create();
    } else {
      var d = new Date();
      var timezoneOffsetMinutes = d.getTimezoneOffset();
      scDate = SC.DateTime.parse(logEntry.ts, '%Y-%m-%dT%H:%M:%S.%s%Z');
      scDate.set('timezone', timezoneOffsetMinutes);
    }

    var severity = logEntry.severity;
    var severityClassName = 'severity';
    if (severity <= 3) {
      severityClassName = severityClassName + ' alert-message block-message error';
    } else if (severity == 4 || severity == 5) {
      severityClassName = severityClassName + ' alert-message block-message warning';
    }

    var formattedMessage = logEntry.messageWithKeywordsHilighted;
    if (SC.empty(formattedMessage)) {
      formattedMessage = '&nbsp;';
    } else {
      formattedMessage = formattedMessage.replace(/\n/g, '<br/>');
      if (formattedMessage.length > 100) {
        // Add spaces to break long lines (word-break not working in chrome)
        formattedMessage = formattedMessage.replace(/([^\s-]{20})/g, '$1&shy;');
      }
    }

    // Save log entry in our array
    var displayedLogEntries = App.pageController.get('displayedLogEntries');
    displayedLogEntries.push(logEntry);
    var displayedLogEntryIndex = displayedLogEntries.length - 1;

    var newLogEntryHtml = '<div class="logEntry" ondblclick="alert(' + displayedLogEntryIndex + ')">' +
      '<div class="row">' +
        '<div class="left">' + scDate.toFormattedString('%Y-%m-%d %H:%M:%S.%s %Z') + '</div>' +
        '<div class="right">' +
          formattedMessage +
          '<div class="rightFooter">' +
            '<span class="' + severityClassName + '"><span class="label">severity:</span> ' + App.REPOSITORY_ENTRY_SEVERITY_MAP[severity] + '</span>' +
            '<span class="divider">|</span>' +
            '<span class="host"><span class="label">host:</span> ' + logEntry.host + '</span>' +
            '<span class="divider">|</span>' +
            '<span class="source"><span class="label">source:</span> ' + logEntry.source + '</span>' +
          '</div>' +
        '</div>' +
      '</div>';

    $('#results').append(newLogEntryHtml);

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

// --------------------------------------------------------------------------------------------------------------------
// States
// --------------------------------------------------------------------------------------------------------------------
App.statechart = SC.Statechart.create({

  rootState: SC.State.extend({

    initialSubstate: 'notSearching',

    /**
     * Prompt the user to enter criteria
     */
    notSearching: SC.State.extend({
      enterState: function() {
      },

      exitState: function() {
      },

      startSearch: function() {
        App.pageController.set('errorMessage', '');
        this.gotoState('searching');
      },

      doShowMore: function() {
        App.pageController.set('errorMessage', '');
        this.gotoState('showingMore');
      }
    }),

    /**
     * Block the user from entering data while executing a search
     */
    searching: SC.State.extend({
      enterState: function() {
        App.pageController.set('isSearching', YES);

        // Run later to give time for working icon animation to run
        SC.run.later(this, this.startSearch, 100);
      },

      exitState: function() {
        App.pageController.set('isSearching', NO);
      },

      startSearch: function() {
        try {
          // Clear previous log entries
          var rows = $('#results > div');
          rows.slice(1).remove();
          App.pageController.get('displayedLogEntries').length = 0;

          // Validate
          var timespan = App.pageController.getPath('timespan.value');
          var fromDate = App.pageController.getPath('fromDate');
          var fromTime = App.pageController.getPath('fromTime');
          var toDate = App.pageController.getPath('toDate');
          var toTime = App.pageController.getPath('toTime');
          if (!SC.empty(timespan) && (!SC.empty(fromDate) || !SC.empty(fromTime) || !SC.empty(toDate) || !SC.empty(toTime))) {
            throw App.$error('_search.timeSpecifiedError', null, null);
          }
          this.checkDate(fromDate, '_search.fromDate.invalid', 'fromDateField');
          this.checkTime(fromTime, '_search.fromTime.invalid', 'fromTimeField');
          this.checkDate(toDate, '_search.toDate.invalid', 'toDateField');
          this.checkTime(toTime, '_search.toTime.invalid', 'toTimeField');

          // Turn conditions json into javascript objects
          var conditions = {};
          var conditionString = App.pageController.get('conditions');
          if (!SC.empty(conditionString)) {
            conditions = JSON.parse(conditionString);
          }

          // Make up time condition
          if (SC.none(conditions.ts) && !SC.empty(App.pageController.getPath('timespan.value'))) {
            var minutesAgo = parseInt(App.pageController.getPath('timespan.value')) * -1;
            conditions.ts = {
              '$gte': SC.DateTime.create().advance({minute: minutesAgo}).toChililogServerDateTime(),
              '$lte': SC.DateTime.create().toChililogServerDateTime()
            };
          }
          if (SC.none(conditions.ts)) {
            var parseFormat = '%Y-%m-%d %H:%M:%S';
            var from = App.pageController.get('fromDateTime');
            var to = App.pageController.get('toDateTime');
            if (!SC.empty(from) && !SC.empty(to)) {
              conditions.ts = {
                '$gte': SC.DateTime.parse(from, parseFormat).toChililogServerDateTime(),
                '$lte': SC.DateTime.parse(to, parseFormat).toChililogServerDateTime()
              };
              if (SC.DateTime.compare(conditions.ts['$gte'], conditions.ts['$lte']) > 0) {
                throw App.$error('_search.dateTimeRangeError', null, 'fromDateField');
              }
            } else if (!SC.empty(from)) {
              conditions.ts = {
                '$gte': SC.DateTime.parse(from, parseFormat).toChililogServerDateTime()
              };
            } else if (!SC.empty(to)) {
              conditions.ts = {
                '$lte': SC.DateTime.parse(to, parseFormat).toChililogServerDateTime()
              };
            }
          }

          // Source, host and severity
          if (SC.none(conditions.source)) {
            var source = App.pageController.get('source');
            if (!SC.empty(source)) {
              conditions.source = source;
            }
          }
          if (SC.none(conditions.host)) {
            var host = App.pageController.get('host');
            if (!SC.empty(host)) {
              conditions.host = host;
            }
          }
          if (SC.none(conditions.severity)) {
            var severity = App.pageController.getPath('severity.value');
            if (!SC.empty(severity)) {
              conditions.severity = parseInt(severity);
            }
          }

          // Final criteria
          var criteria = {
            documentID: App.pageController.getPath('repository.documentID'),
            conditions: conditions,
            keywordUsage: 'All',
            keywords: App.pageController.get('keywords'),
            startPage: 1,
            recordsPerPage: App.pageController.get('rowsPerSearch'),
            doPageCount: 'false'
          };
          App.repositoryRuntimeEngine.findLogEntries(criteria, this, this.endSearch);

          // Save criteria for show more
          App.pageController.set('previousSearchCriteria', criteria);

          // Clear table to signal to the user that we are searching
          App.pageController.set('results', null);
        }
        catch (err) {
          // End search with error
          this.endSearch(App.pageController.get('repository'), 0, null, err);
        }
      },

      checkDate: function (dateString, errorCode, fieldId) {
        if (SC.empty(dateString)) {
          return;
        }
        if (SC.DateTime.parse(dateString, '%Y-%m-%d') === null) {
          throw App.$error(errorCode, [dateString], fieldId);
        }
      },

      checkTime: function (timeString, errorCode, fieldId) {
        if (SC.empty(timeString)) {
          return;
        }
        try {
          var ss = timeString.split(':');
          if (ss.length != 3) {
            throw new SC.Error('err');
          }
          var hours = parseInt((ss[0]))
          if (hours < 0 || hours > 24) {
            throw new SC.Error('err');
          }
          var miniutes = parseInt((ss[1]))
          if (miniutes < 0 || miniutes > 60) {
            throw new SC.Error('err');
          }
          var seconds = parseInt((ss[2]))
          if (seconds < 0 || seconds > 60) {
            throw new SC.Error('err');
          }
        }
        catch (err) {
          throw App.$error(errorCode, [timeString], fieldId);
        }
      },

      /**
       * Called back when search returns
       * @param documentID
       * @param records
       * @param params
       * @param error
       */
      endSearch: function(documentID, records, params, error) {
        if (SC.none(error)) {
          var recordCount = SC.none(records) ? 0 : records.length;
          App.pageController.set('rowsFound', recordCount > 0);
          App.pageController.set('canShowMore', recordCount === App.pageController.get('rowsPerSearch'));
        } else {
          App.pageController.set('errorMessage', error);

          // Set focus on field if specified
          if (!SC.empty(error.errorFieldId)) {
            var dataElementId = error.errorFieldId.replace('Field', 'Data');
            setTimeout(function(){$('#' + dataElementId)[0].focus();}, 100);
          }
        }

        $('#results').css('display', recordCount > 0 ? 'block' : 'none');

        for (var i=0; i< recordCount; i++) {
          App.pageController.writeLogEntry(records[i]);
        }

        this.gotoState('notSearching');
      }
    }),

    showingMore: SC.State.extend({
      enterState: function() {
        App.pageController.set('isSearching', YES);
        this.startShowMore();
      },

      exitState: function() {
        App.pageController.set('isSearching', NO);
      },

      /**
       * Get more records from the server
       */
      startShowMore: function() {
        var criteria = App.pageController.get('previousSearchCriteria');
        criteria.startPage = criteria.startPage + 1;
        App.repositoryRuntimeEngine.find(criteria, this, this.endShowMore);
      },

      /**
       * Called back when search returns
       * @param documentID
       * @param records
       * @param params
       * @param error
       */
      endShowMore: function(documentID, records, params, error) {
        if (SC.none(error)) {
          var recordCount = SC.none(records) ? 0 : records.length;
          App.pageController.set('rowsFound', recordCount > 0);
          App.pageController.set('canShowMore', recordCount === App.pageController.get('rowsPerSearch'));
        } else {
          App.pageController.set('errorMessage', error);
        }

        $('#results').css('display', recordCount > 0 ? 'block' : 'none');

        for (var i=0; i< recordCount; i++) {
          App.pageController.writeLogEntry(records[i]);
        }

        this.gotoState('notSearching');
      }

    })
  })
});

// --------------------------------------------------------------------------------------------------------------------
// Start page processing
// --------------------------------------------------------------------------------------------------------------------
App.pageFileName = Auth.getPageName();

if (App.sessionEngine.load()) {
  App.setupStandardPage(App.pageFileName);

  App.statechart.initStatechart();

  // Load repository status so as to setup the dropdown list
  App.repositoryRuntimeEngine.load(this, function() {
    var recordArray = App.repositoryRuntimeEngine.getRecords();
    App.pageController.get('repositoryOptions').set('content', recordArray);
    App.pageController.set('repository', App.pageController.get('repositoryOptions').get('firstObject'));
  }, null);

} else {
  // Not logged in so go to login page
  window.location = 'login.html?returnTo=' + encodeURIComponent(App.pageFileName);
}


