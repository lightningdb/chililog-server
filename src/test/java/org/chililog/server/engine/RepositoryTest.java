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

package org.chililog.server.engine;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Pattern;

import org.chililog.server.common.AppProperties;
import org.chililog.server.common.ChiliLogException;
import org.chililog.server.data.MongoConnection;
import org.chililog.server.data.RepositoryFieldConfigBO;
import org.chililog.server.data.RepositoryConfigBO;
import org.chililog.server.data.RepositoryParserConfigBO;
import org.chililog.server.data.UserBO;
import org.chililog.server.data.UserController;
import org.chililog.server.data.RepositoryConfigBO.Status;
import org.chililog.server.data.RepositoryParserConfigBO.AppliesTo;
import org.chililog.server.data.RepositoryParserConfigBO.ParseFieldErrorHandling;
import org.chililog.server.engine.MqService;
import org.chililog.server.engine.Repository;
import org.chililog.server.engine.RepositoryService;
import org.chililog.server.engine.RepositoryStorageWorker;
import org.chililog.server.engine.parsers.DelimitedEntryParser;
import org.hornetq.api.core.HornetQException;
import org.hornetq.api.core.Message;
import org.hornetq.api.core.SimpleString;
import org.hornetq.api.core.client.ClientMessage;
import org.hornetq.api.core.client.ClientProducer;
import org.hornetq.api.core.client.ClientSession;
import org.hornetq.api.core.management.QueueControl;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

/**
 * Test a repository
 * 
 * @author vibul
 * 
 */
public class RepositoryTest {

    private static DB _db;
    private static RepositoryConfigBO _repoConfig;

    private static final String REPOSITORY_NAME = "junit_test";

    private static final String PUBLISHER_USERNAME = "RepositoryTest.publisher";
    private static final String PUBLISHER_PASSWORD = "pw4publisher!";

    private static final String SUBSCRIBER_USERNAME = "RepositoryTest.subscriber";
    private static final String SUBSCRIBER_PASSWORD = "pw4subscriber!";

    private static final String MONGODB_COLLECTION_NAME = "repo_junit_test";

