// ==========================================================================
// Project:   Chililog
// Copyright: ©2011 My Company, Inc.
// ==========================================================================

/** @class

  The Chililog data source is a bit like Seinfeld show. It is a data source that does NOTHING.

 This is because it is a read only copy of the data that is on the server.  We fill it data using our data controllers.

 This data source is a big copy of SC.FixturesDataSource.

 @extends SC.DataSource
 */
Chililog.DataSource = SC.DataSource.extend(
/** @scope Chililog.DataSource.prototype */ {

  /**
   We are not going to handle this because it is done via our data controllers
   We only perform local queries on the store.

   @param {SC.Store} store the requesting store
   @param {SC.Query} query query describing the request
   @returns {Boolean} YES if you can handle fetching the query, NO otherwise
   */
  fetch: function(store, query) {
    return NO; // return YES if you handled the query
  },

  /**
   We are not going to handle this because it is done via our data controllers.
   We only perform local queries on the store.

   @param {SC.Store} store the requesting store
   @param {SC.Query} query query describing the request
   @returns {Boolean} YES if you can handle fetching the query, NO otherwise
   */
  retrieveRecord: function(store, storeKey) {
    return NO; // return YES if you handled the storeKey
  },

  /**
   Called from `createdRecords()` to created a single record.  This is the
   most basic primitive to can implement to support creating a record.

   To support cascading data stores, be sure to return `NO` if you cannot
   handle the passed storeKey or `YES` if you can.

   @param {SC.Store} store the requesting store
   @param {Array} storeKey key to update
   @param {Hash} params to be passed down to data source. originated
   from the commitRecords() call on the store
   @returns {Boolean} YES if handled
   */
  createRecord: function(store, storeKey, params) {
    var id = store.idFor(storeKey);
    var recordType = store.recordTypeFor(storeKey);
    this._invalidateCachesFor(recordType, storeKey, id);

    store.dataSourceDidComplete(storeKey);

    return YES; // return YES if you handled the storeKey
  },

  /**
   Called from `updatesRecords()` to update a single record.  This is the
   most basic primitive to can implement to support updating a record.

   To support cascading data stores, be sure to return `NO` if you cannot
   handle the passed storeKey or `YES` if you can.

   @param {SC.Store} store the requesting store
   @param {Array} storeKey key to update
   @param {Hash} params to be passed down to data source. originated
   from the commitRecords() call on the store
   @returns {Boolean} YES if handled
   */
  updateRecord: function(store, storeKey, params) {
    var id = store.idFor(storeKey);
    var recordType = store.recordTypeFor(storeKey);
    this._invalidateCachesFor(recordType, storeKey, id);

    store.dataSourceDidComplete(storeKey);

    return YES; // return YES if you handled the storeKey
  },

  /**
   Called from `destroyRecords()` to destroy a single record.  This is the
   most basic primitive to can implement to support destroying a record.

   To support cascading data stores, be sure to return `NO` if you cannot
   handle the passed storeKey or `YES` if you can.

   @param {SC.Store} store the requesting store
   @param {Array} storeKey key to update
   @param {Hash} params to be passed down to data source. originated
   from the commitRecords() call on the store
   @returns {Boolean} YES if handled
   */
  destroyRecord: function(store, storeKey, params) {
    var id = store.idFor(storeKey);
    var recordType = store.recordTypeFor(storeKey);
    this._invalidateCachesFor(recordType, storeKey, id);

    store.dataSourceDidDestroy(storeKey);

    return YES;  // return YES if you handled the storeKey
  },

  /** @private
   Invalidates any internal caches based on the recordType and optional
   other parameters.  Currently this only invalidates the storeKeyCache used
   for fetch, but it could invalidate others later as well.

   @param {SC.Record} recordType the type of record modified
   @param {Number} storeKey optional store key
   @param {String} id optional record id
   @returns {SC.FixturesDataSource} receiver
   */
  _invalidateCachesFor: function(recordType, storeKey, id) {
    var cache = this._storeKeyCache;
    if (cache) {
      delete cache[SC.guidFor(recordType)];
    }
    return this;
  }

});
