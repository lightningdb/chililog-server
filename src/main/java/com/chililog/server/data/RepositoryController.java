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

package com.chililog.server.data;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.NullArgumentException;

import com.chililog.server.common.ChiliLogException;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

/**
 * <p>
 * Base controller for all repository controllers
 * </p>
 * 
 * @author vibul
 * 
 */
public abstract class RepositoryController extends Controller
{

    /**
     * Parse a string for fields. All exceptions are caught and logged. If <code>null</code> is returned, this indicates
     * that the entry should be skipped.
     * 
     * @param inputName
     *            Name of the input device or application that created this text entry
     * @param inputIpAddress
     *            IP address of the input device or application that created this text entry
     * @param textEntry
     *            The text for this entry to parse
     * @return <code>RepositoryEntryBO</code> ready for saving to mongoDB. If the entry is to be skipped and not written
     *         to mongoDB, then null is returned
     */
    public abstract RepositoryEntryBO parse(String inputName, String inputIpAddress, String textEntry);

    /**
     * Returns the last error processed by <code>parse</code>.
     */
    public abstract Exception getLastParseError();

    /**
     * Returns a list of matching entries
     * 
     * @param db
     *            Database connection
     * @param criteria
     *            Criteria to filter resultset
     * @return List of matching entries
     * @throws ChiliLogException
     */
    public ArrayList<RepositoryEntryBO> getList(DB db, RepositoryListCriteria criteria) throws ChiliLogException
    {
        ArrayList<DBObject> list = executeFindQuery(db, criteria);
        ArrayList<RepositoryEntryBO> boList = new ArrayList<RepositoryEntryBO>();
        if (list != null && !list.isEmpty())
        {
            for (DBObject o : list)
            {
                boList.add(new RepositoryEntryBO(o));
            }
        }
        return boList;
    }

    /**
     * Find matching entries
     * 
     * @param db
     *            Database connection
     * @param criteria
     *            Criteria to filter resultset. Fields, conditions and orderby are used.
     * @param queryParameters
     *            query parameters
     * @return List of matching entries
     */
    public ArrayList<DBObject> executeFindQuery(DB db, RepositoryListCriteria criteria)
    {
        if (db == null)
        {
            throw new NullArgumentException("db");
        }
        if (criteria == null)
        {
            throw new NullArgumentException("criteria");
        }

        DBCollection coll = db.getCollection(this.getDBCollectionName());
        int recordsPerPage = criteria.getRecordsPerPage();
        int skipDocumentCount = (criteria.getStartPage() - 1) * recordsPerPage;

        DBObject fields = criteria.getFieldsDbObject();
        DBObject conditions = criteria.getConditionsDbObject();
        DBObject orderBy = criteria.getOrderByDbObject();

        DBCursor cur = coll.find(conditions, fields).skip(skipDocumentCount).limit(recordsPerPage).sort(orderBy);
        ArrayList<DBObject> list = new ArrayList<DBObject>();
        while (cur.hasNext())
        {
            DBObject dbo = cur.next();
            list.add(dbo);
        }

        // Do page count by executing query again
        if (criteria.getDoPageCount())
        {
            int documentCount = coll.find(conditions).count();
            criteria.calculatePageCount(documentCount);
        }

        return list;
    }

    /**
     * Count of number of entries that matches the condition
     * 
     * @param db
     *            Database connection
     * @param criteria
     *            Criteria to filter resultset. Condition is used.
     * @param queryParameters
     *            query parameters
     * @return Number of matching entries
     */
    public int executeCountQuery(DB db, RepositoryListCriteria criteria)
    {
        if (db == null)
        {
            throw new NullArgumentException("db");
        }
        if (criteria == null)
        {
            throw new NullArgumentException("criteria");
        }

        DBCollection coll = db.getCollection(this.getDBCollectionName());

        DBObject conditions = criteria.getConditionsDbObject();

        return coll.find(conditions).count();
    }

    /**
     * Count of number of entries that matches the condition
     * 
     * @param db
     *            Database connection
     * @param criteria
     *            Criteria to filter resultset. Fields and Conditions is used.
     * @return List of distinct values for the nominated field.
     */
    @SuppressWarnings("rawtypes")
    public List executeDistinctQuery(DB db, RepositoryListCriteria criteria)
    {
        if (db == null)
        {
            throw new NullArgumentException("db");
        }
        if (criteria == null)
        {
            throw new NullArgumentException("criteria");
        }

        DBCollection coll = db.getCollection(this.getDBCollectionName());

        DBObject fields = criteria.getFieldsDbObject();
        if (fields == null || fields.keySet().isEmpty())
        {
            throw new IllegalArgumentException("Field is required for a 'distinct' query.");
        }

        String fieldName = null;
        for (String n : fields.keySet())
        {
            fieldName = n;
            break;
        }

        DBObject conditions = criteria.getConditionsDbObject();

        return coll.distinct(fieldName, conditions);
    }

    /**
     * Count of number of entries that matches the condition
     * 
     * @param db
     *            Database connection
     * @param criteria
     *            Criteria to filter resultset. Fields, Conditions, Initial, ReduceFunction and FinalizeFunction are
     *            used.
     * @return Specified fields and aggregation counter.
     */
    public DBObject executeGroupQuery(DB db, RepositoryListCriteria criteria)
    {
        if (db == null)
        {
            throw new NullArgumentException("db");
        }
        if (criteria == null)
        {
            throw new NullArgumentException("criteria");
        }

        DBCollection coll = db.getCollection(this.getDBCollectionName());

        DBObject fields = criteria.getFieldsDbObject();
        DBObject conditions = criteria.getConditionsDbObject();
        DBObject initial = criteria.getIntialDbObject();

        return coll.group(fields, conditions, initial, criteria.getReduceFunction(), criteria.getFinalizeFunction());
    }

}
