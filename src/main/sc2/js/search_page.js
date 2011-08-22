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
  classNames: 'ui-state-error ui-corner-all error'.w(),
  messageBinding: 'App.pageController.errorMessage',
  isVisibleBinding: SC.Binding.from('App.pageController.errorMessage').oneWay().bool()
});

/**
 * Repository options
 */
App.RepositorySelectOption = App.SelectOption.extend({
  labelBinding: '*content.displayNameOrName',
  valueBinding: '*content.documentID'
});

/**
 * Repository
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
 * From date
 */
App.FromDateField = SC.View.extend({
  classNames: 'field'.w(),

  label: '_search.fromDate'.loc(),

  Data : App.TextBoxView.extend({
    valueBinding: 'App.pageController.fromDate',
    name: 'fromDate',
    disabledBinding: SC.Binding.from('App.pageController.isSearching').oneWay().bool()
  })
});

/**
 * From time
 */
App.FromTimeField = SC.View.extend({
  classNames: 'field'.w(),

  label: '_search.fromTime'.loc(),

  Data : App.TextBoxView.extend({
    valueBinding: 'App.pageController.fromTime',
    name: 'fromTime',
    disabledBinding: SC.Binding.from('App.pageController.isSearching').oneWay().bool()
  })
});

/**
 * To date
 */
App.ToDateField = SC.View.extend({
  classNames: 'field'.w(),

  label: '_search.toDate'.loc(),

  Data : App.TextBoxView.extend({
    valueBinding: 'App.pageController.toDate',
    name: 'toDate',
    disabledBinding: SC.Binding.from('App.pageController.isSearching').oneWay().bool()
  })
});

/**
 * To time
 */
App.ToTimeField = SC.View.extend({
  classNames: 'field'.w(),

  label: '_search.toTime'.loc(),

  Data : App.TextBoxView.extend({
    valueBinding: 'App.pageController.toTime',
    name: 'toTime',
    disabledBinding: SC.Binding.from('App.pageController.isSearching').oneWay().bool()
  })
});

/**
 * Source filter
 */
App.SourceField = SC.View.extend({
  classNames: 'field'.w(),

  label: '_search.source'.loc(),

  Data : App.TextBoxView.extend({
    valueBinding: 'App.pageController.source',
    name: 'source',
    disabledBinding: SC.Binding.from('App.pageController.isSearching').oneWay().bool()
  })
});

/**
 * Host filter
 */
App.HostField = SC.View.extend({
  classNames: 'field'.w(),

  label: '_search.host'.loc(),

  Data : App.TextBoxView.extend({
    valueBinding: 'App.pageController.host',
    name: 'host',
    disabledBinding: SC.Binding.from('App.pageController.isSearching').oneWay().bool()
  })
});

/**
 * Severity filter
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
 * Timespan
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
 * Keywords filter
 */
App.KeywordsField = SC.View.extend({
  classNames: 'field'.w(),

  label: '_search.keywords'.loc(),

  Data : App.TextBoxView.extend({
    valueBinding: 'App.pageController.keywords',
    name: 'keywords',
    disabledBinding: SC.Binding.from('App.pageController.isSearching').oneWay().bool()
  })
});

/**
 * Message filter
 */
App.MessageField = SC.View.extend({
  classNames: 'field'.w(),

  label: '_search.message'.loc(),

  Data : App.TextBoxView.extend({
    valueBinding: 'App.pageController.message',
    name: 'message',
    disabledBinding: SC.Binding.from('App.pageController.isSearching').oneWay().bool()
  })
});

/**
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
 * Button to search
 */
App.SearchButton = JQ.Button.extend({
  isVisibleBinding: SC.Binding.from('App.pageController.showAdvancedCriteria').oneWay().bool().not(),

  label: '_search'.loc(),

  click: function() {
    App.statechart.sendAction('startSearch');
    return;
  }
});

/**
 * Button to search
 */
App.SearchButton2 = JQ.Button.extend({
  label: '_search'.loc(),

  click: function() {
    App.statechart.sendAction('startSearch');
    return;
  }
});

/**
 * Button to show advanced criteria
 */
App.AdvancedButton = JQ.Button.extend({
  isVisibleBinding: SC.Binding.from('App.pageController.showAdvancedCriteria').oneWay().bool().not(),

  label: '_search.advancedCriteria'.loc(),

  click: function() {
    App.pageController.set('showAdvancedCriteria', YES);
    return;
  }
});

/**
 * DIV for advanced criteria
 */
App.AdvancedCriteria = SC.View.extend({
  classNames: 'criteria'.w(),
  isVisibleBinding: SC.Binding.from('App.pageController.showAdvancedCriteria').oneWay().bool()
});

/**
 * Log entries returned from search
 */
App.LogEntryCollectionView = SC.CollectionView.extend({
  contentBinding: 'App.pageController.logEntries',
  classNames: 'divTable'.w(),
  itemViewClass: SC.View.extend({
    classNames: 'divTableRow'.w()
  })
});

// --------------------------------------------------------------------------------------------------------------------
// Controllers
// --------------------------------------------------------------------------------------------------------------------

/**
 * Mediates between state charts and views
 */
