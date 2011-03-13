Endash.CollectionSuperFastPath = {
  
  initMixin: function() {
    this.createBlocks();
  },
  
  reloadIfNeeded: function() {
    var invalid = this._invalidIndexes;
    if (!invalid || !this.get('isVisibleInWindow')) return this ; // delay
    this._invalidIndexes = NO ;

    var nowShowing = this.get('nowShowing');
    
    // if the set is defined but it contains the entire nowShowing range, just
    // replace
    if (invalid.isIndexSet && invalid.contains(nowShowing)) invalid = YES ;
      
    var blocks = this.get('blocks');
    if(!blocks) {
      blocks = this.createBlocks();
      return;
    }

    var firstBlock = blocks.objectAt(0);
    var secondBlock = blocks.objectAt(1);
    var thirdBlock = blocks.objectAt(2);
    
    // if either the first or last block have scrolled off, we want to reposition them on the other side
    // if the second block has scrolled off, then that means we need to reset the blocks entirely
    
    if(invalid === YES) {
      this.resetBlocks();
    } else {
      if(!firstBlock.get('isVisibleInWindow') && !thirdBlock.contains(content.get('length'))) {
        this.repositionBlock(0);
      } else {
        if(!thirdBlock.get('isVisibleInWindow') && !firstBlock.contains(0)) {
        this.repositionBlock(2);        
        }
      }

      this.displayIndexes(nowShowing);      

    }
    

  },
  
  displayIndexes: function(indexes) {
    var blocks = this.get('blocks'),
      block = blocks[0].contains(indexes.get('max')) ? block[0] : block[2],
      index = block.get('range').get('min'),
      content = this.get('content'),
      row, attrs;

    var attrs = {}
    indexes.forEach(function(i) {
      rows = block.get('rows');
      if(!rows) block.set('rows', rows = [])

      item = content.ObjectAt(i);

      this.setAttributesForItem(item, i, attrs);
        
      row = rows.objectAt(i - index);
      
      if(!row) {
        attrs.parentView = block;
        attrs.layout = {
          top: this.rowOffsetForContentIndex(i) - this.rowOffsetForContentIndex(index);
          height: this.rowHeightForContentIndex(i),
          left: 0,
          bottom: 0
        };
        rows.replaceAt(i - index, this.createRow(block, attrs));
      } else {
        this.configureItemView(row, attrs);
      }
    });
  },
  
  createRow: function(block, attrs) {
    block.createChildView(this.get('exampleView'), attrs);
  },
  
  createBlocks: function() {
    this.set('blocks', [
      this.createBlock(0),
      this.createBlock(1),
      this.createBlock(2)
    ]);

    this.resetBlocks();
  },
  
  resetBlocks: function() {
    var nowShowing = this.get('nowShowing');
    var blocks = this.get('blocks');
    var numRows = nowShowing.get('length')
    var rowsPerBlock = Math.ceil((numRows * 2) / 3)
    var first = nowShowing.get('min')
    
    var range = []
    
    if(first >= Math.floor(numRows / 2)) {
      // then we're going to base the initial position around the middle of the now showing
      range[0] = SC.IndexSet.create(first - Math.floor(numRows / 2), rowsPerBlock);
      range[1] = SC.IndexSet.create(range[0].get('max') + 1, rowsPerBlock);
      range[2] = SC.IndexSet.create(range[1].get('max') + 1, rowsPerBlock);
    } else {
      // we're going to base the initial position right at the top
      range[0] = SC.IndexSet.create(0, rowsPerBlock);
      range[1] = SC.IndexSet.create(range[0].get('max') + 1, rowsPerBlock);
      range[2] = SC.IndexSet.create(range[1].get('max') + 1, rowsPerBlock);
    }
    
    blocks.forEach(function(block, index) {
      block.set('range', range[index]);
      block.set('layout', {
        top: this.rowOffsetForItemView(range[index].get('min')),
        height: this.rowOffsetForItemView(range[index].get('max') + 1),
        left: 0,
        right: 0
      });
    });
    
    this.displayIndexes(nowShowing)
  },
  
  repositionBlock: function(block) {
    var newBlock = block === 0 ? 2 : 0,
      blocks = this.get('blocks'),
      nowShowing = this.get('nowShowing'),
      content = this.get('content'),
      numRows = nowShowing.get('length'),
      rowsPerBlock = Math.ceil((numRows * 2) / 3),
      _block = blocks[block];
    
    blocks[block] = blocks[1]
    blocks[1] = blocks[newBlock]
    blocks[newBlock] = _block

    if(newBlock == 0) {
      newRange = SC.IndexSet.create(blocks[1].get('min') - rowsPerBlock, rowsPerBlock);
    } else {
      newRange = SC.IndexSet.create(blocks[1].range.get('max') + 1, SC.min(content.get('length'), rowsPerBlock))
    }
    
    _block.set('range', newRange)
    _block.set('layout', {
      top: this.rowOffsetForItemView(newRange.get('min')),
      height: this.rowOffsetForItemView(newRange.get('max') + 1),
      left: 0,
      right: 0,
    });
  }
  
  createBlock: function(index) {
    attrs = {
      parentView: this.get('containerView') || this,
      owner: this,
      layerId: this.get('layerId') + '-block-' + index
    };
  
    block = this.createChildView(SC.View.extend({
      contains: function(index) {
        return this.range.contains(index);
      }
    }), attrs);
    return block;
  },
  
  _cv_nowShowingDidChange: function() {
    var nowShowing  = this.get('nowShowing'),
        last        = this._sccv_lastNowShowing,
        diff, diff1, diff2;

    if (last !== nowShowing) {
      if (last && nowShowing) {;
        diff = this._TMP_DIFF1.add(nowShowing).remove(last);
      } else diff = last || nowShowing ;
    }

    // if nowShowing has actually changed, then update
    if (diff && diff.get('length') > 0) {
      this._sccv_lastNowShowing = nowShowing ? nowShowing.frozenCopy() : null;
      this.updateContentRangeObserver();
      this.reload(diff);
    }
    
    // cleanup tmp objects
    if (diff) diff.clear();
  }.observes('nowShowing'),
  
  setAttributesForItem: function(item, index, attrs) {
    // var del = this.get('contentDelegate'), 
        // isGroupView = this.contentIndexIsGroup(this, this.get('content'), index),
        // ExampleView = this.exampleViewForItem(item, index),
    var    content = this.get("content");
    
    // 
    // FIGURE OUT "NORMAL" ATTRIBUTES
    //
    // attrs.createdFromExampleView = ExampleView;
    // attrs.parentView = this.get('containerView') || this;
    attrs.contentIndex = index;
    attrs.owner = attrs.displayDelegate = this;
    attrs.content = item;
    attrs.page = this.page;
    attrs.layerId = this.layerIdFor(index);
    attrs.isEnabled = del.contentIndexIsEnabled(this, content, index);
    attrs.isSelected = del.contentIndexIsSelected(this, content, index);
    attrs.outlineLevel = del.contentIndexOutlineLevel(this, content, index);
    attrs.disclosureState = del.contentIndexDisclosureState(this, content, index);
    attrs.isVisibleInWindow = this.get('isVisibleInWindow');
    attrs.isGroupView = isGroupView;
    attrs.layout = this.layoutForContentIndex(index);
    if (!attrs.layout) attrs.layout = ExampleView.prototype.layout;
  },

  configureItemView: function(itemView, attrs) {
    // set settings. Self explanatory.
    itemView.beginPropertyChanges();
    itemView.setIfChanged('content', attrs.content);
    itemView.setIfChanged('contentIndex', attrs.contentIndex);
    // itemView.setIfChanged('parentView', attrs.parentView);
    itemView.setIfChanged('layerId', attrs.layerId);
    itemView.setIfChanged('isEnabled', attrs.isEnabled);
    itemView.setIfChanged('isSelected', attrs.isSelected);
    itemView.setIfChanged('outlineLevel', attrs.outlineLevel);
    // itemView.setIfChanged('layout', attrs.layout);
    itemView.setIfChanged('disclosureState', attrs.disclosureState);
    itemView.setIfChanged('isVisibleInWindow', attrs.isVisibleInWindow);
    itemView.setIfChanged('isGroupView', attrs.isGroupView);
    itemView.setIfChanged('page', this.page);
    itemView.endPropertyChanges();
  }, 
}