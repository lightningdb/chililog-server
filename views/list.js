// SC.ListView = SC.ListView.extend(SC.DisplayDelegate, {
//   useDisplayDelegate: NO,
// 
//   layoutForContentIndex: function(contentIndex) {
//     var view = this.getItemForContentIndex(contentIndex),
//       delegate = view.useDisplayDelegate ? view.get('displayDelegate') : NO;
//     
//     if(delegate && delegate.layoutForContentIndex) {
//       return delegate.layoutForContentIndex(contentIndex);
//     }
//     
//     return {
//       top:    this.rowOffsetForContentIndex(contentIndex),
//       height: this.rowHeightForContentIndex(contentIndex),
//       left:   0, 
//       right:  0
//     };
//   },
//   
//   createItemViewForContentIndex: function(idx) {
//     var ret = sc_super(),
//       parentItem,
//       parentItemView;
// 
//     if(!(delegate = this.delegateForContentIndex(idx))) {
//       return ret;
//     } else {
//       ret.set('displayDelegate', delegate);
//     }
// 
//     return ret;
//   },
//   
//   delegateForContentIndex: function(idx) {
//     if(!this.useDisplayDelegate) {
//       return NO;
//     }
//     
//     var content = this.get('content'),
//       item = content.objectAt(idx),
//       delegate
//       
//     if(parentItem = item.get('parentItem')) {
//       return this.itemViewForContentObject(parentItem);
//     } else {
//       return this;
//     }
// 
//   }
//   
// 
// });