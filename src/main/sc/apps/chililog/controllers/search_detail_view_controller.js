// ==========================================================================
// Project:   Chililog.mainPaneController
// Copyright: ©2011 My Company, Inc.
// ==========================================================================

sc_require('controllers/view_controller_mixin');

/**
 * Controller for searching for repository entries
 */
Chililog.searchDetailViewController = SC.ObjectController.create({

  contentBinding: 'Chililog.searchListViewController.selection',

  /**
   * Format timestamp for display
   */
  timestampText: function() {
    var ts = this.get('timestamp');
    return SC.none(ts) ? '' : ts.toFormattedString('%Y-%m-%d %H:%M:%S.%s');
  }.property('timestamp').cacheable(),

  /**
   * Convert keyword array to CSV for display
   */
  keywordsText: function() {
    var keywords = this.get('keywords');
    var text = '';
    if (!SC.empty(keywords)) {
      for (var i=0; i< keywords.length; i++) {
        text = text + keywords[i] + ', ';
      }
    }
    if (!SC.empty(text)) {
      text = text.substr(0, text.length - 3);
    }
    return text;
  }.property('keywords').cacheable(),

  /**
   * Converts fields nave-value array into text for display
   */
  fieldsText: function() {
    var fields = this.get('fields');
    var text = '';
    if (!SC.empty(fields)) {
      for (var i=0; i< fields.length; i++) {
        var fld = fields[i];
        text = text + fld['name'] + '=' + fld['value'] + '\n';
      }
    }
    return text;
  }.property('fields').cacheable(),

  /**
   * Show this modal form
   */
  show: function() {
    Chililog.searchDetailView.append();
  },

  /**
   * Hide this modal form
   */
  hide: function() {
    Chililog.searchDetailView.remove();
  },

  /**
   * Event handler to trigger hiding this form
   */
  done: function() {
    Chililog.statechart.sendEvent('done');
  },

  /**
   * Event handler to select previous item in the list
   */
  previous: function() {
    var tableDataView = Chililog.searchListView.getPath('table._dataView.contentView');
    tableDataView.selectPreviousItem();
  },

  /**
   * Event handler to select next item in the list
   */
  next: function() {
    var tableDataView = Chililog.searchListView.getPath('table._dataView.contentView');
    tableDataView.selectNextItem();
  }

});
