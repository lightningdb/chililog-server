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
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.regex.Pattern;

import org.chililog.server.App;
import org.chililog.server.common.JsonTranslator;
import org.chililog.server.data.MongoConnection;
import org.chililog.server.data.RepositoryEntryBO;
import org.chililog.server.data.RepositoryEntryController;
import org.chililog.server.data.RepositoryFieldInfoBO;
import org.chililog.server.data.RepositoryInfoBO;
import org.chililog.server.data.RepositoryInfoController;
import org.chililog.server.data.RepositoryParserInfoBO;
import org.chililog.server.data.UserBO;
import org.chililog.server.data.UserController;
import org.chililog.server.data.RepositoryEntryBO.Severity;
import org.chililog.server.data.RepositoryInfoBO.Status;
import org.chililog.server.data.RepositoryParserInfoBO.AppliesTo;
import org.chililog.server.data.RepositoryParserInfoBO.ParseFieldErrorHandling;
import org.chililog.server.engine.parsers.DelimitedEntryParser;
import org.chililog.server.engine.parsers.EntryParser;
import org.chililog.server.engine.parsers.EntryParserFactory;
import org.chililog.server.workbench.workers.ErrorAO;
import org.chililog.server.workbench.workers.RepositoryStatusAO;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

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
public class RepositoryRuntimeTest
{
    private static DB _db;
    private static String _systemAdminAuthToken;
    private static String _repoAdminAuthToken;
    private static String _repoWorkbenchUserAuthToken;
    private static String _repoWorkbenchUserNoAccessAuthToken;
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
        user.setStatus(org.chililog.server.data.UserBO.Status.Enabled);
        UserController.getInstance().save(_db, user);

        // Create repository admin user
        user = new UserBO();
        user.setUsername("TestRepo_RepoAdmin");
        user.setPassword("hello", true);
        user.setEmailAddress("TestRepo_RepoAdmin@chililog.com");
        user.addRole("repo.test_repo.administrator");
        user.setStatus(org.chililog.server.data.UserBO.Status.Enabled);
        UserController.getInstance().save(_db, user);

        // Create repository workbench user
        user = new UserBO();
        user.setUsername("TestRepo_RepoWorkbench");
        user.setPassword("hello", true);
        user.setEmailAddress("TestRepo_RepoWorkbench@chililog.com");
        user.addRole("repo.test_repo.workbench");
        user.setStatus(org.chililog.server.data.UserBO.Status.Enabled);
        UserController.getInstance().save(_db, user);

        // Create repository workbench user with no access to test_repo
        user = new UserBO();
        user.setUsername("TestRepo_RepoWorkbench_NoAccess");
        user.setPassword("hello", true);
        user.setEmailAddress("TestRepo_RepoWorkbench_NoAccess@chililog.com");
        user.addRole("repo.chililog.workbench");
        user.setStatus(org.chililog.server.data.UserBO.Status.Enabled);
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
        App.start(null);

        // Login
        _systemAdminAuthToken = ApiUtils.login("TestRepo_SystemAdmin", "hello");
        _repoAdminAuthToken = ApiUtils.login("TestRepo_RepoAdmin", "hello");
        _repoWorkbenchUserAuthToken = ApiUtils.login("TestRepo_RepoWorkbench", "hello");
        _repoWorkbenchUserNoAccessAuthToken = ApiUtils.login("TestRepo_RepoWorkbench_NoAccess", "hello");
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

        App.stop(null);
        
        coll = _db.getCollection("repo_test_repo");
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
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repository_runtime", HttpMethod.GET,
                _systemAdminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        RepositoryStatusAO[] getListResponseAO = JsonTranslator.getInstance().fromJson(responseContent.toString(),
                RepositoryStatusAO[].class);
        assertTrue(getListResponseAO.length > 0);
        String myRepoDocId = null;
        for (RepositoryStatusAO r : getListResponseAO)
        {
            if (r.getName().equals("test_repo"))
            {
                myRepoDocId = r.getDocumentID();
            }
        }
        assertNotNull(myRepoDocId);