App.pageController = SC.Object.create({
  /**
   * Value of the repository field
   */
  repository: '',

  /**
   * Value of the from date field
   */
  fromDate: '',

  /**
   * Value of the from time field
   */
  fromTime: '',

  /**
   * Value of the to date field
   */
  toDate: '',

  /**
   * Value of the to time field
   */
  toTime: '',

  /**
   * Value of the source field
   */
  source: '',

  /**
   * Value of the host field
   */
  host: '',

  /**
   * Value of the severity field
   */
  severity: 'All',

  /**
   * Value of the timespan field
   */
  timespan: '',

  /**
   * Value of the keywords field
   */
  keywords: '',

  /**
   * Value of the message field
   */
  message: '',

  /**
   * JSON conditions
   */
  condition: '',

  /**
   * Error message to display
   */
  errorMessage: '',

  /**
   * Indicates if we are currently streaming or not
   */
  isSearching: NO,

  /**
   * Options for displaying in the repository dropdown
   */
  repositoryOptions: [],

  /**
   * Options for displaying in the severity dropdown
   */
  severityOptions: [
    SC.Object.create({label: '', value: '', selected: YES}),
    SC.Object.create({label: '_repositoryEntryRecord.Severity.Emergency'.loc(), value: '0'}),
    SC.Object.create({label: '_repositoryEntryRecord.Severity.Action'.loc(), value: '1'}),
    SC.Object.create({label: '_repositoryEntryRecord.Severity.Critical'.loc(), value: '2'}),
    SC.Object.create({label: '_repositoryEntryRecord.Severity.Error'.loc(), value: '3'}),
    SC.Object.create({label: '_repositoryEntryRecord.Severity.Warning'.loc(), value: '4'}),
    SC.Object.create({label: '_repositoryEntryRecord.Severity.Notice'.loc(), value: '5'}),
    SC.Object.create({label: '_repositoryEntryRecord.Severity.Information'.loc(), value: '6'}),
    SC.Object.create({label: '_repositoryEntryRecord.Severity.Debug'.loc(), value: '7'})
  ],

  /**
   * Options for displaying in the timespan dropdown
   */
  timespanOptions: [
    SC.Object.create({label: ''.loc(), value: ''}),
    SC.Object.create({label: '_search.timespan.5'.loc(), value: '5'}),
    SC.Object.create({label: '_search.timespan.15'.loc(), value: '15'}),
    SC.Object.create({label: '_search.timespan.30'.loc(), value: '30'}),
    SC.Object.create({label: '_search.timespan.60'.loc(), value: '60'}),
    SC.Object.create({label: '_search.timespan.1440'.loc(), value: '1440', selected: YES}),
    SC.Object.create({label: '_search.timespan.10080'.loc(), value: '10080'}),
    SC.Object.create({label: '_search.timespan.20160'.loc(), value: '20160'}),
    SC.Object.create({label: '_search.timespan.43200'.loc(), value: '43200'})
  ],

  /**
   * Number of rows to return per search
   */
  rowsPerSearch: 50,

  /**
   * Flag to indicate if we want to show advanced criteria
   */
  showAdvancedCriteria: NO

});

/**
 * Controls access to the log data via the API engine
 */
App.engineController = SC.Object.create({

  doSearch: function() {
    try {
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
      App.repositoryRuntimeInfoEngine.find(criteria, this, this.endSearch);

      // Save criteria for show more
      this.set('previousSearchCriteria', criteria);

      // Clear table to signal to the user that we are searching
      App.pageController.set('results', null);
    }
    catch (err) {
      // End search with error
      this.endSearch(App.pageController.get('repository'), 0, null, err);
    }
  },

  /**
   * Called back when search returns
   * @param documentID
   * @param recordCount
   * @param params
   * @param error
   */
  endSearch: function(documentID, recordCount, params, error) {
    if (SC.none(error)) {
      App.pageController.set('rowsFoundAfterSearch', recordCount > 0);
      App.pageController.set('canShowMore', recordCount === App.pageController.get('rowsPerSearch'));
    } else {
      App.pageController.set('errorMessage', error);
    }

    App.statechart.sendAction('finishSearch');
  },

  /**
   * Show more records
   */
  doShowMore: function() {
    var criteria = this.get('previousSearchCriteria');
    criteria.startPage = criteria.startPage + 1;
    App.repositoryRuntimeInfoEngine.find(criteria, this, this.endSearch);
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
      }
    }),

    /**
     * Block the user from entering data
     */
    searching: SC.State.extend({
      enterState: function() {
        App.pageController.set('isSearching', YES);
        App.engineController.doSearch();
      },

      exitState: function() {
        App.pageController.set('isSearching', NO);
      },

      finishSearch: function() {
        this.gotoState('notSearching');
      }

    })
  })
});

// --------------------------------------------------------------------------------------------------------------------
// Start page processing
// --------------------------------------------------------------------------------------------------------------------
App.pageFileName = "search.html";

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

    var query2 = SC.Query.local(App.RepositoryEntryRecord, {
      orderBy: 'timestamp'
    });
    var arrayProxy2 = App.store.find(query2);
    App.pageController.set('logEntries', arrayProxy2);

  }, null);
} else {
  // Not logged in so go to login page
  window.location = 'login.html?returnTo=' + encodeURIComponent(App.pageFileName);
}
