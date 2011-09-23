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

package org.chililog.server.data;

import static org.junit.Assert.*;

import java.util.List;
import java.util.regex.Pattern;

import org.bson.types.ObjectId;
import org.chililog.server.common.ChiliLogException;
import org.chililog.server.data.MongoConnection;
import org.chililog.server.data.Strings;
import org.chililog.server.data.UserBO;
import org.chililog.server.data.UserController;
import org.chililog.server.data.UserListCriteria;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

/**
 * Test UserBO and UserController
 * 
 * @author vibul
 * 
 */
public class UserTest {

    private static DB _db;

    @BeforeClass
    public static void classSetup() throws Exception {
        _db = MongoConnection.getInstance().getConnection();
        assertNotNull(_db);

        // Clean up old test data if any exists
        DBCollection coll = _db.getCollection(UserController.MONGODB_COLLECTION_NAME);
        Pattern pattern = Pattern.compile("^UserTestUser[\\w]*$");
        DBObject query = new BasicDBObject();
        query.put(UserBO.USERNAME_FIELD_NAME, pattern);
        coll.remove(query);
    }

    @AfterClass
    public static void classTeardown() {
        // Clean up old test data if any exists
        DBCollection coll = _db.getCollection(UserController.MONGODB_COLLECTION_NAME);
        Pattern pattern = Pattern.compile("^UserTestUser[\\w]*$");
        DBObject query = new BasicDBObject();
        query.put(UserBO.USERNAME_FIELD_NAME, pattern);
        coll.remove(query);
    }

    @Test
    public void testPassword() throws ChiliLogException {
        UserBO user = new UserBO();
        user.setPassword("abc", true);
        assertTrue(user.validatePassword("abc"));
        assertFalse(user.validatePassword("ABC"));
    }

    @Test(expected = ChiliLogException.class)
    public void testGetNotFound() throws ChiliLogException {
        UserController.getInstance().getByUsername(_db, "notfound");
    }

    @Test
    public void testTryGetNotFound() throws ChiliLogException {
        UserBO user = UserController.getInstance().tryGetByUsername(_db, "notfound");
        assertNull(user);
    }

    @Test
    public void testCRUD() throws ChiliLogException {
        // Insert
        UserBO user = new UserBO();
        user.setUsername("UserTestUser1");
        user.setPassword("abc123!", true);
        user.addRole("junittestrole1");
        user.addRole("junittestrole2");
        user.setDisplayName("Lloyd Christmas");
        user.setEmailAddress("UserTestUser1@chililog.com");

        assertFalse(user.isExistingRecord());
        assertNull(user.getDocumentID());
        assertEquals(-1, user.getDocumentVersion());

        UserController.getInstance().save(_db, user);
        assertTrue(user.isExistingRecord());
        assertNotNull(user.getDocumentID());
        assertEquals(1, user.getDocumentVersion());

        // Get
        UserBO user2 = UserController.getInstance().getByUsername(_db, "UserTestUser1");
        assertEquals("UserTestUser1", user2.getUsername());
        assertTrue(user2.hasRole("junittestrole1"));
        assertTrue(user2.hasRole("junittestrole2"));
        assertEquals(UserBO.Status.ENABLED, user2.getStatus());
        assertEquals("Lloyd Christmas", user2.getDisplayName());
        assertEquals("UserTestUser1@chililog.com", user2.getEmailAddress());
        assertEquals(1, user2.getDocumentVersion());

        String id = user.getDocumentID().toString();
        user2 = UserController.getInstance().get(_db, new ObjectId(id));
        assertEquals("UserTestUser1", user2.getUsername());

        // Update
        user.setUsername("UserTestUser2");
        user.addRole("junittestrole3");
        user.setStatus(UserBO.Status.DISABLED);
        user.setDisplayName("Harry Dunne");
        user.setEmailAddress("UserTestUser1update@chililog.com");
        UserController.getInstance().save(_db, user);
        assertEquals(2, user.getDocumentVersion());

        user2 = UserController.getInstance().getByEmailAddress(_db, "UserTestUser1update@chililog.com");
        assertEquals("UserTestUser2", user2.getUsername());
        assertTrue(user2.hasRole("junittestrole1"));
        assertTrue(user2.hasRole("junittestrole2"));
        assertTrue(user2.hasRole("junittestrole3"));
        assertEquals(UserBO.Status.DISABLED, user2.getStatus());
        assertEquals("Harry Dunne", user2.getDisplayName());
        assertEquals("UserTestUser1update@chililog.com", user2.getEmailAddress());
        assertEquals(2, user2.getDocumentVersion());

        // Remove
        UserController.getInstance().remove(_db, user);

        // Get again
        user2 = UserController.getInstance().tryGetByUsername(_db, "UserTestUser1");
        assertNull(user2);
        user2 = UserController.getInstance().tryGetByUsername(_db, "UserTestUser2");
        assertNull(user2);

        // Remove again should not throw an error
        UserController.getInstance().remove(_db, user);
    }