    @BeforeClass
    public static void classSetup() throws Exception {
        _db = MongoConnection.getInstance().getConnection();

        // Clean up old test data if any exists
        DBCollection coll = _db.getCollection(UserController.MONGODB_COLLECTION_NAME);
        Pattern pattern = Pattern.compile("^RepositoryTest\\.[\\w]*$");
        DBObject query = new BasicDBObject();
        query.put("username", pattern);
        coll.remove(query);

        UserBO user = new UserBO();
        user.setUsername(PUBLISHER_USERNAME);
        user.setPassword(PUBLISHER_PASSWORD, true);
        user.addRole(UserBO.createRepositoryPublisherRoleName(REPOSITORY_NAME));
        UserController.getInstance().save(_db, user);

        user = new UserBO();
        user.setUsername(SUBSCRIBER_USERNAME);
        user.setPassword(SUBSCRIBER_PASSWORD, true);
        user.addRole(UserBO.createRepositoryPublisherRoleName(REPOSITORY_NAME));
        UserController.getInstance().save(_db, user);

        // Create repo
        _repoConfig = new RepositoryConfigBO();
        _repoConfig.setName(REPOSITORY_NAME);
        _repoConfig.setDisplayName("Repository Test 1");
        _repoConfig.setStoreEntriesIndicator(true);
        _repoConfig.setStorageQueueDurableIndicator(false);
        _repoConfig.setStorageQueueWorkerCount(2);

        RepositoryParserConfigBO repoParserConfig = new RepositoryParserConfigBO();
        repoParserConfig.setName("parser1");
        repoParserConfig.setAppliesTo(AppliesTo.All);
        repoParserConfig.setClassName(DelimitedEntryParser.class.getName());
        repoParserConfig.setParseFieldErrorHandling(ParseFieldErrorHandling.SkipEntry);
        repoParserConfig.getProperties().put(DelimitedEntryParser.DELIMITER_PROPERTY_NAME, "|");
        _repoConfig.getParsers().add(repoParserConfig);

        RepositoryFieldConfigBO repoFieldConfig = new RepositoryFieldConfigBO();
        repoFieldConfig.setName("field1");
        repoFieldConfig.setDataType(RepositoryFieldConfigBO.DataType.String);
        repoFieldConfig.getProperties().put(DelimitedEntryParser.POSITION_FIELD_PROPERTY_NAME, "1");
        repoParserConfig.getFields().add(repoFieldConfig);

        repoFieldConfig = new RepositoryFieldConfigBO();
        repoFieldConfig.setName("field2");
        repoFieldConfig.setDataType(RepositoryFieldConfigBO.DataType.Integer);
        repoFieldConfig.getProperties().put(DelimitedEntryParser.POSITION_FIELD_PROPERTY_NAME, "2");
        repoParserConfig.getFields().add(repoFieldConfig);

        repoFieldConfig = new RepositoryFieldConfigBO();
        repoFieldConfig.setName("field3");
        repoFieldConfig.setDataType(RepositoryFieldConfigBO.DataType.Long);
        repoFieldConfig.getProperties().put(DelimitedEntryParser.POSITION_FIELD_PROPERTY_NAME, "3");
        repoParserConfig.getFields().add(repoFieldConfig);

        repoFieldConfig = new RepositoryFieldConfigBO();
        repoFieldConfig.setName("field4");
        repoFieldConfig.setDataType(RepositoryFieldConfigBO.DataType.Double);
        repoFieldConfig.getProperties().put(DelimitedEntryParser.POSITION_FIELD_PROPERTY_NAME, "4");
        repoParserConfig.getFields().add(repoFieldConfig);

        repoFieldConfig = new RepositoryFieldConfigBO();
        repoFieldConfig.setName("field5");
        repoFieldConfig.setDataType(RepositoryFieldConfigBO.DataType.Date);
        repoFieldConfig.getProperties().put(DelimitedEntryParser.POSITION_FIELD_PROPERTY_NAME, "5");
        repoFieldConfig.getProperties().put(RepositoryFieldConfigBO.DATE_FORMAT_PROPERTY_NAME, "yyyy-MM-dd HH:mm:ss");
        repoParserConfig.getFields().add(repoFieldConfig);

        repoFieldConfig = new RepositoryFieldConfigBO();
        repoFieldConfig.setName("field6");
        repoFieldConfig.setDataType(RepositoryFieldConfigBO.DataType.Boolean);
        repoFieldConfig.getProperties().put(DelimitedEntryParser.POSITION_FIELD_PROPERTY_NAME, "6");
        repoParserConfig.getFields().add(repoFieldConfig);

        // Database
        _db = MongoConnection.getInstance().getConnection();
        assertNotNull(_db);
    }

    @Before
    public void testSetup() throws Exception {
        DBCollection coll = _db.getCollection(MONGODB_COLLECTION_NAME);
        coll.drop();
    }

    @AfterClass
    public static void classTeardown() throws Exception {
        // Clean up old test data if any exists
        DBCollection coll = _db.getCollection(MONGODB_COLLECTION_NAME);
        coll.drop();

        coll = _db.getCollection(UserController.MONGODB_COLLECTION_NAME);
        Pattern pattern = Pattern.compile("^RepositoryTest\\.[\\w]*$");
        DBObject query = new BasicDBObject();
        query.put("username", pattern);
        coll.remove(query);
    }

