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

package org.chililog.server;

import static org.junit.Assert.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

import org.chililog.server.App;
import org.chililog.server.data.MongoConnection;
import org.chililog.server.data.RepositoryFieldConfigBO;
import org.chililog.server.data.RepositoryConfigBO;
import org.chililog.server.data.RepositoryConfigController;
import org.chililog.server.data.RepositoryParserConfigBO;
import org.chililog.server.data.UserBO;
import org.chililog.server.data.UserController;
import org.chililog.server.data.RepositoryParserConfigBO.AppliesTo;
import org.chililog.server.data.RepositoryParserConfigBO.ParseFieldErrorHandling;
import org.chililog.server.engine.MqService;
import org.chililog.server.engine.RepositoryEntryMqMessage;
import org.chililog.server.engine.parsers.DelimitedEntryParser;
import org.hornetq.api.core.Message;
import org.hornetq.api.core.SimpleString;
import org.hornetq.api.core.client.ClientMessage;
import org.hornetq.api.core.client.ClientProducer;
import org.hornetq.api.core.client.ClientSession;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

/**
 * Unit test for simple App.
 */
public class AppTest {

    private static DB _db;
    private static RepositoryConfigBO _repoInfo;

    private static final String REPOSITORY_NAME = "app_junit_test";
    private static final String MONGODB_COLLECTION_NAME = "repo_app_junit_test";

    @BeforeClass
    public static void classSetup() throws Exception {

        // Create repo
        _repoInfo = new RepositoryConfigBO();
        _repoInfo.setName(REPOSITORY_NAME);
        _repoInfo.setDisplayName("Repository Test 1");
        _repoInfo.setStoreEntriesIndicator(true);
        _repoInfo.setStorageQueueDurableIndicator(false);
        _repoInfo.setStorageQueueWorkerCount(2);

        RepositoryParserConfigBO repoParserInfo = new RepositoryParserConfigBO();
        repoParserInfo.setName("parser1");
        repoParserInfo.setAppliesTo(AppliesTo.All);
        repoParserInfo.setClassName(DelimitedEntryParser.class.getName());
        repoParserInfo.setParseFieldErrorHandling(ParseFieldErrorHandling.SkipEntry);
        repoParserInfo.getProperties().put(DelimitedEntryParser.DELIMITER_PROPERTY_NAME, "|");
        _repoInfo.getParsers().add(repoParserInfo);

        RepositoryFieldConfigBO repoFieldInfo = new RepositoryFieldConfigBO();
        repoFieldInfo.setName("field1");
        repoFieldInfo.setDataType(RepositoryFieldConfigBO.DataType.String);
        repoFieldInfo.getProperties().put(DelimitedEntryParser.POSITION_FIELD_PROPERTY_NAME, "1");
        repoParserInfo.getFields().add(repoFieldInfo);

        repoFieldInfo = new RepositoryFieldConfigBO();
        repoFieldInfo.setName("field2");
        repoFieldInfo.setDataType(RepositoryFieldConfigBO.DataType.Integer);
        repoFieldInfo.getProperties().put(DelimitedEntryParser.POSITION_FIELD_PROPERTY_NAME, "2");
        repoParserInfo.getFields().add(repoFieldInfo);

        repoFieldInfo = new RepositoryFieldConfigBO();
        repoFieldInfo.setName("field3");
        repoFieldInfo.setDataType(RepositoryFieldConfigBO.DataType.Long);
        repoFieldInfo.getProperties().put(DelimitedEntryParser.POSITION_FIELD_PROPERTY_NAME, "3");
        repoParserInfo.getFields().add(repoFieldInfo);

        repoFieldInfo = new RepositoryFieldConfigBO();
        repoFieldInfo.setName("field4");
        repoFieldInfo.setDataType(RepositoryFieldConfigBO.DataType.Double);
        repoFieldInfo.getProperties().put(DelimitedEntryParser.POSITION_FIELD_PROPERTY_NAME, "4");
        repoParserInfo.getFields().add(repoFieldInfo);

        repoFieldInfo = new RepositoryFieldConfigBO();
        repoFieldInfo.setName("field5");
        repoFieldInfo.setDataType(RepositoryFieldConfigBO.DataType.Date);
        repoFieldInfo.getProperties().put(DelimitedEntryParser.POSITION_FIELD_PROPERTY_NAME, "5");
        repoFieldInfo.getProperties().put(RepositoryFieldConfigBO.DATE_FORMAT_PROPERTY_NAME, "yyyy-MM-dd HH:mm:ss");
        repoParserInfo.getFields().add(repoFieldInfo);

        repoFieldInfo = new RepositoryFieldConfigBO();
        repoFieldInfo.setName("field6");
        repoFieldInfo.setDataType(RepositoryFieldConfigBO.DataType.Boolean);
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
        coll = _db.getCollection(RepositoryConfigController.MONGODB_COLLECTION_NAME);
        pattern = Pattern.compile("^" + REPOSITORY_NAME + "$");
        query = new BasicDBObject();
        query.put("name", pattern);
        coll.remove(query);

        // Clean up old test data if any exists
        coll = _db.getCollection(MONGODB_COLLECTION_NAME);
        if (coll != null) {
            coll.drop();
        }

        // Create repository record
        RepositoryConfigController.getInstance().save(_db, _repoInfo);

        // Create publisher user
        UserBO user = new UserBO();
        user.setUsername("AppTestUser_Publisher");
        user.setPassword("222", true);
        user.addRole(_repoInfo.getPublisherRoleName());
        UserController.getInstance().save(_db, user);

        // Create subscriber user
        user = new UserBO();
        user.setUsername("AppTestUser_Subscriber");
        user.setPassword("333", true);
        user.addRole(_repoInfo.getSubscriberRoleName());
        UserController.getInstance().save(_db, user);
    }

