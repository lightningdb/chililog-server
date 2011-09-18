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
// Javascript for admin_repos.html
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
 * Button to add a new user
 */
App.AddButton = App.ButtonView.extend({
  label: '_admin.repo.create'.loc(),
  disabledBinding: SC.Binding.from('App.pageController.isSearching').oneWay().bool(),

  click: function() {
    App.statechart.sendAction('createRepo');
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
          App.statechart.sendAction('showRepo', $('#' + this.id).attr('contentIndex'));
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

  title: '',

  didTitleChange: function() {
    var title = App.pageController.get('dialogTitle');
    this.$().dialog('option', 'title', title);
  }.observes('App.pageController.dialogTitle'),

  didInsertElement: function() {
    this._super();

    // JQuery UI dialog setup
    this.$().dialog({
      autoOpen: false,
      height: 360,
      width: 850,
      resizable: false,
      modal: true,
      close: function(event, ui) {
        // For when the X is clicked
        App.statechart.sendAction('cancel');
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
    App.statechart.sendAction('ok');
    return;
  },

  // Hide dialog on ESC key pressed
  cancel: function() {
    App.statechart.sendAction('cancel');
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
  helpMessageDidChange: function() {
    var msg = App.pageController.get('nameErrorMessage');
    this._updateHelp(msg, YES);
  }.observes('App.pageController.nameErrorMessage'),

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
  helpMessageDidChange: function() {
    var msg = App.pageController.get('storageQueueWorkerCountErrorMessage');
    this._updateHelp(msg, YES);
  }.observes('App.pageController.storageQueueWorkerCountErrorMessage'),

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
  helpMessageDidChange: function() {
    var msg = App.pageController.get('storageMaxKeywordsErrorMessage');
    this._updateHelp(msg, YES);
  }.observes('App.pageController.storageMaxKeywordsErrorMessage'),

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
  helpMessageDidChange: function() {
    var msg = App.pageController.get('maxMemoryErrorMessage');
    this._updateHelp(msg, YES);
  }.observes('App.pageController.maxMemoryErrorMessage'),

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
  helpMessageDidChange: function() {
    var msg = App.pageController.get('pageSizeErrorMessage');
    this._updateHelp(msg, YES);
  }.observes('App.pageController.pageSizeErrorMessage'),

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
  helpMessageDidChange: function() {
    var msg = App.pageController.get('pageCountCacheErrorMessage');
    this._updateHelp(msg, YES);
  }.observes('App.pageController.pageCountCacheErrorMessage'),

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
 * Button to show next log entry
 */
App.DialogRemoveButton = App.ButtonView.extend({
  label: '_remove'.loc(),
  title: '_removeTooltip'.loc(),
  isVisibleBinding: SC.Binding.from('App.pageController.canRemove').oneWay().bool(),

  click: function() {
    App.statechart.sendAction('remove');
    return;
  }
});

/**
 * @class
 * Button to save and close dialog window
 */
App.DialogOkButton = App.ButtonView.extend({
  label: '_ok'.loc(),
  disabledBinding: SC.Binding.from('App.pageController.isSavingOrRemoving').oneWay().bool(),

  click: function() {
    App.statechart.sendAction('ok');
    return;
  }
});

/**
 * @class
 * Button to discard changes and close window
 */
App.DialogCancelButton = App.ButtonView.extend({
  label: '_cancel'.loc(),
  disabledBinding: SC.Binding.from('App.pageController.canSave').oneWay().bool().not(),

  click: function() {
    App.statechart.sendAction('cancel');
    return;
  }
});

/**
 * @class
 * Button to save but not close dialog window
 */
App.DialogApplyButton = App.ButtonView.extend({
  label: '_apply'.loc(),
  disabledBinding: SC.Binding.from('App.pageController.canSave').oneWay().bool().not(),

  click: function() {
    App.statechart.sendAction('apply');
    return;
  }
});

/**
 * @class
 * Spinner displayed while saving or deleting
 */
App.DialogWorkingImage = App.ImgView.extend({
  src: 'images/working.gif',
  visible: NO,
  isVisibleBinding: SC.Binding.from('App.pageController.isSavingOrRemoving').oneWay().bool()
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
    SC.Object.create({label: '_admin.repo.startupStatus.online'.loc(), value: App.REPOSITORY_STATUS_ONLINE }),
    SC.Object.create({label: '_admin.repo.startupStatus.readonly'.loc(), value: App.REPOSITORY_STATUS_READONLY}),
    SC.Object.create({label: '_admin.repo.startupStatus.offline'.loc(), value: App.REPOSITORY_STATUS_OFFLINE})
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
    SC.Object.create({label: '_admin.repo.maxMemoryPolicy.page'.loc(), value: App.REPOSITORY_MAX_MEMORY_POLICY_PAGE}),
    SC.Object.create({label: '_admin.repo.maxMemoryPolicy.drop'.loc(), value: App.REPOSITORY_MAX_MEMORY_POLICY_DROP}),
    SC.Object.create({label: '_admin.repo.maxMemoryPolicy.block'.loc(), value: App.REPOSITORY_MAX_MEMORY_POLICY_BLOCK})
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
   * Error message to display for name field
   *
   * @type String
   */
  nameErrorMessage: '',

  /**
   * Error message to display for storage queue worker count
   *
   * @type String
   */
  storageQueueWorkerCountErrorMessage: '',

  /**
   * Error message for max keywords field
   *
   * @type String
   */
  storageMaxKeywordsErrorMessage: '',

  /**
   * Error message for max memory field
   *
   * @type String
   */
  maxMemoryErrorMessage: '',

  /**
   * Error message for page size field
   *
   * @type String
   */
  pageSizeErrorMessage: '',

  /**
   * Error message for page count field
   *
   * @type String
   */
  pageCountCacheErrorMessage: '',
  
  /**
   * Clear the dialog error messages
   */
  clearDialogErrors: function() {
    App.pageController.set('nameErrorMessage', '');
    App.pageController.set('storageQueueWorkerCountErrorMessage', '');
    App.pageController.set('storageMaxKeywordsErrorMessage', '');
    App.pageController.set('maxMemoryErrorMessage', '');
    App.pageController.set('maxMemoryErrorMessage', '');
    App.pageController.set('pageSizeErrorMessage', '');
    App.pageController.set('pageCountCacheErrorMessage', '');
  },

  /**
   * Indicates if we are currently saving
   *
   * @type Boolean
   */
  isSavingOrRemoving: NO,

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
   * Flag to indicate if the selected record is a new record
   */
  isNewSelectedRecord: function() {
    var documentVersion = this.getPath('selectedRecord.documentVersion');
    return SC.none(documentVersion) || documentVersion === 0;
  }.property('selectedRecord.documentVersion').cacheable(),

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
    App.pageController.clearDialogErrors();

    var nestedRecord = App.pageController.get('selectedRecord');
    if (!SC.none(nestedRecord)) {
      App.userEngine.discardChanges(nestedRecord);
    }

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
  }.property('selectedRecordIndex', '*selectedRecord.status').cacheable(),

  /**
   * Title for the dialog window
   * @type String
   */
  dialogTitle: function() {
    var selectedRecordIndex = App.pageController.get('selectedRecordIndex');
    if (selectedRecordIndex == -1) {
      return '_admin.repo.createTitle'.loc();
    } else {
      return '_admin.repo.editTitle'.loc(this.getPath('selectedRecord.username'));
    }
  }.property('selectedRecordIndex', 'selectedRecord.username').cacheable(),

  /**
   * Open the dialog
   * @param {int} recordIndex index of record in results array to display. If -1, then assume we want to create a new user
   */
  showDialog: function(recordIndex) {
    App.pageController.clearDialogErrors();
    if (recordIndex == -1) {
      App.pageController.set('confirmPassword', '');
      App.pageController.set('selectedRecordIndex', recordIndex);
      App.pageController.set('selectedRecord', App.repositoryConfigEngine.create());
    } else {
      App.pageController.selectRecord(recordIndex);
    }
    $('#repoDialog').dialog('open');
    $('#dialogNameField input').focus();
  },

  /**
   * Close the dialog
   */
  hideDialog: function() {
    App.pageController.deselectRecord();
    $('#repoDialog').dialog('close');
  }

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
     * Prompt the user to enter search criteria
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

      showRepo: function(recordIndex) {
        recordIndex = parseInt(recordIndex);
        App.pageController.showDialog(recordIndex);
        this.gotoState('showingDialog');
      },

      createRepo: function() {
        App.pageController.showDialog(-1);
        this.gotoState('showingDialog');
      }
    }),

    /**
     * Show repository dialog
     */
    showingDialog: SC.State.extend({
      enterState: function() {
      },

      exitState: function() {
      },

      hideDialog: function() {
        this.gotoState('notSearching');
      },

      /**
       * OK clicked - save and close dialog
       */
      ok: function() {
        // If record has not changed, then don't save
        var recordStatus = App.pageController.getPath('selectedRecord.status');
        if (!SC.none(recordStatus) && recordStatus === SC.Record.READY_CLEAN ) {
          this.cancel();
          return;
        }

        var ctx = { action: 'ok' };
        this.gotoState('saving', ctx);
      },

      /**
       * Cancel clicked - discard and close dialog
       */
      cancel: function() {
        App.pageController.hideDialog();
        this.gotoState('notSearching');
      },

      /**
       * Apply clicked - save and keep dialog open
       */
      apply: function() {
        var ctx = { action: 'apply' };
        this.gotoState('saving', ctx);
      },

      /**
       * Delete clicked - delete and close dialog
       */
      remove: function() {
        if (confirm('_admin.repo.confirmDelete'.loc())) {
          this.gotoState('removing');
        }
      },

      /**
       * Show prior to the selected record
       */
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

      /**
       * Show record after the selected record
       */
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
     * Call server to save our record
     */
    saving: SC.State.extend({

      enterState: function(ctx) {
        App.pageController.set('isSavingOrRemoving', YES);
        this._startSave(ctx.action === 'ok');
      },

      exitState: function() {
        App.pageController.set('isSavingOrRemoving', NO);
      },

      /**
       * Save selected record
       * @param {Boolean} closeWhenFinished If yes, we will exist the dialog of save is successful
       */
      _startSave: function(closeWhenFinished) {
        try {
          var selectedRecord = App.pageController.get('selectedRecord');
          if (!this._validate()) {
            this.gotoState('showingDialog');
            return;
          }

          // Call server
          var params = {closeWhenFinished: closeWhenFinished};
          App.repositoryConfigEngine.save(selectedRecord, this, this._endSave, params);
        }
        catch (err) {
          // End search with error
          this._endSave(null, null, err);
        }
      },

      /**
       * Validate the dialog data
       * @returns Boolean YES if ok, NO if error
       */
      _validate: function() {
        App.pageController.clearDialogErrors();

        var isError = NO;

        var selectedRecord = App.pageController.get('selectedRecord');
        var name = selectedRecord.get('name');
        if (SC.empty(name)) {
          App.pageController.set('nameErrorMessage', '_admin.repo.username.required'.loc());
          isError = YES;
        }

        return !isError;
      },

      /**
       * Called back when save is finished
       * @param documentID DocumentID of the user record that was saved
       * @param params context params passed in startSave
       * @param error Error object. Null if no error.
       */
      _endSave: function(documentID, params, error) {
        if (SC.none(error)) {
          // Find the correct index and select record again
          for (var i=0; i < App.resultsController.get('length'); i++) {
            var userRecord = App.resultsController.objectAtContent(i);
            if (userRecord.get(App.DOCUMENT_ID_RECORD_FIELD_NAME) === documentID) {
              App.pageController.selectRecord(i);
              break;
            }
          }

          if (params.closeWhenFinished) {
            App.pageController.hideDialog();
            this.gotoState('notSearching');
          } else {
            this.gotoState('showingDialog');
          }
        } else {
          alert('Error: ' + error.message);
          this.gotoState('showingDialog');
        }
      }
    }),

    /**
     * Call server to delete our record
     */
    removing: SC.State.extend({

      enterState: function(ctx) {
        App.pageController.set('isSavingOrRemoving', YES);
        this._startRemove();
      },

      exitState: function() {
        App.pageController.set('isSavingOrRemoving', NO);
      },

      /**
       * Save selected record
       * @param {Boolean} closeWhenFinished If yes, we will exist the dialog of save is successful
       */
      _startRemove: function(closeWhenFinished) {
        try {
          var selectedRecord = App.pageController.get('selectedRecord');
          var documentID = selectedRecord.get(App.DOCUMENT_ID_RECORD_FIELD_NAME);
          App.repositoryConfigEngine.remove(documentID, this, this._endRemove);
        }
        catch (err) {
          // End search with error
          this._endRemove(null, null, err);
        }
      },

      /**
       * Called back when delete is finished
       * @param documentID DocumentID of the user record that was saved
       * @param params context params passed in startSave
       * @param error Error object. Null if no error.
       */
      _endRemove: function(documentID, params, error) {
        if (SC.none(error)) {
          App.pageController.hideDialog();
          this.gotoState('notSearching');
        } else {
          alert('Error: ' + error.message);
          this.gotoState('showingDialog');
        }
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
          App.pageController.set('errorMessage', error.message);
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
          App.pageController.set('errorMessage', error.message);
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
