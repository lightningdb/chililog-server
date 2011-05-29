// ==========================================================================
// Project:   Chililog
// Copyright: ©2011 My Company, Inc.
// ==========================================================================

/**
 * Shortcut for creating an SC.Error object using the errorCode as the lookup for localised error messages
 *
 * @param {String} errorCode Unique error code that doubles up as the code for the localised error message
 * @param {Array} [params] Optional array of parameter substitutes for the localised string
 * @param {SC.View} [field] Optional field where the error occurred.
 * @returns {SC.Error} Error object
 */
Chililog.$error = function(errorCode, params, field) {
  // Get localized string
  // Copied from SC.String.loc() because that function did a fmt with arguments and we need to pass in array
  if (!SC.Locale.currentLocale) {
    SC.Locale.createCurrentLocale();
  }
  var localized = SC.Locale.currentLocale.locWithDefault(errorCode);
  if (SC.typeOf(localized) !== SC.T_STRING) {
    localized = str;
  }

  var message = localized;
  if (!SC.none(params)) {
    message = SC.String.fmt(localized, params);
  }

  var err = SC.$error(message, null, null, errorCode);
  if (!SC.none(field)) {
    err.set('errorField', field);
  }
  
  return err;
};

/**
 * Extends the standard date and time object for our special formatter
 * 
 * @param {SC.DateTime} dt
 */
SC.DateTime.reopen({

  toChililogServerDateTime: function() {
    return this.toTimezone(0).toFormattedString('%Y-%m-%dT%H:%M:%SZ');
  }

});

/**
 * @private Cache for if we are running tests or not
 */
Chililog._isRunningTests = null;

/**
 * Flag to indicate if we are running tests or not. This is triggered by a url hash containing 'test'
 * http://localhost:4020/chililog/en/current/tests/controllers/session_data_controller.html#test
 * @type Boolean
 */
Chililog.isRunningTests = function() {
  if (Chililog._isRunningTests == null) {
    Chililog._isRunningTests = window.location.hash.toString().match('test');
  }
  return Chililog._isRunningTests;
};



