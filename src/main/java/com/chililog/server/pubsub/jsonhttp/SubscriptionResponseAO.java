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

package com.chililog.server.pubsub.jsonhttp;

/**
 * Subscription Response API JSON binding object. Encapsulates the data for subscription log entries
 */
public class SubscriptionResponseAO
{
    private String _messageType = "SubscriptionResponse";
    private String _messageID = "";
    private LogEntryAO _logEntry = null;
    private boolean _success = true;
    private String _errorMessage = null;
    private String _errorStackTrace = null;

    /**
     * Basic constructor
     */
    public SubscriptionResponseAO()
    {
        return;
    }

    /**
     * Constructor for successful response
     * 
     * @param messageID
     *            Request message id for correlation
     */
    public SubscriptionResponseAO(String messageID)
    {
        _messageID = messageID;
    }
    
    /**
     * Constructor for successful response with associated log entry
     * 
     * @param messageID
     *            Request message id for correlation
     * @param logEntry
     *            Log entry to has been received
     */
    public SubscriptionResponseAO(String messageID, LogEntryAO logEntry)
    {
        _messageID = messageID;
        _logEntry = logEntry;
    }

    /**
     * Constructor for error response
     * 
     * @param messageID
     *            Request message id for correlation
     * @param ex
     *            Exception describing the error
     */
    public SubscriptionResponseAO(String messageID, Throwable ex)
    {
        _success = false;
        _messageID = messageID;
        _errorMessage = ex.getMessage();
        _errorStackTrace = ex.toString();
    }

    /**
     * Returns the type of message: "PublicationRequest"
     * 
     * @return
     */
    public String getMessageType()
    {
        return _messageType;
    }

    public void setMessageType(String messageType)
    {
        _messageType = messageType;
    }

    /**
     * Returns the message id as set in the request
     */
    public String getMessageID()
    {
        return _messageID;
    }

    public void setMessageID(String messageId)
    {
        _messageID = messageId;
    }

    /**
     * Returns the only entry received during subscription.
     */
    public LogEntryAO getLogEntry()
    {
        return _logEntry;
    }

    public void setLogEntry(LogEntryAO logEntry)
    {
        _logEntry = logEntry;
    }

    /**
     * Returns if the request as identified by the message id has been processed successfully. If not, error details are
     * supplied.
     */
    public boolean isSuccess()
    {
        return _success;
    }

    public void setSuccess(boolean success)
    {
        _success = success;
    }

    /**
     * In the event of an unsuccessful processing of a request, returns the error message. If successful, null is
     * returned.
     */
    public String getErrorMessage()
    {
        return _errorMessage;
    }

    public void setErrorMessage(String errorMessage)
    {
        _errorMessage = errorMessage;
    }

    /**
     * Returns the stack trace in the event of an error, null if successful.
     */
    public String getErrorStackTrace()
    {
        return _errorStackTrace;
    }

    public void setErrorStackTrace(String errorStackTrace)
    {
        _errorStackTrace = errorStackTrace;
    }

}
