// ==========================================================================
// Project:   Chililog.ImageView Unit Test
// Copyright: ©2011 My Company, Inc.
// ==========================================================================
/*globals Chililog module test ok equals same stop start */

module("Chililog");

test("test error", function() {
  var error = Chililog.$error('_testError', ['PARAM1', 'PARAM2', 'PARAM3'], 'label', 'errorValue')

  equals(error.get('message'), 'Test Error param1=PARAM1, param2=PARAM2, param3=PARAM3', 'Error message');
  equals(error.get('code'), '_testError', 'Error code');
  equals(error.get('label'), 'label', 'Error label');
  equals(error.get('errorValue'), 'errorValue', 'Error value');
});

