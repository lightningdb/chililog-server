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
// Main Page Views
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
    App.statechart.sendAction('search');
    return;
  }
});

/**
 * @class
 * Button to add a new user
 */
App.AddButton = App.ButtonView.extend({
  label: '_admin.user.create'.loc(),
  disabledBinding: SC.Binding.from('App.pageController.isSearching').oneWay().bool(),

  click: function() {
    App.statechart.sendAction('createUser');
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
          App.statechart.sendAction('showUser', $('#' + this.id).attr('contentIndex'));
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

// --------------------------------------------------------------------------------------------------------------------
// User Dialog Views
// --------------------------------------------------------------------------------------------------------------------

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
      height: 440,
      width: 850,
      resizable: false,
      modal: true,
      close: function(event, ui) {
        // For when the X is clicked
        App.statechart.sendAction('cancel');
      }
    });

    // Add event handler to tab <a>. Delegate does it for current and future tabs
    this.$().delegate('ul.tabs li > a', 'click', function(e) {
      var $this = $(this);
      var href = $this.attr('href');

      if (/^#\w+/.test(href)) {
        e.preventDefault();
      }

      App.viewUtils.activateTab($this);
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
App.DialogUserNameField = App.FieldView.extend({
  label: '_admin.user.username'.loc(),
  isRequired: YES,
  help: '_admin.user.username.help'.loc(),
  helpMessageDidChange: function() {
    var msg = App.pageController.get('usernameErrorMessage');
    if (SC.empty(msg)) {
      this._updateHelp('_admin.user.username.help'.loc(), NO);
    } else {
      this._updateHelp(msg, YES);
    }
  }.observes('App.pageController.usernameErrorMessage'),

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
  isRequired: YES,
  helpMessageDidChange: function() {
    var msg = App.pageController.get('emailAddressErrorMessage');
    this._updateHelp(msg, YES);
  }.observes('App.pageController.emailAddressErrorMessage'),

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
        for (var i = 0; i < options.length; i++) {
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
 * Email Address Field
 */
App.DialogPasswordField = App.FieldView.extend({
  label: '_admin.user.password'.loc(),
  isRequiredBinding: SC.Binding.from('App.pageController.isNewSelectedRecord').oneWay().bool(),
  helpMessageDidChange: function() {
    var isNewRecord = App.pageController.get('isNewSelectedRecord');
    var msg = App.pageController.get('passwordErrorMessage');
    if (!isNewRecord && SC.empty(msg)) {
      this._updateHelp('_admin.user.password.editHelp'.loc(), NO);
    } else {
      this._updateHelp(msg, YES);
    }
  }.observes('App.pageController.passwordErrorMessage', 'App.pageController.isNewSelectedRecord'),

  DataView : App.TextBoxView.extend(App.DialogFieldDataMixin, {
    classNames: 'large'.w(),
    type: 'password',
    valueBinding: 'App.pageController.selectedRecord.password'
  })
});

/**
 * @class
 * Email Address Field
 */
App.DialogConfirmPasswordField = App.FieldView.extend({
  label: '_admin.user.confirmPassword'.loc(),
  isRequiredBinding: SC.Binding.from('App.pageController.isNewSelectedRecord').oneWay().bool(),
  helpMessageDidChange: function() {
    var msg = App.pageController.get('confirmPasswordErrorMessage');
    this._updateHelp(msg, YES);
  }.observes('App.pageController.confirmPasswordErrorMessage'),

  DataView : App.TextBoxView.extend(App.DialogFieldDataMixin, {
    classNames: 'large'.w(),
    type: 'password',
    valueBinding: 'App.pageController.confirmPassword'
  })
});

/**
 * @class
 * Is System Administrator Field
 */
App.DialogIsSystemAdministratorField = App.FieldView.extend({
  label: '_admin.user.isSystemAdministrator'.loc(),
  DataView : App.SelectView.extend(App.DialogFieldDataMixin, {
    classNames: 'xxlarge'.w(),
    contentBinding: 'App.pageController.isSystemAdministratorOptions',
    valueBinding: SC.Binding.from('App.pageController.selectedRecord.isSystemAdministrator').transform(function(value, isForward) {
      if (isForward) {
        var options = App.pageController.get('isSystemAdministratorOptions');
        for (var i = 0; i < options.length; i++) {
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
 * Repository Access Field
 */
App.DialogRepoAccessField = App.FieldView.extend({
  label: '_admin.user.repositoryAccesses'.loc(),
  DataView : App.SelectView.extend(App.DialogFieldDataMixin, {
    classNames: 'xxlarge tall'.w(),
    multiple: YES,
    contentBinding: 'App.pageController.selectedRecordRepositoryAccesses'

  }),

  AddButtonView: App.ButtonView.extend({
    label: '_add'.loc(),
    disabled: NO,
    click: function() {
      App.statechart.sendAction('addRepositoryAccesses');
      return;
    }
  }),

  RemoveButtonView: App.ButtonView.extend({
    label: '_removeSelection'.loc(),
    disabledBinding: SC.Binding.from('App.pageController.selectedRecordRepositoryAccesses.selection').transform(function(value, isForward) {
      if (isForward) {
        if (SC.none(value)) {
          return YES;
        }
        return value.get('length') == 0;
      } else {
        return value;
      }
    }),
    click: function() {
      App.statechart.sendAction('removeRepositoryAccesses');
      return;
    }
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
// Add Repository Access Dialog Views
// --------------------------------------------------------------------------------------------------------------------

/**
 * @class
 * Dialog for adding repository access
 */
App.RepositoryAccessDialog = SC.View.extend({
  attributeBindings: ['title'],

  title: '_admin.user.addRepoAccess'.loc(),

  didInsertElement: function() {
    this._super();

    // JQuery UI dialog setup
    this.$().dialog({
      autoOpen: false,
      height: 220,
      width: 550,
      resizable: false,
      modal: true,
      close: function(event, ui) {
        // For when the X is clicked
        App.statechart.sendAction('cancelAdd');
      }
    });
  }

});


/**
 * @class
 * Specifies the properties of a SC.RepositoryStatusRecord that should be used for the select option label and value
 */
App.RepositoryNameSelectOption = App.SelectOption.extend({
  labelBinding: '*content.name',
  valueBinding: '*content.documentID'
});

/**
 * @class
 * Repository Field
 */
App.DialogRepositoryNameField = App.FieldView.extend({
  label: '_admin.user.repositoryAccesses.repository'.loc(),
  DataView : App.SelectView.extend(App.DialogFieldDataMixin, {
    classNames: 'xlarge'.w(),
    contentBinding: 'App.pageController.addRepositoryNameOptions',
    itemViewClass: App.RepositoryNameSelectOption,
    valueBinding: 'App.pageController.addRepositoryName'
  })
});

/**
 * @class
 * Role Field
 */
App.DialogRepositoryRoleField = App.FieldView.extend({
  label: '_admin.user.repositoryAccesses.role'.loc(),
  DataView : App.SelectView.extend(App.DialogFieldDataMixin, {
    classNames: 'xlarge'.w(),
    contentBinding: 'App.pageController.addRepositoryRoleOptions',
    valueBinding: 'App.pageController.addRepositoryRole'
  })
});


/**
 * @class
 * Button to Add repository access
 */
App.DialogRepositoryAccessAddButton = App.ButtonView.extend({
  label: '_add'.loc(),
  click: function() {
    App.statechart.sendAction('add');
    return;
  }
});

/**
 * @class
 * Button to cancel adding repository access
 */
App.DialogRepositoryAccessCancelButton = App.ButtonView.extend({
  label: '_cancel'.loc(),
  click: function() {
    App.statechart.sendAction('cancelAdd');
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
   * Indicates if we are currently searching or not
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
   * Error message to display for username
   *
   * @type String
   */
  usernameErrorMessage: '',

  /**
   * Error message to display for email address
   *
   * @type String
   */
  emailAddressErrorMessage: '',

  /**
   * Error message to display for password
   *
   * @type String
   */
  passwordErrorMessage: '',

  /**
   * Error message to display for confirm password
   *
   * @type String
   */
  confirmPasswordErrorMessage: '',

  /**
   * Data entered into the confirm password textbox
   *
   * @type String
   */
  confirmPassword: '',

  /**
   * Clear the dialog error messages
   */
  clearDialogErrors: function() {
    App.pageController.set('usernameErrorMessage', '');
    App.pageController.set('emailAddressErrorMessage', '');
    App.pageController.set('passwordErrorMessage', '');
    App.pageController.set('confirmPasswordErrorMessage', '');
  },

  /**
   * Options for user's status
   * @type Array of SC.Object
   */
  currentStatusOptions: [
    SC.Object.create({label: '_admin.user.currentStatus.enabled'.loc(), value: App.USER_STATUS_ENABLED}),
    SC.Object.create({label: '_admin.user.currentStatus.disabled'.loc(), value: App.USER_STATUS_DISABLED}),
    SC.Object.create({label: '_admin.user.currentStatus.locked'.loc(), value: App.USER_STATUS_LOCKED})
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
   * ArrayProxy wrapper around repository access
   *
   * @type SC.ArrayProxy
   */
  selectedRecordRepositoryAccesses: SC.ArrayProxy.create({ content: [] }),

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
    var nestedRecord = App.userEngine.edit(record.get(App.DOCUMENT_ID_RECORD_FIELD_NAME));
    App.pageController.set('selectedRecordIndex', recordIndex);
    App.pageController.set('selectedRecord', nestedRecord);
    App.pageController.setPath('selectedRecordRepositoryAccesses.content', nestedRecord.get('repositoryAccesses'));
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
   * Flag to indicate if we can show the cancel or apply buttons
   *
   * @type Boolean
   */
  canSave: function() {
    var recordStatus = this.getPath('selectedRecord.status');
    if (!SC.none(recordStatus) && recordStatus !== SC.Record.READY_CLEAN && !this.get('isSavingOrRemoving')) {
      return YES;
    }

    return NO;
  }.property('selectedRecord.status', 'isSavingOrRemoving').cacheable(),

  /**
   * Flag to indicate if we can show the delete button
   *
   * @type Boolean
   */
  canRemove: function() {
    var recordStatus = this.getPath('selectedRecord.status');
    if (!SC.none(recordStatus) && recordStatus === SC.Record.READY_CLEAN && !this.get('isSavingOrRemoving')) {
      return YES;
    }

    return NO;
  }.property('selectedRecord.status', 'isSavingOrRemoving').cacheable(),

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

    var recordStatus = this.getPath('selectedRecord.status');
    if (!SC.none(recordStatus) && recordStatus !== SC.Record.READY_CLEAN && !this.get('isSavingOrRemoving')) {
      return NO;
    }

    return YES;
  }.property('selectedRecordIndex', 'selectedRecord.status', 'isSavingOrRemoving').cacheable(),

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

    var recordStatus = this.getPath('selectedRecord.status');
    if (!SC.none(recordStatus) && recordStatus !== SC.Record.READY_CLEAN && !this.get('isSavingOrRemoving')) {
      return NO;
    }

    return YES;
  }.property('selectedRecordIndex', 'selectedRecord.status', 'isSavingOrRemoving').cacheable(),

  /**
   * Title for the dialog window
   * @type String
   */
  dialogTitle: function() {
    var selectedRecordIndex = App.pageController.get('selectedRecordIndex');
    if (selectedRecordIndex == -1) {
      return '_admin.user.createTitle'.loc();
    } else {
      return '_admin.user.editTitle'.loc(this.getPath('selectedRecord.username'));
    }
  }.property('selectedRecordIndex', 'selectedRecord.username').cacheable(),

  /**
   * Open the dialog
   * @param {int} recordIndex index of record in results array to display. If -1, then assume we want to create a new user
   */
  showDialog: function(recordIndex) {
    App.pageController.clearDialogErrors();
    if (recordIndex == -1) {
      var nestedRecord = App.userEngine.create();
      App.pageController.set('confirmPassword', '');
      App.pageController.set('selectedRecordIndex', recordIndex);
      App.pageController.set('selectedRecord', nestedRecord);
      App.pageController.setPath('selectedRecordRepositoryAccesses.content', nestedRecord.get('repositoryAccesses'));
    } else {
      App.pageController.selectRecord(recordIndex);
    }
    $('#userDialog').dialog('open');
    App.viewUtils.activateTab($('#dialogGeneralTab'));
    $('#dialogUserNameField input').focus();
  },

  /**
   * Close the dialog
   */
  hideDialog: function() {
    App.pageController.deselectRecord();
    $('#userDialog').dialog('close');
  },

  /**
   * Validate the dialog data
   * @returns Boolean YES if ok, NO if error
   */
  validateDialog: function() {
    App.pageController.clearDialogErrors();

    var isError = NO;

    var selectedRecord = App.pageController.get('selectedRecord');
    var username = selectedRecord.get('username');
    if (SC.empty(username)) {
      App.pageController.set('usernameErrorMessage', '_admin.user.username.required'.loc());
      isError = YES;
    } else if (!App.viewValidators.checkCode(username)) {
      App.pageController.set('usernameErrorMessage', '_admin.user.username.invalid'.loc(username));
      isError = YES;
    }

    var emailAddress = selectedRecord.get('emailAddress');
    if (SC.empty(emailAddress)) {
      App.pageController.set('emailAddressErrorMessage', '_admin.user.emailAddress.required'.loc());
      isError = YES;
    } else if (!App.viewValidators.checkEmailAddress(emailAddress)) {
      App.pageController.set('emailAddressErrorMessage', '_admin.user.emailAddress.invalid'.loc(emailAddress));
      isError = YES;
    }

    var documentVersion = selectedRecord.get(App.DOCUMENT_VERSION_RECORD_FIELD_NAME);
    if (documentVersion === 0) {
      // New user - password is required
      var password = selectedRecord.get('password');
      var confirmPassword = App.pageController.get('confirmPassword');
      if (SC.empty(password)) {
        App.pageController.set('passwordErrorMessage', '_admin.user.password.required'.loc());
        isError = YES;
      } else if (password.length < 8) {
        App.pageController.set('passwordErrorMessage', '_admin.user.password.invalid'.loc());
        isError = YES;
      }

      if (SC.empty(confirmPassword)) {
        App.pageController.set('confirmPasswordErrorMessage', '_admin.user.confirmPassword.required'.loc());
        isError = YES;
      } else if (password !== confirmPassword) {
        App.pageController.set('confirmPasswordErrorMessage', '_admin.user.confirmPassword.invalid'.loc());
        isError = YES;
      }
    } else {
      // Existing user - password is optional
      var password = selectedRecord.get('password');
      var confirmPassword = App.pageController.get('confirmPassword');
      if (!SC.empty(password)) {
        if (password.length < 8) {
          App.pageController.set('passwordErrorMessage', '_admin.user.password.invalid'.loc());
          isError = YES;
        }
        if (SC.empty(confirmPassword)) {
          App.pageController.set('confirmPasswordErrorMessage', '_admin.user.confirmPassword.required'.loc());
          isError = YES;
        } else if (password !== confirmPassword) {
          App.pageController.set('confirmPasswordErrorMessage', '_admin.user.confirmPassword.invalid'.loc());
          isError = YES;
        }
      }
    }

    // If error, then return to tab #1
    if (isError) {
      App.viewUtils.activateTab($('#dialogGeneralTab'));
    }

    return !isError;
  },

  /**
   * Name of selected repository to which the user will be granted access
   */
  addRepositoryName: '',

  /**
   * Options for repository
   */
  addRepositoryNameOptions: SC.ArrayProxy.create({ content: [] }),

  /**
   *Name of selected role with which the user will be granted access
   */
  addRepositoryRole: '',

  /**
   * Options for user's repository role
   * @type Array of SC.Object
   */
  addRepositoryRoleOptions: [
    SC.Object.create({label: '_admin.user.repositoryAccesses.workbenchRole'.loc(), value: App.REPOSITORY_WORKBENCH_ROLE}),
    SC.Object.create({label: '_admin.user.repositoryAccesses.publisherRole'.loc(), value: App.REPOSITORY_PUBLISHER_ROLE}),
    SC.Object.create({label: '_admin.user.repositoryAccesses.subscriberRole'.loc(), value: App.REPOSITORY_SUBSCRIBER_ROLE})
  ],

  /**
   * Open the repository access dialog
   */
  showRepositoryAccessDialog: function() {
    App.pageController.set('addRepositoryName', App.pageController.get('addRepositoryNameOptions').get('firstObject'));
    $('#repositoryAccessDialog').dialog('open');
    $('#dialogRepositoryNameField select').focus();
  },

  /**
   * Close the repository access dialog
   */
  hideRepositoryAccessDialog: function() {
    $('#repositoryAccessDialog').dialog('close');
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
     * Prompt the user to enter criteria
     */
    notSearching: SC.State.extend({
      enterState: function() {
      },

      exitState: function() {
      },

      search: function() {
        App.pageController.set('errorMessage', '');
        this.gotoState('searching');
      },

      showMore: function() {
        App.pageController.set('errorMessage', '');
        this.gotoState('showingMore');
      },

      showUser: function(recordIndex) {
        recordIndex = parseInt(recordIndex);
        App.pageController.showDialog(recordIndex);
        this.gotoState('showingDialog');
      },

      createUser: function() {
        App.pageController.showDialog(-1);
        this.gotoState('showingDialog');
      }
    }),

    /**
     * Currently showing modal dialog containing selected record
     */
    showingDialog: SC.State.extend({
      enterState: function() {
      },

      exitState: function() {
      },

      /**
       * OK clicked - save and close dialog
       */
      ok: function() {
        // If record has not changed, then don't save
        var recordStatus = App.pageController.getPath('selectedRecord.status');
        if (!SC.none(recordStatus) && recordStatus === SC.Record.READY_CLEAN) {
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
        if (confirm('_admin.user.confirmDelete'.loc())) {
          this.gotoState('removing');
        }
      },

      /**
       * Show the dialog
       */
      addRepositoryAccesses: function() {
        App.pageController.showRepositoryAccessDialog();
        this.gotoState('showingRepositoryAccessDialog');
      },

      /**
       * Remove selected repository accesses
       */
      removeRepositoryAccesses: function() {
        var arrayProxy = App.pageController.get('selectedRecordRepositoryAccesses');
        var selection = arrayProxy.get('selection');
        var array = arrayProxy.get('content');
        if (!SC.none(selection)) {
          selection.forEach(function(item) {
            arrayProxy.removeObject(item);
          });
        }
        arrayProxy.set('selection', []);
        App.pageController.setPath('selectedRecord.repositoryAccessesChanged', YES);
        return;
      },

      /**
       * Show prior to the selected record
       */
      showPreviousRecord: function() {
        var recordIndex = App.pageController.get('selectedRecordIndex');
        if (recordIndex === 0) {
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

    showingRepositoryAccessDialog:  SC.State.extend({
      enterState: function() {
      },

      exitState: function() {
      },

      add: function() {
        var repoName = App.pageController.getPath('addRepositoryName.name');
        var repoRole = App.pageController.getPath('addRepositoryRole.value');
        var roleLabel = App.USER_ROLE_MAP[repoRole];
        var role = 'repo.' + repoName +'.' + repoRole;

        var newAccess = {
          repository: repoName,
          role: repoRole,
          label: repoName + ' (' + roleLabel + ')',
          value: role
        };

        var arrayProxy = App.pageController.get('selectedRecordRepositoryAccesses');
        var found = NO;
        for (var i=0; i<arrayProxy.get('length'); i++) {
          if (arrayProxy.objectAt(i).value == role) {
            alert('_admin.user.repositoryAccesses.alreadyExists'.loc(repoName, roleLabel));
            found = YES;
            break;
          }
        }
        if (!found) {
          // Add to array
          arrayProxy.pushObject(newAccess);

          // Set dummy value to trigger record status change
          App.pageController.setPath('selectedRecord.repositoryAccessesChanged', YES);
        }

        App.pageController.hideRepositoryAccessDialog();
        this.gotoState('showingDialog');
      },

      cancelAdd: function() {
        App.pageController.hideRepositoryAccessDialog();
        this.gotoState('showingDialog');
        return NO;
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
          if (!App.pageController.validateDialog()) {
            this.gotoState('showingDialog');
            return;
          }

          // Call server
          var params = {closeWhenFinished: closeWhenFinished};
          App.userEngine.save(selectedRecord, this, this._endSave, params);
        }
        catch (err) {
          // End search with error
          this._endSave(null, null, err);
        }
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
          for (var i = 0; i < App.resultsController.get('length'); i++) {
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
          App.userEngine.remove(documentID, this, this._endRemove);
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
     * Executing a search. Block the user from entering data.
     */
    searching: SC.State.extend({
      enterState: function() {
        App.pageController.set('isSearching', YES);

        // Run later to give time for working icon animation to run
        SC.run.later(this, this._startSearch, 100);
      },

      exitState: function() {
        App.pageController.set('isSearching', NO);
      },

      /**
       * Asynchronously call server
       */
      _startSearch: function() {
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
          App.userEngine.search(criteria, this, this._endSearch);

          // Save criteria for show more
          App.pageController.set('previousSearchCriteria', criteria);
        }
        catch (err) {
          // End search with error
          this._endSearch(null, err);
        }
      },

      /**
       * Called back when search returns
       * @param params
       * @param error
       */
      _endSearch: function(params, error) {
        if (SC.none(error)) {
          //Cannot use App.resultsController.get('length'); to get length because binding has not happened in the runloop
          var recordCount = App.store.find(App.UserRecord).get('length');
          App.pageController.set('showResults', recordCount > 0);
          App.pageController.set('showNoRowsFound', recordCount === 0);
          App.pageController.set('canShowMore', recordCount === App.pageController.get('rowsPerSearch'));
        } else {
          App.pageController.set('errorMessage', error.message);
        }

        this.gotoState('notSearching');
      }
    }),

    /**
     * Fetching more records using the same search criteria
     */
    showingMore: SC.State.extend({
      enterState: function() {
        App.pageController.set('isSearching', YES);
        this._startShowMore();
      },

      exitState: function() {
        App.pageController.set('isSearching', NO);
      },

      /**
       * Get more records from the server
       */
      _startShowMore: function() {
        var criteria = App.pageController.get('previousSearchCriteria');
        criteria.startPage = criteria.startPage + 1;
        App.repositoryRuntimeEngine.find(criteria, this, this._endShowMore);
      },

      /**
       * Called back when search returns
       * @param documentID
       * @param records
       * @param params
       * @param error
       */
      _endShowMore: function(params, error) {
        if (SC.none(error)) {
          //Cannot use App.resultsController.get('length'); to get length because binding has not happened in the runloop
          var recordCount = App.store.find(App.UserRecord).get('length');
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
  App.resultsController.set('content', App.userEngine.getRecords());
  App.repositoryRuntimeEngine.load();

  var recordArray = App.repositoryRuntimeEngine.getRecords();
  App.pageController.get('addRepositoryNameOptions').set('content', recordArray);

  App.statechart.initStatechart();
} else {
  // Not logged in so go to login page
  window.location = 'login.html?returnTo=' + encodeURIComponent(App.pageFileName);
}
