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

/**
 * Subscription Request API JSON binding object. Encapsulates the data for subscribing to log entries in a repository
 */
public class SubscriptionRequestAO {

    private String _messageType = "SubscriptionRequest";
    private String _messageID = "";
    private String _repositoryName;
    private String _username;
    private String _password;

    /**
     * Basic constructor
     */
    public SubscriptionRequestAO() {
        return;
    }

    /**
     * Returns the type of message: "SubscriptionRequest"
     */
    public String getMessageType() {
        return _messageType;
    }

    public void setMessageType(String messageType) {
        _messageType = messageType;
    }

    /**
     * Returns the message id as allocated by the caller.
     */
    public String getMessageID() {
        return _messageID;
    }

    public void setMessageID(String messageID) {
        _messageID = messageID;
    }

    /**
     * Returns the name of the repository into which the log entries will be published
     */
    public String getRepositoryName() {
        return _repositoryName;
    }

    public void setRepositoryName(String repositoryName) {
        _repositoryName = repositoryName;
    }

    /**
     * Returns the username for authentication
     */
    public String getUsername() {
        return _username;
    }

    public void setUsername(String username) {
        _username = username;
    }

    /**
     * Returns the password for authentication
     */
    public String getPassword() {
        return _password;
    }

    public void setPassword(String password) {
        _password = password;
    }

}
