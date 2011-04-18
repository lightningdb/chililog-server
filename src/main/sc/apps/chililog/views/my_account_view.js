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
      isVisibleBinding: SC.Binding.from('Chililog.myAccountViewController.state').oneWay().transform(
        function(value, isForward) {
          return value === Chililog.myAccountViewStates.SAVING;
        }),
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
      isVisibleBinding: SC.Binding.from('Chililog.myAccountViewController.state').oneWay().transform(
        function(value, isForward) {
          return value === Chililog.myAccountViewStates.CHANGING_PASSWORD;
        }),
      useImageQueue: NO
    }),

    successMessage: SC.LabelView.design({
      layout: { top: 240, left: 190, width: 185, height: 25, opacity: 0 },
      classNames: ['success'],
      value: '_myAccountView.ChangePasswordSuccess',
      localize: YES
    })
  }),  // changePassword

  /**
   * Displays success or error messages when saving profile
   *
   * @param {Object} result YES if success, SC.Error if error, null if not set or executing.
   * @param {String} subViewName 'myProfile' or 'changePassword'
   */
  processResult: function(result, subViewName) {
    if (SC.none(result)) {
      return;
    }

    if (result === YES) {
      // Success
      var view = null;
      var field = null;
      if (subViewName === 'myProfile') {
        view = this.getPath('myProfile.successMessage');
        field = this.getPath('myProfile.username.field');
      } else if (subViewName === 'changePassword') {
        view = this.getPath('changePassword.successMessage');
        field = this.getPath('changePassword.oldPassword.field');
      }

      if (!SC.none(view)) {
        // Have to invokeLater because of webkit
        // http://groups.google.com/group/sproutcore/browse_thread/thread/482740f497d80462/cba903f9cc6aadf8?lnk=gst&q=animate#cba903f9cc6aadf8
        view.adjust("opacity", 1);
        this.invokeLater(function() {
          view.animate("opacity", 0, { duration: 2, timing:'ease-in' });
          }, 10);
      }

      if (!SC.none(field)) {
        field.becomeFirstResponder();
      }
    } else if (SC.instanceOf(result, SC.Error)) {
      // Error
      var message = error.get('message');
      if (SC.empty(result)) {
        return;
      }
      SC.AlertPane.error({ message: message });

      var label = result.get('label');
      if (SC.empty(label)) {
        label = 'username';
      }

      var fieldPath = '%@.%@.field'.fmt(subViewName, label);
      var field = this.getPath(fieldPath);
      if (!SC.none(field)) {
        field.becomeFirstResponder();
      }
    } else {
      // Assume error message string
      SC.AlertPane.error(result);
    }
  },

  /**
   * Displays success or error messages when saving profile
   */
  saveProfileResultDidChange: function() {
    var result = Chililog.myAccountViewController.get('saveProfileResult');
    this.processResult(result, 'myProfile');
  }.observes('Chililog.myAccountViewController.saveProfileResult'),

  /**
   * Displays success or error messages when saving profile
   */
  changePasswordResultDidChange: function() {
    var result = Chililog.myAccountViewController.get('changePasswordResult');
    this.processResult(result, 'changePassword');
  }.observes('Chililog.myAccountViewController.changePasswordResult')

});
