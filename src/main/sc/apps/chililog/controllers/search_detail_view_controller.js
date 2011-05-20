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

  timestampText: function() {
    var v = this.get('timestamp');
    return SC.none(v) ? '' : v.toFormattedString('%Y-%m-%d %H:%M:%S.%s');
  }.property('timestamp').cacheable(),

  show: function() {
    Chililog.searchDetailView.append();
  },

  hide: function() {
    Chililog.searchDetailView.remove();
  },

  done: function() {
    Chililog.statechart.sendEvent('done');
  },

  previous: function() {
    var tableDataView = Chililog.searchListView.getPath('table._dataView.contentView');
    tableDataView.selectPreviousItem();
  }, 

  next: function() {
    var tableDataView = Chililog.searchListView.getPath('table._dataView.contentView');
    tableDataView.selectNextItem();
  }

});
