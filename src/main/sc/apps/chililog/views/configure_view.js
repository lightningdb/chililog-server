// ==========================================================================
// Project:   Chililog - mainPage
// Copyright: ©2011 My Company, Inc.
// ==========================================================================

/**
 * Configure view
 */
Chililog.ConfigureView = SC.View.design({
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
        hasContentIcon: YES,
        contentValueKey: 'treeItemLabel',
        contentIconKey: 'treeItemIcon',
        contentBinding: 'Chililog.configureTreeViewController.arrangedObjects',
        selectionBinding: 'Chililog.configureTreeViewController.selection'
      })
    }),

    bottomRightView: SC.ContainerView.design({
    })
  })
});

/**
 * Instance configure view
 */
Chililog.configureView = Chililog.ConfigureView.create();


/**
 * User details
 */
Chililog.ConfigureUserView = SC.View.design({
  layout: { top: 10, left: 10, bottom: 10, right: 10 },
  childViews: 'title body'.w(),

  title: SC.LabelView.design({
    layout: { top: 0, left: 0, width: 200, height: 30 },
    tagName: 'h1',
    controlSize: SC.HUGE_CONTROL_SIZE,
    value: '_configureUserView.Title',
    localize: YES
  }),

  body: SC.View.design({
    layout: { top: 35, left: 0, width: 400, height: 300 },
    classNames: ['edit-box'],
    childViews: 'username'.w(),

    username: SC.View.design({
      layout: {top: 50, left: 20, right: 20, height: 50 },
      childViews: 'label field'.w(),

      label: SC.LabelView.design({
        layout: { top: 0, left: 0, right: 0, height: 19 },
        value: '_myAccountView.Username',
        localize: YES
      }),

      field: SC.TextFieldView.design({
        layout: { top: 20, left: 0, height: 25 },
        valueBinding: 'Chililog.configureUserViewController.username'
      })
    })
  })
});

/**
 * Instance configure user view
 */
Chililog.configureUserView = Chililog.ConfigureUserView.create();

/**
 * Repository details
 */
Chililog.ConfigureRepositoryView = SC.View.design({
  layout: { top: 10, left: 10, bottom: 10, right: 10 },
  childViews: 'title'.w(),

  title: SC.LabelView.design({
    layout: { top: 0, left: 0, width: 200, height: 30 },
    tagName: 'h1',
    controlSize: SC.HUGE_CONTROL_SIZE,
    value: '_configureRepositoryView.Title',
    localize: YES
  })
});

/**
 * Instance configure repository view
 */
Chililog.configureRepositoryView = Chililog.ConfigureRepositoryView.create();
