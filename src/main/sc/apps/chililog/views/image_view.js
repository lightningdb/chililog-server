// ==========================================================================
// Project:   Chililog.ImageView
// Copyright: ©2011 My Company, Inc.
// ==========================================================================

/** @class

  Image view without HTML canvas so that animated GIFs will work.

  @extends SC.ImageView
*/
Chililog.ImageView = SC.ImageView.extend(
/** @scope Chililog.ImageView.prototype */ {

  /**
   * Override the default so that we don't support canvas and force IMG tag to be used
   */
  useCanvas: function() {
    return false;
  }.property()

});
