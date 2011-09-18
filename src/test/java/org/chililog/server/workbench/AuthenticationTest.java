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

package org.chililog.server.workbench;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.regex.Pattern;

import org.chililog.server.common.BuildProperties;
import org.chililog.server.common.JsonTranslator;
import org.chililog.server.data.MongoConnection;
import org.chililog.server.data.UserBO;
import org.chililog.server.data.UserController;
import org.chililog.server.data.UserBO.Status;
import org.chililog.server.workbench.WorkbenchService;
import org.chililog.server.workbench.workers.AuthenticatedUserAO;
import org.chililog.server.workbench.workers.AuthenticatedUserPasswordAO;
import org.chililog.server.workbench.workers.AuthenticationAO;
import org.chililog.server.workbench.workers.ErrorAO;
import org.chililog.server.workbench.workers.Worker;
import org.chililog.server.workbench.workers.AuthenticationAO.ExpiryType;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

/**
 * Test the Authentication API
 * 
 * @author vibul
 * 
 */
public class AuthenticationTest
{
    private static DB _db;

    @BeforeClass
    public static void classSetup() throws Exception
    {
        _db = MongoConnection.getInstance().getConnection();
        assertNotNull(_db);

        // Create user
        UserBO user = new UserBO();
        user.setUsername("AuthenticationTest");
        user.setEmailAddress("AuthenticationTest@chililog.com");
        user.setPassword("hello there", true);
        user.addRole(UserBO.SYSTEM_ADMINISTRATOR_ROLE_NAME);
        user.setStatus(Status.ENABLED);
        UserController.getInstance().save(_db, user);

        // Create user to change profile
        user = new UserBO();
        user.setUsername("AuthenticationTest_UpdateProfile");
        user.setEmailAddress("AuthenticationTest_UpdateProfile@chililog.com");
        user.setPassword("hello there", true);
        user.addRole("repo.sandpit.workbench");
        user.setStatus(Status.ENABLED);
        UserController.getInstance().save(_db, user);

        // Create user to change profile
        user = new UserBO();
        user.setUsername("AuthenticationTest_ChangePassword");
        user.setEmailAddress("AuthenticationTest_ChangePassword@chililog.com");
        user.setPassword("hello there", true);
        user.addRole("repo.sandpit.workbench");
        user.setStatus(Status.ENABLED);
        UserController.getInstance().save(_db, user);

        // Create disabled user
        user = new UserBO();
        user.setUsername("AuthenticationTest_DisabledUser");
        user.setEmailAddress("AuthenticationTest_DisabledUser@chililog.com");
        user.setPassword("hello there", true);
        user.addRole(UserBO.SYSTEM_ADMINISTRATOR_ROLE_NAME);
        user.setStatus(Status.DISABLED);
        UserController.getInstance().save(_db, user);

        // Create locked out user
        user = new UserBO();
        user.setUsername("AuthenticationTest_LockedUser");
        user.setEmailAddress("AuthenticationTest_LockedUser@chililog.com");
        user.setPassword("hello there", true);
        user.addRole(UserBO.SYSTEM_ADMINISTRATOR_ROLE_NAME);
        user.setStatus(Status.LOCKED);
        UserController.getInstance().save(_db, user);

        // Create no access user
        user = new UserBO();
        user.setUsername("AuthenticationTest_AccessDeniedUser");
        user.setEmailAddress("AuthenticationTest_AccessDeniedUser@chililog.com");
        user.setPassword("hello there", true);
        user.addRole("repo.sandpit.publisher");
        user.addRole("repo.chililog.subscriber");
        user.setStatus(Status.ENABLED);
        UserController.getInstance().save(_db, user);

        WorkbenchService.getInstance().start();
    }

    @AfterClass
    public static void classTeardown()
    {
        // Clean up old test data if any exists
        DBCollection coll = _db.getCollection(UserController.MONGODB_COLLECTION_NAME);
        Pattern pattern = Pattern.compile("^AuthenticationTest[\\w]*$");
        DBObject query = new BasicDBObject();
        query.put("username", pattern);
        coll.remove(query);

        WorkbenchService.getInstance().stop();
    }

