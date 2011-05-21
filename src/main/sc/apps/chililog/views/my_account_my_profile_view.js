// ==========================================================================
// Project:   Chililog - mainPage
// Copyright: ©2011 My Company, Inc.
// ==========================================================================

sc_require('views/label_mixin');

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
      var canSave = Chililog.myAccountMyProfileViewController.get('canSaveProfile');
      if (canSave) {
        Chililog.myAccountMyProfileViewController.saveProfile();
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
      value: '_myAccountMyProfileView.Username'.loc()
    }),

    field: SC.TextFieldView.design({
      layout: { top: 10, left: 210, width: 300, height: 30 },
      valueBinding: 'Chililog.myAccountMyProfileViewController.username',
      maxLength: 100,
      validator: Chililog.RegExpValidator.extend({
        keyDownRegExp: /[A-Za-z0-9_\-\.'@]/,
        fieldRegExp: /^[A-Za-z0-9_\-\.'@]+$/,
        invalidFieldErrorMessage: '_myAccountMyProfileView.Username.Invalid',
        requiredFieldErrorMessage: '_myAccountMyProfileView.Username.Required'
      })
    })
  }),

  emailAddress: SC.View.design({
    layout: {top: 50, left: 0, right: 0, height: 50 },
    classNames: ['data-item'],
    childViews: 'label field'.w(),

    label: SC.LabelView.design(Chililog.RequiredFieldLabelMixin,{
      layout: { top: 15, left: 10, width: 200, height: 30 },
      value: '_myAccountMyProfileView.EmailAddress'.loc()
    }),

    field: SC.TextFieldView.design({
      layout: { top: 10, left: 210, width: 300, height: 30 },
      valueBinding: 'Chililog.myAccountMyProfileViewController.emailAddress',
      maxLength: 200,
      validator: Chililog.EmailAddressValidator.extend({
        invalidFieldErrorMessage: '_myAccountMyProfileView.EmailAddress.Invalid',
        requiredFieldErrorMessage: '_myAccountMyProfileView.EmailAddress.Required'
      })
    })
  }),

  displayName: SC.View.design({
    layout: {top: 100, left: 0, right: 0, height: 80 },
    classNames: ['data-item'],
    childViews: 'label help field'.w(),

    label: SC.LabelView.design({
      layout: { top: 15, left: 10, width: 200, height: 20 },
      value: '_myAccountMyProfileView.DisplayName'.loc()
    }),

    help: SC.LabelView.design({
      classNames: ['help'],
      layout: { top: 35, left: 10, width: 170, height: 50 },
      value: '_myAccountMyProfileView.DisplayName.Help'.loc()
    }),

    field: SC.TextFieldView.design({
      layout: { top: 10, left: 210, width: 300, height: 30 },
      valueBinding: 'Chililog.myAccountMyProfileViewController.displayName'
    })
  }),

  saveButton: SC.ButtonView.design({
    layout: {top: 190, left: 10, width: 90 },
    title: '_save',
    localize: YES,
    controlSize: SC.HUGE_CONTROL_SIZE,
    isDefault: YES,
    isEnabledBinding: SC.Binding.from('Chililog.myAccountMyProfileViewController.canSave').oneWay(),
    target: 'Chililog.myAccountMyProfileViewController',
    action: 'save'
  }),

  cancelButton: SC.ButtonView.design({
    layout: {top: 190, left: 110, width: 90 },
    title: '_cancel',
    localize: YES,
    controlSize: SC.HUGE_CONTROL_SIZE,
    isEnabledBinding: SC.Binding.from('Chililog.myAccountMyProfileViewController.canSave').oneWay(),
    target: 'Chililog.myAccountMyProfileViewController',
    action: 'discardChanges'
  }),

  savingImage: Chililog.ImageView.design({
    layout: { top: 195, left: 210, width: 16, height: 16 },
    value: sc_static('images/working'),
    isVisibleBinding: SC.Binding.from('Chililog.myAccountMyProfileViewController.isSaving').oneWay().bool(),
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

