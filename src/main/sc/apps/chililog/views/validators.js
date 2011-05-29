// ==========================================================================
// Project:   Chililog.mainPaneController
// Copyright: ©2011 My Company, Inc.
// ==========================================================================

/**
 * Handles parsing and validating of positive integers.
 */
Chililog.PositiveIntegerValidator = SC.Validator.extend({

    /**
     * Default Value to be displayed. If the value in the text field is null,
     * undefined or an empty string, it will be replaced by this value.
     *
     * @property
     * @type Number
     * @default null
     */
    defaultValue: null,

    /**
     * Format number of thousand separators for display
     *
     * @property
     * @type Boolean
     * @default NO
     */
    formatNumber: NO,

    /**
     * If the field is required, then set this error message
     *
     * @property
     * @type String
     * @default null Field is not required.
     */
    requiredFieldErrorMessage: null,

    /**
     * Converts Number to string for display
     *
     * @param {Number} object The object to transform
     * @param {SC.FormView} form The form this field belongs to. (optional)
     * @param {SC.View} view The view the value is required for.
     * @returns {Number} a string suitable for display
     */
    fieldValueForObject: function(object, form, field) {
      var ret = '';
      switch (SC.typeOf(object)) {
        case SC.T_NUMBER:
          ret = object.toFixed(0);
          break;
        case SC.T_NULL:
        case SC.T_UNDEFINED:
          ret = this.get('defaultValue');
          if (SC.none(ret)) {
            ret = '';
          }
          break;
      }

      if (this.get('formatNumber')) {
        ret = this.addThousandSeparator(ret);
      }

      return ret;
    },

    /**
     * Adds thousands separator to the number
     * @param nStr
     */
    addThousandSeparator: function (nStr) {
      nStr += '';
      var x = nStr.split('.');
      var x1 = x[0];
      var x2 = x.length > 1 ? '.' + x[1] : '';
      var rgx = Chililog.positiveIntegerValidatorConfig.thousandGroupingRegExp;
      while (rgx.test(x1)) {
        x1 = x1.replace(rgx, '$1' + Chililog.positiveIntegerValidatorConfig.thousandSeparator + '$2');
      }
      return x1 + x2;
    },

    /**
     * Converts a string to an integer
     *
     * @param {String} value the field value.  (Usually a String).
     * @param {SC.FormView} form The form this field belongs to. (optional)
     * @param {SC.View} view The view this value was pulled from.
     * @returns {Number} A number suitable for consumption by the app.
     */
    objectForFieldValue: function(value, form, field) {
      var ret;

      // strip out commas
      value = value.replace(Chililog.positiveIntegerValidatorConfig.thousandSeparatorRegExp, '');

      switch (SC.typeOf(value)) {
        case SC.T_STRING:
          if (value.length === 0) {
            ret = this.get('defaultValue');
          } else {
            ret = parseInt(value, 0);
          }
          break;
        case SC.T_NULL:
        case SC.T_UNDEFINED:
          ret = this.get('defaultValue');
          break;
      }
      if (isNaN(value)) {
        ret = this.get('defaultValue');
      }
      return ret;
    },

    /**
     * Invoked just before the form is submitted.
     *
     * @param {SC.FormView} form the form for the field
     * @param {SC.View} field the field to validate
     * @returns SC.VALIDATE_OK or an error object.
     */
    validateSubmit: function(form, field) {
      var value = field.get('fieldValue');

      var requiredErrorMessage = this.get('requiredFieldErrorMessage');
      if (SC.empty(value)) {
        if (SC.none(requiredErrorMessage)) {
          return SC.VALIDATE_OK;
        } else {
          if (requiredErrorMessage.indexOf('_') === 0) {
            requiredErrorMessage = requiredErrorMessage.loc();
          }
          return Chililog.$error(requiredErrorMessage, [value], field);
        }
      }

      return SC.VALIDATE_OK;
    },

    /**
     Allow only numbers
     */
    validateKeyDown: function(form, field, charStr) {
      if (charStr.length === 0) {
        return true;
      }
      var result = charStr.match(Chililog.positiveIntegerValidatorConfig.keyDownRegExp);
      return !SC.none(result);
    }
  });