    @Test
    public void testBasicOK() throws Exception {
        SimpleDateFormat sf = new SimpleDateFormat(RepositoryStorageWorker.TIMESTAMP_FORMAT);
        sf.setTimeZone(TimeZone.getTimeZone(RepositoryStorageWorker.TIMESTAMP_TIMEZONE));

        // Start
        MqService.getInstance().start();
        Repository repo = new Repository(_repoConfig);
        repo.bringOnline();
        assertEquals(Status.ONLINE, repo.getStatus());

        // Write some repository entries
        ClientSession producerSession = MqService.getInstance().getTransactionalClientSession(PUBLISHER_USERNAME,
                PUBLISHER_PASSWORD);

        String queueAddress = _repoConfig.getPubSubAddress();
        ClientProducer producer = producerSession.createProducer(queueAddress);

        ClientMessage message = producerSession.createMessage(Message.TEXT_TYPE, false);
        message.putStringProperty(RepositoryStorageWorker.TIMESTAMP_PROPERTY_NAME, sf.format(new Date()));
        message.putStringProperty(RepositoryStorageWorker.SOURCE_PROPERTY_NAME, "RepositoryTest");
        message.putStringProperty(RepositoryStorageWorker.HOST_PROPERTY_NAME, "localhost");
        message.putStringProperty(RepositoryStorageWorker.SEVERITY_PROPERTY_NAME, "1");
        String entry1 = "line1|2|3|4.4|2001-5-5 5:5:5|True";
        message.getBodyBuffer().writeNullableSimpleString(SimpleString.toSimpleString(entry1));
        producer.send(message);

        message = producerSession.createMessage(Message.TEXT_TYPE, false);
        message.putStringProperty(RepositoryStorageWorker.TIMESTAMP_PROPERTY_NAME, sf.format(new Date()));
        message.putStringProperty(RepositoryStorageWorker.SOURCE_PROPERTY_NAME, "RepositoryTest");
        message.putStringProperty(RepositoryStorageWorker.HOST_PROPERTY_NAME, "localhost");
        message.putStringProperty(RepositoryStorageWorker.SEVERITY_PROPERTY_NAME, "2");
        String entry2 = "line2|2|3|4.4|2001-5-5 5:5:5|True";
        message.getBodyBuffer().writeNullableSimpleString(SimpleString.toSimpleString(entry2));
        producer.send(message);

        message = producerSession.createMessage(Message.TEXT_TYPE, false);
        message.putStringProperty(RepositoryStorageWorker.TIMESTAMP_PROPERTY_NAME, sf.format(new Date()));
        message.putStringProperty(RepositoryStorageWorker.SOURCE_PROPERTY_NAME, "RepositoryTest");
        message.putStringProperty(RepositoryStorageWorker.HOST_PROPERTY_NAME, "localhost");
        message.putStringProperty(RepositoryStorageWorker.SEVERITY_PROPERTY_NAME, "3");
        String entry3 = "line3|2|3|4.4|2001-5-5 5:5:5|True";
        message.getBodyBuffer().writeNullableSimpleString(SimpleString.toSimpleString(entry3));
        producer.send(message);

        producerSession.commit();

        // Wait for threads to process
        Thread.sleep(3000);

        // Make sure they are in the database
        DBCollection coll = _db.getCollection(MONGODB_COLLECTION_NAME);
        assertEquals(3, coll.find().count());

        // Stop
        repo.takeOffline();
        assertEquals(Status.OFFLINE, repo.getStatus());
        MqService.getInstance().stop();
    }

    @Test
    public void testUpdateRepositoryConfig() throws Exception {
        SimpleDateFormat sf = new SimpleDateFormat(RepositoryStorageWorker.TIMESTAMP_FORMAT);
        sf.setTimeZone(TimeZone.getTimeZone(RepositoryStorageWorker.TIMESTAMP_TIMEZONE));

        // Start
        MqService.getInstance().start();
        Repository repo = new Repository(_repoConfig);
        repo.bringOnline();
        assertEquals(Status.ONLINE, repo.getStatus());

        // Try to update repo - should error because it is not off line
        // Simulate we getting new repoConfig from the DB
        try {
            repo.setRepoConfig(_repoConfig);
            fail();
        } catch (Exception ex) {
            assertEquals(ChiliLogException.class, ex.getClass());
        }

        // Always wait for writer threads to properly start
        Thread.sleep(1000);
        assertEquals(2, repo.getStorageWorkers().size());

        // Stop
        repo.takeOffline();
        assertEquals(Status.OFFLINE, repo.getStatus());

        // Update worker count from 2 to 10
        _repoConfig.setStorageQueueWorkerCount(10);

        // Restart
        repo = new Repository(_repoConfig);
        repo.bringOnline();

        // Write 10,000 repository entries
        ClientSession producerSession = MqService.getInstance().getTransactionalClientSession(PUBLISHER_USERNAME,
                PUBLISHER_PASSWORD);

        String queueAddress = _repoConfig.getPubSubAddress();
        ClientProducer producer = producerSession.createProducer(queueAddress);

        for (int i = 1; i <= 10000; i++) {
            ClientMessage message = producerSession.createMessage(Message.TEXT_TYPE, false);
            message.putStringProperty(RepositoryStorageWorker.TIMESTAMP_PROPERTY_NAME, sf.format(new Date()));
            message.putStringProperty(RepositoryStorageWorker.SOURCE_PROPERTY_NAME, "RepositoryTest");
            message.putStringProperty(RepositoryStorageWorker.HOST_PROPERTY_NAME, "localhost");
            message.putStringProperty(RepositoryStorageWorker.SEVERITY_PROPERTY_NAME, "3");
            String entry1 = "line" + i + "|2|3|4.4|2001-5-5 5:5:5|True";
            message.getBodyBuffer().writeNullableSimpleString(SimpleString.toSimpleString(entry1));
            ;
            producer.send(message);
            producerSession.commit();
        }

        // Wait for threads to process
        Thread.sleep(5000);

        // Check that threads are still running
        for (RepositoryStorageWorker rw : repo.getStorageWorkers()) {
            assertTrue(rw.isRunning());
        }
        assertEquals(10, repo.getStorageWorkers().size());

        // Make sure that we've processed all the messages
        QueueControl qc = MqService.getInstance().getQueueControl(repo.getRepoConfig().getPubSubAddress(),
                repo.getRepoConfig().getStorageQueueName());
        assertEquals(0, qc.getMessageCount());

        // Make sure they are in the database
        DBCollection coll = _db.getCollection(MONGODB_COLLECTION_NAME);
        assertEquals(10000, coll.find().count());

        // Stop
        repo.takeOffline();
        assertEquals(Status.OFFLINE, repo.getStatus());
        MqService.getInstance().stop();

        // Reset count
        _repoConfig.setStorageQueueWorkerCount(2);
    }

