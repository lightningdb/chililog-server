// ==========================================================================
// Project:   Chililog.CheckBoxView
// Copyright: ©2011 My Company, Inc.
// ==========================================================================

/** @class

  RadioView view with handling focus and up/down arrow keys as well as focus ring

 @extends SC.ImageView
 */
Chililog.RadioView = SC.RadioView.extend(
/** @scope Chililog.RadioView.prototype */ {

  /**
   * Get focus when mouse clicked so that the focus ring is displayed
   * @param evt
   */
  mouseDown: function(evt) {
    this.becomeFirstResponder();
    return sc_super();
  },

  /**
   * Allow up/down arrow to change selection
   * @param evt
   */
  keyUp: function(evt) {
    if (evt.which === SC.Event.KEY_UP || evt.keyCode === SC.Event.KEY_UP ||
      evt.which === SC.Event.KEY_DOWN || evt.keyCode === SC.Event.KEY_DOWN) {
      if (!this.get('isEnabled')) {
        return YES;
      }

      var items = this.get('items');
      var valueKey = this.get('itemValueKey');
      var currentValue = this.get('value');
      for (var i = 0; i < items.length; i++) {
        if (items[i][valueKey] === currentValue) {
          var newIndex = 0;
          if (evt.which === SC.Event.KEY_UP || evt.keyCode === SC.Event.KEY_UP) {
            newIndex = i - 1;
            if (newIndex < 0) {
              newIndex = items.length - 1;
            }
          } else {
            newIndex = i + 1;
            if (newIndex >= items.length) {
              newIndex = 0;
            }
          }
          this.set('value', items[newIndex][valueKey]);
          break;
        }
      }

      // even if radiobuttons are not set to get firstResponder, allow default
      // action, that way textfields loose focus as expected.
      evt.allowDefault();
      return YES;
    }
    return NO;
  }
});
