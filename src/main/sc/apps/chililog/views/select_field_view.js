// ==========================================================================
// Project:   Chililog.CheckBoxView
// Copyright: ©2011 My Company, Inc.
// ==========================================================================

/** @class

  Fixes the standard SC.SelectFieldView view not redrawing dropdown list after switching contentView

 @extends SC.ImageView
 */
Chililog.SelectFieldView = SC.SelectFieldView.extend({

  /**
   * When visible in window, flag cpDidChange to make sure that we render the drop down list
   */
  _isVisibleInWindowObserver: function() {
    if (this.get('isVisibleInWindow')) {
      this.set('cpDidChange', YES);
    }
  }.observes('isVisibleInWindow')

});
