// ==========================================================================
// Project:   Chililog.mainPaneController
// Copyright: ©2011 My Company, Inc.
// ==========================================================================

sc_require('controllers/view_controller_mixin');

/**
 * Controller for the base configuration view in which listing and detail sub-view will sit
 */
Chililog.configureViewController = SC.Object.create({

  /**
   * Menu items to display on the left hand side list view
   */
  menuItems: [],

  /**
   * Update menu items when the logged in user changes
   */
  updateMenuItems: function() {
    var isSystemAdministrator = Chililog.sessionDataController.get('isSystemAdministrator');
    var isRepositoryAdministrator = Chililog.sessionDataController.get('isRepositoryAdministrator');
    var menuItems = [];

    if (isRepositoryAdministrator || isSystemAdministrator) {
      menuItems.push({
        id: 'Repositories',
        label: '_configureView.Repositories'.loc(),
        icon: sc_static('images/repositories.png')
      });
    }

    if (isSystemAdministrator) {
      menuItems.push({
        id: 'Users',
        label: '_configureView.Users'.loc(),
        icon: sc_static('images/users.png')
      });
    }

    this.set('menuItems', menuItems);
  }.observes('Chililog.sessionDataController.loggedInUser'),

  /**
   * Menu item selected
   */
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
  },

  /**
   * Make sure the selected menu item is the repository
   */
  selectRepositoriesMenuItem: function() {
    var menuItems = Chililog.configureView.getPath('left.contentView');
    menuItems.select(0);
  },

  /**
   * Make sure the selected menu item is the user
   */
  selectUsersMenuItem: function() {
    var menuItems = Chililog.configureView.getPath('left.contentView');
    menuItems.select(1);
  }

});

