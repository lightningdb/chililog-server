// ==========================================================================
// Project:   Chililog - mainPage
// Copyright: ©2011 My Company, Inc.
// ==========================================================================

sc_require('views/image_view');
sc_require('views/radio_view');
sc_require('views/validators');

/**********************************************************************************************************************
 * Main
 **********************************************************************************************************************/

/**
 * Configure view
 */
Chililog.ConfigureView = SC.View.design({
  layout: { top: 10, left: 10, bottom: 10, right: 10 },
  childViews: 'title left right'.w(),

  title: SC.LabelView.design({
    layout: { top: 0, left: 0, width: 200, height: 30 },
    tagName: 'h1',
    controlSize: SC.HUGE_CONTROL_SIZE,
    value: '_configureView.Title',
    localize: YES
  }),

  left: SC.ScrollView.design({
    layout: { top: 35, left: 0, bottom: 0, width: 200 },
    classNames: ['list-menu'],

    contentView: SC.ListView.design({
      layout: { top: 0, bottom: 0, left: 0, right: 0 },
      rowHeight: 40,
      isEditable: NO,
      actOnSelect: YES,
      hasContentIcon: YES,
      contentValueKey: 'label',
      contentIconKey: 'icon',
      content: [
        {
          id: 'Repositories',
          label: '_configureView.Repositories'.loc(),
          icon: sc_static('images/repositories.png')
        },
        {
          id: 'Users',
          label: '_configureView.Users'.loc(),
          icon: sc_static('images/users.png')
        }
      ],
      target: Chililog.configureViewController,
      action: 'onSelect'
    })
  }),

  right: SC.SceneView.design({
    layout: { top: 35, bottom: 0, left: 208, right: 0 },
    classNames: ['box']
  })

});

/**
 * Instance configure view
 */
Chililog.configureView = Chililog.ConfigureView.create();


/**********************************************************************************************************************
 * Users
 **********************************************************************************************************************/

/**
 * User List
 */
Chililog.ConfigureUserListView = SC.View.design({
  layout: { top: 0, left: 0, bottom: 0, right: 0 },
  childViews: 'title createButton table'.w(),

  title: SC.LabelView.design({
    layout: { top: 5, left: 10, width: 200, height: 30 },
    tagName: 'h1',
    controlSize: SC.HUGE_CONTROL_SIZE,
    value: '_configureUserListView.Title',
    localize: YES
  }),

  createButton: SC.ButtonView.design({
    layout: { top: 40, left: 10, width: 130 },
    title: '_configureUserListView.Create',
    localize: YES,
    target: Chililog.configureUserListViewController,
    action: 'create'
  }),

  moreButton: SC.PopupButtonView.design({
    layout: { top: 40, left: 150, width: 130, height: 30 },
    classNames: ['button'],
    title: '_moreActions',
    localize: YES,
    menu: SC.MenuPane.design({
      layout: { width: 200 },
      items: [
        {
          title: '_deleteSelected'
        }
      ]
    })
  }),

  table: SC.TableView.design({
    layout: { top: 80, left: 10, right: 10, bottom: 10 },
    classNames: ['table'],
    contentBinding: 'Chililog.configureUserListViewController.arrangedObjects',
    selectionBinding: 'Chililog.configureUserListViewController.selection',
    useHeaders: YES,
    isEditable: NO,
    columns:[
      SC.TableColumn.create({
        key:   'username',
        title: '_configureUserDetailView.Username'.loc(),
        width: 150,
        isReorderable: NO   //Bug with reorder when switching with other configure options
      }),
      SC.TableColumn.create({
        key:   'emailAddress',
        title: '_configureUserDetailView.EmailAddress'.loc(),
        width: 250,
        isReorderable: NO
      }),
      SC.TableColumn.create({
        key:   'displayName',
        title: '_configureUserDetailView.DisplayName'.loc(),
        width: 250,
        isReorderable: NO,
        formatter:  function(v) {
          return SC.none(v) ? '' : v;
        }
      }),
      SC.TableColumn.create({
        key:   'currentStatus',
        title: '_configureUserDetailView.CurrentStatus'.loc(),
        width: 100,
        isReorderable: NO
      })
    ],
    target: Chililog.configureUserListViewController,
    action: 'edit'
  })

});

