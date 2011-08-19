

// Create a subclass of `JQ.Button` to define behavior for our button.
App.Button = JQ.Button.extend({
  // When the button is clicked...
  click: function() {
    alert('hello');
  }
});