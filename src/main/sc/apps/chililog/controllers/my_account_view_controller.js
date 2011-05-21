// ==========================================================================
// Project:   Chililog.mainPaneController
// Copyright: ©2011 My Company, Inc.
// ==========================================================================

sc_require('controllers/view_controller_mixin');

/**
 * Controller for the my account page. Handles menu selection events.
 *
 * @extends SC.Object
 */
Chililog.myAccountViewController = SC.ObjectController.create(Chililog.ViewControllerMixin,
/** @scope Chililog.myAccountViewController.prototype */ {

  /**
   * Menu item selected
   */
  onSelect: function() {
    var selectionSet = Chililog.myAccountView.getPath('left.contentView.selection');
    if (SC.none(selectionSet) || selectionSet.get('length') === 0) {
      return null;
    }
    var selection = selectionSet.get('firstObject');
    var id = selection['id'];
    if (id === 'MyProfile') {
      Chililog.statechart.sendEvent('editMyProfile');
    } else if (id === 'ChangePassword') {
      Chililog.statechart.sendEvent('changeMyPassword');
    }

    return;
  },

  /**
   * Make sure the selected menu item is My Profile
   */
  selectMyProfileMenuItem: function() {
    var menuItems = Chililog.myAccountView.getPath('left.contentView');
    menuItems.select(0);
  },

  /**
   * Make sure the selected menu item is Change Password
   */
  selectChangePasswordMenuItem: function() {
    var menuItems = Chililog.myAccountView.getPath('left.contentView');
    menuItems.select(1);
  }

});

