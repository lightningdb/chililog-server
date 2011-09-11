//
// Copyright 2010 Cinch Logic Pty Ltd.
//
// http://www.chililog.com
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

// --------------------------------------------------------------------------------------------------------------------
// Chililog Controls
// --------------------------------------------------------------------------------------------------------------------

/** @Class
 *
 * Our own text box supports additional attributes on the textbox
 */
App.TextBoxView = SC.TextField.extend({
  /**
   * Specify additional attributes
   */
  attributeBindings: ['type', 'placeholder', 'value', 'name', 'tabindex', 'disabled', 'readonly'],

  /**
   * Name of the text box
   */
  name: '',

  /**
   * Tabindex
   */
  tabindex: '1',

  /**
   * Flag to indicate if this is disabled or not
   */
  disabled: NO,

  /**
   * Read only text box
   */
  readonly: NO
});

/** @Class
 *
 * Our own text area supports additional attributes on the textarea
 */
App.TextAreaView = SC.TextArea.extend({
  /**
   * Specify additional attributes
   */
  attributeBindings: ['placeholder', 'value', 'name', 'tabindex', 'disabled', 'readonly'],

  /**
   * Name of the text box
   */
  name: '',

  /**
   * Tabindex
   */
  tabindex: '1',

  /**
   * Flag to indicate if this is disabled or not
   */
  disabled: NO,
  
  /**
   * Read only text box
   */
  readonly: NO
});

/**  @Class
 *
 * Our own image tag
 */
App.ImgView = SC.View.extend({
  tagName: 'img',

  attributeBindings: ['src', 'alt'],

  /**
   * URL to the image
   */
  src: '',

  /**
   * Alternate text
   */
  alt: ' '
});

/**  @Class
 *
 * Our own simple button view so that the label will be set as the button text
 */
App.ButtonView = SC.Button.extend({

  /**
   * Added disabled attribute binding
   */
  attributeBindings: ['type', 'disabled', 'tabindex', 'title'],

  /**
   * Tooltip
   */
  title: '',

  /**
   * Tabindex
   */
  tabindex: '1',

  /**
   * Text for the button
   */
  label: '',
  
  /**
   * Flag to indicate if this is disabled or not
   */
  disabled: NO,

  /**
    @private
  */
  willInsertElement: function() {
    this._super();
    this._updateElementValue();
  },

  _updateElementValue: function() {
    this.$().html(this.get('label'));
  }.observes('label')
});

/** @class
 *
 * Checkbox view control
 */
App.CheckboxView = SC.Checkbox.extend({

  /**
   * Tabindex
   */
  tabindex: '1',

  /**
   * Flag to indicate if this is disabled or not
   */
  disabled: NO,

  /**
   * Added a space in between the title and checkbox so that there is a gap
   */
  defaultTemplate: SC.Handlebars.compile('<label><input type="checkbox" {{bindAttr checked="value" disabled="disabled" tabindex="tabindex"}} > {{title}}</label>'),

  keyUp: function(event) {
    this.interpretKeyEvents(event);
  },

  /**
    @private
  */
  interpretKeyEvents: function(event) {
    var map = SC.TextField.KEY_EVENTS;
    var method = map[event.keyCode];

    if (method) { return this[method](event); }
    else { SC.run.once(this, this._updateElementValue); }
  }

});

/** @class
 *
 * Select control that is waiting to be pulled
 * Thanks to https://github.com/ebryn/sproutcore20
 */
App.SelectOption = SC.View.extend({
  tagName: 'option',
  classNames: ['sc-select-option'],

  /*
    Note: we can't use a template with {{label}} here because it
    uses a BindableSpan. The browser will eat the span inside of
    an option tag.
  */
  template: function(context, options) {
    options.data.buffer.push(context.get('label'));
  },
  attributeBindings: ['value', 'selected'],

  labelBinding: '*content.label',
  valueBinding: '*content.value',
  selectedBinding: '*content.selected',

  _labelDidChange: function() {
    this.rerender();
  }.observes('label')
});

