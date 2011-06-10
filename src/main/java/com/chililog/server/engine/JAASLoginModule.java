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
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import com.chililog.server.data.RepositoryInfoBO;

/**
 * <p>
 * JAAS login module for HornetQ.
 * </p>
 * <p>
 * We don't validate the username/password. Rather, the username and password is combined to form a role. By convention,
 * the role code will be used to perform authentication and authorisation. For example:
 * </p>
 * 
 * <pre>
 * Repository name: repo1
 * Write queue name: repository.repo1.write
 * Write queue password: pw1
 * Role allowed to write to the queue: repo1~~~pw1  
 * 
 * usename: repo1
 * password: pw1
 * role: repo1~~~pw1
 * </pre>
 * 
 * @author vibul
 * 
 */
public class JAASLoginModule implements LoginModule
{
    private Subject _subject;

    /**
     * Basic constructor
     */
    public JAASLoginModule()
    {
        return;
    }

    public boolean abort() throws LoginException
    {
        return true;
    }

    public boolean commit() throws LoginException
    {
        return true;
    }

    public void initialize(final Subject subject,
                           final CallbackHandler callbackHandler,
                           final Map<String, ?> sharedState,
                           final Map<String, ?> options)
    {
        _subject = subject;
    }

    /**
     * <p>
     * We check the credentials against the data store in the User collection of our MongoDb. If successful, the user's
     * roles are also loaded.
     * </p>
     * 
     * <p>
     * We assume that the
     * </p>
     * 
     * @return True if login is successful, False if not.
     */
    public boolean login() throws LoginException
    {
        try
        {
            //
            // This code is from org.hornetq.spi.core.security.JAASSecurityManager.getAuthenticatedSubject();
            // It is how hornetq uses JAAS to authenticate
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

            // Get the password
            Iterator<char[]> iterator2 = _subject.getPrivateCredentials(char[].class).iterator();
            char[] passwordChars = iterator2.next();
            String password = new String(passwordChars);

            // Don't need to perform user validation.
            // It will be enforced by convention in the role

            // Add roles
            String role = RepositoryInfoBO.createHornetQRoleName(username, password);
            Group roles = new SimpleGroup("Roles");
            roles.addMember(new SimplePrincipal(role));
            _subject.getPrincipals().add(roles);

            return true;
        }
        catch (Exception ex)
        {
            throw new LoginException(ex.getMessage());
        }
    }

    /**
     * There is nothing special to do to log out. We don't have sessions to clear.
     */
    public boolean logout() throws LoginException
    {
        return true;
    }

    /**
     * @return Returns the current subject of authentication
     */
    public Subject getSubject()
    {
        return _subject;
    }

    /**
     * Group to store a collection of roles expressed as a Principal
     * 
     * @author vibul
     * 
     */
    public class SimpleGroup implements Group
    {
        private final String _name;

        private final Set<Principal> _members = new HashSet<Principal>();

        public SimpleGroup(final String name)
        {
            this._name = name;
        }

        public boolean addMember(final Principal principal)
        {
            return _members.add(principal);
        }

        public boolean isMember(final Principal principal)
        {
            return _members.contains(principal);
        }

        public Enumeration<? extends Principal> members()
        {
            return Collections.enumeration(_members);
        }

        public boolean removeMember(final Principal principal)
        {
            return _members.remove(principal);
        }

        public String getName()
        {
            return _name;
        }
    }

    /**
     * Simple principal for storing a role name
     * 
     * @author vibul
     * 
     */
    public static class SimplePrincipal implements Principal, java.io.Serializable
    {
        private static final long serialVersionUID = 1L;

        private final String _name;

        public SimplePrincipal(final String name)
        {
            this._name = name;
        }

        /**
         * Compare this SimplePrincipal's name against another Principal
         * 
         * @return true if name equals another.getName();
         */
        @Override
        public boolean equals(final Object another)
        {
            if (!(another instanceof Principal))
            {
                return false;
            }
            String anotherName = ((Principal) another).getName();
            boolean equals = false;
            if (_name == null)
            {
                equals = anotherName == null;
            }
            else
            {
                equals = _name.equals(anotherName);
            }
            return equals;
        }

        @Override
        public int hashCode()
        {
            return _name == null ? 0 : _name.hashCode();
        }

        @Override
        public String toString()
        {
            return _name;
        }

        public String getName()
        {
            return _name;
        }
    }
}
