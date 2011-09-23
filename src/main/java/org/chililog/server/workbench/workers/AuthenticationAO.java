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

package org.chililog.server.workbench.workers;

/**
 * <p>
 * Authentication API Object is used for login
 * </p>
 * 
 * @author vibul
 * 
 */
public class AuthenticationAO {
    private String _username;

    private String _password;

    private ExpiryType _expiryType;

    private int _expirySeconds;

    public AuthenticationAO() {
        return;
    }

    /**
     * The user's login name
     */
    public String getUsername() {
        return _username;
    }

    public void setUsername(String username) {
        _username = username;
    }

    /**
     * The user's password
     */
    public String getPassword() {
        return _password;
    }

    public void setPassword(String password) {
        _password = password;
    }

    /**
     * Method used to determine when the issued authentication token expires. See {@link ExpiryType}
     */
    public ExpiryType getExpiryType() {
        return _expiryType;
    }

    public void setExpiryType(ExpiryType expiryType) {
        _expiryType = expiryType;
    }

    /**
     * Number of seconds before the issued authentication token expires
     */
    public int getExpirySeconds() {
        return _expirySeconds;
    }

    public void setExpirySeconds(int expirySeconds) {
        _expirySeconds = expirySeconds;
    }

    /**
     * Determines how an authentication will exipre
     */
    public static enum ExpiryType {
        /**
         * A new token will be issues to extend the expiry period upon every API call
         */
        Sliding,

        /**
         * A new token will NOT be issued and the expiry period will not be extended
         */
        Absolute
    }
}
