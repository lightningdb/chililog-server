// ==========================================================================
// Project:   Chililog - mainPage
// Copyright: ©2011 My Company, Inc.
// ==========================================================================

/**
 * My profile view
 */

Chililog.MyAccountView = SC.View.design({
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
    layout: { top: 35, left: 0, width: 400, height: 300 },
    classNames: ['edit-box'],
    childViews: 'title username email displayName saveButton cancelButton savingImage successMessage'.w(),

    /**
     * Handle the RETURN key being pressed. Default to clicking save profile behaviour.
     * @param evt
     */
    keyDown: function(evt) {
      var which = evt.which;
      if (which === SC.Event.KEY_RETURN) {
        var canSave = Chililog.myAccountViewController.get('canSaveProfile');
        if (canSave) {
          Chililog.myAccountViewController.saveProfile();
          return YES;
        }
      }
      return NO;
    },

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
      layout: {top: 110, left: 20, right: 20, height: 50 },
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
      layout: {top: 170, left: 20, right: 20, height: 50 },
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
      layout: {top: 240, left: 20, width: 90 },
      title: '_save',
      localize: YES,
      controlSize: SC.HUGE_CONTROL_SIZE,
      isDefault: YES,
      isEnabledBinding: SC.Binding.from('Chililog.myAccountViewController.canSaveProfile').oneWay(),
      target: 'Chililog.myAccountViewController',
      action: 'saveProfile'
    }),

    cancelButton: SC.ButtonView.design({
      layout: {top: 240, left: 120, width: 90 },
      title: '_cancel',
      localize: YES,
      controlSize: SC.HUGE_CONTROL_SIZE,
      isEnabledBinding: SC.Binding.from('Chililog.myAccountViewController.canSaveProfile').oneWay(),
      target: 'Chililog.myAccountViewController',
      action: 'discardProfileChanges'
    }),

    savingImage: Chililog.ImageView.design({
      layout: { top: 245, left: 220, width: 16, height: 16 },
      value: sc_static('images/working'),
      isVisibleBinding: SC.Binding.from('Chililog.myAccountViewController.isSavingProfile').oneWay().bool(),
      useImageQueue: NO
    }),

    successMessage: SC.LabelView.design({
      layout: { top: 240, left: 220, width: 155, height: 25, opacity: 0 },
      classNames: ['success'],
      value: '_myAccountView.SaveProfileSuccess',
      localize: YES
    })
  }),  //myProfile

  changePassword: SC.View.design({
    layout: { top: 35, left: 410, width: 400, height: 300 },
    classNames: ['edit-box'],
    childViews: 'title oldPassword newPassword confirmNewPassword changePasswordButton changingPasswordImage successMessage'.w(),

    /**
     * Handle the RETURN key being pressed. Default to clicking change password behaviour.
     * @param evt
     */
    keyDown: function(evt) {
      var which = evt.which;
      if (which === SC.Event.KEY_RETURN) {
        var canChangePassword = Chililog.myAccountViewController.get('canChangePassword');
        if (canChangePassword) {
          Chililog.myAccountViewController.changePassword();
          return YES;
        }
      }
      return NO;
    },

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
        layout: { top: 20, left: 0, height: 25 },
        isPassword: YES,
        valueBinding: 'Chililog.myAccountViewController.oldPassword'
      })
    }),

    newPassword: SC.View.design({
      layout: {top: 110, left: 20, right: 20, height: 50 },
      childViews: 'label field'.w(),

      label: SC.LabelView.design({
        layout: { top: 0, left: 0, right: 0, height: 19 },
        value: '_myAccountView.NewPassword',
        localize: YES
      }),

      field: SC.TextFieldView.design({
        layout: { top: 20, left: 0, height: 25 },
        isPassword: YES,
        valueBinding: 'Chililog.myAccountViewController.newPassword'
      })
    }),

    confirmNewPassword: SC.View.design({
      layout: {top: 170, left: 20, right: 20, height: 50 },
      childViews: 'label field'.w(),

      label: SC.LabelView.design({
        layout: { top: 0, left: 0, right: 0, height: 19 },
        value: '_myAccountView.ConfirmPassword',
        localize: YES
      }),

      field: SC.TextFieldView.design({
        layout: { top: 20, left: 0, height: 25 },
        isPassword: YES,
        valueBinding: 'Chililog.myAccountViewController.confirmNewPassword'
      })
    }),

    changePasswordButton: SC.ButtonView.design({
      layout: {top: 240, left: 20, width: 160 },
      title: '_myAccountView.ChangePassword',
      localize: YES,
      controlSize: SC.HUGE_CONTROL_SIZE,
      isDefault: YES,
      isEnabledBinding: SC.Binding.from('Chililog.myAccountViewController.canChangePassword').oneWay(),
      target: 'Chililog.myAccountViewController',
      action: 'changePassword'
    }),

    changingPasswordImage: Chililog.ImageView.design({
      layout: { top: 245, left: 190, width: 16, height: 16 },
      value: sc_static('images/working'),
      isVisibleBinding: SC.Binding.from('Chililog.myAccountViewController.isChangingPassword').oneWay().bool(),
      useImageQueue: NO
    }),

    successMessage: SC.LabelView.design({
      layout: { top: 240, left: 190, width: 185, height: 25, opacity: 0 },
      classNames: ['success'],
      value: '_myAccountView.ChangePasswordSuccess',
      localize: YES
    })
  })  // changePassword
});

/**
 * Instance the view
 */
Chililog.myAccountView = Chililog.MyAccountView.create();