    @Before
    public void testSetup() throws Exception {
        DBCollection coll = _db.getCollection(MONGODB_COLLECTION_NAME);
        coll.drop();
    }

    @AfterClass
    public static void classTeardown() throws Exception {
        // Clean up old users
        DBCollection coll = _db.getCollection(UserController.MONGODB_COLLECTION_NAME);
        Pattern pattern = Pattern.compile("^AppTestUser[\\w]*$");
        DBObject query = new BasicDBObject();
        query.put("username", pattern);
        coll.remove(query);

        // Clean old repository info
        coll = _db.getCollection(RepositoryConfigController.MONGODB_COLLECTION_NAME);
        query.put("name", REPOSITORY_NAME);
        coll.remove(query);

        // Clean up old test data if any exists
        coll = _db.getCollection(MONGODB_COLLECTION_NAME);
        coll.drop();
    }

    @Test
    public void test10000() throws Exception {
        // Start
        App.start(null);

        SimpleDateFormat sf = RepositoryEntryMqMessage.getDateFormatter();

        // Write some repository entries
        ClientSession producerSession = MqService.getInstance().getTransactionalClientSession("AppTestUser_Publisher",
                "222");

        String publicationAddress = _repoInfo.getPubSubAddress();
        ClientProducer producer = producerSession.createProducer(publicationAddress);

        for (int i = 0; i < 10000; i++) {
            ClientMessage message = producerSession.createMessage(Message.TEXT_TYPE, false);
            message.putStringProperty(RepositoryEntryMqMessage.TIMESTAMP, sf.format(new Date()));
            message.putStringProperty(RepositoryEntryMqMessage.SOURCE, "AppTest");
            message.putStringProperty(RepositoryEntryMqMessage.HOST, "localhost");
            message.putStringProperty(RepositoryEntryMqMessage.SEVERITY, "3");
            String entry1 = "line" + i + "|2|3|4.4|2001-5-5 5:5:5|True";
            message.getBodyBuffer().writeNullableSimpleString(SimpleString.toSimpleString(entry1));
            producer.send(message);
            producerSession.commit();
        }

        // Wait for threads to process
        Thread.sleep(5000);

        // Make sure they are in the database
        DBCollection coll = _db.getCollection(MONGODB_COLLECTION_NAME);
        assertEquals(10000, coll.find().count());

        // Stop
        App.stop(null);
    }

}
