// =====================NO=====================================================
// Project:   Chililog
// Copyright: ©2011 My Company, Inc.
// ==========================================================================

sc_require('views/image_view');
sc_require('views/radio_view');
sc_require('views/validators');
sc_require('views/label_mixin');

/**
 * Repository details
 */
Chililog.ConfigureRepositoryInfoDetailView = SC.PanelPane.design({
  layout: { width:850, height:450, centerX:0, centerY:-50 },
  contentView: SC.View.extend({
    childViews: 'title body buttons'.w(),

    title: SC.LabelView.design({
      layout: { top: 10, left: 10, right: 10, height: 30 },
      tagName: 'h1',
      controlSize: SC.HUGE_CONTROL_SIZE,
      valueBinding: 'Chililog.configureRepositoryInfoDetailViewController.title',
      localize: YES
    }),

    body: SC.TabView.design({
      layout: { top: 50, left: 10, bottom: 50, right: 10 },
      itemTitleKey: 'title',
      itemValueKey: 'value',
      items: [
        { title: '_configureRepositoryInfoDetailView.GeneralAttributes'.loc(), value: 'Chililog.repositoryAttributesView'},
        { title: '_configureRepositoryInfoDetailView.WriteQueueAttributes'.loc(), value: 'Chililog.repositoryWriteQueueAttributesView'},
        { title: '_configureRepositoryInfoDetailView.ReadQueueAttributes'.loc(), value: 'Chililog.repositoryReadQueueAttributesView'},
        { title: '_configureRepositoryInfoDetailView.RepositoryAccesses'.loc(), value: 'Chililog.repositoryAccessView'}
      ]
    }),

    buttons: SC.View.design({
      layout: { bottom: 0, left: 10, right: 10, height: 40 },
      childViews: 'previousButton nextButton deleteButton savingImage saveButton cancelButton savingImage'.w(),

      previousButton: SC.ButtonView.design({
        layout: { top: 0, left: 0, width: 40 },
        title: '_previous',
        localize: YES,
        controlSize: SC.HUGE_CONTROL_SIZE,
        isVisibleBinding: SC.Binding.from('Chililog.configureRepositoryInfoDetailViewController.canDelete').oneWay().bool(),
        target: Chililog.configureRepositoryInfoDetailViewController,
        action: 'previous'
      }),

      nextButton: SC.ButtonView.design({
        layout: { top: 0, left: 50, width: 40 },
        title: '_next',
        localize: YES,
        controlSize: SC.HUGE_CONTROL_SIZE,
        isVisibleBinding: SC.Binding.from('Chililog.configureRepositoryInfoDetailViewController.canDelete').oneWay().bool(),
        target: Chililog.configureRepositoryInfoDetailViewController,
        action: 'next'
      }),

      deleteButton: SC.ButtonView.design({
        layout: {top: 0, centerX: 0, width: 80 },
        title: '_delete',
        localize: YES,
        controlSize: SC.HUGE_CONTROL_SIZE,
        isVisibleBinding: SC.Binding.from('Chililog.configureRepositoryInfoDetailViewController.canDelete').oneWay().bool(),
        target: Chililog.configureRepositoryInfoDetailViewController,
        action: 'confirmErase'
      }),

      savingImage: Chililog.ImageView.design({
        layout: { top: 5, right: 180, width: 16, height: 16 },
        value: sc_static('images/working'),
        isVisibleBinding: SC.Binding.from('Chililog.configureRepositoryInfoDetailViewController.isSaving').oneWay().bool(),
        useImageQueue: NO
      }),

      saveButton: SC.ButtonView.design({
        layout: {top: 0, right: 90, width: 80 },
        title: '_save',
        localize: YES,
        controlSize: SC.HUGE_CONTROL_SIZE,
        isDefault: YES,
        isEnabledBinding: SC.Binding.from('Chililog.configureRepositoryInfoDetailViewController.canSave').oneWay(),
        target: 'Chililog.configureRepositoryInfoDetailViewController',
        action: 'save'
      }),

      cancelButton: SC.ButtonView.design({
        layout: {top: 0, right: 0, width: 80 },
        title: '_cancel',
        localize: YES,
        controlSize: SC.HUGE_CONTROL_SIZE,
        isCancel: YES,
        target: 'Chililog.configureRepositoryInfoDetailViewController',
        action: 'discardChanges'
      })
    })

  })
});

