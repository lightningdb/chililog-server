// ==========================================================================
// Project:   Chililog - mainPage
// Copyright: ©2011 My Company, Inc.
// ==========================================================================

/**
 * My profile view
 */

Chililog.configureView = SC.View.design({
  layout: { top: 10, left: 10, bottom: 10, right: 10 },
  childViews: 'title tree'.w(),

  title: SC.LabelView.design({
    layout: { top: 0, left: 0, width: 200, height: 30 },
    tagName: 'h1',
    controlSize: SC.HUGE_CONTROL_SIZE,
    value: '_configureView.Title',
    localize: YES
  }),

  tree: SC.ListView.design({
    layout: { top: 35, left: 0, bottom: 10, width: 400 },
    contentValueKey: 'treeItemName',
    contentBinding: 'Chililog.configureViewTreeController.arrangedObjects',
    selectionBinding: 'Chililog.configureViewTreeController.selection'
  })  //tree


});
