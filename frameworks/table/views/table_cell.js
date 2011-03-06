Endash.TableCellView = SC.View.extend({
  // wantsAcceleratedLayer: YES,
  isPoolable: YES,
  classNames: ['table-cell'],
  
  renderLayout: function(context, firstTime) {
    if(firstTime)
      return sc_super()
  },
  // 
  // widthDidChange: function() {
  //   this.get('parentView').columnWidthDidChange(this.get('columnIndex'))
  // }.observes('*column.width')
}),
