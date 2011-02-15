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

package com.chililog.server.security;

import static org.junit.Assert.*;

import java.security.acl.Group;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Pattern;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.chililog.server.common.AppProperties;
import com.chililog.server.common.ChiliLogException;
import com.chililog.server.data.MongoConnection;
import com.chililog.server.data.UserBO;
import com.chililog.server.data.UserController;
import com.chililog.server.security.JAASCallbackHandler;
import com.chililog.server.security.JAASConfiguration;
import com.chililog.server.security.MongoDBJAASLoginModule;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

public class MongoDBJAASTest
{
    private static DB _db;
    private static JAASConfiguration _config;
    private static JAASCallbackHandler _callbackHandler;

    @BeforeClass
    public static void Setup() throws ChiliLogException
    {
        // Setup a user for testing
        _db = MongoConnection.getInstance().getConnection();
        assertNotNull(_db);

        // Clean up old test data if any exists
        DBCollection coll = _db.getCollection(UserController.MONGODB_COLLECTION_NAME);
        Pattern pattern = Pattern.compile("^NativeMongoDBJAASTestUser[\\w]*$");
        DBObject query = new BasicDBObject();
        query.put("username", pattern);
        coll.remove(query);

        UserBO user = new UserBO();
        user.setUsername("NativeMongoDBJAASTestUser1");
        user.setPassword("abc123!", true);
        user.addRole("junittestrole1");
        user.addRole("junittestrole2");
        UserController.getInstance().save(_db, user);

        // Setup JAAS config
        _config = new JAASConfiguration();
        _callbackHandler = new JAASCallbackHandler();
    }

    @Test
    public void testOK() throws LoginException
    {
        LoginContext lc = login("NativeMongoDBJAASTestUser1", "abc123!");

        Subject subject2 = lc.getSubject();
        assertNotNull(subject2);

        // Check if admin user is in the admin group
        // This code is from org.hornetq.spi.core.security.JAASSecurityManager.getSubjectRoles();
        Set<Group> subjectGroups = subject2.getPrincipals(Group.class);
        Iterator<Group> iter = subjectGroups.iterator();
        Group roles = null;
        while (iter.hasNext())
        {
            Group grp = iter.next();
            String name = grp.getName();
            if (name.equals("Roles"))
            {
                roles = grp;
            }
        }
        assertTrue(roles.isMember(new MongoDBJAASLoginModule.SimplePrincipal("junittestrole1")));
        assertTrue(roles.isMember(new MongoDBJAASLoginModule.SimplePrincipal("junittestrole2")));
        return;
    }

    @Test(expected = LoginException.class)
    public void testBadPassword() throws LoginException
    {
        login("NativeMongoDBJAASTestUser1", "bad password");
    }

    @Test(expected = LoginException.class)
    public void testBadUsername() throws LoginException
    {
        login("bad username", "abc123!");
    }

    public void testSystemUser() throws LoginException
    {
        LoginContext lc = login(AppProperties.getInstance().getJaasSystemUsername(), AppProperties.getInstance()
                .getJaasSystemPassword());

        Subject subject2 = lc.getSubject();
        assertNotNull(subject2);

        // Check if admin user is in the admin group
        // This code is from org.hornetq.spi.core.security.JAASSecurityManager.getSubjectRoles();
        Set<Group> subjectGroups = subject2.getPrincipals(Group.class);
        Iterator<Group> iter = subjectGroups.iterator();
        Group roles = null;
        while (iter.hasNext())
        {
            Group grp = iter.next();
            String name = grp.getName();
            if (name.equals("Roles"))
            {
                roles = grp;
            }
        }
        assertTrue(roles.isMember(new MongoDBJAASLoginModule.SimplePrincipal(AppProperties.getInstance()
                .getJaasSystemRole())));
        return;
    }

    @Test(expected = LoginException.class)
    public void testBadSystemPassword() throws LoginException
    {
        login(AppProperties.getInstance().getJaasSystemUsername(), "abc123!");
    }

    /**
     * <p>
     * JAAS login
     * </p>
     * <p>
     * This code is from org.hornetq.spi.core.security.JAASSecurityManager.getAuthenticatedSubject();. It is how hornetq
     * uses JAAS to authenticate
     * </p>
     * 
     * @param username
     * @param password
     * @return
     * @throws LoginException
     */
    public LoginContext login(String username, String password) throws LoginException
    {
        Subject subject = new Subject();
        subject.getPrincipals().add(new MongoDBJAASLoginModule.SimplePrincipal(username));
        subject.getPrivateCredentials().add(password.toCharArray());

        // The configuration name is set to "ignored" because our implementation of _config does not use it
        // We only have 1 configuration
        LoginContext lc = new LoginContext("ignored", subject, _callbackHandler, _config);
        lc.login();
        return lc;
    }
}
