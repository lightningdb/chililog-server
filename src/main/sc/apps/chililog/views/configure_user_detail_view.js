// ==========================================================================
// Project:   Chililog
// Copyright: ©2011 My Company, Inc.
// ==========================================================================

sc_require('views/image_view');
sc_require('views/radio_view');
sc_require('views/validators');
sc_require('views/label_mixin');

/**
 * User details
 */
Chililog.ConfigureUserDetailView = SC.PanelPane.design({
  layoutBinding: 'Chililog.configureUserDetailViewController.paneLayout',

  contentView: SC.View.extend({
    childViews: 'title body buttons'.w(),

    title: SC.LabelView.design({
      layout: { top: 10, left: 10, right: 10, height: 30 },
      tagName: 'h1',
      controlSize: SC.HUGE_CONTROL_SIZE,
      valueBinding: 'Chililog.configureUserDetailViewController.title',
      localize: YES
    }),

    body: SC.View.design({
      layout: { top: 50, left: 10, bottom: 50, right: 10 },
      classNames: ['box'],
      childViews: 'username emailAddress displayName currentStatus password confirmPassword'.w(),

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
      })
    }),

    buttons: SC.View.design({
      layout: { bottom: 0, left: 10, right: 10, height: 40 },
      childViews: 'previousButton nextButton deleteButton savingImage saveButton cancelButton savingImage'.w(),

      previousButton: SC.ButtonView.design({
        layout: { top: 0, left: 0, width: 40 },
        title: '_previous',
        localize: YES,
        controlSize: SC.HUGE_CONTROL_SIZE,
        isVisibleBinding: SC.Binding.from('Chililog.configureUserDetailViewController.canDelete').oneWay().bool(),
        target: Chililog.configureUserDetailViewController,
        action: 'previous'
      }),

      nextButton: SC.ButtonView.design({
        layout: { top: 0, left: 50, width: 40 },
        title: '_next',
        localize: YES,
        controlSize: SC.HUGE_CONTROL_SIZE,
        isVisibleBinding: SC.Binding.from('Chililog.configureUserDetailViewController.canDelete').oneWay().bool(),
        target: Chililog.configureUserDetailViewController,
        action: 'next'
      }),

      deleteButton: SC.ButtonView.design({
        layout: {top: 0, centerX: 0, width: 80 },
        title: '_delete',
        localize: YES,
        controlSize: SC.HUGE_CONTROL_SIZE,
        isVisibleBinding: SC.Binding.from('Chililog.configureUserDetailViewController.canDelete').oneWay().bool(),
        target: Chililog.configureUserDetailViewController,
        action: 'confirmErase'
      }),

      savingImage: Chililog.ImageView.design({
        layout: { top: 15, right: 180, width: 16, height: 16 },
        value: sc_static('images/working'),
        isVisibleBinding: SC.Binding.from('Chililog.configureUserDetailViewController.isSaving').oneWay().bool(),
        useImageQueue: NO
      }),

      saveButton: SC.ButtonView.design({
        layout: {top: 0, right: 90, width: 80 },
        title: '_save',
        localize: YES,
        controlSize: SC.HUGE_CONTROL_SIZE,
        isDefault: YES,
        isEnabledBinding: SC.Binding.from('Chililog.configureUserDetailViewController.canSave').oneWay(),
        target: 'Chililog.configureUserDetailViewController',
        action: 'save'
      }),

      cancelButton: SC.ButtonView.design({
        layout: {top: 0, right: 0, width: 80 },
        title: '_cancel',
        localize: YES,
        controlSize: SC.HUGE_CONTROL_SIZE,
        isCancel: YES,
        target: 'Chililog.configureUserDetailViewController',
        action: 'discardChanges'
      })
    })

  })

});

/**
 * Instance configure user view
 */
Chililog.configureUserDetailView = Chililog.ConfigureUserDetailView.create();