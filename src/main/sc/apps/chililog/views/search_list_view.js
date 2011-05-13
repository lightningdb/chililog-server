// ==========================================================================
// Project:   Chililog
// Copyright: ©2011 My Company, Inc.
// ==========================================================================

Chililog.SearchListView = SC.LabelView.design({
  layout: { top: 0, left: 0, bottom: 0, right: 0 },
  childViews: 'title basicAdvancedOptions basicSearch table'.w(),

  title: SC.LabelView.design({
    layout: { top: 5, left: 10, width: 200, height: 30 },
    tagName: 'h1',
    controlSize: SC.HUGE_CONTROL_SIZE,
    value: '_searchListView.Title',
    localize: YES
  }),

  basicAdvancedOptions: SC.SegmentedView.design({
    layout: { top: 10, left: 300, right: 10, height: 30 },
    align: SC.ALIGN_RIGHT,
    items: [
      { value: 'basic', title: 'Basic', toolTip: 'Most commonly used search parameters', width: '70' },
      { value: 'advanced', title: 'Advanced', toolTip: 'Shows all search parameters', width: '70' }
    ],
    itemValueKey: 'value',
    itemTitleKey: 'title',
    itemToolTipKey: 'toolTip',
    itemWidthKey: 'width',
    itemTargetKey: 'target',
    itemActionKey: 'action'
  }),

  basicSearch: SC.View.design({
    layout: { top: 40, left: 10, right: 10, height: 75 },
    classNames: ['box'],
    childViews: 'repositories timespan keywords searchButton'.w(),

    repositories: SC.View.design({
      layout: { top: 10, left: 15, bottom: 10, width: 150 },
      childViews: 'label field'.w(),

      label: SC.LabelView.design({
        layout: { top: 0, left: 0, right: 0, height: 20 },
        fontWeight: SC.BOLD_WEIGHT, 
        value: '_searchListView.Repository'.loc()
      }),

      field: SC.SelectFieldView.design({
        layout: { top: 20, left: 0, width: 150, height: 30 },
        objectsBinding: 'Chililog.searchListViewController.repositories',
        nameKey: 'displayNameOrName',
        valueKey: 'documentID',
        valueBinding: 'Chililog.searchListViewController.basicRepository'
      })
    }),

    timespan: SC.View.design({
      layout: { top: 10, left: 175, bottom: 10, width: 150 },
      childViews: 'label field'.w(),

      label: SC.LabelView.design({
        layout: { top: 0, left: 0, right: 0, height: 20 },
        fontWeight: SC.BOLD_WEIGHT,
        value: '_searchListView.TimeSpan'.loc()
      }),

      field: SC.SelectFieldView.design({
        layout: { top: 20, left: 0, width: 150, height: 30 },
        objects: [
          { name:'_searchListView.TimeSpan.5'.loc(), value:'5' },
          { name:'_searchListView.TimeSpan.15'.loc(), value:'15' },
          { name:'_searchListView.TimeSpan.30'.loc(), value:'30' },
          { name:'_searchListView.TimeSpan.60'.loc(), value:'60' },
          { name:'_searchListView.TimeSpan.1440'.loc(), value:'1440' },
          { name:'_searchListView.TimeSpan.10080'.loc(), value:'10080' },
          { name:'_searchListView.TimeSpan.20160'.loc(), value:'20160' },
          { name:'_searchListView.TimeSpan.43200'.loc(), value:'43200' }
        ],
        nameKey: 'name',
        valueKey: 'value',
        disableSort: YES,
        valueBinding: 'Chililog.searchListViewController.basicTimeSpan'
      })
    }),

    keywords: SC.View.design({
      layout: { top: 10, left: 335, bottom: 10, width: 500 },
      childViews: 'label field'.w(),

      label: SC.LabelView.design({
        layout: { top: 0, left: 0, right: 0, height: 20 },
        fontWeight: SC.BOLD_WEIGHT,
        value: '_searchListView.Keywords'.loc()
      }),

      field: SC.TextFieldView.design({
        layout: { top: 20, left: 0, right: 0, height: 30 },
        valueBinding: 'Chililog.searchListViewController.basicKeywords',
        maxLength: 300
      })
    }),

    searchButton: SC.ButtonView.design({
      layout: { top: 30, left: 845, width: 80 },
      title: '_searchListView.Search',
      localize: YES,
      controlSize: SC.HUGE_CONTROL_SIZE,
      target: Chililog.searchListViewController,
      action: 'basicSearch'
    })
  }),


  table: SC.TableView.design({
    layout: { top: 125, left: 10, right: 10, bottom: 10 },
    classNames: ['table'],
    contentBinding: 'Chililog.searchListViewController.arrangedObjects',
    selectionBinding: 'Chililog.searchListViewController.selection',
    useHeaders: YES,
    isEditable: NO,
    columns:[
      SC.TableColumn.create({
        key:   'timestamp',
        title: '_searchListView.Timestamp'.loc(),
        width: 180,
        isReorderable: NO,   //Bug with reorder when switching with other configure options
        formatter: function(v) {
          return v.toFormattedString('%Y-%m-%d %H:%M:%S.%s');
        }
      }),
      SC.TableColumn.create({
        key:   'source',
        title: '_searchListView.Source'.loc(),
        width: 150,
        isReorderable: NO
      }),
      SC.TableColumn.create({
        key:   'host',
        title: '_searchListView.Host'.loc(),
        width: 150,
        isReorderable: NO
      }),
      SC.TableColumn.create({
        key:   'severityText',
        title: '_searchListView.Severity'.loc(),
        width: 100,
        isReorderable: NO
      }),
      SC.TableColumn.create({
        key:   'message',
        title: '_searchListView.Message'.loc(),
        width: 600,
        isReorderable: NO
      })
    ],
    target: Chililog.searchViewController,
    action: 'view'
  })

});

Chililog.searchListView = Chililog.SearchListView.create();

