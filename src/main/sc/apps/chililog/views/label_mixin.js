/**
 * Mixin for for label of required fields. Adds indicator to user that this is a requried field.
 */
Chililog.RequiredFieldLabelMixin = {
  formatter: function(v) {
    return v + '*';
  }
}