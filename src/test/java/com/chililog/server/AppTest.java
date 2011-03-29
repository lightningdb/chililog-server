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

package com.chililog.server;

import static org.junit.Assert.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Pattern;

import org.hornetq.api.core.Message;
import org.hornetq.api.core.client.ClientMessage;
import org.hornetq.api.core.client.ClientProducer;
import org.hornetq.api.core.client.ClientSession;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.chililog.server.data.MongoConnection;
import com.chililog.server.data.RepositoryFieldInfoBO;
import com.chililog.server.data.RepositoryInfoBO;
import com.chililog.server.data.RepositoryInfoController;
import com.chililog.server.data.RepositoryParserInfoBO;
import com.chililog.server.data.RepositoryParserInfoBO.AppliesTo;
import com.chililog.server.data.RepositoryParserInfoBO.ParseFieldErrorHandling;
import com.chililog.server.data.UserBO;
import com.chililog.server.data.UserController;
import com.chililog.server.engine.MqManager;
import com.chililog.server.engine.RepositoryWriter;
import com.chililog.server.engine.parsers.DelimitedEntryParser;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

/**
 * Unit test for simple App.
 */
public class AppTest
{

    private static DB _db;
    private static RepositoryInfoBO _repoInfo;

    private static final String REPOSITORY_NAME = "app_junit_test";
    private static final String MONGODB_COLLECTION_NAME = "app_junit_test_repository";

    @BeforeClass
    public static void classSetup() throws Exception
    {

        // Create repo
        _repoInfo = new RepositoryInfoBO();
        _repoInfo.setName(REPOSITORY_NAME);
        _repoInfo.setDisplayName("Repository Test 1");
        _repoInfo.setReadQueueDurable(false);
        _repoInfo.setWriteQueueDurable(false);
        _repoInfo.setWriteQueueWorkerCount(2);
        
        RepositoryParserInfoBO repoParserInfo = new RepositoryParserInfoBO();
        repoParserInfo.setName("parser1");
        repoParserInfo.setAppliesTo(AppliesTo.All);
        repoParserInfo.setClassName(DelimitedEntryParser.class.getName());
        repoParserInfo.setParseFieldErrorHandling(ParseFieldErrorHandling.SkipEntry);
        repoParserInfo.getProperties().put(DelimitedEntryParser.DELIMITER_PROPERTY_NAME, "|");
        _repoInfo.getParsers().add(repoParserInfo);
        
        RepositoryFieldInfoBO repoFieldInfo = new RepositoryFieldInfoBO();
        repoFieldInfo.setName("field1");
        repoFieldInfo.setDataType(RepositoryFieldInfoBO.DataType.String);
        repoFieldInfo.getProperties().put(DelimitedEntryParser.POSITION_FIELD_PROPERTY_NAME, "1");
        repoParserInfo.getFields().add(repoFieldInfo);

        repoFieldInfo = new RepositoryFieldInfoBO();
        repoFieldInfo.setName("field2");
        repoFieldInfo.setDataType(RepositoryFieldInfoBO.DataType.Integer);
        repoFieldInfo.getProperties().put(DelimitedEntryParser.POSITION_FIELD_PROPERTY_NAME, "2");
        repoParserInfo.getFields().add(repoFieldInfo);

        repoFieldInfo = new RepositoryFieldInfoBO();
        repoFieldInfo.setName("field3");
        repoFieldInfo.setDataType(RepositoryFieldInfoBO.DataType.Long);
        repoFieldInfo.getProperties().put(DelimitedEntryParser.POSITION_FIELD_PROPERTY_NAME, "3");
        repoParserInfo.getFields().add(repoFieldInfo);

        repoFieldInfo = new RepositoryFieldInfoBO();
        repoFieldInfo.setName("field4");
        repoFieldInfo.setDataType(RepositoryFieldInfoBO.DataType.Double);
        repoFieldInfo.getProperties().put(DelimitedEntryParser.POSITION_FIELD_PROPERTY_NAME, "4");
        repoParserInfo.getFields().add(repoFieldInfo);

        repoFieldInfo = new RepositoryFieldInfoBO();
        repoFieldInfo.setName("field5");
        repoFieldInfo.setDataType(RepositoryFieldInfoBO.DataType.Date);
        repoFieldInfo.getProperties().put(DelimitedEntryParser.POSITION_FIELD_PROPERTY_NAME, "5");
        repoFieldInfo.getProperties().put(RepositoryFieldInfoBO.DATE_FORMAT_PROPERTY_NAME, "yyyy-MM-dd HH:mm:ss");
        repoParserInfo.getFields().add(repoFieldInfo);

        repoFieldInfo = new RepositoryFieldInfoBO();
        repoFieldInfo.setName("field6");
        repoFieldInfo.setDataType(RepositoryFieldInfoBO.DataType.Boolean);
        repoFieldInfo.getProperties().put(DelimitedEntryParser.POSITION_FIELD_PROPERTY_NAME, "6");
        repoParserInfo.getFields().add(repoFieldInfo);

        // Database
        _db = MongoConnection.getInstance().getConnection();
        assertNotNull(_db);

        // Clean up old users
        DBCollection coll = _db.getCollection(UserController.MONGODB_COLLECTION_NAME);
        Pattern pattern = Pattern.compile("^AppTestUser[\\w]*$");
        DBObject query = new BasicDBObject();
        query.put("username", pattern);
        coll.remove(query);

        // Clean old repository info
        coll = _db.getCollection(RepositoryInfoController.MONGODB_COLLECTION_NAME);
        pattern = Pattern.compile("^" + REPOSITORY_NAME + "$");
        query = new BasicDBObject();
        query.put("name", pattern);
        coll.remove(query);

        // Clean up old test data if any exists
        coll = _db.getCollection(MONGODB_COLLECTION_NAME);
        if (coll != null)
        {
            coll.drop();
        }

        // Create repository record
        RepositoryInfoController.getInstance().save(_db, _repoInfo);
        
        // Create writer user
        UserBO user = new UserBO();
        user.setUsername("AppTestUser_Writer");
        user.setPassword("222", true);
        user.addRole(_repoInfo.getWriteQueueRole());
        UserController.getInstance().save(_db, user);

        // Create reader user
        user = new UserBO();
        user.setUsername("AppTestUser_Reader");
        user.setPassword("333", true);
        user.addRole(_repoInfo.getReadQueueRole());
        UserController.getInstance().save(_db, user);
    }

