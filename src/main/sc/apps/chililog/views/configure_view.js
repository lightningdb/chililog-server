// ==========================================================================
// Project:   Chililog - mainPage
// Copyright: ©2011 My Company, Inc.
// ==========================================================================

sc_require('views/image_view');

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
    layoutBinding: 'Chililog.configureUserViewController.bodyLayout',
    classNames: ['edit-box'],
    childViews: 'username email displayName currentStatus passwords buttons'.w(),

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

    email: SC.View.design({
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
      layout: {top: 260, left: 20, right: 20, height: 50 },
      childViews: 'password confirmPassword'.w(),
      isVisibleBinding: SC.Binding.from('Chililog.configureUserViewController.isAdding').oneWay(),

      password: SC.View.design({
        layout: {top: 20, left: 20, right: 20, height: 50 },
        childViews: 'label field'.w(),

        label: SC.LabelView.design({
          layout: { top: 0, left: 0, right: 0, height: 19 },
          value: '_configureUserView.Username',
          localize: YES
        }),

        field: SC.TextFieldView.design({
          layout: { top: 20, left: 0, height: 25 },
          isPassword: YES,
          valueBinding: 'Chililog.configureUserViewController.password'
        })
      }),
      
      confirmPassword: SC.View.design({
        layout: {top: 20, left: 20, right: 20, height: 50 },
        childViews: 'label field'.w(),

        label: SC.LabelView.design({
          layout: { top: 0, left: 0, right: 0, height: 19 },
          value: '_configureUserView.Username',
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
Chililog.configureUserView = Chililog.ConfigureUserView.create();

/**
 * Repository details
 */
Chililog.ConfigureRepositoryInfoView = SC.View.design({
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
Chililog.configureRepositoryInfoView = Chililog.ConfigureRepositoryInfoView.create();
