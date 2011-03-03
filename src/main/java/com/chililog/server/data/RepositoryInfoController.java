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
import com.mongodb.MongoException;

/**
 * Singleton to manage our access to the repository collection in our mongoDB
 * 
 * @author vibul
 * 
 */
public class RepositoryInfoController extends Controller
{
    public static final String MONGODB_COLLECTION_NAME = "repositories_info";

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
     * Retrieves the specified repository
     * 
     * @param db
     *            mongoDB connection
     * @param name
     *            name of the repository to retrieve
     * @return code>RepositoryInfoBO</code> representing the repository
     * @throws ChiliLogException
     *             if not found or database error
     */
    public RepositoryInfoBO get(DB db, String name) throws ChiliLogException
    {
        RepositoryInfoBO o = tryGet(db, name);
        if (o == null)
        {
            throw new ChiliLogException(Strings.REPO_INFO_NOT_FOUND_ERROR, name);
        }
        return o;
    }

    /**
     * Tries to retrieve the specified repository. If not found, null is returned.
     * 
     * @param db
     *            mongoDB connection
     * @param name
     *            name of repository to retrieve
     * @return <code>RepositoryInfoBO</code> representing the repository or null if repository is not found
     * @throws ChiliLogException
     *             if database or data error
     */
    public RepositoryInfoBO tryGet(DB db, String name) throws ChiliLogException
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
            BasicDBObject query = new BasicDBObject();
            query.put(RepositoryInfoBO.NAME_FIELD_NAME, name);
            DBObject dbo = coll.findOne(query);
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
        BasicDBObject query = new BasicDBObject();        
        if (!StringUtils.isBlank(criteria.getNamePattern()))
        {
            Pattern pattern = Pattern.compile(criteria.getNamePattern());
            query.put(RepositoryInfoBO.NAME_FIELD_NAME, pattern);
        }

        // Order
        DBObject orderBy = new BasicDBObject();
        orderBy.put(RepositoryInfoBO.NAME_FIELD_NAME, 1);
        
        // Get matching records
        int recordsPerPage = criteria.getRecordsPerPage();
        int startPage = (criteria.getStartPage() - 1) * recordsPerPage;
        DBCursor cur = coll.find(query).skip(startPage).limit(recordsPerPage).sort(orderBy);
        ArrayList<RepositoryInfoBO> list = new ArrayList<RepositoryInfoBO>();
        while (cur.hasNext())
        {
            DBObject dbo = cur.next();
            list.add(new RepositoryInfoBO(dbo));
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
        BasicDBObject query = new BasicDBObject();
        query.put(RepositoryInfoBO.NAME_FIELD_NAME, repository.getName());
        if (repository.isExistingRecord())
        {
            query.put(BaseBO.INTERNAL_ID_FIELD_NAME, new BasicDBObject("$ne", repository.getInternalID()));
        }
        long i = coll.getCount(query);
        if (i > 0)
        {
            throw new ChiliLogException(Strings.REPO_INFO_DUPLICATE_NAME_ERROR, repository.getName());
        }

        // Validate unique field names
        for (RepositoryFieldInfoBO f : repository.getFields())
        {
            String fieldName = f.getName();
            int count = 0;
            for (RepositoryFieldInfoBO f2 : repository.getFields())
            {
                if (f2.getName() == fieldName)
                {
                    count++;
                }
            }
            if (count != 1)
            {
                throw new ChiliLogException(Strings.REPO_INFO_DUPLICATE_FIELD_NAME_ERROR, fieldName, repository.getName());
            }
        }
        
        // Save it
        super.save(db, repository);
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