/**
 * Singleton cache of regular expression and other settings
 */
Chililog.positiveIntegerValidatorConfig = {

  keyDownRegExp: /^[0-9\0]$/,

  thousandSeparator: '_thousandSeparator'.loc(),

  thousandSeparatorRegExp: new RegExp('_thousandSeparator'.loc(), 'g'),

  thousandGroupingRegExp: /(\d+)(\d{3})/
};

/**
 * Handles parsing and validating of user specified regular expressions.
 * Assumes a string object
 */
Chililog.RegExpValidator = SC.Validator.extend({

    /**
     * Regular expression pattern to use for checking a character on keyDown
     *
     * @property
     * @type RegExp
     * @default null No keyDown checking is performed
     */
    keyDownRegExp: null,

    /**
     * Regular expression pattern to use for checking the entire field
     *
     * @property
     * @type RegExp
     * @default null no field validation is performed
     */
    fieldRegExp: null,

    /**
     * If the field is required, then set this error message
     *
     * @property
     * @type Boolean
     * @default null Field is not required.
     */
    requiredFieldErrorMessage: null,

    /**
     * Message to use in errors
     *
     * @property
     * @type String "Data Item Name" is invalid
     */
    invalidFieldErrorMessage: '"%@" is invalid.',

    /**
     * No conversion required because field and object are assumed to be of type string
     *
     * @param {String} object The object to transform
     * @param {SC.FormView} form The form this field belongs to. (optional)
     * @param {SC.View} view The view the value is required for.
     * @returns {String} string that was passed in
     */
    fieldValueForObject: function(object, form, field) {
      return object;
    },

    /**
     * No conversion required because field and object are assumed to be of type string
     *
     * @param {String} value the field value.  (Usually a String).
     * @param {SC.FormView} form The form this field belongs to. (optional)
     * @param {SC.View} view The view this value was pulled from.
     * @returns {String} string that was passed in
     */
    objectForFieldValue: function(value, form, field) {
      return value;
    },

    /**
     * Invoked just before the form is submitted.
     *
     * @param {SC.FormView} form the form for the field
     * @param {SC.View} field the field to validate
     * @returns SC.VALIDATE_OK or an error object.
     */
    validateSubmit: function(form, field) {
      var value = field.get('fieldValue');

      var requiredErrorMessage = this.get('requiredFieldErrorMessage');
      if (SC.empty(value)) {
        if (SC.none(requiredErrorMessage)) {
          return SC.VALIDATE_OK;
        } else {
          if (requiredErrorMessage.indexOf('_') === 0) {
            requiredErrorMessage = requiredErrorMessage.loc(value);
          }
          return Chililog.$error(requiredErrorMessage, [value], field);
        }
      }

      var regexp = this.get('fieldRegExp');
      var result = value.match(regexp);
      if (!result) {
        var invalidErrorMessage = this.get('invalidFieldErrorMessage');
        if (invalidErrorMessage.indexOf('_') === 0) {
          invalidErrorMessage = invalidErrorMessage.loc(value);
        }
        return Chililog.$error(invalidErrorMessage, [value], field);
      }

      return SC.VALIDATE_OK;
    },

    /**
     * Validate when the user presses a key
     */
    validateKeyDown: function(form, field, charStr) {
      if (charStr.length === 0) {
        return YES;
      }

      var regexp = this.get('keyDownRegExp');
      if (SC.none(regexp)) {
        return YES;
      }

      var result = charStr.match(regexp);
      return !SC.none(result);
    }
  });

/**
 * Handles parsing and validating of email addresses
 * Assumes a string object
 */
