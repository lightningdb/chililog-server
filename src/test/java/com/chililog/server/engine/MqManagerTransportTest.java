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
import java.util.HashMap;
import java.util.regex.Pattern;

import org.hornetq.api.core.HornetQBuffer;
import org.hornetq.api.core.HornetQException;
import org.hornetq.api.core.SimpleString;
import org.hornetq.api.core.TransportConfiguration;
import org.hornetq.api.core.client.*;
import org.hornetq.api.core.client.ClientSession.QueueQuery;
import org.hornetq.api.core.management.HornetQServerControl;
import org.hornetq.core.message.impl.MessageImpl;
import org.hornetq.core.remoting.impl.invm.InVMConnectorFactory;
import org.hornetq.core.remoting.impl.netty.NettyConnectorFactory;
import org.hornetq.utils.DataConstants;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.chililog.client.stomp.Client;
import com.chililog.client.stomp.TestListener;
import com.chililog.server.common.Log4JLogger;
import com.chililog.server.data.MongoConnection;
import com.chililog.server.data.UserBO;
import com.chililog.server.data.UserController;
import com.chililog.server.engine.MqManager;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

/**
 * Test case for HornetQ integration for different transports
 * 
 * @author vibul
 * 
 */
public class MqManagerTransportTest
{
    private static Log4JLogger _logger = Log4JLogger.getLogger(MqManagerTransportTest.class);

    private static DB _db;

    private static ClientSessionFactory _inVmClientSessionFactory;
    private static ClientSessionFactory _coreClientSessionFactory;

    @BeforeClass
    public static void classSetup() throws Exception
    {
        _db = MongoConnection.getInstance().getConnection();
        assertNotNull(_db);

        // Clean up old test data if any exists
        DBCollection coll = _db.getCollection(UserController.MONGODB_COLLECTION_NAME);
        Pattern pattern = Pattern.compile("^MqTransportTestUser[\\w]*$");
        DBObject query = new BasicDBObject();
        query.put("username", pattern);
        coll.remove(query);

        // Create system management user
        UserBO user = new UserBO();
        user.setUsername("MqTransportTestUser_System");
        user.setPassword("111", true);
        user.addRole("system");
        UserController.getInstance().save(_db, user);

        // Create writer user
        user = new UserBO();
        user.setUsername("MqTransportTestUser_Writer");
        user.setPassword("222", true);
        user.addRole("writer");
        UserController.getInstance().save(_db, user);

        // Create reader user
        user = new UserBO();
        user.setUsername("MqTransportTestUser_Reader");
        user.setPassword("333", true);
        user.addRole("reader");
        UserController.getInstance().save(_db, user);

        MqManager.getInstance().start();

        // Configure security
        HornetQServerControl hqControl = MqManager.getInstance().getNativeServer().getHornetQServerControl();
        hqControl.addSecuritySettings("MqTransportTest#", "writer", "reader", "system", "system", "system", "system",
                "system, writer, reader");
        MqManager.getInstance().getNativeServer().getConfiguration().setSecurityEnabled(true);

        // IN VM connector
        ServerLocator sl = HornetQClient.createServerLocatorWithoutHA(new TransportConfiguration(
                InVMConnectorFactory.class.getName()));
        _inVmClientSessionFactory = sl.createSessionFactory();

        // Remote Core connector for access over TCP/IP
        ServerLocator sl2 = HornetQClient.createServerLocatorWithoutHA(new TransportConfiguration(
                NettyConnectorFactory.class.getName()));
        _coreClientSessionFactory = sl2.createSessionFactory();

    }

    @AfterClass
    public static void classTeardown() throws Exception
    {
        MqManager.getInstance().stop();

        // Clean up old test data if any exists
        DBCollection coll = _db.getCollection(UserController.MONGODB_COLLECTION_NAME);
        Pattern pattern = Pattern.compile("^MqTransportTestUser[\\w]*$");
        DBObject query = new BasicDBObject();
        query.put("username", pattern);
        coll.remove(query);
    }

