// ==========================================================================
// Project:   Chililog
// Copyright: ©2011 My Company, Inc.
// ==========================================================================


/**
 * Mixin for healping view controllers
 */
Chililog.ViewControllerMixin = {

  /**
   * Recursively finds fields and validates them
   * 
   * @param {SC.View} view parent view under which we will look for fields to validate
   * @returns {Object} SC.VALIDATE_OK if valid, else SC.Error
   */
  findFieldAndValidate: function(view) {
    var childViews = view.get('childViews');

    if (SC.none(childViews)) {
      return SC.VALIDATE_OK;
    }

    for(var i=0; i < childViews.length; i++)
    {
      var v = childViews[i];

      // Don't check if view is not visible
      if (!v.get('isVisible')) {
        continue;
      }

      if (v instanceof SC.TextFieldView ||
          v instanceof Chililog.RadioView ||
          v instanceof Chililog.CheckboxView) {
        var validator = v.get('validator');
        if (!SC.none(validator)) {
          var result = v.performValidateSubmit();
          if (result !== SC.VALIDATE_OK) {
            return result;
          }
        }
      } else {
        var result = this.findFieldAndValidate(v);
        if (result !== SC.VALIDATE_OK) {
          return result;
        }
      }
    }
    return SC.VALIDATE_OK;
  },

  /**
   * Sets focus on a field
   *
   * @param {SC.View} field to set focus
   * @param {Number} [delayMilliSeconds] Optional milli-seconds to delay setting focus
   */
  setFocusOnField: function(field, delayMilliSeconds) {
    if (SC.none(delayMilliSeconds)) {
      field.becomeFirstResponder();
      return;
    }

    this.invokeLater(function() {
      field.becomeFirstResponder();
    }, delayMilliSeconds);
  },

  /**
   * Display an error message to the user and sets focus on the field contained in the error
   * 
   * @param {SC.Error} error
   */
  showError: function(error) {
    if (SC.instanceOf(error, SC.Error)) {
      // Error
      var message = error.get('message');
      SC.AlertPane.error({ message: message });

      var field = error.get('errorField');

      // See if we have set a default field if not found
      if (SC.none(field)) {
        field = this.get('errorDefaultField');
      }

      if (!SC.none(field)) {
        if (field.scrollToVisible) {
          field.scrollToVisible();
        }
        this.setFocusOnField(field);
      }
    } else {
      // Assume error message string
      SC.AlertPane.error(error);
    }    
  }


};