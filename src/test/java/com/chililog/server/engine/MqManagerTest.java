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

import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;

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
import com.chililog.server.data.RepositoryInfoBO;

/**
 * More tests regarding MqManager and transactions
 */
public class MqManagerTest
{
    private static Log4JLogger _logger = Log4JLogger.getLogger(MqManagerTest.class);    

    private static final String WRITER_USERNAME = "writer";
    private static final String WRITER_PASSWORD = "pw4writer";
    private static final String WRITER_ROLE = RepositoryInfoBO.createHornetQRoleName(WRITER_USERNAME, WRITER_PASSWORD);
    
    private static final String READER_USERNAME = "reader";
    private static final String READER_PASSWORD = "pw4reader";
    private static final String READER_ROLE = RepositoryInfoBO.createHornetQRoleName(READER_USERNAME, READER_PASSWORD);
    
    @BeforeClass
    public static void classSetup() throws Exception
    {
        MqManager.getInstance().start();

        // Configure security
        MqManager.getInstance().addSecuritySettings("MqManagerTest#", WRITER_ROLE, READER_ROLE);
        MqManager.getInstance().getNativeServer().getConfiguration().setSecurityEnabled(true);
    }

    @AfterClass
    public static void classTeardown() throws Exception
    {
        MqManager.getInstance().stop();
    }

    @Test
    public void testDeployDeleteQueue() throws Exception
    {
        ClientSession clientSession = MqManager.getInstance().getNonTransactionalSystemClientSession();

        QueueQuery q = clientSession.queueQuery(new SimpleString("queue1"));
        assertNotNull(q);
        assertFalse(q.isExists());

        MqManager.getInstance().deployQueue("queue1", "queue1", false);

        q = clientSession.queueQuery(new SimpleString("queue1"));
        assertNotNull(q);
        assertTrue(q.isExists());

        // Delete it
        MqManager.getInstance().destroyQueue("queue1");
        q = clientSession.queueQuery(new SimpleString("queue1"));
        assertFalse(q.isExists());

        clientSession.close();
    }

    @Test
    public void testDeployQueueTwice() throws Exception
    {
        ClientSession clientSession = MqManager.getInstance().getNonTransactionalSystemClientSession();
        MqManager.getInstance().deployQueue("queue2", "queue2", false);

        QueueQuery q = clientSession.queueQuery(new SimpleString("queue1"));
        assertNotNull(q);

        // What if we do it twice?
        // We use deploy() so it should be OK. There should not be an exception
        MqManager.getInstance().deployQueue("queue2", "queue2", false);
        
        q = clientSession.queueQuery(new SimpleString("queue1"));
        assertNotNull(q);
    }
    
    @Test
    public void testGetQueueControl() throws Exception
    {
        MqManager.getInstance().deployQueue("queue3", "queue3", false);

        QueueControl qc = MqManager.getInstance().getQueueControl("queue3", "queue3");
        assertNotNull(qc);
        assertFalse(qc.isDurable());

        qc = MqManager.getInstance().getQueueControl("xxx", "nonexistentqueue");
        assertNull(qc);
    }
    
    @Test
    public void testNonTransactional() throws Exception
    {
        ClientSession systemSession = MqManager.getInstance().getNonTransactionalSystemClientSession();

        ClientSession producerSession = MqManager.getInstance().getNonTransactionalClientSession(
                WRITER_USERNAME, WRITER_PASSWORD);
        assertTrue(producerSession.isAutoCommitSends());
        assertTrue(producerSession.isAutoCommitAcks());

        ClientSession consumerSession = MqManager.getInstance().getNonTransactionalClientSession(
                READER_USERNAME, READER_PASSWORD);
        assertTrue(consumerSession.isAutoCommitSends());
        assertTrue(consumerSession.isAutoCommitAcks());

        String queueAddress = "MqManagerTest.NonTransactional";
        String queueName = "MqManagerTest.NonTransactional";

        // Create queue
        MqManager.getInstance().deployQueue(queueAddress, queueName, false);

        // Write
        ClientProducer producer = producerSession.createProducer(queueAddress);
        ClientMessage message = producerSession.createMessage(Message.TEXT_TYPE, false);

        String msg = "Hello sent at " + new Date();
        message.getBodyBuffer().writeNullableString(msg);

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
        String msg2 = messageReceived.getBodyBuffer().readNullableString();
        _logger.info("Received TextMessage: " + msg2);
        assertEquals(msg, msg2);

        messageReceived.acknowledge();

        messageReceived = messageConsumer.receive(100);
        assertNull(messageReceived);

        // Need close() to force message acknowledgment because the acknowledgment is being batched.
        consumerSession.close();

        q = systemSession.queueQuery(new SimpleString(queueName));
        assertNotNull(q);
        assertTrue(q.isExists());
        assertEquals(0, q.getMessageCount());
    }

    @Test
    public void testTransactional() throws Exception
    {
        ClientSession systemSession = MqManager.getInstance().getNonTransactionalSystemClientSession();

        ClientSession producerSession = MqManager.getInstance().getTransactionalClientSession(
                WRITER_USERNAME, WRITER_PASSWORD);
        assertFalse(producerSession.isAutoCommitSends());
        assertFalse(producerSession.isAutoCommitAcks());

        ClientSession consumerSession = MqManager.getInstance().getTransactionalClientSession(
                READER_USERNAME, READER_PASSWORD);
        assertFalse(consumerSession.isAutoCommitSends());
        assertFalse(consumerSession.isAutoCommitAcks());

        String queueAddress = "MqManagerTest.Transactional";
        String queueName = "MqManagerTest.Transactional";

        // Create queue
        MqManager.getInstance().deployQueue(queueAddress, queueName, false);

        // Write
        ClientProducer producer = producerSession.createProducer(queueAddress);
        ClientMessage message = producerSession.createMessage(Message.TEXT_TYPE,false);

        String msg = "Hello sent at " + new Date();
        message.getBodyBuffer().writeNullableString(msg);
        
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
        String msg2 = messageReceived.getBodyBuffer().readNullableString();
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
        QueueControl qc = (QueueControl) MBeanServerInvocationHandler.newProxyInstance(MqManager.getInstance()
                .getNativeServer().getMBeanServer(), objectName, QueueControl.class, false);
        assertEquals(0, qc.getMessageCount());

    }
}
