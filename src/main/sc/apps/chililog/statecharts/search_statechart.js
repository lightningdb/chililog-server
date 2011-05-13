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
      // Set binding on table
      var ctrl = Chililog.searchListViewController;
      if (SC.none(ctrl.get('content'))) {
        var repoEntryQuery = SC.Query.local(Chililog.RepositoryEntryRecord, { orderBy: 'timestamp' });
        var repoEntries = Chililog.store.find(repoEntryQuery);
        ctrl.set('content', repoEntries);
      }

      // Update repository drop down
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
        }
      },

      exitState: function() {
      },

      doBasicSearch: function() {
        var viewCtrl = Chililog.searchListViewController;
        var criteria = {
          documentID: viewCtrl.get('basicRepository'),
          conditions: '',
          keywordUsage: 'All',
          keywords: viewCtrl.get('basicKeywords'),
          startPage: 1,
          recordsPerPage: 100,
          doPageCount: 'false'
        };
        var repositoryDocumentID = viewCtrl.get('basicRepository');
        var conditions = '';
        Chililog.repositoryDataController.find(criteria, this, this.endBasicSearch);
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