    @Before
    public void testSetup() throws Exception
    {
        DBCollection coll = _db.getCollection(MONGODB_COLLECTION_NAME);
        coll.drop();
    }

    @AfterClass
    public static void classTeardown() throws Exception
    {
        // Clean up old users
        DBCollection coll = _db.getCollection(UserController.MONGODB_COLLECTION_NAME);
        Pattern pattern = Pattern.compile("^AppTestUser[\\w]*$");
        DBObject query = new BasicDBObject();
        query.put("username", pattern);
        coll.remove(query);
        
        // Clean old repository info
        coll = _db.getCollection(RepositoryInfoController.MONGODB_COLLECTION_NAME);
        query.put("name", REPOSITORY_NAME);
        coll.remove(query);

        // Clean up old test data if any exists
        coll = _db.getCollection(MONGODB_COLLECTION_NAME);
        coll.drop();
    }

    @Test
    public void test10000() throws Exception
    {
        // Start
        App.startChiliLogServer();

        SimpleDateFormat sf = new SimpleDateFormat(RepositoryWriter.TIMESTAMP_FORMAT);
        sf.setTimeZone(TimeZone.getTimeZone(RepositoryWriter.TIMESTAMP_TIMEZONE));
        
        // Write some repository entries
        ClientSession producerSession = MqManager.getInstance().getTransactionalClientSession("AppTestUser_Writer",
                "222");

        String queueAddress = _repoInfo.getWriteQueueAddress();
        ClientProducer producer = producerSession.createProducer(queueAddress);

        for (int i = 0; i < 10000; i++)
        {
            ClientMessage message = producerSession.createMessage(Message.TEXT_TYPE, false);
            message.putStringProperty(RepositoryWriter.TIMESTAMP_PROPERTY_NAME, sf.format(new Date()));
            message.putStringProperty(RepositoryWriter.SOURCE_PROPERTY_NAME, "AppTest");
            message.putStringProperty(RepositoryWriter.HOST_PROPERTY_NAME, "localhost");
            message.putStringProperty(RepositoryWriter.SEVERITY_PROPERTY_NAME, "3");
            String entry1 = "line" + i + "|2|3|4.4|2001-5-5 5:5:5|True";
            message.getBodyBuffer().writeString(entry1);
            producer.send(message);
            producerSession.commit();
        }

        // Wait for threads to process
        Thread.sleep(5000);

        // Make sure they are in the database
        DBCollection coll = _db.getCollection(MONGODB_COLLECTION_NAME);
        assertEquals(10000, coll.find().count());

        // Stop
        App.stopChiliLogServer();
    }

}
