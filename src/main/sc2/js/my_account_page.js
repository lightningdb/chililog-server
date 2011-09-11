//
// Copyright 2010 Cinch Logic Pty Ltd.
//
// http://www.chililog.com
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//

//
// Javascript for my_profile.html
//

// --------------------------------------------------------------------------------------------------------------------
// Views
// --------------------------------------------------------------------------------------------------------------------

/**
 * @class
 * Common functions for profile field data
 */
App.ProfileFieldDataMixin = {
  // Save when ENTER clicked
  insertNewline: function() {
    App.statechart.sendAction('startProfileSave');
    return;
  }
};

/**
 * @class
 * Success message
 */
App.ProfileSuccessMessage = SC.View.extend({
  classNames: 'alert-message block-message success inline'.w(),
  messageBinding: 'App.pageController.profileSuccessMessage',
  defaultTemplate: SC.Handlebars.compile('{{message}}'),
  isVisibleBinding: SC.Binding.from('App.pageController.profileSuccessMessage').oneWay().bool()
});

/**
 * @class
 * Success message
 */
App.ProfileErrorMessage = SC.View.extend({
  classNames: 'alert-message block-message error inline'.w(),
  messageBinding: 'App.pageController.profileErrorMessage ',
  defaultTemplate: SC.Handlebars.compile('{{message}}'),
  isVisibleBinding: SC.Binding.from('App.pageController.profileErrorMessage').oneWay().bool()
});

/**
 * @class
 * Email address
 */
App.UsernameField = SC.View.extend({
  classNames: 'field'.w(),
  label: '_myAccount.username'.loc(),
  Data : App.TextBoxView.extend(App.ProfileFieldDataMixin, {
    valueBinding: 'App.pageController.authenticatedUserRecord.username',
    disabled: YES
  })
});

/**
 * @class
 * Email address
 */
App.UsernameField = SC.View.extend({
  classNames: 'field'.w(),
  label: '_myAccount.username'.loc(),
  Data : App.TextBoxView.extend(App.ProfileFieldDataMixin, {
    valueBinding: 'App.pageController.authenticatedUserRecord.username',
    disabled: YES
  })
});

/**
 * @class
 * Email address
 */
App.EmailAddressField = SC.View.extend({
  classNames: 'field'.w(),
  label: '_myAccount.emailAddress'.loc(),
  Data : App.TextBoxView.extend(App.ProfileFieldDataMixin, {
    valueBinding: 'App.pageController.authenticatedUserRecord.emailAddress',
    disabledBinding: SC.Binding.from('App.pageController.isSaving').oneWay().bool(),
    didInsertElement: function() {
      this.$().focus();
    }
  })
});

/**
 * @class
 * Display Name
 */
App.DisplayNameField = SC.View.extend({
  classNames: 'field'.w(),
  label: '_myAccount.displayName'.loc(),
  Data : App.TextBoxView.extend(App.ProfileFieldDataMixin, {
    valueBinding: 'App.pageController.authenticatedUserRecord.displayName',
    disabledBinding: SC.Binding.from('App.pageController.isSaving').oneWay().bool()
  })
});

/**
 * @class
 * Button to save user profile details
 */
App.ProfileSaveButton = App.ButtonView.extend({
  disabledBinding: SC.Binding.from('App.pageController.canSaveProfile').oneWay().bool().not(),
  label: '_save'.loc(),
  click: function() {
    App.statechart.sendAction('startSavingProfile');
    return;
  }
});

/**
 * @class
 * Spinner displayed while saving
 */
App.ProfileWorkingImage = App.ImgView.extend({
  src: 'images/working.gif',
  visible: NO,
  isVisibleBinding: SC.Binding.from('App.pageController.isProfileSaving').oneWay().bool()
});


/**
 * @class
 * Common functions for password field data
 */
App.PasswordFieldDataMixin = {
  // Save when ENTER clicked
  insertNewline: function() {
    App.statechart.sendAction('startSavingassword');
    return;
  }
};

/**
 * @class
 * Old Password
 */
App.OldPasswordField = SC.View.extend({
  classNames: 'field'.w(),
  label: '_myAccount.oldPassword'.loc(),
  Data : App.TextBoxView.extend(App.PasswordFieldDataMixin, {
    type: 'password',
    valueBinding: 'App.pageController.oldPassword',
    disabledBinding: SC.Binding.from('App.pageController.isSaving').oneWay().bool()
  })
});

/**
 * @class
 * New  Password
 */
App.NewPasswordField = SC.View.extend({
  classNames: 'field'.w(),
  label: '_myAccount.newPassword'.loc(),
  Data : App.TextBoxView.extend(App.PasswordFieldDataMixin, {
    type: 'password',
    valueBinding: 'App.pageController.newPassword',
    disabledBinding: SC.Binding.from('App.pageController.isSaving').oneWay().bool()
  })
});

/**
 * @class
 * Confirm Password
 */
App.ConfirmPasswordField = SC.View.extend({
  classNames: 'field'.w(),
  label: '_myAccount.confirmPassword'.loc(),
  help: '_myAccount.confirmPassword.help'.loc(),
  Data : App.TextBoxView.extend(App.PasswordFieldDataMixin, {
    type: 'password',
    valueBinding: 'App.pageController.confirmPassword',
    disabledBinding: SC.Binding.from('App.pageController.isSaving').oneWay().bool()
  })
});

/**
 * @class
 * Button to change password
 */
App.PasswordSaveButton = App.ButtonView.extend({
  disabledBinding: SC.Binding.from('App.pageController.canSavePassword').oneWay().bool().not(),
  label: '_myAccount.changePassword'.loc(),
  click: function() {
    App.statechart.sendAction('startPasswordSave');
    return;
  }
});