    public void testRoles() {
        UserBO user = new UserBO();

        user.addRole("role1");
        assertTrue(user.hasRole("role1"));
        user.addRole("role2");
        assertTrue(user.hasRole("role2"));
        user.addRole("role3");
        assertTrue(user.hasRole("role3"));

        String[] roles = user.getRoles();
        assertEquals("role1", roles[0]);
        assertEquals("role2", roles[1]);
        assertEquals("role3", roles[2]);
        assertEquals(3, roles.length);

        user.removeRole("role1");
        assertFalse(user.hasRole("role1"));
        assertTrue(user.hasRole("role2"));
        assertTrue(user.hasRole("role3"));

        roles = user.getRoles();
        assertEquals("role2", roles[0]);
        assertEquals("role3", roles[1]);
        assertEquals(2, roles.length);
    }

    @Test
    public void testDuplicateUsername() throws ChiliLogException {
        try {
            // Insert
            UserBO user = new UserBO();
            user.setUsername("UserTestUser3");
            user.setPassword("abc123!", true);
            user.setEmailAddress("UserTestUser3@chililog.com");
            UserController.getInstance().save(_db, user);

            UserBO user2 = new UserBO();
            user2.setUsername("UserTestUser3");
            user2.setPassword("abc123!", true);
            user.setEmailAddress("UserTestUser3@chililog.com");
            UserController.getInstance().save(_db, user2);

            fail("Exception expected");
        } catch (ChiliLogException ex) {
            assertEquals(Strings.USER_DUPLICATE_USERNAME_ERROR, ex.getErrorCode());
        }
    }

    @Test
    public void testEmptyUsername() throws ChiliLogException {
        try {
            // Insert
            UserBO user = new UserBO();
            user.setUsername("");
            user.setPassword("abc123!", true);
            UserController.getInstance().save(_db, user);

            fail("Exception expected");
        } catch (ChiliLogException ex) {
            assertEquals(Strings.MONGODB_MISSING_REQURIED_FIELD_ERROR, ex.getErrorCode());
        }
    }

    @Test
    public void testDuplicateEmailAddress() throws ChiliLogException {
        try {
            // Insert
            UserBO user = new UserBO();
            user.setUsername("UserTestUser6");
            user.setPassword("abc123!", true);
            user.setEmailAddress("UserTestUser6@chililog.com");
            UserController.getInstance().save(_db, user);

            UserBO user2 = new UserBO();
            user2.setUsername("UserTestUser6.1");
            user2.setPassword("abc123!", true);
            user2.setEmailAddress("UserTestUser6@chililog.com");
            UserController.getInstance().save(_db, user2);

            fail("Exception expected");
        } catch (ChiliLogException ex) {
            assertEquals(Strings.USER_DUPLICATE_EMAIL_ADDRESS_ERROR, ex.getErrorCode());
        }
    }

    @Test
    public void testBadEmailAddress() throws ChiliLogException {
        try {
            // Insert
            UserBO user = new UserBO();
            user.setUsername("UserTestUser7");
            user.setPassword("abc123!", true);
            user.setEmailAddress("bad email address");
            UserController.getInstance().save(_db, user);

            fail("Exception expected");
        } catch (ChiliLogException ex) {
            assertEquals(Strings.USER_EMAIL_ADDRESS_FORMAT_ERROR, ex.getErrorCode());
        }
    }

    @Test
    public void testNoEmailAddress() throws ChiliLogException {
        UserBO user = new UserBO();
        user.setUsername("UserTestUser8");
        user.setPassword("abc123!", true);
        UserController.getInstance().save(_db, user);
    }

