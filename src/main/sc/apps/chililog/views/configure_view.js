// ==========================================================================
// Project:   Chililog - mainPage
// Copyright: ©2011 My Company, Inc.
// ==========================================================================

sc_require('views/image_view');
sc_require('views/radio_view');
sc_require('views/validators');
sc_require('views/label_mixin');

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
  childViews: 'title createButton refreshButton table'.w(),

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

  refreshButton: SC.ButtonView.design({
    layout: { top: 40, left: 150, width: 130 },
    title: '_refresh',
    localize: YES,
    isEnabledBinding: SC.Binding.from('Chililog.configureUserListViewController.isRefreshing').oneWay().not(),
    target: Chililog.configureUserListViewController,
    action: 'refresh'
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
  childViews: 'title backButton deleteButton successMessage body'.w(),

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

  successMessage: SC.LabelView.design({
    layout: { top: 40, centerX: 0, width: 200, height: 25, opacity: 0 },
    classNames: ['success'],
    value: '_saveSuccess',
    localize: YES
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

        label: SC.LabelView.design(Chililog.RequiredFieldLabelMixin, {
          layout: { top: 15, left: 10, width: 200, height: 30 },
          value: '_configureUserDetailView.Username'.loc()
        }),

        field: SC.TextFieldView.design({
          layout: { top: 10, left: 210, width: 300, height: 30 },
          valueBinding: 'Chililog.configureUserDetailViewController.username',
          maxLength: 100,
          validator: Chililog.RegExpValidator.extend({
            keyDownRegExp: /[A-Za-z0-9_\-\.'@]/,
            fieldRegExp: /^[A-Za-z0-9_\-\.'@]+$/,
            invalidFieldErrorMessage: '_configureUserDetailView.Username.Invalid',
            requiredFieldErrorMessage: '_configureUserDetailView.Username.Required'
          })
        })
      }),

      emailAddress: SC.View.design({
        layout: {top: 50, left: 0, right: 0, height: 50 },
        classNames: ['data-item'],
        childViews: 'label field'.w(),

        label: SC.LabelView.design(Chililog.RequiredFieldLabelMixin, {
          layout: { top: 15, left: 10, width: 200, height: 30 },
          value: '_configureUserDetailView.EmailAddress'.loc()
        }),

        field: SC.TextFieldView.design({
          layout: { top: 10, left: 210, width: 300, height: 30 },
          valueBinding: 'Chililog.configureUserDetailViewController.emailAddress',
          maxLength: 200,
          validator: Chililog.EmailAddressValidator.extend({
            invalidFieldErrorMessage: '_configureUserDetailView.EmailAddress.Invalid',
            requiredFieldErrorMessage: '_configureUserDetailView.EmailAddress.Required'
          })
        })
      }),

      displayName: SC.View.design({
        layout: {top: 100, left: 0, right: 0, height: 80 },
        classNames: ['data-item'],
        childViews: 'label help field'.w(),

        label: SC.LabelView.design({
          layout: { top: 15, left: 10, width: 200, height: 20 },
          value: '_configureUserDetailView.DisplayName'.loc()
        }),

        help: SC.LabelView.design({
          classNames: ['help'],
          layout: { top: 35, left: 10, width: 170, height: 50 },
          value: '_configureUserDetailView.DisplayName.Help'.loc()
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

        label: SC.LabelView.design(Chililog.RequiredFieldLabelMixin, {
          layout: { top: 15, left: 10, width: 200, height: 30 },
          value: '_configureUserDetailView.CurrentStatus'.loc()
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

        label: SC.LabelView.design(Chililog.RequiredFieldLabelMixin, {
          layout: { top: 15, left: 10, width: 200, height: 30 },
          value: '_configureUserDetailView.Password'.loc()
        }),

        field: SC.TextFieldView.design({
          layout: { top: 10, left: 210, width: 300, height: 30 },
          isPassword: YES,
          valueBinding: 'Chililog.configureUserDetailViewController.password',
          maxLength: 100,
          validator: Chililog.RegExpValidator.extend({
            fieldRegExp: /^(?=.{8,}$)(?=(?:.*?\d){1})(?=(?:.*?[A-Za-z]){1})(?=(?:.*?\W){1})/,
            invalidFieldErrorMessage: '_configureUserDetailView.Password.Invalid',
            requiredFieldErrorMessage: '_configureUserDetailView.Password.Required'
          })
        })
      }),

      confirmPassword: SC.View.design({
        layout: {top: 320, left: 0, right: 0, height: 50 },
        classNames: ['data-item'],
        childViews: 'label field'.w(),
        isVisibleBinding: SC.Binding.from('Chililog.configureUserDetailViewController.isCreating').oneWay(),

        label: SC.LabelView.design(Chililog.RequiredFieldLabelMixin, {
          layout: { top: 15, left: 10, width: 200, height: 30 },
          value: '_configureUserDetailView.ConfirmPassword'.loc()
        }),

        field: SC.TextFieldView.design({
          layout: { top: 10, left: 210, width: 300, height: 30 },
          isPassword: YES,
          valueBinding: 'Chililog.configureUserDetailViewController.confirmPassword',
          maxLength: 100,
          validator: Chililog.NotEmptyValidator.extend({
            requiredFieldErrorMessage: '_configureUserDetailView.ConfirmPassword.Required'
          })
        })
      }),

      buttons: SC.View.design({
        layoutBinding: 'Chililog.configureUserDetailViewController.buttonsLayout',
        childViews: 'saveButton cancelButton savingImage'.w(),

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
  childViews: 'title createButton refreshButton table'.w(),

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

  refreshButton: SC.ButtonView.design({
    layout: { top: 40, left: 190, width: 130 },
    title: '_refresh',
    localize: YES,
    isEnabledBinding: SC.Binding.from('Chililog.configureRepositoryInfoListViewController.isRefreshing').oneWay().not(),
    target: Chililog.configureRepositoryInfoListViewController,
    action: 'refresh'
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
  childViews: 'title backButton deleteButton successMessage body'.w(),

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

  successMessage: SC.LabelView.design({
    layout: { top: 40, centerX: 0, width: 200, height: 25, opacity: 0 },
    classNames: ['success'],
    value: '_saveSuccess',
    localize: YES
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
        layout: {top: 50, left: 0, right: 0, height: 49 },
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
        layout: {top: 100, left: 0, right: 0, height: 99 },
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
        layout: {top: 200, left: 0, right: 0, height: 49 },
        classNames: ['data-item'],
        childViews: 'label label2'.w(),

        label: SC.LabelView.design({
          layout: { top: 15, left: 10, width: 200, height: 30 },
          value: '_configureRepositoryInfoDetailView.CurrentStatus'.loc()
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
          value: '_configureRepositoryInfoDetailView.WriteDivider'.loc()
        }),

        writeQueueAddress: SC.View.design({
          layout: {top: 50, left: 0, right: 0, height: 49 },
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
          layout: {top: 100, left: 0, right: 0, height: 49 },
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
          layout: {top: 150, left: 0, right: 0, height: 49 },
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
          layout: {top: 200, left: 0, right: 0, height: 69 },
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
          layout: {top: 270, left: 0, right: 0, height: 49 },
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
          layout: {top: 320, left: 0, right: 0, height: 49 },
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
          layout: {top: 370, left: 0, right: 0, height: 49 },
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
          layout: {top: 420, left: 0, right: 0, height: 89 },
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
          layout: {top: 510, left: 0, right: 0, height: 49 },
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
          layout: {top: 560, left: 0, right: 0, height: 49 },
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
            value: '_configureRepositoryInfoDetailView.ReadQueueAddress'.loc()
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
            value: '_configureRepositoryInfoDetailView.ReadQueueUsername'.loc()
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
            value: '_configureRepositoryInfoDetailView.ReadQueuePassword'.loc()
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
      }),

      buttons: SC.View.design({
        layout: {top: 1200, left: 0, right: 0, height: 50 },
        childViews: 'saveButton cancelButton savingImage'.w(),

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
        })
      })

    })
  })

});

/**
 * Instance configure repository view
 */
Chililog.configureRepositoryInfoDetailView = Chililog.ConfigureRepositoryInfoDetailView.create();
