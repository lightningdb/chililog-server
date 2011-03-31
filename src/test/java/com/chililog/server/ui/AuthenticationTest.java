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

package com.chililog.server.ui;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.regex.Pattern;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.chililog.server.common.JsonTranslator;
import com.chililog.server.data.MongoConnection;
import com.chililog.server.data.UserBO;
import com.chililog.server.data.UserController;
import com.chililog.server.data.UserBO.Status;
import com.chililog.server.ui.api.AuthenticationAO;
import com.chililog.server.ui.api.UserAO;
import com.chililog.server.ui.api.AuthenticationAO.ExpiryType;
import com.chililog.server.ui.api.Worker;
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
        
        // Create writer user
        UserBO user = new UserBO();
        user.setUsername("AuthenticationTest");
        user.setPassword("hello there", true);
        user.addRole(Worker.WORKBENCH_ADMINISTRATOR_USER_ROLE);
        UserController.getInstance().save(_db, user);
        
        WebServerManager.getInstance().start();
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
        
        WebServerManager.getInstance().stop();
    }

    
    /**
     * GET = 405 Method Not Allowed
     * 
     * @throws IOException
     */
    @Test
    public void testGET() throws IOException
    {
        // Create a URL for the desired page
        URL url = new URL("http://localhost:8989/api/Authentication");
        URLConnection conn = url.openConnection();

        String content = null;
        try
        {
            conn.getInputStream();
            fail();
        }
        catch (Exception ex)
        {
            ApiUtils.getResponseErrorContent((HttpURLConnection) conn);
        }

        HashMap<String, String> headers = new HashMap<String, String>();
        String responseCode = ApiUtils.getResponseHeaders(conn, headers);
        
        //_logger.debug(ApiUtils.formatResponseForLogging(responseCode, headers, content));

        assertEquals("HTTP/1.1 405 Method Not Allowed", responseCode);
        assertNotNull(headers.get("Date"));
        assertEquals("POST, DELETE", headers.get("Allow"));
        assertNull(headers.get("Content-Type"));
        assertNull(content);
    }
    
    /**
     * PUT = 405 Method Not Allowed
     * 
     * @throws IOException
     */
    @Test
    public void testPUT() throws IOException
    {
        // Create a URL for the desired page
        URL url = new URL("http://localhost:8989/api/Authentication");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("PUT");

        String content = null;
        try
        {
            conn.getInputStream();
            fail();
        }
        catch (Exception ex)
        {
            ApiUtils.getResponseErrorContent((HttpURLConnection) conn);
        }

        HashMap<String, String> headers = new HashMap<String, String>();
        String responseCode = ApiUtils.getResponseHeaders(conn, headers);
        
        //_logger.debug(ApiUtils.formatResponseForLogging(responseCode, headers, content));

        assertEquals("HTTP/1.1 405 Method Not Allowed", responseCode);
        assertNotNull(headers.get("Date"));
        assertEquals("POST, DELETE", headers.get("Allow"));
        assertNull(headers.get("Content-Type"));
        assertNull(content);
    }
    
    /**
     * POST - login successful
     * 
     * @throws IOException
     */
    @Test
    public void testPOST() throws IOException
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
        
        OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
        JsonTranslator.getInstance().toJson(requestContent,  out);
        out.close();

        // Get response
        String responseContent = ApiUtils.getResponseContent(conn);
        
        HashMap<String, String> headers = new HashMap<String, String>();
        String responseCode = ApiUtils.getResponseHeaders(conn, headers);
        
        ApiUtils.check200OKResponse(responseCode, headers);
        assertNotNull(headers.get(Worker.AUTHENTICATION_TOKEN_HEADER));
        
        UserAO loggedInUser = JsonTranslator.getInstance().fromJson(responseContent.toString(), UserAO.class);
        assertEquals("AuthenticationTest", loggedInUser.getUsername());
        assertNull(loggedInUser.getPassword());
        assertEquals(1, loggedInUser.getRoles().length);
        assertEquals(Worker.WORKBENCH_ADMINISTRATOR_USER_ROLE, loggedInUser.getRoles()[0]);
        assertNotNull(loggedInUser.getDocumentID());
        assertEquals(Status.Enabled, loggedInUser.getStatus());
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
        
        OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
        JsonTranslator.getInstance().toJson(requestContent,  out);
        out.close();

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
        
        OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
        JsonTranslator.getInstance().toJson(requestContent,  out);
        out.close();

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
        
        OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
        JsonTranslator.getInstance().toJson(requestContent,  out);
        out.close();

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
        
        OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
        JsonTranslator.getInstance().toJson(requestContent,  out);
        out.close();

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
