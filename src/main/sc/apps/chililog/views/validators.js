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
   * Converts Number to string for display
   * 
   * @param {Number} object The object to transform
   * @param {SC.FormView} form The form this field belongs to. (optional)
   * @param {SC.View} view The view the value is required for.
   * @returns {Number} a string suitable for display
   */
  fieldValueForObject: function(object, form, field) {
    switch (SC.typeOf(object)) {
      case SC.T_NUMBER:
        object = object.toFixed(0);
        break;
      case SC.T_NULL:
      case SC.T_UNDEFINED:
        object = this.get('defaultValue');
        break;
    }
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
    // strip out commas
    value = value.replace(/,/g, '');
    switch (SC.typeOf(value)) {
      case SC.T_STRING:
        if (value.length === 0) {
          value = this.get('defaultValue');
        } else {
          value = parseInt(value, 0);
        }
        break;
      case SC.T_NULL:
      case SC.T_UNDEFINED:
        value = this.get('defaultValue');
        break;
    }
    if (isNaN(value)) {
      return this.get('defaultValue');
    }
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
    return SC.$error(SC.String.loc("Invalid.Number(%@)", label), label);
  },

  /**
   * Allow only numbers
   */
  validateKeyDown: function(form, field, charStr) {
    if (charStr.length === 0) {
      return true;
    }
    var result = charStr.match(Chililog.positiveIntegerValidatorRegExp);
    return !SC.none(result);
  }
});

/**
 * Singleton regular expression to improve performance so we don't have to parse it everytime
 */
Chililog.positiveIntegerValidatorRegExp = /^[0-9\0]$/;


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
   * Regular expression pattern to use for checking the entire field. Set to null for not checking.
   *
   * @property
   * @type RegExp
   * @default null
   */
  fieldRegExp: null,

  /**
   * Regular expression pattern to use for checking a keydown event. i.e. regexp for the valid letters or digits.
   * Set to null for no checking.
   *
   * @property
   * @type RegExp
   * @default null
   */
  keyDownRegExp: null,

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
    if (SC.empty(value)) {
      return true;
    }
    
    var regexp = this.get('fieldRegExp');
    if (SC.none(regexp)) {
      return true;
    }

    var result = value.match(regexp);
    return !SC.none(result);
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
   * Allow only regular expression keys
   */
  validateKeyDown: function(form, field, charStr) {
    if (charStr.length === 0) {
      return true;
    }

    var regexp = this.get('keyDownRegExp');
    if (SC.none(regexp)) {
      return true;
    }

    var result = charStr.match(regexp);
    return !SC.none(result);
  }
});