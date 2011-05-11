// ==========================================================================
// Project:   Chililog.mainPaneController
// Copyright: ©2011 My Company, Inc.
// ==========================================================================

sc_require('controllers/view_controller_mixin');

/**********************************************************************************************************************
 * Users
 **********************************************************************************************************************/

/**
 * List users
 */
Chililog.configureUserListViewController = SC.ArrayController.create({
  /**
   * Show list of users in the right hand side details pane
   */
  show: function() {
    Chililog.configureView.setPath('right.scenes', ['Chililog.configureUserListView', 'Chililog.configureUserDetailView']);
    Chililog.configureView.setPath('right.nowShowing', 'Chililog.configureUserListView');
    return;
  },

  /**
   * Trigger event to create a new user
   */
  create: function() {
    Chililog.statechart.sendEvent('createUser');
  },

  /**
   * User double clicked on record so edit it
   */
  edit: function() {
    var selectionSet = this.get('selection');
    if (SC.none(selectionSet) || selectionSet.get('length') === 0) {
      return null;
    }
    var selection = selectionSet.get('firstObject');
    var id = selection.get(Chililog.DOCUMENT_ID_RECORD_FIELD_NAME);
    Chililog.statechart.sendEvent('editUser', id);
  },

  /**
   * Flag to indicate if we are in the process of refreshing
   */
  isRefreshing: NO,

  /**
   * Since this is a simple async call, skip the statechart and directly call the data controller
   */
  refresh: function() {
    Chililog.userDataController.synchronizeWithServer(NO, this, this.endRefresh);
    this.set('isRefreshing', YES);
  },

  /**
   * Finish refreshing
   */
  endRefresh: function() {
    this.set('isRefreshing', NO);
  }

});

/**
 * Controls the data when configuring users
 */
Chililog.configureUserDetailViewController = SC.ObjectController.create(Chililog.ViewControllerMixin, {

  /**
   * User record to display
   * @type Chililog.UserRecord
   */
  content: null,

  /**
   * Flag to indicate if we are creating
   */
  title: function() {
    var record = this.get('content');
    if (SC.none(record)) {
      return '';
    }
    if (record.get(Chililog.DOCUMENT_VERSION_RECORD_FIELD_NAME) === 0) {
      return '_configureUserDetailView.CreateTitle'.loc();
    } else {
      return '_configureUserDetailView.EditTitle'.loc(record.get('username'));
    }
  }.property('content').cacheable(),

  /**
   * Flag to indicate if we are creating
   */
  isCreating: function() {
    var record = this.get('content');
    if (!SC.none(record) && record.get(Chililog.DOCUMENT_VERSION_RECORD_FIELD_NAME) === 0) {
      return YES;
    }
    return NO;
  }.property('content').cacheable(),

  /**
   * Adjust height of body box depending on if we are adding or not
   */
  bodyLayout: function() {
    if (this.get('isCreating')) {
      return { top: 0, left: 0, right: 0, height: 450 };
    } else {
      return { top: 0, left: 0, right: 0, height: 350 };
    }
  }.property('isCreating').cacheable(),

  /**
   * Adjust height of buttons depending on if we are adding or not
   */
  buttonsLayout: function() {
    if (this.get('isCreating')) {
      return {top: 370, left: 0, right: 0, height: 50 };
    } else {
      return {top: 270, left: 0, right: 0, height: 50 };
    }
  }.property('isCreating').cacheable(),

  /**
   * Show the user details form
   */
  show: function() {
    Chililog.configureView.setPath('right.nowShowing', 'Chililog.configureUserDetailView');

    // Set scroller to top of page
    Chililog.configureUserDetailView.setPath('body.verticalScrollOffset', 0);

    // Need to delay setting focus because our scene view takes focus so we have to wait until that finishes first
    var field = Chililog.configureUserDetailView.getPath('body.contentView.username.field');
    this.invokeLater(function() {
      field.becomeFirstResponder();
    }, 400);
  },

  /**
   * Flag to indicate if the user's profile can be saved.
   * Can only be saved if form is loaded and the data has changed
   *
   * @type Boolean
   */
  canSave: function() {
    var recordStatus = this.getPath('content.status');
    if (!SC.none(recordStatus) && recordStatus !== SC.Record.READY_CLEAN && !this.get('isSaving')) {
      return YES;
    }
    return NO;
  }.property('content.status', 'isSaving').cacheable(),

  /**
   * Flag to indicate if we are in the middle of trying to save a profile
   */
  isSaving: NO,

  /**
   * Trigger event to save the user's profile
   */
  save: function() {
    // Check field values
    var rootView = Chililog.configureUserDetailView.getPath('body.contentView');
    var result = this.findFieldAndValidate(rootView);

    // Special cross field checks here
    if (result === SC.VALIDATE_OK) {
      if (this.get('isCreating')) {
        if (this.get('password') !== this.get('confirmPassword')) {
          result = Chililog.$error('_configureUserDetailView.ConfirmPassword.Invalid'.loc(), null,
            Chililog.configureUserDetailView.getPath('body.contentView.password.field'));
        }
      }
    }

    if (result !== SC.VALIDATE_OK) {
      this.showError(result);
      return;
    }

    Chililog.statechart.sendEvent('save');
  },

  /**
   * Confirm erase
   */
  confirmErase: function() {
    SC.AlertPane.warn({
      message: '_configureUserDetailView.ConfirmDelete'.loc(this.getPath('content.username')),
      buttons: [
        {
          title: '_delete'.loc(),
          action: this.erase
        },
        {
          title: '_cancel'.loc()
        }
      ]
    });
  },

  /**
   * Trigger event to delete the user. This is called back from confirmErase
   */
  erase: function() {
    var record = Chililog.configureUserDetailViewController.get('content');
    Chililog.statechart.sendEvent('eraseUser', record.get(Chililog.DOCUMENT_ID_RECORD_FIELD_NAME));
  },

  /**
   * Trigger event to prompt to discard changes
   */
  discardChanges: function() {
    Chililog.statechart.sendEvent('discardChanges');
  },

  /**
   * Trigger event to discard changes and go back to the view page
   */
  back: function() {
    Chililog.statechart.sendEvent('discardChanges');
  },

  /**
   * Show success message when profile successfully saved
   */
  showSaveSuccess: function() {
    var view = Chililog.configureUserDetailView.get('successMessage');
    var field = Chililog.configureUserDetailView.getPath('body.contentView.username.field');

    if (!SC.none(view)) {
      // Have to invokeLater because of webkit
      // http://groups.google.com/group/sproutcore/browse_thread/thread/482740f497d80462/cba903f9cc6aadf8?lnk=gst&q=animate#cba903f9cc6aadf8
      view.adjust("opacity", 1);
      this.invokeLater(function() {
        view.animate("opacity", 0, { duration: 4, timing:'ease-in' });
      }, 10);
    }

    this.setFocusOnField(field);
  }

});

