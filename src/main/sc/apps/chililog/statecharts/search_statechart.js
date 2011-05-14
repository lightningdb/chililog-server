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
  },

  viewingSearchResults: SC.State.design({

    initialSubstate: 'viewingSearchResults_Idle',

    enterState: function() {
      // Update repository drop down
      var ctrl = Chililog.searchListViewController;
      var nameValueArray = [];
      if (SC.none(ctrl.get('repositories'))) {
        var repoInfoQuery = SC.Query.local(Chililog.RepositoryInfoRecord, { orderBy: 'name' });
        var repoInfoArray = Chililog.store.find(repoInfoQuery);
        ctrl.set('repositories', repoInfoArray);
      }

      // Have to set this to make sure that SelectFieldList renders the drop downs
      //cpDidChange
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
          var repoEntryQuery = SC.Query.local(Chililog.RepositoryEntryRecord, { orderBy: 'timestamp' });
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
      enterState: function(context) {
        if (context.serachType == 'basic') {
          this.doBasicSearch();
          Chililog.searchListViewController.set('isBasicSearching', YES);
        }
      },

      exitState: function() {
        Chililog.searchListViewController.set('isBasicSearching', NO);
      },

      doBasicSearch: function() {
        var ctrl = Chililog.searchListViewController;
        var criteria = {
          documentID: ctrl.get('basicRepository'),
          conditions: '',
          keywordUsage: 'All',
          keywords: ctrl.get('basicKeywords'),
          startPage: 1,
          recordsPerPage: 100,
          doPageCount: 'false'
        };
        var repositoryDocumentID = ctrl.get('basicRepository');
        var conditions = '';
        Chililog.repositoryDataController.find(criteria, this, this.endBasicSearch);

        // Clear table to signal to the user that we are searching
        ctrl.set('content', null);
      },

      endBasicSearch: function(params, error) {
        var ctrl = Chililog.searchListViewController;
        if (!SC.none(error)) {
          ctrl.showError(error);
        }
        this.gotoState('viewingSearchResults_Idle');
      }
    }),

    /**
     * Search event
     * @param {Hash} params Data hash of parameters.
     * 
     */
    basicSearch: function(params) {
      this.gotoState('viewingSearchResults_Searching', {serachType: 'basic'});
    }
    
  })
  
});