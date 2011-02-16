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

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.reflect.ConstructorUtils;
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
import org.hornetq.core.server.impl.HornetQServerImpl;
import org.hornetq.integration.logging.Log4jLogDelegateFactory;
import org.hornetq.spi.core.security.JAASSecurityManager;

import com.chililog.server.common.AppProperties;
import com.chililog.server.common.Log4JLogger;

/**
 * <p>
 * Manages our HornetQ server and connections to it
 * </p>
 * 
 * @author vibul
 * 
 */
public class MqManager
{
    static Log4JLogger _logger = Log4JLogger.getLogger(MqManager.class);
    private HornetQServer _hornetqServer;

    private ServerLocator _sl;
    private ClientSessionFactory _csf;
    private String _systemUsername;
    private String _systemPassword;
    private String _systemRoleName;

    /**
     * Returns the singleton instance for this class
     */
    public static MqManager getInstance()
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
        public static final MqManager INSTANCE = new MqManager();
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
    private MqManager()
    {
        try
        {
            _systemUsername = AppProperties.getInstance().getJaasSystemUsername();
            _systemPassword = AppProperties.getInstance().getJaasSystemPassword();
            _systemRoleName = AppProperties.getInstance().getJaasSystemRole();
            return;
        }
        catch (Exception e)
        {
            _logger.error("Error loading MQ Connection Manager: " + e.getMessage(), e);
            System.exit(1);

        }
    }