Chililog.configureUserListView = Chililog.ConfigureUserListView.create();

/**
 * User details
 */
Chililog.ConfigureUserDetailView = SC.View.design({
  layout: { top: 0, left: 0, bottom: 0, right: 0 },
  childViews: 'title backButton deleteButton body'.w(),

  title: SC.LabelView.design({
    layout: { top: 5, left: 10, right: 10, height: 30 },
    tagName: 'h1',
    controlSize: SC.HUGE_CONTROL_SIZE,
    valueBinding: 'Chililog.configureUserDetailViewController.title',
    localize: YES
  }),

  backButton: SC.ButtonView.design({
    layout: { top: 40, left: 10, width: 80 },
    title: '_back',
    localize: YES,
    isEnabledBinding: SC.Binding.from('Chililog.configureUserDetailViewController.canSave').oneWay().not(),
    target: Chililog.configureUserListViewController,
    action: 'show'
  }),

  deleteButton: SC.ButtonView.design({
    layout: {top: 40, left: 100, width: 80 },
    title: '_delete',
    localize: YES,
    isEnabledBinding: SC.Binding.from('Chililog.configureUserDetailViewController.canSave').oneWay().not(),
    target: Chililog.configureUserDetailViewController,
    action: 'confirmErase'
  }),

  body: SC.ScrollView.design({
    layout: { top: 80, left: 10, bottom: 10, right: 10 },
    classNames: ['box'],
    contentView: SC.View.design({
      layoutBinding: SC.Binding.from('Chililog.configureUserDetailViewController.bodyLayout').oneWay(),
      childViews: 'username emailAddress displayName currentStatus password confirmPassword buttons'.w(),

      username: SC.View.design({
        layout: {top: 0, left: 0, right: 0, height: 50 },
        classNames: ['data-item'],
        childViews: 'label field'.w(),

        label: SC.LabelView.design({
          layout: { top: 15, left: 10, width: 200, height: 30 },
          value: '_configureUserDetailView.Username',
          localize: YES
        }),

        field: SC.TextFieldView.design({
          layout: { top: 10, left: 210, width: 300, height: 30 },
          valueBinding: 'Chililog.configureUserDetailViewController.username',
          maxLength: 100,
          validator: Chililog.RegExpValidator.extend({ keyDownRegExp: /[A-Za-z0-9_\-\.'@]/ })
        })
      }),

      emailAddress: SC.View.design({
        layout: {top: 50, left: 0, right: 0, height: 50 },
        classNames: ['data-item'],
        childViews: 'label field'.w(),

        label: SC.LabelView.design({
          layout: { top: 15, left: 10, width: 200, height: 30 },
          value: '_configureUserDetailView.EmailAddress',
          localize: YES
        }),

        field: SC.TextFieldView.design({
          layout: { top: 10, left: 210, width: 300, height: 30 },
          valueBinding: 'Chililog.configureUserDetailViewController.emailAddress',
          maxLength: 200,
          validator: Chililog.RegExpValidator.extend({ keyDownRegExp: /[A-Za-z0-9_\-\.'@]/ })
        })
      }),

      displayName: SC.View.design({
        layout: {top: 100, left: 0, right: 0, height: 80 },
        classNames: ['data-item'],
        childViews: 'label help field'.w(),

        label: SC.LabelView.design({
          layout: { top: 15, left: 10, width: 200, height: 20 },
          value: '_configureUserDetailView.DisplayName',
          localize: YES
        }),

        help: SC.LabelView.design({
          classNames: ['help'],
          layout: { top: 35, left: 10, width: 170, height: 50 },
          value: '_configureUserDetailView.DisplayNameHelp',
          localize: YES
        }),

        field: SC.TextFieldView.design({
          layout: { top: 10, left: 210, width: 300, height: 30 },
          valueBinding: 'Chililog.configureUserDetailViewController.displayName',
          maxLength: 100
        })
      }),

      currentStatus: SC.View.design({
        layout: {top: 180, left: 0, right: 0, height: 90 },
        classNames: ['data-item'],
        childViews: 'label field'.w(),

        label: SC.LabelView.design({
          layout: { top: 15, left: 10, width: 200, height: 30 },
          value: '_configureUserDetailView.CurrentStatus',
          localize: YES
        }),

        field: Chililog.RadioView.design({
          layout: { top: 15, left: 210, width: 500, height: 80 },
          items: [
            { title: '_configureUserDetailView.CurrentStatus.Enabled'.loc(), value: 'Enabled'},
            { title: '_configureUserDetailView.CurrentStatus.Disabled'.loc(), value: 'Disabled'},
            { title: '_configureUserDetailView.CurrentStatus.Locked'.loc(), value: 'Locked'}
          ],
          itemTitleKey: 'title',
          itemValueKey: 'value',
          valueBinding: 'Chililog.configureUserDetailViewController.currentStatus'
        })
      }),

      password: SC.View.design({
        layout: {top: 270, left: 0, right: 0, height: 50 },
        classNames: ['data-item'],
        childViews: 'label field'.w(),
        isVisibleBinding: SC.Binding.from('Chililog.configureUserDetailViewController.isCreating').oneWay(),

        label: SC.LabelView.design({
          layout: { top: 15, left: 10, width: 200, height: 30 },
          value: '_configureUserDetailView.Password',
          localize: YES
        }),

        field: SC.TextFieldView.design({
          layout: { top: 10, left: 210, width: 300, height: 30 },
          isPassword: YES,
          valueBinding: 'Chililog.configureUserDetailViewController.password',
          maxLength: 100
        })
      }),

      confirmPassword: SC.View.design({
        layout: {top: 320, left: 0, right: 0, height: 50 },
        classNames: ['data-item'],
        childViews: 'label field'.w(),
        isVisibleBinding: SC.Binding.from('Chililog.configureUserDetailViewController.isCreating').oneWay(),

        label: SC.LabelView.design({
          layout: { top: 15, left: 10, width: 200, height: 30 },
          value: '_configureUserDetailView.ConfirmPassword',
          localize: YES
        }),

        field: SC.TextFieldView.design({
          layout: { top: 10, left: 210, width: 300, height: 30 },
          isPassword: YES,
          valueBinding: 'Chililog.configureUserDetailViewController.confirmPassword',
          maxLength: 100
        })
      }),

      buttons: SC.View.design({
        layoutBinding: 'Chililog.configureUserDetailViewController.buttonsLayout',
        childViews: 'saveButton cancelButton savingImage successMessage'.w(),

        saveButton: SC.ButtonView.design({
          layout: {top: 10, left: 10, width: 90 },
          title: '_save',
          localize: YES,
          controlSize: SC.HUGE_CONTROL_SIZE,
          isDefault: YES,
          isEnabledBinding: SC.Binding.from('Chililog.configureUserDetailViewController.canSave').oneWay(),
          target: 'Chililog.configureUserDetailViewController',
          action: 'save'
        }),

        cancelButton: SC.ButtonView.design({
          layout: {top: 10, left: 110, width: 90 },
          title: '_cancel',
          localize: YES,
          controlSize: SC.HUGE_CONTROL_SIZE,
          isEnabledBinding: SC.Binding.from('Chililog.configureUserDetailViewController.canSave').oneWay(),
          target: 'Chililog.configureUserDetailViewController',
          action: 'discardChanges'
        }),

        savingImage: Chililog.ImageView.design({
          layout: { top: 15, left: 210, width: 16, height: 16 },
          value: sc_static('images/working'),
          isVisibleBinding: SC.Binding.from('Chililog.configureUserDetailViewController.isSaving').oneWay().bool(),
          useImageQueue: NO
        }),

        successMessage: SC.LabelView.design({
          layout: { top: 10, left: 210, width: 200, height: 25, opacity: 0 },
          classNames: ['success'],
          value: '_saveSuccess',
          localize: YES
        })
      })
    })
  })
});

/**
 * Instance configure user view
 */
Chililog.configureUserDetailView = Chililog.ConfigureUserDetailView.create();


/**********************************************************************************************************************
 * Repositories
 **********************************************************************************************************************/

/**
 * Repositories Listing
 */
Chililog.ConfigureRepositoryInfoListView = SC.View.design({
  layout: { top: 0, left: 0, bottom: 0, right: 0 },
  childViews: 'title createButton table'.w(),

  title: SC.LabelView.design({
    layout: { top: 5, left: 10, width: 200, height: 30 },
    tagName: 'h1',
    controlSize: SC.HUGE_CONTROL_SIZE,
    value: '_configureRepositoryInfoListView.Title',
    localize: YES
  }),

  createButton: SC.ButtonView.design({
    layout: { top: 40, left: 10, width: 170 },
    title: '_configureRepositoryInfoListView.Create',
    localize: YES,
    target: Chililog.configureRepositoryInfoListViewController,
    action: 'create'
  }),

  table: SC.TableView.design({
    layout: { top: 80, left: 10, right: 10, bottom: 10 },
    classNames: ['table'],
    contentBinding: 'Chililog.configureRepositoryInfoListViewController.arrangedObjects',
    selectionBinding: 'Chililog.configureRepositoryInfoListViewController.selection',
    useHeaders: YES,
    isEditable: NO,
    columns:[
      SC.TableColumn.create({
        key:   'name',
        title: '_configureRepositoryInfoDetailView.Name'.loc(),
        width: 150,
        isReorderable: NO   //Bug with reorder when switching with other configure options
      }),
      SC.TableColumn.create({
        key:   'displayName',
        title: '_configureRepositoryInfoDetailView.DisplayName'.loc(),
        width: 250,
        isReorderable: NO,
        formatter:  function(v) {
          return SC.none(v) ? '' : v;
        }
      }),
      SC.TableColumn.create({
        key:   'currentStatus',
        title: '_configureRepositoryInfoDetailView.CurrentStatus'.loc(),
        width: 120,
        isReorderable: NO
      }),
      SC.TableColumn.create({
        key:   'description',
        title: '_configureRepositoryInfoDetailView.Description'.loc(),
        width: 300,
        isReorderable: NO,
        formatter:  function(v) {
          return SC.none(v) ? '' : v;
        }
      })
    ],
    target: Chililog.configureRepositoryInfoListViewController,
    action: 'edit'
  })

});

Chililog.configureRepositoryInfoListView = Chililog.ConfigureRepositoryInfoListView.create();


/**
 * Repository details
 */
Chililog.ConfigureRepositoryInfoDetailView = SC.View.design({
  layout: { top: 10, left: 10, bottom: 10, right: 10 },
  childViews: 'title backButton deleteButton body'.w(),

  title: SC.LabelView.design({
    layout: { top: 5, left: 10, right: 10, height: 30 },
    tagName: 'h1',
    controlSize: SC.HUGE_CONTROL_SIZE,
    valueBinding: 'Chililog.configureRepositoryInfoDetailViewController.title',
    localize: YES
  }),

  backButton: SC.ButtonView.design({
    layout: { top: 40, left: 10, width: 80 },
    title: '_back',
    localize: YES,
    isEnabledBinding: SC.Binding.from('Chililog.configureRepositoryInfoDetailViewController.canSave').oneWay().not(),
    target: Chililog.configureRepositoryInfoListViewController,
    action: 'show'
  }),

  deleteButton: SC.ButtonView.design({
    layout: {top: 40, left: 100, width: 80 },
    title: '_delete',
    localize: YES,
    isEnabledBinding: SC.Binding.from('Chililog.configureRepositoryInfoDetailViewController.canSave').oneWay().not(),
    target: Chililog.configureRepositoryInfoDetailViewController,
    action: 'confirmErase'
  }),

  body: SC.ScrollView.design({
    layout: { top: 80, left: 10, bottom: 10, right: 10 },
    classNames: ['box'],
    contentView: SC.View.design({
      layout: { top: 0, left: 0, right: 0, height: 1300 },
      childViews: 'name displayName description currentStatus startupStatus writeQueueAttributes readQueueAttributes buttons'.w(),

      name: SC.View.design({
        layout: {top: 0, left: 0, right: 0, height: 50 },
        classNames: ['data-item'],
        childViews: 'label field help'.w(),

        label: SC.LabelView.design({
          layout: { top: 15, left: 10, width: 200, height: 30 },
          value: '_configureRepositoryInfoDetailView.Name',
          localize: YES
        }),

        field: SC.TextFieldView.design({
          layout: { top: 10, left: 210, width: 200, height: 30 },
          valueBinding: 'Chililog.configureRepositoryInfoDetailViewController.name',
          maxLength: 50,
          validator: Chililog.RegExpValidator.extend({ keyDownRegExp: /[a-z0-9_]/  }) //Only allow a-z, 0-9, _
        }),

        help: SC.LabelView.design({
          layout: { top: 17, left: 420, right: 10, height: 30 },
          classNames: ['help'],
          value: '_configureRepositoryInfoDetailView.NameHelp',
          localize: YES
        })
      }),

      displayName: SC.View.design({
        layout: {top: 50, left: 0, right: 0, height: 49 },
        classNames: ['data-item'],
        childViews: 'label field'.w(),

        label: SC.LabelView.design({
          layout: { top: 15, left: 10, width: 200, height: 30 },
          value: '_configureRepositoryInfoDetailView.DisplayName',
          localize: YES
        }),

        field: SC.TextFieldView.design({
          layout: { top: 10, left: 210, width: 200, height: 30 },
          valueBinding: 'Chililog.configureRepositoryInfoDetailViewController.displayName',
          maxLength: 100
        })
      }),

      description: SC.View.design({
        layout: {top: 100, left: 0, right: 0, height: 99 },
        classNames: ['data-item'],
        childViews: 'label field'.w(),

        label: SC.LabelView.design({
          layout: { top: 15, left: 10, width: 200, height: 30 },
          value: '_configureRepositoryInfoDetailView.Description',
          localize: YES
        }),

        field: SC.TextFieldView.design({
          layout: { top: 10, left: 210, width: 500, height: 70 },
          isTextArea: YES,
          valueBinding: 'Chililog.configureRepositoryInfoDetailViewController.description'
        })
      }),

      currentStatus: SC.View.design({
        layout: {top: 200, left: 0, right: 0, height: 49 },
        classNames: ['data-item'],
        childViews: 'label label2'.w(),

        label: SC.LabelView.design({
          layout: { top: 15, left: 10, width: 200, height: 30 },
          value: '_configureRepositoryInfoDetailView.CurrentStatus',
          localize: YES
        }),

        label2: SC.LabelView.design({
          layout: { top: 10, left: 210, width: 300, height: 30 },
          classNames: ['readonly'],
          valueBinding: 'Chililog.configureRepositoryInfoDetailViewController.currentStatus'
        })
      }),

      startupStatus: SC.View.design({
        layout: {top: 250, left: 0, right: 0, height: 69 },
        classNames: ['data-item'],
        childViews: 'label field'.w(),

        label: SC.LabelView.design({
          layout: { top: 15, left: 10, width: 200, height: 30 },
          value: '_configureRepositoryInfoDetailView.StartupStatus',
          localize: YES
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
      }),

      writeQueueAttributes: SC.View.design({
        layout: {top: 320, left: 0, right: 0, height: 609 },
        classNames: ['data-group'],
        childViews: ('writeDivider writeQueueAddress writeQueueUsername writeQueuePassword writeQueueDurable ' +
          'maxKeywords writeQueueWorkerCount writeQueueMaxMemory writeQueueMaxMemoryPolicy writeQueuePageSize ' +
          'writeQueuePageCountCache').w(),

        writeDivider: SC.LabelView.design({
          layout: { top: 0, left: 0, right: 0, height: 49 },
          classNames: ['data-divider'],
          value: '_configureRepositoryInfoDetailView.WriteDivider',
          localize: YES
        }),

        writeQueueAddress: SC.View.design({
          layout: {top: 50, left: 0, right: 0, height: 49 },
          classNames: ['data-item'],
          childViews: 'label label2'.w(),

          label: SC.LabelView.design({
            layout: { top: 15, left: 10, width: 200, height: 30 },
            value: '_configureRepositoryInfoDetailView.WriteQueueAddress',
            localize: YES
          }),

          label2: SC.LabelView.design({
            layout: { top: 10, left: 210, right: 10, height: 30 },
            classNames: ['readonly'],
            valueBinding: 'Chililog.configureRepositoryInfoDetailViewController.writeQueueAddress'
          })
        }),

        writeQueueUsername: SC.View.design({
          layout: {top: 100, left: 0, right: 0, height: 49 },
          classNames: ['data-item'],
          childViews: 'label label2'.w(),

          label: SC.LabelView.design({
            layout: { top: 15, left: 10, width: 200, height: 30 },
            value: '_configureRepositoryInfoDetailView.WriteQueueUsername',
            localize: YES
          }),

          label2: SC.LabelView.design({
            layout: { top: 10, left: 210, right: 10, height: 30 },
            classNames: ['readonly'],
            valueBinding: 'Chililog.configureRepositoryInfoDetailViewController.name'
          })
        }),

        writeQueuePassword: SC.View.design({
          layout: {top: 150, left: 0, right: 0, height: 49 },
          classNames: ['data-item'],
          childViews: 'label field'.w(),

          label: SC.LabelView.design({
            layout: { top: 15, left: 10, width: 200, height: 30 },
            value: '_configureRepositoryInfoDetailView.WriteQueuePassword',
            localize: YES
          }),

          field: SC.TextFieldView.design({
            layout: { top: 10, left: 210, width: 200, height: 30 },
            valueBinding: 'Chililog.configureRepositoryInfoDetailViewController.writeQueuePassword',
            maxLength: 100
          })
        }),

        writeQueueDurable: SC.View.design({
          layout: {top: 200, left: 0, right: 0, height: 69 },
          classNames: ['data-item'],
          childViews: 'label field'.w(),

          label: SC.LabelView.design({
            layout: { top: 15, left: 10, width: 200, height: 30 },
            value: '_configureRepositoryInfoDetailView.WriteQueueDurable',
            localize: YES
          }),

          field: Chililog.RadioView.design({
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
          layout: {top: 270, left: 0, right: 0, height: 49 },
          classNames: ['data-item'],
          childViews: 'label field help'.w(),

          label: SC.LabelView.design({
            layout: { top: 15, left: 10, width: 200, height: 30 },
            value: '_configureRepositoryInfoDetailView.MaxKeywords',
            localize: YES
          }),

          field: SC.TextFieldView.design({
            layout: { top: 10, left: 210, width: 50, height: 30 },
            valueBinding: 'Chililog.configureRepositoryInfoDetailViewController.maxKeywords',
            maxLength: 3,
            validator: Chililog.PositiveIntegerValidator.extend({ formatNumber: YES })
          }),

          help: SC.LabelView.design({
            layout: { top: 17, left: 270, right: 10, height: 30 },
            classNames: ['help'],
            value: '_configureRepositoryInfoDetailView.MaxKeywordsHelp',
            localize: YES
          })
        }),

        writeQueueWorkerCount: SC.View.design({
          layout: {top: 320, left: 0, right: 0, height: 49 },
          classNames: ['data-item'],
          childViews: 'label field help'.w(),

          label: SC.LabelView.design({
            layout: { top: 15, left: 10, width: 200, height: 30 },
            value: '_configureRepositoryInfoDetailView.WriteQueueWorkerCount',
            localize: YES
          }),

          field: SC.TextFieldView.design({
            layout: { top: 10, left: 210, width: 50, height: 30 },
            valueBinding: 'Chililog.configureRepositoryInfoDetailViewController.writeQueueWorkerCount',
            maxLength: 2,
            validator: Chililog.PositiveIntegerValidator.extend({ formatNumber: YES })
          }),

          help: SC.LabelView.design({
            layout: { top: 17, left: 270, right: 10, height: 30 },
            classNames: ['help'],
            value: '_configureRepositoryInfoDetailView.WriteQueueWorkerCountHelp',
            localize: YES
          })
        }),

        writeQueueMaxMemory: SC.View.design({
          layout: {top: 370, left: 0, right: 0, height: 49 },
          classNames: ['data-item'],
          childViews: 'label field help'.w(),

          label: SC.LabelView.design({
            layout: { top: 15, left: 10, width: 200, height: 30 },
            value: '_configureRepositoryInfoDetailView.WriteQueueMaxMemory',
            localize: YES
          }),

          field: SC.TextFieldView.design({
            layout: { top: 10, left: 210, width: 100, height: 30 },
            valueBinding: 'Chililog.configureRepositoryInfoDetailViewController.writeQueueMaxMemory',
            maxLength: 10,
            validator: Chililog.PositiveIntegerValidator.extend({ formatNumber: YES })
          }),

          help: SC.LabelView.design({
            layout: { top: 17, left: 320, right: 10, height: 30 },
            classNames: ['help'],
            value: '_configureRepositoryInfoDetailView.WriteQueueMaxMemoryHelp',
            localize: YES
          })
        }),

        writeQueueMaxMemoryPolicy: SC.View.design({
          layout: {top: 420, left: 0, right: 0, height: 89 },
          classNames: ['data-item'],
          childViews: 'label field'.w(),

          label: SC.LabelView.design({
            layout: { top: 15, left: 10, width: 200, height: 30 },
            value: '_configureRepositoryInfoDetailView.WriteQueueMaxMemoryPolicy',
            localize: YES
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
          layout: {top: 510, left: 0, right: 0, height: 49 },
          classNames: ['data-item'],
          childViews: 'label field help'.w(),

          label: SC.LabelView.design({
            layout: { top: 15, left: 10, width: 200, height: 30 },
            value: '_configureRepositoryInfoDetailView.WriteQueuePageSize',
            localize: YES
          }),

          field: SC.TextFieldView.design({
            layout: { top: 10, left: 210, width: 100, height: 30 },
            valueBinding: 'Chililog.configureRepositoryInfoDetailViewController.writeQueuePageSize',
            validator: Chililog.PositiveIntegerValidator.extend({ formatNumber: YES })
          }),

          help: SC.LabelView.design({
            layout: { top: 17, left: 320, right: 10, height: 30 },
            classNames: ['help'],
            value: '_configureRepositoryInfoDetailView.WriteQueuePageSizeHelp',
            localize: YES
          })
        }),

        writeQueuePageCountCache: SC.View.design({
          layout: {top: 560, left: 0, right: 0, height: 49 },
          classNames: ['data-item'],
          childViews: 'label field help'.w(),

          label: SC.LabelView.design({
            layout: { top: 15, left: 10, width: 200, height: 30 },
            value: '_configureRepositoryInfoDetailView.WriteQueuePageCountCache',
            localize: YES
          }),

          field: SC.TextFieldView.design({
            layout: { top: 10, left: 210, width: 50, height: 30 },
            valueBinding: 'Chililog.configureRepositoryInfoDetailViewController.writeQueuePageCountCache',
            validator: Chililog.PositiveIntegerValidator.extend({ formatNumber: YES })
          }),

          help: SC.LabelView.design({
            layout: { top: 17, left: 270, right: 10, height: 30 },
            classNames: ['help'],
            value: '_configureRepositoryInfoDetailView.WriteQueuePageCountCacheHelp',
            localize: YES
          })
        })
      }),

      readQueueAttributes: SC.View.design({
        layout: {top: 930, left: 0, right: 0, height: 269 },
        childViews: 'readDivider readQueueAddress readQueueUsername readQueuePassword readQueueDurable'.w(),

        readDivider: SC.LabelView.design({
          layout: { top: 0, left: 0, right: 0, height: 49 },
          classNames: ['data-divider'],
          value: '_configureRepositoryInfoDetailView.ReadDivider',
          localize: YES
        }),

        readQueueAddress: SC.View.design({
          layout: {top: 50, left: 0, right: 0, height: 49 },
          classNames: ['data-item'],
          childViews: 'label label2'.w(),

          label: SC.LabelView.design({
            layout: { top: 15, left: 10, width: 200, height: 30 },
            value: '_configureRepositoryInfoDetailView.ReadQueueAddress',
            localize: YES
          }),

          label2: SC.LabelView.design({
            layout: { top: 10, left: 210, width: 300, height: 30 },
            classNames: ['readonly'],
            valueBinding: 'Chililog.configureRepositoryInfoDetailViewController.readQueueAddress'
          })
        }),

        readQueueUsername: SC.View.design({
          layout: {top: 100, left: 0, right: 0, height: 49 },
          classNames: ['data-item'],
          childViews: 'label label2'.w(),

          label: SC.LabelView.design({
            layout: { top: 15, left: 10, width: 200, height: 30 },
            value: '_configureRepositoryInfoDetailView.ReadQueueUsername',
            localize: YES
          }),

          label2: SC.LabelView.design({
            layout: { top: 10, left: 210, width: 300, height: 30 },
            classNames: ['readonly'],
            valueBinding: 'Chililog.configureRepositoryInfoDetailViewController.name'
          })
        }),

        readQueuePassword: SC.View.design({
          layout: {top: 150, left: 0, right: 0, height: 49 },
          classNames: ['data-item'],
          childViews: 'label field'.w(),

          label: SC.LabelView.design({
            layout: { top: 15, left: 10, width: 200, height: 30 },
            value: '_configureRepositoryInfoDetailView.ReadQueuePassword',
            localize: YES
          }),

          field: SC.TextFieldView.design({
            layout: { top: 10, left: 210, width: 200, height: 30 },
            valueBinding: 'Chililog.configureRepositoryInfoDetailViewController.readQueuePassword'
          })
        }),

        readQueueDurable: SC.View.design({
          layout: {top: 200, left: 0, right: 0, height: 68 },
          classNames: ['data-item'],
          childViews: 'label field'.w(),

          label: SC.LabelView.design({
            layout: { top: 15, left: 10, width: 200, height: 30 },
            value: '_configureRepositoryInfoDetailView.ReadQueueDurable',
            localize: YES
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
      }),

      buttons: SC.View.design({
        layout: {top: 1200, left: 0, right: 0, height: 50 },
        childViews: 'saveButton cancelButton savingImage successMessage'.w(),

        saveButton: SC.ButtonView.design({
          layout: {top: 10, left: 10, width: 90 },
          title: '_save',
          localize: YES,
          controlSize: SC.HUGE_CONTROL_SIZE,
          isDefault: YES,
          isEnabledBinding: SC.Binding.from('Chililog.configureRepositoryInfoDetailViewController.canSave').oneWay(),
          target: 'Chililog.configureRepositoryInfoDetailViewController',
          action: 'save'
        }),

        cancelButton: SC.ButtonView.design({
          layout: {top: 10, left: 110, width: 90 },
          title: '_cancel',
          localize: YES,
          controlSize: SC.HUGE_CONTROL_SIZE,
          isEnabledBinding: SC.Binding.from('Chililog.configureRepositoryInfoDetailViewController.canSave').oneWay(),
          target: 'Chililog.configureRepositoryInfoDetailViewController',
          action: 'discardChanges'
        }),

        savingImage: Chililog.ImageView.design({
          layout: { top: 15, left: 210, width: 16, height: 16 },
          value: sc_static('images/working'),
          isVisibleBinding: SC.Binding.from('Chililog.configureRepositoryInfoDetailViewController.isSaving').oneWay().bool(),
          useImageQueue: NO
        }),

        successMessage: SC.LabelView.design({
          layout: { top: 10, left: 210, width: 200, height: 25, opacity: 0 },
          classNames: ['success'],
          value: '_saveSuccess',
          localize: YES
        })
      })

    })
  })

});

/**
 * Instance configure repository view
 */
Chililog.configureRepositoryInfoDetailView = Chililog.ConfigureRepositoryInfoDetailView.create();
