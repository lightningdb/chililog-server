// ==========================================================================
// Project:   Chililog - mainPage
// Copyright: ©2011 My Company, Inc.
// ==========================================================================

/**
 * My profile view
 */

Chililog.configureView = SC.View.design({
  layout: { top: 10, left: 10, bottom: 10, right: 10 },
  childViews: 'title body'.w(),

  title: SC.LabelView.design({
    layout: { top: 0, left: 0, width: 200, height: 30 },
    tagName: 'h1',
    controlSize: SC.HUGE_CONTROL_SIZE,
    value: '_configureView.Title',
    localize: YES
  }),

  body: SC.SplitView.design({
    layout: { top: 35, left: 0, right: 0, bottom: 0 },
    classNames: ['edit-box'],
    defaultThickness: 0.2,

    topLeftView: SC.ScrollView.design({
      layout: { top: 0, bottom: 0, left: 0, right: 0 },
      hasHorizontalScroller: NO,

      contentView: SC.ListView.design({
        layout: { top: 0, bottom: 0, left: 0, right: 0 },
        rowHeight: 24,
        contentValueKey: 'treeItemLabel',
        contentBinding: 'Chililog.configureViewTreeController.arrangedObjects',
        selectionBinding: 'Chililog.configureViewTreeController.selection'
      })
    }),

    bottomRightView: SC.View.design({
    })
  })



  
});
