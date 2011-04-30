// ==========================================================================
// Project:   Chililog - mainPage
// Copyright: ©2011 My Company, Inc.
// ==========================================================================

sc_require('views/image_view');

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
  childViews: 'title createButton moreButton table'.w(),

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
        isReorderable: NO
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
    target: Chililog.configureUserListViewController,
    action: 'show'
  }),
  
  deleteButton: SC.ButtonView.design({
    layout: {top: 40, left: 100, width: 80 },
    title: '_delete',
    localize: YES,
    isVisibleBinding: SC.Binding.from('Chililog.configureUserDetailViewController.canSave').oneWay().not(),
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
          valueBinding: 'Chililog.configureUserDetailViewController.username'
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
          valueBinding: 'Chililog.configureUserDetailViewController.emailAddress'
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
          value: '(Optional name or nickname to display instead of the username)',
          localize: YES
        }),

        field: SC.TextFieldView.design({
          layout: { top: 10, left: 210, width: 300, height: 30 },
          valueBinding: 'Chililog.configureUserDetailViewController.displayName'
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

        field: SC.RadioView.design({
          layout: { top: 15, left: 210, width: 500, height: 80 },
          items: [
            { title: 'Enabled. <span class="help">User can login.</span>', value: 'Enabled'},
            { title: 'Disabled. <span class="help">User cannot login.</span>', value: 'Disabled'},
            { title: 'Locked. <span class="help">User failed to login too many times. Password must be reset.</span>', value: 'Locked'}
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
          valueBinding: 'Chililog.configureUserDetailViewController.password'
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
          valueBinding: 'Chililog.configureUserDetailViewController.confirmPassword'
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
  childViews: 'title createButton moreButton'.w(),

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
    localize: YES
  }),

  moreButton: SC.PopupButtonView.design({
    layout: { top: 40, left: 190, width: 130, height: 30 },
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
  })
});

Chililog.configureRepositoryInfoListView = Chililog.ConfigureRepositoryInfoListView.create();


/**
 * Repository details
 */
Chililog.ConfigureRepositoryInfoDetailView = SC.View.design({
  layout: { top: 10, left: 10, bottom: 10, right: 10 },
  childViews: 'title body'.w(),

  title: SC.LabelView.design({
    layout: { top: 0, left: 0, width: 200, height: 30 },
    tagName: 'h1',
    controlSize: SC.HUGE_CONTROL_SIZE,
    value: '_configureRepositoryInfoView.Title',
    localize: YES
  }),

  body: SC.View.design({
    layout: { top: 35, left: 0, width: 400, height: 300 },
    classNames: ['box'],
    childViews: 'name'.w(),

    name: SC.View.design({
      layout: {top: 50, left: 20, right: 20, height: 50 },
      childViews: 'label field'.w(),

      label: SC.LabelView.design({
        layout: { top: 0, left: 0, right: 0, height: 19 },
        value: '_configureRepositoryInfoView.Name',
        localize: YES
      }),

      field: SC.TextFieldView.design({
        layout: { top: 20, left: 0, height: 25 },
        valueBinding: 'Chililog.configureRepositoryInfoViewController.name'
      })
    })
  })
});

/**
 * Instance configure repository view
 */
Chililog.configureRepositoryInfoDetailView = Chililog.ConfigureRepositoryInfoDetailView.create();
