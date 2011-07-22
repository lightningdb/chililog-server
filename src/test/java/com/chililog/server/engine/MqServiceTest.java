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

import static org.junit.Assert.*;

import java.util.Date;
import java.util.regex.Pattern;

import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;

import org.hornetq.api.core.HornetQException;
import org.hornetq.api.core.Message;
import org.hornetq.api.core.SimpleString;
import org.hornetq.api.core.client.ClientConsumer;
import org.hornetq.api.core.client.ClientMessage;
import org.hornetq.api.core.client.ClientProducer;
import org.hornetq.api.core.client.ClientSession;
import org.hornetq.api.core.client.ClientSession.QueueQuery;
import org.hornetq.api.core.management.ObjectNameBuilder;
import org.hornetq.api.core.management.QueueControl;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.chililog.server.common.Log4JLogger;
import com.chililog.server.data.MongoConnection;
import com.chililog.server.data.UserBO;
import com.chililog.server.data.UserController;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

/**
 * More tests regarding MqManager and transactions
 */
public class MqServiceTest
{
    private static Log4JLogger _logger = Log4JLogger.getLogger(MqServiceTest.class);

    private static DB _db;

    private static final String PUBLISHER_USERNAME = "MqManagerTest.publisher";
    private static final String PUBLISHER_PASSWORD = "pw4publisher!";
    private static final String PUBLISHER_ROLE = "publisher";

    private static final String SUBSCRIBER_USERNAME = "MqManagerTest.subscriber";
    private static final String SUBSCRIBER_PASSWORD = "pw4subscriber!";
    private static final String SUBSCRIBER_ROLE = "subscriber";

    @BeforeClass
    public static void classSetup() throws Exception
    {
        _db = MongoConnection.getInstance().getConnection();

        // Clean up old test data if any exists
        DBCollection coll = _db.getCollection(UserController.MONGODB_COLLECTION_NAME);
        Pattern pattern = Pattern.compile("^MqManagerTest\\.[\\w]*$");
        DBObject query = new BasicDBObject();
        query.put("username", pattern);
        coll.remove(query);

        UserBO user = new UserBO();
        user.setUsername(PUBLISHER_USERNAME);
        user.setPassword(PUBLISHER_PASSWORD, true);
        user.addRole(PUBLISHER_ROLE);
        UserController.getInstance().save(_db, user);
        
        user = new UserBO();
        user.setUsername(SUBSCRIBER_USERNAME);
        user.setPassword(SUBSCRIBER_PASSWORD, true);
        user.addRole(SUBSCRIBER_ROLE);
        UserController.getInstance().save(_db, user);
        
        // Start Mq
        MqService.getInstance().start();

        // Configure security
        MqService.getInstance().addSecuritySettings("MqManagerTest#", PUBLISHER_ROLE, SUBSCRIBER_ROLE);
        MqService.getInstance().getNativeServer().getConfiguration().setSecurityEnabled(true);
    }

    @AfterClass
    public static void classTeardown() throws Exception
    {
        MqService.getInstance().stop();

        // Clean up old test data if any exists
        DBCollection coll = _db.getCollection(UserController.MONGODB_COLLECTION_NAME);
        Pattern pattern = Pattern.compile("^MqManagerTest\\.[\\w]*$");
        DBObject query = new BasicDBObject();
        query.put("username", pattern);
        coll.remove(query);
    }

    @Test
    public void testDeployDeleteQueue() throws Exception
    {
        ClientSession clientSession = MqService.getInstance().getNonTransactionalSystemClientSession();

        QueueQuery q = clientSession.queueQuery(new SimpleString("queue1"));
        assertNotNull(q);
        assertFalse(q.isExists());

        MqService.getInstance().deployQueue("queue1", "queue1", false);

        q = clientSession.queueQuery(new SimpleString("queue1"));
        assertNotNull(q);
        assertTrue(q.isExists());

        // Delete it
        MqService.getInstance().destroyQueue("queue1");
        q = clientSession.queueQuery(new SimpleString("queue1"));
        assertFalse(q.isExists());

        clientSession.close();
    }

    @Test
    public void testDeployQueueTwice() throws Exception
    {
        ClientSession clientSession = MqService.getInstance().getNonTransactionalSystemClientSession();
        MqService.getInstance().deployQueue("queue2", "queue2", false);

        QueueQuery q = clientSession.queueQuery(new SimpleString("queue1"));
        assertNotNull(q);

        // What if we do it twice?
        // We use deploy() so it should be OK. There should not be an exception
        MqService.getInstance().deployQueue("queue2", "queue2", false);

        q = clientSession.queueQuery(new SimpleString("queue1"));
        assertNotNull(q);
    }

    @Test
    public void testGetQueueControl() throws Exception
    {
        MqService.getInstance().deployQueue("queue3", "queue3", false);

        QueueControl qc = MqService.getInstance().getQueueControl("queue3", "queue3");
        assertNotNull(qc);
        assertFalse(qc.isDurable());

        qc = MqService.getInstance().getQueueControl("xxx", "nonexistentqueue");
        assertNull(qc);
    }