    /**
     * Test supplying a bad password
     * 
     * @param csf
     * @throws Exception
     */
    public void testBadPassword(ClientSessionFactory csf) throws Exception
    {
        try
        {
            ClientSession s = csf.createSession("MqTransportTestUser_System", "badpassword", false, false, false,
                    false, csf.getServerLocator().getAckBatchSize());
            if (s != null)
            {
                s.close();
            }
            fail("Exception expected");
        }
        catch (Exception ex)
        {
            // HornetQException[errorCode=105 message=Unable to validate user: MqTransportTestUser_System]
            assertEquals(HornetQException.class.getName(), ex.getClass().getName());
            assertEquals(105, ((HornetQException) ex).getCode());
        }
    }

    @Test
    public void testBadPassword_InVM() throws Exception
    {
        testBadPassword(_inVmClientSessionFactory);
    }

    @Test
    public void testBadPassword_Core() throws Exception
    {
        testBadPassword(_coreClientSessionFactory);
    }

    // TODO Bug in HornetQ means exception not thrown
    // @Test (expected=LoginException.class)
    public void testBassPassword_Stomp() throws Exception
    {
        new Client("localhost", 61613, "ser", "ser");
    }

    /**
     * Test reading/writing
     * 
     * @throws Exception
     */
    public void testOK(ClientSessionFactory csf, String type) throws Exception
    {

        // ************************************
        // Create queue OK
        // ************************************
        // Create a core queue
        String queueAddress = "MqTransportTest" + type;
        String queueName = "MqTransportTest" + type;
        ClientSession coreSession = null;
        try
        {
            coreSession = csf.createSession("MqTransportTestUser_System", "111", false, false, false, false, csf
                    .getServerLocator().getAckBatchSize());

            coreSession.createQueue(queueAddress, queueName, true);

            // Make sure the queue is there
            QueueQuery q = coreSession.queueQuery(new SimpleString(queueAddress));
            assertNotNull(q);
            assertEquals(queueAddress, q.getAddress().toString());
        }
        finally
        {
            if (coreSession != null)
            {
                coreSession.close();
            }
        }

        // Read/Write to queue
        ClientSession session = null;
        try
        {
            // ************************************
            // Write OK
            // ************************************
            // Create the session, and producer
            session = csf.createSession("MqTransportTestUser_Writer", "222", false, true, true, true, csf
                    .getServerLocator().getAckBatchSize());

            ClientProducer producer = session.createProducer(queueAddress);

            // Create and send a message
            ClientMessage message = session.createMessage(false);

            String msg = "Hello sent at " + new Date();
            message.getBodyBuffer().writeNullableString(msg);

            producer.send(message);
            _logger.info("Sent TextMessage: " + msg);

            QueueQuery q = session.queueQuery(new SimpleString(queueAddress));
            assertNotNull(q);
            assertEquals(queueAddress, q.getAddress().toString());
            assertEquals(1, q.getMessageCount());

            // ************************************
            // Read FAIL
            // ************************************
            // Should not be able to consume messages because we in the writer role
            try
            {
                ClientConsumer messageConsumer = session.createConsumer(queueName);
                messageConsumer.toString();
                fail("Exception expected for failed read");
            }
            catch (Exception ex)
            {
                // HornetQException[errorCode=105 message=Unable to validate user: MqTransportTestUser_Writer for check
                // type
                // CONSUME for address HornetQIntegrationTest]
                assertEquals(HornetQException.class.getName(), ex.getClass().getName());
                assertEquals(105, ((HornetQException) ex).getCode());
            }

            session.close();

            // ************************************
            // Write FAIL
            // ************************************
            // Create the session, and producer
            session = csf.createSession("MqTransportTestUser_Reader", "333", false, false, false, true, csf
                    .getServerLocator().getAckBatchSize());

            // HornetQ throws error on server but it never makes it back to the client!
            // The only way to check is that there should only be 1 message in the queue after this
            producer = session.createProducer(queueAddress);
            message.putStringProperty("prop", "should error");
            producer.send(message);
            producer.send(message);
            producer.send(message);

            q = session.queueQuery(new SimpleString(queueAddress));
            assertNotNull(q);
            assertEquals(queueAddress, q.getAddress().toString());
            assertEquals(1, q.getMessageCount());

            // ************************************
            // Read OK
            // ************************************
            // Should be able to consume messages because we in the reader role
            ClientConsumer messageConsumer = session.createConsumer(queueName);
            session.start();

            ClientMessage messageReceived = messageConsumer.receive(1000);
            String msg2 = messageReceived.getBodyBuffer().readNullableString();
            _logger.info("Received TextMessage: " + msg2);
            assertEquals(msg, msg2);

            // Should have no more messages
            messageReceived = messageConsumer.receive(1000);
            assertNull(messageReceived);
        }
        finally
        {
            if (session != null)
            {
                session.close();
            }
        }
    }