/** @class
 *
 * Select control that is waiting to be pulled
 * Thanks to https://github.com/ebryn/sproutcore20
 */
App.SelectView = SC.CollectionView.extend({
  tagName: 'select',
  classNames: ['sc-select'],
  attributeBindings: ['multiple', 'disabled', 'tabindex'],

  /**
   * Tabindex
   */
  tabindex: '1',

  disabled: NO,
  
  itemViewClass: App.SelectOption,

  _value: null,

  value: function(key, value) {
    if (value !== undefined) {
      set(this, '_value', value);

      get(this, 'childViews').forEach(function(el, idx) {
        var content = get(el, 'content');

        if (content === value) {
          set(content, 'selected', true);
        } else {
          set(content, 'selected', false);
        }
      });
    }

    return get(this, '_value');
  }.property('_value'),

  willInsertElement: function() {
    this._elementValueDidChange();
  },

  change: function() {
    this._elementValueDidChange();
  },

  _elementValueDidChange: function() {
    var views = SC.View.views,
        selectedOptions = this.$('option:selected'),
        value;

    if (get(this, 'multiple') && get(this, 'multiple') !== "false") {
      value = selectedOptions.toArray().map(function(el) { return get(views[el.id], 'content'); });
    } else {
      var optionView = views[selectedOptions.prop('id')];
      if (!SC.none(optionView)) {
        value = get(optionView, 'content');
      }
    }

    set(this, 'value', value);
    set(get(this, 'content'), 'selection', value);
  },

  arrayWillChange: function(content, start, removed) {
    var selected, idx, obj;

    if (content && removed) {
      for (idx = start; idx < start+removed; idx++) {
        obj = content.objectAt(idx);

        if (selected = get(content, 'selection')) {
          if (SC.isArray(selected) && selected.contains(obj)) {
            selected.removeObject(obj);
          } else if (selected === obj) {
            set(content, 'selection', null);
          }
        }
      }
    }

    this._super(content, start, removed);
  }
});

// --------------------------------------------------------------------------------------------------------------------
// Validators
// --------------------------------------------------------------------------------------------------------------------
App.validators = {

  /**
   * Email address regular expression check
   * Good enough algorithm from. Perfect match is too slow.
   * http://fightingforalostcause.net/misc/2006/compare-email-regex.php
   * @type RegExp
   */
  emailAddressRegExp: /^([\w\!\#$\%\&\'\*\+\-\/\=\?\^\`{\|\}\~]+\.)*[\w\!\#$\%\&\'\*\+\-\/\=\?\^\`{\|\}\~]+@((((([a-z0-9]{1}[a-z0-9\-]{0,62}[a-z0-9]{1})|[a-z])\.)+[a-z]{2,6})|(\d{1,3}\.){3}\d{1,3}(\:\d{1,5})?)$/i,

  /**
   * Checks if an email address is valid
   * @param emailAddressToCheck
   * @returns YES if valid, NO if not valid
   */
  checkEmailAddress: function (emailAddressToCheck) {
    var result = App.validators.emailAddressRegExp.test(emailAddressToCheck);
    return result;
  }

};


// --------------------------------------------------------------------------------------------------------------------
// Utility methods
// --------------------------------------------------------------------------------------------------------------------



/**
 * Setup the fields on an authenticated page
 */
App.setupStandardPage = function(pageFileName) {
  // Selected nav
  if (pageFileName === 'stream.html') {
    $('#navStream').addClass('selected');
  } else if (pageFileName === 'search.html') {
    $('#navSearch').addClass('selected');
  } else if (pageFileName.indexOf('admin') === 0) {
    $('#navAdmin').addClass('selected');
  }

  // Setup User
  var loggedInUser = App.sessionEngine.get('loggedInUser');
  $('#navUsername').append(loggedInUser.get('displayNameOrUsername') + "&nbsp;");

  // Open top bar menu when clicked
  $("a.menu").click(function (e) {
    var $li = $(this).parent("li").toggleClass('open');
    return false;
  });

  // Close top bar menu when body clicked
  $("body").bind("click", function (e) {
    $('a.menu').parent("li").removeClass("open");
  });
}