    @Test
    public void testNonTransactional() throws Exception
    {
        ClientSession systemSession = MqService.getInstance().getNonTransactionalSystemClientSession();

        ClientSession producerSession = MqService.getInstance().getNonTransactionalClientSession(PUBLISHER_USERNAME,
                PUBLISHER_PASSWORD);
        assertTrue(producerSession.isAutoCommitSends());
        assertTrue(producerSession.isAutoCommitAcks());

        ClientSession consumerSession = MqService.getInstance().getNonTransactionalClientSession(SUBSCRIBER_USERNAME,
                SUBSCRIBER_PASSWORD);
        assertTrue(consumerSession.isAutoCommitSends());
        assertTrue(consumerSession.isAutoCommitAcks());

        String queueAddress = "MqManagerTest.NonTransactional";
        String queueName = "MqManagerTest.NonTransactional";

        // Create queue
        MqService.getInstance().deployQueue(queueAddress, queueName, false);

        // Write
        ClientProducer producer = producerSession.createProducer(queueAddress);
        ClientMessage message = producerSession.createMessage(Message.TEXT_TYPE, false);

        String msg = "Hello sent at " + new Date();
        message.getBodyBuffer().writeNullableSimpleString(SimpleString.toSimpleString(msg));;

        producer.send(message);
        _logger.info("Sent TextMessage: " + msg);

        QueueQuery q = systemSession.queueQuery(new SimpleString(queueName));
        assertNotNull(q);
        assertTrue(q.isExists());
        assertEquals(1, q.getMessageCount());

        // Read
        // Should be able to consume messages because we in the reader role
        ClientConsumer messageConsumer = consumerSession.createConsumer(queueName);
        consumerSession.start();

        ClientMessage messageReceived = messageConsumer.receive(1000);
        String msg2 = messageReceived.getBodyBuffer().readNullableSimpleString().toString();
        _logger.info("Received TextMessage: " + msg2);
        assertEquals(msg, msg2);

        messageReceived.acknowledge();

        messageReceived = messageConsumer.receive(100);
        assertNull(messageReceived);

        // Need close() to force message acknowledgement because the acknowledgement is being batched.
        consumerSession.close();

        q = systemSession.queueQuery(new SimpleString(queueName));
        assertNotNull(q);
        assertTrue(q.isExists());
        assertEquals(0, q.getMessageCount());
    }

    @Test
    public void testTransactional() throws Exception
    {
        ClientSession systemSession = MqService.getInstance().getNonTransactionalSystemClientSession();

        ClientSession producerSession = MqService.getInstance().getTransactionalClientSession(PUBLISHER_USERNAME,
                PUBLISHER_PASSWORD);
        assertFalse(producerSession.isAutoCommitSends());
        assertFalse(producerSession.isAutoCommitAcks());

        ClientSession consumerSession = MqService.getInstance().getTransactionalClientSession(SUBSCRIBER_USERNAME,
                SUBSCRIBER_PASSWORD);
        assertFalse(consumerSession.isAutoCommitSends());
        assertFalse(consumerSession.isAutoCommitAcks());

        String queueAddress = "MqManagerTest.Transactional";
        String queueName = "MqManagerTest.Transactional";

        // Create queue
        MqService.getInstance().deployQueue(queueAddress, queueName, false);

        // Write
        ClientProducer producer = producerSession.createProducer(queueAddress);
        ClientMessage message = producerSession.createMessage(Message.TEXT_TYPE, false);

        String msg = "Hello sent at " + new Date();
        message.getBodyBuffer().writeNullableSimpleString(SimpleString.toSimpleString(msg));

        producer.send(message);
        _logger.info("Sent TextMessage: " + msg);

        QueueQuery q = systemSession.queueQuery(new SimpleString(queueName));
        assertNotNull(q);
        assertTrue(q.isExists());
        assertEquals(0, q.getMessageCount());

        producerSession.commit();

        q = systemSession.queueQuery(new SimpleString(queueName));
        assertNotNull(q);
        assertTrue(q.isExists());
        assertEquals(1, q.getMessageCount());

        // Read
        // Should be able to consume messages because we in the reader role
        ClientConsumer messageConsumer = consumerSession.createConsumer(queueName);
        consumerSession.start();

        ClientMessage messageReceived = messageConsumer.receive(1000);
        String msg2 = messageReceived.getBodyBuffer().readNullableSimpleString().toString();
        _logger.info("Received TextMessage: " + msg2);
        assertEquals(msg, msg2);

        messageReceived.acknowledge();
        consumerSession.commit();

        q = systemSession.queueQuery(new SimpleString(queueName));
        assertNotNull(q);
        assertTrue(q.isExists());
        assertEquals(0, q.getMessageCount());

        messageReceived = messageConsumer.receive(100);
        assertNull(messageReceived);

        // Get count via management API (extract from HornetQ unit test case)
        ObjectName objectName = ObjectNameBuilder.DEFAULT.getQueueObjectName(new SimpleString(queueAddress),
                new SimpleString(queueName));
        QueueControl qc = (QueueControl) MBeanServerInvocationHandler.newProxyInstance(MqService.getInstance()
                .getNativeServer().getMBeanServer(), objectName, QueueControl.class, false);
        assertEquals(0, qc.getMessageCount());

    }

    @Test
    public void testBadPassword() throws Exception
    {
        try
        {
            MqService.getInstance().getTransactionalClientSession(PUBLISHER_USERNAME, "badpassword");
            fail("Exception expected: message=Unable to validate user: MqManagerTest.publisher");
        }
        catch (HornetQException ex)
        {
            assertEquals(105, ex.getCode());
            assertTrue(ex.getMessage().contains("Unable to validate user"));
        }

        try
        {
            MqService.getInstance().getTransactionalClientSession(SUBSCRIBER_USERNAME, "badpassword");
            fail("Exception expected: message=Unable to validate user: MqManagerTest.subscriber");
        }
        catch (HornetQException ex)
        {
            assertEquals(105, ex.getCode());
            assertTrue(ex.getMessage().contains("Unable to validate user"));
        }

    }
}