    @Test
    public void testOK_InVM() throws Exception
    {
        testOK(_inVmClientSessionFactory, "invm");
    }

    @Test
    public void testOK_Core() throws Exception
    {
        testOK(_coreClientSessionFactory, "core");
    }

    @Test
    public void testOK_Stomp() throws Exception
    {

        // ************************************
        // Create queue OK
        // ************************************
        // Create a core queue
        String queueAddress = "MqTransportTestStomp";
        String queueName = "MqTransportTestStomp";
        ClientSession coreSession = null;
        try
        {
            coreSession = _inVmClientSessionFactory.createSession("MqTransportTestUser_System", "111", false, false,
                    false, false, _inVmClientSessionFactory.getServerLocator().getAckBatchSize());

            coreSession.createQueue(queueAddress, queueName, true);

            // Make sure the queue is there
            QueueQuery q = coreSession.queueQuery(new SimpleString(queueAddress));
            assertNotNull(q);
            assertEquals(queueAddress, q.getAddress().toString());
        }
        finally
        {
            if (coreSession != null)
            {
                coreSession.close();
            }
        }

        // Read/Write to queue
        ClientSession session = null;
        try
        {
            // ************************************
            // Write OK
            // ************************************
            // Create the session, and producer
            TestListener errorListener = new TestListener();
            Client c = new Client("localhost", 61613, "MqTransportTestUser_Writer", "222");
            c.addErrorListener(errorListener);

            final String propName = "myprop";
            String msg = "Hello sent at " + new Date();
            HashMap<String, String> m = new HashMap<String, String>();
            m.put(propName, msg);

            c.sendW(queueAddress, msg, m);
            _logger.info("Sent TextMessage: " + msg);

            // Create the session to check
            session = _inVmClientSessionFactory.createSession("MqTransportTestUser_Writer", "222", false, true, true,
                    true, _inVmClientSessionFactory.getServerLocator().getAckBatchSize());

            QueueQuery q = session.queueQuery(new SimpleString(queueAddress));
            assertNotNull(q);
            assertEquals(queueAddress, q.getAddress().toString());
            assertEquals(1, q.getMessageCount());

            // ************************************
            // Read FAIL
            // ************************************
            // Should not be able to consume messages because we in the writer role
            c.subscribe(queueAddress);
            Thread.sleep(1000);
            assertTrue(errorListener.getLastMessageBody().contains(
                    "MqTransportTestUser_Writer doesn't have permission='CONSUME' on address MqTransportTestStomp"));

            session.close();
            c.disconnect();

            // ************************************
            // Write FAIL
            // ************************************
            // Create the session, and producer
            session = _inVmClientSessionFactory.createSession("MqTransportTestUser_Reader", "333", false, false, false,
                    true, _inVmClientSessionFactory.getServerLocator().getAckBatchSize());

            c = new Client("localhost", 61613, "MqTransportTestUser_Reader", "333");
            c.addErrorListener(errorListener);

            c.send(queueAddress, "should error");
            Thread.sleep(1000);
            assertTrue(errorListener.getLastMessageBody().contains(
                    "MqTransportTestUser_Reader doesn't have permission='SEND' on address MqTransportTestStomp"));

            q = session.queueQuery(new SimpleString(queueAddress));
            assertNotNull(q);
            assertEquals(queueAddress, q.getAddress().toString());
            assertEquals(1, q.getMessageCount());

            // ************************************
            // Read OK
            // ************************************
            // Should not be able to consume messages because we in the writer role
            TestListener msgListener = new TestListener();
            c.subscribe(queueAddress, msgListener);
            Thread.sleep(1000);
            assertEquals(msg, msgListener.getLastMessageBody());
        }
        finally
        {
            if (session != null)
            {
                session.close();
            }
        }
    }