/**
 * Instance configure repository view
 */
Chililog.configureRepositoryInfoDetailView = Chililog.ConfigureRepositoryInfoDetailView.create();


/**
 * Write Queue views to fit in our tabs
 */
Chililog.RepositoryAttributesView = SC.View.design({
  layout: {top: 0, left: 0, right: 0, bottom: 0},
  classNames: ['data-group'],
  childViews: 'name displayName description currentStatus startupStatus'.w(),

  name: SC.View.design({
    layout: {top: 10, left: 0, right: 0, height: 50 },
    classNames: ['data-item'],
    childViews: 'label field help'.w(),

    label: SC.LabelView.design(Chililog.RequiredFieldLabelMixin, {
      layout: { top: 15, left: 10, width: 200, height: 30 },
      value: '_configureRepositoryInfoDetailView.Name'.loc()
    }),

    field: SC.TextFieldView.design({
      layout: { top: 10, left: 210, width: 200, height: 30 },
      valueBinding: 'Chililog.configureRepositoryInfoDetailViewController.name',
      maxLength: 50,
      validator: Chililog.RegExpValidator.extend({
        keyDownRegExp: /[a-z0-9_]/, //Only allow a-z, 0-9, _
        fieldRegExp: /^[a-z0-9_]+$/,
        invalidFieldErrorMessage: '_configureRepositoryInfoDetailView.Name.Invalid',
        requiredFieldErrorMessage: '_configureRepositoryInfoDetailView.Name.Required'
      })
    }),

    help: SC.LabelView.design({
      layout: { top: 17, left: 420, right: 10, height: 30 },
      classNames: ['help'],
      value: '_configureRepositoryInfoDetailView.Name.Help'.loc()
    })
  }),

  displayName: SC.View.design({
    layout: {top:60, left: 0, right: 0, height: 49 },
    classNames: ['data-item'],
    childViews: 'label field'.w(),

    label: SC.LabelView.design({
      layout: { top: 15, left: 10, width: 200, height: 30 },
      value: '_configureRepositoryInfoDetailView.DisplayName'.loc()
    }),

    field: SC.TextFieldView.design({
      layout: { top: 10, left: 210, width: 200, height: 30 },
      valueBinding: 'Chililog.configureRepositoryInfoDetailViewController.displayName',
      maxLength: 100
    })
  }),

  description: SC.View.design({
    layout: {top: 110, left: 0, right: 0, height: 99 },
    classNames: ['data-item'],
    childViews: 'label field'.w(),

    label: SC.LabelView.design({
      layout: { top: 15, left: 10, width: 200, height: 30 },
      value: '_configureRepositoryInfoDetailView.Description'.loc()
    }),

    field: SC.TextFieldView.design({
      layout: { top: 10, left: 210, width: 500, height: 70 },
      isTextArea: YES,
      valueBinding: 'Chililog.configureRepositoryInfoDetailViewController.description'
    })
  }),

  currentStatus: SC.View.design({
    layout: {top: 210, left: 0, right: 0, height: 49 },
    classNames: ['data-item'],
    childViews: 'label label2'.w(),

    label: SC.LabelView.design({
      layout: { top: 15, left: 10, width: 200, height: 30 },
      value: '_configureRepositoryInfoDetailView.CurrentStatus'.loc()
    }),

    label2: SC.LabelView.design({
      layout: { top: 10, left: 210, width: 300, height: 30 },
      classNames: ['readonly'],
      valueBinding: 'Chililog.configureRepositoryInfoDetailViewController.currentStatusText'
    })
  }),

  startupStatus: SC.View.design({
    layout: {top: 260, left: 0, right: 0, height: 100 },
    classNames: ['data-item'],
    childViews: 'label field'.w(),

    label: SC.LabelView.design(Chililog.RequiredFieldLabelMixin, {
      layout: { top: 15, left: 10, width: 200, height: 30 },
      value: '_configureRepositoryInfoDetailView.StartupStatus'.loc()
    }),

    field: Chililog.RadioView.design({
      layout: { top: 15, left: 210, width: 500, height: 80 },
      items: [
        { title: '_configureRepositoryInfoDetailView.StartupStatus.Online'.loc(), value: 'ONLINE' },
        { title: '_configureRepositoryInfoDetailView.StartupStatus.Offline'.loc(), value: 'OFFLINE' }
      ],
      itemTitleKey: 'title',
      itemValueKey: 'value',
      valueBinding: 'Chililog.configureRepositoryInfoDetailViewController.startupStatus'
    })
  })


});


