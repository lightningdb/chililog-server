// ==========================================================================
// Project:   Chililog - mainPage
// Copyright: ©2011 My Company, Inc.
// ==========================================================================

sc_require('views/label_mixin');

/**
 * Allows the user to change password
 */
Chililog.MyPasswordView = SC.View.design({
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
      var canChangePassword = Chililog.myAccountMyPasswordViewController.get('canChangePassword');
      if (canChangePassword) {
        Chililog.myAccountMyPasswordViewController.save();
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
      value: '_myAccountMyPasswordView.OldPassword'.loc()
    }),

    field: SC.TextFieldView.design({
      layout: { top: 10, left: 210, width: 300, height: 30 },
      isPassword: YES,
      valueBinding: 'Chililog.myAccountMyPasswordViewController.oldPassword'
    })
  }),

  newPassword: SC.View.design({
    layout: {top: 50, left: 0, right: 0, height: 50 },
    classNames: ['data-item'],
    childViews: 'label field'.w(),

    label: SC.LabelView.design(Chililog.RequiredFieldLabelMixin,{
      layout: { top: 15, left: 10, width: 200, height: 30 },
      value: '_myAccountMyPasswordView.NewPassword'.loc()
    }),

    field: SC.TextFieldView.design({
      layout: { top: 10, left: 210, width: 300, height: 30 },
      isPassword: YES,
      valueBinding: 'Chililog.myAccountMyPasswordViewController.newPassword',
      maxLength: 100,
      validator: Chililog.RegExpValidator.extend({
        fieldRegExp: /^(?=.{8,}$)(?=(?:.*?\d){1})(?=(?:.*?[A-Za-z]){1})(?=(?:.*?\W){1})/,
        invalidFieldErrorMessage: '_myAccountMyPasswordView.NewPassword.Invalid',
        requiredFieldErrorMessage: '_myAccountMyPasswordView.NewPassword.Required'
      })
    })
  }),

  confirmNewPassword: SC.View.design({
    layout: {top: 100, left: 0, right: 0, height: 50 },
    classNames: ['data-item'],
    childViews: 'label field'.w(),

    label: SC.LabelView.design(Chililog.RequiredFieldLabelMixin,{
      layout: { top: 15, left: 10, width: 200, height: 20 },
      value: '_myAccountMyPasswordView.ConfirmNewPassword'.loc()
    }),

    field: SC.TextFieldView.design({
      layout: { top: 10, left: 210, width: 300, height: 30 },
      isPassword: YES,
      valueBinding: 'Chililog.myAccountMyPasswordViewController.confirmNewPassword',
      maxLength: 100,
      validator: Chililog.NotEmptyValidator.extend({
        requiredFieldErrorMessage: '_myAccountMyPasswordView.ConfirmNewPassword.Required'
      })
    })
  }),

  changePasswordButton: SC.ButtonView.design({
    layout: {top: 160, left: 10, width: 160 },
    title: '_myAccountMyPasswordView.ChangePassword',
    localize: YES,
    controlSize: SC.HUGE_CONTROL_SIZE,
    isDefault: YES,
    isEnabledBinding: SC.Binding.from('Chililog.myAccountMyPasswordViewController.canChangePassword').oneWay(),
    target: 'Chililog.myAccountMyPasswordViewController',
    action: 'save'
  }),

  changingPasswordImage: Chililog.ImageView.design({
    layout: { top: 165, left: 180, width: 16, height: 16 },
    value: sc_static('images/working'),
    isVisibleBinding: SC.Binding.from('Chililog.myAccountMyPasswordViewController.isSaving').oneWay().bool(),
    useImageQueue: NO
  }),

  successMessage: SC.LabelView.design({
    layout: { top: 160, left: 180, width: 185, height: 25, opacity: 0 },
    classNames: ['success'],
    value: '_myAccountMyPasswordView.ChangePassword.Success',
    localize: YES
  })
});

/**
 * Instance the view
 */
Chililog.myPasswordView = Chililog.MyPasswordView.create();

