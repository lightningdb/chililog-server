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
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;

import com.chililog.server.common.ChiliLogException;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;

/**
 * Singleton to manage our access to the repository information collection in our mongoDB
 * 
 * @author vibul
 * 
 */
public class RepositoryInfoController extends Controller
{
    public static final String MONGODB_COLLECTION_NAME = "repoinfo";

    /**
     * Returns the singleton instance for this class
     */
    public static RepositoryInfoController getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    /**
     * SingletonHolder is loaded on the first execution of Singleton.getInstance() or the first access to
     * SingletonHolder.INSTANCE, not before.
     * 
     * @see http://en.wikipedia.org/wiki/Singleton_pattern
     */
    private static class SingletonHolder
    {
        public static final RepositoryInfoController INSTANCE = new RepositoryInfoController();
    }

    /**
     * <p>
     * Singleton constructor
     * </p>
     */
    private RepositoryInfoController()
    {
        return;
    }

    /**
     * Returns the name of the mongoDB collection for this business object
     */
    @Override
    protected String getDBCollectionName()
    {
        return MONGODB_COLLECTION_NAME;
    }

    /**
     * Retrieves the specified repository by its id
     * 
     * @param db
     *            mongoDB connection
     * @param id
     *            unique id for the document stored in mongoDB
     * @return code>RepositoryInfoBO</code> representing the repository
     * @throws ChiliLogException
     *             if not found or database error
     */
    public RepositoryInfoBO get(DB db, ObjectId id) throws ChiliLogException
    {
        RepositoryInfoBO o = tryGet(db, id);
        if (o == null)
        {
            throw new ChiliLogException(Strings.REPO_INFO_NOT_FOUND_ERROR, id.toString());
        }
        return o;
    }

    /**
     * Tries to retrieve the specified repository by its id. If not found, null is returned.
     * 
     * @param db
     *            mongoDB connection
     * @param id
     *            unique id for the document stored in mongoDB
     * @return <code>RepositoryInfoBO</code> representing the repository or null if repository is not found
     * @throws ChiliLogException
     *             if database or data error
     */
    public RepositoryInfoBO tryGet(DB db, ObjectId id) throws ChiliLogException
    {
        try
        {
            if (db == null)
            {
                throw new IllegalArgumentException("db cannot be null");
            }
            if (id == null)
            {
                throw new IllegalArgumentException("id cannot be null");
            }

            DBCollection coll = db.getCollection(MONGODB_COLLECTION_NAME);
            BasicDBObject condition = new BasicDBObject();
            condition.put(BO.DOCUMENT_ID_FIELD_NAME, id);
            DBObject dbo = coll.findOne(condition);
            if (dbo == null)
            {
                return null;
            }
            return new RepositoryInfoBO(dbo);
        }
        catch (MongoException ex)
        {
            throw new ChiliLogException(ex, Strings.MONGODB_QUERY_ERROR, ex.getMessage());
        }
    }

    /**
     * Retrieves the specified repository by its name
     * 
     * @param db
     *            mongoDB connection
     * @param name
     *            name of the repository to retrieve
     * @return code>RepositoryInfoBO</code> representing the repository
     * @throws ChiliLogException
     *             if not found or database error
     */
    public RepositoryInfoBO getByName(DB db, String name) throws ChiliLogException
    {
        RepositoryInfoBO o = tryGetByName(db, name);
        if (o == null)
        {
            throw new ChiliLogException(Strings.REPO_INFO_NOT_FOUND_ERROR, name);
        }
        return o;
    }

    /**
     * Tries to retrieve the specified repository by its name. If not found, null is returned.
     * 
     * @param db
     *            mongoDB connection
     * @param name
     *            name of repository to retrieve
     * @return <code>RepositoryInfoBO</code> representing the repository or null if repository is not found
     * @throws ChiliLogException
     *             if database or data error
     */
    public RepositoryInfoBO tryGetByName(DB db, String name) throws ChiliLogException
    {
        try
        {
            if (db == null)
            {
                throw new IllegalArgumentException("db cannot be null");
            }
            if (StringUtils.isBlank(name))
            {
                throw new IllegalArgumentException("name cannot be blank");
            }

            DBCollection coll = db.getCollection(MONGODB_COLLECTION_NAME);
            BasicDBObject condition = new BasicDBObject();
            condition.put(RepositoryInfoBO.NAME_FIELD_NAME, name);
            DBObject dbo = coll.findOne(condition);
            if (dbo == null)
            {
                return null;
            }
            return new RepositoryInfoBO(dbo);
        }
        catch (MongoException ex)
        {
            throw new ChiliLogException(ex, Strings.MONGODB_QUERY_ERROR, ex.getMessage());
        }
    }

