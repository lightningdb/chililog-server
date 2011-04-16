// ==========================================================================
// Project:   Chililog - mainPage
// Copyright: ©2011 My Company, Inc.
// ==========================================================================

/**
 * My profile view
 */

Chililog.myAccountView = SC.View.design({
  layout: { top: 10, left: 10, bottom: 10, right: 10 },
  childViews: 'title myProfile changePassword'.w(),

  title: SC.LabelView.design({
    layout: { top: 0, left: 0, width: 200, height: 30 },
    tagName: 'h1',
    controlSize: SC.HUGE_CONTROL_SIZE,
    value: '_myAccountView.Title',
    localize: YES
  }),

  myProfile: SC.View.design({
    layout: { top: 35, left: 0, width: 400, height: 265 },
    classNames: ['edit-box'],
    childViews: 'title username email displayName saveButton cancelButton'.w(),

    title: SC.LabelView.design({
      layout: {top: 20, left: 20, right: 20, height: 30 },
      tagName: 'h1',
      controlSize: SC.LARGE_CONTROL_SIZE,
      value: '_myAccountView.MyProfile',
      localize: YES
    }),

    username: SC.View.design({
      layout: {top: 50, left: 20, right: 20, height: 50 },
      childViews: 'label field'.w(),

      label: SC.LabelView.design({
        layout: { top: 0, left: 0, right: 0, height: 19 },
        value: '_myAccountView.Username',
        localize: YES
      }),

      field: SC.TextFieldView.design({
        layout: { top: 20, left: 0, height: 25 },
        valueBinding: 'Chililog.myAccountViewController.username'
      })
    }),

    email: SC.View.design({
      layout: {top: 100, left: 20, right: 20, height: 50 },
      childViews: 'label field'.w(),

      label: SC.LabelView.design({
        layout: { top: 0, left: 0, right: 0, height: 19 },
        value: '_myAccountView.EmailAddress',
        localize: YES
      }),

      field: SC.TextFieldView.design({
        layout: { top: 20, left: 0, height: 25 },
        valueBinding: 'Chililog.myAccountViewController.emailAddress'
      })
    }),

    displayName: SC.View.design({
      layout: {top: 150, left: 20, right: 20, height: 50 },
      childViews: 'label field'.w(),

      label: SC.LabelView.design({
        layout: { top: 0, left: 0, right: 0, height: 19 },
        value: '_myAccountView.DisplayName',
        localize: YES
      }),

      field: SC.TextFieldView.design({
        layout: { top: 20, left: 0, height: 25 },
        valueBinding: 'Chililog.myAccountViewController.displayName'
      })
    }),

    saveButton: SC.ButtonView.design({
      layout: {top: 210, left: 20, width: 100 },
      title: '_save',
      localize: YES,
      controlSize: SC.HUGE_CONTROL_SIZE,
      isDefault: YES,
      target: 'Chililog.myAccountViewController',
      action: 'save'
    }),
    
    cancelButton: SC.ButtonView.design({
      layout: {top: 210, left: 130, width: 100 },
      title: '_cancel',
      localize: YES,
      controlSize: SC.HUGE_CONTROL_SIZE
    })
  }),  //myProfile

  changePassword: SC.View.design({
    layout: { top: 35, left: 410, width: 400, height: 265 },
    classNames: ['edit-box'],
    childViews: 'title oldPassword newPassword confirmNewPassword changePasswordButton'.w(),

    title: SC.LabelView.design({
      layout: {top: 20, left: 20, right: 20, height: 30 },
      tagName: 'h1',
      controlSize: SC.LARGE_CONTROL_SIZE,
      value: '_myAccountView.ChangePassword',
      localize: YES
    }),

    oldPassword: SC.View.design({
      layout: {top: 50, left: 20, right: 20, height: 50 },
      childViews: 'label field'.w(),

      label: SC.LabelView.design({
        layout: { top: 0, left: 0, right: 0, height: 19 },
        value: '_myAccountView.OldPassword',
        localize: YES
      }),

      field: SC.TextFieldView.design({
        layout: { top: 20, left: 0, height: 25 }
      })
    }),

    newPassword: SC.View.design({
      layout: {top: 100, left: 20, right: 20, height: 50 },
      childViews: 'label field'.w(),

      label: SC.LabelView.design({
        layout: { top: 0, left: 0, right: 0, height: 19 },
        value: '_myAccountView.NewPassword',
        localize: YES
      }),

      field: SC.TextFieldView.design({
        layout: { top: 20, left: 0, height: 25 }
      })
    }),

    confirmNewPassword: SC.View.design({
      layout: {top: 150, left: 20, right: 20, height: 50 },
      childViews: 'label field'.w(),

      label: SC.LabelView.design({
        layout: { top: 0, left: 0, right: 0, height: 19 },
        value: '_myAccountView.ConfirmPassword',
        localize: YES
      }),

      field: SC.TextFieldView.design({
        layout: { top: 20, left: 0, height: 25 }
      })
    }),

    changePasswordButton: SC.ButtonView.design({
      layout: {top: 210, left: 20, width: 200 },
      title: '_myAccountView.ChangePassword',
      localize: YES,
      controlSize: SC.HUGE_CONTROL_SIZE,
      isDefault: YES,
      target: 'Chililog.myAccountViewController',
      action: 'changePassword'

    })
  })  // changePassword

});