    /**
     * Starts our HornetQ message queue
     * 
     * @throws Exception
     */
    @SuppressWarnings("rawtypes")
    public void start() throws Exception
    {
        if (_hornetqServer != null)
        {
            _logger.info("HornetQ Message Queue Already Started.");
            return;
        }

        _logger.info("Starting HornetQ Message Queue ...");
        AppProperties appProperties = AppProperties.getInstance();

        // Use log4j
        org.hornetq.core.logging.Logger.setDelegateFactory(new Log4jLogDelegateFactory());

        // Configure our server
        Configuration config = new ConfigurationImpl();
        config.setPersistenceEnabled(appProperties.getMqPersistenceEnabled());
        config.setSecurityEnabled(true);
        config.setLogDelegateFactoryClassName(Log4jLogDelegateFactory.class.getName());

        // Clustering - if we don't set username/password, we get annoying warning message in log
        config.setClustered(appProperties.getMqClusteredEnabled());
        config.setClusterUser(appProperties.getJaasSystemUsername());
        config.setClusterPassword(appProperties.getJaasSystemPassword());

        config.setManagementAddress(new SimpleString("jms.queue.hornetq.management"));

        // Transports
        config.setAcceptorConfigurations(createHornetTransports());

        // Setup JAAS security manager.
        JAASSecurityManager securityManager = new JAASSecurityManager();
        securityManager.setConfigurationName(AppProperties.getInstance().getJaasConfigurationName());

        Class configClass = ClassUtils.getClass(AppProperties.getInstance().getJaasConfigurationClassName());
        Object configObject = ConstructorUtils.invokeConstructor(configClass, null);
        securityManager.setConfiguration((javax.security.auth.login.Configuration) configObject);

        Class callbackHandlerClass = ClassUtils.getClass(AppProperties.getInstance().getJaasCallbackHandlerClassName());
        Object callbackHandlerObject = ConstructorUtils.invokeConstructor(callbackHandlerClass, null);
        securityManager.setCallbackHandler((CallbackHandler) callbackHandlerObject);

        // Start server. See org.hornetq.core.server.HornetQServers
        _hornetqServer = new HornetQServerImpl(config, ManagementFactory.getPlatformMBeanServer(), securityManager);
        _hornetqServer.start();

        // Get client session factory ready
        _sl = HornetQClient.createServerLocatorWithoutHA(new TransportConfiguration(InVMConnectorFactory.class
                .getName()));
        _csf = _sl.createSessionFactory();

        // Add security
        _hornetqServer.getHornetQServerControl().addSecuritySettings("jms.queue.hornetq.management", _systemRoleName,
                _systemRoleName, _systemRoleName, _systemRoleName, _systemRoleName, _systemRoleName, _systemRoleName);

        _logger.info("HornetQ Message Queue Started.");
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
        if (appProperties.getMqCoreProtocolEnabled())
        {
            _logger.info("Configuring Core Protocol");
            transport = new TransportConfiguration(NettyAcceptorFactory.class.getName(),
                    appProperties.getMqCoreProtocolConfig());
            transports.add(transport);
        }

        // Stomp
        if (appProperties.getMqStompProtocolEnabled())
        {
            _logger.info("Configuring Stomp Protocol");
            transport = new TransportConfiguration(NettyAcceptorFactory.class.getName(),
                    appProperties.getMqStompProtocolConfig());
            transports.add(transport);
        }

        // Stomp Web Socket
        if (appProperties.getMqStompWebSocketProtocolEnabled())
        {
            _logger.info("Configuring Stomp Web Socket Protocol");
            transport = new TransportConfiguration(NettyAcceptorFactory.class.getName(),
                    appProperties.getMqStompWebSocketProtocolConfig());
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
     * Add security settings to a queue
     * 
     * @param queueAddress
     *            The queue's address to which the security setting is to apply. A pattern can be provided where *
     *            represents a work and # represents more than 1 work.
     * @param writerRoles
     *            Comma separated list of roles that can write to the queue
     * @param readerRoles
     *            Comma separated list of roles that can read from the queue
     * @throws Exception
     */
    public void addSecuritySettings(String queueAddress, String writerRoles, String readerRoles) throws Exception
    {
        HornetQServerControl hqControl = _hornetqServer.getHornetQServerControl();
        hqControl.removeSecuritySettings(queueAddress);
        hqControl.addSecuritySettings(queueAddress, writerRoles, readerRoles, _systemRoleName, _systemRoleName,
                _systemRoleName, _systemRoleName, _systemRoleName);
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
     *            flag to indicate if this queue is to be persisted
     * @param deadLetterQueueAddress
     *            Address to the dead letter queue. Set to null if dead letter queue not supported.
     * @throws Exception
     *             if error
     */
    public void deployQueue(String queueAddress, String queueName, boolean isDurable, String deadLetterQueueAddress)
            throws Exception
    {
        HornetQServerControl hqControl = _hornetqServer.getHornetQServerControl();
        boolean doCreate = false;

        // Let check if we really have to create a new queue
        QueueControl qc = getQueueControl(queueAddress, queueName);
        if (qc == null)
        {
            doCreate = true;
        }
        else if (qc.isDurable() == isDurable || (deadLetterQueueAddress == null && qc.getDeadLetterAddress() == null))
        {
            // Do nothing
        }
        else if (qc.isDurable() != isDurable)
        {
            // Queue exist but properties are different so we have to delete/create
            hqControl.destroyQueue(queueName);
        }
        else if ((qc.getDeadLetterAddress() == null && deadLetterQueueAddress != null)
                || (qc.getDeadLetterAddress() != null && deadLetterQueueAddress == null)
                || (!qc.getDeadLetterAddress().equalsIgnoreCase(deadLetterQueueAddress)))
        {
            // We just need to update the DLA. Queue does not have to be delete/create
            qc.setDeadLetterAddress(deadLetterQueueAddress);
            return;
        }

        if (doCreate)
        {
            hqControl.createQueue(queueAddress, queueName, null, isDurable);
            if (!StringUtils.isBlank(deadLetterQueueAddress))
            {
                qc = MqManager.getInstance().getQueueControl(queueAddress, queueName);
                qc.setDeadLetterAddress(deadLetterQueueAddress);
            }
        }
    }

    /**
     * Delete a queue
     * 
     * @param queueName
     *            Name of the queue
     * @throws Exception
     *             if error
     */
    public void deleteQueue(String queueName) throws Exception
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
