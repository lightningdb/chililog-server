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

import java.lang.management.ManagementFactory;
import java.util.HashSet;

import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;
import javax.security.auth.callback.CallbackHandler;

import org.hornetq.api.core.SimpleString;
import org.hornetq.api.core.TransportConfiguration;
import org.hornetq.api.core.client.ClientSession;
import org.hornetq.api.core.client.ClientSessionFactory;
import org.hornetq.api.core.client.HornetQClient;
import org.hornetq.api.core.client.ServerLocator;
import org.hornetq.api.core.management.HornetQServerControl;
import org.hornetq.api.core.management.ObjectNameBuilder;
import org.hornetq.api.core.management.QueueControl;
import org.hornetq.core.config.Configuration;
import org.hornetq.core.config.impl.ConfigurationImpl;
import org.hornetq.core.remoting.impl.invm.InVMAcceptorFactory;
import org.hornetq.core.remoting.impl.invm.InVMConnectorFactory;
import org.hornetq.core.remoting.impl.netty.NettyAcceptorFactory;
import org.hornetq.core.server.HornetQServer;
import org.hornetq.core.server.JournalType;
import org.hornetq.core.server.impl.HornetQServerImpl;
import org.hornetq.integration.logging.Log4jLogDelegateFactory;
import org.hornetq.spi.core.security.JAASSecurityManager;

import com.chililog.server.common.AppProperties;
import com.chililog.server.common.Log4JLogger;
import com.chililog.server.data.UserBO;

/**
 * <p>
 * The Message Queue Manager hides the complexities of the embedded HornetQ server. It provides a simple API to manage
 * the HornetQ server including start, stop, querying queues and establishing connections to the server for message
 * processing.
 * </p>
 * 
 * <pre>
 * // Start HornetQ
 * MqManager.getInstance().start();
 * 
 * // Stop HornetQ
 * MqManager.getInstance().stop();
 * </pre>
 * 
 * @author vibul
 * 
 */
public class MqService
{
    static Log4JLogger _logger = Log4JLogger.getLogger(MqService.class);
    private HornetQServer _hornetqServer;

    private ServerLocator _sl;
    private ClientSessionFactory _csf;
    private String _systemUsername;
    private String _systemPassword;

