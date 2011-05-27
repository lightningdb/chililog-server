// ==========================================================================
// Project:   Chililog
// Copyright: ©2011 My Company, Inc.
// ==========================================================================

Chililog.SearchListView = SC.LabelView.design({
  layout: { top: 0, left: 0, bottom: 0, right: 0 },
  childViews: 'title toggleSearchModeButton basicSearch advancedSearch table footer noRowsFoundMessage'.w(),

  title: SC.LabelView.design({
    layout: { top: 5, left: 10, width: 200, height: 30 },
    tagName: 'h1',
    controlSize: SC.HUGE_CONTROL_SIZE,
    value: '_searchListView.Title',
    localize: YES
  }),

  toggleSearchModeButton: SC.ButtonView.design({
    layout: { top: 10, right: 10, height: 30, width: 175 },
    align: SC.ALIGN_RIGHT,
    titleBinding: SC.Binding.from('Chililog.searchListViewController.toggleSearchModeButtonTitle').oneWay(),
    localize: YES,
    target: Chililog.searchListViewController,
    action: 'toggleSearchMode'
  }),

  basicSearch: SC.View.design({
    layout: { top: 40, left: 10, right: 10, height: 75 },
    classNames: ['box'],
    childViews: 'repositories timespan keywords searchButton searchingImage'.w(),
    isVisibleBinding: SC.Binding.from('Chililog.searchListViewController.isBasicSearchMode').oneWay().bool(),

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
      isEnabledBinding: SC.Binding.from('Chililog.searchListViewController.isSearching').oneWay().not(),
      isDefault: YES,
      target: Chililog.searchListViewController,
      action: 'basicSearch'
    }),

    searchingImage: Chililog.ImageView.design({
      layout: { top: 35, left: 935, width: 16, height: 16 },
      value: sc_static('images/working'),
      isVisibleBinding: SC.Binding.from('Chililog.searchListViewController.isSearching').oneWay().bool(),
      useImageQueue: NO
    })
  }),

  advancedSearch: SC.View.design({
    layout: { top: 40, left: 10, right: 10, height: 150 },
    classNames: ['box'],
    isVisibleBinding: SC.Binding.from('Chililog.searchListViewController.isBasicSearchMode').oneWay().not()
  }),
  
  table: SC.TableView.design({
    layoutBinding: SC.Binding.from('Chililog.searchListViewController.tableLayout').oneWay(),
    classNames: ['table'],
    contentBinding: 'Chililog.searchListViewController.arrangedObjects',
    selectionBinding: 'Chililog.searchListViewController.selection',
    useHeaders: YES,
    isEditable: NO,
    columns:[
      SC.TableColumn.create({
        key:   'xxx',
        title: '_searchListView.Row'.loc(),
        width: 50,
        isReorderable: NO,
        isSortable: NO,
        formatter: function(v, target) {
          return target.get('contentIndex') + 1;
        },
        exampleView: SC.TableCellContentView.extend({
          textAlign: SC.ALIGN_RIGHT,
          fontWeight: SC.BOLD_WEIGHT
        })
      }),
      SC.TableColumn.create({
        key:   'timestamp',
        title: '_searchListView.Timestamp'.loc(),
        width: 180,
        isReorderable: NO,   //Bug with reorder when switching with other configure options
        formatter: function(v) {
          return SC.none(v) ? '' : v.toFormattedString('%Y-%m-%d %H:%M:%S.%s');
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
        isReorderable: NO,
        exampleView: SC.TableCellContentView.extend({
          render: function(context, firstTime) {
            if (!firstTime) {
              context.removeClass('severity-error');
              context.removeClass('severity-warning');
            }
            var severity = this.getPath('content.severity');
            if (severity <= 3) {
              context.addClass('severity-error');
            } else if (severity === 4 || severity === 5) {
              context.addClass('severity-warning');
            }
            sc_super();
          }
        })
      }),
      SC.TableColumn.create({
        key:   'messageWithKeywordsHilighted',
        title: '_searchListView.Message'.loc(),
        width: 600,
        maxWidth: 1024,
        isReorderable: NO,
        escapeHTML: NO
      })
    ],
    target: Chililog.searchListViewController,
    action: 'view'
  }),

  footer: SC.View.design({
    layout: { left: 10, right: 10, bottom: 10, height: 33 },
    classNames: ['box'],
    isVisibleBinding: SC.Binding.from('Chililog.searchListViewController.canShowMore').oneWay().bool(),
    childViews: 'moreButton'.w(),

    moreButton: SC.ButtonView.design({
      layout: { right: 5, top: 5, width: 100 },
      title: '_showMore',
      localize: YES,
      target: Chililog.searchListViewController,
      action: 'showMore'
    })
  }),

  /**
   * This is overlay on top of the table so it looks like it is part of the table
   */
  noRowsFoundMessage: SC.LabelView.design({
    layoutBinding: SC.Binding.from('Chililog.searchListViewController.noRowsFoundMessageLayout').oneWay(),
    isVisibleBinding: SC.Binding.from('Chililog.searchListViewController.rowsFoundAfterSearch').oneWay().not(),
    value: '_searchListView.NoRowsFound'.loc()
  })

});

// Because of bug with table when swappping in/out of content view, we have to create it manually everytime
Chililog.searchListView = null;

