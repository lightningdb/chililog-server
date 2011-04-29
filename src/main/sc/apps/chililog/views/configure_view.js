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
    classNames: ['list-box'],

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

  right: SC.ContainerView.design({
    layout: { top: 35, bottom: 0, left: 208, right: 0 },
    classNames: ['edit-box']
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
 * User Scene
 */
Chililog.ConfigureUserSceneView = SC.SceneView.design({
  layout: { top: 0, left: 0, bottom: 0, right: 0 },
  scenes: ['Chililog.configureUserListView', 'Chililog.configureUserDetailView']
});

Chililog.configureUserSceneView = Chililog.ConfigureUserSceneView.create();

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
    localize: YES
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
    classNames: ['table-box'],
    contentBinding: 'Chililog.configureUserListViewController.arrangedObjects',
    selectionBinding: 'Chililog.configureUserListViewController.selection',
    useHeaders: YES,
    isEditable: NO,
    columns:[
      SC.TableColumn.create({
        key:   'username',
        title: '_configureUserView.Username'.loc(),
        width: 150,
        isReorderable: NO   //Bug with reorder when switching with other configure options
      }),
      SC.TableColumn.create({
        key:   'emailAddress',
        title: '_configureUserView.EmailAddress'.loc(),
        width: 250,
        isReorderable: NO
      }),
      SC.TableColumn.create({
        key:   'displayName',
        title: '_configureUserView.DisplayName'.loc(),
        width: 250,
        isReorderable: NO
      }),
      SC.TableColumn.create({
        key:   'currentStatus',
        title: '_configureUserView.CurrentStatus'.loc(),
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
  layout: { top: 10, left: 10, bottom: 10, right: 10 },
  childViews: 'title deleteButton body'.w(),

  title: SC.LabelView.design({
    layout: { top: 0, left: 0, width: 200, height: 30 },
    tagName: 'h1',
    controlSize: SC.HUGE_CONTROL_SIZE,
    value: '_configureUserView.Title',
    localize: YES
  }),

  deleteButton: SC.ButtonView.design({
    layout: {top: 0, left: 360, width: 40 },
    title: 'D',
    localize: YES,
    controlSize: SC.HUGE_CONTROL_SIZE,
    isVisibleBinding: SC.Binding.from('Chililog.configureUserViewController.canSave').oneWay().not(),
    target: Chililog.configureUserViewController,
    action: 'confirmErase'
  }),

  body: SC.View.design({
    layoutBinding: 'Chililog.configureUserViewController.bodyLayout',
    classNames: ['edit-box'],
    childViews: 'username emailAddress displayName currentStatus passwords buttons'.w(),

    username: SC.View.design({
      layout: {top: 20, left: 20, right: 20, height: 50 },
      childViews: 'label field'.w(),

      label: SC.LabelView.design({
        layout: { top: 0, left: 0, right: 0, height: 19 },
        value: '_configureUserView.Username',
        localize: YES
      }),

      field: SC.TextFieldView.design({
        layout: { top: 20, left: 0, height: 25 },
        valueBinding: 'Chililog.configureUserViewController.username'
      })
    }),

    emailAddress: SC.View.design({
      layout: {top: 80, left: 20, right: 20, height: 50 },
      childViews: 'label field'.w(),

      label: SC.LabelView.design({
        layout: { top: 0, left: 0, right: 0, height: 19 },
        value: '_configureUserView.EmailAddress',
        localize: YES
      }),

      field: SC.TextFieldView.design({
        layout: { top: 20, left: 0, height: 25 },
        valueBinding: 'Chililog.configureUserViewController.emailAddress'
      })
    }),

    displayName: SC.View.design({
      layout: {top: 140, left: 20, right: 20, height: 50 },
      childViews: 'label field'.w(),

      label: SC.LabelView.design({
        layout: { top: 0, left: 0, right: 0, height: 19 },
        value: '_configureUserView.DisplayName',
        localize: YES
      }),

      field: SC.TextFieldView.design({
        layout: { top: 20, left: 0, height: 25 },
        valueBinding: 'Chililog.configureUserViewController.displayName'
      })
    }),

    currentStatus: SC.View.design({
      layout: {top: 200, left: 20, right: 20, height: 50 },
      childViews: 'label field'.w(),

      label: SC.LabelView.design({
        layout: { top: 0, left: 0, right: 0, height: 19 },
        value: '_configureUserView.CurrentStatus',
        localize: YES
      }),

      field: SC.SelectFieldView.design({
        layout: { top: 20, left: 0, height: 25 },
        objects: [
          { name: 'Enabled', value: 'Enabled'},
          { name: 'Disabled', value: 'Disabled'},
          { name: 'Locked', value: 'Locked'}
        ],
        nameKey: 'name',
        valueKey: 'value',
        disableSort: YES,
        valueBinding: 'Chililog.configureUserViewController.currentStatus'
      })
    }),

    passwords: SC.View.design({
      layout: {top: 260, left: 20, right: 20, height: 120 },
      childViews: 'password confirmPassword'.w(),
      isVisibleBinding: SC.Binding.from('Chililog.configureUserViewController.isCreating').oneWay(),

      password: SC.View.design({
        layout: {top: 0, left: 0, right: 0, height: 50 },
        childViews: 'label field'.w(),

        label: SC.LabelView.design({
          layout: { top: 0, left: 0, right: 0, height: 19 },
          value: '_configureUserView.Password',
          localize: YES
        }),

        field: SC.TextFieldView.design({
          layout: { top: 20, left: 0, height: 25 },
          isPassword: YES,
          valueBinding: 'Chililog.configureUserViewController.password'
        })
      }),

      confirmPassword: SC.View.design({
        layout: {top: 60, left: 0, right: 0, height: 50 },
        childViews: 'label field'.w(),

        label: SC.LabelView.design({
          layout: { top: 0, left: 0, right: 0, height: 19 },
          value: '_configureUserView.ConfirmPassword',
          localize: YES
        }),

        field: SC.TextFieldView.design({
          layout: { top: 20, left: 0, height: 25 },
          isPassword: YES,
          valueBinding: 'Chililog.configureUserViewController.confirmPassword'
        })
      })
    }),

    buttons: SC.View.design({
      layoutBinding: 'Chililog.configureUserViewController.buttonsLayout',
      childViews: 'saveButton cancelButton savingImage successMessage'.w(),

      saveButton: SC.ButtonView.design({
        layout: {top: 0, left: 0, width: 90 },
        title: '_save',
        localize: YES,
        controlSize: SC.HUGE_CONTROL_SIZE,
        isDefault: YES,
        isEnabledBinding: SC.Binding.from('Chililog.configureUserViewController.canSave').oneWay(),
        target: 'Chililog.configureUserViewController',
        action: 'save'
      }),

      cancelButton: SC.ButtonView.design({
        layout: {top: 0, left: 100, width: 90 },
        title: '_cancel',
        localize: YES,
        controlSize: SC.HUGE_CONTROL_SIZE,
        isEnabledBinding: SC.Binding.from('Chililog.configureUserViewController.canSave').oneWay(),
        target: 'Chililog.configureUserViewController',
        action: 'discardChanges'
      }),

      savingImage: Chililog.ImageView.design({
        layout: { top: 5, left: 200, width: 16, height: 16 },
        value: sc_static('images/working'),
        isVisibleBinding: SC.Binding.from('Chililog.configureUserViewController.isSaving').oneWay().bool(),
        useImageQueue: NO
      }),

      successMessage: SC.LabelView.design({
        layout: { top: 0, left: 200, width: 155, height: 25, opacity: 0 },
        classNames: ['success'],
        value: '_myAccountView.SaveProfileSuccess',
        localize: YES
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
 * Repositories Scene
 */
Chililog.ConfigureRepositoryInfoSceneView = SC.SceneView.design({
  layout: { top: 0, left: 0, bottom: 0, right: 0 },
  scenes: ['Chililog.configureRepositoryInfoListView', 'Chililog.configureRepositoryInfoView']
});

Chililog.configureRepositoryInfoSceneView = Chililog.ConfigureRepositoryInfoSceneView.create();


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
    classNames: ['edit-box'],
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
