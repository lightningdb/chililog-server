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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;
import org.chililog.server.common.ChiliLogException;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;

/**
 * Singleton to manage our access to repository configuration documents in our mongoDB
 * 
 * @author vibul
 * 
 */
public class RepositoryConfigController extends Controller
{
    public static final String MONGODB_COLLECTION_NAME = "repo";

    /**
     * Returns the singleton instance for this class
     */
    public static RepositoryConfigController getInstance()
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
        public static final RepositoryConfigController INSTANCE = new RepositoryConfigController();
    }

    /**
     * <p>
     * Singleton constructor
     * </p>
     */
    private RepositoryConfigController()
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
    public RepositoryConfigBO get(DB db, ObjectId id) throws ChiliLogException
    {
        RepositoryConfigBO o = tryGet(db, id);
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
    public RepositoryConfigBO tryGet(DB db, ObjectId id) throws ChiliLogException
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
            return new RepositoryConfigBO(dbo);
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
    public RepositoryConfigBO getByName(DB db, String name) throws ChiliLogException
    {
        RepositoryConfigBO o = tryGetByName(db, name);
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
    public RepositoryConfigBO tryGetByName(DB db, String name) throws ChiliLogException
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
            condition.put(RepositoryConfigBO.NAME_FIELD_NAME, name);
            DBObject dbo = coll.findOne(condition);
            if (dbo == null)
            {
                return null;
            }
            return new RepositoryConfigBO(dbo);
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
    public ArrayList<RepositoryConfigBO> getList(DB db, RepositoryConfigListCriteria criteria) throws ChiliLogException
    {
        DBCollection coll = db.getCollection(MONGODB_COLLECTION_NAME);

        // Filter
        BasicDBObject condition = new BasicDBObject();
        if (!StringUtils.isBlank(criteria.getNamePattern()))
        {
            Pattern pattern = Pattern.compile(criteria.getNamePattern());
            condition.put(RepositoryConfigBO.NAME_FIELD_NAME, pattern);
        }
        if (criteria.getNameRestrictions() != null && criteria.getNameRestrictions().length > 0)
        {
            condition.put(RepositoryConfigBO.NAME_FIELD_NAME, new BasicDBObject("$in", criteria.getNameRestrictions()));
        }

        // Order
        DBObject orderBy = new BasicDBObject();
        orderBy.put(RepositoryConfigBO.NAME_FIELD_NAME, 1);

        // Get matching records
        int recordsPerPage = criteria.getRecordsPerPage();
        int skipDocumentCount = (criteria.getStartPage() - 1) * recordsPerPage;
        DBCursor cur = coll.find(condition).skip(skipDocumentCount).limit(recordsPerPage).sort(orderBy);
        ArrayList<RepositoryConfigBO> list = new ArrayList<RepositoryConfigBO>();
        while (cur.hasNext())
        {
            DBObject dbo = cur.next();
            list.add(new RepositoryConfigBO(dbo));
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
     * @param repoConfig
     *            Repository configuration to save
     * @throws ChiliLogException
     *             if there are errors
     */
    public void save(DB db, RepositoryConfigBO repoConfig) throws ChiliLogException
    {
        // Validate unique name
        DBCollection coll = db.getCollection(MONGODB_COLLECTION_NAME);
        BasicDBObject condition = new BasicDBObject();
        condition.put(RepositoryConfigBO.NAME_FIELD_NAME, repoConfig.getName());
        if (repoConfig.isExistingRecord())
        {
            condition.put(BO.DOCUMENT_ID_FIELD_NAME, new BasicDBObject("$ne", repoConfig.getDocumentID()));
        }
        long i = coll.getCount(condition);
        if (i > 0)
        {
            throw new ChiliLogException(Strings.REPO_INFO_DUPLICATE_NAME_ERROR, repoConfig.getName());
        }

        // Validate unique parser names
        for (RepositoryParserConfigBO p : repoConfig.getParsers())
        {
            String parserName = p.getName();
            int pCount = 0;
            for (RepositoryParserConfigBO p2 : repoConfig.getParsers())
            {
                if (p2.getName().equals(parserName))
                {
                    pCount++;
                }
            }
            if (pCount != 1)
            {
                throw new ChiliLogException(Strings.REPO_INFO_DUPLICATE_PARSER_NAME_ERROR, parserName,
                        repoConfig.getName());
            }

            // Validate unique field names per parser
            for (RepositoryFieldConfigBO f : p.getFields())
            {
                String fieldName = f.getName();
                int count = 0;
                for (RepositoryFieldConfigBO f2 : p.getFields())
                {
                    if (f2.getName().equals(fieldName))
                    {
                        count++;
                    }
                }
                if (count != 1)
                {
                    throw new ChiliLogException(Strings.REPO_INFO_DUPLICATE_FIELD_NAME_ERROR, fieldName, parserName,
                            repoConfig.getName());
                }
            }
        }

        // Save it
        super.save(db, repoConfig);

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
     * @param repoConfig
     *            User to remove
     * @throws ChiliLogException
     *             if there are errors
     */
    public void remove(DB db, RepositoryConfigBO repoConfig) throws ChiliLogException
    {
        super.remove(db, repoConfig);
    }
}