/**********************************************************************************************************************
 * Repositories
 **********************************************************************************************************************/

/**
 * Controls the data when configuring users
 */
Chililog.configureRepositoryInfoListViewController = SC.ArrayController.create(Chililog.ViewControllerMixin, {

  /**
   * Show list of repositories in the right hand side details pane
   */
  show: function() {
    Chililog.configureView.setPath('right.scenes', ['Chililog.configureRepositoryInfoListView', 'Chililog.configureRepositoryInfoDetailView']);
    Chililog.configureView.setPath('right.nowShowing', 'Chililog.configureRepositoryInfoListView');
    return;
  },

  /**
   * Trigger event to create a new user
   */
  create: function() {
    Chililog.statechart.sendEvent('createRepositoryInfo');
  },

  /**
   * User double clicked on record so edit it
   */
  edit: function() {
    var selectionSet = this.get('selection');
    if (SC.none(selectionSet) || selectionSet.get('length') === 0) {
      return null;
    }
    var selection = selectionSet.get('firstObject');
    var id = selection.get(Chililog.DOCUMENT_ID_RECORD_FIELD_NAME);
    Chililog.statechart.sendEvent('editRepositoryInfo', id);
  },

  /**
   * Flag to indicate if we are in the process of refreshing
   */
  isRefreshing: NO,

  /**
   * Since this is a simple async call, skip the statechart and directly call the data controller
   */
  refresh: function() {
    Chililog.statechart.sendEvent('refresh');
  }
});

/**
 * Controls the data when configuring repositories
 */
