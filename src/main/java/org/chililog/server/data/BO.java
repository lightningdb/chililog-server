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

import org.bson.types.ObjectId;
import org.chililog.server.common.ChiliLogException;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * <p>
 * A Business Object represents an object in our data model. It provides a strongly typed means by which we can access
 * read and write mongoDB records.
 * </p>
 * 
 * @author vibul
 * 
 */
public abstract class BO {
    private DBObject _dbObject = null;

    public static final String DOCUMENT_ID_FIELD_NAME = "_id";
    public static final String DOCUMENT_VERSION_FIELD_NAME = "doc_version";

    /**
     * Basic constructor
     */
    public BO() {
        return;
    }

    /**
     * Constructor that loads our properties retrieved from the mongoDB dbObject
     * 
     * @param dbObject
     *            database object as retrieved from mongoDB
     * @throws ChiliLogException
     */
    public BO(DBObject dbObject) {
        _dbObject = dbObject;
        return;
    }

    /**
     * Saves class specific properties to the mongoDB dbObject
     * 
     * @param dbObject
     *            mongoDB database object as retrieved from mongoDB
     */
    protected abstract void savePropertiesToDBObject(DBObject dbObject) throws ChiliLogException;

    /**
     * Converts our properties into
     */
    public DBObject toDBObject() throws ChiliLogException {
        if (_dbObject == null) {
            _dbObject = new BasicDBObject();
        }
        savePropertiesToDBObject(_dbObject);
        return _dbObject;
    }

    /**
     * Flag to indicate if this business object represents an existing record in mongoDB or not
     */
    public boolean isExistingRecord() {
        return _dbObject != null && _dbObject.get(DOCUMENT_ID_FIELD_NAME) != null;
    }

    /**
     * Returns the internal mongoDB id
     */
    public ObjectId getDocumentID() {
        if (isExistingRecord()) {
            return (ObjectId) _dbObject.get(DOCUMENT_ID_FIELD_NAME);
        }
        else {
            return null;
        }
    }

    /**
     * Returns the version of this record. The version number is used in optimistic locking. If the record has not been
     * saved, then -1 is returned.
     */
    public long getDocumentVersion() {
        if (isExistingRecord()) {
            return (Long) (_dbObject.get(DOCUMENT_VERSION_FIELD_NAME));
        }
        else {
            return -1;
        }
    }

}