/**
 * Instance configure repository view
 */
Chililog.repositoryAttributesView = Chililog.RepositoryAttributesView.create();


/**
 * Write Queue views to fit in our tabs
 */
Chililog.RepositoryWriteQueueAttributesView = SC.ScrollView.design({
  layout: {top: 0, left: 0, right: 0, bottom: 0 },
  classNames: ['data-group'],
  contentView:   SC.View.design({
    layout: {top: 0, left: 0, right: 0, height: 609 },
    classNames: ['data-group'],
    childViews: ('writeQueueAddress writeQueueUsername writeQueuePassword writeQueueDurable ' +
      'maxKeywords writeQueueWorkerCount writeQueueMaxMemory writeQueueMaxMemoryPolicy writeQueuePageSize ' +
      'writeQueuePageCountCache').w(),

    writeQueueAddress: SC.View.design({
      layout: {top: 10, left: 0, right: 0, height: 49 },
      classNames: ['data-item'],
      childViews: 'label label2'.w(),

      label: SC.LabelView.design({
        layout: { top: 15, left: 10, width: 200, height: 30 },
        value: '_configureRepositoryInfoDetailView.WriteQueueAddress'.loc()
      }),

      label2: SC.LabelView.design({
        layout: { top: 10, left: 210, right: 10, height: 30 },
        classNames: ['readonly'],
        valueBinding: 'Chililog.configureRepositoryInfoDetailViewController.writeQueueAddress'
      })
    }),

    writeQueueUsername: SC.View.design({
      layout: {top: 60, left: 0, right: 0, height: 49 },
      classNames: ['data-item'],
      childViews: 'label label2'.w(),

      label: SC.LabelView.design({
        layout: { top: 15, left: 10, width: 200, height: 30 },
        value: '_configureRepositoryInfoDetailView.WriteQueueUsername'.loc()
      }),

      label2: SC.LabelView.design({
        layout: { top: 10, left: 210, right: 10, height: 30 },
        classNames: ['readonly'],
        valueBinding: 'Chililog.configureRepositoryInfoDetailViewController.name'
      })
    }),

    writeQueuePassword: SC.View.design({
      layout: {top: 110, left: 0, right: 0, height: 49 },
      classNames: ['data-item'],
      childViews: 'label field'.w(),

      label: SC.LabelView.design({
        layout: { top: 15, left: 10, width: 200, height: 30 },
        value: '_configureRepositoryInfoDetailView.WriteQueuePassword'.loc()
      }),

      field: SC.TextFieldView.design({
        layout: { top: 10, left: 210, width: 200, height: 30 },
        valueBinding: 'Chililog.configureRepositoryInfoDetailViewController.writeQueuePassword',
        maxLength: 100
      })
    }),

    writeQueueDurable: SC.View.design({
      layout: {top: 160, left: 0, right: 0, height: 69 },
      classNames: ['data-item'],
      childViews: 'label field'.w(),

      label: SC.LabelView.design({
        layout: { top: 15, left: 10, width: 200, height: 30 },
        value: '_configureRepositoryInfoDetailView.WriteQueueDurable'.loc()
      }),

      field: Chililog.RadioView.design(Chililog.RequiredFieldLabelMixin, {
        layout: { top: 15, left: 210, width: 500, height: 80 },
        items: [
          { title: '_configureRepositoryInfoDetailView.WriteQueueDurable.Yes'.loc(), value: YES },
          { title: '_configureRepositoryInfoDetailView.WriteQueueDurable.No'.loc(), value: NO }
        ],
        itemTitleKey: 'title',
        itemValueKey: 'value',
        valueBinding: 'Chililog.configureRepositoryInfoDetailViewController.writeQueueDurable'
      })
    }),

    maxKeywords: SC.View.design({
      layout: {top: 230, left: 0, right: 0, height: 49 },
      classNames: ['data-item'],
      childViews: 'label field help'.w(),

      label: SC.LabelView.design(Chililog.RequiredFieldLabelMixin, {
        layout: { top: 15, left: 10, width: 200, height: 30 },
        value: '_configureRepositoryInfoDetailView.MaxKeywords'.loc()
      }),

      field: SC.TextFieldView.design({
        layout: { top: 10, left: 210, width: 50, height: 30 },
        valueBinding: 'Chililog.configureRepositoryInfoDetailViewController.maxKeywords',
        maxLength: 3,
        validator: Chililog.PositiveIntegerValidator.extend({
          formatNumber: YES,
          requiredFieldErrorMessage: '_configureRepositoryInfoDetailView.MaxKeywords.Required'
        })
      }),

      help: SC.LabelView.design({
        layout: { top: 17, left: 270, right: 10, height: 30 },
        classNames: ['help'],
        value: '_configureRepositoryInfoDetailView.MaxKeywords.Help'.loc()
      })
    }),

    writeQueueWorkerCount: SC.View.design({
      layout: {top: 280, left: 0, right: 0, height: 49 },
      classNames: ['data-item'],
      childViews: 'label field help'.w(),

      label: SC.LabelView.design(Chililog.RequiredFieldLabelMixin, {
        layout: { top: 15, left: 10, width: 200, height: 30 },
        value: '_configureRepositoryInfoDetailView.WriteQueueWorkerCount'.loc()
      }),

      field: SC.TextFieldView.design({
        layout: { top: 10, left: 210, width: 50, height: 30 },
        valueBinding: 'Chililog.configureRepositoryInfoDetailViewController.writeQueueWorkerCount',
        maxLength: 2,
        validator: Chililog.PositiveIntegerValidator.extend({
          formatNumber: YES,
          requiredFieldErrorMessage: '_configureRepositoryInfoDetailView.WriteQueueWorkerCount.Required'
        })
      }),

      help: SC.LabelView.design({
        layout: { top: 17, left: 270, right: 10, height: 30 },
        classNames: ['help'],
        value: '_configureRepositoryInfoDetailView.WriteQueueWorkerCount.Help'.loc()
      })
    }),

    writeQueueMaxMemory: SC.View.design({
      layout: {top: 330, left: 0, right: 0, height: 49 },
      classNames: ['data-item'],
      childViews: 'label field help'.w(),

      label: SC.LabelView.design(Chililog.RequiredFieldLabelMixin, {
        layout: { top: 15, left: 10, width: 200, height: 30 },
        value: '_configureRepositoryInfoDetailView.WriteQueueMaxMemory'.loc()
      }),

      field: SC.TextFieldView.design({
        layout: { top: 10, left: 210, width: 100, height: 30 },
        valueBinding: 'Chililog.configureRepositoryInfoDetailViewController.writeQueueMaxMemory',
        maxLength: 10,
        validator: Chililog.PositiveIntegerValidator.extend({
          formatNumber: YES,
          requiredFieldErrorMessage: '_configureRepositoryInfoDetailView.WriteQueueMaxMemory.Required'
        })
      }),

      help: SC.LabelView.design({
        layout: { top: 17, left: 320, right: 10, height: 30 },
        classNames: ['help'],
        value: '_configureRepositoryInfoDetailView.WriteQueueMaxMemory.Help'.loc()
      })
    }),

    writeQueueMaxMemoryPolicy: SC.View.design({
      layout: {top: 380, left: 0, right: 0, height: 89 },
      classNames: ['data-item'],
      childViews: 'label field'.w(),

      label: SC.LabelView.design(Chililog.RequiredFieldLabelMixin, {
        layout: { top: 15, left: 10, width: 200, height: 30 },
        value: '_configureRepositoryInfoDetailView.WriteQueueMaxMemoryPolicy'.loc()
      }),

      field: Chililog.RadioView.design({
        layout: { top: 15, left: 210, width: 600, height: 70 },
        items: [
          { title: '_configureRepositoryInfoDetailView.WriteQueueMaxMemoryPolicy.Page'.loc(), value: 'PAGE'},
          { title: '_configureRepositoryInfoDetailView.WriteQueueMaxMemoryPolicy.Drop'.loc(), value: 'DROP'},
          { title: '_configureRepositoryInfoDetailView.WriteQueueMaxMemoryPolicy.Block'.loc(), value: 'BLOCK'}
        ],
        itemTitleKey: 'title',
        itemValueKey: 'value',
        valueBinding: 'Chililog.configureRepositoryInfoDetailViewController.writeQueueMaxMemoryPolicy'
      })
    }),

    writeQueuePageSize: SC.View.design({
      layout: {top: 470, left: 0, right: 0, height: 49 },
      classNames: ['data-item'],
      childViews: 'label field help'.w(),

      label: SC.LabelView.design(Chililog.RequiredFieldLabelMixin, {
        layout: { top: 15, left: 10, width: 200, height: 30 },
        value: '_configureRepositoryInfoDetailView.WriteQueuePageSize'.loc()
      }),

      field: SC.TextFieldView.design({
        layout: { top: 10, left: 210, width: 100, height: 30 },
        valueBinding: 'Chililog.configureRepositoryInfoDetailViewController.writeQueuePageSize',
        validator: Chililog.PositiveIntegerValidator.extend({
          formatNumber: YES,
          requiredFieldErrorMessage: '_configureRepositoryInfoDetailView.WriteQueuePageSize.Required'
        })
      }),

      help: SC.LabelView.design({
        layout: { top: 17, left: 320, right: 10, height: 30 },
        classNames: ['help'],
        value: '_configureRepositoryInfoDetailView.WriteQueuePageSize.Help'.loc()
      })
    }),

    writeQueuePageCountCache: SC.View.design({
      layout: {top: 520, left: 0, right: 0, height: 49 },
      classNames: ['data-item'],
      childViews: 'label field help'.w(),

      label: SC.LabelView.design(Chililog.RequiredFieldLabelMixin, {
        layout: { top: 15, left: 10, width: 200, height: 30 },
        value: '_configureRepositoryInfoDetailView.WriteQueuePageCountCache'.loc()
      }),

      field: SC.TextFieldView.design({
        layout: { top: 10, left: 210, width: 50, height: 30 },
        valueBinding: 'Chililog.configureRepositoryInfoDetailViewController.writeQueuePageCountCache',
        validator: Chililog.PositiveIntegerValidator.extend({
          formatNumber: YES,
          requiredFieldErrorMessage: '_configureRepositoryInfoDetailView.WriteQueuePageCountCache.Required'
        })
      }),

      help: SC.LabelView.design({
        layout: { top: 17, left: 270, right: 10, height: 30 },
        classNames: ['help'],
        value: '_configureRepositoryInfoDetailView.WriteQueuePageCountCache.Help'.loc()
      })
    })

  })
});