Chililog.configureRepositoryInfoDetailViewController = SC.ObjectController.create(Chililog.ViewControllerMixin, {

  /**
   * Repository record to display
   * @type Chililog.RepositoryRecord
   */
  content: null,

  /**
   * Text to show in the success message text box
   */
  successMessage: null,

  /**
   * Address of the write queue
   */
  writeQueueAddress: function() {
    return 'repository.%@.write'.fmt(this.get('name'));
  }.property('name').cacheable(),

  /**
   * Login username to access the write queue
   */
  writeQueueUsername: function() {
    return this.get('name');
  }.property('name').cacheable(),

  /**
   * Address of the read queue
   */
  readQueueAddress: function() {
    return 'repository.%@.read'.fmt(this.get('name'));
  }.property('name').cacheable(),

  /**
   * Login username of the read queue
   */
  readQueueUsername: function() {
    return this.get('name');
  }.property('name').cacheable(),

  /**
   * Flag to indicate if we are creating
   */
  title: function() {
    var record = this.get('content');
    if (SC.none(record)) {
      return '';
    }
    if (record.get(Chililog.DOCUMENT_VERSION_RECORD_FIELD_NAME) === 0) {
      return '_configureRepositoryInfoDetailView.CreateTitle'.loc();
    } else {
      return '_configureRepositoryInfoDetailView.EditTitle'.loc(record.get('name'));
    }
  }.property('content').cacheable(),

  /**
   * Flag to indicate if we are creating
   */
  isCreating: function() {
    var record = this.get('content');
    if (!SC.none(record) && record.get(Chililog.DOCUMENT_VERSION_RECORD_FIELD_NAME) === 0) {
      return YES;
    }
    return NO;
  }.property('content').cacheable(),

  /**
   * Show the details form
   */
  show: function() {
    Chililog.configureView.setPath('right.nowShowing', 'Chililog.configureRepositoryInfoDetailView');

    // Set scroller to top of page
    Chililog.configureRepositoryInfoDetailView.setPath('body.verticalScrollOffset', 0);
    
    // Need to delay setting focus because our scene view takes focus so we have to wait until that finishes first
    this.setFocusOnField(Chililog.configureRepositoryInfoDetailView.getPath('body.contentView.name.field'), 400);
  },

  /**
   * Flag to indicate if the user's profile can be saved.
   * Can only be saved if form is loaded and the data has changed
   *
   * @type Boolean
   */
  canSave: function() {
    var recordStatus = this.getPath('content.status');
    if (!SC.none(recordStatus) && recordStatus !== SC.Record.READY_CLEAN && !this.get('isSaving')) {
      return YES;
    }
    return NO;
  }.property('content.status', 'isSaving').cacheable(),

  /**
   * Flag to indicate if we are in the middle of trying to save a profile
   */
  isSaving: NO,

  /**
   * Trigger event to save the user's profile
   */
  save: function() {
    // Check field values
    var rootView = Chililog.configureRepositoryInfoDetailView.getPath('body.contentView');
    var result = this.findFieldAndValidate(rootView);
    if (result !== SC.VALIDATE_OK) {
      this.showError(result);
      return;
    }

    // Check that page file size < max memory
    if (this.get('writeQueuePageSize') > this.get('writeQueueMaxMemory')) {
      this.showError(Chililog.$error('_configureRepositoryInfoDetailView.WriteQueuePageSize.InvalidSize',
        [this.get('writeQueuePageSize'), this.get('writeQueueMaxMemory')],
        Chililog.configureRepositoryInfoDetailView.getPath('body.contentView.writeQueueAttributes.writeQueuePageSize.field')));
      return;
    }

    Chililog.statechart.sendEvent('save');
  },

  /**
   * Confirm erase
   */
  confirmErase: function() {
    SC.AlertPane.warn({
      message: '_configureRepositoryInfoDetailView.ConfirmDelete'.loc(this.getPath('content.name')),
      buttons: [
        {
          title: '_delete'.loc(),
          action: this.erase
        },
        {
          title: '_cancel'.loc()
        }
      ]
    });
  },

  /**
   * Trigger event to delete the user. This is called back from confirmErase
   */
  erase: function() {
    var record = Chililog.configureRepositoryInfoDetailViewController.get('content');
    Chililog.statechart.sendEvent('eraseRepositoryInfo', record.get(Chililog.DOCUMENT_ID_RECORD_FIELD_NAME));
  },

  /**
   * Trigger event to prompt to discard changes
   */
  discardChanges: function() {
    Chililog.statechart.sendEvent('discardChanges');
  },

  /**
   * Trigger event to discard changes and go back to the view page
   */
  back: function() {
    Chililog.statechart.sendEvent('discardChanges');
  },

  /**
   * Show success message when profile successfully saved
   */
  showSaveSuccess: function() {
    var view = Chililog.configureRepositoryInfoDetailView.get('successMessage');
    var field = Chililog.configureRepositoryInfoDetailView.getPath('body.contentView.name.field');
    this.set('successMessage', '_successMessage'.loc());

    if (!SC.none(view)) {
      // Have to invokeLater because of webkit
      // http://groups.google.com/group/sproutcore/browse_thread/thread/482740f497d80462/cba903f9cc6aadf8?lnk=gst&q=animate#cba903f9cc6aadf8
      view.adjust("opacity", 1);
      this.invokeLater(function() {
        view.animate("opacity", 0, { duration: 4, timing:'ease-in' });
      }, 10);
    }

    this.setFocusOnField(field);
  }

});

/**********************************************************************************************************************
 * Main
 **********************************************************************************************************************/

/**
 * Controls the data when configuring repositories
 */
Chililog.configureViewController = SC.Object.create({

  onSelect: function() {
    var selectionSet = Chililog.configureView.getPath('left.contentView.selection');
    if (SC.none(selectionSet) || selectionSet.get('length') === 0) {
      return null;
    }
    var selection = selectionSet.get('firstObject');
    var id = selection['id'];
    if (id === 'Users') {
      Chililog.statechart.sendEvent('viewUsers');
    } else if (id === 'Repositories') {
      Chililog.statechart.sendEvent('viewRepositoryInfo');
    }

    return;
  }
});

