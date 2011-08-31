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
// JQuery for out headerBar menu
// --------------------------------------------------------------------------------------------------------------------
$("body").bind("click", function (e) {
  $('a.menu').parent("li").removeClass("open");
});

$("a.menu").click(function (e) {
  var $li = $(this).parent("li").toggleClass('open');
  return false;
});

// --------------------------------------------------------------------------------------------------------------------
// Instance our app
// --------------------------------------------------------------------------------------------------------------------
var App = SC.Application.create({
  NAMESPACE: 'App',
  VERSION: '1.0'
});


// --------------------------------------------------------------------------------------------------------------------
// Globals
// --------------------------------------------------------------------------------------------------------------------
/**
 * Shortcut for creating an SC.Error object using the errorCode as the lookup for localised error messages
 *
 * @param {String} errorCode Unique error code that doubles up as the code for the localised error message
 * @param {Array} [params] Optional array of parameter substitutes for the localised string
 * @param {String} [field] Optional id of field where the error occurred.
 * @returns {SC.Error} Error object
 */
App.$error = function(errorCode, params, fieldId) {
  // Get localized string
  var localized = SC.String.loc(errorCode, params);

  var message = localized;
  if (!SC.none(params)) {
    message = SC.String.fmt(localized, params);
  }

  var err = new SC.Error(message);
  if (!SC.none(errorCode) && errorCode.charAt(0) === '_') {
    err.set('errorCode', errorCode);
  }
  if (!SC.none(fieldId)) {
    err.set('errorField', fieldId);
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
 * Extends the standard date and time object for our special formatter
 */
SC.DateTime.reopen({

  /**
   * Provides timestamp like '2011-08-02T01:02:03Z'. Chililog standardises this format.
   *
   * @returns {String} timestamp in 'yyyy-MM-ddTHH:mm:SSZ' format using the UTC timezone
   */
  toChililogServerDateTime: function() {
    return this.toTimezone(0).toFormattedString('%Y-%m-%dT%H:%M:%SZ');
  }

});


