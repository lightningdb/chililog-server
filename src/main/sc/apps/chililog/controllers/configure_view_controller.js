// ==========================================================================
// Project:   Chililog.mainPaneController
// Copyright: ©2011 My Company, Inc.
// ==========================================================================

/**
 * Controls the data when configuring users
 */
Chililog.configureUserViewController = SC.ObjectController.create({

  /**
   * User record to display
   * @type Chililog.UserRecord
   */
  content: null,

  /**
   * Show the user details form
   */
  show: function() {
    Chililog.configureView.setPath('body.bottomRightView.contentView', Chililog.configureUserView);
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
   * Trigger event to discard changes to the user's profile
   */
  discardChanges: function() {
    Chililog.statechart.sendEvent('discardChanges');
  },

  /**
   * Show success message when profile successfully saved
   */
  showSaveSuccess: function() {
    var view = Chililog.configureUserView.getPath('body.successMessage');
    var field = Chililog.configureUserView.getPath('body.username.field');

    if (!SC.none(view)) {
      // Have to invokeLater because of webkit
      // http://groups.google.com/group/sproutcore/browse_thread/thread/482740f497d80462/cba903f9cc6aadf8?lnk=gst&q=animate#cba903f9cc6aadf8
      view.adjust("opacity", 1);
      this.invokeLater(function() {
        view.animate("opacity", 0, { duration: 2, timing:'ease-in' });
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

      var fieldPath = 'body.%@.field'.fmt(label);
      var field = Chililog.configureUserView.getPath(fieldPath);
      if (!SC.none(field)) {
        field.becomeFirstResponder();
      }
    } else {
      // Assume error message string
      SC.AlertPane.error(error);
    }
  }  
  
});

/**
 * Controls the data when configuring repositories
 */
Chililog.configureRepositoryInfoViewController = SC.ObjectController.create({

  /**
   * Repository record to display
   * @type Chililog.RepositoryRecord
   */
  content: null,

  /**
   * Show the repository details form
   */
  show: function() {
    Chililog.configureView.setPath('body.bottomRightView.contentView', Chililog.configureRepositoryInfoView);
  }
});

/**
 * Controller that for the mainPane. Mainly handles menu selection option and login/logout
 *
 * @extends SC.Object
 */
Chililog.configureTreeViewController = SC.TreeController.create({

  /**
   * Poplulate our tree
   */
  populate: function() {
    var repositories = SC.Object.create({
      treeItemIsExpanded: YES,
      treeItemLabel: 'Repositories',
      treeItemIcon: sc_static('images/repositories.png'),
      treeItemChildren: function() {
        var repoInfoQuery = SC.Query.local(Chililog.RepositoryInfoRecord, { orderBy: 'name' });
        var repoInfo = Chililog.store.find(repoInfoQuery);
        return repoInfo;
      }.property()
    });

    var users = SC.Object.create({
      treeItemIsExpanded: YES,
      treeItemLabel: 'Users',
      treeItemIcon: sc_static('images/users.png'),
      treeItemChildren: function() {
        var userQuery = SC.Query.local(Chililog.UserRecord, { orderBy: 'username' });
        var users = Chililog.store.find(userQuery);
        return users;
      }.property()
    });

    var rootNode = SC.Object.create({
      treeItemIsExpanded: YES,
      treeItemLabel: 'Chililog',
      treeItemIcon: null,
      treeItemChildren: [repositories, users]
    });
    this.set('content', rootNode);
  },

  /**
   * Returns the selected item
   */
  selectedItem: function() {
    var selectionSet = this.get('selection');
    if (SC.none(selectionSet) || selectionSet.get('length') === 0) {
      return null;
    }
    var selection = selectionSet.get('firstObject');
    return selection;
  }.property('selection').cacheable(),

  /**
   * When selection changes, then we send an event
   */
  selectionDidChange: function() {
    var selectedItem = this.get('selectedItem');
    if (SC.none(selectedItem)) {
      return;
    }

    if (SC.instanceOf(selectedItem, Chililog.UserRecord)) {
      Chililog.statechart.sendEvent('editUser', selectedItem.get(Chililog.DOCUMENT_ID_RECORD_FIELD_NAME));
    } else if (SC.instanceOf(selectedItem, Chililog.RepositoryInfoRecord)) {
      Chililog.statechart.sendEvent('editRepositoryInfo', selectedItem.get(Chililog.DOCUMENT_ID_RECORD_FIELD_NAME));
    } 
  }.observes('selectedItem')

});