Chililog.repositoryWriteQueueAttributesView = Chililog.RepositoryWriteQueueAttributesView.create();


/**
 * Read Queue views to fit in our tabs
 */
Chililog.RepositoryReadQueueAttributesView = SC.View.design({
  layout: {top: 0, left: 0, right: 0, bottom: 0 },
  classNames: ['data-group'],
  childViews: 'readQueueAddress readQueueUsername readQueuePassword readQueueDurable'.w(),

  readQueueAddress: SC.View.design({
    layout: {top: 10, left: 0, right: 0, height: 49 },
    classNames: ['data-item'],
    childViews: 'label label2'.w(),

    label: SC.LabelView.design({
      layout: { top: 15, left: 10, width: 200, height: 30 },
      value: '_configureRepositoryInfoDetailView.ReadQueueAddress'.loc()
    }),

    label2: SC.LabelView.design({
      layout: { top: 10, left: 210, width: 300, height: 30 },
      classNames: ['readonly'],
      valueBinding: 'Chililog.configureRepositoryInfoDetailViewController.readQueueAddress'
    })
  }),

  readQueueUsername: SC.View.design({
    layout: {top: 60, left: 0, right: 0, height: 49 },
    classNames: ['data-item'],
    childViews: 'label label2'.w(),

    label: SC.LabelView.design({
      layout: { top: 15, left: 10, width: 200, height: 30 },
      value: '_configureRepositoryInfoDetailView.ReadQueueUsername'.loc()
    }),

    label2: SC.LabelView.design({
      layout: { top: 10, left: 210, width: 300, height: 30 },
      classNames: ['readonly'],
      valueBinding: 'Chililog.configureRepositoryInfoDetailViewController.name'
    })
  }),

  readQueuePassword: SC.View.design({
    layout: {top: 110, left: 0, right: 0, height: 49 },
    classNames: ['data-item'],
    childViews: 'label field'.w(),

    label: SC.LabelView.design({
      layout: { top: 15, left: 10, width: 200, height: 30 },
      value: '_configureRepositoryInfoDetailView.ReadQueuePassword'.loc()
    }),

    field: SC.TextFieldView.design({
      layout: { top: 10, left: 210, width: 200, height: 30 },
      valueBinding: 'Chililog.configureRepositoryInfoDetailViewController.readQueuePassword'
    })
  }),

  readQueueDurable: SC.View.design({
    layout: {top: 160, left: 0, right: 0, height: 200 },
    classNames: ['data-item'],
    childViews: 'label field'.w(),

    label: SC.LabelView.design({
      layout: { top: 15, left: 10, width: 200, height: 30 },
      value: '_configureRepositoryInfoDetailView.ReadQueueDurable'.loc()
    }),

    field: Chililog.RadioView.design({
      layout: { top: 15, left: 210, width: 500, height: 80 },
      items: [
        { title: '_configureRepositoryInfoDetailView.ReadQueueDurable.Yes'.loc(), value: YES },
        { title: '_configureRepositoryInfoDetailView.ReadQueueDurable.No'.loc(), value: NO }
      ],
      itemTitleKey: 'title',
      itemValueKey: 'value',
      valueBinding: 'Chililog.configureRepositoryInfoDetailViewController.readQueueDurable'
    })
  })
});

