// ==========================================================================
// Project:   Chililog
// Copyright: ©2011 My Company, Inc.
// ==========================================================================

/**
 * Modal Pane and shows progress to the user
 */
Chililog.progressPane = SC.PanelPane.create({
  layout: { width: 300, height: 200, centerX: 0, centerY: 0 },
  contentView: SC.View.extend({
    layout: {top: 10, left: 10, bottom: 10, right: 10},
    childViews: 'message image'.w(),

    message: SC.LabelView.design({
      layout: { top: 60, left: 35, width: 200, height: 30 },
      tagName: 'h1',
      controlSize: SC.REGULAR_CONTROL_SIZE,
      valueBinding: 'Chililog.progressPane.message'
    }),

    image: Chililog.ImageView.design({
      layout: { top: 80, left: 30, width: 220, height: 19 },
      value: sc_static('images/working_bar'),
      useImageQueue: NO
    })
  }),

  /**
   * Minimum amount of time to show the form before we hide it.
   * This stops "flashing" in the event of a very fast network connection
   */
  minDisplayMilliseconds: 750,

  /**
   * Message to show to the user
   * @type String
   */
  message: 'Waiting on server',

  /**
   * Timestamp in milliseconds of when show() was called
   * @type Date
   */
  shownAt: null,

  /**
   * Show this pane
   */
  show: function(message) {
    this.set('shownAt', new Date().getTime());
    this.set('message', message);
    this.append();
  },

  /**
   * Hide this pane taking into account the minimum number of milliseconds this panel should be displayed
   */
  hideWithoutFlashing: function() {
    var now = new Date().getTime();
    var shownAt = this.get('shownAt');
    var diff = now - shownAt;
    var min = this.get('minDisplayMilliseconds');

    if (diff < min) {
      this.invokeLater(function() {
        Chililog.progressPane.remove();
      }, min - diff);
    } else {
      this.remove();
    }
  },

  /**
   * Hide this pane
   */
  hide: function() {
    this.remove();
  }
});
