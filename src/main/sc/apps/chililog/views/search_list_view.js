// ==========================================================================
// Project:   Chililog
// Copyright: ©2011 My Company, Inc.
// ==========================================================================

Chililog.SearchListView = SC.LabelView.design({
  layout: { top: 0, left: 0, bottom: 0, right: 0 },
  childViews: 'basicSearch advancedSearch table footer noRowsFoundMessage'.w(),

  basicSearch: SC.View.design({
    layout: { top: 10, left: 10, right: 10, height: 75 },
    classNames: ['box'],
    childViews: 'repositories timespan keywords searchButton searchingImage toggleSearchModeButton'.w(),
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
    }),

    toggleSearchModeButton: SC.ButtonView.design({
      layout: { top: 30, left: 935, width: 175 },
      align: SC.ALIGN_RIGHT,
      titleBinding: SC.Binding.from('Chililog.searchListViewController.toggleSearchModeButtonTitle').oneWay(),
      localize: YES,
      controlSize: SC.HUGE_CONTROL_SIZE,
      isVisibleBinding: SC.Binding.from('Chililog.searchListViewController.isSearching').oneWay().not(),
      target: Chililog.searchListViewController,
      action: 'toggleSearchMode'
    })
  }),

  advancedSearch: SC.View.design({
    layout: { top: 10, left: 10, right: 10, height: 225 },
    classNames: ['box'],
    childViews: 'repositories timeType timespan timeFrom timeTo source host severity keywords conditions searchButton searchingImage toggleSearchModeButton'.w(),
    isVisibleBinding: SC.Binding.from('Chililog.searchListViewController.isBasicSearchMode').oneWay().not(),

    repositories: SC.View.design({
      layout: { top: 10, left: 15, width: 150, height: 50 },
      childViews: 'label field'.w(),

      label: SC.LabelView.design({
        layout: { top: 0, left: 0, right: 0, height: 20 },
        fontWeight: SC.BOLD_WEIGHT,
        value: '_searchListView.Repository'.loc()
      }),

      field: SC.SelectFieldView.design({
        layout: { top: 20, left: 0, width: 150, height: 29 },
        objectsBinding: 'Chililog.searchListViewController.repositories',
        nameKey: 'displayNameOrName',
        valueKey: 'documentID',
        valueBinding: 'Chililog.searchListViewController.advancedRepository'
      })
    }),

    timeType: SC.View.design({
      layout: { top: 10, left: 175, bottom: 10, width: 150, height: 50 },
      childViews: 'label field'.w(),

      label: SC.LabelView.design({
        layout: { top: 0, left: 0, right: 0, height: 20 },
        fontWeight: SC.BOLD_WEIGHT,
        value: '_searchListView.TimeType'.loc()
      }),

      field: SC.SelectFieldView.design({
        layout: { top: 20, left: 0, width: 150, height: 29 },
        objects: [
          { name:'_searchListView.TimeType.InThePast'.loc(), value:'InThePast' },
          { name:'_searchListView.TimeType.InBetween'.loc(), value:'InBetween' }
        ],
        nameKey: 'name',
        valueKey: 'value',
        disableSort: YES,
        valueBinding: 'Chililog.searchListViewController.advancedTimeType'
      })
    }),

    timespan: SC.View.design({
      layout: { top: 10, left: 335, width: 150, height: 50 },
      childViews: 'field'.w(),
      isVisibleBinding: SC.Binding.from('Chililog.searchListViewController.isInThePastTimeType').oneWay().bool(),

      field: SC.SelectFieldView.design({
        layout: { top: 20, left: 0, width: 150, height: 29 },
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
        valueBinding: 'Chililog.searchListViewController.advancedTimeSpan'
      })
    }),

    timeFrom: SC.View.design({
      layout: { top: 10, left: 335, width: 160, height: 50 },
      childViews: 'label field'.w(),
      isVisibleBinding: SC.Binding.from('Chililog.searchListViewController.isInThePastTimeType').oneWay().not(),

      label: SC.LabelView.design({
        layout: { top: 0, left: 0, right: 0, height: 20 },
        fontWeight: SC.BOLD_WEIGHT,
        value: '_searchListView.TimeFrom'.loc()
      }),

      field: SC.TextFieldView.design({
        layout: { top: 20, left: 0, right: 0, height: 29 },
        valueBinding: 'Chililog.searchListViewController.advancedTimeFrom',
        hint: 'yyyy-mm-dd hh:mm:ss',
        maxLength: 20,
        validator: Chililog.DateTimeValidator.extend({
          keyDownRegExp: /^[0-9 :\-\0]$/,
          format: '%Y-%m-%d %H:%M:%S',
          invalidFieldErrorMessage: '_searchListView.TimeFrom.Invalid'
        })
      })
    }),

    timeTo: SC.View.design({
      layout: { top: 10, left: 505, width: 160, height: 50 },
      childViews: 'label field'.w(),
      isVisibleBinding: SC.Binding.from('Chililog.searchListViewController.isInThePastTimeType').oneWay().not(),

      label: SC.LabelView.design({
        layout: { top: 0, left: 0, right: 0, height: 20 },
        fontWeight: SC.BOLD_WEIGHT,
        value: '_searchListView.TimeTo'.loc()
      }),

      field: SC.TextFieldView.design({
        layout: { top: 20, left: 0, right: 0, height: 29 },
        valueBinding: 'Chililog.searchListViewController.advancedTimeTo',
        hint: 'yyyy-mm-dd hh:mm:ss',
        maxLength: 20,
        validator: Chililog.DateTimeValidator.extend({
          keyDownRegExp: /^[0-9 :\-\0]$/,
          format: '%Y-%m-%d %H:%M:%S',
          invalidFieldErrorMessage: '_searchListView.TimeFrom.Invalid'
        })
      })
    }),

    severity: SC.View.design({
      layoutBinding: SC.Binding.from('Chililog.searchListViewController.advancedSeverityLayout').oneWay(),
      childViews: 'label field'.w(),

      label: SC.LabelView.design({
        layout: { top: 0, left: 0, right: 0, height: 20 },
        fontWeight: SC.BOLD_WEIGHT,
        value: '_searchListView.Severity'.loc()
      }),

      field: SC.SelectFieldView.design({
        layout: { top: 20, left: 0, width: 150, height: 29 },
        objects: [
          { name:'_searchListView.Severity.Any'.loc(), value:'' },
          { name:'_repositoryEntryRecord.Severity.Emergency'.loc(), value:'0' },
          { name:'_repositoryEntryRecord.Severity.Action'.loc(), value:'1' },
          { name:'_repositoryEntryRecord.Severity.Critical'.loc(), value:'2' },
          { name:'_repositoryEntryRecord.Severity.Error'.loc(), value:'3' },
          { name:'_repositoryEntryRecord.Severity.Warning'.loc(), value:'4' },
          { name:'_repositoryEntryRecord.Severity.Notice'.loc(), value:'5' },
          { name:'_repositoryEntryRecord.Severity.Information'.loc(), value:'6' },
          { name:'_repositoryEntryRecord.Severity.Debug'.loc(), value:'7' }
        ],
        nameKey: 'name',
        valueKey: 'value',
        disableSort: YES,
        valueBinding: 'Chililog.searchListViewController.advancedSeverity'
      })
    }),

    source: SC.View.design({
      layout: { top: 70, left: 15, width: 150, height: 50 },
      childViews: 'label field'.w(),

      label: SC.LabelView.design({
        layout: { top: 0, left: 0, right: 0, height: 20 },
        fontWeight: SC.BOLD_WEIGHT,
        value: '_searchListView.Source'.loc()
      }),

      field: SC.TextFieldView.design({
        layout: { top: 20, left: 0, right: 0, height: 29 },
        valueBinding: 'Chililog.searchListViewController.advancedSource',
        maxLength: 300
      })
    }),

    host: SC.View.design({
      layout: { top: 70, left: 175, width: 150, height: 50 },
      childViews: 'label field'.w(),

      label: SC.LabelView.design({
        layout: { top: 0, left: 0, right: 0, height: 20 },
        fontWeight: SC.BOLD_WEIGHT,
        value: '_searchListView.Host'.loc()
      }),

      field: SC.TextFieldView.design({
        layout: { top: 20, left: 0, right: 0, height: 29 },
        valueBinding: 'Chililog.searchListViewController.advancedHost',
        maxLength: 300
      })
    }),

    keywords: SC.View.design({
      layoutBinding: SC.Binding.from('Chililog.searchListViewController.advancedKeywordsLayout').oneWay(),
      childViews: 'label field'.w(),

      label: SC.LabelView.design({
        layout: { top: 0, left: 0, right: 0, height: 20 },
        fontWeight: SC.BOLD_WEIGHT,
        value: '_searchListView.Keywords'.loc()
      }),

      field: SC.TextFieldView.design({
        layout: { top: 20, left: 0, right: 0, height: 29 },
        valueBinding: 'Chililog.searchListViewController.advancedKeywords',
        maxLength: 300
      })
    }),

    conditions: SC.View.design({
      layoutBinding: SC.Binding.from('Chililog.searchListViewController.advancedConditionsLayout').oneWay(),
      childViews: 'label field'.w(),

      label: SC.LabelView.design({
        layout: { top: 0, left: 0, right: 0, height: 20 },
        fontWeight: SC.BOLD_WEIGHT,
        value: '_searchListView.Conditions'.loc()
      }),

      field: SC.TextFieldView.design({
        layout: { top: 20, left: 0, right: 0, height: 64 },
        valueBinding: 'Chililog.searchListViewController.advancedConditions',
        isTextArea: YES,
        validator: Chililog.JsonValidator.extend({
          invalidFieldErrorMessage: '_searchListView.Condition.Invalid'
        })
      })
    }),

    searchButton: SC.ButtonView.design({
      layoutBinding: SC.Binding.from('Chililog.searchListViewController.advancedSearchButtonLayout').oneWay(),
      title: '_searchListView.Search',
      localize: YES,
      controlSize: SC.HUGE_CONTROL_SIZE,
      isEnabledBinding: SC.Binding.from('Chililog.searchListViewController.isSearching').oneWay().not(),
      isDefault: YES,
      target: Chililog.searchListViewController,
      action: 'advancedSearch'
    }),

    searchingImage: Chililog.ImageView.design({
      layoutBinding: SC.Binding.from('Chililog.searchListViewController.advancedSearchImageLayout').oneWay(),
      value: sc_static('images/working'),
      isVisibleBinding: SC.Binding.from('Chililog.searchListViewController.isSearching').oneWay().bool(),
      useImageQueue: NO
    }),

    toggleSearchModeButton: SC.ButtonView.design({
      layoutBinding: SC.Binding.from('Chililog.searchListViewController.advancedToggleSearchModeButtonLayout').oneWay(),
      align: SC.ALIGN_RIGHT,
      titleBinding: SC.Binding.from('Chililog.searchListViewController.toggleSearchModeButtonTitle').oneWay(),
      localize: YES,
      controlSize: SC.HUGE_CONTROL_SIZE,
      isVisibleBinding: SC.Binding.from('Chililog.searchListViewController.isSearching').oneWay().not(),
      target: Chililog.searchListViewController,
      action: 'toggleSearchMode'
    })
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

