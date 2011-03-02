SC.View.extend(SC.Control, {
  isPoolable: YES,

  classNames: ['sc-dataview-row', 'sc-list-item-view']
  
  // init: function() {
  //   var ret = sc_super(),
  //     columns = this.get('parentView').get('columns'),
  //     column,
  //     cell,
  //     cells = []
  // 
  //   var cellViews = ret._sc_cell_views = []
  // 
  //   for(var i = 0, len = columns.get('length'); i < len; i++)   {
  //     cell = this._createNewCellView(idx, i, {parentView: ret})
  //     cellViews[i] = cell
  //     cells.push(cell)
  //   }
  // 
  //   ret.set('childViews', cells)
  //   ret.replaceLayer();
  // },
  // 
  // _createNewCellView: function(idx, col, attrs) {
  //   var columns = this.get('parentView').get('columns'),
  //     content = this.get('content'),
  //     item = content.objectAt(idx),
  //     column = columns.objectAt(col),
  //     E = this.cellViewForColumn(col),
  //     cellViews
  // 
  //   if(!attrs)
  //     attrs = {}
  // 
  //   attrs.classNames = 'column-' + col
  //   attrs.contentIndex = idx
  //   attrs.content = item
  //   attrs.contentValueKey = column.get('key')
  // 
  //   return E.create(attrs)
  // },
  // 
  // awakeFromPool: function() {
  //   var item = this.get('content'),
  //     columns = this.get('parentView').get('columns'),
  //     attrs = {},
  //     E,
  //     column, cell;
  // 
  //   for(var i = 0, len = columns.get('length'); i < len; i++)   {
  // 
  //     cell = this._sc_cell_views[i];
  // 
  //     if (cell.isPoolable) {
  //       cell.beginPropertyChanges();
  //       cell.set('contentIndex', idx);
  //       cell.set('content', item);
  //       cell.endPropertyChanges()
  //     } else {
  //       cell.destroy();
  //       cell = this._createNewCellView(idx, i);
  //       ret._sc_cell_views[i] = cell;
  //       ret.appendChild(cell);
  //     }
  //   }
  // }
});