    @Test
    public void testRepositoryStatusSwitching() throws Exception {
        SimpleDateFormat sf = new SimpleDateFormat(RepositoryStorageWorker.TIMESTAMP_FORMAT);
        sf.setTimeZone(TimeZone.getTimeZone(RepositoryStorageWorker.TIMESTAMP_TIMEZONE));

        // ************************************************************************************************************
        // ONLINE
        // ************************************************************************************************************
        MqService.getInstance().start();
        Repository repo = new Repository(_repoConfig);
        repo.bringOnline();
        assertEquals(Status.ONLINE, repo.getStatus());

        // try to bring online again - should error
        try {
            repo.bringOnline();
            fail();
        } catch (Exception ex) {
            assertEquals(ChiliLogException.class, ex.getClass());
        }

        // Write some repository entries
        ClientSession producerSession = MqService.getInstance().getTransactionalClientSession(PUBLISHER_USERNAME,
                PUBLISHER_PASSWORD);

        String queueAddress = _repoConfig.getPubSubAddress();
        ClientProducer producer = producerSession.createProducer(queueAddress);

        for (int i = 1; i <= 10; i++) {
            ClientMessage message = producerSession.createMessage(Message.TEXT_TYPE, false);
            message.putStringProperty(RepositoryStorageWorker.TIMESTAMP_PROPERTY_NAME, sf.format(new Date()));
            message.putStringProperty(RepositoryStorageWorker.SOURCE_PROPERTY_NAME, "RepositoryTest");
            message.putStringProperty(RepositoryStorageWorker.HOST_PROPERTY_NAME, "localhost");
            message.putStringProperty(RepositoryStorageWorker.SEVERITY_PROPERTY_NAME, "3");
            String entry1 = "line" + i + "|2|3|4.4|2001-5-5 5:5:5|True";
            message.getBodyBuffer().writeNullableSimpleString(SimpleString.toSimpleString(entry1));
            ;
            producer.send(message);
            producerSession.commit();
        }

        // Make sure that we've processed all the messages
        Thread.sleep(3000);
        QueueControl qc = MqService.getInstance().getQueueControl(repo.getRepoConfig().getPubSubAddress(),
                repo.getRepoConfig().getStorageQueueName());
        assertEquals(0, qc.getMessageCount());

        // Make sure they are in the database
        DBCollection coll = _db.getCollection(MONGODB_COLLECTION_NAME);
        assertEquals(10, coll.find().count());

        // ************************************************************************************************************
        // STOP
        // ************************************************************************************************************
        repo.takeOffline();
        assertEquals(Status.OFFLINE, repo.getStatus());

        // Offline again - should be no errors
        repo.takeOffline();
        assertEquals(Status.OFFLINE, repo.getStatus());

        // Sending a message after stopping should result in an error
        // Have to wait for at least 1 seconds for credentials cache to timeout
        // security-invalidation-interval defaults to 0 milliseconds
        Thread.sleep(1000);
        try {
            ClientMessage message = producerSession.createMessage(Message.TEXT_TYPE, false);
            message.putStringProperty(RepositoryStorageWorker.TIMESTAMP_PROPERTY_NAME, sf.format(new Date()));
            message.putStringProperty(RepositoryStorageWorker.SOURCE_PROPERTY_NAME, "RepositoryTest");
            message.putStringProperty(RepositoryStorageWorker.HOST_PROPERTY_NAME, "localhost");
            message.putStringProperty(RepositoryStorageWorker.SEVERITY_PROPERTY_NAME, "3");
            String entry1 = "lineXXX|2|3|4.4|2001-5-5 5:5:5|True";
            message.getBodyBuffer().writeNullableSimpleString(SimpleString.toSimpleString(entry1));
            ;
            producer.send(message);
            producerSession.commit();
        } catch (Exception ex) {
            // HornetQException[errorCode=105 message=User: junit_test doesn't have permission='SEND' on address
            // repo.junit_test]
            assertEquals(HornetQException.class, ex.getClass());
            assertEquals(HornetQException.SECURITY_EXCEPTION, ((HornetQException) ex).getCode());
        }

        // Check that there are no threads are still running
        for (RepositoryStorageWorker rw : repo.getStorageWorkers()) {
            assertTrue(!rw.isRunning());
        }
        assertEquals(0, repo.getStorageWorkers().size());

        // ************************************************************************************************************
        // READONLU
        // ************************************************************************************************************
        repo.makeReadonly();
        assertEquals(Status.READONLY, repo.getStatus());

        // Offline again - should be no errors
        repo.makeReadonly();
        assertEquals(Status.READONLY, repo.getStatus());

        // Sending a message after stopping should result in an error
        // Have to wait for at least 1 seconds for credentials cache to timeout
        // security-invalidation-interval defaults to 0 milliseconds
        Thread.sleep(1000);
        try {
            ClientMessage message = producerSession.createMessage(Message.TEXT_TYPE, false);
            message.putStringProperty(RepositoryStorageWorker.TIMESTAMP_PROPERTY_NAME, sf.format(new Date()));
            message.putStringProperty(RepositoryStorageWorker.SOURCE_PROPERTY_NAME, "RepositoryTest");
            message.putStringProperty(RepositoryStorageWorker.HOST_PROPERTY_NAME, "localhost");
            message.putStringProperty(RepositoryStorageWorker.SEVERITY_PROPERTY_NAME, "3");
            String entry1 = "lineXXX|2|3|4.4|2001-5-5 5:5:5|True";
            message.getBodyBuffer().writeNullableSimpleString(SimpleString.toSimpleString(entry1));
            ;
            producer.send(message);
            producerSession.commit();
        } catch (Exception ex) {
            // HornetQException[errorCode=105 message=User: junit_test doesn't have permission='SEND' on address
            // repo.junit_test]
            assertEquals(HornetQException.class, ex.getClass());
            assertEquals(HornetQException.SECURITY_EXCEPTION, ((HornetQException) ex).getCode());
        }

        // ************************************************************************************************************
        // Stop
        // ************************************************************************************************************
        producer.close();
        producerSession.close();
        MqService.getInstance().stop();

        // Reset count
        _repoConfig.setStorageQueueWorkerCount(2);
    }