    @Test
    public void testList() throws ChiliLogException {
        // Insert
        UserBO user = new UserBO();
        user.setUsername("UserTestUserList4");
        user.setPassword("abc123!", true);
        user.addRole("ListRoleA");
        user.addRole("ListRoleB");
        user.setEmailAddress("UserTestUserList4@chililog.com");
        UserController.getInstance().save(_db, user);

        UserBO user2 = new UserBO();
        user2.setUsername("UserTestUserList5");
        user2.setPassword("abc123!", true);
        user2.addRole("ListRoleA");
        user2.addRole("ListRoleC");
        user2.setEmailAddress("UserTestUserList5@chililog.com");
        user2.setStatus(UserBO.Status.DISABLED);
        UserController.getInstance().save(_db, user2);

        List<UserBO> list = null;

        // ***************************
        // role
        // ***************************
        UserListCriteria criteria = new UserListCriteria();
        criteria.setRole("ListRoleA");
        list = UserController.getInstance().getList(_db, criteria);
        assertEquals(2, list.size());
        assertEquals("UserTestUserList4", list.get(0).getUsername());
        assertEquals("UserTestUserList5", list.get(1).getUsername());

        criteria = new UserListCriteria();
        criteria.setRole("ListRoleB");
        list = UserController.getInstance().getList(_db, criteria);
        assertEquals(1, list.size());
        assertEquals("UserTestUserList4", list.get(0).getUsername());

        criteria = new UserListCriteria();
        criteria.setRole("no matching role");
        list = UserController.getInstance().getList(_db, criteria);
        assertEquals(0, list.size());

        // ***************************
        // username pattern
        // ***************************
        criteria = new UserListCriteria();
        criteria.setUsernamePattern("^UserTestUserList[\\w]*$");
        list = UserController.getInstance().getList(_db, criteria);
        assertEquals(2, list.size());
        assertEquals("UserTestUserList4", list.get(0).getUsername());
        assertEquals("UserTestUserList5", list.get(1).getUsername());

        criteria = new UserListCriteria();
        criteria.setUsernamePattern("^no matches for sure[\\w]*$");
        list = UserController.getInstance().getList(_db, criteria);
        assertEquals(0, list.size());

        // ***************************
        // email address pattern
        // ***************************
        criteria = new UserListCriteria();
        criteria.setEmailAddressPattern("^UserTestUserList[\\w]*@chililog\\.com$");
        list = UserController.getInstance().getList(_db, criteria);
        assertEquals(2, list.size());
        assertEquals("UserTestUserList4", list.get(0).getUsername());
        assertEquals("UserTestUserList5", list.get(1).getUsername());

        criteria = new UserListCriteria();
        criteria.setEmailAddressPattern("^no matches for sure[\\w]*$");
        list = UserController.getInstance().getList(_db, criteria);
        assertEquals(0, list.size());

        // ***************************
        // status
        // ***************************
        criteria = new UserListCriteria();
        criteria.setUsernamePattern("^UserTestUserList[\\w]*$");
        criteria.setStatus(UserBO.Status.ENABLED);
        list = UserController.getInstance().getList(_db, criteria);
        assertEquals(1, list.size());
        assertEquals("UserTestUserList4", list.get(0).getUsername());

        criteria = new UserListCriteria();
        criteria.setUsernamePattern("^UserTestUserList[\\w]*$");
        criteria.setStatus(UserBO.Status.LOCKED);
        list = UserController.getInstance().getList(_db, criteria);
        assertEquals(0, list.size());

        // ***************************
        // role pattern
        // ***************************
        criteria = new UserListCriteria();
        criteria.setRolePattern("^ListRoleA$");
        list = UserController.getInstance().getList(_db, criteria);
        assertEquals(2, list.size());
        assertEquals("UserTestUserList4", list.get(0).getUsername());
        assertEquals("UserTestUserList5", list.get(1).getUsername());

        criteria = new UserListCriteria();
        criteria.setRolePattern("^ListRoleB$");
        list = UserController.getInstance().getList(_db, criteria);
        assertEquals(1, list.size());
        assertEquals("UserTestUserList4", list.get(0).getUsername());

        criteria = new UserListCriteria();
        criteria.setRolePattern("^no matches for sure[\\w]*$");
        list = UserController.getInstance().getList(_db, criteria);
        assertEquals(0, list.size());
    }

    @Test
    public void testExtractRepositoryNameFromRole() {
        assertEquals("aaa", UserBO.extractRepositoryNameFromRole("repo.aaa.administrator"));
        assertEquals("bbb", UserBO.extractRepositoryNameFromRole("repo.bbb.workbench"));
        assertEquals("ccc", UserBO.extractRepositoryNameFromRole("repo.ccc.publisher"));
        assertEquals("ddd", UserBO.extractRepositoryNameFromRole("repo.ddd.subscriber"));
        assertNull(UserBO.extractRepositoryNameFromRole(UserBO.SYSTEM_ADMINISTRATOR_ROLE_NAME));
        assertNull(UserBO.extractRepositoryNameFromRole("random string sssss"));
    }
}
