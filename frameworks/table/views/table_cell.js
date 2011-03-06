Endash.TableCellView = SC.View.extend({
  // wantsAcceleratedLayer: YES,
  isPoolable: YES,
  classNames: ['table-cell'],
  
  renderLayout: function(context, firstTime) {
    if(firstTime)
      return sc_super()
  }
});