    /**
     * Get a list of all users
     * 
     * @param db
     *            mongoDB connection
     * @param criteria
     *            criteria to filter users
     * @return List of users matching the specified criteria
     * @throws ChiliLogException
     *             if database or data error
     */
    public ArrayList<RepositoryInfoBO> getList(DB db, RepositoryInfoListCriteria criteria) throws ChiliLogException
    {
        DBCollection coll = db.getCollection(MONGODB_COLLECTION_NAME);

        // Filter
        BasicDBObject condition = new BasicDBObject();
        if (!StringUtils.isBlank(criteria.getNamePattern()))
        {
            Pattern pattern = Pattern.compile(criteria.getNamePattern());
            condition.put(RepositoryInfoBO.NAME_FIELD_NAME, pattern);
        }
        if (criteria.getNameRestrictions() != null && criteria.getNameRestrictions().length > 0)
        {
            condition.put(RepositoryInfoBO.NAME_FIELD_NAME, new BasicDBObject("$in", criteria.getNameRestrictions()));
        }

        // Order
        DBObject orderBy = new BasicDBObject();
        orderBy.put(RepositoryInfoBO.NAME_FIELD_NAME, 1);

        // Get matching records
        int recordsPerPage = criteria.getRecordsPerPage();
        int skipDocumentCount = (criteria.getStartPage() - 1) * recordsPerPage;
        DBCursor cur = coll.find(condition).skip(skipDocumentCount).limit(recordsPerPage).sort(orderBy);
        ArrayList<RepositoryInfoBO> list = new ArrayList<RepositoryInfoBO>();
        while (cur.hasNext())
        {
            DBObject dbo = cur.next();
            list.add(new RepositoryInfoBO(dbo));
        }

        // Do page count by executing query again
        if (criteria.getDoPageCount())
        {
            int documentCount = coll.find(condition).count();
            criteria.calculatePageCount(documentCount);
        }

        return list;
    }

    /**
     * Saves the repository into mongoDB
     * 
     * @param db
     *            MongoDb connection
     * @param repository
     *            User to save
     * @throws ChiliLogException
     *             if there are errors
     */
    public void save(DB db, RepositoryInfoBO repository) throws ChiliLogException
    {
        // Validate unique name
        DBCollection coll = db.getCollection(MONGODB_COLLECTION_NAME);
        BasicDBObject condition = new BasicDBObject();
        condition.put(RepositoryInfoBO.NAME_FIELD_NAME, repository.getName());
        if (repository.isExistingRecord())
        {
            condition.put(BO.DOCUMENT_ID_FIELD_NAME, new BasicDBObject("$ne", repository.getDocumentID()));
        }
        long i = coll.getCount(condition);
        if (i > 0)
        {
            throw new ChiliLogException(Strings.REPO_INFO_DUPLICATE_NAME_ERROR, repository.getName());
        }

        // Validate unique parser names
        for (RepositoryParserInfoBO p : repository.getParsers())
        {
            String parserName = p.getName();
            int pCount = 0;
            for (RepositoryParserInfoBO p2 : repository.getParsers())
            {
                if (p2.getName().equals(parserName))
                {
                    pCount++;
                }
            }
            if (pCount != 1)
            {
                throw new ChiliLogException(Strings.REPO_INFO_DUPLICATE_PARSER_NAME_ERROR, parserName,
                        repository.getName());
            }

            // Validate unique field names per parser
            for (RepositoryFieldInfoBO f : p.getFields())
            {
                String fieldName = f.getName();
                int count = 0;
                for (RepositoryFieldInfoBO f2 : p.getFields())
                {
                    if (f2.getName().equals(fieldName))
                    {
                        count++;
                    }
                }
                if (count != 1)
                {
                    throw new ChiliLogException(Strings.REPO_INFO_DUPLICATE_FIELD_NAME_ERROR, fieldName, parserName,
                            repository.getName());
                }
            }
        }

        // Save it
        super.save(db, repository);

        // Add repository and index
        createIndexes(db);
    }

    /**
     * Creates the required repository index
     * 
     * @param db
     *            Database connection
     */
    private void createIndexes(DB db)
    {
        DBCollection col = db.getCollection(this.getDBCollectionName());
        List<DBObject> indexes = col.getIndexInfo();

        boolean found = false;
        for (DBObject idx : indexes)
        {
            if (idx.get("name").equals("keyword_ts_index"))
            {
                found = true;
                break;
            }
        }
        if (!found)
        {
            BasicDBObject keys = new BasicDBObject();
            keys.put(RepositoryEntryBO.KEYWORDS_FIELD_NAME, 1);
            keys.put(RepositoryEntryBO.TIMESTAMP_FIELD_NAME, 1);
            col.ensureIndex(keys, "keyword_ts_index");
        }
    }

    /**
     * Removes the specified repository from mongoDB
     * 
     * @param db
     *            MongoDb connection
     * @param repository
     *            User to remove
     * @throws ChiliLogException
     *             if there are errors
     */
    public void remove(DB db, RepositoryInfoBO repository) throws ChiliLogException
    {
        super.remove(db, repository);
    }
}
