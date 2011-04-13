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
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.regex.Pattern;

import org.jboss.netty.handler.codec.http.HttpMethod;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.chililog.server.App;
import com.chililog.server.common.JsonTranslator;
import com.chililog.server.data.MongoConnection;
import com.chililog.server.data.RepositoryEntryBO;
import com.chililog.server.data.RepositoryEntryBO.Severity;
import com.chililog.server.data.RepositoryEntryController;
import com.chililog.server.data.RepositoryFieldInfoBO;
import com.chililog.server.data.RepositoryInfoBO;
import com.chililog.server.data.RepositoryParserInfoBO;
import com.chililog.server.data.RepositoryInfoBO.Status;
import com.chililog.server.data.RepositoryInfoController;
import com.chililog.server.data.RepositoryParserInfoBO.ParseFieldErrorHandling;
import com.chililog.server.data.UserBO;
import com.chililog.server.data.UserController;
import com.chililog.server.data.RepositoryParserInfoBO.AppliesTo;
import com.chililog.server.engine.parsers.DelimitedEntryParser;
import com.chililog.server.engine.parsers.EntryParser;
import com.chililog.server.engine.parsers.EntryParserFactory;
import com.chililog.server.ui.api.ErrorAO;
import com.chililog.server.ui.api.RepositoryAO;
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
    private static String _systemAdminAuthToken;
    private static String _repoAdminAuthToken;
    private static String _repoPowerUserAuthToken;
    private static String _repoStandardUserAuthToken;
    private static String _repoStandardUserNoAccessAuthToken;
    private static String _repoInfoId;

    @BeforeClass
    public static void classSetup() throws Exception
    {
        _db = MongoConnection.getInstance().getConnection();
        assertNotNull(_db);

        // Clean up old user test data if any exists
        DBCollection coll = _db.getCollection(UserController.MONGODB_COLLECTION_NAME);
        Pattern pattern = Pattern.compile("^TestRepo[\\w]*$");
        DBObject query = new BasicDBObject();
        query.put("username", pattern);
        coll.remove(query);

        // Clean up old repository test data if any exists
        coll = _db.getCollection(RepositoryInfoController.MONGODB_COLLECTION_NAME);
        pattern = Pattern.compile("^test_repo[\\w]*$");
        query = new BasicDBObject();
        query.put("name", pattern);
        coll.remove(query);

        // Create system admin user
        UserBO user = new UserBO();
        user.setUsername("TestRepo_SystemAdmin");
        user.setEmailAddress("TestRepo_SystemAdmin@chililog.com");
        user.setPassword("hello", true);
        user.addRole(UserBO.SYSTEM_ADMINISTRATOR_ROLE_NAME);
        user.setStatus(com.chililog.server.data.UserBO.Status.Enabled);
        UserController.getInstance().save(_db, user);

        // Create repository admin user
        user = new UserBO();
        user.setUsername("TestRepo_RepoAdmin");
        user.setPassword("hello", true);
        user.setEmailAddress("TestRepo_RepoAdmin@chililog.com");
        user.addRole("repo.test_repo.administrator");
        user.setStatus(com.chililog.server.data.UserBO.Status.Enabled);
        UserController.getInstance().save(_db, user);

        // Create repository power user
        user = new UserBO();
        user.setUsername("TestRepo_RepoPower");
        user.setPassword("hello", true);
        user.setEmailAddress("TestRepo_RepoPower@chililog.com");
        user.addRole("repo.test_repo.power");
        user.setStatus(com.chililog.server.data.UserBO.Status.Enabled);
        UserController.getInstance().save(_db, user);

        // Create repository standard user
        user = new UserBO();
        user.setUsername("TestRepo_RepoStandard");
        user.setPassword("hello", true);
        user.setEmailAddress("TestRepo_RepoStandard@chililog.com");
        user.addRole("repo.test_repo.standard");
        user.setStatus(com.chililog.server.data.UserBO.Status.Enabled);
        UserController.getInstance().save(_db, user);

        // Create repository standard user with no access
        user = new UserBO();
        user.setUsername("TestRepo_RepoStandard_NoAccess");
        user.setPassword("hello", true);
        user.setEmailAddress("TestRepo_RepoStandard_NoAccess@chililog.com");
        user.addRole("repo.chililog.standard");
        user.setStatus(com.chililog.server.data.UserBO.Status.Enabled);
        UserController.getInstance().save(_db, user);

        // Create test repo
        RepositoryInfoBO repoInfo = new RepositoryInfoBO();
        repoInfo.setName("test_repo");
        repoInfo.setDisplayName("RepositoriesTest 1");
        
        RepositoryParserInfoBO repoParserInfo = new RepositoryParserInfoBO();
        repoParserInfo.setName("parser1");
        repoParserInfo.setAppliesTo(AppliesTo.All);
        repoParserInfo.setClassName(DelimitedEntryParser.class.getName());
        repoParserInfo.setParseFieldErrorHandling(ParseFieldErrorHandling.SkipEntry);
        repoParserInfo.getProperties().put(DelimitedEntryParser.DELIMITER_PROPERTY_NAME, "|");
        repoInfo.getParsers().add(repoParserInfo);

        RepositoryFieldInfoBO repoFieldInfo = new RepositoryFieldInfoBO();
        repoFieldInfo.setName("field1");
        repoFieldInfo.setDataType(RepositoryFieldInfoBO.DataType.String);
        repoFieldInfo.getProperties().put(DelimitedEntryParser.POSITION_FIELD_PROPERTY_NAME, "1");
        repoParserInfo.getFields().add(repoFieldInfo);

        repoFieldInfo = new RepositoryFieldInfoBO();
        repoFieldInfo.setName("field2");
        repoFieldInfo.setDataType(RepositoryFieldInfoBO.DataType.Integer);
        repoFieldInfo.getProperties().put(DelimitedEntryParser.POSITION_FIELD_PROPERTY_NAME, "2");
        repoParserInfo.getFields().add(repoFieldInfo);

        RepositoryInfoController.getInstance().save(_db, repoInfo);
        _repoInfoId = repoInfo.getDocumentID().toString();

        coll = _db.getCollection(repoInfo.getMongoDBCollectionName());
        if (coll != null)
        {
            coll.drop();
        }

        // Add 3 lines
        RepositoryEntryController c = RepositoryEntryController.getInstance(repoInfo);
        EntryParser p = EntryParserFactory.getParser(repoInfo, repoInfo.getParsers().get(0));
        RepositoryEntryBO entry = p.parse("2011-01-01T05:05:05.100Z", "log1", "127.0.0.1", Severity.Information.toString(), "line1|1");
        c.save(_db, entry);

        entry = p.parse("2011-01-01T05:05:05.200Z","log1", "127.0.0.2", Severity.Error.toString(), "line2|2");
        c.save(_db, entry);

        entry = p.parse("2011-01-01T05:05:05.300Z","log1", "127.0.0.3", Severity.Emergency.toString(), "line3|3");
        c.save(_db, entry);

        // Start server
        App.startChiliLogServer();

        // Login
        _systemAdminAuthToken = ApiUtils.login("TestRepo_SystemAdmin", "hello");
        _repoAdminAuthToken = ApiUtils.login("TestRepo_RepoAdmin", "hello");
        _repoPowerUserAuthToken = ApiUtils.login("TestRepo_RepoPower", "hello");
        _repoStandardUserAuthToken = ApiUtils.login("TestRepo_RepoStandard", "hello");
        _repoStandardUserNoAccessAuthToken = ApiUtils.login("TestRepo_RepoStandard_NoAccess", "hello");
    }

    @AfterClass
    public static void classTeardown() throws Exception
    {
        // Clean up old user test data if any exists
        DBCollection coll = _db.getCollection(UserController.MONGODB_COLLECTION_NAME);
        Pattern pattern = Pattern.compile("^TestRepo[\\w]*$");
        DBObject query = new BasicDBObject();
        query.put("username", pattern);
        coll.remove(query);

        // Clean up old repository test data if any exists
        coll = _db.getCollection(RepositoryInfoController.MONGODB_COLLECTION_NAME);
        pattern = Pattern.compile("^test_repo[\\w]*$");
        query = new BasicDBObject();
        query.put("name", pattern);
        coll.remove(query);

        App.stopChiliLogServer();
        
        coll = _db.getCollection("test_repo_repository");
        if (coll != null)
        {
            coll.drop();
        }
    }

    /**
     * Test queries
     * 
     * @throws Exception
     */
    @Test
    public void testGetAll() throws Exception
    {
        HttpURLConnection httpConn;
        StringBuilder responseContent = new StringBuilder();
        StringBuilder responseCode = new StringBuilder();
        HashMap<String, String> headers = new HashMap<String, String>();

        // Get all - system admin
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repositories", HttpMethod.GET,
                _systemAdminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        RepositoryAO[] getListResponseAO = JsonTranslator.getInstance().fromJson(responseContent.toString(),
                RepositoryAO[].class);
        assertTrue(getListResponseAO.length > 0);
        String myRepoDocId = null;
        for (RepositoryAO r : getListResponseAO)
        {
            if (r.getName().equals("test_repo"))
            {
                myRepoDocId = r.getDocumentID();
            }
        }
        assertNotNull(myRepoDocId);

        // Get all - repo admin user. Should only get 1 repo back because we only have access to 1 repo
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repositories", HttpMethod.GET,
                _repoAdminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        getListResponseAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), RepositoryAO[].class);
        assertEquals(1, getListResponseAO.length);
        assertEquals("test_repo", getListResponseAO[0].getName());

        // Get all - repo power user. Should only get 1 repo back because we only have access to 1 repo
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repositories", HttpMethod.GET,
                _repoPowerUserAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        getListResponseAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), RepositoryAO[].class);
        assertEquals(1, getListResponseAO.length);
        assertEquals("test_repo", getListResponseAO[0].getName());

        // Get all - repo standard user. Should only get 1 repo back because we only have access to 1 repo
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repositories", HttpMethod.GET,
                _repoStandardUserAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        getListResponseAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), RepositoryAO[].class);
        assertEquals(1, getListResponseAO.length);
        assertEquals("test_repo", getListResponseAO[0].getName());
    }

    /**
     * Test queries
     * 
     * @throws Exception
     */
    @Test
    public void testGetOne() throws Exception
    {
        HttpURLConnection httpConn;
        StringBuilder responseContent = new StringBuilder();
        StringBuilder responseCode = new StringBuilder();
        HashMap<String, String> headers = new HashMap<String, String>();

        // Get 1 - system admin
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repositories/" + _repoInfoId,
                HttpMethod.GET, _systemAdminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        RepositoryAO readResponseAO = JsonTranslator.getInstance().fromJson(responseContent.toString(),
                RepositoryAO.class);
        assertEquals(_repoInfoId, readResponseAO.getDocumentID());
        assertEquals("test_repo", readResponseAO.getName());
        assertEquals(Status.ONLINE, readResponseAO.getStatus());

        // Get 1 - repo admin        
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repositories/" + _repoInfoId,
                HttpMethod.GET, _repoAdminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        // Get 1 - repo power        
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repositories/" + _repoInfoId,
                HttpMethod.GET, _repoPowerUserAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        // Get 1 - repo standard        
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repositories/" + _repoInfoId,
                HttpMethod.GET, _repoStandardUserAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        // Get 1 - repo standard with no access. ERROR.       
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repositories/" + _repoInfoId,
                HttpMethod.GET, _repoStandardUserNoAccessAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check400BadRequestResponse(responseCode.toString(), headers);

        ErrorAO errorAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), ErrorAO.class);
        assertEquals("ChiliLogException:UI.RepositoryNotFoundError", errorAO.getErrorCode());

        // Get 1 - repository not found
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repositories/123", HttpMethod.GET,
                _repoAdminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check400BadRequestResponse(responseCode.toString(), headers);

        errorAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), ErrorAO.class);
        assertEquals("ChiliLogException:UI.RepositoryNotFoundError", errorAO.getErrorCode());
    }

    /**
     * Test queries
     * 
     * @throws Exception
     */
    @Test
    public void testGetEntries() throws Exception
    {
        HttpURLConnection httpConn;
        StringBuilder responseContent = new StringBuilder();
        StringBuilder responseCode = new StringBuilder();
        HashMap<String, String> headers = new HashMap<String, String>();

        // Get entries - system admin
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repositories/" + _repoInfoId
                + "/entries?query_type=find", HttpMethod.GET, _systemAdminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        String json = responseContent.toString();
        assertTrue(json.contains("\"field1\" : \"line1\" , \"field2\" : 1"));
        assertTrue(json.contains("\"field1\" : \"line2\" , \"field2\" : 2"));
        assertTrue(json.contains("\"field1\" : \"line3\" , \"field2\" : 3"));

        // Get entries - by repo admin user
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repositories/" + _repoInfoId
                + "/entries?query_type=find", HttpMethod.GET, _repoAdminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        String json2 = responseContent.toString();
        assertEquals(json, json2);
        assertTrue(json2.contains("\"field1\" : \"line1\" , \"field2\" : 1"));
        assertTrue(json2.contains("\"field1\" : \"line2\" , \"field2\" : 2"));
        assertTrue(json2.contains("\"field1\" : \"line3\" , \"field2\" : 3"));
        
        // Get entries - by repo power user
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repositories/" + _repoInfoId
                + "/entries?query_type=find", HttpMethod.GET, _repoPowerUserAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        // Get entries - by repo standard user
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repositories/" + _repoInfoId
                + "/entries?query_type=find", HttpMethod.GET, _repoStandardUserAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        // Get entries - repo standard with no access. ERROR.       
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repositories/" + _repoInfoId
                + "/entries?query_type=find", HttpMethod.GET, _repoStandardUserNoAccessAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check400BadRequestResponse(responseCode.toString(), headers);

        ErrorAO errorAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), ErrorAO.class);
        assertEquals("ChiliLogException:UI.RepositoryNotFoundError", errorAO.getErrorCode());
        
        // Get entries - repository not found
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repositories/123/entries", HttpMethod.GET,
                _repoAdminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check400BadRequestResponse(responseCode.toString(), headers);

        errorAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), ErrorAO.class);
        assertEquals("ChiliLogException:UI.RepositoryNotFoundError", errorAO.getErrorCode());
    }

    
    /**
     * Get entries
     * 
     * @throws Exception
     */
    @Test
    public void testGetEntriesWithCriteria() throws Exception
    {
        HttpURLConnection httpConn;
        StringBuilder responseContent = new StringBuilder();
        StringBuilder responseCode = new StringBuilder();
        HashMap<String, String> headers = new HashMap<String, String>();

        // Find
        String conditions = URLEncoder.encode("{ \"field1\" : \"line1\" }", "UTF-8");

        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repositories/" + _repoInfoId
                + "/entries?query_type=find&conditions=" + conditions, HttpMethod.GET, _systemAdminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        String json = responseContent.toString();
        assertTrue(json.contains("\"field1\" : \"line1\" , \"field2\" : 1"));
        assertFalse(json.contains("\"field1\" : \"line2\" , \"field2\" : 2"));
        assertFalse(json.contains("\"field1\" : \"line3\" , \"field2\" : 3"));

        // Count
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repositories/" + _repoInfoId
                + "/entries?query_type=count&conditions=" + conditions, HttpMethod.GET, _systemAdminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        json = responseContent.toString();
        assertTrue(json.contains("{ \"count\" : 1}"));

        // Distinct
        String fields = URLEncoder.encode("{ \"field1\" : 1 }", "UTF-8");

        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repositories/" + _repoInfoId
                + "/entries?query_type=distinct&fields=" + fields, HttpMethod.GET, _systemAdminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        json = responseContent.toString();
        assertTrue(json.contains("{ \"distinct\" : [ \"line1\" , \"line2\" , \"line3\"]}"));
    }

    /**
     * Test start and stop repositories
     * 
     * @throws Exception
     */
    @Test
    public void testStartStopSystemAdmin() throws Exception
    {
        HttpURLConnection httpConn;
        StringBuilder responseContent = new StringBuilder();
        StringBuilder responseCode = new StringBuilder();
        HashMap<String, String> headers = new HashMap<String, String>();

        // Start all where repositories have already started - should not error
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repositories?action=start",
                HttpMethod.POST, _systemAdminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check204NoContentResponse(responseCode.toString(), headers);

        // Stop all
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repositories?action=stop", HttpMethod.POST,
                _systemAdminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check204NoContentResponse(responseCode.toString(), headers);

        // GET all - check that all repositories have stopped
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repositories", HttpMethod.GET,
                _systemAdminAuthToken);

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
                _systemAdminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check204NoContentResponse(responseCode.toString(), headers);

        // Start all
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repositories?action=start",
                HttpMethod.POST, _systemAdminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check204NoContentResponse(responseCode.toString(), headers);

        // GET all - check that all repositories have started
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repositories", HttpMethod.GET,
                _systemAdminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        getListResponseAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), RepositoryAO[].class);
        for (RepositoryAO r : getListResponseAO)
        {
            assertEquals(Status.ONLINE, r.getStatus());
        }

        // Stop 1
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repositories/" + _repoInfoId + "?action=stop",
                HttpMethod.POST, _systemAdminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check204NoContentResponse(responseCode.toString(), headers);

        // GET all - check that only our repository have stopped
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repositories", HttpMethod.GET,
                _systemAdminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        getListResponseAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), RepositoryAO[].class);
        for (RepositoryAO r : getListResponseAO)
        {
            if (r.getDocumentID().equals(_repoInfoId))
            {
                assertEquals(Status.OFFLINE, r.getStatus());
            }
            else
            {
                assertEquals(Status.ONLINE, r.getStatus());
            }
        }

        // Start 1
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repositories/" + _repoInfoId + "?action=start",
                HttpMethod.POST, _systemAdminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check204NoContentResponse(responseCode.toString(), headers);

        // GET all - check that all repositories have started
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repositories", HttpMethod.GET,
                _systemAdminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        getListResponseAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), RepositoryAO[].class);
        for (RepositoryAO r : getListResponseAO)
        {
            assertEquals(Status.ONLINE, r.getStatus());
        }

        // Reload
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repositories?action=reload",
                HttpMethod.POST, _systemAdminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check204NoContentResponse(responseCode.toString(), headers);
    }

    /**
     * Test start and stop repositories
     * 
     * @throws Exception
     */
    @Test
    public void testStartStopRepoAdmin() throws Exception
    {
        HttpURLConnection httpConn;
        StringBuilder responseContent = new StringBuilder();
        StringBuilder responseCode = new StringBuilder();
        HashMap<String, String> headers = new HashMap<String, String>();

        // Start all where repositories have already started - should not error
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repositories?action=start",
                HttpMethod.POST, _systemAdminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check204NoContentResponse(responseCode.toString(), headers);

        // GET all - check that all repositories have started
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repositories", HttpMethod.GET,
                _systemAdminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        RepositoryAO[] getListResponseAO = JsonTranslator.getInstance().fromJson(responseContent.toString(),
                RepositoryAO[].class);
        for (RepositoryAO r : getListResponseAO)
        {
            assertEquals(Status.ONLINE, r.getStatus());
        }

        // Stop 1
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repositories/" + _repoInfoId + "?action=stop",
                HttpMethod.POST, _repoAdminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check204NoContentResponse(responseCode.toString(), headers);

        // GET all - check that only our repository have stopped
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repositories", HttpMethod.GET,
                _systemAdminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        getListResponseAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), RepositoryAO[].class);
        for (RepositoryAO r : getListResponseAO)
        {
            if (r.getDocumentID().equals(_repoInfoId))
            {
                assertEquals(Status.OFFLINE, r.getStatus());
            }
            else
            {
                assertEquals(Status.ONLINE, r.getStatus());
            }
        }

        // Start 1
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repositories/" + _repoInfoId + "?action=start",
                HttpMethod.POST, _repoAdminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check204NoContentResponse(responseCode.toString(), headers);

        // GET all - check that all repositories have started
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repositories", HttpMethod.GET,
                _systemAdminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        getListResponseAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), RepositoryAO[].class);
        for (RepositoryAO r : getListResponseAO)
        {
            assertEquals(Status.ONLINE, r.getStatus());
        }

    }

    /**
     * Test access restrictions and other errors
     * 
     * @throws Exception
     */
    @Test
    public void testStartStopReloadAllError() throws Exception
    {
        HttpURLConnection httpConn;
        StringBuilder responseContent = new StringBuilder();
        StringBuilder responseCode = new StringBuilder();
        HashMap<String, String> headers = new HashMap<String, String>();

        // Start All - Repo Admin user
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repositories?action=start",
                HttpMethod.POST, _repoAdminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check401UnauthorizedResponse(responseCode.toString(), headers);

        // Start All - Power user
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repositories?action=start",
                HttpMethod.POST, _repoPowerUserAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check401UnauthorizedResponse(responseCode.toString(), headers);
        
        // Start All - Standard user
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repositories?action=start",
                HttpMethod.POST, _repoStandardUserAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check401UnauthorizedResponse(responseCode.toString(), headers);

        // Start All - Standard user with no access
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repositories?action=start",
                HttpMethod.POST, _repoStandardUserNoAccessAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check401UnauthorizedResponse(responseCode.toString(), headers);

        // Stop All - Repo Admin user
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repositories?action=stop",
                HttpMethod.POST, _repoAdminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check401UnauthorizedResponse(responseCode.toString(), headers);

        // Stop All - Power user
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repositories?action=stop", HttpMethod.POST,
                _repoPowerUserAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check401UnauthorizedResponse(responseCode.toString(), headers);

        // Stop All - Standard user
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repositories?action=stop", HttpMethod.POST,
                _repoStandardUserAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check401UnauthorizedResponse(responseCode.toString(), headers);

        // Stop All - Standard user with no access
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repositories?action=stop",
                HttpMethod.POST, _repoStandardUserNoAccessAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check401UnauthorizedResponse(responseCode.toString(), headers);

        // Reload All - Repo Admin user
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repositories?action=reload",
                HttpMethod.POST, _repoAdminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check401UnauthorizedResponse(responseCode.toString(), headers);

        // Reload All - Power user
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repositories?action=reload",
                HttpMethod.POST, _repoPowerUserAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check401UnauthorizedResponse(responseCode.toString(), headers);

        // Reload All - Standard user
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repositories?action=reload",
                HttpMethod.POST, _repoStandardUserAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check401UnauthorizedResponse(responseCode.toString(), headers);

        // Reload All - Standard user with no access
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repositories?action=reload",
                HttpMethod.POST, _repoStandardUserNoAccessAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check401UnauthorizedResponse(responseCode.toString(), headers);

    }

    /**
     * DELETE = 405 Method Not Allowed
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
