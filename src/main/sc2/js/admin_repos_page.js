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
// Javascript for admin_users.html
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
 * Repository field
 */
App.RepositoryField = App.StackedFieldView.extend({
  label: '_admin.repo.name'.loc(),

  DataView : App.TextBoxView.extend(App.CriteriaFieldDataMixin, {
    classNames: 'large'.w(),
    valueBinding: 'App.pageController.repositoryName',
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

  label: '_showMore'.loc(),

  click: function() {
    App.statechart.sendAction('showMore');
    return;
  }
});

/**
 * @class
 * Container view for the ShowMore button
 */
App.Results = SC.View.extend({
  isVisibleBinding: SC.Binding.from('App.pageController.showResults').oneWay().bool(),

  nameLabel: '_admin.repo.name'.loc(),
  displayNameLabel: '_admin.repo.displayName'.loc(),
  descriptionLabel: '_admin.repo.description'.loc(),
  currentStatusLabel: '_admin.repo.currentStatus'.loc(),

  CollectionView : SC.CollectionView.extend({
    contentBinding: 'App.resultsController',

    itemViewClass: SC.View.extend({
      tagName: 'tr',

      // Spit out the content's index in the array proxy as an attribute of the tr element
      attributeBindings: ['contentIndex'],

      willInsertElement: function() {
        this._super();

        // Add handler for double clicking
        var id = this.$().attr('id');
        this.$().dblclick(function() {
          App.statechart.sendAction('showDialog', $('#' + this.id).attr('contentIndex'));
        });
      }
    })

  })

});

/**
 * @class
 * View displayed when when on rows found
 */
App.NoRowsMessage = App.BlockMessageView.extend({
  messageType: 'warning',
  message: '_admin.repo.noRowsFound'.loc(),
  isVisibleBinding: SC.Binding.from('App.pageController.showNoRowsFound').oneWay().bool()
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
 * Spinner displayed while searching
 */
App.WorkingImage = App.ImgView.extend({
  src: 'images/working.gif',
  visible: NO,
  isVisibleBinding: SC.Binding.from('App.pageController.isSearching').oneWay().bool()
});


/**
 * @class
 * Dialog div
 */
App.Dialog = SC.View.extend({
  attributeBindings: ['title'],

  title: '_admin.repo.editTitle'.loc(),

  didInsertElement: function() {
    this._super();

    // JQuery UI dialog setup
    this.$().dialog({
      autoOpen: false,
      height: 360,
      width: 850,
      modal: true,
      close: function(event, ui) {
        // For when the X is clicked
        App.statechart.sendAction('hideDialog');
      }
    });

    // Add event handler to tab <a>. Delegate does  it for current and future tabs
    this.$().delegate('ul.tabs li > a', 'click', function(e) {
      var $this = $(this),
        href = $this.attr('href'),
        li = $this.parent('li'),
        ul = li.parent();

      if (/^#\w+/.test(href)) {
        e.preventDefault();
      }

      if ($this.hasClass('active')) {
        return
      }

      var $href = $(href)

      // Make this li active
      ul.find('.active').removeClass('active')
      li.addClass('active')

      // Make linked fieldset active
      $href.parent().find('.active').removeClass('active')
      $href.addClass('active')
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
 * Name Field
 */
App.DialogNameField = App.FieldView.extend({
  label: '_admin.repo.name'.loc(),
  help: '_admin.repo.name.help'.loc(),
  DataView : App.TextBoxView.extend(App.DialogFieldDataMixin, {
    classNames: 'medium'.w(),
    valueBinding: 'App.pageController.selectedRecord.name'
  })
});

/**
 * @class
 * Display Name Field
 */
App.DialogDisplayNameField = App.FieldView.extend({
  label: '_admin.repo.displayName'.loc(),
  DataView : App.TextBoxView.extend(App.DialogFieldDataMixin, {
    classNames: 'medium'.w(),
    valueBinding: 'App.pageController.selectedRecord.displayName'
  })
});

/**
 * @class
 * Description Field
 */
App.DialogDescriptionField = App.FieldView.extend({
  label: '_admin.repo.description'.loc(),
  DataView : App.TextBoxView.extend(App.DialogFieldDataMixin, {
    classNames: 'xxlarge'.w(),
    valueBinding: 'App.pageController.selectedRecord.description'
  })
});

/**
 * @class
 * Startup status Field
 */
App.DialogStartupStatusField = App.FieldView.extend({
  label: '_admin.repo.startupStatus'.loc(),
  DataView : App.SelectView.extend(App.DialogFieldDataMixin, {
    classNames: 'xxlarge'.w(),
    contentBinding: 'App.pageController.startupStatusOptions',
    valueBinding: SC.Binding.from('App.pageController.selectedRecord.startupStatus').transform(function(value, isForward) {
      if (isForward) {
        var options = App.pageController.get('startupStatusOptions');
        return value === 'ONLINE' ? options[0] : options[1];
      } else {
        return value.get('value');
      }
    })
  })
});

/**
 * @class
 * Store Entries Field
 */
App.DialogStoreEntriesIndicatorField = App.FieldView.extend({
  label: '_admin.repo.storeEntries'.loc(),
  DataView : App.SelectView.extend(App.DialogFieldDataMixin, {
    classNames: 'xxlarge'.w(),
    contentBinding: 'App.pageController.storeEntriesOptions',
    valueBinding: SC.Binding.from('App.pageController.selectedRecord.storeEntriesIndicator').transform(function(value, isForward) {
      if (isForward) {
        var options = App.pageController.get('storeEntriesOptions');
        return value ? options[0] : options[1];
      } else {
        return value.get('value');
      }
    })
  })
});

/**
 * @class
 * Storage Queue Durable Field
 */
App.DialogStorageQueueDurableIndicatorField = App.FieldView.extend({
  label: '_admin.repo.storageQueueDurable'.loc(),
  DataView : App.SelectView.extend(App.DialogFieldDataMixin, {
    classNames: 'xxlarge'.w(),
    contentBinding: 'App.pageController.storageQueueDurableOptions',
    valueBinding: SC.Binding.from('App.pageController.selectedRecord.storageQueueDurableIndicator').transform(function(value, isForward) {
      if (isForward) {
        var options = App.pageController.get('storageQueueDurableOptions');
        return value ? options[0] : options[1];
      } else {
        return value.get('value');
      }
    })
  })
});

/**
 * @class
 * Storage Queue Worker Count Field
 */
App.DialogStorageQueueWorkerCountField = App.FieldView.extend({
  label: '_admin.repo.storageQueueWorkerCount'.loc(),
  help: '_admin.repo.storageQueueWorkerCount.help'.loc(),
  DataView : App.TextBoxView.extend(App.DialogFieldDataMixin, {
    classNames: 'small'.w(),
    valueBinding: 'App.pageController.selectedRecord.storageQueueWorkerCount'
  })
});

/**
 * @class
 * Storage Max Keywords Field
 */
App.DialogStorageMaxKeywordsField = App.FieldView.extend({
  label: '_admin.repo.storageMaxKeywords'.loc(),
  help: '_admin.repo.storageMaxKeywords.help'.loc(),
  DataView : App.TextBoxView.extend(App.DialogFieldDataMixin, {
    classNames: 'small'.w(),
    valueBinding: 'App.pageController.selectedRecord.storageMaxKeywords'
  })
});

/**
 * @class
 * Max Memory Field
 */
App.DialogMaxMemoryField = App.FieldView.extend({
  label: '_admin.repo.maxMemory'.loc(),
  help: '_admin.repo.maxMemory.help'.loc(),
  DataView : App.TextBoxView.extend(App.DialogFieldDataMixin, {
    classNames: 'small'.w(),
    valueBinding: 'App.pageController.selectedRecord.maxMemory'
  })
});

/**
 * @class
 * Max Memory Policy Field
 */
App.DialogMaxMemoryPolicyField = App.FieldView.extend({
  label: '_admin.repo.maxMemoryPolicy'.loc(),
  DataView : App.SelectView.extend(App.DialogFieldDataMixin, {
    classNames: 'xxlarge'.w(),
    contentBinding: 'App.pageController.maxMemoryPolicyOptions',
    valueBinding: SC.Binding.from('App.pageController.selectedRecord.maxMemoryPolicy').transform(function(value, isForward) {
      if (isForward) {
        var options = App.pageController.get('maxMemoryPolicyOptions');
        for (var i=0; i< options.length; i++) {
          if (options[i].get('value') === value) {
            return options[i];
          }
        }
        return options[0];
      } else {
        return value.get('value');
      }
    })
  })
});

/**
 * @class
 * Page Size Field
 */
App.DialogPageSizeField = App.FieldView.extend({
  label: '_admin.repo.pageSize'.loc(),
  help: '_admin.repo.pageSize.help'.loc(),
  DataView : App.TextBoxView.extend(App.DialogFieldDataMixin, {
    classNames: 'small'.w(),
    valueBinding: 'App.pageController.selectedRecord.pageSize'
  })
});

/**
 * @class
 * Page Count Cache Field
 */
App.DialogPageCountCacheField = App.FieldView.extend({
  label: '_admin.repo.pageCountCache'.loc(),
  help: '_admin.repo.pageCountCache.help'.loc(),
  DataView : App.TextBoxView.extend(App.DialogFieldDataMixin, {
    classNames: 'small'.w(),
    valueBinding: 'App.pageController.selectedRecord.pageCountCache'
  })
});


/**
 * @class
 * Button to show previous log entry
 */
App.DialogPreviousButton = App.ButtonView.extend({
  label: '_previous'.loc(),
  title: '_previousTooltip'.loc(),
  disabledBinding: SC.Binding.from('App.pageController.canShowPreviousRecord').oneWay().bool().not(),

  click: function() {
    App.statechart.sendAction('showPreviousRecord');
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
  disabledBinding: SC.Binding.from('App.pageController.canShowNextRecord').oneWay().bool().not(),

  click: function() {
    App.statechart.sendAction('showNextRecord');
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
   * Error message to display
   *
   * @type String
   */
  errorMessage: '',

  /**
   * Name of repository to search
   *
   * @type String
   */
  repositoryName: null,


  /**
   * Options for Startup Status question in dialog
   * @type Array of SC.Object
   */
  startupStatusOptions: [
    SC.Object.create({label: '_admin.repo.startupStatus.online'.loc(), value: 'ONLINE'}),
    SC.Object.create({label: '_admin.repo.startupStatus.offline'.loc(), value: 'OFFLINE'})
  ],

  /**
   * Options for Store Entry question in dialog
   * @type Array of SC.Object
   */
  storeEntriesOptions: [
    SC.Object.create({label: '_admin.repo.storeEntries.yes'.loc(), value: YES}),
    SC.Object.create({label: '_admin.repo.storeEntries.no'.loc(), value: NO})
  ],

  /**
   * Options for Storage Queue Durable question in dialog
   * @type Array of SC.Object
   */
  storageQueueDurableOptions: [
    SC.Object.create({label: '_admin.repo.storageQueueDurable.yes'.loc(), value: YES}),
    SC.Object.create({label: '_admin.repo.storageQueueDurable.no'.loc(), value: NO})
  ],

  /**
   * Options for Max Memory Policy question in dialog
   * @type Array of SC.Object
   */
  maxMemoryPolicyOptions: [
    SC.Object.create({label: '_admin.repo.maxMemoryPolicy.page'.loc(), value: 'PAGE'}),
    SC.Object.create({label: '_admin.repo.maxMemoryPolicy.drop'.loc(), value: 'DROP'}),
    SC.Object.create({label: '_admin.repo.maxMemoryPolicy.block'.loc(), value: 'BLOCK'})
  ],

  /**
   * Indicates if we are currently streaming or not
   *
   * @type Boolean
   */
  isSearching: NO,

  /**
   * Flag to indicate if wish to show the results view
   *
   * @type Boolean
   */
  showResults: NO,

  /**
   * Flag to indicate if we wish to show the no rows found message
   *
   * @type Boolean
   */
  showNoRowsFound: NO,

  /**
   * Flag to indicate if there are more rows to show
   *
   * @type Boolean
   */
  canShowMore: NO,

  /**
   * Number of rows to return per search
   *
   * @type int
   */
  rowsPerSearch: 10,

  /**
   * Previous search criteria
   *
   * @type Object
   */
  previousSearchCriteria: null,

  /**
   * Index of selected record in App.resultsController
   *
   * @type int
   */
  selectedRecordIndex: -1,

  /**
   * Selected record to display in the dialog
   *
   * @type App.RepositoryConfigRecord
   */
  selectedRecord: null,


  /**
   * Select the record specified by the index
   * @param {int} recordIndex index of the selected record in App.resultsController
   */
  selectRecord: function(recordIndex) {
    var record = App.resultsController.objectAtContent(recordIndex);
    var nestedRecord = App.repositoryConfigEngine.edit(record.get(App.DOCUMENT_ID_RECORD_FIELD_NAME));
    App.pageController.set('selectedRecordIndex', recordIndex);
    App.pageController.set('selectedRecord', nestedRecord);
  },

  /**
   * Deselect the record specified by the index
   */
  deselectRecord: function() {
    var nestedRecord = App.pageController.get('selectedRecord');
    App.repositoryConfigEngine.discardChanges(nestedRecord);

    App.pageController.set('selectedRecordIndex', -1);
    App.pageController.set('selectedRecord', null);
  },

  /**
   * Flag to indicate if we can show the previous button
   *
   * @type Boolean
   */
  canShowPreviousRecord: function() {
    var selectedRecordIndex = App.pageController.get('selectedRecordIndex');
    if (selectedRecordIndex <= 0) {
      return NO;
    }

    var recordStatus = this.getPath('authenticatedUserRecord.status');
     if (!SC.none(recordStatus) && recordStatus !== SC.Record.READY_CLEAN && !this.get('isSaving')) {
      return NO;
    }

    return YES;
  }.property('selectedRecordIndex', '*selectedRecord.status').cacheable(),

  /**
   * Flag to indicate if we can show the next button
   *
   * @type Boolean
   */
  canShowNextRecord: function() {
    var selectedRecordIndex = App.pageController.get('selectedRecordIndex');
    if (selectedRecordIndex === App.resultsController.get('length') - 1) {
      return NO;
    }

    var recordStatus = this.getPath('authenticatedUserRecord.status');
     if (!SC.none(recordStatus) && recordStatus !== SC.Record.READY_CLEAN && !this.get('isSaving')) {
      return NO;
    }

    return YES;
  }.property('selectedRecordIndex', '*selectedRecord.status').cacheable()

});

/**
 * @class
 * Proxy user results in the store
 */
App.resultsController = SC.ArrayProxy.create({
  content: []
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

      showDialog: function(recordIndex) {
        recordIndex = parseInt(recordIndex);
        App.pageController.selectRecord(recordIndex);
        this.gotoState('showingDialog');
      }
    }),

    showingDialog: SC.State.extend({
      enterState: function() {
        $('#repoDialog').dialog('open')
      },

      exitState: function() {
        App.pageController.deselectRecord();
        $('#repoDialog').dialog('close')
      },

      hideDialog: function() {
        this.gotoState('notSearching');
      },

      showPreviousRecord: function() {
        var recordIndex = App.pageController.get('selectedRecordIndex');
        if (recordIndex === 0 ) {
          return;
        }

        // Discard selected record
        App.pageController.deselectRecord();

        // Show previous
        recordIndex = recordIndex - 1;
        App.pageController.selectRecord(recordIndex);
      },

      showNextRecord: function() {
        var recordIndex = App.pageController.get('selectedRecordIndex');
        if (recordIndex === App.resultsController.get('length') - 1) {
          return;
        }

        // Discard selected record
        App.pageController.deselectRecord();

        // Show next
        recordIndex = recordIndex + 1;
        App.pageController.selectRecord(recordIndex);
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
          App.repositoryConfigEngine.clearData();

          // Final criteria
          var criteria = {
            name: App.pageController.getPath('repositoryNamet'),
            startPage: 1,
            recordsPerPage: App.pageController.get('rowsPerSearch'),
            doPageCount: 'false'
          };
          App.repositoryConfigEngine.search(criteria, this, this.endSearch);

          // Save criteria for show more
          App.pageController.set('previousSearchCriteria', criteria);
        }
        catch (err) {
          // End search with error
          this.endSearch(null, err);
        }
      },

      /**
       * Called back when search returns
       * @param documentID
       * @param records
       * @param params
       * @param error
       */
      endSearch: function(params, error) {
        if (SC.none(error)) {
          //Cannot use App.resultsController.get('length'); to get length because binding has not happened in the runloop
          var recordCount = App.store.find(App.RepositoryConfigRecord).get('length');
          App.pageController.set('showResults', recordCount > 0);
          App.pageController.set('showNoRowsFound', recordCount === 0);
          App.pageController.set('canShowMore', recordCount === App.pageController.get('rowsPerSearch'));
        } else {
          App.pageController.set('errorMessage', error);
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
      endShowMore: function(params, error) {
        if (SC.none(error)) {
          //Cannot use App.resultsController.get('length'); to get length because binding has not happened in the runloop
          var recordCount = App.store.find(App.RepositoryConfigRecord).get('length');
          App.pageController.set('canShowMore', recordCount % App.pageController.get('rowsPerSearch') == 0);
        } else {
          App.pageController.set('errorMessage', error);
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
  App.repositoryRuntimeEngine.load();
  App.resultsController.set('content', App.repositoryConfigEngine.getRecords());

  App.statechart.initStatechart();
} else {
  // Not logged in so go to login page
  window.location = 'login.html?returnTo=' + encodeURIComponent(App.pageFileName);
}
