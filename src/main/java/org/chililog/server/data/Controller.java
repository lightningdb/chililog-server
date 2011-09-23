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

package org.chililog.server.data;

import org.apache.commons.lang.NullArgumentException;
import org.chililog.server.common.ChiliLogException;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.mongodb.WriteConcern;

/**
 * Base controller class for accessing data in mongoDB
 * 
 * @author vibul
 * 
 */
public abstract class Controller {

    /**
     * Basic constructor
     */
    public Controller() {
        return;
    }

    /**
     * Returns the name of the mongoDB collection for this business object
     */
    protected abstract String getDBCollectionName();

    /**
     * Returns the mongoDB write strategy used in saving. By default, it is set to <code>SAFE</code>. This means
     * exceptions are raised for network issues, and server errors; waits on a server for the write operation. However,
     * it can be overridden.
     */
    protected WriteConcern getDBWriteConern() {
        return WriteConcern.SAFE;
    }

    /**
     * Saves the user into mongoDB
     * 
     * @param db
     *            MongoDb connection
     * @param businessObject
     *            Business object to save
     * @throws ChiliLogException
     *             if there is an error during saving
     */
    public void save(DB db, BO businessObject) throws ChiliLogException {
        if (db == null) {
            throw new NullArgumentException("db");
        }
        if (businessObject == null) {
            throw new NullArgumentException("businessObject");
        }

        try {
            DBObject obj = businessObject.toDBObject();
            DBCollection coll = db.getCollection(this.getDBCollectionName());
            if (businessObject.isExistingRecord()) {
                long recordVersion = businessObject.getDocumentVersion();
                obj.put(BO.DOCUMENT_VERSION_FIELD_NAME, recordVersion + 1);

                BasicDBObject query = new BasicDBObject();
                query.put(BO.DOCUMENT_ID_FIELD_NAME, obj.get(BO.DOCUMENT_ID_FIELD_NAME));
                query.put(BO.DOCUMENT_VERSION_FIELD_NAME, recordVersion);

                coll.update(query, obj, false, false, this.getDBWriteConern());
            } else {
                obj.put(BO.DOCUMENT_VERSION_FIELD_NAME, (long) 1);
                coll.insert(obj);
            }
        } catch (MongoException ex) {
            throw new ChiliLogException(ex, Strings.MONGODB_SAVE_ERROR, ex.getMessage());
        }
    }

    /**
     * Removes the specified business object form mongoDB
     * 
     * @param db
     *            MongoDb connection
     * @param businessObject
     *            business object to remove from the database
     * @throws ChiliLogException
     *             if there is any error during deleting
     */
    public void remove(DB db, BO businessObject) throws ChiliLogException {
        if (db == null) {
            throw new NullArgumentException("db");
        }
        if (businessObject == null) {
            throw new NullArgumentException("businessObject");
        }

        try {
            DBCollection coll = db.getCollection(this.getDBCollectionName());
            if (businessObject.isExistingRecord()) {
                DBObject obj = new BasicDBObject();
                obj.put(BO.DOCUMENT_ID_FIELD_NAME, businessObject.getDocumentID());
                coll.remove(obj);
            }
        } catch (MongoException ex) {
            throw new ChiliLogException(ex, Strings.MONGODB_REMOVE_ERROR, ex.getMessage());
        }
    }
}
