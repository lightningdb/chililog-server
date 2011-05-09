// ==========================================================================
// Project:   Chililog - mainPage
// Copyright: ©2011 My Company, Inc.
// ==========================================================================

sc_require('views/label_mixin');

// *********************************************************************************************************************
// My Profile
// *********************************************************************************************************************
/**
 * Allows the user to change their profile details
 */
Chililog.MyProfileView = SC.View.design({
  layout: { top: 0, left: 0, bottom: 0, right: 0 },
  classNames: ['box'],
  childViews: 'username emailAddress displayName saveButton cancelButton savingImage successMessage'.w(),

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

  username: SC.View.design({
    layout: {top: 0, left: 0, right: 0, height: 50 },
    classNames: ['data-item'],
    childViews: 'label field'.w(),

    label: SC.LabelView.design(Chililog.RequiredFieldLabelMixin, {
      layout: { top: 15, left: 10, width: 200, height: 30 },
      value: '_myAccountView.Username'.loc()
    }),

    field: SC.TextFieldView.design({
      layout: { top: 10, left: 210, width: 300, height: 30 },
      valueBinding: 'Chililog.myAccountViewController.username',
      maxLength: 100,
      validator: Chililog.RegExpValidator.extend({
        keyDownRegExp: /[A-Za-z0-9_\-\.'@]/,
        fieldRegExp: /^[A-Za-z0-9_\-\.'@]+$/,
        invalidFieldErrorMessage: '_myAccountView.Username.Invalid',
        requiredFieldErrorMessage: '_myAccountView.Username.Required'
      })
    })
  }),

  emailAddress: SC.View.design({
    layout: {top: 50, left: 0, right: 0, height: 50 },
    classNames: ['data-item'],
    childViews: 'label field'.w(),

    label: SC.LabelView.design(Chililog.RequiredFieldLabelMixin,{
      layout: { top: 15, left: 10, width: 200, height: 30 },
      value: '_myAccountView.EmailAddress'.loc()
    }),

    field: SC.TextFieldView.design({
      layout: { top: 10, left: 210, width: 300, height: 30 },
      valueBinding: 'Chililog.myAccountViewController.emailAddress',
      maxLength: 200,
      validator: Chililog.EmailAddressValidator.extend({
        invalidFieldErrorMessage: '_myAccountView.EmailAddress.Invalid',
        requiredFieldErrorMessage: '_myAccountView.EmailAddress.Required'
      })
    })
  }),

  displayName: SC.View.design({
    layout: {top: 100, left: 0, right: 0, height: 80 },
    classNames: ['data-item'],
    childViews: 'label help field'.w(),

    label: SC.LabelView.design({
      layout: { top: 15, left: 10, width: 200, height: 20 },
      value: '_myAccountView.DisplayName'.loc()
    }),

    help: SC.LabelView.design({
      classNames: ['help'],
      layout: { top: 35, left: 10, width: 170, height: 50 },
      value: '_myAccountView.DisplayName.Help'.loc()
    }),

    field: SC.TextFieldView.design({
      layout: { top: 10, left: 210, width: 300, height: 30 },
      valueBinding: 'Chililog.myAccountViewController.displayName'
    })
  }),

  saveButton: SC.ButtonView.design({
    layout: {top: 190, left: 10, width: 90 },
    title: '_save',
    localize: YES,
    controlSize: SC.HUGE_CONTROL_SIZE,
    isDefault: YES,
    isEnabledBinding: SC.Binding.from('Chililog.myAccountViewController.canSaveProfile').oneWay(),
    target: 'Chililog.myAccountViewController',
    action: 'saveProfile'
  }),

  cancelButton: SC.ButtonView.design({
    layout: {top: 190, left: 110, width: 90 },
    title: '_cancel',
    localize: YES,
    controlSize: SC.HUGE_CONTROL_SIZE,
    isEnabledBinding: SC.Binding.from('Chililog.myAccountViewController.canSaveProfile').oneWay(),
    target: 'Chililog.myAccountViewController',
    action: 'discardProfileChanges'
  }),

  savingImage: Chililog.ImageView.design({
    layout: { top: 195, left: 210, width: 16, height: 16 },
    value: sc_static('images/working'),
    isVisibleBinding: SC.Binding.from('Chililog.myAccountViewController.isSavingProfile').oneWay().bool(),
    useImageQueue: NO
  }),

  successMessage: SC.LabelView.design({
    layout: { top: 190, left: 210, width: 155, height: 25, opacity: 0 },
    classNames: ['success'],
    value: '_saveSuccess',
    localize: YES
  })
});

/**
 * Instance the view
 */
Chililog.myProfileView = Chililog.MyProfileView.create();


// *********************************************************************************************************************
// Change Password
// *********************************************************************************************************************
/**
 * Allows the user to change password
 */
Chililog.ChangePasswordView = SC.View.design({
  layout: { top: 0, left: 0, bottom: 0, right: 0 },
  classNames: ['box'],
  childViews: 'oldPassword newPassword confirmNewPassword changePasswordButton changingPasswordImage successMessage'.w(),

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

  oldPassword: SC.View.design({
    layout: {top: 0, left: 0, right: 0, height: 50 },
    classNames: ['data-item'],
    childViews: 'label field'.w(),

    label: SC.LabelView.design(Chililog.RequiredFieldLabelMixin,{
      layout: { top: 15, left: 10, width: 200, height: 30 },
      value: '_myAccountView.OldPassword'.loc()
    }),

    field: SC.TextFieldView.design({
      layout: { top: 10, left: 210, width: 300, height: 30 },
      isPassword: YES,
      valueBinding: 'Chililog.myAccountViewController.oldPassword'
    })
  }),

  newPassword: SC.View.design({
    layout: {top: 50, left: 0, right: 0, height: 50 },
    classNames: ['data-item'],
    childViews: 'label field'.w(),

    label: SC.LabelView.design(Chililog.RequiredFieldLabelMixin,{
      layout: { top: 15, left: 10, width: 200, height: 30 },
      value: '_myAccountView.NewPassword'.loc()
    }),

    field: SC.TextFieldView.design({
      layout: { top: 10, left: 210, width: 300, height: 30 },
      isPassword: YES,
      valueBinding: 'Chililog.myAccountViewController.newPassword',
      maxLength: 100,
      validator: Chililog.RegExpValidator.extend({
        fieldRegExp: /^(?=.{8,}$)(?=(?:.*?\d){1})(?=(?:.*?[A-Za-z]){1})(?=(?:.*?\W){1})/,
        invalidFieldErrorMessage: '_myAccountView.NewPassword.Invalid',
        requiredFieldErrorMessage: '_myAccountView.NewPassword.Required'
      })
    })
  }),

  confirmNewPassword: SC.View.design({
    layout: {top: 100, left: 0, right: 0, height: 50 },
    classNames: ['data-item'],
    childViews: 'label field'.w(),

    label: SC.LabelView.design(Chililog.RequiredFieldLabelMixin,{
      layout: { top: 15, left: 10, width: 200, height: 20 },
      value: '_myAccountView.ConfirmNewPassword'.loc()
    }),

    field: SC.TextFieldView.design({
      layout: { top: 10, left: 210, width: 300, height: 30 },
      isPassword: YES,
      valueBinding: 'Chililog.myAccountViewController.confirmNewPassword',
      maxLength: 100,
      validator: Chililog.NotEmptyValidator.extend({
        requiredFieldErrorMessage: '_myAccountView.ConfirmNewPassword.Required'
      })
    })
  }),

  changePasswordButton: SC.ButtonView.design({
    layout: {top: 160, left: 10, width: 160 },
    title: '_myAccountView.ChangePassword',
    localize: YES,
    controlSize: SC.HUGE_CONTROL_SIZE,
    isDefault: YES,
    isEnabledBinding: SC.Binding.from('Chililog.myAccountViewController.canChangePassword').oneWay(),
    target: 'Chililog.myAccountViewController',
    action: 'changePassword'
  }),

  changingPasswordImage: Chililog.ImageView.design({
    layout: { top: 165, left: 180, width: 16, height: 16 },
    value: sc_static('images/working'),
    isVisibleBinding: SC.Binding.from('Chililog.myAccountViewController.isChangingPassword').oneWay().bool(),
    useImageQueue: NO
  }),

  successMessage: SC.LabelView.design({
    layout: { top: 160, left: 180, width: 185, height: 25, opacity: 0 },
    classNames: ['success'],
    value: '_myAccountView.ChangePassword.Success',
    localize: YES
  })
});

/**
 * Instance the view
 */
Chililog.changePasswordView = Chililog.ChangePasswordView.create();


// *********************************************************************************************************************
// Main account view put down here so that sub-views are instanced before this main view. This is required
// when setting the default tab view.
// *********************************************************************************************************************
/**
 * The main account page show tabs for account options
 */
Chililog.MyAccountView = SC.View.design({
  layout: { top: 10, left: 10, bottom: 10, right: 10 },
  childViews: 'title body'.w(),

  title: SC.LabelView.design({
    layout: { top: 0, left: 0, width: 200, height: 30 },
    tagName: 'h1',
    controlSize: SC.HUGE_CONTROL_SIZE,
    value: '_myAccountView.Title',
    localize: YES
  }),

  body: SC.TabView.design({
    layout: { top: 35, left: 0, right: 0, bottom: 10 },
    tabLocation: SC.TOP_TOOLBAR_LOCATION,
    tabHeight: 35,
    segmentedView: SC.SegmentedView.extend({controlSize: SC.LARGE_CONTROL_SIZE, align: SC.ALIGN_LEFT}),
    items: [
      { title: 'My Profile', value: 'Chililog.myProfileView'},
      { title: 'Change Password', value: 'Chililog.changePasswordView'}
    ],
    itemTitleKey: 'title',
    itemValueKey: 'value',
    nowShowing: 'Chililog.myProfileView',

    containerViewDidChange2: function() {
      var ns = this.getPath('containerView.contentView');
      var field = null;
      if (ns === Chililog.myProfileView) {
        field = this.getPath('containerView.contentView.username.field');
      } else if (ns === Chililog.changePasswordView) {
        field = this.getPath('containerView.contentView.oldPassword.field');
      }
      // Put in delay to let view be displayed in container before setting focus
      if (!SC.none(field)) {
        this.invokeLater(function() {
          field.becomeFirstResponder();
        }, 200);
      }
    }.observes('.containerView.contentView')
  })
});

/**
 * Instance the view
 */
Chililog.myAccountView = Chililog.MyAccountView.create();