    /**
     * Returns the singleton instance for this class
     */
    public static MqService getInstance()
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
        public static final MqService INSTANCE = new MqService();
    }

    /**
     * <p>
     * Singleton constructor
     * </p>
     * <p>
     * If there is an exception, we log the error and exit because there's no point continuing without MQ client session
     * </p>
     * 
     * @throws Exception
     */
    private MqService()
    {
        try
        {
            _systemUsername = AppProperties.getInstance().getMqSystemUsername();
            _systemPassword = AppProperties.getInstance().getMqSystemPassword();
            return;
        }
        catch (Exception e)
        {
            _logger.error("Error loading MQ Connection Manager: " + e.getMessage(), e);
            System.exit(1);

        }
    }

    /**
     * Returns the trusted system user name
     */
    public String getSystemUsername()
    {
        return _systemUsername;
    }

    /**
     * Returns the trusted system user's password
     */
    public String getSystemPassword()
    {
        return _systemPassword;
    }

    /**
     * Starts our HornetQ message queue
     * 
     * @throws Exception
     */
    public void start() throws Exception
    {
        if (_hornetqServer != null)
        {
            _logger.info("Message Queue Already Started.");
            return;
        }

        _logger.info("Starting Message Queue ...");
        AppProperties appProperties = AppProperties.getInstance();

        // Use log4j
        org.hornetq.core.logging.Logger.setDelegateFactory(new Log4jLogDelegateFactory());

        // Configure our server
        Configuration config = new ConfigurationImpl();

        // Journal - see http://docs.jboss.org/hornetq/2.2.2.Final/user-manual/en/html_single/index.html#persistence
        // TODO allow configuration to async for it to be faster
        config.setPersistenceEnabled(appProperties.getMqJournallingEnabled());
        config.setJournalType(JournalType.NIO);
        config.setJournalDirectory(appProperties.getMqJournalDirectory());
        config.setPagingDirectory(appProperties.getMqPagingDirectory());
        config.setSecurityEnabled(true);
        config.setSecurityInvalidationInterval(appProperties.getMqSecurityInvalidationInterval());
        
        // Logging
        config.setLogDelegateFactoryClassName(Log4jLogDelegateFactory.class.getName());

        // Clustering - if we don't set username/password, we get annoying warning message in log
        config.setClustered(appProperties.getMqClusteredEnabled());
        config.setClusterUser(appProperties.getMqSystemUsername());
        config.setClusterPassword(appProperties.getMqSystemPassword());

        // Management address to send management messages to
        config.setManagementAddress(new SimpleString("jms.queue.hornetq.management"));

        // Transports
        config.setAcceptorConfigurations(createHornetTransports());

        // Setup JAAS security manager.
        JAASSecurityManager securityManager = new JAASSecurityManager();
        securityManager.setConfigurationName("not_used");

        javax.security.auth.login.Configuration configObject = new JAASConfiguration();
        securityManager.setConfiguration(configObject);

        CallbackHandler callbackHandlerObject = new JAASCallbackHandler();
        securityManager.setCallbackHandler(callbackHandlerObject);

        // Start server. See org.hornetq.core.server.HornetQServers
        _hornetqServer = new HornetQServerImpl(config, ManagementFactory.getPlatformMBeanServer(), securityManager);
        _hornetqServer.start();

        // Get client session factory ready
        _sl = HornetQClient.createServerLocatorWithoutHA(new TransportConfiguration(InVMConnectorFactory.class
                .getName()));
        _csf = _sl.createSessionFactory();

        // Add security to JMS management API to make it hard to hack
        String adminRoleName = UserBO.SYSTEM_ADMINISTRATOR_ROLE_NAME;
        _hornetqServer.getHornetQServerControl().addSecuritySettings("jms.queue.hornetq.management", adminRoleName,
                adminRoleName, adminRoleName, adminRoleName, adminRoleName, adminRoleName, adminRoleName);

        _logger.info("Message Queue Started.");
        return;
    }

    /**
     * Create transports so we can communicate with HornetQ
     * 
     * @return HornetQ transport configuration
     */
    private HashSet<TransportConfiguration> createHornetTransports()
    {
        HashSet<TransportConfiguration> transports = new HashSet<TransportConfiguration>();
        AppProperties appProperties = AppProperties.getInstance();
        TransportConfiguration transport;

        // Always add the in Java Virtual Machine so we can talk to it
        transport = new TransportConfiguration(InVMAcceptorFactory.class.getName());
        transports.add(transport);

        // Core or JMS protocol
        if (appProperties.getPubSubCoreProtocolEnabled())
        {
            _logger.info("Configuring Core Protocol");
            transport = new TransportConfiguration(NettyAcceptorFactory.class.getName(),
                    appProperties.getPubSubCoreProtocolConfig());
            transports.add(transport);
        }

        // Stomp
        if (appProperties.getPubSubStompProtocolEnabled())
        {
            _logger.info("Configuring Stomp Protocol");
            transport = new TransportConfiguration(NettyAcceptorFactory.class.getName(),
                    appProperties.getPubSubStompProtocolConfig());
            transports.add(transport);
        }

        // Stomp Web Socket
        if (appProperties.getPubSubStompWebSocketProtocolEnabled())
        {
            _logger.info("Configuring Stomp Web Socket Protocol");
            transport = new TransportConfiguration(NettyAcceptorFactory.class.getName(),
                    appProperties.getPubSubStompWebSocketProtocolConfig());
            transports.add(transport);
        }

        return transports;
    }

    /**
     * Returns our instance of the HornetQ server
     */
    public HornetQServer getNativeServer()
    {
        return _hornetqServer;
    }

    /**
     * Stops the HornetQ server
     * 
     * @throws Exception
     */
    public void stop() throws Exception
    {
        if (_hornetqServer == null)
        {
            _logger.info("HornetQ Message Queue Already Stopped.");
            return;
        }

        _logger.info("Stopping HornetQ Message Queue ...");
        _csf.close();
        _hornetqServer.stop();
        _hornetqServer = null;
        _logger.info("HornetQ Message Queue Stopped.");
    }

    /**
     * <p>
     * Creates a HornetQ transactional client session for the system user. It is the caller's responsibility to close
     * the session
     * </p>
     * 
     * @return MqSession connection to HornetQ
     * @throws Exception
     */
    public ClientSession getTransactionalSystemClientSession() throws Exception
    {
        return _csf.createSession(_systemUsername, _systemPassword, false, false, false, false, _sl.getAckBatchSize());
    }

    /**
     * <p>
     * Creates a HornetQ non transactional client session for the system user. It is the caller's responsibility to
     * close the session
     * </p>
     * 
     * @return MqSession connection to HornetQ
     * @throws Exception
     */
    public ClientSession getNonTransactionalSystemClientSession() throws Exception
    {
        return _csf.createSession(_systemUsername, _systemPassword, false, true, true, false, _sl.getAckBatchSize());
    }

    /**
     * <p>
     * Creates a HornetQ non transactional session for the specified user.
     * </p>
     * <p>
     * It is the caller's responsibility to:
     * <ul>
     * <li>call <code>ClientSession.commit</code> to commit transactions</li>
     * <li>close the session</li>
     * </ul>
     * </p>
     * 
     * @param username
     *            User credentials for authentication
     * @param password
     *            User credentials for authentication
     * @return MqSession connection to HornetQ
     * @throws Exception
     */
    public ClientSession getTransactionalClientSession(String username, String password) throws Exception
    {
        return _csf.createSession(username, password, false, false, false, false, _sl.getAckBatchSize());
    }

    /**
     * <p>
     * Creates a HornetQ non-transactional session for the specified user.
     * </p>
     * <p>
     * It is the caller's responsibility to close the session.
     * </p>
     * <p>
     * However, the class does NOT have to call <code>ClientSession.commit</code> on send or receive because auto commit
     * on send and auto acknowledge on receive are turned on. Acknowledgments are sent in batches to optimize
     * performance.
     * </p>
     * 
     * @param username
     *            User credentials for authentication
     * @param password
     *            User credentials for authentication
     * @return ClientSession session connection to HornetQ
     * @throws Exception
     */
    public ClientSession getNonTransactionalClientSession(String username, String password) throws Exception
    {
        return _csf.createSession(username, password, false, true, true, false, _sl.getAckBatchSize());
    }

    /**
     * Close a client session connection logging but ignoring all exceptions
     * 
     * @param clientSession
     *            session to close
     */
    public void closeClientSession(ClientSession clientSession)
    {
        try
        {
            if (clientSession != null)
            {
                clientSession.close();
            }
        }
        catch (Exception ex)
        {
            // Log and ignore errors on closing a connection
            _logger.error(ex, Strings.CLOSE_MQ_SESSION_ERROR, ex.getMessage());
        }
    }

    /**
     * Adds or updates the properties of an address
     * 
     * @param address
     *            matching address
     * @param DLA
     *            address to send dead letters (undelivered messages). Maybe null.
     * @param expiryAddress
     *            defines where to send a message that has expired
     * @param lastValueQueue
     *            Any queues created for this address a last value queue; i.e. queues which discard any messages when a
     *            newer message with the same value for a well-defined last-value property is put in the queue
     * @param deliveryAttempts
     *            defines how many time a cancelled message can be redelivered before sending to the dead-letter-address
     * @param maxSizeBytes
     *            What's the max memory the address could have before entering on page mode
     * @param pageSizeBytes
     *            The size of each page file used on the paging system
     * @param pageMaxCacheSize
     *            The system will keep up to <page-max-cache-size page files in memory to optimize IO during paging
     *            navigation
     * @param redeliveryDelay
     *            defines how long to wait before attempting redelivery of a cancelled message
     * @param redistributionDelay
     *            defines how long to wait when the last consumer is closed on a queue before redistributing any
     *            messages
     * @param sendToDLAOnNoRoute
     *            If a message is sent to an address, but the server does not route it to any queues, for example, there
     *            might be no queues bound to that address, or none of the queues have filters that match, then normally
     *            that message would be discarded. However if this parameter is set to true for that address, if the
     *            message is not routed to any queues it will instead be sent to the dead letter address (DLA) for that
     *            address, if it exists.
     * @param addressFullMessagePolicy
     *            This attribute can have one of the following values: PAGE, DROP or BLOCK and determines what happens
     *            when an address where max-size-bytes is specified becomes full. The default value is PAGE. If the
     *            value is PAGE then further messages will be paged to disk. If the value is DROP then further messages
     *            will be silently dropped. If the value is BLOCK then client message producers will block when they try
     *            and send further messages.
     * @throws Exception
     */
    public void addAddressSettings(final String address,
                                   final String DLA,
                                   final String expiryAddress,
                                   final boolean lastValueQueue,
                                   final int deliveryAttempts,
                                   final long maxSizeBytes,
                                   final int pageSizeBytes,
                                   final int pageMaxCacheSize,
                                   final long redeliveryDelay,
                                   final long redistributionDelay,
                                   final boolean sendToDLAOnNoRoute,
                                   final String addressFullMessagePolicy) throws Exception
    {
        HornetQServerControl hqControl = _hornetqServer.getHornetQServerControl();
        hqControl.addAddressSettings(address, DLA, expiryAddress, lastValueQueue, deliveryAttempts, maxSizeBytes,
                pageSizeBytes, pageMaxCacheSize, redeliveryDelay, redistributionDelay, sendToDLAOnNoRoute,
                addressFullMessagePolicy);
        return;
    }

    /**
     * Add security settings to a queue
     * 
     * @param address
     *            The HornetQ address to which the security setting is to apply. A pattern can be provided where *
     *            represents a word and # represents and letters.
     * @param publisherRoles
     *            Comma separated list of roles that can publish to the address; i.e. submit log entries for processing
     * @param subscriberRoles
     *            Comma separated list of roles that can subscribe to the address; i.e. read log entries
     * @throws Exception
     */
    public void addSecuritySettings(String address, String publisherRoles, String subscriberRoles) throws Exception
    {
        String adminRoleName = UserBO.SYSTEM_ADMINISTRATOR_ROLE_NAME;
        
        HornetQServerControl hqControl = _hornetqServer.getHornetQServerControl();
        hqControl.removeSecuritySettings(address);

        String createQueueRoles = adminRoleName + "," + subscriberRoles;
        hqControl.addSecuritySettings(address, publisherRoles, subscriberRoles, createQueueRoles, adminRoleName,
                createQueueRoles, adminRoleName, adminRoleName);
    }

    /**
     * Removes security settings to a queue
     * 
     * @param queueAddress
     *            The queue's address to which the security setting is to apply. A pattern can be provided where *
     *            represents a work and # represents more than 1 work.
     * @throws Exception
     */
    public void removeSecuritySettings(String queueAddress) throws Exception
    {
        HornetQServerControl hqControl = _hornetqServer.getHornetQServerControl();
        hqControl.removeSecuritySettings(queueAddress);
        return;
    }

    /**
     * Creates a queue if it does not exist. If it exists, then the queue properties are updated.
     * 
     * @param queueAddress
     *            Address of the queue
     * @param queueName
     *            Name of the queue
     * @param isDurable
     *            Flag to indicate if this queue is to be persisted. Set to false for temporary queues.
     * @throws Exception
     *             if error
     */
    public void deployQueue(String queueAddress, String queueName, boolean isDurable) throws Exception
    {
        HornetQServerControl hqControl = _hornetqServer.getHornetQServerControl();
        boolean doCreate = false;

        // Let check if we really have to create a new queue
        QueueControl qc = getQueueControl(queueAddress, queueName);
        if (qc == null)
        {
            doCreate = true;
        }
        else if (qc.isDurable() != isDurable)
        {
            // Queue exist but properties are different so we have to delete/create
            hqControl.destroyQueue(queueName);
            doCreate = true;
        }

        if (doCreate)
        {
            hqControl.createQueue(queueAddress, queueName, null, isDurable);
        }
    }

    /**
     * Removes the named queue.
     * 
     * @param queueName
     *            Name of the queue
     * @throws Exception
     *             if error
     */
    public void destroyQueue(String queueName) throws Exception
    {
        HornetQServerControl hqControl = _hornetqServer.getHornetQServerControl();
        hqControl.destroyQueue(queueName);
    }

    /**
     * Gets the queue control management class
     * 
     * @param queueAddress
     *            Address of queue
     * @param queueName
     *            Name of queue
     * @return <code>QueueControl</code> that can be used for managing queues. Null if returned if queue not found
     * @throws Exception
     */
    public QueueControl getQueueControl(String queueAddress, String queueName) throws Exception
    {
        ObjectName objectName = ObjectNameBuilder.DEFAULT.getQueueObjectName(new SimpleString(queueAddress),
                new SimpleString(queueName));

        if (!_hornetqServer.getMBeanServer().isRegistered(objectName))
        {
            return null;
        }

        return (QueueControl) MBeanServerInvocationHandler.newProxyInstance(_hornetqServer.getMBeanServer(),
                objectName, QueueControl.class, false);

    }

    /**
     * Returns if our HornetQ message service is running or not
     */
    public boolean isRunning()
    {
        return _hornetqServer != null;
    }
}
