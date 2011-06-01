// ==========================================================================
// Project:   Chililog
// Copyright: ©2011 My Company, Inc.
// ==========================================================================

/**
 * State chart for configure screens
 */
Chililog.SearchState = SC.State.extend({

  initialSubstate: 'viewingRepositoryEntries',

  /**
   * Show the search view upon entry into this state
   */
  enterState: function() {
    Chililog.mainViewController.doShow('search');
  },

  exitState: function() {
    // Clear result set otherwise our table takes forever to load
    // Not sure why it takes so long but it does!
    Chililog.repositoryDataController.clearRepositoryEntries();
    Chililog.searchListViewController.set('canShowMore', NO);
  },

  /**
   *  Just viewing the list of entries.
   */
  viewingRepositoryEntries: SC.State.design({

    enterState: function() {
      // Set content for table if there isn't any - 1st time in or after search
      var ctrl = Chililog.searchListViewController;
      if (SC.none(ctrl.get('content'))) {
        var repoEntryQuery = SC.Query.local(Chililog.RepositoryEntryRecord, { orderBy: 'timestampString' });
        var repoEntries = Chililog.store.find(repoEntryQuery);
        ctrl.set('content', repoEntries);
      }
    },

    exitState: function() {
      return;
    },

    /**
     * Search event
     * @param {Hash} params Data hash of parameters.
     */
    basicSearch: function(params) {
      this.gotoState('searchingForRepositoryEntries', {searchType: 'basic'});
    },

    /**
     * Search event
     * @param {Hash} params Data hash of parameters.
     */
    advancedSearch: function(params) {
      this.gotoState('searchingForRepositoryEntries', {searchType: 'advanced'});
    },

    /**
     * Get more records
     * @param {Hash} params Data hash of parameters.
     */
    showMore: function(params) {
      this.gotoState('searchingForRepositoryEntries', {searchType: 'showMore'});
    },

    /**
     * Show the details of a specific search entry
     */
    viewEntry: function() {
      this.gotoState('viewingRepositoryEntry');
    },

    /**
     * Toggle between basic and advance search
     */
    toggleSearchMode: function() {
      var ctrl = Chililog.searchListViewController;

      // Clear result set
      Chililog.repositoryDataController.clearRepositoryEntries();
      ctrl.set('canShowMore', NO);

      // Switch
      var isBasicSearchMode = ctrl.get('isBasicSearchMode');
      ctrl.set('isBasicSearchMode', !isBasicSearchMode);
    }
  }),


  /**
   * Searching for more entries
   */
  searchingForRepositoryEntries: SC.State.design({

    /**
     * Save of the previous basic search criteria for use in show more
     */
    previousBasicSearchCriteria: null,

    enterState: function(context) {
      if (context.searchType === 'basic') {
        this.doBasicSearch();
      } else if (context.searchType === 'advanced') {
        this.doAdvancedSearch();
      } else if (context.searchType === 'showMore') {
        this.doShowMore();
      }
      Chililog.searchListViewController.set('isSearching', YES);
    },

    exitState: function() {
      Chililog.searchListViewController.set('isSearching', NO);
    },

    doBasicSearch: function() {
      try {
        var ctrl = Chililog.searchListViewController;

        // Make up time condition
        var minutesAgo = parseInt(ctrl.get('basicTimeSpan')) * -1;
        var conditions = {
          'ts' : {
            '$gte': SC.DateTime.create().advance({minute: minutesAgo}).toChililogServerDateTime(),
            '$lte': SC.DateTime.create().toChililogServerDateTime()
          }
        };

        var criteria = {
          documentID: ctrl.get('basicRepository'),
          conditions: conditions,
          keywordUsage: 'All',
          keywords: ctrl.get('basicKeywords'),
          startPage: 1,
          recordsPerPage: ctrl.get('rowsPerSearch'),
          doPageCount: 'false'
        };
        Chililog.repositoryDataController.find(criteria, this, this.endSearch);

        // Save criteria for show more
        this.set('previousSearchCriteria', criteria);

        // Clear table to signal to the user that we are searching
        ctrl.set('content', null);
      }
      catch (err) {
        // End search with error
        this.endSearch(ctrl.get('basicRepository'), 0, null, err);
      }
    },

    doAdvancedSearch: function() {
      try {
        var ctrl = Chililog.searchListViewController;

        // Turn conditions json into javascript objects
        var conditions = {};
        var conditionString = ctrl.get('advancedConditions');
        if (!SC.empty(conditionString)) {
          conditions = SC.json.decode(conditionString);
        }

        // Make up time condition
        if (SC.none(conditions.ts)) {
          if (ctrl.get('isInThePastTimeType')) {
            var minutesAgo = parseInt(ctrl.get('advancedTimeSpan')) * -1;
            conditions.ts = {
              '$gte': SC.DateTime.create().advance({minute: minutesAgo}).toChililogServerDateTime(),
              '$lte': SC.DateTime.create().toChililogServerDateTime()
            };
          } else {
            var parseFormat = '%Y-%m-%d %H:%M:%S';
            var from = ctrl.get('advancedTimeFrom');
            var to = ctrl.get('advancedTimeTo');
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
        }

        // Source, host and severity
        if (SC.none(conditions.source)) {
          var source = ctrl.get('advancedSource');
          if (!SC.empty(source)) {
            conditions.source = source;
          }
        }
        if (SC.none(conditions.host)) {
          var host = ctrl.get('advancedHost');
          if (!SC.empty(host)) {
            conditions.host = host;
          }
        }
        if (SC.none(conditions.severity)) {
          var severity = ctrl.get('advancedSeverity');
          if (!SC.empty(severity)) {
            conditions.severity = parseInt(severity);
          }
        }

        // Final criteria
        var criteria = {
          documentID: ctrl.get('advancedRepository'),
          conditions: conditions,
          keywordUsage: 'All',
          keywords: ctrl.get('advancedKeywords'),
          startPage: 1,
          recordsPerPage: ctrl.get('rowsPerSearch'),
          doPageCount: 'false'
        };
        Chililog.repositoryDataController.find(criteria, this, this.endSearch);

        // Save criteria for show more
        this.set('previousSearchCriteria', criteria);

        // Clear table to signal to the user that we are searching
        ctrl.set('content', null);
      }
      catch (err) {
        // End search with error
        this.endSearch(ctrl.get('advancedRepository'), 0, null, err);
      }
    },

    endSearch: function(documentID, recordCount, params, error) {
      var ctrl = Chililog.searchListViewController;
      if (!SC.none(error)) {
        ctrl.showError(error);
      } else {
        ctrl.set('rowsFoundAfterSearch', recordCount > 0);
        ctrl.set('canShowMore', recordCount === ctrl.get('rowsPerSearch'));
      }

      this.gotoState('viewingRepositoryEntries');
    },

    doShowMore: function() {
      var ctrl = Chililog.searchListViewController;
      var criteria = this.get('previousSearchCriteria');
      criteria.startPage = criteria.startPage + 1;
      Chililog.repositoryDataController.find(criteria, this, this.endSearch);
    }
  }),

  /**
   * View the details page for a specific entry
   */
  viewingRepositoryEntry: SC.State.design({
    enterState: function(context) {
      Chililog.searchDetailViewController.show();
    },

    exitState: function() {
      Chililog.searchDetailViewController.hide();
    },

    done: function() {
      this.gotoState('viewingRepositoryEntries');
    }
  })

});