/**
 * @class
 * Spinner displayed while saving
 */
App.PasswordWorkingImage = App.ImgView.extend({
  src: 'images/working.gif',
  visible: NO,
  isVisibleBinding: SC.Binding.from('App.pageController.isPasswordSaving').oneWay().bool()
});

// --------------------------------------------------------------------------------------------------------------------
// Controllers
// --------------------------------------------------------------------------------------------------------------------
/**
 * @class
 * Mediates between state charts and views for the main page
 */
App.pageController = SC.Object.create({
  /**
   * User name
   * @type String
   */
  authenticatedUserRecord: null,

  /**
   * Flag to indicate if we are saving password
   * @type Boolean
   */
  isSavingProfile: NO,

  /**
   * Flag to indicate if we can save the profile or not. Can only save if changes have been made
   */
  canSaveProfile: function() {
    var recordStatus = this.getPath('authenticatedUserRecord.status');
    if (!SC.none(recordStatus) && recordStatus !== SC.Record.READY_CLEAN && !this.get('isSaving')) {
      return YES;
    }
    return NO;
  }.property('authenticatedUserRecord.status', 'isSaving').cacheable(),

  /**
   * Message to display when save successful
   */
  profileSuccessMessage: null,

  /**
   * Message to display save has an error
   */
  profileErrorMessage: null,

  /**
   * User's current password
   * @type String
   */
  oldPassword: '',

  /**
   * New password
   * @type String
   */
  newPassword: '',

  /**
   * Confirm password
   * @type String
   */
  confirmPassword: '',

  /**
   * Flag to indicate if we are saving profile
   * @type Boolean
   */
  isSavingPassword: NO,

  /**
   * Flag to indicate if the user is able to change password
   * Only valid if old, new and confirm passwords have been entered
   *
   * @type Boolean
   */
  canSavePassword: function() {
    if (!this.get('isSaving') && !SC.empty(this.get('oldPassword')) && !SC.empty(this.get('newPassword')) && !SC.empty(this.get('confirmPassword'))) {
      return YES;
    }
    return NO;
  }.property('oldPassword', 'newPassword', 'confirmPassword', 'isSaving').cacheable(),

  /**
   * Flag to indicate if we are saving profile or password
   * @type Boolean
   */
  isSaving: function() {
    var isSavingPassword = this.get('isSavingPassword');
    var isSavingProfile = this.get('isSavingProfile');
    return isSavingPassword || isSavingProfile;
  }.property('isSavingPassword', 'isSavingProfile').cacheable()

});


// --------------------------------------------------------------------------------------------------------------------
// States
// --------------------------------------------------------------------------------------------------------------------
App.statechart = SC.Statechart.create({

  rootState: SC.State.extend({

    initialSubstate: 'loading',

    /**
     * Initial data load
     */
    loading: SC.State.extend({
      enterState: function() {
        // Load user details
        var user = App.sessionEngine.editProfile();
        App.pageController.set('authenticatedUserRecord', user);

        this.gotoState('editing');
      },

      exitState: function() {
      }
    }),

    /**
     * Prompt the user to change profile or password
     */
    editing: SC.State.extend({
      enterState: function() {
      },

      exitState: function() {
      },

      startSavingProfile: function() {
        this.gotoState('savingProfile');
      }
    }),

    /**
     * Save user profile information to the server
     */
    savingProfile: SC.State.extend({
      enterState: function() {
        App.pageController.set('profileSuccessMessage', null);
        App.pageController.set('profileErrorMessage', null);
        App.pageController.set('isSavingProfile', YES);
        this.save();
      },

      exitState: function() {
        App.pageController.set('isSavingProfile', NO);
      },

      save: function() {
        var user = App.pageController.get('authenticatedUserRecord');
        App.sessionEngine.saveProfile(user, this, this.endSave);
      },

      endSave: function(documentID, callbackParams, error) {
        if (SC.none(error)) {
          // Show success message
          App.pageController.set('profileSuccessMessage', '_saveSuccess'.loc());
          var element = $('#successMessage');
          element.stop().show();
          for (var i=0; i < 5; i++) {
            element.effect('highlight', { color : '#FDF5D9'}, 100);
          }
          element.delay(3000).fadeOut(3000);

          // Update the display name in to drop down menu
          var loggedInUser = App.sessionEngine.get('loggedInUser');
          $('#navUsername').empty().append(loggedInUser.get('displayNameOrUsername') + "&nbsp;");

          // Reload
          this.gotoState('loading');
        } else {
          // Show error message
          App.pageController.set('profileErrorMessage', error);

          // Set focus on field if specified
          if (!SC.empty(error.errorFieldId)) {
            var dataElementId = error.errorFieldId.replace('Field', 'Data');
            setTimeout(function() {
              $('#' + dataElementId)[0].focus();
            }, 100);
          } else {
            
          }

          // Re-edit
          this.gotoState('editing');
        }

      }
    }),

    /**
     * Save password information to the server
     */
    savingPassword: SC.State.extend({
      enterState: function() {
      },

      exitState: function() {
      }
    })

  })
});


// --------------------------------------------------------------------------------------------------------------------
// Start page processing
// --------------------------------------------------------------------------------------------------------------------
App.pageFileName = Auth.getPageName();

if (App.sessionEngine.load()) {
  App.setupStandardPage(App.pageFileName);
  App.statechart.initStatechart();
} else {
  // Not logged in so go to login page
  window.location = 'login.html?returnTo=' + encodeURIComponent(App.pageFileName);
}

