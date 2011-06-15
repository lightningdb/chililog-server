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

package com.chililog.server.engine;

import java.net.InetAddress;
import java.util.Date;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import com.chililog.server.common.ChiliLogException;
import com.chililog.server.data.BO;
import com.chililog.server.data.MongoConnection;
import com.chililog.server.data.RepositoryEntryBO;
import com.chililog.server.data.RepositoryEntryBO.Severity;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

/**
 * Test our internal log4j appender
 * 
 * @author vibul
 * 
 */
public class InternalLog4JAppenderTest
{
    private DB _db;
    private String _machineName;

    @Before
    public void testSetup() throws Exception
    {
        // Database
        _db = MongoConnection.getInstance().getConnection();
        assertNotNull(_db);

        // Clean up old test data if any exists
        DBCollection coll = _db.getCollection(InternalLog4JAppender.MONGODB_COLLECTION_NAME);
        if (coll != null)
        {
            DBObject query = new BasicDBObject();
            coll.remove(query);
        }

        InetAddress addr = InetAddress.getLocalHost();
        _machineName = addr.getHostName();
    }

    /**
     * Test directory calling appender
     * 
     * @throws Exception
     */
    @Test
    public void testDirect() throws Exception
    {
        InternalLog4JAppender appender = new InternalLog4JAppender();
        Date now = new Date();
        String msg = "debug message";
        Logger logger = Logger.getLogger(InternalLog4JAppenderTest.class);
        LoggingEvent event = new LoggingEvent("com.chililog.server.engine", logger, now.getTime(), Level.DEBUG, msg,
                Thread.currentThread().getName(), null, null, null, null);

        appender.append(event);

        // Check if entry is there
        DBCollection coll = _db.getCollection(InternalLog4JAppender.MONGODB_COLLECTION_NAME);
        assertEquals(1, coll.find().count());

        DBObject dbObject = coll.findOne();

        assertNotNull(dbObject.get(RepositoryEntryBO.TIMESTAMP_FIELD_NAME));
        assertEquals("ChiliLogServer", dbObject.get(RepositoryEntryBO.SOURCE_FIELD_NAME));
        assertEquals(_machineName, dbObject.get(RepositoryEntryBO.HOST_FIELD_NAME));
        assertEquals(Severity.Debug.toCode(), dbObject.get(RepositoryEntryBO.SEVERITY_FIELD_NAME));
        assertEquals(msg, dbObject.get(RepositoryEntryBO.MESSAGE_FIELD_NAME));
        assertEquals((long) 1, dbObject.get(BO.DOCUMENT_VERSION_FIELD_NAME));

        assertEquals(InternalLog4JAppenderTest.class.getName(), dbObject.get(InternalLog4JAppender.CATEGORY_FIELD_NAME));
        assertEquals(Thread.currentThread().getName(), dbObject.get(InternalLog4JAppender.THREAD_FIELD_NAME));

        return;
    }

    /**
     * Test writing via a proper logger
     * 
     * @throws ChiliLogException
     */
    @Test
    public void testViaLogger() throws ChiliLogException
    {
        String msg = "debug message";

        Logger logger = Logger.getLogger(InternalLog4JAppenderTest.class);
        logger.addAppender(new InternalLog4JAppender());
        logger.setLevel(Level.DEBUG);
        logger.debug(msg);

        // Check if entry is there
        DBCollection coll = _db.getCollection(InternalLog4JAppender.MONGODB_COLLECTION_NAME);
        assertEquals(1, coll.find().count());

        DBObject dbObject = coll.findOne();

        assertNotNull(dbObject.get(RepositoryEntryBO.TIMESTAMP_FIELD_NAME));
        assertEquals("ChiliLogServer", dbObject.get(RepositoryEntryBO.SOURCE_FIELD_NAME));
        assertEquals(_machineName, dbObject.get(RepositoryEntryBO.HOST_FIELD_NAME));
        assertEquals(Severity.Debug.toCode(), dbObject.get(RepositoryEntryBO.SEVERITY_FIELD_NAME));
        assertEquals(msg, dbObject.get(RepositoryEntryBO.MESSAGE_FIELD_NAME));
        assertEquals((long) 1, dbObject.get(BO.DOCUMENT_VERSION_FIELD_NAME));

        assertEquals(InternalLog4JAppenderTest.class.getName(), dbObject.get(InternalLog4JAppender.CATEGORY_FIELD_NAME));
        assertEquals(Thread.currentThread().getName(), dbObject.get(InternalLog4JAppender.THREAD_FIELD_NAME));

        return;
    }

    @Test
    public void testNull() throws ChiliLogException, InterruptedException
    {
        // Have to put in unique logger name otherwise we duplicate adding appenders
        Logger logger = Logger.getLogger(InternalLog4JAppenderTest.class.getName() + "_TestNull");
        logger.addAppender(new InternalLog4JAppender());
        logger.setLevel(Level.DEBUG);
        logger.debug(null);

        // Check if entry is there
        DBCollection coll = _db.getCollection(InternalLog4JAppender.MONGODB_COLLECTION_NAME);
        assertEquals(0, coll.find().count());

        return;

    }
}
