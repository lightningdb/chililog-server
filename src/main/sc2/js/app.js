//
// Copyright 2010 Cinch Logic Pty Ltd.
//
// http://www.App.com
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
// Contains common code applicable for all HTML pages
//

// --------------------------------------------------------------------------------------------------------------------
// Instance our app
// --------------------------------------------------------------------------------------------------------------------
var App = SC.Application.create({
  NAMESPACE: 'App',
  VERSION: '00.00.02-ALHPA',
  COPYRIGHT: '&copy; 2011 Cinch Logic.'
});


// --------------------------------------------------------------------------------------------------------------------
// Globals
// --------------------------------------------------------------------------------------------------------------------
/**
 * Shortcut for creating an SC.Error object using the errorCode as the lookup for localised error messages
 *
 * @param {String} errorCode Unique error code that doubles up as the code for the localised error message
 * @param {Array} [params] Optional array of parameter substitutes for the localised string
 * @param {String} [errorFieldId] Optional id of field where the error occurred.
 * @returns {SC.Error} Error object
 */
App.$error = function(errorCode, params, errorFieldId) {
  // Get localized string
  var localized = SC.String.loc(errorCode, params);
  var message = SC.empty(localized) ? errorCode : localized;

  if (!SC.none(params)) {
    message = SC.String.fmt(localized, params);
  }

  var err = { isAppError: true, message: message };
  if (!SC.none(errorCode) && errorCode.charAt(0) === '_') {
    err.errorCode = errorCode;
  }
  if (!SC.none(errorFieldId)) {
    err.errorFieldId = errorFieldId;
  }

  return err;
};

/**
 * Returns the querystring as name-value hash object
 * @return {object} Hash name-values for the query string
 *
 * @example
 * var qs = App.getQueryStringHash();
 * var value = qs['name'];
 */
App.getQueryStringHash = function() {
  var qsObj = {};

  var idx = window.location.href.indexOf('?');
  if (idx >= 0) {
    var qs = window.location.href.slice(idx + 1).split('&');
    for (var i = 0; i < qs.length; i++) {
      var nvArray = qs[i].split('=');
      var name = nvArray[0];
      var value = nvArray[1];
      qsObj[name] = value;
    }
  }

  return qsObj;
}

/**
 * @class
 * Format our date and times
 */
App.DateTime = {
  /**
   * Parses the timestamp returned by the server
   *
   * @returns {SC.DateTime} timestamp in the local
   */
  parseChililogServerDateTime: function(s) {
    var d = SC.DateTime.parse(s, '%Y-%m-%dT%H:%M:%S.%s%Z');
    d.set('timezone', new Date().getTimezoneOffset());
    return d;
  },

  /**
   * Returns the timestamp as a string in the local format
   * @param {SC.DateTime} date to format
   * @returns {String} timestamp in '2011-02-20 23:22:11.123 +10:00' format
   */
  toChililogLocalDateTime: function(d) {
    return d.toFormattedString('%Y-%m-%d %H:%M:%S.%s %Z');
  },

  /**
   * Returns the timestamp as a string in the format '2011-08-02T01:02:03Z' as required by chililog server
   * @param {SC.DateTime} date to format
   * @returns {String} timestamp in '2011-02-20T23:22:11.123Z' format using the UTC timezone
   */
  toChililogServerDateTime: function(d) {
    return d.toTimezone(0).toFormattedString('%Y-%m-%dT%H:%M:%S.%sZ');
  }
};

App.WebSocket = null;
if (window.WebSocket) {
  App.WebSocket = window.WebSocket;
} else if (window.MozWebSocket) {
// Firefox uses non standard naming for some reason???
  App.WebSocket = window.MozWebSocket;
}


/**
 * Extends the standard date and time object for our special formatter
 */
SC.DateTime.reopen({


});
