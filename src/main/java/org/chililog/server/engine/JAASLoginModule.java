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

package org.chililog.server.engine;

import java.security.Principal;
import java.security.acl.Group;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.apache.commons.lang.StringUtils;
import org.chililog.server.common.AppProperties;
import org.chililog.server.data.MongoConnection;
import org.chililog.server.data.UserBO;
import org.chililog.server.data.UserController;

import com.mongodb.DB;

/**
 * <p>
 * JAAS login module for HornetQ. We lookup the user table, validate the password and then add the roles to the user
 * </p>
 * 
 * @author vibul
 * 
 */
public class JAASLoginModule implements LoginModule {
    private Subject _subject;
    private String _systemUsername;
    private String _systemPassword;

    /**
     * Basic constructor
     */
    public JAASLoginModule() {
        _systemUsername = AppProperties.getInstance().getMqSystemUsername();
        _systemPassword = AppProperties.getInstance().getMqSystemPassword();
        return;
    }

    public boolean abort() throws LoginException {
        return true;
    }

    public boolean commit() throws LoginException {
        return true;
    }

    public void initialize(final Subject subject,
                           final CallbackHandler callbackHandler,
                           final Map<String, ?> sharedState,
                           final Map<String, ?> options) {
        _subject = subject;
    }

    /**
     * <p>
     * We check the credentials against the repository. By convention, the username is the repository name and the
     * password is either the publisher or subscriber password. The role assigned to the user is constructed from the
     * combination of username and publisher password.
     * </p>
     * 
     * @return Returns true if this method succeeded, or false if this LoginModule should be ignored.
     */
    public boolean login() throws LoginException {
        try {
            //
            // This code is from org.hornetq.spi.core.security.JAASSecurityManager.getAuthenticatedSubject();
            // It is how HornetQ uses JAAS to authenticate
            //
            // Subject subject = new Subject();
            // if (user != null)
            // {
            // subject.getPrincipals().add(principal);
            // }
            // subject.getPrivateCredentials().add(passwordChars);
            // LoginContext lc = new LoginContext(configurationName, subject, callbackHandler, config);

            // Get the user name
            Iterator<Principal> iterator = _subject.getPrincipals().iterator();
            String username = iterator.next().getName();
            if (StringUtils.isBlank(username)) {
                throw new FailedLoginException("Username is requried.");
            }

            // Get the password
            Iterator<char[]> iterator2 = _subject.getPrivateCredentials(char[].class).iterator();
            char[] passwordChars = iterator2.next();
            String password = new String(passwordChars);
            if (StringUtils.isBlank(password)) {
                throw new FailedLoginException("Password is requried.");
            }

            // Check if system user
            if (username.equals(_systemUsername) && password.equals(_systemPassword)) {
                Group roles = new SimpleGroup("Roles");
                roles.addMember(new SimplePrincipal(UserBO.SYSTEM_ADMINISTRATOR_ROLE_NAME));
                _subject.getPrincipals().add(roles);
                return true;
            }

            // Let's validate non-system user
            DB db = MongoConnection.getInstance().getConnection();
            UserBO user = UserController.getInstance().tryGetByUsername(db, username);
            if (user == null) {
                throw new FailedLoginException("Invalid username or password.");
            }
            if (StringUtils.isBlank(password) || !user.validatePassword(password)) {
                throw new FailedLoginException("Invalid username or password.");
            }

            // Add role
            Group roles = new SimpleGroup("Roles");
            for (String role : user.getRoles()) {
                roles.addMember(new SimplePrincipal(role));
            }
            _subject.getPrincipals().add(roles);

            // OK
            return true;
        }
        catch (Exception ex) {
            throw new LoginException(ex.getMessage());
        }
    }

    /**
     * There is nothing special to do to log out. We don't have sessions to clear.
     */
    public boolean logout() throws LoginException {
        return true;
    }

    /**
     * @return Returns the current subject of authentication
     */
    public Subject getSubject() {
        return _subject;
    }

    /**
     * Group to store a collection of roles expressed as a Principal
     * 
     * @author vibul
     * 
     */
    public class SimpleGroup implements Group {
        private final String _name;

        private final Set<Principal> _members = new HashSet<Principal>();

        public SimpleGroup(final String name) {
            this._name = name;
        }

        public boolean addMember(final Principal principal) {
            return _members.add(principal);
        }

        public boolean isMember(final Principal principal) {
            return _members.contains(principal);
        }

        public Enumeration<? extends Principal> members() {
            return Collections.enumeration(_members);
        }

        public boolean removeMember(final Principal principal) {
            return _members.remove(principal);
        }

        public String getName() {
            return _name;
        }
    }

    /**
     * Simple principal for storing a role name
     * 
     * @author vibul
     * 
     */
    public static class SimplePrincipal implements Principal, java.io.Serializable {
        private static final long serialVersionUID = 1L;

        private final String _name;

        public SimplePrincipal(final String name) {
            this._name = name;
        }

        /**
         * Compare this SimplePrincipal's name against another Principal
         * 
         * @return true if name equals another.getName();
         */
        @Override
        public boolean equals(final Object another) {
            if (!(another instanceof Principal)) {
                return false;
            }
            String anotherName = ((Principal) another).getName();
            boolean equals = false;
            if (_name == null) {
                equals = anotherName == null;
            }
            else {
                equals = _name.equals(anotherName);
            }
            return equals;
        }

        @Override
        public int hashCode() {
            return _name == null ? 0 : _name.hashCode();
        }

        @Override
        public String toString() {
            return _name;
        }

        public String getName() {
            return _name;
        }
    }
}
