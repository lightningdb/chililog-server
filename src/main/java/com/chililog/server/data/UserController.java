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
import org.bson.types.ObjectId;

import com.chililog.server.common.ChiliLogException;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;

/**
 * Singleton to manage our access to the user collection in our mongoDB
 * 
 * @author vibul
 * 
 */
public class UserController extends Controller
{
    public static final String MONGODB_COLLECTION_NAME = "users";

    /**
     * Returns the singleton instance for this class
     */
    public static UserController getInstance()
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
        public static final UserController INSTANCE = new UserController();
    }

    /**
     * <p>
     * Singleton constructor
     * </p>
     */
    private UserController()
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
     * Retrieves the specified user by the id
     * 
     * @param db
     *            mongoDB connection
     * @param id
     *            unique id for the document stored in mongoDB
     * @return code>UserBO</code> representing the user
     * @throws ChiliLogException
     *             if not found or database error
     */
    public UserBO get(DB db, ObjectId id) throws ChiliLogException
    {
        UserBO o = tryGet(db, id);
        if (o == null)
        {
            throw new ChiliLogException(Strings.USER_NOT_FOUND_ERROR, id.toString());
        }
        return o;
    }

    /**
     * Tries to retrieve the specified user by the id. If not found, null is returned.
     * 
     * @param db
     *            mongoDB connection
     * @param id
     *            unique id for the document stored in mongoDB
     * @return <code>UserBO</code> representing the user or null if user is not found
     * @throws ChiliLogException
     *             if database or data error
     */
    public UserBO tryGet(DB db, ObjectId id) throws ChiliLogException
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
            return new UserBO(dbo);
        }
        catch (MongoException ex)
        {
            throw new ChiliLogException(ex, Strings.MONGODB_QUERY_ERROR, ex.getMessage());
        }
    }

    /**
     * Retrieves the specified user by the username
     * 
     * @param db
     *            mongoDB connection
     * @param username
     *            username of user to retrieve
     * @return code>UserBO</code> representing the user
     * @throws ChiliLogException
     *             if not found or database error
     */
    public UserBO getByUsername(DB db, String username) throws ChiliLogException
    {
        UserBO o = tryGetByUsername(db, username);
        if (o == null)
        {
            throw new ChiliLogException(Strings.USER_NOT_FOUND_ERROR, username);
        }
        return o;
    }

    /**
     * Tries to retrieve the specified user by the username. If not found, null is returned.
     * 
     * @param db
     *            mongoDB connection
     * @param username
     *            username of user to retrieve
     * @return <code>UserBO</code> representing the user or null if user is not found
     * @throws ChiliLogException
     *             if database or data error
     */
    public UserBO tryGetByUsername(DB db, String username) throws ChiliLogException
    {
        try
        {
            if (db == null)
            {
                throw new IllegalArgumentException("db cannot be null");
            }
            if (StringUtils.isBlank(username))
            {
                throw new IllegalArgumentException("username cannot be blank");
            }

            DBCollection coll = db.getCollection(MONGODB_COLLECTION_NAME);
            BasicDBObject query = new BasicDBObject();
            query.put(UserBO.USERNAME_FIELD_NAME, username);
            DBObject dbo = coll.findOne(query);
            if (dbo == null)
            {
                return null;
            }
            return new UserBO(dbo);
        }
        catch (MongoException ex)
        {
            throw new ChiliLogException(ex, Strings.MONGODB_QUERY_ERROR, ex.getMessage());
        }
    }

    /**
     * Retrieves the specified user by the email address
     * 
     * @param db
     *            mongoDB connection
     * @param emailAddress
     *            Email address of user to retrieve
     * @return code>UserBO</code> representing the user
     * @throws ChiliLogException
     *             if not found or database error
     */
    public UserBO getByEmailAddress(DB db, String emailAddress) throws ChiliLogException
    {
        UserBO o = tryGetByEmailAddress(db, emailAddress);
        if (o == null)
        {
            throw new ChiliLogException(Strings.USER_NOT_FOUND_ERROR, emailAddress);
        }
        return o;
    }

    /**
     * Tries to retrieve the specified user by the email address. If not found, null is returned.
     * 
     * @param db
     *            mongoDB connection
     * @param emailAddress
     *            Email address of user to retrieve
     * @return <code>UserBO</code> representing the user or null if user is not found
     * @throws ChiliLogException
     *             if database or data error
     */
    public UserBO tryGetByEmailAddress(DB db, String emailAddress) throws ChiliLogException
    {
        try
        {
            if (db == null)
            {
                throw new IllegalArgumentException("db cannot be null");
            }
            if (StringUtils.isBlank(emailAddress))
            {
                throw new IllegalArgumentException("emailAddress cannot be blank");
            }

            DBCollection coll = db.getCollection(MONGODB_COLLECTION_NAME);
            BasicDBObject query = new BasicDBObject();
            query.put(UserBO.EMAIL_ADDRESS_FIELD_NAME, emailAddress);
            DBObject dbo = coll.findOne(query);
            if (dbo == null)
            {
                return null;
            }
            return new UserBO(dbo);
        }
        catch (MongoException ex)
        {
            throw new ChiliLogException(ex, Strings.MONGODB_QUERY_ERROR, ex.getMessage());
        }
    }

    /**
     * Get a list of users
     * 
     * @param db
     *            mongoDB connection
     * @param criteria
     *            criteria to filter users
     * @return List of users matching the specified criteria
     * @throws ChiliLogException
     *             if database or data error
     */
    public ArrayList<UserBO> getList(DB db, UserListCriteria criteria) throws ChiliLogException
    {
        DBCollection coll = db.getCollection(MONGODB_COLLECTION_NAME);

        // Filter
        BasicDBObject condition = new BasicDBObject();
        if (!StringUtils.isBlank(criteria.getUsernamePattern()))
        {
            Pattern pattern = Pattern.compile(criteria.getUsernamePattern());
            condition.put(UserBO.USERNAME_FIELD_NAME, pattern);
        }
        if (!StringUtils.isBlank(criteria.getEmailAddressPattern()))
        {
            Pattern pattern = Pattern.compile(criteria.getEmailAddressPattern());
            condition.put(UserBO.EMAIL_ADDRESS_FIELD_NAME, pattern);
        }
        if (!StringUtils.isBlank(criteria.getRole()))
        {
            condition.put(UserBO.ROLES_FIELD_NAME, criteria.getRole());
        }
        if (criteria.getStatus() != null)
        {
            condition.put(UserBO.STATUS_FIELD_NAME, criteria.getStatus().toString());
        }

        // Order
        DBObject orderBy = new BasicDBObject();
        orderBy.put(UserBO.USERNAME_FIELD_NAME, 1);

        // Get matching records
        int recordsPerPage = criteria.getRecordsPerPage();
        int skipDocumentCount = (criteria.getStartPage() - 1) * recordsPerPage;
        DBCursor cur = coll.find(condition).skip(skipDocumentCount).limit(recordsPerPage).sort(orderBy);
        ArrayList<UserBO> list = new ArrayList<UserBO>();
        while (cur.hasNext())
        {
            DBObject dbo = cur.next();
            list.add(new UserBO(dbo));
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
     * Saves the user into mongoDB
     * 
     * @param db
     *            MongoDb connection
     * @param user
     *            User to save
     * @throws ChiliLogException
     *             if there are errors
     */
    public void save(DB db, UserBO user) throws ChiliLogException
    {
        // Validate unique username
        DBCollection coll = db.getCollection(MONGODB_COLLECTION_NAME);
        BasicDBObject condition = new BasicDBObject();
        condition.put(UserBO.USERNAME_FIELD_NAME, user.getUsername());
        if (user.isExistingRecord())
        {
            condition.put(BO.DOCUMENT_ID_FIELD_NAME, new BasicDBObject("$ne", user.getDocumentID()));
        }
        long i = coll.getCount(condition);
        if (i > 0)
        {
            throw new ChiliLogException(Strings.USER_DUPLICATE_USERNAME_ERROR, user.getUsername());
        }

        // Validate unique email address
        if (!StringUtils.isBlank(user.getEmailAddress()))
        {
            condition = new BasicDBObject();
            condition.put(UserBO.EMAIL_ADDRESS_FIELD_NAME, user.getEmailAddress());
            if (user.isExistingRecord())
            {
                condition.put(BO.DOCUMENT_ID_FIELD_NAME, new BasicDBObject("$ne", user.getDocumentID()));
            }
            i = coll.getCount(condition);
            if (i > 0)
            {
                throw new ChiliLogException(Strings.USER_DUPLICATE_EMAIL_ADDRESS_ERROR, user.getEmailAddress());
            }
        }

        // Save it
        super.save(db, user);
    }

    /**
     * Removes the specified user from mongoDB
     * 
     * @param db
     *            MongoDb connection
     * @param user
     *            User to remove
     * @throws ChiliLogException
     *             if there are errors
     */
    public void remove(DB db, UserBO user) throws ChiliLogException
    {
        super.remove(db, user);
    }
}
