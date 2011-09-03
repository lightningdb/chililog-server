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
 * From time
 */
App.FromTimeField = SC.View.extend({
  classNames: 'field'.w(),

  label: '_search.fromTime'.loc(),

  Data : App.TextBoxView.extend({
    valueBinding: 'App.pageController.fromTime',
    name: 'fromTime',
    placeholder: 'hh:mm:ss',
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
 * To time
 */
App.ToTimeField = SC.View.extend({
  classNames: 'field'.w(),

  label: '_search.toTime'.loc(),

  Data : App.TextBoxView.extend({
    valueBinding: 'App.pageController.toTime',
    name: 'toTime',
    placeholder: 'hh:mm:ss',
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
App.SearchButton = App.ButtonView.extend({
  disabledBinding: SC.Binding.from('App.pageController.isSearching').oneWay().bool(),
  
  label: '_search'.loc(),

  click: function() {
    App.statechart.sendAction('startSearch');
    return;
  }
});

/**
 * Show more rows
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
 * Button to show advanced criteria
 */
App.AdvancedButton = App.ButtonView.extend({
  disabledBinding: SC.Binding.from('App.pageController.isSearching').oneWay().bool(),
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
  isVisibleBinding: SC.Binding.from('App.pageController.showAdvancedCriteria').oneWay().bool()
});

/**
 * Spinner displayed while searching
 */
App.WorkingImage = App.ImgView.extend({
  src: 'images/working.gif',
  visible: NO,
  isVisibleBinding: SC.Binding.from('App.pageController.isSearching').oneWay().bool()
});

/**
 * Log entries returned from search
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

App.BottomBar = SC.View.extend({
  isVisibleBinding: SC.Binding.from('App.pageController.canShowMore').oneWay().bool()
});

/**
 * Show message when on rows found
 */
App.NoRowsView = SC.View.extend({
  isVisibleBinding: SC.Binding.from('App.pageController.rowsFound').oneWay().bool().not()
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
   * FROM Date and time combined
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
   * Flag to indicate if row found or not
   */
  rowsFound: YES,

  /**
   * Flag to indicate if there are more rows to show
   */
  canShowMore: NO,

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
  rowsPerSearch: 30,

  /**
   * Flag to indicate if we want to show advanced criteria
   */
  showAdvancedCriteria: NO,

  /**
   * Saved page criteria used when we show more records
   */
  previousSearchCriteria: null

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
          App.pageController.set('previousSearchCriteria', criteria);

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
          App.pageController.set('rowsFound', recordCount > 0);
          App.pageController.set('canShowMore', recordCount === App.pageController.get('rowsPerSearch'));
        } else {
          App.pageController.set('errorMessage', error);
        }

        $('#results').css('display', recordCount > 0 ? 'block' : 'none');

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
        App.repositoryRuntimeInfoEngine.find(criteria, this, this.endShowMore);
      },

      /**
       * Called back when search returns
       * @param documentID
       * @param recordCount
       * @param params
       * @param error
       */
      endShowMore: function(documentID, recordCount, params, error) {
        if (SC.none(error)) {
          App.pageController.set('rowsFound', recordCount > 0);
          App.pageController.set('canShowMore', recordCount === App.pageController.get('rowsPerSearch'));
        } else {
          App.pageController.set('errorMessage', error);
        }

        $('#results').css('display', recordCount > 0 ? 'block' : 'none');

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

