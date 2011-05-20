// ==========================================================================
// Project:   Chililog
// Copyright: ©2011 My Company, Inc.
// ==========================================================================

/**
 * State chart for configure screens
 */
Chililog.SearchState = SC.State.extend({

  initialSubstate: 'viewingSearchResults',

  /**
   * Show the search view upon entry into this state
   */
  enterState: function() {
    Chililog.mainViewController.doShow('search');
    //Chililog.searchListViewController.resetTableView();
  },

  viewingSearchResults: SC.State.design({

    initialSubstate: 'viewingSearchResults_Idle',

    enterState: function() {
      // Update repository drop down content if it has not been set
      var ctrl = Chililog.searchListViewController;
      var nameValueArray = [];
      if (SC.none(ctrl.get('repositories'))) {
        var repoInfoQuery = SC.Query.local(Chililog.RepositoryInfoRecord, { orderBy: 'name' });
        var repoInfoArray = Chililog.store.find(repoInfoQuery);
        ctrl.set('repositories', repoInfoArray);
      }
    },

    exitState: function() {
      return;
    },

    /**
     * Let the user browse listing results
     */
    viewingSearchResults_Idle: SC.State.design({
      enterState: function() {
        // Set content for table if there isn't any - 1st time in or after search
        var ctrl = Chililog.searchListViewController;
        if (SC.none(ctrl.get('content'))) {
          var repoEntryQuery = SC.Query.local(Chililog.RepositoryEntryRecord, { orderBy: 'timestampString' });
          var repoEntries = Chililog.store.find(repoEntryQuery);
          ctrl.set('content', repoEntries);
        }

        return;
      },
      exitState: function() {
        return;
      }
    }),

    /**
     * Trigger refreshing of repository data
     */
    viewingSearchResults_Searching: SC.State.design({

      /**
       * Save of the previous basic search criteria for use in show more
       */
      previousBasicSearchCriteria: null,

      enterState: function(context) {
        if (context.serachType === 'basic') {
          this.doBasicSearch();
        } else if (context.serachType === 'showMore') {
          this.doShowMore();
        }
        Chililog.searchListViewController.set('isSearching', YES);
      },

      exitState: function() {
        Chililog.searchListViewController.set('isSearching', NO);
      },

      doBasicSearch: function() {
        var ctrl = Chililog.searchListViewController;

        // Make up time condition
        var minutesAgo = parseInt(ctrl.get('basicTimeSpan')) * -1;
        var conditions = {
          'c_ts' : {
            '$gte': SC.DateTime.create().advance({minute: minutesAgo}).toTimezone(0).toFormattedString('%Y-%m-%dT%H:%M:%SZ'),
            '$lte': SC.DateTime.create().toTimezone(0).toFormattedString('%Y-%m-%dT%H:%M:%SZ') 
          }
        };

        var criteria = {
          documentID: ctrl.get('basicRepository'),
          conditions: conditions,
          keywordUsage: 'All',
          keywords: ctrl.get('basicKeywords'),
          startPage: 1,
          recordsPerPage: ctrl.get('basicRowsPerSearch'),
          doPageCount: 'false'
        };
        Chililog.repositoryDataController.find(criteria, this, this.endSearch);

        // Save criteria for show more
        this.set('previousBasicSearchCriteria', criteria);

        // Clear table to signal to the user that we are searching
        ctrl.set('content', null);
      },

      endSearch: function(documentID, recordCount, params, error) {
        var ctrl = Chililog.searchListViewController;
        if (!SC.none(error)) {
          ctrl.showError(error);
        } else {
          ctrl.set('rowsFoundAfterSearch', recordCount > 0);
          ctrl.set('canShowMore', recordCount === ctrl.get('basicRowsPerSearch'));
        }

        this.gotoState('viewingSearchResults_Idle');
      },

      doShowMore: function() {
        var ctrl = Chililog.searchListViewController;
        var criteria = this.get('previousBasicSearchCriteria');
        criteria.startPage = criteria.startPage + 1;
        Chililog.repositoryDataController.find(criteria, this, this.endSearch);
      }
    }),

    /**
     * Search event
     * @param {Hash} params Data hash of parameters.
     * 
     */
    basicSearch: function(params) {
      this.gotoState('viewingSearchResults_Searching', {serachType: 'basic'});
    },

    /**
     * Search event
     * @param {Hash} params Data hash of parameters.
     *
     */
    showMore: function(params) {
      this.gotoState('viewingSearchResults_Searching', {serachType: 'showMore'});
    }

  })
  
});