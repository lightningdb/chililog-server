// ==========================================================================
// Project:   Chililog.CheckBoxView
// Copyright: ©2011 My Company, Inc.
// ==========================================================================

/** @class

  Checkbox view with handling for spacebar to toggle selection.

 @extends SC.ImageView
 */
Chililog.CheckboxView = SC.CheckboxView.extend(
/** @scope Chililog.CheckboxView.prototype */ {

  keyDown: function(evt) {
    if (evt.which === 32 || evt.keyCode === 32) {
      this.mouseDown(evt);
    } else {
      sc_super();
    }
  },

  keyUp: function(evt) {
    if (evt.which === 32 || evt.keyCode === 32) {
      this.mouseUp(evt);
    }
  }
});
