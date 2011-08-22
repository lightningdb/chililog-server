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

//
// Contains common code applicable for all HTML pages
//

// --------------------------------------------------------------------------------------------------------------------
// JQuery UI Integration. Thanks to Yehuda. http://yehudakatz.com/2011/06/11/using-sproutcore-2-0-with-jquery-ui/
// --------------------------------------------------------------------------------------------------------------------
// Put jQuery UI inside its own namespace
JQ = {};

// Create a new mixin for jQuery UI widgets using the new SproutCore 2.0
// mixin syntax.
JQ.Widget = SC.Mixin.create({
  // When SproutCore creates the view's DOM element, it will call this
  // method.
  willInsertElement: function() {
    this._super();

    // Make jQuery UI options available as SproutCore properties
    var options = this._gatherOptions();

    // Make sure that jQuery UI events trigger methods on this view.
    this._gatherEvents(options);

    // Create a new instance of the jQuery UI widget based on its `uiType`
    // and the current element.
    var ui = jQuery.ui[this.get('uiType')](options, this.get('element'));

    // Save off the instance of the jQuery UI widget as the `ui` property
    // on this SproutCore view.
    this.set('ui', ui);
  },

  // When SproutCore tears down the view's DOM element, it will call
  // this method.
  willDestroyElement: function() {
    var ui = this.get('ui');

    if (ui) {
      // Tear down any observers that were created to make jQuery UI
      // options available as SproutCore properties.
      var observers = this._observers;
      for (var prop in observers) {
        if (observers.hasOwnProperty(prop)) {
          this.removeObserver(prop, observers[prop]);
        }
      }
      ui._destroy();
    }
  },

  // Each jQuery UI widget has a series of options that can be configured.
  // For instance, to disable a button, you call
  // `button.options('disabled', true)` in jQuery UI. To make this compatible
  // with SproutCore bindings, any time the SproutCore property for a
  // given jQuery UI option changes, we update the jQuery UI widget.
  _gatherOptions: function() {
    var uiOptions = this.get('uiOptions'), options = {};

    // The view can specify a list of jQuery UI options that should be treated
    // as SproutCore properties.
    uiOptions.forEach(function(key) {
      options[key] = this.get(key);

      // Set up an observer on the SproutCore property. When it changes,
      // call jQuery UI's `setOption` method to reflect the property onto
      // the jQuery UI widget.
      var observer = function() {
        var value = this.get(key);
        this.get('ui')._setOption(key, value);
      };

      this.addObserver(key, observer);

      // Insert the observer in a Hash so we can remove it later.
      this._observers = this._observers || {};
      this._observers[key] = observer;
    }, this);

    return options;
  },

  // Each jQuery UI widget has a number of custom events that they can
  // trigger. For instance, the progressbar widget triggers a `complete`
  // event when the progress bar finishes. Make these events behave like
  // normal SproutCore events. For instance, a subclass of JQ.ProgressBar
  // could implement the `complete` method to be notified when the jQuery
  // UI widget triggered the event.
  _gatherEvents: function(options) {
    var uiEvents = this.get('uiEvents') || [], self = this;

    uiEvents.forEach(function(event) {
      var callback = self[event];

      if (callback) {
        // You can register a handler for a jQuery UI event by passing
        // it in along with the creation options. Update the options hash
        // to include any event callbacks.
        options[event] = function(event, ui) {
          callback.call(self, event, ui);
        };
      }
    });
  }
});

// Create a new SproutCore view for the jQuery UI Button widget
JQ.Button = SC.View.extend(JQ.Widget, {
  uiType: 'button',
  uiOptions: ['label', 'disabled'],

  tagName: 'button'
});

// Create a new SproutCore view for the jQuery UI Menu widget (new
// in jQuery UI 1.9). Because it wraps a collection, we extend from
// SproutCore's CollectionView rather than a normal view.
//
// This means that you should use `#collection` in your template to
// create this view.
JQ.Menu = SC.CollectionView.extend(JQ.Widget, {
  uiType: 'menu',
  uiOptions: ['disabled'],
  uiEvents: ['select'],

  tagName: 'ul',

  // Whenever the underlying Array for this `CollectionView` changes,
  // refresh the jQuery UI widget.
  arrayDidChange: function(content, start, removed, added) {
    this._super(content, start, removed, added);

    var ui = this.get('ui');
    if (ui) {
      ui.refresh();
    }
  }
});

// Create a new SproutCore view for the jQuery UI Progrress Bar widget
JQ.ProgressBar = SC.View.extend(JQ.Widget, {
  uiType: 'progressbar',
  uiOptions: ['value', 'max'],
  uiEvents: ['change', 'complete']
});

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
 * Our own text box supports additional attributes on the textbox
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
 * Our own image field with visibility attribute
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
  attributeBindings: ['multiple'],

  itemViewClass: App.SelectOption,

  value: null,

  willInsertElement: function() {
    this._elementValueDidChange();
  },

  change: function() {
    this._elementValueDidChange();
  },

  _elementValueDidChange: function() {
    var views = SC.View.views,
      selectedOptions = this.$('option:selected'),
      value = null;

    if (SC.get(this, 'multiple') && SC.get(this, 'multiple') !== "false") {
      value = selectedOptions.toArray().map(function(el) {
        return SC.get(views[el.id], 'content');
      });
    } else {
      if (selectedOptions) {
        var id = selectedOptions.prop('id');
        if (SC.none(id)) {
          // Get the first option if there is one
          if (this.get('content').get) {
            value = this.get('content').get('firstObject');
          }
        } else {
          value = SC.get(views[selectedOptions.prop('id')], 'content');
        }
      }
    }

    set(this, 'value', value);
    set(SC.get(this, 'content'), 'selection', value);
  },

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

  arrayDidChange: function(content, start, removed, added) {
    this._super(content, start, removed, added);
    this._elementValueDidChange();
  }
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
    $('#navStream').addClass('active');
  } else if (pageFileName === 'search.html') {
    $('#navSearch').addClass('active');
  } else if (pageFileName.indexOf('admin') === 0) {
    $('#navAdmin').addClass('active');
  } else if (pageFileName === 'my_profile.html') {
    $('#navMyProfile').addClass('active');
  }

  // User
  var loggedInUser = App.sessionEngine.get('loggedInUser');
  $('#dividerUser').append(loggedInUser.get('displayNameOrUsername'));
}