    @Test
    public void testBadEntries() throws Exception {
        String deadLetterAddress = AppProperties.getInstance().getMqDeadLetterAddress();
        SimpleDateFormat sf = new SimpleDateFormat(RepositoryStorageWorker.TIMESTAMP_FORMAT);
        sf.setTimeZone(TimeZone.getTimeZone(RepositoryStorageWorker.TIMESTAMP_TIMEZONE));

        // Start
        MqService.getInstance().start();
        Repository repo = new Repository(_repoConfig);
        repo.bringOnline();
        assertEquals(Status.ONLINE, repo.getStatus());

        // Create a dead letter queue
        MqService.getInstance().deployQueue(deadLetterAddress, "dead_letters.junit_test", false);

        // Have to wait for at least 1 seconds for credentials cache to timeout
        // security-invalidation-interval defaults to 0 milliseconds
        Thread.sleep(1000);

        // Write some repository entries
        ClientSession producerSession = MqService.getInstance().getTransactionalClientSession(PUBLISHER_USERNAME,
                PUBLISHER_PASSWORD);

        String queueAddress = _repoConfig.getPubSubAddress();
        ClientProducer producer = producerSession.createProducer(queueAddress);

        // Write some good entries
        for (int i = 1; i <= 100; i++) {
            ClientMessage message = producerSession.createMessage(Message.TEXT_TYPE, false);
            message.putStringProperty(RepositoryStorageWorker.TIMESTAMP_PROPERTY_NAME, sf.format(new Date()));
            message.putStringProperty(RepositoryStorageWorker.SOURCE_PROPERTY_NAME, "RepositoryTest");
            message.putStringProperty(RepositoryStorageWorker.HOST_PROPERTY_NAME, "localhost");
            message.putStringProperty(RepositoryStorageWorker.SEVERITY_PROPERTY_NAME, "Debug");
            String entry1 = "line" + i + "|2|3|4.4|2001-5-5 5:5:5|True";
            if (i == 33) {
                entry1 = i + " - bad entry no delimiter";
            }
            message.getBodyBuffer().writeNullableSimpleString(SimpleString.toSimpleString(entry1));
            ;
            producer.send(message);
            producerSession.commit();
        }

        // Wait for threads to process
        Thread.sleep(3000);

        // Check that threads are still running
        for (RepositoryStorageWorker rw : repo.getStorageWorkers()) {
            assertTrue(rw.isRunning());
        }
        assertEquals(2, repo.getStorageWorkers().size());

        // Make sure that bad entries have been removed from the write queue
        QueueControl qc = MqService.getInstance().getQueueControl(repo.getRepoConfig().getPubSubAddress(),
                repo.getRepoConfig().getStorageQueueName());
        assertEquals(0, qc.getMessageCount());

        // Make sure that the bad entry ends up in the dead letter queue
        qc = MqService.getInstance().getQueueControl(deadLetterAddress, "dead_letters.junit_test");
        assertEquals((long) 1, qc.getMessageCount());

        // Make sure that only good entries have been saved to the DB
        DBCollection coll = _db.getCollection(MONGODB_COLLECTION_NAME);
        assertEquals(99, coll.find().count());

        // Stop
        repo.takeOffline();
        assertEquals(Status.OFFLINE, repo.getStatus());
        MqService.getInstance().stop();

    }

