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

package org.chililog.server.pubsub.jsonhttp;

import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.chililog.server.common.ChiliLogException;
import org.chililog.server.common.JsonTranslator;
import org.chililog.server.common.Log4JLogger;
import org.chililog.server.data.MongoConnection;
import org.chililog.server.data.RepositoryInfoBO;
import org.chililog.server.data.RepositoryInfoController;
import org.chililog.server.data.UserBO;
import org.chililog.server.data.UserController;
import org.chililog.server.engine.MqService;
import org.chililog.server.engine.RepositoryStorageWorker;
import org.chililog.server.pubsub.Strings;
import org.hornetq.api.core.client.ClientConsumer;
import org.hornetq.api.core.client.ClientMessage;
import org.hornetq.api.core.client.ClientSession;
import org.hornetq.api.core.client.MessageHandler;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.handler.codec.http.websocket.DefaultWebSocketFrame;

import com.mongodb.DB;

/**
 * Worker to process subscription requests
 */
public class SubscriptionWorker
{
    private static Log4JLogger _logger = Log4JLogger.getLogger(SubscriptionWorker.class);

    private Channel _channel = null;
    private ClientSession _session = null;
    private ClientConsumer _consumer = null;

    /**
     * Constructor
     * 
     * @param channel
     *            Netty channel to write to
     */
    public SubscriptionWorker(Channel channel)
    {
        _channel = channel;
    }

    /**
     * Process a publishing request
     * 
     * @param request
     *            Publishing request in JSON format
     * @param response
     *            Publishing response in JSON format
     * @return true if successful; false if error
     */
    public boolean process(String request, StringBuilder response)
    {
        String messageID = null;
        try
        {
            if (StringUtils.isBlank(request))
            {
                throw new IllegalArgumentException("Request content is blank.");
            }

            SimpleDateFormat sf = new SimpleDateFormat(RepositoryStorageWorker.TIMESTAMP_FORMAT);
            sf.setTimeZone(TimeZone.getTimeZone(RepositoryStorageWorker.TIMESTAMP_TIMEZONE));

            // Parse JSON
            SubscriptionRequestAO requestAO = JsonTranslator.getInstance().fromJson(request,
                    SubscriptionRequestAO.class);
            messageID = requestAO.getMessageID();

            // Authenticate
            authenticate(requestAO);

            // Subscribe
            String queueAddress = RepositoryInfoBO.buildPubSubAddress(requestAO.getRepositoryName());
            String queueName = queueAddress + ".json-http-" + _channel.getId() + "." + UUID.randomUUID().toString();

            _session = MqService.getInstance().getNonTransactionalClientSession(requestAO.getUsername(),
                    requestAO.getPassword());

            _session.createTemporaryQueue(queueAddress, queueName);

            _consumer = _session.createConsumer(queueName);

            MqMessageHandler handler = new MqMessageHandler(_channel, messageID);
            _consumer.setMessageHandler(handler);

            _session.start();

            // Prepare response
            SubscriptionResponseAO responseAO = new SubscriptionResponseAO(messageID);
            JsonTranslator.getInstance().toJson(responseAO, response);

            // Finish
            return true;
        }
        catch (Exception ex)
        {
            _logger.error(ex, "Error processing message: %s", request);

            SubscriptionResponseAO responseAO = new SubscriptionResponseAO(messageID, ex);
            JsonTranslator.getInstance().toJson(responseAO, response);
            return false;
        }
    }

    /**
     * Stop subscription
     */
    public void stop()
    {
        try
        {
            if (_consumer != null)
            {
                _consumer.close();
            }
            if (_session != null)
            {
                _session.close();
            }
        }
        catch (Exception ex)
        {
            _logger.error(ex, "Error stopping subscription");
        }
    }

    /**
     * Authenticate request
     * 
     * @param subscriptionAO
     * @throws ChiliLogException
     */
    public void authenticate(SubscriptionRequestAO subscriptionAO) throws ChiliLogException
    {
        String repoName = subscriptionAO.getRepositoryName();

        // Check db
        DB db = MongoConnection.getInstance().getConnection();

        // Make user repository exists
        RepositoryInfoController.getInstance().getByName(db, repoName);

        // Make sure user exists and password is valid
        UserBO user = UserController.getInstance().getByUsername(db, subscriptionAO.getUsername());
        user.validatePassword(subscriptionAO.getPassword());

        // Make sure the user can publish to the repository
        String administratorRole = UserBO.createRepositoryAdministratorRoleName(repoName);
        String subscriptionRole = UserBO.createRepositorySubscriberRoleName(repoName);

        if (!user.hasRole(administratorRole) && !user.hasRole(subscriptionRole))
        {
            throw new ChiliLogException(Strings.PUBLISHER_AUTHENTICATION_ERROR, subscriptionAO.getUsername(), repoName);
        }
    }

    /**
     * Class to handle incoming log messages
     */
    public static class MqMessageHandler implements MessageHandler
    {
        private static Log4JLogger _logger = Log4JLogger.getLogger(MqMessageHandler.class);
        private String _messageID = null;
        private Channel _channel = null;

        /**
         * Constructor
         * 
         * @param channel
         *            Netty channel to write messages into
         */
        public MqMessageHandler(Channel channel, String messageID)
        {
            _messageID = messageID;
            _channel = channel;
        }

        /**
         * When a message is received, pass it on to the client
         */
        @Override
        public void onMessage(ClientMessage message)
        {
            try
            {
                if (!_channel.isOpen() || !_channel.isConnected())
                {
                    return;                    
                }
                
                LogEntryAO logEntry = new LogEntryAO();
                logEntry.setTimestamp(message.getStringProperty(RepositoryStorageWorker.TIMESTAMP_PROPERTY_NAME));
                logEntry.setSource(message.getStringProperty(RepositoryStorageWorker.SOURCE_PROPERTY_NAME));
                logEntry.setHost(message.getStringProperty(RepositoryStorageWorker.HOST_PROPERTY_NAME));
                logEntry.setSeverity(message.getStringProperty(RepositoryStorageWorker.SEVERITY_PROPERTY_NAME));
                logEntry.setMessage(message.getBodyBuffer().readNullableSimpleString().toString());
                
                SubscriptionResponseAO responseAO = new SubscriptionResponseAO(_messageID, logEntry);
                String responseJson = JsonTranslator.getInstance().toJson(responseAO);
                _channel.write(new DefaultWebSocketFrame(responseJson));
                
                return;
            }
            catch (Exception ex)
            {
                _logger.error(ex, "Error forwarding subscription message JSON HTTP web socket client");
            }
        }

    }
}
