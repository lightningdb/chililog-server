// ==========================================================================
// Project:   SproutCore - JavaScript Application Framework
// Copyright: ©2006-2009 Sprout Systems, Inc. and contributors.
//            Portions ©2008-2009 Apple Inc. All rights reserved.
// License:   Licened under MIT license (see license.js)
// ==========================================================================
/*globals SC Endash */

/**
  @class

  @extends SC.View
  @author Christopher Swasey
*/
Endash.ThumbView = SC.View.extend(

/** @scope SC.ThumbView.prototype */ {

  classNames: ['sc-thumb-view'],
  
  isEnabled: YES,
  isEnabledBindingDefault: SC.Binding.bool(),
  
  delegate: null,  
  
  render: function(context, firstTime) {
    if(firstTime)
    {
      context.begin('div').classNames(["dragger"]).end();
    }
  }

});