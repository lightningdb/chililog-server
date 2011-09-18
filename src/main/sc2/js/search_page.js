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
 * Common functions for field data
 */
App.CriteriaFieldDataMixin = {

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
App.RepositoryField = App.StackedFieldView.extend({
  label: '_search.repository'.loc(),

  DataView : App.SelectView.extend({
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
App.FromDateField = App.StackedFieldView.extend({
  label: '_search.fromDate'.loc(),

  DataView : App.TextBoxView.extend(App.CriteriaFieldDataMixin, {
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
App.FromTimeField = App.StackedFieldView.extend({
  label: '_search.fromTime'.loc(),

  DataView : App.TextBoxView.extend(App.CriteriaFieldDataMixin, {
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
App.ToDateField = App.StackedFieldView.extend({
  label: '_search.toDate'.loc(),

  DataView : App.TextBoxView.extend(App.CriteriaFieldDataMixin, {
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
App.ToTimeField = App.StackedFieldView.extend({
  label: '_search.toTime'.loc(),

  DataView : App.TextBoxView.extend(App.CriteriaFieldDataMixin, {
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
App.SourceField = App.StackedFieldView.extend({
  label: '_search.source'.loc(),

  DataView : App.TextBoxView.extend(App.CriteriaFieldDataMixin, {
    valueBinding: 'App.pageController.source',
    name: 'source',
    disabledBinding: SC.Binding.from('App.pageController.isSearching').oneWay().bool()
  })
});

/**
 * @class
 * Host field
 */
App.HostField = App.StackedFieldView.extend({
  label: '_search.host'.loc(),

  DataView : App.TextBoxView.extend(App.CriteriaFieldDataMixin, {
    valueBinding: 'App.pageController.host',
    name: 'host',
    disabledBinding: SC.Binding.from('App.pageController.isSearching').oneWay().bool()
  })
});

/**
 * @class
 * Severity field
 */
App.SeverityField = App.StackedFieldView.extend({
  label: '_search.severity'.loc(),

  DataView : App.SelectView.extend({
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
App.TimespanField = App.StackedFieldView.extend({
  label: '_search.timespan'.loc(),

  DataView : App.SelectView.extend({
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
App.KeywordsField = App.StackedFieldView.extend({
  label: '_search.keywords'.loc(),

  DataView : App.TextBoxView.extend(App.CriteriaFieldDataMixin, {
    valueBinding: 'App.pageController.keywords',
    name: 'keywords',
    disabledBinding: SC.Binding.from('App.pageController.isSearching').oneWay().bool()
  })
});

/**
 * @class
 * Condition
 */
App.ConditionField = App.StackedFieldView.extend({
  label: '_search.condition'.loc(),

  DataView : App.TextAreaView.extend({
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
    App.statechart.sendAction('showMore');
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
App.NoRowsView = App.BlockMessageView.extend({
  messageType: 'warning',
  message: '_search.noRowsFound'.loc(),
  isVisibleBinding: SC.Binding.from('App.pageController.rowsFound').oneWay().bool().not()
});


/**
 * @class
 * Dialog div
 */
App.Dialog = SC.View.extend({
  attributeBindings: ['title'],
  
  title: '_search.logEntries'.loc(),

  didInsertElement: function() {
    this._super();

    // JQuery UI dialog setup
    this.$().dialog({
        autoOpen: false,
        height: 620,
        width: 690,
        resizable: false,
        modal: true,
        close: function(event, ui) {
          // For when the X is clicked
          App.statechart.sendAction('hideDialog');
        }
    });
  }
});

/**
 * @class
 * Common functions for field data
 */
App.DialogFieldDataMixin = {

  // Hide dialog on ENTER key pressed
  insertNewline: function() {
    App.statechart.sendAction('hideDialog');
    return;
  },

  // Hide dialog on ESC key pressed
  cancel: function() {
    App.statechart.sendAction('hideDialog');
    return;
  }
};

/**
 * @class
 * Dialog timestamp
 */
App.DialogTimestampField = App.StackedFieldView.extend({
  label: '_search.timestamp'.loc(),

  DataView : App.TextBoxView.extend(App.DialogFieldDataMixin, {
    valueBinding: 'App.dialogController.timestamp'
  })
});

/**
 * @class
 * Dialog severity
 */
App.DialogSeverityField = App.StackedFieldView.extend(App.DialogFieldDataMixin, {
  label: '_search.severity'.loc(),

  DataView : App.TextBoxView.extend({
    valueBinding: 'App.dialogController.severityText',

    updateClassNames: function() {
      this.$().removeClass('error');
      this.$().removeClass('warning');

      var severity = App.dialogController.get('severity');
      if (severity <= 3) {
        this.$().addClass('error');
      } else if (severity == 4 || severity == 5) {
        this.$().addClass('warning');
      }
    }.observes('value')
  })
});

/**
 * @class
 * Dialog source
 */
App.DialogSourceField = App.StackedFieldView.extend(App.DialogFieldDataMixin, {
  label: '_search.source'.loc(),

  DataView : App.TextBoxView.extend({
    valueBinding: 'App.dialogController.source'
  })
});

/**
 * @class
 * Dialog host
 */
App.DialogHostField = App.StackedFieldView.extend(App.DialogFieldDataMixin, {
  label: '_search.host'.loc(),

  DataView : App.TextBoxView.extend({
    valueBinding: 'App.dialogController.host'
  })
});

/**
 * @class
 * Dialog Message
 */
App.DialogMessageField = App.StackedFieldView.extend(App.DialogFieldDataMixin, {
  label: '_search.message'.loc(),

  DataView : App.TextAreaView.extend({
    valueBinding: 'App.dialogController.message'
  })
});

/**
 * @class
 * Keywords view in the details dialog
 */
App.DialogKeywordsField = App.StackedFieldView.extend(App.DialogFieldDataMixin, {
  label: '_search.keywords'.loc(),

  DataView : App.TextAreaView.extend({
    valueBinding: 'App.dialogController.keywordsString'
  })
});

/**
 * @class
 * Fields view in the details dialog
 */
App.DialogFieldsField = App.StackedFieldView.extend(App.DialogFieldDataMixin, {
  label: '_search.fields'.loc(),

  DataView : App.TextAreaView.extend({
    valueBinding: 'App.dialogController.fieldsString'
  })
});

/**
 * @class
 * Dialog document id
 */
App.DialogDocumentIDField = App.StackedFieldView.extend(App.DialogFieldDataMixin, {
  label: '_search.documentID'.loc(),

  DataView : App.TextBoxView.extend({
    valueBinding: 'App.dialogController.documentID'
  })
});

/**
 * @class
 * Dialog saved timestamp
 */
App.DialogSavedTimestampField = App.StackedFieldView.extend(App.DialogFieldDataMixin, {
  label: '_search.savedTimestamp'.loc(),

  DataView : App.TextBoxView.extend({
    valueBinding: 'App.dialogController.savedTimestamp'
  })
});

/**
 * @class
 * Button to show previous log entry
 */
App.DialogPreviousButton = App.ButtonView.extend({
  label: '_previous'.loc(),
  title: '_previousTooltip'.loc(),
  disabledBinding: SC.Binding.from('App.dialogController.hasPreviousEntries').oneWay().bool().not(),

  click: function() {
    App.statechart.sendAction('showPreviousLogEntry');
    return;
  }
});

/**
 * @class
 * Button to show next log entry
 */
App.DialogNextButton = App.ButtonView.extend({
  label: '_next'.loc(),
  title: '_nextTooltip'.loc(),
  disabledBinding: SC.Binding.from('App.dialogController.hasLaterEntries').oneWay().bool().not(),

  click: function() {
    App.statechart.sendAction('showNextLogEntry');
    return;
  }
});

/**
 * @class
 * Button to close dialog window
 */
App.DialogDoneButton = App.ButtonView.extend({
  label: '_done'.loc(),

  click: function() {
    App.statechart.sendAction('hideDialog');
    return;
  }
});

// --------------------------------------------------------------------------------------------------------------------
// Controllers
// --------------------------------------------------------------------------------------------------------------------

/**
 * @class
 * Mediates between state charts and views for the main page
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
   * FROM Date and time combined in the format '%Y-%m-%d %H:%M:%S'
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
   * TO Date and time combined in the format '%Y-%m-%d %H:%M:%S'
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
    SC.Object.create({label: '_search.timespan.60'.loc(), value: '60'}),
    SC.Object.create({label: '_search.timespan.1440'.loc(), value: '1440'}),
    SC.Object.create({label: '_search.timespan.10080'.loc(), value: '10080'}),
    SC.Object.create({label: '_search.timespan.20160'.loc(), value: '20160'}),
    SC.Object.create({label: '_search.timespan.43200'.loc(), value: '43200', selected: YES})
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
    if (SC.empty(logEntry.ts)) {
      scDate = SC.DateTime.create();
    } else {
      scDate = App.DateTime.parseChililogServerDateTime(logEntry.ts);
    }

    var severity = logEntry.severity;
    var severityClassName = '';
    if (severity <= 3) {
      severityClassName = 'important';
    } else if (severity == 4 || severity == 5) {
      severityClassName = 'warning';
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

    var newLogEntryHtml = '<div class="logEntry" ondblclick="App.statechart.sendAction(\'showDialog\',' + displayedLogEntryIndex + ');">' +
      '<div class="row">' +
        '<div class="left">' + App.DateTime.toChililogLocalDateTime(scDate) + '</div>' +
        '<div class="right">' +
          formattedMessage +
          '<div class="rightFooter">' +
            '<span class="severity"><span class="label ' + severityClassName+ '">severity:</span> ' + App.REPOSITORY_ENTRY_SEVERITY_MAP[severity] + '</span>' +
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

/**
 * @class
 * Mediates between state charts and views for the dialog showing details of a log entry
 */
App.dialogController = SC.Object.create({

  /**
   * Index of log entry in displayedLogEntryIndex array that is currently loaded
   *
   * @type int
   */
  currentLogEntryIndex: -1,

  /**
   * Unique id for this log entry
   *
   * @type String
   */
  documentID: null,

  /**
   * Timestamp string, representing the time when the log entry was generated
   *
   * @type String
   */
  timestamp: null,

  /**
   * Timestamp string, representing the time when the log entry was saved into the repository
   *
   * @type String
   */
  savedTimestamp: null,

  /**
   * Name of the app or device that generated the log entry
   *
   * @type String
   */
  source: null,

  /**
   * Network host name or address of the app or device that generated the log entry
   *
   * @type String
   */
  host: null,

  /**
   * Severity code
   *
   * @type String
   */
  severity: null,

  /**
   * severity converted into display text
   *
   * @type String
   */
  severityText: function() {
   return App.REPOSITORY_ENTRY_SEVERITY_MAP[this.get('severity')];
  }.property('severity').cacheable(),

  /**
   * Raw log message
   *
   * @type String
   */
  message: null,

  /**
   * Array of keywords
   *
   * @type Array of Strings
   */
  keywords: null,

  /**
   * Comma separated keywords
   *
   * @type Strings
   */
  keywordsString: null,
  
  /**
   * Array of field objects. Each object has a 'name' and 'value' property
   *
   * @type Array of Objects
   */
  fields: null,

  /**
   * String of field in the format "Name = Value". Each field is separated by a new line.
   *
   * @type Strings
   */
  fieldsString: null,

  /**
   * Flag to indicate if there are earlier log entries that can be displayed
   *
   * @type Boolean
   */
  hasPreviousEntries: NO,

  /**
   * Flag to indicate if there are later log entries that can be displayed
   * 
   * @type Boolean
   */
  hasLaterEntries: NO,

  /**
   * Load the details of an API object into this controller
   *
   * @param {Object} logEntryAO API Object representing the log entry returned by the server
   */
  loadFromApiObject: function(displayedLogEntryIndex) {

    var logEntries = App.pageController.get('displayedLogEntries');
    var logEntryAO = logEntries[displayedLogEntryIndex];

    this.set('currentLogEntryIndex', displayedLogEntryIndex);
    this.set('hasPreviousEntries', displayedLogEntryIndex !== 0);
    this.set('hasLaterEntries', displayedLogEntryIndex !== logEntries.length - 1);

    var now = new Date();
    var timezoneOffsetMinutes = now.getTimezoneOffset();

    var propertyMap = this.get('propertyMap');
    for (var i = 0; i < propertyMap.length; i++) {
      var map = propertyMap[i];
      var controllerPropertyName = map[0];
      var apiObjectPropertyName = map[1];

      var apiObjectValue = logEntryAO[apiObjectPropertyName];
      if (controllerPropertyName === 'timestamp' || controllerPropertyName === 'savedTimestamp') {
        var d = App.DateTime.parseChililogServerDateTime(apiObjectValue);
        apiObjectValue = App.DateTime.toChililogLocalDateTime(d);
      }
      this.set(controllerPropertyName, apiObjectValue);
    }

    // Make keywords CSV
    var keywords = this.get('keywords');
    var keywordsString = '';
    if (!SC.none(keywords)) {
      for (var i = 0; i < keywords.length; i++) {
        if (i > 0) {
          keywordsString = keywordsString + ', ';
        }
        keywordsString = keywordsString + keywords[i];
      }
    }
    this.set('keywordsString', keywordsString);

    // Get fields - these properties have names prefixed with 'fld_'
    var fieldsArray = [];
    var fieldsString = '';
    for (var propertyName in logEntryAO) {
      if (!SC.empty(propertyName) && typeof(propertyName) === 'string' && propertyName.indexOf('fld_') === 0) {
        var nvp = { name: propertyName.substr(4), value: logEntryAO[propertyName]};
        fieldsArray.push(nvp);
        fieldsString = fieldsString + nvp.name + ' = ' + nvp.value + '\n';
      }
    }
    this.set('fields', fieldsArray);
    this.set('fieldsString', fieldsString);

    return;
  },

  /**
   * Load the previous log entry
   */
  loadPreviousLogEntry: function() {
    var currentLogEntryIndex = this.get('currentLogEntryIndex');
    this.loadFromApiObject(currentLogEntryIndex - 1);
  },

  /**
   * Load the next log entry
   */
  loadNextLogEntry: function() {
    var currentLogEntryIndex = this.get('currentLogEntryIndex');
    this.loadFromApiObject(currentLogEntryIndex + 1);
  },

  /**
   *
   * @type Array of name-value pairs
   */
  propertyMap: [
    [App.DOCUMENT_ID_RECORD_FIELD_NAME, '_id' ],
    ['timestampString' ,'ts'],
    ['timestamp' ,'ts'],
    ['savedTimestamp' ,'saved_ts'],
    ['source' ,'source'],
    ['host' ,'host'],
    ['severity', 'severity'],
    ['message' ,'message'],
    ['keywords' ,'keywords']
  ]

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

      showMore: function() {
        App.pageController.set('errorMessage', '');
        this.gotoState('showingMore');
      },

      showDialog: function(displayedLogEntryIndex) {
        // Load logEntry into the dialog controller
        App.dialogController.loadFromApiObject(displayedLogEntryIndex);
        this.gotoState('showingDetailsDialog');
      }

    }),

    showingDetailsDialog: SC.State.extend({
      enterState: function() {
        $('#searchDialog').dialog('open')
      },

      exitState: function() {
        $('#searchDialog').dialog('close')
      },

      hideDialog: function() {
        this.gotoState('notSearching');
      },

      showPreviousLogEntry: function() {
        App.dialogController.loadPreviousLogEntry();
      },

      showNextLogEntry: function() {
        App.dialogController.loadNextLogEntry();
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
              '$gte': App.DateTime.toChililogServerDateTime(SC.DateTime.create().advance({minute: minutesAgo})),
              '$lte': App.DateTime.toChililogServerDateTime(SC.DateTime.create())
            };
          }
          if (SC.none(conditions.ts)) {
            var parseFormat = '%Y-%m-%d %H:%M:%S';
            var from = App.pageController.get('fromDateTime');
            var to = App.pageController.get('toDateTime');
            if (!SC.empty(from) && !SC.empty(to)) {
              conditions.ts = {
                '$gte': App.DateTime.toChililogServerDateTime(SC.DateTime.parse(from, parseFormat)),
                '$lte': App.DateTime.toChililogServerDateTime(SC.DateTime.parse(to, parseFormat))
              };
              if (SC.DateTime.compare(conditions.ts['$gte'], conditions.ts['$lte']) > 0) {
                throw App.$error('_search.dateTimeRangeError', null, 'fromDateField');
              }
            } else if (!SC.empty(from)) {
              conditions.ts = {
                '$gte': App.DateTime.toChililogServerDateTime(SC.DateTime.parse(from, parseFormat))
              };
            } else if (!SC.empty(to)) {
              conditions.ts = {
                '$lte': App.DateTime.toChililogServerDateTime(SC.DateTime.parse(to, parseFormat))
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
          var minutes = parseInt((ss[1]))
          if (minutes < 0 || minutes > 60) {
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
  App.viewUtils.setupStandardPage(App.pageFileName);

  App.statechart.initStatechart();

  // Load repository status so as to setup the dropdown list
  App.repositoryRuntimeEngine.load(this, function() {
    var recordArray = App.repositoryRuntimeEngine.getRecords('currentStatus != "' + App.REPOSITORY_STATUS_OFFLINE  +'"');
    App.pageController.get('repositoryOptions').set('content', recordArray);
    App.pageController.set('repository', App.pageController.get('repositoryOptions').get('firstObject'));
  }, null);

} else {
  // Not logged in so go to login page
  window.location = 'login.html?returnTo=' + encodeURIComponent(App.pageFileName);
}