Chililog.repositoryReadQueueAttributesView = Chililog.RepositoryReadQueueAttributesView.create();


/**
 * Repository access view to fit in our tabs
 */
Chililog.RepositoryAccessView = SC.View.design({
  layout: {top: 0, left: 0, right: 0, bottom: 0 },
  classNames: ['data-group'],
  childViews: 'repositoryAccesses'.w(),

  repositoryAccesses: SC.View.design({
    layout: {top: 25, left: 0, right: 0, height: 320 },
    classNames: ['data-item'],
    childViews: 'label field'.w(),

    label: SC.LabelView.design({
      layout: { top: 15, left: 10, width: 200, height: 30 },
      value: '_configureRepositoryInfoDetailView.RepositoryAccesses'.loc()
    }),

    field: SC.TableView.design({
      layout: { top: 15, left: 210, width: 550, height: 270 },
      classNames: ['table'],
      contentBinding: 'Chililog.configureRepositoryInfoDetailViewController.repositoryAccessArrayController.arrangedObjects',
      selectionBinding: 'Chililog.configureRepositoryInfoDetailViewController.repositoryAccessArrayController.selection',
      useHeaders: YES,
      isEditable: NO,
      canEditContent: NO,
      canDeleteContent: NO,

      columns:[
        SC.TableColumn.create({
          key:   'username',
          title: '_configureRepositoryInfoDetailView.RepositoryAccesses.Username'.loc(),
          width: 150,
          isReorderable: NO,
          sortState: SC.SORT_ASCENDING
        }),
        SC.TableColumn.create({
          key:   'userDisplayName',
          title: '_configureRepositoryInfoDetailView.RepositoryAccesses.UserDisplayName'.loc(),
          width: 200,
          isReorderable: NO,
          sortState: SC.SORT_ASCENDING
        }),
        SC.TableColumn.create({
          key:   'role',
          title: '_configureRepositoryInfoDetailView.RepositoryAccesses.Role'.loc(),
          width: 150,
          isReorderable: NO,
          formatter: function(v) {
            var map = Chililog.configureUserDetailViewController.get('repositoryAccessRoles');
            for (var i = 0; i < map.length; i++) {
              if (map[i].code === v) {
                return map[i].displayText;
              }
            }
            return v;
          }
        })
      ],

      /**
       * Reset when visible to make sure that screen is displayed
       correctly when show/not showing in container views
       */
      doReset: function() {
        var isVisibleInWindow = this.get('isVisibleInWindow');
        if (isVisibleInWindow) {
          var x = this.getPath('_dataView.contentView');
          x._reset();
        }
      }.observes('isVisibleInWindow')
    })
  })
});

Chililog.repositoryAccessView = Chililog.RepositoryAccessView.create();
