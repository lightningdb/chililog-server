// ==========================================================================
// Project:   Chililog.mainPaneController
// Copyright: ©2011 My Company, Inc.
// ==========================================================================

/**
 * Handles parsing and validating of positive integers.
 */
Chililog.PositiveIntegerValidator = SC.Validator.extend(
/** @scope SC.Validator.PositiveInteger.prototype */ {

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
   * Validate the field value.
   * 
   * @param {SC.FormView} form the form this view belongs to
   * @param {SC.View} field the field to validate.  Responds to fieldValue.
   * @returns {Boolean} YES if field is valid.
   */
  validate: function(form, field) {
    var value = field.get('fieldValue');
    return (value === '') || !isNaN(value);
  },

  /**
   * Returns an error object if the field is invalid.
   *
   * @param {SC.FormView} form the form this view belongs to
   * @param {SC.View} field the field to validate.  Responds to fieldValue.
   * @returns {SC.Error} an error object
   */
  validateError: function(form, field) {
    var label = field.get('errorLabel') || 'Field';
    return SC.$error(SC.String.loc("Invalid.Number(%@)", label), label);
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
Chililog.RegExpValidator = SC.Validator.extend(
/** @scope SC.Validator.PositiveInteger.prototype */ {

  /**
   * Default Value to be displayed. If the value in the text field is null,
   * undefined or an empty string, it will be replaced by this value.
   *
   * @property
   * @type String
   * @default null
   */
  defaultValue: null,

  /**
   * Regular expression pattern to use for checking
   *
   * @property
   * @type RegExp
   * @default null
   */
  regexp: null,

  /**
   * No conversion required because field and object are assumed to be of type string
   *
   * @param {Number} object The object to transform
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
   * Validate the field value.
   *
   * @param {SC.FormView} form the form this view belongs to
   * @param {SC.View} field the field to validate.  Responds to fieldValue.
   * @returns {Boolean} YES if field is valid.
   */
  validate: function(form, field) {
    var value = field.get('fieldValue');
    return (value === '') || !isNaN(value);
  },

  /**
   * Returns an error object if the field is invalid.
   *
   * @param {SC.FormView} form the form this view belongs to
   * @param {SC.View} field the field to validate.  Responds to fieldValue.
   * @returns {SC.Error} an error object
   */
  validateError: function(form, field) {
    var label = field.get('errorLabel') || 'Field';
    return SC.$error(SC.String.loc("Invalid value (%@)", label), label);
  },

  /**
   * Allow only regular
   */
  validateKeyDown: function(form, field, charStr) {
    if (charStr.length === 0) {
      return true;
    }

    var regexp = this.get('regexp');
    if (SC.none(regexp)) {
      return true;
    }

    var result = charStr.match(regexp);
    return !SC.none(result);
  }
});