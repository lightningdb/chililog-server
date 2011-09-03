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
  attributeBindings: ['type', 'placeholder', 'value', 'name', 'tabindex', 'disabled'],

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
  disabled: NO
});

/** @Class
 *
 * Our own text area supports additional attributes on the textarea
 */
App.TextAreaView = SC.TextArea.extend({
  /**
   * Specify additional attributes
   */
  attributeBindings: ['placeholder', 'value', 'name', 'tabindex', 'disabled'],

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
  disabled: NO
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
  attributeBindings: ['type', 'disabled'],

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
   * Flag to indicate if this is disabled or not
   */
  disabled: NO,

  /**
   * Added a space in between the title and checkbox so that there is a gap
   */
  defaultTemplate: SC.Handlebars.compile('<label><input type="checkbox" {{bindAttr checked="value" disabled="disabled"}} > {{title}}</label>')

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
  attributeBindings: ['multiple', 'disabled'],

  itemViewClass: App.SelectOption,

  /**
   * Content must be an array or arrayproxy of SC objects
   */
  content: [],

  /**
   * Flag to indicate if this is disabled or not
   */
  disabled: NO,

  /**
   * The selected option.
   */
  selectedOption: null,

  /**
   * On insert into the DOM, set the selected item
   */
  willInsertElement: function() {
    this._elementValueDidChange();
  },

  /**
   * The browser's onChange event means that we have to take data from the DOM to update our contents
   */
  change: function() {
    this._elementValueDidChange();
  },

  /**
   * Get data from DOM to update our contents
   */
  _elementValueDidChange: function() {
    var views = SC.View.views,
      selectedOptions = this.$('option:selected'),
      option = null;

    if (selectedOptions) {
      var id = selectedOptions.prop('id');
      if (SC.none(id)) {
        // Get the first option if there is one
        if (this.get('content').get) {
          option = this.get('content').get('firstObject');
        }
      } else {
        option = SC.get(views[selectedOptions.prop('id')], 'content');
      }
    }

    set(this, 'selectedOption', option);
    set(SC.get(this, 'content'), 'selection', option);
  },

  /**
   * Contents array changed in size
   * @param content
   * @param start
   * @param removed
   */
  arrayWillChange: function(content, start, removed) {
    var selected = SC.get(content, 'selection'), idx, obj;

    if (content && removed) {
      for (idx = start; idx < start + removed; idx++) {
        obj = content.objectAt(idx);

        if (selected && selected.contains && selected.contains(obj)) {
          selected.removeObject(obj);
        }
      }
    }

    this._super(content, start, removed);
  },

  /**
   * Contents array changed in size
   * @param content
   * @param start
   * @param removed
   * @param added
   */
  arrayDidChange: function(content, start, removed, added) {
    this._super(content, start, removed, added);
    this._elementValueDidChange();
  },

  /**
   * The content data has changed so we need to update the DOM
   */
  selectedOptionDidChange: function() {
    var selectedOption = this.get('selectedOption');
    if (SC.none(selectedOption)) {
      return;
    }

    var selectedValue = selectedOption.get('value');
    set(SC.get(this, 'content'), 'selection', selectedOption);

    // Make sure that the DOM reflects the selected value
    var options = this.$('option');
    if (!SC.none(options)) {
      for (var i=0; i< options.length; i++) {
        var option = options[i];
        option.selected = (option.value === selectedValue);
      }
    }
  }.observes('selectedOption')
});

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

  // User
  var loggedInUser = App.sessionEngine.get('loggedInUser');
  $('#navUsername').append(loggedInUser.get('displayNameOrUsername') + "&nbsp;");
}