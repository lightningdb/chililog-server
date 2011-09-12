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

/**
 * @class
 * Common functions for profile field data
 */
App.BlockMessageView = SC.View.extend({
  classNames: 'alert-message block-message'.w(),

  /**
   * Template is just the message
   * @Type SC.Handlebars
   */
  defaultTemplate: SC.Handlebars.compile('{{message}}'),

  /**
   * Message text to display
   * @Type String
   */
  message: '',

  /**
   * Type of message - 'success' or 'error'
   * @Type String
   */
  messageType: 'success',

  /**
   * Flag to indicate if this view is visible or not
   * @Type Boolean
   */
  isVisible: NO,

  /**
   * Set the class when adding the DOM element
   */
  willInsertElement: function() {
    this.$().addClass(this.get('messageType'));
  },

  /**
   * Updates the message when changed. Call this when the message property you are observing changes
   *
   *     messageDidChange: function() {
   *       var msg = App.pageController.get(messagePropertyName);
   *       this._updateMessage(msg);
   *     }.observes('App.pageController.profileSuccessMessage')
   *
   * @param {String} messagePropertyName name of property in the App.pageController where the message to display is stored
   */
  _updateMessage: function(msg) {
    var isEmpty = SC.empty(msg);
    this.set('isVisible', !isEmpty);
    this.set('message', msg);
  }
});


/**
 * @class
 * Common functions for profile field data
 */
App.InlineMessageView = SC.View.extend({
  classNames: 'alert-message block-message inline'.w(),

  /**
   * Template is just the message
   * @Type SC.Handlebars
   */
  defaultTemplate: SC.Handlebars.compile('{{message}}'),

  /**
   * Message text to display
   * @Type String
   */
  message: '',

  /**
   * Type of message - 'success' or 'error'
   * @Type String
   */
  messageType: 'success',

  /**
   * Flash the message and fade when message is displayed
   */
  highlightAndFade: NO,

  /**
   * Flag to indicate if this view is visible or not
   * @Type Boolean
   */
  isVisible: NO,

  /**
   * Set the class when adding the DOM element
   */
  willInsertElement: function() {
    this.$().addClass(this.get('messageType'));
  },

  /**
   * Updates the message when changed. Call this when the message property you are observing changes
   *
   *     messageDidChange: function() {
   *       var msg = App.pageController.get(messagePropertyName);
   *       this._updateMessage(msg);
   *     }.observes('App.pageController.profileSuccessMessage')
   *
   * @param {String} messagePropertyName name of property in the App.pageController where the message to display is stored
   */
  _updateMessage: function(msg) {
    var isEmpty = SC.empty(msg);
    this.set('isVisible', !isEmpty);
    this.set('message', msg);

    if (!isEmpty && this.get('highlightAndFade')) {
      this.doHighlightAndFade();
    }
  },

  /**
   * Use JQuery UI to flash message to user and then fade out from display
   */
  doHighlightAndFade: function() {
    var domElement = this.$();
    domElement.stop().show();
    for (var i=0; i < 3; i++) {
      domElement.effect('highlight', { color : 'gold'}, 100);
    }
    domElement.delay(3000).fadeOut(3000);
  }
});

/**
 * @class
 * Defines a fields where label and data are on the same line
 */
App.FieldView = SC.View.extend({
  classNames: 'field clearfix'.w(),

  /**
   * Template is just the message
   * @Type SC.Handlebars
   */
  defaultTemplate: SC.Handlebars.compile('{{view LabelView}}<div class="input">{{view DataView}}<span class="help-inline">{{help}}</span></div>'),

  /**
   * Label to display to let the user know what this field is about
   * @type String
   */
  label: '',

  /**
   * Flag indicating if the field is a required field. If so, an '*' is placed in the label text
   * @type Boolean
   */
  isRequired: NO,

  /**
   * Help text
   * @type String
   */
  help: '',

  /**
   * Show the specified message to the user
   * @param msg Message to display to the user
   * @param {Boolean} isError YES if this is an error message, NO if not.
   */
  _updateHelp: function(msg, isError) {
    this.set('help', msg);
    this.$().removeClass('error');
    if (isError && !SC.empty(msg)) {
      this.$().addClass('error');
    }
  },

  /**
   * 
   */
  LabelView: SC.View.extend({
    tagName: 'label',

    attributeBindings: ['for'],

    /**
     * ID of data element
     * @type String
     */
    'for': '',

    /**
     * Text to display the user
     * @type String
     */
    textBinding: 'parentView.label',

    /**
     * The required symbol
     * @type String
     */
    required: function() {
      return (this.getPath('parentView.isRequired')) ? '*' : '';
    }.property('parentView.isRequired').cacheable(),

    /**
     * Handlebar template for this label
     * @type SC.Handlebars
     */
    defaultTemplate: SC.Handlebars.compile('{{text}}{{required}}')
  }),

  /**
   * Class representing the data capture control. Must be defined by the child class.
   * @type SC.View
   */
  DataView: null,

  /**
   * Set the 'for' attribute for the label to that of the data view
   */
  willInsertElement: function() {
    this._super();

    var childViews = this.get('childViews');
    var labelView = childViews[0];
    var dataView = childViews[1];
    labelView.set('for', dataView.$().attr('id'));
  }

});


/**
 * @class
 * Defines a fields where label is on top of the data
 */
App.StackedFieldView = SC.View.extend({
  classNames: 'field floating'.w(),

  /**
   * Template is just the message
   * @Type SC.Handlebars
   */
  defaultTemplate: SC.Handlebars.compile('{{view LabelView}}{{view DataView}}'),

  /**
   * Label to display to let the user know what this field is about
   * @type String
   */
  label: '',

  /**
   * Flag indicating if the field is a required field. If so, an '*' is placed in the label text
   * @type Boolean
   */
  isRequired: NO,

  /**
   *
   */
  LabelView: SC.View.extend({
    tagName: 'label',

    attributeBindings: ['for'],

    /**
     * ID of data element
     * @type String
     */
    'for': '',

    /**
     * Text to display the user
     * @type String
     */
    textBinding: 'parentView.label',

    /**
     * The required symbol
     * @type String
     */
    required: function() {
      return (this.getPath('parentView.isRequired')) ? '*' : '';
    }.property('parentView.isRequired').cacheable(),

    /**
     * Handlebar template for this label
     * @type SC.Handlebars
     */
    defaultTemplate: SC.Handlebars.compile('{{text}}{{required}}')
  }),

  /**
   * Class representing the data capture control. To be defined by the child class
   * @type SC.View
   */
  DataView: null,

  /**
   * Set the 'for' attribute for the label to that of the data view
   */
  willInsertElement: function() {
    this._super();

    var childViews = this.get('childViews');
    var labelView = childViews[0];
    var dataView = childViews[1];
    labelView.set('for', dataView.$().attr('id'));
  }

});

// --------------------------------------------------------------------------------------------------------------------
// Validators
// --------------------------------------------------------------------------------------------------------------------
App.viewValidators = {

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
    var result = App.viewValidators.emailAddressRegExp.test(emailAddressToCheck);
    return result;
  }

};


// --------------------------------------------------------------------------------------------------------------------
// Utility methods
// --------------------------------------------------------------------------------------------------------------------
App.viewUtils = {

  setupStandardPage: function(pageFileName) {
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
};

