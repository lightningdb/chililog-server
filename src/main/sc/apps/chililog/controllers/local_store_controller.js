// ==========================================================================
// Project:   Chililog
// Copyright: ©2011 My Company, Inc.
// ==========================================================================

/**
 * Wrapper around the browser local store functions
 *
 * @extends SC.Object
 */
Chililog.localStoreController = SC.Object.create({

  isSupported: function() {
    try {
      return 'localStorage' in window && window['localStorage'] !== null;
    } catch (e) {
      return false;
    }
  },

  /**
   * Returns the value of the stored item.
   *
   * @param {String} key  Unique identifier for the item
   * @returns {String} value Value of the item. null is returned if not found.
   */
  getItem: function(key) {
    if (!this.isSupported()) {
      return null;
    }

    return localStorage[key];
  },

  /**
   * Saves an item into the local store
   * @param {String} key. Unique identifier for the item
   * @param {String} value. Value of the item
   */
  setItem: function(key, value) {
    if (!this.isSupported()) {
      return;
    }

    localStorage[key]= value;
  },

  /**
   * 
   * @param key
   */
  removeItem: function(key) {
    if (!this.isSupported()) {
      return;
    }

    localStorage.removeItem(key);
  }
  
});