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

import java.lang.management.ManagementFactory;
import java.util.Date;
import java.util.HashSet;

import org.apache.log4j.Logger;
import org.hornetq.api.core.SimpleString;
import org.hornetq.api.core.TransportConfiguration;
import org.hornetq.api.core.client.*;
import org.hornetq.api.core.client.ClientSession.QueueQuery;
import org.hornetq.core.config.Configuration;
import org.hornetq.core.config.impl.ConfigurationImpl;
import org.hornetq.core.remoting.impl.invm.InVMAcceptorFactory;
import org.hornetq.core.remoting.impl.invm.InVMConnectorFactory;
import org.hornetq.core.remoting.impl.netty.NettyAcceptorFactory;
import org.hornetq.core.server.HornetQServer;
import org.hornetq.core.server.impl.HornetQServerImpl;
import org.hornetq.integration.logging.Log4jLogDelegateFactory;
import org.junit.Test;

/**
 * Test case to see if we can simply embed HornetQ
 * 
 * @author vibul
 * 
 */
public class HornetQEmbeddedTest
{
    private static Logger _logger = Logger.getLogger(HornetQEmbeddedTest.class);

    /**
     * Copied from HornetQ Core EmbeddedExample
     * 
     * @throws Exception
     */
    @Test
    public void testPubSub() throws Exception
    {
        HornetQServer hornetqServer;

        // *******************************
        // Start server
        // *******************************
        // Use log4j
        org.hornetq.core.logging.Logger.setDelegateFactory(new Log4jLogDelegateFactory());

        // Configure our server
        Configuration config = new ConfigurationImpl();
        config.setPersistenceEnabled(false);
        config.setSecurityEnabled(false);

        HashSet<TransportConfiguration> transports = new HashSet<TransportConfiguration>();
        transports.add(new TransportConfiguration(NettyAcceptorFactory.class.getName()));
        transports.add(new TransportConfiguration(InVMAcceptorFactory.class.getName()));
        config.setAcceptorConfigurations(transports);

        hornetqServer = new HornetQServerImpl(config, ManagementFactory.getPlatformMBeanServer());
        hornetqServer.start();

        ClientSession coreSession = null;
        ClientSession session = null;
        try
        {
            // *******************************
            // Create Q
            // *******************************
            // As we are not using a JNDI environment we instantiate the objects directly
            ServerLocator sl = HornetQClient.createServerLocatorWithoutHA(new TransportConfiguration(
                    InVMConnectorFactory.class.getName()));
            ClientSessionFactory sf = sl.createSessionFactory();

            // Create a core queue
            coreSession = sf.createSession(false, true, true);

            final String address = "topic";
            final String queue1Name = "topic.queue1";
            final String queue2Name = "topic.queue2";

            coreSession.createQueue(address, queue1Name, true);
            coreSession.createQueue(address, queue2Name, true);

            QueueQuery q = coreSession.queueQuery(new SimpleString(queue1Name));
            assertNotNull(q);
            assertEquals(address, q.getAddress().toString());

            q = coreSession.queueQuery(new SimpleString(queue2Name));
            assertNotNull(q);
            assertEquals(address, q.getAddress().toString());

            coreSession.close();

            // *******************************
            // Publish
            // *******************************

            // Step 5. Create the session, and producer
            session = sf.createSession();

            ClientProducer producer = session.createProducer(address);

            // Step 6. Create and send a message
            ClientMessage message = session.createMessage(false);

            final String propName = "myprop";

            message.putStringProperty(propName, "Hello sent at " + new Date());

            _logger.info("Sending the message.");

            producer.send(message);

            // *******************************
            // Subscriber #1
            // *******************************
            // Step 7. Create the message consumer and start the connection
            ClientConsumer messageConsumer = session.createConsumer(queue1Name);
            session.start();

            // Step 8. Receive the message.
            ClientMessage messageReceived = messageConsumer.receive(1000);
            _logger.info("Received TextMessage:" + messageReceived.getStringProperty(propName));
            assertTrue(messageReceived.getStringProperty(propName).startsWith("Hello"));

            // Make sure that there are no more messages
            messageReceived = messageConsumer.receive(1000);
            assertNull(messageReceived);
            
            // *******************************
            // Subscriber #2
            // *******************************
            // Step 7. Create the message consumer and start the connection
            messageConsumer = session.createConsumer(queue2Name);
            session.start();

            // Step 8. Receive the message.
            messageReceived = messageConsumer.receive(1000);
            _logger.info("Received TextMessage:" + messageReceived.getStringProperty(propName));
            assertTrue(messageReceived.getStringProperty(propName).startsWith("Hello"));

            // Make sure that there are no more messages
            messageReceived = messageConsumer.receive(1000);
            assertNull(messageReceived);
            
        }
        finally
        {
            // Step 9. Be sure to close our resources!
            if (coreSession != null)
            {
                coreSession.close();
            }
            if (session != null)
            {
                session.close();
            }
            hornetqServer.stop();
        }

        return;
    }

}
