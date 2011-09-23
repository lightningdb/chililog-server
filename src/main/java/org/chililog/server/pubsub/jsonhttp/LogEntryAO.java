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

import org.chililog.server.data.RepositoryEntryBO.Severity;

/**
 * API JSON binding object for log entries.
 * 
 * @author vibul
 */
public class LogEntryAO {
    private String _timestamp;
    private String _source;
    private String _host;
    private String _severity;
    private String _message;

    /**
     * Returns the timestamp at which the log entry was made
     */
    public String getTimestamp() {
        return _timestamp;
    }

    public void setTimestamp(String timestamp) {
        _timestamp = timestamp;
    }

    /**
     * Returns the name of the application or device that generated the log entry
     */
    public String getSource() {
        return _source;
    }

    public void setSource(String source) {
        _source = source;
    }

    /**
     * Returns the hostname or ip address of the device that generated the log entry
     */
    public String getHost() {
        return _host;
    }

    public void setHost(String host) {
        _host = host;
    }

    /**
     * Returns the severity. Can be a number 0 to 7 or a text description of the serverity. See {@link Severity}.
     */
    public String getSeverity() {
        return _severity;
    }

    public void setSeverity(String severity) {
        _severity = severity;
    }

    public String getMessage() {
        return _message;
    }

    public void setMessage(String message) {
        _message = message;
    }

}