Chililog.EmailAddressValidator = Chililog.RegExpValidator.extend({

  keyDownRegExp: /[\w\!\#$\%\&\'\*\+\-\/\=\?\^\`{\|\}\~@\.\:]/i,

  // Good enough algorithm from. Perfect match is too slow.
  // http://fightingforalostcause.net/misc/2006/compare-email-regex.php
  fieldRegExp: /^([\w\!\#$\%\&\'\*\+\-\/\=\?\^\`{\|\}\~]+\.)*[\w\!\#$\%\&\'\*\+\-\/\=\?\^\`{\|\}\~]+@((((([a-z0-9]{1}[a-z0-9\-]{0,62}[a-z0-9]{1})|[a-z])\.)+[a-z]{2,6})|(\d{1,3}\.){3}\d{1,3}(\:\d{1,5})?)$/i,

  requiredFieldErrorMessage: null,

  invalidFieldErrorMessage: 'Email Address "%@" is invalid.'
});

/**
 * Handles validating of required string fields with no format checks
 * Assumes a string
 */
Chililog.NotEmptyValidator = SC.Validator.extend({
    /**
     * If the field is required, then set this error message
     *
     * @property
     * @type Boolean
     * @default null Field is not required.
     */
    requiredFieldErrorMessage: null,

    /**
     * No conversion required because field and object are assumed to be of type string
     *
     * @param {String} object The object to transform
     * @param {SC.FormView} form The form this field belongs to. (optional)
     * @param {SC.View} view The view the value is required for.
     * @returns {String} string that was passed in
     */
    fieldValueForObject: function(object, form, field) {
      return object;
    },

    /**
     * No conversion required because field and object are assumed to be of type string
     *
     * @param {String} value the field value.  (Usually a String).
     * @param {SC.FormView} form The form this field belongs to. (optional)
     * @param {SC.View} view The view this value was pulled from.
     * @returns {String} string that was passed in
     */
    objectForFieldValue: function(value, form, field) {
      return value;
    },

    /**
     * Invoked just before the form is submitted.
     *
     * @param {SC.FormView} form the form for the field
     * @param {SC.View} field the field to validate
     * @returns SC.VALIDATE_OK or an error object.
     */
    validateSubmit: function(form, field) {
      var value = field.get('fieldValue');

      var requiredErrorMessage = this.get('requiredFieldErrorMessage');
      if (SC.empty(value)) {
        if (SC.none(requiredErrorMessage)) {
          return SC.VALIDATE_OK;
        } else {
          if (requiredErrorMessage.indexOf('_') === 0) {
            requiredErrorMessage = requiredErrorMessage.loc(value);
          }
          return Chililog.$error(requiredErrorMessage, [ value ], field);
        }
      }

      return SC.VALIDATE_OK;
    }
  });


/**
 * Handles parsing and validating of date and time values.
 * Value stored as string to stop funny behaviour with delete and backspace when editing field due to
 * parsed values being displayed
 */
Chililog.DateTimeValidator = SC.Validator.extend({

    /**
     * Key press validation
     */
    keyDownRegExp: /^[0-9\-\0]$/,

    /**
     * Default Value to be displayed. If the value in the text field is null,
     * undefined or an empty string, it will be replaced by this value.
     *
     * @property
     * @type SC.DateTime
     * @default null
     */
    defaultValue: null,

    /**
     * If the field is required, then set this error message
     *
     * @property
     * @type String
     * @default null Field is not required.
     */
    requiredFieldErrorMessage: null,

    /**
     * Message to use in errors
     *
     * @property
     * @type String "Data Item Name" is invalid
     */
    invalidFieldErrorMessage: '"%@" is invalid.',

    /**
     * Date Format string
     */
    format: '%Y-%m-%d',

    /**
     * Converts SC.DateTime to string for display
     *
     * @param {Number} object The object to transform
     * @param {SC.FormView} form The form this field belongs to. (optional)
     * @param {SC.View} view The view the value is required for.
     * @returns {Number} a string suitable for display
     */
    fieldValueForObject: function(object, form, field) {
      return object;
    },

    /**
     * Converts a string to an integer
     *
     * @param {String} value the field value.  (Usually a String).
     * @param {SC.FormView} form The form this field belongs to. (optional)
     * @param {SC.View} view The view this value was pulled from.
     * @returns {Number} A number suitable for consumption by the app.
     */
    objectForFieldValue: function(value, form, field) {
      return value;
    },

    /**
     * Invoked just before the form is submitted.
     *
     * @param {SC.FormView} form the form for the field
     * @param {SC.View} field the field to validate
     * @returns SC.VALIDATE_OK or an error object.
     */
    validateSubmit: function(form, field) {
      var value = field.$input().val();

      var requiredErrorMessage = this.get('requiredFieldErrorMessage');
      if (SC.empty(value) && !SC.none(requiredErrorMessage)) {
        if (requiredErrorMessage.indexOf('_') === 0) {
          requiredErrorMessage = requiredErrorMessage.loc(value);
        }
        return Chililog.$error(requiredErrorMessage, [ value ], field);
      }

      if (!SC.empty(value)) {
        var result = SC.DateTime.parse(value, this.get('format'));
        if (SC.none(result)) {
          var invalidErrorMessage = this.get('invalidFieldErrorMessage');
          if (invalidErrorMessage.indexOf('_') === 0) {
            invalidErrorMessage = invalidErrorMessage.loc(value);
          }
          return Chililog.$error(invalidErrorMessage, [ value ], field);
        }
      }

      return SC.VALIDATE_OK;
    },

    /**
     Allow only valid keys
     */
    validateKeyDown: function(form, field, charStr) {
      var regexp = this.get('keyDownRegExp');
      if (charStr.length === 0 || SC.none(regexp)) {
        return true;
      }
      var result = charStr.match(regexp);
      return !SC.none(result);
    }
        
  });


/**
 * Handles checking if a string is valid JSON or not
 */
Chililog.JsonValidator = SC.Validator.extend({

    /**
     * Default Value to be displayed. If the value in the text field is null,
     * undefined or an empty string, it will be replaced by this value.
     *
     * @property
     * @type SC.DateTime
     * @default null
     */
    defaultValue: null,

    /**
     * If the field is required, then set this error message
     *
     * @property
     * @type String
     * @default null Field is not required.
     */
    requiredFieldErrorMessage: null,

    /**
     * Message to use in errors
     *
     * @property
     * @type String "Data Item Name" is invalid
     */
    invalidFieldErrorMessage: '"%@" is invalid.',

    /**
     * Converts SC.DateTime to string for display
     *
     * @param {Number} object The object to transform
     * @param {SC.FormView} form The form this field belongs to. (optional)
     * @param {SC.View} view The view the value is required for.
     * @returns {Number} a string suitable for display
     */
    fieldValueForObject: function(object, form, field) {
      return object;
    },

    /**
     * Converts a string to an integer
     *
     * @param {String} value the field value.  (Usually a String).
     * @param {SC.FormView} form The form this field belongs to. (optional)
     * @param {SC.View} view The view this value was pulled from.
     * @returns {Number} A number suitable for consumption by the app.
     */
    objectForFieldValue: function(value, form, field) {
      return value;
    },

    /**
     * Invoked just before the form is submitted.
     *
     * @param {SC.FormView} form the form for the field
     * @param {SC.View} field the field to validate
     * @returns SC.VALIDATE_OK or an error object.
     */
    validateSubmit: function(form, field) {
      var value = field.$input().val();

      var requiredErrorMessage = this.get('requiredFieldErrorMessage');
      if (SC.empty(value) && !SC.none(requiredErrorMessage)) {
        if (requiredErrorMessage.indexOf('_') === 0) {
          requiredErrorMessage = requiredErrorMessage.loc(value);
        }
        return Chililog.$error(requiredErrorMessage, [ value ], field);
      }

      if (!SC.empty(value)) {
        try {
        var result = SC.json.decode(value);
        }
        catch (err) {
          var invalidErrorMessage = this.get('invalidFieldErrorMessage');
          if (invalidErrorMessage.indexOf('_') === 0) {
            invalidErrorMessage = invalidErrorMessage.loc(value);
          }
          return Chililog.$error(invalidErrorMessage, [ value ], field);
        }
      }

      return SC.VALIDATE_OK;
    }

  });
