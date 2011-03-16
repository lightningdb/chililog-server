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
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.regex.Pattern;

import org.jboss.netty.handler.codec.http.HttpMethod;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.chililog.server.App;
import com.chililog.server.common.JsonTranslator;
import com.chililog.server.data.DelimitedRepositoryController;
import com.chililog.server.data.MongoConnection;
import com.chililog.server.data.RepositoryEntryBO;
import com.chililog.server.data.RepositoryFieldInfoBO;
import com.chililog.server.data.RepositoryInfoBO;
import com.chililog.server.data.RepositoryInfoBO.Status;
import com.chililog.server.data.RepositoryInfoController;
import com.chililog.server.data.UserBO;
import com.chililog.server.data.UserController;
import com.chililog.server.data.RepositoryInfoBO.ParseFieldErrorHandling;
import com.chililog.server.ui.api.ErrorAO;
import com.chililog.server.ui.api.RepositoryAO;
import com.chililog.server.ui.api.Worker;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

/**
 * Test the Repositories API
 * 
 * @author vibul
 * 
 */
public class RepositoriesTest
{
    private static DB _db;
    private static String _adminAuthToken;
    private static String _analystAuthToken;
    private static String _analystWithAccessAuthToken;
    private static String _repoId;

    @BeforeClass
    public static void classSetup() throws Exception
    {
        _db = MongoConnection.getInstance().getConnection();
        assertNotNull(_db);

        // Clean up old user test data if any exists
        DBCollection coll = _db.getCollection(UserController.MONGODB_COLLECTION_NAME);
        Pattern pattern = Pattern.compile("^RepositoriesTest[\\w]*$");
        DBObject query = new BasicDBObject();
        query.put("username", pattern);
        coll.remove(query);

        // Clean up old repository test data if any exists
        coll = _db.getCollection(RepositoryInfoController.MONGODB_COLLECTION_NAME);
        pattern = Pattern.compile("^RepositoriesTest[\\w]*$");
        query = new BasicDBObject();
        query.put("name", pattern);
        coll.remove(query);

        // Create admin user
        UserBO user = new UserBO();
        user.setUsername("RepositoriesTest_Admin");
        user.setPassword("hello", true);
        user.addRole(Worker.WORKBENCH_ADMINISTRATOR_USER_ROLE);
        UserController.getInstance().save(_db, user);

        // Create analyst user
        user = new UserBO();
        user.setUsername("RepositoriesTest_Analyst");
        user.setPassword("hello", true);
        user.addRole(Worker.WORKBENCH_ANALYST_USER_ROLE);
        UserController.getInstance().save(_db, user);

        // Create test repo
        RepositoryInfoBO repoInfo = new RepositoryInfoBO();
        repoInfo.setName("RepositoriesTest_test");
        repoInfo.setDisplayName("RepositoriesTest 1");
        repoInfo.setControllerClassName("com.chililog.server.data.DelimitedRepositoryController");
        repoInfo.setParseFieldErrorHandling(ParseFieldErrorHandling.SkipEntry);
        repoInfo.getProperties().put(DelimitedRepositoryController.DELIMITER_REPO_PROPERTY_NAME, "|");

        RepositoryFieldInfoBO repoFieldInfo = new RepositoryFieldInfoBO();
        repoFieldInfo.setName("field1");
        repoFieldInfo.setDataType(RepositoryFieldInfoBO.DataType.String);
        repoFieldInfo.getProperties().put(DelimitedRepositoryController.POSITION_REPO_FIELD_PROPERTY_NAME, "1");
        repoInfo.getFields().add(repoFieldInfo);

        repoFieldInfo = new RepositoryFieldInfoBO();
        repoFieldInfo.setName("field2");
        repoFieldInfo.setDataType(RepositoryFieldInfoBO.DataType.Integer);
        repoFieldInfo.getProperties().put(DelimitedRepositoryController.POSITION_REPO_FIELD_PROPERTY_NAME, "2");
        repoInfo.getFields().add(repoFieldInfo);

        RepositoryInfoController.getInstance().save(_db, repoInfo);
        _repoId = repoInfo.getDocumentID().toString();

        // Create analyst user
        user = new UserBO();
        user.setUsername("RepositoriesTest_Analyst_WithAccess");
        user.setPassword("hello", true);
        user.addRole(Worker.WORKBENCH_ANALYST_USER_ROLE);
        user.addRole(repoInfo.getReadQueueRole());
        UserController.getInstance().save(_db, user);

        // Add 3 lines
        DelimitedRepositoryController c = new DelimitedRepositoryController(repoInfo);
        RepositoryEntryBO entry = c.parse("line1|1");
        c.save(_db, entry);

        entry = c.parse("line2|2");
        c.save(_db, entry);

        entry = c.parse("line3|3");
        c.save(_db, entry);

        // Start server
        App.startChiliLogServer();

        // Login
        _adminAuthToken = ApiUtils.login("RepositoriesTest_Admin", "hello");
        _analystAuthToken = ApiUtils.login("RepositoriesTest_Analyst", "hello");
        _analystWithAccessAuthToken = ApiUtils.login("RepositoriesTest_Analyst_WithAccess", "hello");
    }

