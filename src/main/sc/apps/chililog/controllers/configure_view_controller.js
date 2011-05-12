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

    Chililog.configureView.setPath('right.contentView', Chililog.configureRepositoryInfoSceneView);
  },

  /**
   * Make sure the selected menu item is the user
   */
  selectUsersMenuItem: function() {
    var menuItems = Chililog.configureView.getPath('left.contentView');
    menuItems.select(1);

    Chililog.configureView.setPath('right.contentView', Chililog.configureUserSceneView);
  }

});