    /**
     * GEt the logged in user's details
     * 
     * @throws Exception
     */
    @Test
    public void testGET() throws Exception
    {
        HttpURLConnection httpConn;
        StringBuilder responseContent = new StringBuilder();
        StringBuilder responseCode = new StringBuilder();
        HashMap<String, String> headers = new HashMap<String, String>();

        // Login OK
        String token = ApiUtils.login("AuthenticationTest", "hello there");

        // Get user details
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/authentication", HttpMethod.GET, token);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        AuthenticatedUserAO readResponseAO = JsonTranslator.getInstance().fromJson(responseContent.toString(),
                AuthenticatedUserAO.class);
        assertEquals("AuthenticationTest", readResponseAO.getUsername());
        assertEquals("AuthenticationTest@chililog.com", readResponseAO.getEmailAddress());

        BuildProperties buildProperties = BuildProperties.getInstance();
        assertEquals(buildProperties.getAppVersion(), headers.get(Worker.AUTHENTICATION_SERVER_VERSION));
        assertEquals(buildProperties.getBuildTimestamp(), headers.get(Worker.AUTHENTICATION_SERVER_BUILD_TIMESTAMP));
    } 

    /**
     * Update profile
     * 
     * @throws Exception
     */
    @Test
    public void testUpdateProfile() throws Exception
    {
        HttpURLConnection httpConn;
        StringBuilder responseContent = new StringBuilder();
        StringBuilder responseCode = new StringBuilder();
        HashMap<String, String> headers = new HashMap<String, String>();

        // Login OK
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/authentication", HttpMethod.POST, null);

        AuthenticationAO requestContent = new AuthenticationAO();
        requestContent.setUsername("AuthenticationTest_UpdateProfile");
        requestContent.setPassword("hello there");
        requestContent.setExpiryType(ExpiryType.Absolute);
        requestContent.setExpirySeconds(6000);

        ApiUtils.sendJSON(httpConn, requestContent);
        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        AuthenticatedUserAO authenticatedUser = JsonTranslator.getInstance().fromJson(responseContent.toString(),
                AuthenticatedUserAO.class);
        assertEquals("AuthenticationTest_UpdateProfile", authenticatedUser.getUsername());
        assertEquals("AuthenticationTest_UpdateProfile@chililog.com", authenticatedUser.getEmailAddress());
        assertNull(authenticatedUser.getDisplayName());

        String token = headers.get(Worker.AUTHENTICATION_TOKEN_HEADER);

        // Update OK
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/authentication?action=update_profile",
                HttpMethod.PUT, token);

        authenticatedUser.setUsername("AuthenticationTest_UpdateProfile2");
        authenticatedUser.setEmailAddress("AuthenticationTest_UpdateProfile2@chililog.com");
        authenticatedUser.setDisplayName("Changed Man");

        ApiUtils.sendJSON(httpConn, authenticatedUser);
        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        authenticatedUser = JsonTranslator.getInstance()
                .fromJson(responseContent.toString(), AuthenticatedUserAO.class);
        assertEquals("AuthenticationTest_UpdateProfile2", authenticatedUser.getUsername());
        assertEquals("AuthenticationTest_UpdateProfile2@chililog.com", authenticatedUser.getEmailAddress());
        assertEquals("Changed Man", authenticatedUser.getDisplayName());

        // Update - error wrong document id
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/authentication?action=update_profile",
                HttpMethod.PUT, token);

        AuthenticatedUserAO request = new AuthenticatedUserAO();
        request.setDocumentID("badid");
        request.setDocumentVersion(4L);
        request.setUsername("AuthenticationTest_UpdateProfile2");
        request.setEmailAddress("AuthenticationTest_UpdateProfile2@chililog.com");
        request.setDisplayName("Changed Man");

        ApiUtils.sendJSON(httpConn, request);
        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check401UnauthorizedResponse(responseCode.toString(), headers);

        ErrorAO errorAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), ErrorAO.class);
        assertEquals("ChiliLogException:Workbench.NotAuthorizedError", errorAO.getErrorCode());

        // Update - error missing username
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/authentication?action=update_profile",
                HttpMethod.PUT, token);

        request = new AuthenticatedUserAO();
        request.setDocumentID(authenticatedUser.getDocumentID());
        request.setDocumentVersion(authenticatedUser.getDocumentVersion());
        request.setUsername(null);
        request.setEmailAddress("AuthenticationTest_UpdateProfile2@chililog.com");
        request.setDisplayName("Changed Man");

        ApiUtils.sendJSON(httpConn, request);
        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check400BadRequestResponse(responseCode.toString(), headers);

        errorAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), ErrorAO.class);
        assertEquals("ChiliLogException:Data.MongoDB.MissingRequiredFieldError", errorAO.getErrorCode());

        // Update - error duplicate username
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/authentication?action=update_profile",
                HttpMethod.PUT, token);

        request = new AuthenticatedUserAO();
        request.setDocumentID(authenticatedUser.getDocumentID());
        request.setDocumentVersion(authenticatedUser.getDocumentVersion());
        request.setUsername("AuthenticationTest");
        request.setEmailAddress("AuthenticationTest_UpdateProfile2@chililog.com");
        request.setDisplayName("Changed Man");

        ApiUtils.sendJSON(httpConn, request);
        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check400BadRequestResponse(responseCode.toString(), headers);

        errorAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), ErrorAO.class);
        assertEquals("ChiliLogException:Data.User.DuplicateUsernameError", errorAO.getErrorCode());

        // Update - duplicate email
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/authentication?action=update_profile",
                HttpMethod.PUT, token);

        request = new AuthenticatedUserAO();
        request.setDocumentID(authenticatedUser.getDocumentID());
        request.setDocumentVersion(authenticatedUser.getDocumentVersion());
        request.setUsername("AuthenticationTest_UpdateProfile2");
        request.setEmailAddress("AuthenticationTest@chililog.com");
        request.setDisplayName("Changed Man");

        ApiUtils.sendJSON(httpConn, request);
        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check400BadRequestResponse(responseCode.toString(), headers);

        errorAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), ErrorAO.class);
        assertEquals("ChiliLogException:Data.User.DuplicateEmailAddressError", errorAO.getErrorCode());
    }

    /**
     * Change password
     * 
     * @throws Exception
     */
    @Test
    public void testChangePassword() throws Exception
    {
        HttpURLConnection httpConn;
        StringBuilder responseContent = new StringBuilder();
        StringBuilder responseCode = new StringBuilder();
        HashMap<String, String> headers = new HashMap<String, String>();

        // Login OK
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/authentication", HttpMethod.POST, null);

        AuthenticationAO requestContent = new AuthenticationAO();
        requestContent.setUsername("AuthenticationTest_ChangePassword");
        requestContent.setPassword("hello there");
        requestContent.setExpiryType(ExpiryType.Absolute);
        requestContent.setExpirySeconds(6000);

        ApiUtils.sendJSON(httpConn, requestContent);
        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        AuthenticatedUserAO authenticatedUser = JsonTranslator.getInstance().fromJson(responseContent.toString(),
                AuthenticatedUserAO.class);
        assertEquals("AuthenticationTest_ChangePassword", authenticatedUser.getUsername());
        assertEquals("AuthenticationTest_ChangePassword@chililog.com", authenticatedUser.getEmailAddress());
        assertNull(authenticatedUser.getDisplayName());

        String token = headers.get(Worker.AUTHENTICATION_TOKEN_HEADER);

        // Change password OK
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/authentication?action=change_password",
                HttpMethod.PUT, token);

        AuthenticatedUserPasswordAO request = new AuthenticatedUserPasswordAO();
        request.setDocumentID(authenticatedUser.getDocumentID());
        request.setOldPassword("hello there");
        request.setNewPassword("bye");
        request.setConfirmNewPassword("bye");

        ApiUtils.sendJSON(httpConn, request);
        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        authenticatedUser = JsonTranslator.getInstance()
                .fromJson(responseContent.toString(), AuthenticatedUserAO.class);
        assertEquals("AuthenticationTest_ChangePassword", authenticatedUser.getUsername());
        assertEquals("AuthenticationTest_ChangePassword@chililog.com", authenticatedUser.getEmailAddress());
        assertNull(authenticatedUser.getDisplayName());

        // Login again OK
        ApiUtils.login("AuthenticationTest_ChangePassword", "bye");

        // Change password error - bad old password
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/authentication?action=change_password",
                HttpMethod.PUT, token);

        request = new AuthenticatedUserPasswordAO();
        request.setDocumentID(authenticatedUser.getDocumentID());
        request.setOldPassword("bad password");
        request.setNewPassword("bye1");
        request.setConfirmNewPassword("bye1");

        ApiUtils.sendJSON(httpConn, request);
        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check401UnauthorizedResponse(responseCode.toString(), headers);

        ErrorAO errorAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), ErrorAO.class);
        assertEquals("ChiliLogException:Workbench.AuthenticationBadUsernameOrPasswordError", errorAO.getErrorCode());

        // Change password error - bad confirm password
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/authentication?action=change_password",
                HttpMethod.PUT, token);

        request = new AuthenticatedUserPasswordAO();
        request.setDocumentID(authenticatedUser.getDocumentID());
        request.setOldPassword("bye");
        request.setNewPassword("bye1");
        request.setConfirmNewPassword("bye2");

        ApiUtils.sendJSON(httpConn, request);
        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check401UnauthorizedResponse(responseCode.toString(), headers);

        errorAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), ErrorAO.class);
        assertEquals("ChiliLogException:Workbench.AuthenticationBadUsernameOrPasswordError", errorAO.getErrorCode());

    }

    /**
     * Change password
     * 
     * @throws Exception
     */
    @Test
    public void testInvalidPutActions() throws Exception
    {
        HttpURLConnection httpConn;
        StringBuilder responseContent = new StringBuilder();
        StringBuilder responseCode = new StringBuilder();
        HashMap<String, String> headers = new HashMap<String, String>();

        // Login OK
        String token = ApiUtils.login("AuthenticationTest", "hello there");

        // Bad action
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/authentication?action=bad", HttpMethod.PUT,
                token);

        AuthenticatedUserPasswordAO request = new AuthenticatedUserPasswordAO();
        request.setDocumentID("abc");
        request.setOldPassword("hello there");
        request.setNewPassword("bye");
        request.setConfirmNewPassword("bye");

        ApiUtils.sendJSON(httpConn, request);
        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check400BadRequestResponse(responseCode.toString(), headers);

        ErrorAO errorAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), ErrorAO.class);
        assertEquals("java.lang.UnsupportedOperationException", errorAO.getErrorCode());

        // Bad no action
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/authentication", HttpMethod.PUT, token);

        request = new AuthenticatedUserPasswordAO();
        request.setDocumentID("abc");
        request.setOldPassword("hello there");
        request.setNewPassword("bye");
        request.setConfirmNewPassword("bye");

        ApiUtils.sendJSON(httpConn, request);
        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check400BadRequestResponse(responseCode.toString(), headers);

        errorAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), ErrorAO.class);
        assertEquals("ChiliLogException:Workbench.UriQueryStringParameterError", errorAO.getErrorCode());
    }

    /**
     * Change password
     * 
     * @throws Exception
     */
    @Test
    public void testInvalidTokens() throws Exception
    {
        HttpURLConnection httpConn;
        StringBuilder responseContent = new StringBuilder();
        StringBuilder responseCode = new StringBuilder();
        HashMap<String, String> headers = new HashMap<String, String>();

        // Login OK
        String token = ApiUtils.login("AuthenticationTest", "hello there");

        // No token
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/authentication", HttpMethod.GET, null);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check401UnauthorizedResponse(responseCode.toString(), headers);

        ErrorAO errorAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), ErrorAO.class);
        assertEquals("ChiliLogException:Workbench.AuthenticationTokenInvalidError", errorAO.getErrorCode());

        // Bad hash
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/authentication", HttpMethod.GET, token + "abc");

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check401UnauthorizedResponse(responseCode.toString(), headers);

        errorAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), ErrorAO.class);
        assertEquals("ChiliLogException:Workbench.AuthenticationTokenInvalidError", errorAO.getErrorCode());
    }
    
    /**
     * POST - login successful
     * 
     * @throws IOException
     */
    @Test
    public void testPOST_ByUsername() throws IOException
    {
        // Create a URL for the desired page
        URL url = new URL("http://localhost:8989/api/Authentication");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", Worker.JSON_CONTENT_TYPE);

        AuthenticationAO requestContent = new AuthenticationAO();
        requestContent.setUsername("AuthenticationTest");
        requestContent.setPassword("hello there");
        requestContent.setExpiryType(ExpiryType.Absolute);
        requestContent.setExpirySeconds(6000);

        ApiUtils.sendJSON(conn, requestContent);

        // Get response
        String responseContent = ApiUtils.getResponseContent(conn);

        HashMap<String, String> headers = new HashMap<String, String>();
        String responseCode = ApiUtils.getResponseHeaders(conn, headers);

        ApiUtils.check200OKResponse(responseCode, headers);
        assertNotNull(headers.get(Worker.AUTHENTICATION_TOKEN_HEADER));

        AuthenticatedUserAO loggedInUser = JsonTranslator.getInstance().fromJson(responseContent.toString(),
                AuthenticatedUserAO.class);
        assertEquals("AuthenticationTest", loggedInUser.getUsername());
        assertEquals("AuthenticationTest@chililog.com", loggedInUser.getEmailAddress());
        assertNotNull(loggedInUser.getDocumentID());
    }
    
    /**
     * Refresh authentication token  
     * 
     * @throws Exception
     */
    @Test
    public void testPOST_RefreshToken() throws Exception
    {
        HttpURLConnection httpConn;
        StringBuilder responseContent = new StringBuilder();
        StringBuilder responseCode = new StringBuilder();
        HashMap<String, String> headers = new HashMap<String, String>();

        // Login OK
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/authentication", HttpMethod.POST, null);

        AuthenticationAO requestContent = new AuthenticationAO();
        requestContent.setUsername("AuthenticationTest");
        requestContent.setPassword("hello there");
        requestContent.setExpiryType(ExpiryType.Absolute);
        requestContent.setExpirySeconds(60);

        ApiUtils.sendJSON(httpConn, requestContent);
        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        AuthenticatedUserAO authenticatedUser = JsonTranslator.getInstance().fromJson(responseContent.toString(),
                AuthenticatedUserAO.class);
        assertEquals("AuthenticationTest", authenticatedUser.getUsername());
        assertEquals("AuthenticationTest@chililog.com", authenticatedUser.getEmailAddress());
        assertNull(authenticatedUser.getDisplayName());

        String token = headers.get(Worker.AUTHENTICATION_TOKEN_HEADER);
        assertTrue(token.contains("\"ExpirySeconds\": 60"));

        // Refresh token
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/authentication", HttpMethod.POST, token);

        AuthenticationAO refreshRequestContent = new AuthenticationAO();
        refreshRequestContent.setUsername("AuthenticationTest");
        refreshRequestContent.setExpiryType(ExpiryType.Absolute);
        refreshRequestContent.setExpirySeconds(120);

        ApiUtils.sendJSON(httpConn, refreshRequestContent);
        
        StringBuilder refreshResponseContent = new StringBuilder();
        ApiUtils.getResponse(httpConn, refreshResponseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        authenticatedUser = JsonTranslator.getInstance()
                .fromJson(refreshResponseContent.toString(), AuthenticatedUserAO.class);
        assertEquals("AuthenticationTest", authenticatedUser.getUsername());
        assertEquals("AuthenticationTest@chililog.com", authenticatedUser.getEmailAddress());
        assertNull(authenticatedUser.getDisplayName());

        String token2 = headers.get(Worker.AUTHENTICATION_TOKEN_HEADER);
        assertTrue(token2.contains("\"ExpirySeconds\": 120"));
  
        assertNotSame(token, token2);
    }
    
    /**
     * POST - login successful
     * 
     * @throws IOException
     */
    @Test
    public void testPOST_ByEmailAddress() throws IOException
    {
        // Create a URL for the desired page
        URL url = new URL("http://localhost:8989/api/Authentication");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", Worker.JSON_CONTENT_TYPE);

        AuthenticationAO requestContent = new AuthenticationAO();
        requestContent.setUsername("AuthenticationTest@chililog.com");
        requestContent.setPassword("hello there");
        requestContent.setExpiryType(ExpiryType.Absolute);
        requestContent.setExpirySeconds(6000);

        ApiUtils.sendJSON(conn, requestContent);

        // Get response
        String responseContent = ApiUtils.getResponseContent(conn);

        HashMap<String, String> headers = new HashMap<String, String>();
        String responseCode = ApiUtils.getResponseHeaders(conn, headers);

        ApiUtils.check200OKResponse(responseCode, headers);
        assertNotNull(headers.get(Worker.AUTHENTICATION_TOKEN_HEADER));

        AuthenticatedUserAO loggedInUser = JsonTranslator.getInstance().fromJson(responseContent.toString(),
                AuthenticatedUserAO.class);
        assertEquals("AuthenticationTest", loggedInUser.getUsername());
        assertEquals("AuthenticationTest@chililog.com", loggedInUser.getEmailAddress());
        assertNotNull(loggedInUser.getDocumentID());
    }

    /**
     * POST - login failed because user not found
     * 
     * @throws IOException
     */
    @Test
    public void testPOST_UserNotFound() throws IOException
    {
        // Create a URL for the desired page
        URL url = new URL("http://localhost:8989/api/Authentication");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", Worker.JSON_CONTENT_TYPE);

        AuthenticationAO requestContent = new AuthenticationAO();
        requestContent.setUsername("badusername");
        requestContent.setPassword("hello there");
        requestContent.setExpiryType(ExpiryType.Absolute);
        requestContent.setExpirySeconds(6000);

        ApiUtils.sendJSON(conn, requestContent);

        // Get response
        String responseContent = null;
        try
        {
            conn.getInputStream();
            fail();
        }
        catch (Exception ex)
        {
            responseContent = ApiUtils.getResponseErrorContent((HttpURLConnection) conn);
        }

        HashMap<String, String> headers = new HashMap<String, String>();
        String responseCode = ApiUtils.getResponseHeaders(conn, headers);

        assertEquals("HTTP/1.1 401 Unauthorized", responseCode);
        assertNotNull(headers.get("Date"));
        assertNull(headers.get(Worker.AUTHENTICATION_TOKEN_HEADER));
        assertEquals(Worker.JSON_CONTENT_TYPE, headers.get("Content-Type"));
        assertTrue(responseContent.contains("Bad username or password."));
    }

    /**
     * POST - login failed because of a bad password
     * 
     * @throws IOException
     */
    @Test
    public void testPOST_BadPassword() throws IOException
    {
        // Create a URL for the desired page
        URL url = new URL("http://localhost:8989/api/Authentication");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", Worker.JSON_CONTENT_TYPE);

        AuthenticationAO requestContent = new AuthenticationAO();
        requestContent.setUsername("AuthenticationTest");
        requestContent.setPassword("bad password");
        requestContent.setExpiryType(ExpiryType.Absolute);
        requestContent.setExpirySeconds(6000);

        ApiUtils.sendJSON(conn, requestContent);

        // Get response
        String responseContent = null;
        try
        {
            conn.getInputStream();
            fail();
        }
        catch (Exception ex)
        {
            responseContent = ApiUtils.getResponseErrorContent((HttpURLConnection) conn);
        }

        HashMap<String, String> headers = new HashMap<String, String>();
        String responseCode = ApiUtils.getResponseHeaders(conn, headers);

        assertEquals("HTTP/1.1 401 Unauthorized", responseCode);
        assertNotNull(headers.get("Date"));
        assertNull(headers.get(Worker.AUTHENTICATION_TOKEN_HEADER));
        assertEquals(Worker.JSON_CONTENT_TYPE, headers.get("Content-Type"));
        assertTrue(responseContent.contains("Bad username or password."));
    }

    /**
     * POST - login failed because user status is disabled
     * 
     * @throws IOException
     */
    @Test
    public void testPOST_DisabledStatus() throws IOException
    {
        // Create a URL for the desired page
        URL url = new URL("http://localhost:8989/api/Authentication");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", Worker.JSON_CONTENT_TYPE);

        AuthenticationAO requestContent = new AuthenticationAO();
        requestContent.setUsername("AuthenticationTest_DisabledUser");
        requestContent.setPassword("hello there");
        requestContent.setExpiryType(ExpiryType.Absolute);
        requestContent.setExpirySeconds(6000);

        ApiUtils.sendJSON(conn, requestContent);

        // Get response
        String responseContent = null;
        try
        {
            conn.getInputStream();
            fail();
        }
        catch (Exception ex)
        {
            responseContent = ApiUtils.getResponseErrorContent((HttpURLConnection) conn);
        }

        HashMap<String, String> headers = new HashMap<String, String>();
        String responseCode = ApiUtils.getResponseHeaders(conn, headers);

        assertEquals("HTTP/1.1 401 Unauthorized", responseCode);
        assertNotNull(headers.get("Date"));
        assertNull(headers.get(Worker.AUTHENTICATION_TOKEN_HEADER));
        assertEquals(Worker.JSON_CONTENT_TYPE, headers.get("Content-Type"));
        assertTrue(responseContent.contains("Account disabled."));
    }

    /**
     * POST - login failed because user cannot access any repositories.
     * 
     * @throws IOException
     */
    @Test
    public void testPOST_AccessDeniedStatus() throws IOException
    {
        // Create a URL for the desired page
        URL url = new URL("http://localhost:8989/api/Authentication");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", Worker.JSON_CONTENT_TYPE);

        AuthenticationAO requestContent = new AuthenticationAO();
        requestContent.setUsername("AuthenticationTest_AccessDeniedUser");
        requestContent.setPassword("hello there");
        requestContent.setExpiryType(ExpiryType.Absolute);
        requestContent.setExpirySeconds(6000);

        ApiUtils.sendJSON(conn, requestContent);

        // Get response
        String responseContent = null;
        try
        {
            conn.getInputStream();
            fail();
        }
        catch (Exception ex)
        {
            responseContent = ApiUtils.getResponseErrorContent((HttpURLConnection) conn);
        }

        HashMap<String, String> headers = new HashMap<String, String>();
        String responseCode = ApiUtils.getResponseHeaders(conn, headers);

        assertEquals("HTTP/1.1 401 Unauthorized", responseCode);
        assertNotNull(headers.get("Date"));
        assertNull(headers.get(Worker.AUTHENTICATION_TOKEN_HEADER));
        assertEquals(Worker.JSON_CONTENT_TYPE, headers.get("Content-Type"));
        assertTrue(responseContent.contains("Access denied."));
    }

    /**
     * POST - login failed because user status is locked
     * 
     * @throws IOException
     */
    @Test
    public void testPOST_LockedStatus() throws IOException
    {
        // Create a URL for the desired page
        URL url = new URL("http://localhost:8989/api/Authentication");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", Worker.JSON_CONTENT_TYPE);

        AuthenticationAO requestContent = new AuthenticationAO();
        requestContent.setUsername("AuthenticationTest_LockedUser");
        requestContent.setPassword("hello there");
        requestContent.setExpiryType(ExpiryType.Absolute);
        requestContent.setExpirySeconds(6000);

        ApiUtils.sendJSON(conn, requestContent);

        // Get response
        String responseContent = null;
        try
        {
            conn.getInputStream();
            fail();
        }
        catch (Exception ex)
        {
            responseContent = ApiUtils.getResponseErrorContent((HttpURLConnection) conn);
        }

        HashMap<String, String> headers = new HashMap<String, String>();
        String responseCode = ApiUtils.getResponseHeaders(conn, headers);

        assertEquals("HTTP/1.1 401 Unauthorized", responseCode);
        assertNotNull(headers.get("Date"));
        assertNull(headers.get(Worker.AUTHENTICATION_TOKEN_HEADER));
        assertEquals(Worker.JSON_CONTENT_TYPE, headers.get("Content-Type"));
        assertTrue(responseContent.contains("Account locked."));
    }
    
    /**
     * POST - login failed because user not supplied
     * 
     * @throws IOException
     */
    @Test
    public void testPOST_NoUser() throws IOException
    {
        // Create a URL for the desired page
        URL url = new URL("http://localhost:8989/api/Authentication");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", Worker.JSON_CONTENT_TYPE);

        AuthenticationAO requestContent = new AuthenticationAO();
        requestContent.setUsername(null);
        requestContent.setPassword("hello there");
        requestContent.setExpiryType(ExpiryType.Absolute);
        requestContent.setExpirySeconds(6000);

        ApiUtils.sendJSON(conn, requestContent);

        // Get response
        String responseContent = null;
        try
        {
            conn.getInputStream();
            fail();
        }
        catch (Exception ex)
        {
            responseContent = ApiUtils.getResponseErrorContent((HttpURLConnection) conn);
        }

        HashMap<String, String> headers = new HashMap<String, String>();
        String responseCode = ApiUtils.getResponseHeaders(conn, headers);

        assertEquals("HTTP/1.1 400 Bad Request", responseCode);
        assertNotNull(headers.get("Date"));
        assertNull(headers.get(Worker.AUTHENTICATION_TOKEN_HEADER));
        assertEquals(Worker.JSON_CONTENT_TYPE, headers.get("Content-Type"));
        assertTrue(responseContent.contains("'Username' is required but not supplied."));
    }

    /**
     * POST - login failed because password not supplied
     * 
     * @throws IOException
     */
    @Test
    public void testPOST_NoPassword() throws IOException
    {
        // Create a URL for the desired page
        URL url = new URL("http://localhost:8989/api/Authentication");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", Worker.JSON_CONTENT_TYPE);

        AuthenticationAO requestContent = new AuthenticationAO();
        requestContent.setUsername("AuthenticationTest");
        requestContent.setPassword(null);
        requestContent.setExpiryType(ExpiryType.Absolute);
        requestContent.setExpirySeconds(6000);

        ApiUtils.sendJSON(conn, requestContent);

        // Get response
        String responseContent = null;
        try
        {
            conn.getInputStream();
            fail();
        }
        catch (Exception ex)
        {
            responseContent = ApiUtils.getResponseErrorContent((HttpURLConnection) conn);
        }

        HashMap<String, String> headers = new HashMap<String, String>();
        String responseCode = ApiUtils.getResponseHeaders(conn, headers);

        assertEquals("HTTP/1.1 400 Bad Request", responseCode);
        assertNotNull(headers.get("Date"));
        assertNull(headers.get(Worker.AUTHENTICATION_TOKEN_HEADER));
        assertEquals(Worker.JSON_CONTENT_TYPE, headers.get("Content-Type"));
        assertTrue(responseContent.contains("Password' is required but not supplied."));
    }

    /**
     * DELETE - logout successful
     * 
     * @throws IOException
     */
    @Test
    public void testDELETE() throws IOException
    {
        // Login
        String authToken = ApiUtils.login("AuthenticationTest", "hello there");

        // Logout
        URL url = new URL("http://localhost:8989/api/Authentication");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("DELETE");
        conn.setRequestProperty(Worker.AUTHENTICATION_TOKEN_HEADER, authToken);

        String logoutResponseContent = ApiUtils.getResponseContent(conn);

        HashMap<String, String> logoutHeaders = new HashMap<String, String>();
        String logoutResponseCode = ApiUtils.getResponseHeaders(conn, logoutHeaders);

        ApiUtils.check204NoContentResponse(logoutResponseCode, logoutHeaders);
        assertEquals("", logoutResponseContent);
    }

    /**
     * DELETE - logout failed. Authentication token not present
     * 
     * @throws IOException
     */
    @Test
    public void testDELETE_AuthenticationTokenNotPresent() throws IOException
    {
        // Logout
        URL url = new URL("http://localhost:8989/api/Authentication");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("DELETE");

        String responseContent = null;
        try
        {
            conn.getInputStream();
            fail();
        }
        catch (Exception ex)
        {
            responseContent = ApiUtils.getResponseErrorContent((HttpURLConnection) conn);
        }

        HashMap<String, String> headers = new HashMap<String, String>();
        String responseCode = ApiUtils.getResponseHeaders(conn, headers);

        assertEquals("HTTP/1.1 401 Unauthorized", responseCode);
        assertNotNull(headers.get("Date"));
        assertNull(headers.get(Worker.AUTHENTICATION_TOKEN_HEADER));

        // Content
        assertEquals(Worker.JSON_CONTENT_TYPE, headers.get("Content-Type"));
        assertTrue(responseContent.contains("Authentication token is invalid. Please login again."));
    }

    /**
     * DELETE - logout failed. Authentication token is bad
     * 
     * @throws IOException
     */
    @Test
    public void testDELETE_AuthenticationTokenInvalid() throws IOException
    {
        // Logout
        URL url = new URL("http://localhost:8989/api/Authentication");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("DELETE");
        conn.setRequestProperty(Worker.AUTHENTICATION_TOKEN_HEADER, "badtoken");

        String responseContent = null;
        try
        {
            conn.getInputStream();
            fail();
        }
        catch (Exception ex)
        {
            responseContent = ApiUtils.getResponseErrorContent((HttpURLConnection) conn);
        }

        HashMap<String, String> headers = new HashMap<String, String>();
        String responseCode = ApiUtils.getResponseHeaders(conn, headers);

        assertEquals("HTTP/1.1 401 Unauthorized", responseCode);
        assertNotNull(headers.get("Date"));
        assertNull(headers.get(Worker.AUTHENTICATION_TOKEN_HEADER));

        // Content
        assertEquals(Worker.JSON_CONTENT_TYPE, headers.get("Content-Type"));
        assertTrue(responseContent.contains("Authentication token is invalid. Please login again."));
    }

    /**
     * DELETE - logout failed. Authentication token expired
     * 
     * @throws IOException
     */
    @Test
    public void testDELETE_AuthenticationTokenExpired() throws IOException
    {
        // Login
        String authToken = ApiUtils.login("AuthenticationTest", "hello there", ExpiryType.Absolute, -1);

        // Logout
        URL url = new URL("http://localhost:8989/api/Authentication");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("DELETE");
        conn.setRequestProperty(Worker.AUTHENTICATION_TOKEN_HEADER, authToken);

        String responseContent = ApiUtils.getResponseContent((HttpURLConnection) conn);

        HashMap<String, String> headers = new HashMap<String, String>();
        String responseCode = ApiUtils.getResponseHeaders(conn, headers);

        assertEquals("HTTP/1.1 401 Unauthorized", responseCode);
        assertNotNull(headers.get("Date"));
        assertNull(headers.get(Worker.AUTHENTICATION_TOKEN_HEADER));

        // Content
        assertEquals(Worker.JSON_CONTENT_TYPE, headers.get("Content-Type"));
        assertTrue(responseContent.contains("Authentication token expired. Please login again."));
    }

}
