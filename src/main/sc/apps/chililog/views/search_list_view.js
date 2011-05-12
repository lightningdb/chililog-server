// ==========================================================================
// Project:   Chililog
// Copyright: ©2011 My Company, Inc.
// ==========================================================================

Chililog.SearchListView = SC.LabelView.design({
  layout: { top: 0, left: 0, bottom: 0, right: 0 },
  childViews: 'title keywords searchButton table'.w(),

  title: SC.LabelView.design({
    layout: { top: 5, left: 10, width: 200, height: 30 },
    tagName: 'h1',
    controlSize: SC.HUGE_CONTROL_SIZE,
    value: '_searchListView.Title',
    localize: YES
  }),

  keywords: SC.TextFieldView.design({
    layout: { top: 40, left: 10, width: 500, height: 30 },
    valueBinding: 'Chililog.searchViewController.keywords',
    maxLength: 300
  }),

  searchButton: SC.ButtonView.design({
    layout: { top: 40, left: 520, width: 80 },
    title: '_searchListView.Search',
    localize: YES,
    controlSize: SC.HUGE_CONTROL_SIZE,
    target: Chililog.searchViewController,
    action: 'search'
  }),

  table: SC.TableView.design({
    layout: { top: 80, left: 10, right: 10, bottom: 10 },
    classNames: ['table'],
    contentBinding: 'Chililog.searchViewController.arrangedObjects',
    selectionBinding: 'Chililog.searchViewController.selection',
    useHeaders: YES,
    isEditable: NO,
    columns:[
      SC.TableColumn.create({
        key:   'timestamp',
        title: '_searchListView.Timestamp'.loc(),
        width: 150,
        isReorderable: NO   //Bug with reorder when switching with other configure options
      }),
      SC.TableColumn.create({
        key:   'source',
        title: '_searchListView.Source'.loc(),
        width: 250,
        isReorderable: NO
      }),
      SC.TableColumn.create({
        key:   'host',
        title: '_searchListView.Host'.loc(),
        width: 250,
        isReorderable: NO
      }),
      SC.TableColumn.create({
        key:   'severity',
        title: '_searchListView.Severity'.loc(),
        width: 100,
        isReorderable: NO
      }),
      SC.TableColumn.create({
        key:   'message',
        title: '_searchListView.Message'.loc(),
        width: 100,
        isReorderable: NO
      })
    ],
    target: Chililog.searchViewController,
    action: 'view'
  })

});

Chililog.searchListView = Chililog.SearchListView.create();

