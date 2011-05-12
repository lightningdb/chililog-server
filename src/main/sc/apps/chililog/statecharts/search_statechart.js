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
      var ctrl = Chililog.searchListViewController;
      if (SC.none(ctrl.get('content'))) {
        var repoEntryQuery = SC.Query.local(Chililog.RepositoryEntryRecord, { orderBy: 'timestamp' });
        var repoEntries = Chililog.store.find(repoEntryQuery);
        ctrl.set('content', repoEntries);
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
      enterState: function() {
        this.doSearch();
      },

      exitState: function() {
      },

      doSearch: function() {
        Chililog.repositoryDataController.find(NO, this, this.endSearch);
      },

      endSearch: function(params, error) {
        var ctrl = Chililog.searchListViewController;
        if (!SC.none(error)) {
          ctrl.showError(error);
        }
        this.gotoState('viewingSearchResults_Idle');
      }
    }),

    /**
     * Search event
     */
    search: function() {
      this.gotoState('viewingSearchResults_Searching');
    }
  })
  
});