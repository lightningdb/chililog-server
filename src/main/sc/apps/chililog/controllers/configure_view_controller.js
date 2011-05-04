// ==========================================================================
// Project:   Chililog.mainPaneController
// Copyright: ©2011 My Company, Inc.
// ==========================================================================

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
  }
});

/**
 * Controls the data when configuring users
 */
Chililog.configureUserDetailViewController = SC.ObjectController.create({

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
    var field = Chililog.configureUserDetailView.getPath('body.contentView.username.field');
    // Need to delay setting focus because our scene view takes focus so we have to wait until that finishes first
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
   * Trigger event to discard changes to the user's profile
   */
  discardChanges: function() {
    Chililog.statechart.sendEvent('discardChanges');
  },

  /**
   * Show success message when profile successfully saved
   */
  showSaveSuccess: function() {
    var view = Chililog.configureUserDetailView.getPath('body.contentView.buttons.successMessage');
    var field = Chililog.configureUserDetailView.getPath('body.contentView.username.field');

    if (!SC.none(view)) {
      // Have to invokeLater because of webkit
      // http://groups.google.com/group/sproutcore/browse_thread/thread/482740f497d80462/cba903f9cc6aadf8?lnk=gst&q=animate#cba903f9cc6aadf8
      view.adjust("opacity", 1);
      this.invokeLater(function() {
        view.animate("opacity", 0, { duration: 4, timing:'ease-in' });
      }, 10);
    }

    field.becomeFirstResponder();
  },

  /**
   * Show error message when error happened why trying to save profile
   * @param {SC.Error} error
   */
  showSaveError: function(error) {
    if (SC.instanceOf(error, SC.Error)) {
      // Error
      var message = error.get('message');
      SC.AlertPane.error({ message: message });

      var label = error.get('label');
      if (SC.empty(label)) {
        label = 'username';
      }

      var fieldPath = 'body.contentView.%@.field'.fmt(label);
      var field = Chililog.configureUserDetailView.getPath(fieldPath);
      if (!SC.none(field)) {
        field.becomeFirstResponder();
      }
    } else {
      // Assume error message string
      SC.AlertPane.error(error);
    }
  }

});

/**********************************************************************************************************************
 * Repositories
 **********************************************************************************************************************/

/**
 * Controls the data when configuring users
 */
Chililog.configureRepositoryInfoListViewController = SC.ArrayController.create({

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
  }
});

/**
 * Controls the data when configuring repositories
 */
Chililog.configureRepositoryInfoDetailViewController = SC.ObjectController.create({

  /**
   * Repository record to display
   * @type Chililog.RepositoryRecord
   */
  content: null,

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
   * Show the user details form
   */
  show: function() {
    Chililog.configureView.setPath('right.nowShowing', 'Chililog.configureRepositoryInfoDetailView');
    var field = Chililog.configureRepositoryInfoDetailView.getPath('body.contentView.name.field');
    // Need to delay setting focus because our scene view takes focus so we have to wait until that finishes first
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
    var record = Chililog.configureRepositoryInfoDetailView.get('content');
    Chililog.statechart.sendEvent('eraseRepositoryInfo', record.get(Chililog.DOCUMENT_ID_RECORD_FIELD_NAME));
  },

  /**
   * Trigger event to discard changes to the user's profile
   */
  discardChanges: function() {
    Chililog.statechart.sendEvent('discardChanges');
  },

  /**
   * Show success message when profile successfully saved
   */
  showSaveSuccess: function() {
    var view = Chililog.configureRepositoryInfoDetailView.getPath('body.contentView.buttons.successMessage');
    var field = Chililog.configureRepositoryInfoDetailView.getPath('body.contentView.name.field');

    if (!SC.none(view)) {
      // Have to invokeLater because of webkit
      // http://groups.google.com/group/sproutcore/browse_thread/thread/482740f497d80462/cba903f9cc6aadf8?lnk=gst&q=animate#cba903f9cc6aadf8
      view.adjust("opacity", 1);
      this.invokeLater(function() {
        view.animate("opacity", 0, { duration: 4, timing:'ease-in' });
      }, 10);
    }

    field.becomeFirstResponder();
  },

  /**
   * Show error message when error happened why trying to save profile
   * @param {SC.Error} error
   */
  showSaveError: function(error) {
    if (SC.instanceOf(error, SC.Error)) {
      // Error
      var message = error.get('message');
      SC.AlertPane.error({ message: message });

      var label = error.get('label');
      if (SC.empty(label)) {
        label = 'name';
      }

      var fieldPath = 'body.contentView.%@.field'.fmt(label);
      var field = Chililog.configureRepositoryInfoDetailView.getPath(fieldPath);
      if (!SC.none(field)) {
        field.becomeFirstResponder();
      }
    } else {
      // Assume error message string
      SC.AlertPane.error(error);
    }
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