        // Get all - repo admin user. Should only get 1 repo back because we only have access to 1 repo
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repository_runtime", HttpMethod.GET,
                _repoAdminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        getListResponseAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), RepositoryStatusAO[].class);
        assertEquals(1, getListResponseAO.length);
        assertEquals("test_repo", getListResponseAO[0].getName());

        // Get all - repo workbench user. Should only get 1 repo back because we only have access to 1 repo
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repository_runtime", HttpMethod.GET,
                _repoWorkbenchUserAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        getListResponseAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), RepositoryStatusAO[].class);
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
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repository_runtime/" + _repoInfoId,
                HttpMethod.GET, _systemAdminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        RepositoryStatusAO readResponseAO = JsonTranslator.getInstance().fromJson(responseContent.toString(),
                RepositoryStatusAO.class);
        assertEquals(_repoInfoId, readResponseAO.getDocumentID());
        assertEquals("test_repo", readResponseAO.getName());
        assertEquals(Status.ONLINE, readResponseAO.getStatus());

        // Get 1 - repo admin        
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repository_runtime/" + _repoInfoId,
                HttpMethod.GET, _repoAdminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        // Get 1 - repo workbench        
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repository_runtime/" + _repoInfoId,
                HttpMethod.GET, _repoWorkbenchUserAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        // Get 1 - repo workbench with no access. ERROR.       
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repository_runtime/" + _repoInfoId,
                HttpMethod.GET, _repoWorkbenchUserNoAccessAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check400BadRequestResponse(responseCode.toString(), headers);

        ErrorAO errorAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), ErrorAO.class);
        assertEquals("ChiliLogException:Workbench.RepositoryNotFoundError", errorAO.getErrorCode());

        // Get 1 - repository not found
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repository_runtime/123", HttpMethod.GET,
                _repoAdminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check400BadRequestResponse(responseCode.toString(), headers);

        errorAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), ErrorAO.class);
        assertEquals("ChiliLogException:Workbench.RepositoryNotFoundError", errorAO.getErrorCode());
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
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repository_runtime/" + _repoInfoId
                + "/entries?query_type=find", HttpMethod.GET, _systemAdminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        String json = responseContent.toString();
        assertTrue(json.contains("\"fld_field1\" : \"line1\" , \"fld_field2\" : 1"));
        assertTrue(json.contains("\"fld_field1\" : \"line2\" , \"fld_field2\" : 2"));
        assertTrue(json.contains("\"fld_field1\" : \"line3\" , \"fld_field2\" : 3"));

        // Get entries - by repo admin user
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repository_runtime/" + _repoInfoId
                + "/entries?query_type=find", HttpMethod.GET, _repoAdminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        String json2 = responseContent.toString();
        assertEquals(json, json2);
        assertTrue(json2.contains("\"fld_field1\" : \"line1\" , \"fld_field2\" : 1"));
        assertTrue(json2.contains("\"fld_field1\" : \"line2\" , \"fld_field2\" : 2"));
        assertTrue(json2.contains("\"fld_field1\" : \"line3\" , \"fld_field2\" : 3"));
        
        // Get entries - by workbench power user
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repository_runtime/" + _repoInfoId
                + "/entries?query_type=find", HttpMethod.GET, _repoWorkbenchUserAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        // Get entries - repo workbench with no access. ERROR.       
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repository_runtime/" + _repoInfoId
                + "/entries?query_type=find", HttpMethod.GET, _repoWorkbenchUserNoAccessAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check400BadRequestResponse(responseCode.toString(), headers);

        ErrorAO errorAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), ErrorAO.class);
        assertEquals("ChiliLogException:Workbench.RepositoryNotFoundError", errorAO.getErrorCode());
        
        // Get entries - repository not found
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repository_runtime/123/entries", HttpMethod.GET,
                _repoAdminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check400BadRequestResponse(responseCode.toString(), headers);

        errorAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), ErrorAO.class);
        assertEquals("ChiliLogException:Workbench.RepositoryNotFoundError", errorAO.getErrorCode());
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
        String conditions = URLEncoder.encode("{ \"fld_field1\" : \"line1\", \"ts\" : { \"$gte\" : \"2011-01-01T05:05:05.000Z\" } }", "UTF-8");

        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repository_runtime/" + _repoInfoId
                + "/entries?query_type=find&conditions=" + conditions, HttpMethod.GET, _systemAdminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        String json = responseContent.toString();
        assertTrue(json.contains("\"fld_field1\" : \"line1\" , \"fld_field2\" : 1"));
        assertFalse(json.contains("\"fld_field1\" : \"line2\" , \"fld_field2\" : 2"));
        assertFalse(json.contains("\"fld_field1\" : \"line3\" , \"fld_field2\" : 3"));

        // Count
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repository_runtime/" + _repoInfoId
                + "/entries?query_type=count&conditions=" + conditions, HttpMethod.GET, _systemAdminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        json = responseContent.toString();
        assertTrue(json.contains("{ \"count\" : 1}"));

        // Distinct
        String fields = URLEncoder.encode("{ \"fld_field1\" : 1 }", "UTF-8");

        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repository_runtime/" + _repoInfoId
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
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repository_runtime?action=start",
                HttpMethod.POST, _systemAdminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        // Stop all
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repository_runtime?action=stop", HttpMethod.POST,
                _systemAdminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        // GET all - check that all repositories have stopped
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repository_runtime", HttpMethod.GET,
                _systemAdminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        RepositoryStatusAO[] getListResponseAO = JsonTranslator.getInstance().fromJson(responseContent.toString(),
                RepositoryStatusAO[].class);
        for (RepositoryStatusAO r : getListResponseAO)
        {
            assertEquals(Status.OFFLINE, r.getStatus());
        }

        // Stop all again - should not get error
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repository_runtime?action=stop", HttpMethod.POST,
                _systemAdminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        // Start all
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repository_runtime?action=start",
                HttpMethod.POST, _systemAdminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        // GET all - check that all repositories have started
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repository_runtime", HttpMethod.GET,
                _systemAdminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        getListResponseAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), RepositoryStatusAO[].class);
        for (RepositoryStatusAO r : getListResponseAO)
        {
            assertEquals(Status.ONLINE, r.getStatus());
        }

        // Stop 1
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repository_runtime/" + _repoInfoId + "?action=stop",
                HttpMethod.POST, _systemAdminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        // GET all - check that only our repository have stopped
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repository_runtime", HttpMethod.GET,
                _systemAdminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        getListResponseAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), RepositoryStatusAO[].class);
        for (RepositoryStatusAO r : getListResponseAO)
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
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repository_runtime/" + _repoInfoId + "?action=start",
                HttpMethod.POST, _systemAdminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        // GET all - check that all repositories have started
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repository_runtime", HttpMethod.GET,
                _systemAdminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        getListResponseAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), RepositoryStatusAO[].class);
        for (RepositoryStatusAO r : getListResponseAO)
        {
            assertEquals(Status.ONLINE, r.getStatus());
        }

        // Reload
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repository_runtime?action=reload",
                HttpMethod.POST, _systemAdminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);
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
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repository_runtime?action=start",
                HttpMethod.POST, _systemAdminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        RepositoryStatusAO[] getListResponseAO = JsonTranslator.getInstance().fromJson(responseContent.toString(),
                RepositoryStatusAO[].class);
        for (RepositoryStatusAO r : getListResponseAO)
        {
            assertEquals(Status.ONLINE, r.getStatus());
        }
        
        // GET all - check that all repositories have started
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repository_runtime", HttpMethod.GET,
                _systemAdminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        getListResponseAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), RepositoryStatusAO[].class);
        for (RepositoryStatusAO r : getListResponseAO)
        {
            assertEquals(Status.ONLINE, r.getStatus());
        }

        // Stop 1
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repository_runtime/" + _repoInfoId + "?action=stop",
                HttpMethod.POST, _repoAdminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);
        
        RepositoryStatusAO repoAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), RepositoryStatusAO.class);
        assertEquals(_repoInfoId, repoAO.getDocumentID());
        assertEquals(Status.OFFLINE, repoAO.getStatus());

        // GET all - check that only our repository have stopped
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repository_runtime", HttpMethod.GET,
                _systemAdminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        getListResponseAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), RepositoryStatusAO[].class);
        for (RepositoryStatusAO r : getListResponseAO)
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
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repository_runtime/" + _repoInfoId + "?action=start",
                HttpMethod.POST, _repoAdminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        repoAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), RepositoryStatusAO.class);
        assertEquals(_repoInfoId, repoAO.getDocumentID());
        assertEquals(Status.ONLINE, repoAO.getStatus());
        
        // GET all - check that all repositories have started
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repository_runtime", HttpMethod.GET,
                _systemAdminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        getListResponseAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), RepositoryStatusAO[].class);
        for (RepositoryStatusAO r : getListResponseAO)
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
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repository_runtime?action=start",
                HttpMethod.POST, _repoAdminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check401UnauthorizedResponse(responseCode.toString(), headers);

        // Start All - Workbench user
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repository_runtime?action=start",
                HttpMethod.POST, _repoWorkbenchUserAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check401UnauthorizedResponse(responseCode.toString(), headers);
        
        // Start All - Workbench user with no access
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repository_runtime?action=start",
                HttpMethod.POST, _repoWorkbenchUserNoAccessAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check401UnauthorizedResponse(responseCode.toString(), headers);

        // Stop All - Repo Admin user
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repository_runtime?action=stop",
                HttpMethod.POST, _repoAdminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check401UnauthorizedResponse(responseCode.toString(), headers);

        // Stop All - Workbench user
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repository_runtime?action=stop", HttpMethod.POST,
                _repoWorkbenchUserAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check401UnauthorizedResponse(responseCode.toString(), headers);

        // Stop All - Workbench user with no access
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repository_runtime?action=stop",
                HttpMethod.POST, _repoWorkbenchUserNoAccessAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check401UnauthorizedResponse(responseCode.toString(), headers);

        // Reload All - Repo Admin user
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repository_runtime?action=reload",
                HttpMethod.POST, _repoAdminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check401UnauthorizedResponse(responseCode.toString(), headers);

        // Reload All - Workbench user
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repository_runtime?action=reload",
                HttpMethod.POST, _repoWorkbenchUserAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check401UnauthorizedResponse(responseCode.toString(), headers);

        // Reload All - Workbench user with no access
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repository_runtime?action=reload",
                HttpMethod.POST, _repoWorkbenchUserNoAccessAuthToken);

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
        URL url = new URL("http://localhost:8989/api/repository_runtime");
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
        URL url = new URL("http://localhost:8989/api/repository_runtime");
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
