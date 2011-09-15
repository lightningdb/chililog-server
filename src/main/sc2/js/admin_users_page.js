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
 * Username field
 */
App.UsernameField = App.StackedFieldView.extend({
  label: '_admin.user.username'.loc(),

  DataView : App.TextBoxView.extend(App.CriteriaFieldDataMixin, {
    classNames: 'large'.w(),
    valueBinding: 'App.pageController.username',
    disabledBinding: SC.Binding.from('App.pageController.isSearching').oneWay().bool()
  })
});

/**
 * @class
 * Username field
 */
App.EmailAddressField = App.StackedFieldView.extend({
  label: '_admin.user.emailAddress'.loc(),

  DataView : App.TextBoxView.extend(App.CriteriaFieldDataMixin, {
    classNames: 'large'.w(),
    valueBinding: 'App.pageController.emailAddress',
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

  usernameLabel: '_admin.user.username'.loc(),
  displayNameLabel: '_admin.user.displayName'.loc(),
  emailAddressLabel: '_admin.user.emailAddress'.loc(),
  currentStatusLabel: '_admin.user.currentStatus'.loc(),

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
  message: '_admin.user.noRowsFound'.loc(),
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

  title: '_admin.user.editTitle'.loc(),

  didInsertElement: function() {
    this._super();

    // JQuery UI dialog setup
    this.$().dialog({
      autoOpen: false,
      height: 400,
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
App.DialogUserNameField = App.FieldView.extend({
  label: '_admin.user.username'.loc(),
  help: '_admin.user.username.help'.loc(),
  DataView : App.TextBoxView.extend(App.DialogFieldDataMixin, {
    classNames: 'medium'.w(),
    valueBinding: 'App.pageController.selectedRecord.username'
  })
});

/**
 * @class
 * Display Name Field
 */
App.DialogDisplayNameField = App.FieldView.extend({
  label: '_admin.user.displayName'.loc(),
  DataView : App.TextBoxView.extend(App.DialogFieldDataMixin, {
    classNames: 'xlarge'.w(),
    valueBinding: 'App.pageController.selectedRecord.displayName'
  })
});

/**
 * @class
 * Email Address Field
 */
App.DialogEmailAddressField = App.FieldView.extend({
  label: '_admin.user.emailAddress'.loc(),
  DataView : App.TextBoxView.extend(App.DialogFieldDataMixin, {
    classNames: 'xlarge'.w(),
    valueBinding: 'App.pageController.selectedRecord.emailAddress'
  })
});

/**
 * @class
 * Status Field
 */
App.DialogStatusField = App.FieldView.extend({
  label: '_admin.user.currentStatus'.loc(),
  DataView : App.SelectView.extend(App.DialogFieldDataMixin, {
    classNames: 'xlarge'.w(),
    contentBinding: 'App.pageController.currentStatusOptions',
    valueBinding: SC.Binding.from('App.pageController.selectedRecord.currentStatus').transform(function(value, isForward) {
      if (isForward) {
        var options = App.pageController.get('currentStatusOptions');
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
  }),
  isVisibleBinding: SC.Binding.from('App.pageController.isNewSelectedRecord').oneWay().bool().not()
});

/**
 * @class
 * Email Address Field
 */
App.DialogPasswordField = App.FieldView.extend({
  label: '_admin.user.password'.loc(),
  DataView : App.TextBoxView.extend(App.DialogFieldDataMixin, {
    classNames: 'large'.w(),
    valueBinding: 'App.pageController.selectedRecord.password'
  }),
  isVisibleBinding: SC.Binding.from('App.pageController.isNewSelectedRecord').oneWay().bool()
});

/**
 * @class
 * Email Address Field
 */
App.DialogConfirmPasswordField = App.FieldView.extend({
  label: '_admin.user.confirmPassword'.loc(),
  DataView : App.TextBoxView.extend(App.DialogFieldDataMixin, {
    classNames: 'large'.w(),
    valueBinding: 'App.pageController.confirmPassword'
  }),
  isVisibleBinding: SC.Binding.from('App.pageController.isNewSelectedRecord').oneWay().bool()
});

/**
 * @class
 * Status Field
 */
App.DialogIsSystemAdministratorField = App.FieldView.extend({
  label: '_admin.user.isSystemAdministrator'.loc(),
  DataView : App.SelectView.extend(App.DialogFieldDataMixin, {
    classNames: 'xxlarge'.w(),
    contentBinding: 'App.pageController.isSystemAdministratorOptions',
    valueBinding: SC.Binding.from('App.pageController.selectedRecord.isSystemAdministrator').transform(function(value, isForward) {
      if (isForward) {
        var options = App.pageController.get('isSystemAdministratorOptions');
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
   * Username to search
   *
   * @type String
   */
  username: null,

  /**
   * User email address to search
   *
   * @type String
   */
  emailAddress: null,

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
   * Options for user's status
   * @type Array of SC.Object
   */
  currentStatusOptions: [
    SC.Object.create({label: '_admin.user.currentStatus.enabled'.loc(), value: 'ENABLED'}),
    SC.Object.create({label: '_admin.user.currentStatus.disabled'.loc(), value: 'DISABLED'}),
    SC.Object.create({label: '_admin.user.currentStatus.locked'.loc(), value: 'LOCKED'})
  ],

  /**
   * Options for user's system admin role
   * @type Array of SC.Object
   */
  isSystemAdministratorOptions: [
    SC.Object.create({label: '_admin.user.isSystemAdministrator.yes'.loc(), value: YES}),
    SC.Object.create({label: '_admin.user.isSystemAdministrator.no'.loc(), value: NO})
  ],

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
    var docID = this.getPath('selectedRecord.documentID');
    return SC.empty(docID);
  }.property('selectedRecord.documentID').cacheable(),

  /**
   * Select the record specified by the index
   * @param {int} recordIndex index of the selected record in App.resultsController
   */
  selectRecord: function(recordIndex) {
    var record = App.resultsController.objectAtContent(recordIndex);
    var nestedRecord = App.userEngine.edit(record.get(App.DOCUMENT_ID_RECORD_FIELD_NAME));
    App.pageController.set('selectedRecordIndex', recordIndex);
    App.pageController.set('selectedRecord', nestedRecord);
  },

  /**
   * Deselect the record specified by the index
   */
  deselectRecord: function() {
    var nestedRecord = App.pageController.get('selectedRecord');
    App.userEngine.discardChanges(nestedRecord);

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
        $('#userDialog').dialog('open')
      },

      exitState: function() {
        App.pageController.deselectRecord();
        $('#userDialog').dialog('close')
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
          App.userEngine.clearData();

          // Final criteria
          var criteria = {
            username: App.pageController.getPath('username'),
            email: App.pageController.get('emailAddress'),
            startPage: 1,
            recordsPerPage: App.pageController.get('rowsPerSearch'),
            doPageCount: 'false'
          };
          App.userEngine.search(criteria, this, this.endSearch);

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
          var recordCount = App.store.find(App.UserRecord).get('length');
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
          var recordCount = App.store.find(App.UserRecord).get('length');
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
  App.resultsController.set('content', App.userEngine.getRecords());

  App.statechart.initStatechart();
} else {
  // Not logged in so go to login page
  window.location = 'login.html?returnTo=' + encodeURIComponent(App.pageFileName);
}
