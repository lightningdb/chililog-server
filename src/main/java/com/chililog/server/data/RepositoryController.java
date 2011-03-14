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
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import com.chililog.server.common.ChiliLogException;
import com.mongodb.BasicDBObject;
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
    public static final Pattern DATE_PATTERN = Pattern
            .compile("^([0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}Z)$");
    public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";
    public static final Pattern LONG_NUMBER_PATTERN = Pattern.compile("^NumberLong\\(([0-9]+)\\)$");

    /**
     * Parse a string for fields. All exceptions are caught and logged. If <code>null</code> is returned, this indicates
     * that the entry should be skipped.
     * 
     * @param textEntry
     *            The text for this entry to parse
     * @return <code>RepositoryEntryBO</code> ready for saving to mongoDB. If the entry is to be skipped and not written
     *         to mongoDB, then null is returned
     */
    public abstract RepositoryEntryBO parse(String textEntry);

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
        DBCollection coll = db.getCollection(this.getDBCollectionName());

        // Filter
        BasicDBObject query = parseCriteria(criteria);

        // Order
        DBObject orderBy = new BasicDBObject();
        orderBy.put(RepositoryEntryBO.ENTRY_TIMESTAMP_FIELD_NAME, 1);

        // Get matching records
        int recordsPerPage = criteria.getRecordsPerPage();
        int startPage = (criteria.getStartPage() - 1) * recordsPerPage;
        DBCursor cur = coll.find(query).skip(startPage).limit(recordsPerPage).sort(orderBy);
        ArrayList<RepositoryEntryBO> list = new ArrayList<RepositoryEntryBO>();
        while (cur.hasNext())
        {
            DBObject dbo = cur.next();
            list.add(new RepositoryEntryBO(dbo));
        }

        // Do page count by executing query again
        if (criteria.getDoPageCount())
        {
            int recordCount = coll.find(query).count();
            criteria.calculatePageCount(recordCount);
        }

        return list;
    }

    /**
     * Returns a list of matching entries as raw mongoDB DBObjects. This should be used where serializing to DBObject is
     * required. It is faster to serialize straight from DBObject rather than convert DBObject to BO and then back
     * again.
     * 
     * @param db
     *            Database connection
     * @param criteria
     *            Criteria to filter resultset
     * @return List of matching entries
     * @throws ChiliLogException
     */
    public ArrayList<DBObject> getDBObjectList(DB db, RepositoryListCriteria criteria) throws ChiliLogException
    {
        DBCollection coll = db.getCollection(this.getDBCollectionName());

        // Filter
        BasicDBObject query = parseCriteria(criteria);

        // Order
        DBObject orderBy = new BasicDBObject();
        orderBy.put(RepositoryEntryBO.ENTRY_TIMESTAMP_FIELD_NAME, 1);

        // Get matching records
        int recordsPerPage = criteria.getRecordsPerPage();
        int startPage = (criteria.getStartPage() - 1) * recordsPerPage;
        DBCursor cur = coll.find(query).skip(startPage).limit(recordsPerPage).sort(orderBy);
        ArrayList<DBObject> list = new ArrayList<DBObject>();
        while (cur.hasNext())
        {
            DBObject dbo = cur.next();
            list.add(dbo);
        }

        // Do page count by executing query again
        if (criteria.getDoPageCount())
        {
            int recordCount = coll.find(query).count();
            criteria.calculatePageCount(recordCount);
        }

        return list;
    }

    /**
     * Load criteria as a mongoDB dbObject
     * 
     * @param criteria
     *            Criteria to filter
     * @return BasicDBObject
     */
    private BasicDBObject parseCriteria(RepositoryListCriteria criteria)
    {
        BasicDBObject query = null;
        if (!StringUtils.isBlank(criteria.getJsonCriteria()))
        {
            MongoJsonParser parser = new MongoJsonParser(criteria.getJsonCriteria(), DATE_PATTERN, DATE_FORMAT,
                    LONG_NUMBER_PATTERN);
            query = (BasicDBObject) parser.parse();
        }
        else
        {
            query = new BasicDBObject();
        }

        if (criteria.getFrom() != null)
        {
            query.put(RepositoryEntryBO.ENTRY_TIMESTAMP_FIELD_NAME, new BasicDBObject("$gte", criteria.getFrom()));
        }
        if (criteria.getTo() != null)
        {
            query.put(RepositoryEntryBO.ENTRY_TIMESTAMP_FIELD_NAME, new BasicDBObject("$lte", criteria.getTo()));
        }

        return query;
    }
}
