// ==========================================================================
// Project:   Chililog - mainPage
// Copyright: ©2011 My Company, Inc.
// ==========================================================================

/**
 * The login page
 */
Chililog.loginPage = SC.Page.design({

  loginPane:  SC.MainPane.design({
    layout: { width: 360, height: 160, centerX: 0, centerY: 0 },
    classNames: ['login-pane'],
    childViews: 'boxView'.w(),

    boxView: SC.View.design({
      childViews: 'username password rememberMe loginButton loadingImage errorMessage'.w(),

      username: SC.View.design({
        layout: { left: 17, right: 14, top: 17, height: 26 },
        childViews: 'label field'.w(),

        label: SC.LabelView.design({
          layout: { left: 0, width: 107, height: 18, centerY: 0 },
          value: '_login.Username',
          localize: YES,
          textAlign: SC.ALIGN_RIGHT
        }),

        field: SC.TextFieldView.design({
          layout: { width: 200, height: 22, right: 3, centerY: 0 },
          isEnabledBinding: 'Chililog.loginPaneController.isEdit',
          valueBinding: 'Chililog.loginPaneController.username',
          // Set focus
          // http://groups.google.com/group/sproutcore/browse_thread/thread/7e72be97d0229689
          isVisibleObserver: function() {
            if(this.get('isVisibleInWindow')) {
              this.$input()[0].focus();
            }
          }.observes('isVisibleInWindow')
        })
      }),

      password: SC.View.design({
        layout: { left: 17, right: 14, top: 45, height: 26 },
        childViews: 'label field'.w(),

        label: SC.LabelView.design({
          layout: { left: 0, width: 107, height: 18, centerY: 0 },
          value: '_login.Password',
          localize: YES,
          textAlign: SC.ALIGN_RIGHT
        }),

        field: SC.TextFieldView.design({
          layout: { width: 200, height: 22, right: 3, centerY: 0 },
          isPassword: YES,
          isEnabledBinding: 'Chililog.loginPaneController.isEdit',
          valueBinding: 'Chililog.loginPaneController.password'
        })
      }),

      rememberMe: SC.View.design({
        layout: { left: 17, right: 14, top: 72, height: 26 },
        childViews: 'field'.w(),

        field: SC.CheckboxView.design({
          layout: { width: 200, height: 22, right: 3, centerY: 0 },
          title: '_login.RememberMe',
          localize: YES,
          isEnabledBinding: 'Chililog.loginPaneController.isEdit',
          valueBinding: 'Chililog.loginPaneController.rememberMe'
        })
      }),

      loginButton: SC.ButtonView.design({
        layout: { height: 24, width: 80, bottom: 17, right: 17 },
        title: '_login.Login',
        localize: YES,
        isDefault: YES,
        isEnabledBinding: SC.Binding.from("LoginLogoutSample.loginPageController.isLoggingIn")
          .bool()
          .transform(function(value, isForward) {
          return !value;
        }),

        target: 'Chililog.loginPaneController',
        action: 'beginLogin'
      }),

      loadingImage: SC.ImageView.design({
        layout: { width: 16, height: 16, bottom: 20, right: 110 },
        value: sc_static('images/loading'),
        useImageCache: NO,
        isVisibleBinding: 'Chililog.loginPaneController.isBusy'
      }),

      errorMessage: SC.LabelView.design({
        layout: { height: 40, width: 230, right: 120, bottom: 7 },
        classNames: ['error-message'],

        valueBinding: 'Chililog.loginPaneController.errorMessage'
      })

    })  //boxView

  })  //loginPane
});