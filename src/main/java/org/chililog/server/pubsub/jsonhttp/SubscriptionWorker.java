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

import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.chililog.server.common.ChiliLogException;
import org.chililog.server.common.JsonTranslator;
import org.chililog.server.common.Log4JLogger;
import org.chililog.server.data.MongoConnection;
import org.chililog.server.data.RepositoryConfigBO;
import org.chililog.server.data.RepositoryConfigController;
import org.chililog.server.data.UserBO;
import org.chililog.server.data.UserController;
import org.chililog.server.engine.MqService;
import org.chililog.server.engine.RepositoryEntryMqMessage;
import org.chililog.server.pubsub.Strings;
import org.chililog.server.pubsub.websocket.TextWebSocketFrame;
import org.chililog.server.workbench.workers.AuthenticationTokenAO;
import org.hornetq.api.core.client.ClientConsumer;
import org.hornetq.api.core.client.ClientMessage;
import org.hornetq.api.core.client.ClientSession;
import org.hornetq.api.core.client.MessageHandler;
import org.jboss.netty.channel.Channel;

import com.mongodb.DB;

/**
 * Worker to process subscription requests
 */
public class SubscriptionWorker {

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
    public SubscriptionWorker(Channel channel) {
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
    public boolean process(String request, StringBuilder response) {
        String messageID = null;
        try {
            if (StringUtils.isBlank(request)) {
                throw new IllegalArgumentException("Request content is blank.");
            }

            // Parse JSON
            SubscriptionRequestAO requestAO = JsonTranslator.getInstance().fromJson(request,
                    SubscriptionRequestAO.class);
            messageID = requestAO.getMessageID();

            // Authenticate
            authenticate(requestAO);

            // Subscribe using system user because sometimes a token is supplied from the workbench
            String queueAddress = RepositoryConfigBO.buildPubSubAddress(requestAO.getRepositoryName());
            String queueName = queueAddress + ".json-http-" + _channel.getId() + "." + UUID.randomUUID().toString();
            _session = MqService.getInstance().getNonTransactionalSystemClientSession();

            // Filter messages
            StringBuilder filter = new StringBuilder();
            if (!StringUtils.isBlank(requestAO.getHost())) {
                filter.append(String.format("%s = '%s'", RepositoryEntryMqMessage.HOST, requestAO.getHost()));
            }
            if (!StringUtils.isBlank(requestAO.getSource())) {
                if (filter.length() > 0) {
                    filter.append(" AND ");
                }
                filter.append(String.format("%s = '%s'", RepositoryEntryMqMessage.SOURCE, requestAO.getSource()));
            }
            if (!StringUtils.isBlank(requestAO.getSeverity())) {
                if (filter.length() > 0) {
                    filter.append(" AND ");
                }

                filter.append(String.format("%s IN (", RepositoryEntryMqMessage.SEVERITY));
                int sev = Integer.parseInt(requestAO.getSeverity());
                for (int i = 0; i <= sev; i++) {
                    filter.append(String.format("'%s', ", i));
                }
                filter.replace(filter.length() - 1, filter.length(), ")"); // replace last comma with end )
            }

            if (filter.length() == 0) {
                _session.createTemporaryQueue(queueAddress, queueName);
            } else {
                _logger.debug("Subscription filter %s", filter);
                _session.createTemporaryQueue(queueAddress, queueName, filter.toString());
            }

            _consumer = _session.createConsumer(queueName);

            MqMessageHandler handler = new MqMessageHandler(_channel, messageID);
            _consumer.setMessageHandler(handler);

            _session.start();

            // Prepare response
            SubscriptionResponseAO responseAO = new SubscriptionResponseAO(messageID);
            JsonTranslator.getInstance().toJson(responseAO, response);

            // Finish
            return true;
        } catch (Exception ex) {
            _logger.error(ex, "Error processing message: %s", request);

            SubscriptionResponseAO responseAO = new SubscriptionResponseAO(messageID, ex);
            JsonTranslator.getInstance().toJson(responseAO, response);
            return false;
        }
    }

    /**
     * Stop subscription
     */
    public void stop() {
        try {
            if (_consumer != null) {
                _consumer.close();
            }
            if (_session != null) {
                _session.close();
            }
        } catch (Exception ex) {
            _logger.error(ex, "Error stopping subscription");
        }
    }

    /**
     * Authenticate request
     * 
     * @param subscriptionAO
     * @throws ChiliLogException
     */
    public void authenticate(SubscriptionRequestAO subscriptionAO) throws ChiliLogException {
        String repoName = subscriptionAO.getRepositoryName();

        // Check db
        DB db = MongoConnection.getInstance().getConnection();

        // Make user repository exists
        RepositoryConfigController.getInstance().getByName(db, repoName);

        // Check user
        UserBO user = UserController.getInstance().getByUsername(db, subscriptionAO.getUsername());
        boolean passwordOK = false;
        if (subscriptionAO.getPassword().startsWith("token:")) {
            // Password is a token so we need to check the token
            // Must have come from the workbench
            String jsonToken = subscriptionAO.getPassword().substring(6);
            AuthenticationTokenAO token = AuthenticationTokenAO.fromString(jsonToken);
            passwordOK = token.getUserID().equals(user.getDocumentID().toString());
        } else {
            // Make sure user exists and password is valid
            passwordOK = user.validatePassword(subscriptionAO.getPassword());
        }
        if (!passwordOK) {
            throw new ChiliLogException(Strings.SUBSCRIBER_AUTHENTICATION_ERROR);
        }

        // Make sure the user can publish to the repository
        String administratorRole = UserBO.createRepositoryAdministratorRoleName(repoName);
        String workbenchRole = UserBO.createRepositoryWorkbenchRoleName(repoName);
        String subscriptionRole = UserBO.createRepositorySubscriberRoleName(repoName);

        if (!user.hasRole(administratorRole) && !user.hasRole(subscriptionRole) && !user.hasRole(workbenchRole)
                && !user.isSystemAdministrator()) {
            throw new ChiliLogException(Strings.PUBLISHER_AUTHENTICATION_ERROR, subscriptionAO.getUsername(), repoName);
        }
    }

    /**
     * Class to handle incoming log messages
     */
    public static class MqMessageHandler implements MessageHandler {

        private static Log4JLogger _logger = Log4JLogger.getLogger(MqMessageHandler.class);
        private String _messageID = null;
        private Channel _channel = null;

        /**
         * Constructor
         * 
         * @param channel
         *            Netty channel to write messages into
         */
        public MqMessageHandler(Channel channel, String messageID) {
            _messageID = messageID;
            _channel = channel;
        }

        /**
         * When a message is received, pass it on to the client
         */
        @Override
        public void onMessage(ClientMessage message) {
            try {
                if (!_channel.isOpen() || !_channel.isConnected()) {
                    return;
                }

                LogEntryAO logEntry = new LogEntryAO();
                logEntry.setTimestamp(message.getStringProperty(RepositoryEntryMqMessage.TIMESTAMP));
                logEntry.setSource(message.getStringProperty(RepositoryEntryMqMessage.SOURCE));
                logEntry.setHost(message.getStringProperty(RepositoryEntryMqMessage.HOST));
                logEntry.setSeverity(message.getStringProperty(RepositoryEntryMqMessage.SEVERITY));
                logEntry.setMessage(message.getBodyBuffer().readNullableSimpleString().toString());

                SubscriptionResponseAO responseAO = new SubscriptionResponseAO(_messageID, logEntry);
                String responseJson = JsonTranslator.getInstance().toJson(responseAO);

                _logger.debug("Handling message: %s", responseJson);

                _channel.write(new TextWebSocketFrame(responseJson));

                return;
            } catch (Exception ex) {
                _logger.error(ex, "Error forwarding subscription message JSON HTTP web socket client");
            }
        }

    }
}
