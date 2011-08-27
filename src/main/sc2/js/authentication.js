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
// Perform authentication at the top of the page before we do start up processing
// If not logged in, don't waste time loading all sorts of javascripts and rendering the page
//
Auth = {};

Auth.getPageName = function() {
  var path = window.location.pathname;
  var pageName = path.substring(path.lastIndexOf('/') + 1);
  return pageName;
}

Auth.parseISO8601 = function(str) {
  // we assume str is a UTC date ending in 'Z'
  var parts = str.split('T'),
  dateParts = parts[0].split('-'),
  timeParts = parts[1].split('Z'),
  timeSubParts = timeParts[0].split(':'),
  timeSecParts = timeSubParts[2].split('.'),
  timeHours = Number(timeSubParts[0]),
  _date = new Date;

  _date.setUTCFullYear(Number(dateParts[0]));
  _date.setUTCMonth(Number(dateParts[1])-1);
  _date.setUTCDate(Number(dateParts[2]));
  _date.setUTCHours(Number(timeHours));
  _date.setUTCMinutes(Number(timeSubParts[1]));
  _date.setUTCSeconds(Number(timeSecParts[0]));
  if (timeSecParts[1]) _date.setUTCMilliseconds(Number(timeSecParts[1]));

  // by using setUTC methods the date has already been converted to local time(?)
  return _date;
};

Auth.checkToken =  function() {
  // Get token from local store
  var token = localStorage.getItem('App.AuthenticationToken');
  if (token == null || token == '') {
    return false;
  }
  // Decode token
  var delimiterIndex = token.indexOf('~~~');
  if (delimiterIndex < 0) {
    return false;
  }
  var jsonString = token.substr(0, delimiterIndex);
  var json = JSON.parse(jsonString);
  if (json == null || json == '') {
    return false;
  }

  var expiryString = json.ExpiresOn;
  if (expiryString === null) {
    return false;
  }

  var expiryDate = Auth.parseISO8601(expiryString);
  if (new Date().getTime() > expiryDate.getTime()) {
    return false;
  }

  return true;
}

Auth.checkIfLoggedIn = function () {
  if (!Auth.checkToken()) {
    window.location = 'login.html?returnTo=' + encodeURIComponent(Auth.getPageName());
  }
}