    @Test
    public void testRepositoryService() throws Exception {
        // Start queues
        MqService.getInstance().start();

        // Start
        RepositoryService.getInstance().start();
        Repository[] repos = RepositoryService.getInstance().getRepositories();
        for (Repository r : repos) {
            if (r.getRepoConfig().getStartupStatus() == Status.ONLINE) {
                assertEquals(Status.ONLINE, r.getStatus());
            } else if (r.getRepoConfig().getStartupStatus() == Status.READONLY) {
                assertEquals(Status.READONLY, r.getStatus());
            } else {
                assertEquals(Status.OFFLINE, r.getStatus());
            }
        }

        // Start again - should not error
        RepositoryService.getInstance().start();
        Repository[] repos2 = RepositoryService.getInstance().getRepositories();
        for (Repository r : repos2) {
            if (r.getRepoConfig().getStartupStatus() == Status.ONLINE) {
                assertEquals(Status.ONLINE, r.getStatus());
            } else if (r.getRepoConfig().getStartupStatus() == Status.READONLY) {
                assertEquals(Status.READONLY, r.getStatus());
            } else {
                assertEquals(Status.OFFLINE, r.getStatus());
            }
        }
        assertEquals(repos.length, repos2.length);

        // Stop
        RepositoryService.getInstance().stop();
        repos2 = RepositoryService.getInstance().getRepositories();
        for (Repository r : repos2) {
            assertEquals(Status.OFFLINE, r.getStatus());
        }
        assertEquals(repos.length, repos2.length);

        // Stop again
        RepositoryService.getInstance().stop();
        repos2 = RepositoryService.getInstance().getRepositories();
        for (Repository r : repos2) {
            assertEquals(Status.OFFLINE, r.getStatus());
        }
        assertEquals(repos.length, repos2.length);

        // Stop queues
        MqService.getInstance().stop();
    }
}