    @AfterClass
    public static void classTeardown() throws Exception
    {
        // Clean up old user test data if any exists
        DBCollection coll = _db.getCollection(UserController.MONGODB_COLLECTION_NAME);
        Pattern pattern = Pattern.compile("^RepositoriesTest[\\w]*$");
        DBObject query = new BasicDBObject();
        query.put("username", pattern);
        coll.remove(query);

        // Clean up old repository test data if any exists
        coll = _db.getCollection(RepositoryInfoController.MONGODB_COLLECTION_NAME);
        pattern = Pattern.compile("^RepositoriesTest[\\w]*$");
        query = new BasicDBObject();
        query.put("name", pattern);
        coll.remove(query);

        App.stopChiliLogServer();
    }

    /**
     * Analyst can only GET
     * 
     * @throws Exception
     */
    @Test
    public void testGet() throws Exception
    {
        HttpURLConnection httpConn;
        StringBuilder responseContent = new StringBuilder();
        StringBuilder responseCode = new StringBuilder();
        HashMap<String, String> headers = new HashMap<String, String>();

        // Get all - admin
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repositories", HttpMethod.GET,
                _adminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        RepositoryAO[] getListResponseAO = JsonTranslator.getInstance().fromJson(responseContent.toString(),
                RepositoryAO[].class);
        assertTrue(getListResponseAO.length > 0);
        String myRepoDocId = null;
        for (RepositoryAO r : getListResponseAO)
        {
            if (r.getName().equals("RepositoriesTest_test"))
            {
                myRepoDocId = r.getDocumentID();
            }
        }
        assertNotNull(myRepoDocId);

        // Get all - analyst with access. Should only get 1 repo back because we only have access to 1 repo
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repositories", HttpMethod.GET,
                _analystWithAccessAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        getListResponseAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), RepositoryAO[].class);
        assertEquals(1, getListResponseAO.length);
        assertEquals("RepositoriesTest_test", getListResponseAO[0].getName());

        // Get 1 - admin
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repositories/" + myRepoDocId,
                HttpMethod.GET, _adminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        RepositoryAO readResponseAO = JsonTranslator.getInstance().fromJson(responseContent.toString(),
                RepositoryAO.class);
        assertEquals(myRepoDocId, readResponseAO.getDocumentID());
        assertEquals("RepositoriesTest_test", readResponseAO.getName());
        assertEquals(Status.ONLINE, readResponseAO.getStatus());

        // Get 1 - analyst with access
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repositories/" + myRepoDocId,
                HttpMethod.GET, _analystWithAccessAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        // Get entries - admin
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repositories/" + myRepoDocId + "/entries",
                HttpMethod.GET, _adminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        String json = responseContent.toString();
        assertTrue(json.contains("\"field1\" : \"line1\" , \"field2\" : 1"));
        assertTrue(json.contains("\"field1\" : \"line2\" , \"field2\" : 2"));
        assertTrue(json.contains("\"field1\" : \"line3\" , \"field2\" : 3"));

        // Get entries - by analyst with access
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repositories/" + myRepoDocId + "/entries",
                HttpMethod.GET, _analystWithAccessAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        String json2 = responseContent.toString();
        assertEquals(json, json2);
        assertTrue(json2.contains("\"field1\" : \"line1\" , \"field2\" : 1"));
        assertTrue(json2.contains("\"field1\" : \"line2\" , \"field2\" : 2"));
        assertTrue(json2.contains("\"field1\" : \"line3\" , \"field2\" : 3"));
    }

    /**
     * Test access restrictions and other errors
     * 
     * @throws Exception
     */
    @Test
    public void testGetErrors() throws Exception
    {
        HttpURLConnection httpConn;
        StringBuilder responseContent = new StringBuilder();
        StringBuilder responseCode = new StringBuilder();
        HashMap<String, String> headers = new HashMap<String, String>();

        // Get all - not allowed
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repositories", HttpMethod.GET,
                _analystAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check204NoContentResponse(responseCode.toString(), headers);

        // Get 1 - from repository with no permission
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repositories/" + _repoId, HttpMethod.GET,
                _analystAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check400BadRequestResponse(responseCode.toString(), headers);

        ErrorAO errorAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), ErrorAO.class);
        assertEquals("ChiliLogException:UI.RepositoryNotFoundError", errorAO.getErrorCode());

        // Get 1 - repository not found
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repositories/123", HttpMethod.GET,
                _analystAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check400BadRequestResponse(responseCode.toString(), headers);

        errorAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), ErrorAO.class);
        assertEquals("ChiliLogException:UI.RepositoryNotFoundError", errorAO.getErrorCode());

        // Get entries - from repository with no permission
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repositories/" + _repoId + "/entries",
                HttpMethod.GET, _analystAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check400BadRequestResponse(responseCode.toString(), headers);

        errorAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), ErrorAO.class);
        assertEquals("ChiliLogException:UI.RepositoryNotFoundError", errorAO.getErrorCode());

        // Get entries - repository not found
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repositories/123/entries", HttpMethod.GET,
                _analystAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check400BadRequestResponse(responseCode.toString(), headers);

        errorAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), ErrorAO.class);
        assertEquals("ChiliLogException:UI.RepositoryNotFoundError", errorAO.getErrorCode());
    }

    /**
     * Test start and stop repositories
     * 
     * @throws Exception
     */
    @Test
    public void testStartStop() throws Exception
    {
        HttpURLConnection httpConn;
        StringBuilder responseContent = new StringBuilder();
        StringBuilder responseCode = new StringBuilder();
        HashMap<String, String> headers = new HashMap<String, String>();

        // Start all where repositories have already started - should not error
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repositories?action=start", HttpMethod.POST,
                _adminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check204NoContentResponse(responseCode.toString(), headers);

        // Stop all
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repositories?action=stop", HttpMethod.POST,
                _adminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check204NoContentResponse(responseCode.toString(), headers);

        // GET all - check that all repositories have stopped
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repositories", HttpMethod.GET,
                _adminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        RepositoryAO[] getListResponseAO = JsonTranslator.getInstance().fromJson(responseContent.toString(),
                RepositoryAO[].class);
        for (RepositoryAO r : getListResponseAO)
        {
            assertEquals(Status.OFFLINE, r.getStatus());
        }

        // Stop all again - should not get error
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repositories?action=stop", HttpMethod.POST,
                _adminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check204NoContentResponse(responseCode.toString(), headers);

        // Start all
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repositories?action=start", HttpMethod.POST,
                _adminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check204NoContentResponse(responseCode.toString(), headers);

        // GET all - check that all repositories have started
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repositories", HttpMethod.GET,
                _adminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        getListResponseAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), RepositoryAO[].class);
        for (RepositoryAO r : getListResponseAO)
        {
            assertEquals(Status.ONLINE, r.getStatus());
        }

        // Stop 1
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repositories/" + _repoId + "?action=stop",
                HttpMethod.POST, _adminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check204NoContentResponse(responseCode.toString(), headers);

        // GET all - check that only our repository have stopped
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repositories", HttpMethod.GET,
                _adminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        getListResponseAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), RepositoryAO[].class);
        for (RepositoryAO r : getListResponseAO)
        {
            if (r.getDocumentID().equals(_repoId))
            {
                assertEquals(Status.OFFLINE, r.getStatus());
            }
            else
            {
                assertEquals(Status.ONLINE, r.getStatus());
            }
        }

        // Start 1
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repositories/" + _repoId + "?action=start",
                HttpMethod.POST, _adminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check204NoContentResponse(responseCode.toString(), headers);

        // GET all - check that all repositories have started
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repositories", HttpMethod.GET,
                _adminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        getListResponseAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), RepositoryAO[].class);
        for (RepositoryAO r : getListResponseAO)
        {
            assertEquals(Status.ONLINE, r.getStatus());
        }
        
        // Reload
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repositories?action=reload",
                HttpMethod.POST, _adminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check204NoContentResponse(responseCode.toString(), headers);
    }

    /**
     * Test access restrictions and other errors
     * 
     * @throws Exception
     */
    @Test
    public void testStartStopError() throws Exception
    {
        HttpURLConnection httpConn;
        StringBuilder responseContent = new StringBuilder();
        StringBuilder responseCode = new StringBuilder();
        HashMap<String, String> headers = new HashMap<String, String>();
        
        // Start - Non admin
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repositories?action=start", HttpMethod.POST,
                _analystAuthToken);
    
        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check401UnauthorizedResponse(responseCode.toString(), headers);
        
        // Start - Non admin
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repositories?action=stop", HttpMethod.POST,
                _analystAuthToken);
    
        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check401UnauthorizedResponse(responseCode.toString(), headers);

        // Reload - Non admin
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repositories?action=reload", HttpMethod.POST,
                _analystAuthToken);
    
        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check401UnauthorizedResponse(responseCode.toString(), headers);
}
    
    /**
     * GET = 405 Method Not Allowed
     * 
     * @throws IOException
     */
    @Test
    public void testDELETE() throws IOException
    {
        // Create a URL for the desired page
        URL url = new URL("http://localhost:8989/api/repositories");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("DELETE");
        
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

        // _logger.debug(ApiUtils.formatResponseForLogging(responseCode, headers, content));

        assertEquals("HTTP/1.1 405 Method Not Allowed", responseCode);
        assertNotNull(headers.get("Date"));
        assertEquals("GET, POST", headers.get("Allow"));
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
        URL url = new URL("http://localhost:8989/api/repositories");
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

        // _logger.debug(ApiUtils.formatResponseForLogging(responseCode, headers, content));

        assertEquals("HTTP/1.1 405 Method Not Allowed", responseCode);
        assertNotNull(headers.get("Date"));
        assertEquals("GET, POST", headers.get("Allow"));
        assertNull(headers.get("Content-Type"));
        assertNull(content);
    }

}
