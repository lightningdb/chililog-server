// ==========================================================================
// Project:   Chililog.CheckBoxView
// Copyright: ©2011 My Company, Inc.
// ==========================================================================

/** @class

  Checkbox view with handling for spacebar to toggle selection as well as focus ring.

 @extends SC.ImageView
 */
Chililog.CheckboxView = SC.CheckboxView.extend(
/** @scope Chililog.CheckboxView.prototype */ {

  /**
   * Get focus when mouse clicked so that the focus ring is displayed
   * @param evt
   */
  mouseDown: function(evt) {
    this.becomeFirstResponder();
    return sc_super();
  },

  /**
   * If space bar, simulate mouse down
   * @param evt
   */
  keyDown: function(evt) {
    if (evt.which === 32 || evt.keyCode === 32) {
      this.mouseDown(evt);
    } else {
      sc_super();
    }
  },

  /**
   * If space bar, simulate mouse up
   * @param evt
   */
  keyUp: function(evt) {
    if (evt.which === 32 || evt.keyCode === 32) {
      this.mouseUp(evt);
    }
  }
});
