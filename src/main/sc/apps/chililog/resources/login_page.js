// ==========================================================================
// Project:   Chililog - mainPage
// Copyright: ©2011 My Company, Inc.
// ==========================================================================

/**
 * The login page
 */
Chililog.loginPage = SC.Page.design({

  loginPane:  SC.MainPane.design({
    layout: { top: 0, left: 0, bottom: 0, right: 0 },
    childViews: 'boxView'.w(),

    boxView: SC.View.design({
      layout: { width: 300, height: 280, centerX: 0, centerY: 0 },
      classNames: ['login-box'],
      childViews: 'title line username password rememberMe loginButton loadingImage'.w(),

      title: SC.LabelView.design({
        layout: { top: 20, left: 20, right: 20, height: 30 },
        controlSize: SC.LARGE_CONTROL_SIZE, 
        tagName: 'h1',
        value: 'Chililog Workbench Login'
      }),

      line: SC.LabelView.design({
        layout: { top: 50, left: 20, right: 20, height: 2 },
        tagName: 'hr'
      }),

      username: SC.View.design({
        layout: {top: 70, left: 20, right: 20, height: 50 },
        childViews: 'label field'.w(),

        label: SC.LabelView.design({
          layout: { top: 0, left: 0, right: 0, height: 19 },
          value: '_loginPane.Username',
          localize: YES
        }),

        field: SC.TextFieldView.design({
          layout: { top: 20, left: 0, right: 0, height: 25 },
          isEnabledBinding: 'Chililog.loginPaneController.isEdit',
          valueBinding: 'Chililog.loginPaneController.username'
          // Set focus
          // http://groups.google.com/group/sproutcore/browse_thread/thread/7e72be97d0229689
          //isVisibleInWindowDidChange: function() {
          //  if(this.get('isVisibleInWindow')) {
          //    this.$input()[0].focus();
          //  }
          //}.observes('isVisibleInWindow')
        })
      }),

      password: SC.View.design({
        layout: {top: 130, left: 20, right: 20, height: 50 },
        childViews: 'label field'.w(),

        label: SC.LabelView.design({
          layout: { top: 0, left: 0, right: 0, height: 19 },
          value: '_loginPane.Password',
          localize: YES
        }),

        field: SC.TextFieldView.design({
          layout: { top: 20, left: 0, right: 0, height: 25 },
          isPassword: YES,
          isEnabledBinding: 'Chililog.loginPaneController.isEdit',
          valueBinding: 'Chililog.loginPaneController.password'
        })
      }),

      rememberMe: SC.View.design({
        layout: {top: 190, left: 20, right: 20, height: 20 },
        childViews: 'field'.w(),

        field: SC.CheckboxView.design({
          layout: { top: 0, left: 0, right: 0, height: 20 },
          title: '_loginPane.RememberMe',
          localize: YES,
          isEnabledBinding: 'Chililog.loginPaneController.isEdit',
          valueBinding: 'Chililog.loginPaneController.rememberMe'
        })
      }),

      loginButton: SC.ButtonView.design({
        layout: {top: 230, width: 100, centerX: 0 },
        title: '_loginPane.Login',
        localize: YES,
        isDefault: YES,
        controlSize: SC.HUGE_CONTROL_SIZE,
        isEnabledBinding: SC.Binding.from('Chililog.loginPaneController.isBusy')
          .bool()
          .transform(function(value, isForward) {
          return !value;
        }),

        target: 'Chililog.loginPaneController',
        action: 'login'
      }),

      loadingImage: Chililog.ImageView.design({
        layout: { top: 235, right: 50, width: 16, height: 16 },
        value: sc_static('images/working'),
        isVisibleBinding: 'Chililog.loginPaneController.isBusy',
        useImageCache: NO
      })
    }),  //boxView

    /**
     * Displays error messages
     */
    errorDidChange: function() {
      var error = Chililog.loginPaneController.get('error');
      if (SC.none(error)) {
        return;
      }
      
      if (SC.instanceOf(error, SC.Error)) {
        var message = error.get('message');
        SC.AlertPane.error({ message: message });

        var label = error.get('label');
        if (SC.empty(label)) {
          label = 'username';
        }
        var fieldPath = 'boxView.%@.field'.fmt(label);
        var field = this.getPath(fieldPath);
        if (!SC.none(field)) {
          field.becomeFirstResponder();
        }
      } else {
        SC.AlertPane.error(error);
      }

    }.observes('Chililog.loginPaneController.error'),

    /**
     * Set focus on the username field when pane becomes active
     */
    keyPaneDidChange: function() {
      var isKeyPane = this.get('isKeyPane');
      if (isKeyPane) {
        var field = this.getPath('boxView.username.field');
        field.becomeFirstResponder();
      }
    }.observes('isKeyPane')

  })  //loginPane
});