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

import java.net.UnknownHostException;

import org.apache.log4j.Logger;

import com.chililog.server.common.AppProperties;
import com.chililog.server.common.ChiliLogException;
import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

/**
 * Singleton to manage our connection to the mongoDB database
 * 
 * @author vibul
 * 
 */
public class MongoConnection
{
    private static Logger _logger = Logger.getLogger(MongoConnection.class);
    private Mongo _mongo = null;

    /**
     * Returns the singleton instance for this class
     * 
     * @return
     */
    public static MongoConnection getInstance()
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
        public static final MongoConnection INSTANCE = new MongoConnection();
    }

    /**
     * <p>
     * Singleton constructor that parses and loads the required application properties.
     * </p>
     * 
     * <p>
     * If there are any errors, the JVM is terminated. Without valid application properties, we will fall over elsewhere
     * so might as well terminate here.
     * </p>
     */
    private MongoConnection()
    {
        try
        {
            loadMongo();
        }
        catch (Exception e)
        {
            _logger.error("Error connecting to mongoDB: " + e.getMessage(), e);
            System.exit(1);
        }
    }

    /**
     * <p>
     * Loads our mongoDB connection pool
     * </p>
     * <p>
     * This is a package scope method so that we can use it within our junit test cases. It should not be called from
     * real code.
     * </p>
     * 
     * @throws MongoException
     * @throws UnknownHostException
     * 
     */
    void loadMongo() throws UnknownHostException, MongoException
    {
        AppProperties appProperties = AppProperties.getInstance();
        _mongo = new Mongo(appProperties.getDbIpAddress(), appProperties.getDbIpPort());
    }

    /**
     * Returns a connection to the Mongo database as specified in the app.properties file
     * 
     * @return Mongo Database connection
     * @throws ChiliLogException
     *             if connection or authentication fails
     */
    public DB getConnection() throws ChiliLogException
    {
        AppProperties appProperties = AppProperties.getInstance();
        return getConnection(appProperties.getDbName(), appProperties.getDbUserName(), appProperties.getDbPassword());
    }

    /**
     * Returns a connection to the Mongo database as specified in the app.properties file
     * 
     * @param dbName
     *            database name
     * @param username
     *            username for database
     * @param password
     *            password for database
     * @return Mongo Database connection
     * @throws ChiliLogException
     *             if connection or authentication fails
     */
    public DB getConnection(String dbName, String username, String password) throws ChiliLogException
    {
        try
        {
            DB db = _mongo.getDB(dbName);
            
            // Quirk in mongoDB driver, for some reason we can't authenticate twice
            // "can't call authenticate twice on the same DBObject" exception
            if (!db.isAuthenticated())
            {
                if (!db.authenticate(username, password.toCharArray()))
                {
                    throw new ChiliLogException(Strings.MONGODB_AUTHENTICATION_ERROR, dbName);
                }
            }
            return db;
        }
        catch (MongoException ex)
        {
            throw new ChiliLogException(ex, Strings.MONGODB_CONNECTION_ERROR, ex.getMessage());
        }
    }
}