    @Test
    public void testOK_StompToCore() throws Exception
    {

        // ************************************
        // Create queue OK
        // ************************************
        // Create a core queue
        String queueAddress = "MqTransportTestStompToCore";
        String queueName = "MqTransportTestStompToCore";
        ClientSession coreSession = null;
        try
        {
            coreSession = _inVmClientSessionFactory.createSession("MqTransportTestUser_System", "111", false, false,
                    false, false, _inVmClientSessionFactory.getServerLocator().getAckBatchSize());

            coreSession.createQueue(queueAddress, queueName, true);

            // Make sure the queue is there
            QueueQuery q = coreSession.queueQuery(new SimpleString(queueAddress));
            assertNotNull(q);
            assertEquals(queueAddress, q.getAddress().toString());
        }
        finally
        {
            if (coreSession != null)
            {
                coreSession.close();
            }
        }

        // Read/Write to queue
        ClientSession session = null;
        try
        {
            // ************************************
            // Write OK
            // ************************************
            // Create the session, and producer
            TestListener errorListener = new TestListener();
            Client c = new Client("localhost", 61613, "MqTransportTestUser_Writer", "222");
            c.addErrorListener(errorListener);

            final String propName = "myprop";
            String msg = "Hello sent at " + new Date();
            HashMap<String, String> m = new HashMap<String, String>();
            m.put(propName, msg);

            c.sendW(queueAddress, msg, m);
            _logger.info("Sent TextMessage: " + msg);

            // Create the session to check
            session = _inVmClientSessionFactory.createSession("MqTransportTestUser_Writer", "222", false, true, true,
                    true, _inVmClientSessionFactory.getServerLocator().getAckBatchSize());

            QueueQuery q = session.queueQuery(new SimpleString(queueAddress));
            assertNotNull(q);
            assertEquals(queueAddress, q.getAddress().toString());
            assertEquals(1, q.getMessageCount());

            // ************************************
            // Write FAIL
            // ************************************
            // Create the session, and producer
            session = _inVmClientSessionFactory.createSession("MqTransportTestUser_Reader", "333", false, false, false,
                    true, _inVmClientSessionFactory.getServerLocator().getAckBatchSize());

            // ************************************
            // Read OK
            // ************************************
            // Should be able to consume messages because we in the reader role
            ClientConsumer messageConsumer = session.createConsumer(queueName);
            session.start();

            ClientMessage messageReceived = messageConsumer.receive(1000);

            // See Hornetq/trunk/src/main/org/hornetq/core/protocol/stomp/StompSession.java sendMessage()
            // This is used to send a stomp packet back to a stomp client
            HornetQBuffer buffer = messageReceived.getBodyBuffer();            
            buffer.readerIndex(MessageImpl.BUFFER_HEADER_SPACE + DataConstants.SIZE_INT);
            SimpleString text = buffer.readNullableSimpleString();
            String msg2 = text.toString();
            
            _logger.info("Received TextMessage: " + msg2);
            assertEquals(msg, msg2);

            msg2 = messageReceived.getStringProperty(propName);
            assertEquals(msg, msg2);
            
            // Should have no more messages
            messageReceived = messageConsumer.receive(1000);
            assertNull(messageReceived);
        }
        finally
        {
            if (session != null)
            {
                session.close();
            }
        }
    }

}
