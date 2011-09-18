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

import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.regex.Pattern;

import org.chililog.server.common.JsonTranslator;
import org.chililog.server.data.MongoConnection;
import org.chililog.server.data.RepositoryFieldConfigBO;
import org.chililog.server.data.RepositoryConfigBO;
import org.chililog.server.data.RepositoryConfigController;
import org.chililog.server.data.RepositoryParserConfigBO;
import org.chililog.server.data.UserBO;
import org.chililog.server.data.UserController;
import org.chililog.server.data.RepositoryConfigBO.MaxMemoryPolicy;
import org.chililog.server.data.RepositoryConfigBO.Status;
import org.chililog.server.data.RepositoryParserConfigBO.AppliesTo;
import org.chililog.server.data.RepositoryParserConfigBO.ParseFieldErrorHandling;
import org.chililog.server.engine.parsers.DelimitedEntryParser;
import org.chililog.server.workbench.WorkbenchService;
import org.chililog.server.workbench.workers.ErrorAO;
import org.chililog.server.workbench.workers.RepositoryFieldConfigAO;
import org.chililog.server.workbench.workers.RepositoryConfigAO;
import org.chililog.server.workbench.workers.RepositoryParserConfigAO;
import org.chililog.server.workbench.workers.RepositoryPropertyConfigAO;
import org.chililog.server.workbench.workers.UserAO;
import org.chililog.server.workbench.workers.Worker;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

/**
 * Test the Repository Info API
 * 
 * @author vibul
 * 
 */
public class RepositoryConfigTest
{
    private static DB _db;
    private static String _systemAdminAuthToken;
    private static String _repoAdminAuthToken;
    private static String _repoWorkbenchUserAuthToken;

    @BeforeClass
    public static void classSetup() throws Exception
    {
        _db = MongoConnection.getInstance().getConnection();
        assertNotNull(_db);

        // Clean up old user test data if any exists
        DBCollection coll = _db.getCollection(UserController.MONGODB_COLLECTION_NAME);
        Pattern pattern = Pattern.compile("^TestRepoInfo[\\w]*$");
        DBObject query = new BasicDBObject();
        query.put("username", pattern);
        coll.remove(query);

        // Clean up old repository test data if any exists
        coll = _db.getCollection(RepositoryConfigController.MONGODB_COLLECTION_NAME);
        pattern = Pattern.compile("^test_repoinfo[\\w]*$");
        query = new BasicDBObject();
        query.put("name", pattern);
        coll.remove(query);

        // Create system admin user
        UserBO user = new UserBO();
        user.setUsername("TestRepoInfo_SystemAdmin");
        user.setEmailAddress("TestRepoInfo_SystemAdmin@chililog.com");
        user.setPassword("hello", true);
        user.addRole(UserBO.SYSTEM_ADMINISTRATOR_ROLE_NAME);
        user.setStatus(org.chililog.server.data.UserBO.Status.ENABLED);
        UserController.getInstance().save(_db, user);

        // Create repository admin user
        user = new UserBO();
        user.setUsername("TestRepoInfo_RepoAdmin");
        user.setPassword("hello", true);
        user.setEmailAddress("TestRepoInfo_RepoAdmin@chililog.com");
        user.addRole("repo.test_repoinfo_common.administrator");
        user.setStatus(org.chililog.server.data.UserBO.Status.ENABLED);
        UserController.getInstance().save(_db, user);

        // Create repository workbench user
        user = new UserBO();
        user.setUsername("TestRepoInfo_RepoWorkbench");
        user.setPassword("hello", true);
        user.setEmailAddress("TestRepoInfo_RepoWorkbench@chililog.com");
        user.addRole("repo.test_repoinfo_common.workbench");
        user.setStatus(org.chililog.server.data.UserBO.Status.ENABLED);
        UserController.getInstance().save(_db, user);

        // Create test repo
        RepositoryConfigBO repoInfo = new RepositoryConfigBO();
        repoInfo.setName("test_repoinfo_common");
        repoInfo.setDisplayName("Test 1");
        repoInfo.setDescription("description");
        repoInfo.setStoreEntriesIndicator(true);
        repoInfo.setStorageQueueDurableIndicator(true);
        repoInfo.setStorageQueueWorkerCount(10);
        repoInfo.setStorageMaxKeywords(100);
        repoInfo.setMaxMemory(2);
        repoInfo.setMaxMemoryPolicy(MaxMemoryPolicy.BLOCK);
        repoInfo.setPageSize(1);

        RepositoryParserConfigBO repoParserInfo = new RepositoryParserConfigBO();
        repoParserInfo.setName("parser1");
        repoParserInfo.setAppliesTo(AppliesTo.All);
        repoParserInfo.setClassName(DelimitedEntryParser.class.getName());
        repoParserInfo.setMaxKeywords(101);
        repoParserInfo.setParseFieldErrorHandling(ParseFieldErrorHandling.SkipEntry);
        repoParserInfo.getProperties().put(DelimitedEntryParser.DELIMITER_PROPERTY_NAME, "|");
        repoInfo.getParsers().add(repoParserInfo);

        RepositoryFieldConfigBO repoFieldInfo = new RepositoryFieldConfigBO();
        repoFieldInfo.setName("field1");
        repoFieldInfo.setDataType(RepositoryFieldConfigBO.DataType.String);
        repoFieldInfo.getProperties().put(DelimitedEntryParser.POSITION_FIELD_PROPERTY_NAME, "1");
        repoParserInfo.getFields().add(repoFieldInfo);

        RepositoryConfigController.getInstance().save(_db, repoInfo);

        // Start web server
        WorkbenchService.getInstance().start();

        // Login
        _systemAdminAuthToken = ApiUtils.login("TestRepoInfo_SystemAdmin", "hello");
        _repoAdminAuthToken = ApiUtils.login("TestRepoInfo_RepoAdmin", "hello");
        _repoWorkbenchUserAuthToken = ApiUtils.login("TestRepoInfo_RepoWorkbench", "hello");
    }

    @AfterClass
    public static void classTeardown()
    {
        // Clean up old user test data if any exists
        DBCollection coll = _db.getCollection(UserController.MONGODB_COLLECTION_NAME);
        Pattern pattern = Pattern.compile("^TestRepoInfo[\\w]*$");
        DBObject query = new BasicDBObject();
        query.put("username", pattern);
        coll.remove(query);

        // Clean up old repository test data if any exists
        coll = _db.getCollection(RepositoryConfigController.MONGODB_COLLECTION_NAME);
        pattern = Pattern.compile("^test_repoinfo[\\w]*$");
        query = new BasicDBObject();
        query.put("name", pattern);
        coll.remove(query);
        
        WorkbenchService.getInstance().stop();
    }

    /**
     * Create, Get, Update, Delete
     * 
     * @throws Exception
     */
    @Test
    public void testCRUD() throws Exception
    {
        HttpURLConnection httpConn;
        StringBuilder responseContent = new StringBuilder();
        StringBuilder responseCode = new StringBuilder();
        HashMap<String, String> headers = new HashMap<String, String>();

        // Create
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repository_config", HttpMethod.POST,
                _systemAdminAuthToken);

        RepositoryFieldConfigAO f1 = new RepositoryFieldConfigAO();
        f1.setName("field1");
        f1.setDataType(RepositoryFieldConfigBO.DataType.String);
        f1.setProperties(new RepositoryPropertyConfigAO[]
        { new RepositoryPropertyConfigAO("F1", "F2"), new RepositoryPropertyConfigAO("F3", "F4") });

        RepositoryFieldConfigAO f2 = new RepositoryFieldConfigAO();
        f2.setName("field2");
        f2.setDataType(RepositoryFieldConfigBO.DataType.Integer);
        f2.setProperties(new RepositoryPropertyConfigAO[]
        { new RepositoryPropertyConfigAO("F5", "F6"), new RepositoryPropertyConfigAO("F7", "F8") });

        RepositoryConfigAO createRepoInfoAO = new RepositoryConfigAO();
        createRepoInfoAO.setName("test_repoinfo_1");
        createRepoInfoAO.setDisplayName("Repository Test 1");
        createRepoInfoAO.setDescription("description");
        createRepoInfoAO.setStartupStatus(Status.ONLINE);
        createRepoInfoAO.setStoreEntriesIndicator(true);
        createRepoInfoAO.setStorageQueueDurableIndicator(true);
        createRepoInfoAO.setStorageQueueWorkerCount(2);
        createRepoInfoAO.setStorageMaxKeywords(100);
        createRepoInfoAO.setMaxMemory(10);
        createRepoInfoAO.setMaxMemoryPolicy(MaxMemoryPolicy.BLOCK);
        createRepoInfoAO.setPageSize(2);

        RepositoryParserConfigAO createRepoParserInfo = new RepositoryParserConfigAO();
        createRepoParserInfo.setName("parser1");
        createRepoParserInfo.setAppliesTo(AppliesTo.All);
        createRepoParserInfo.setAppliesToSourceFilter("sss");
        createRepoParserInfo.setAppliesToHostFilter("hhh");
        createRepoParserInfo.setClassName(DelimitedEntryParser.class.getName());
        createRepoParserInfo.setMaxKeywords(101);
        createRepoParserInfo.setParseFieldErrorHandling(ParseFieldErrorHandling.SkipEntry);
        createRepoInfoAO.setParsers(new RepositoryParserConfigAO[]
        { createRepoParserInfo });

        createRepoParserInfo.setFields(new RepositoryFieldConfigAO[]
        { f1, f2 });
        createRepoParserInfo.setProperties(new RepositoryPropertyConfigAO[]
        { new RepositoryPropertyConfigAO("1", "2"), new RepositoryPropertyConfigAO("3", "4") });

        ApiUtils.sendJSON(httpConn, createRepoInfoAO);
        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        RepositoryConfigAO createResponseAO = JsonTranslator.getInstance().fromJson(responseContent.toString(),
                RepositoryConfigAO.class);
        assertEquals("test_repoinfo_1", createResponseAO.getName());
        assertEquals("Repository Test 1", createResponseAO.getDisplayName());
        assertEquals("description", createResponseAO.getDescription());
        assertEquals(Status.ONLINE, createResponseAO.getStartupStatus());
        assertEquals(true, createResponseAO.getStoreEntriesIndicator());
        assertEquals(true, createResponseAO.getStorageQueueDurableIndicator());
        assertEquals(2, createResponseAO.getStorageQueueWorkerCount());
        assertEquals(100L, createResponseAO.getStorageMaxKeywords());
        assertEquals(10, createResponseAO.getMaxMemory());
        assertEquals(MaxMemoryPolicy.BLOCK, createResponseAO.getMaxMemoryPolicy());
        assertEquals(2, createResponseAO.getPageSize());
        assertEquals(new Long(1), createResponseAO.getDocumentVersion());

        RepositoryParserConfigAO createParserResponseAO = createResponseAO.getParsers()[0];
        assertEquals("parser1", createParserResponseAO.getName());
        assertEquals(AppliesTo.All, createParserResponseAO.getAppliesTo());
        assertEquals("sss", createParserResponseAO.getAppliesToSourceFilter());
        assertEquals("hhh", createParserResponseAO.getAppliesToHostFilter());
        assertEquals(DelimitedEntryParser.class.getName(), createParserResponseAO.getClassName());
        assertEquals(101L, createParserResponseAO.getMaxKeywords());
        assertEquals(2, createParserResponseAO.getProperties().length);
        assertEquals(ParseFieldErrorHandling.SkipEntry, createParserResponseAO.getParseFieldErrorHandling());
        assertEquals(2, createParserResponseAO.getProperties().length);
        assertEquals(2, createParserResponseAO.getFields().length);

        assertEquals("field1", createParserResponseAO.getFields()[0].getName());
        assertEquals(RepositoryFieldConfigBO.DataType.String, createParserResponseAO.getFields()[0].getDataType());
        assertEquals(2, createParserResponseAO.getFields()[0].getProperties().length);

        assertEquals("field2", createParserResponseAO.getFields()[1].getName());
        assertEquals(RepositoryFieldConfigBO.DataType.Integer, createParserResponseAO.getFields()[1].getDataType());
        assertEquals(2, createParserResponseAO.getFields()[1].getProperties().length);

        // Read one record
        httpConn = ApiUtils.getHttpURLConnection(
                "http://localhost:8989/api/repository_config/" + createResponseAO.getDocumentID(), HttpMethod.GET,
                _systemAdminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        RepositoryConfigAO readResponseAO = JsonTranslator.getInstance().fromJson(responseContent.toString(),
                RepositoryConfigAO.class);
        assertEquals("test_repoinfo_1", readResponseAO.getName());
        assertEquals("Repository Test 1", readResponseAO.getDisplayName());
        assertEquals("description", readResponseAO.getDescription());
        assertEquals(Status.ONLINE, readResponseAO.getStartupStatus());
        assertEquals(true, readResponseAO.getStoreEntriesIndicator());
        assertEquals(true, readResponseAO.getStorageQueueDurableIndicator());
        assertEquals(2, readResponseAO.getStorageQueueWorkerCount());
        assertEquals(100L, createResponseAO.getStorageMaxKeywords());
        assertEquals(10, readResponseAO.getMaxMemory());
        assertEquals(MaxMemoryPolicy.BLOCK, readResponseAO.getMaxMemoryPolicy());
        assertEquals(2, readResponseAO.getPageSize());

        createParserResponseAO = createResponseAO.getParsers()[0];
        assertEquals("parser1", createParserResponseAO.getName());
        assertEquals(AppliesTo.All, createParserResponseAO.getAppliesTo());
        assertEquals("sss", createParserResponseAO.getAppliesToSourceFilter());
        assertEquals("hhh", createParserResponseAO.getAppliesToHostFilter());
        assertEquals(DelimitedEntryParser.class.getName(), createParserResponseAO.getClassName());
        assertEquals(101L, createParserResponseAO.getMaxKeywords());
        assertEquals(2, createParserResponseAO.getProperties().length);
        assertEquals(ParseFieldErrorHandling.SkipEntry, createParserResponseAO.getParseFieldErrorHandling());
        assertEquals(2, createParserResponseAO.getProperties().length);
        assertEquals(2, createParserResponseAO.getFields().length);

        assertEquals("field1", createParserResponseAO.getFields()[0].getName());
        assertEquals(RepositoryFieldConfigBO.DataType.String, createParserResponseAO.getFields()[0].getDataType());
        assertEquals(2, createParserResponseAO.getFields()[0].getProperties().length);

        assertEquals("field2", createParserResponseAO.getFields()[1].getName());
        assertEquals(RepositoryFieldConfigBO.DataType.Integer, createParserResponseAO.getFields()[1].getDataType());
        assertEquals(2, createParserResponseAO.getFields()[1].getProperties().length);

        // Update
        httpConn = ApiUtils.getHttpURLConnection(
                "http://localhost:8989/api/repository_config/" + createResponseAO.getDocumentID(), HttpMethod.PUT,
                _systemAdminAuthToken);

        readResponseAO.setName("test_repoinfo_1_update");
        readResponseAO.setStorageMaxKeywords(200);
        readResponseAO.getParsers()[0].setMaxKeywords(201);
        readResponseAO.getParsers()[0].setFields(new RepositoryFieldConfigAO[]
        { f1 });
        readResponseAO.getParsers()[0].setProperties(new RepositoryPropertyConfigAO[]
        { new RepositoryPropertyConfigAO("1", "2") });

        ApiUtils.sendJSON(httpConn, readResponseAO);
        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        RepositoryConfigAO updateResponseAO = JsonTranslator.getInstance().fromJson(responseContent.toString(),
                RepositoryConfigAO.class);
        assertEquals("test_repoinfo_1_update", updateResponseAO.getName());
        assertEquals(200, updateResponseAO.getStorageMaxKeywords());
        assertEquals(201, readResponseAO.getParsers()[0].getMaxKeywords());
        assertEquals(1, readResponseAO.getParsers()[0].getProperties().length);
        assertEquals(1, readResponseAO.getParsers()[0].getFields().length);

        // Get list
        httpConn = ApiUtils.getHttpURLConnection(
                "http://localhost:8989/api/repository_config?name="
                        + URLEncoder.encode("^test_repoinfo[\\w]*$", "UTF-8"), HttpMethod.GET, _systemAdminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        RepositoryConfigAO[] getListResponseAO = JsonTranslator.getInstance().fromJson(responseContent.toString(),
                RepositoryConfigAO[].class);
        assertEquals(2, getListResponseAO.length);

        // Delete
        httpConn = ApiUtils.getHttpURLConnection(
                "http://localhost:8989/api/repository_config/" + createResponseAO.getDocumentID(), HttpMethod.DELETE,
                _systemAdminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check204NoContentResponse(responseCode.toString(), headers);

        // Get record to check if it is gone
        httpConn = ApiUtils.getHttpURLConnection(
                "http://localhost:8989/api/repository_config/" + createResponseAO.getDocumentID(), HttpMethod.GET,
                _systemAdminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check400BadRequestResponse(responseCode.toString(), headers);

        ErrorAO errorAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), ErrorAO.class);
        assertEquals("ChiliLogException:Data.RepoInfo.NotFoundError", errorAO.getErrorCode());
    }

    /**
     * Test access by repo admin users
     * 
     * @throws Exception
     */
    @Test
    public void testRepoAdminUser() throws Exception
    {
        HttpURLConnection httpConn;
        StringBuilder responseContent = new StringBuilder();
        StringBuilder responseCode = new StringBuilder();
        HashMap<String, String> headers = new HashMap<String, String>();

        // Get chililog repository
        httpConn = ApiUtils.getHttpURLConnection(
                "http://localhost:8989/api/repository_config?name=chililog", HttpMethod.GET, _systemAdminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        RepositoryConfigAO[] getListResponseAO = JsonTranslator.getInstance().fromJson(responseContent.toString(),
                RepositoryConfigAO[].class);
        assertEquals(1, getListResponseAO.length);
        RepositoryConfigAO chililogRepoInfoAO = getListResponseAO[0];

        // Get list - should only get back repositories we can access
        httpConn = ApiUtils.getHttpURLConnection(
                "http://localhost:8989/api/repository_config?", HttpMethod.GET, _repoAdminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        getListResponseAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), RepositoryConfigAO[].class);
        assertEquals(1, getListResponseAO.length);
        assertEquals("test_repoinfo_common", getListResponseAO[0].getName());

        // Create - not authroized
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repository_config", HttpMethod.POST,
                _repoAdminAuthToken);

        RepositoryConfigAO repoInfoAO = new RepositoryConfigAO();
        repoInfoAO.setName("test_repoinfo_1");
        repoInfoAO.setDisplayName("Repository Test 1");
        repoInfoAO.setDescription("description");

        ApiUtils.sendJSON(httpConn, repoInfoAO);
        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check401UnauthorizedResponse(responseCode.toString(), headers);

        ErrorAO errorAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), ErrorAO.class);
        assertEquals("ChiliLogException:Workbench.NotAuthorizedError", errorAO.getErrorCode());

        // Update - authorized for repo on which we have permission
        httpConn = ApiUtils.getHttpURLConnection(
                "http://localhost:8989/api/repository_config/" + getListResponseAO[0].getDocumentID(), HttpMethod.PUT,
                _repoAdminAuthToken);

        ApiUtils.sendJSON(httpConn, getListResponseAO[0]);
        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        // Update - not authorized for repo with no permission
        httpConn = ApiUtils.getHttpURLConnection(
                "http://localhost:8989/api/repository_config/" + chililogRepoInfoAO.getDocumentID(), HttpMethod.PUT,
                _repoAdminAuthToken);

        ApiUtils.sendJSON(httpConn, chililogRepoInfoAO);
        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check401UnauthorizedResponse(responseCode.toString(), headers);

        errorAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), ErrorAO.class);
        assertEquals("ChiliLogException:Workbench.NotAuthorizedError", errorAO.getErrorCode());

        // Delete - not authorized
        httpConn = ApiUtils.getHttpURLConnection(
                "http://localhost:8989/api/repository_config/" + getListResponseAO[0].getDocumentID(), HttpMethod.DELETE,
                _repoAdminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check401UnauthorizedResponse(responseCode.toString(), headers);

        errorAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), ErrorAO.class);
        assertEquals("ChiliLogException:Workbench.NotAuthorizedError", errorAO.getErrorCode());
    }

    /**
     * Test access by repo power users
     * 
     * @throws Exception
     */
    @Test
    public void testRepoWorkbenchUser() throws Exception
    {
        HttpURLConnection httpConn;
        StringBuilder responseContent = new StringBuilder();
        StringBuilder responseCode = new StringBuilder();
        HashMap<String, String> headers = new HashMap<String, String>();

        // Get chililog repository
        httpConn = ApiUtils.getHttpURLConnection(
                "http://localhost:8989/api/repository_config?name=chililog", HttpMethod.GET, _systemAdminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        RepositoryConfigAO[] getListResponseAO = JsonTranslator.getInstance().fromJson(responseContent.toString(),
                RepositoryConfigAO[].class);
        assertEquals(1, getListResponseAO.length);
        RepositoryConfigAO chililogRepoInfoAO = getListResponseAO[0];

        // Get list - should only get back repositories we can access
        httpConn = ApiUtils.getHttpURLConnection(
                "http://localhost:8989/api/repository_config?", HttpMethod.GET, _repoWorkbenchUserAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        getListResponseAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), RepositoryConfigAO[].class);
        assertEquals(1, getListResponseAO.length);
        assertEquals("test_repoinfo_common", getListResponseAO[0].getName());

        // Create - not authroized
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repository_config", HttpMethod.POST,
                _repoWorkbenchUserAuthToken);

        RepositoryConfigAO repoInfoAO = new RepositoryConfigAO();
        repoInfoAO.setName("test_repoinfo_1");
        repoInfoAO.setDisplayName("Repository Test 1");
        repoInfoAO.setDescription("description");

        ApiUtils.sendJSON(httpConn, repoInfoAO);
        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check401UnauthorizedResponse(responseCode.toString(), headers);

        ErrorAO errorAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), ErrorAO.class);
        assertEquals("ChiliLogException:Workbench.NotAuthorizedError", errorAO.getErrorCode());

        // Update - not authorized for repo on which we have permission
        httpConn = ApiUtils.getHttpURLConnection(
                "http://localhost:8989/api/repository_config/" + getListResponseAO[0].getDocumentID(), HttpMethod.PUT,
                _repoWorkbenchUserAuthToken);

        ApiUtils.sendJSON(httpConn, getListResponseAO[0]);
        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check401UnauthorizedResponse(responseCode.toString(), headers);

        errorAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), ErrorAO.class);
        assertEquals("ChiliLogException:Workbench.NotAuthorizedError", errorAO.getErrorCode());

        // Update - not authorized for repo with no permission
        httpConn = ApiUtils.getHttpURLConnection(
                "http://localhost:8989/api/repository_config/" + chililogRepoInfoAO.getDocumentID(), HttpMethod.PUT,
                _repoWorkbenchUserAuthToken);

        ApiUtils.sendJSON(httpConn, chililogRepoInfoAO);
        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check401UnauthorizedResponse(responseCode.toString(), headers);

        errorAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), ErrorAO.class);
        assertEquals("ChiliLogException:Workbench.NotAuthorizedError", errorAO.getErrorCode());

        // Delete - not authorized
        httpConn = ApiUtils.getHttpURLConnection(
                "http://localhost:8989/api/repository_config/" + getListResponseAO[0].getDocumentID(), HttpMethod.DELETE,
                _repoWorkbenchUserAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check401UnauthorizedResponse(responseCode.toString(), headers);

        errorAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), ErrorAO.class);
        assertEquals("ChiliLogException:Workbench.NotAuthorizedError", errorAO.getErrorCode());
    }
    
    /**
     * Try put and delete without an ID in URI
     * 
     * @throws Exception
     */
    @Test
    public void testMissingID() throws Exception
    {
        HttpURLConnection httpConn;
        StringBuilder responseContent = new StringBuilder();
        StringBuilder responseCode = new StringBuilder();
        HashMap<String, String> headers = new HashMap<String, String>();

        // Update
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repository_config", HttpMethod.PUT,
                _systemAdminAuthToken);

        RepositoryConfigAO repoInfoAO = new RepositoryConfigAO();
        repoInfoAO.setName("test_repoinfo_3");
        repoInfoAO.setDisplayName("Repository Test 3");
        repoInfoAO.setDescription("description");

        ApiUtils.sendJSON(httpConn, repoInfoAO);
        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check400BadRequestResponse(responseCode.toString(), headers);

        ErrorAO errorAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), ErrorAO.class);
        assertEquals("ChiliLogException:Workbench.UriPathParameterError", errorAO.getErrorCode());

        // Delete
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repository_config", HttpMethod.DELETE,
                _systemAdminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check400BadRequestResponse(responseCode.toString(), headers);

        errorAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), ErrorAO.class);
        assertEquals("ChiliLogException:Workbench.UriPathParameterError", errorAO.getErrorCode());
    }

    /**
     * Try put without an ID in URI
     * 
     * @throws Exception
     */
    @Test
    public void testListing() throws Exception
    {
        HttpURLConnection httpConn;
        StringBuilder responseContent = new StringBuilder();
        StringBuilder responseCode = new StringBuilder();
        HashMap<String, String> headers = new HashMap<String, String>();

        // Get list - no records
        httpConn = ApiUtils.getHttpURLConnection(
                "http://localhost:8989/api/repository_config?name=" + URLEncoder.encode("^xxxxxxxxx[\\w]*$", "UTF-8"),
                HttpMethod.GET, _systemAdminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check204NoContentResponse(responseCode.toString(), headers);
        assertEquals("", responseContent.toString());
        assertFalse(headers.containsKey(Worker.PAGE_COUNT_HEADER));

        // Get list - page 1
        httpConn = ApiUtils.getHttpURLConnection(
                "http://localhost:8989/api/repository_config?records_per_page=1&start_page=1&do_page_count=true&name="
                        + URLEncoder.encode("^test_repoinfo[\\w]*$", "UTF-8"), HttpMethod.GET, _systemAdminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        UserAO[] getListResponseAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), UserAO[].class);
        assertEquals(1, getListResponseAO.length);

        String pageCountHeader = headers.get(Worker.PAGE_COUNT_HEADER);
        assertEquals("1", pageCountHeader);

        // Get list - page 2 (no more records)
        httpConn = ApiUtils.getHttpURLConnection(
                "http://localhost:8989/api/repository_config?records_per_page=1&start_page=2&name="
                        + URLEncoder.encode("^test_repoinfo[\\w]*$", "UTF-8"), HttpMethod.GET, _systemAdminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check204NoContentResponse(responseCode.toString(), headers);
        assertEquals("", responseContent.toString());
    }

    /**
     * Bad content
     * 
     * @throws Exception
     */
    @Test
    public void testBadContent() throws Exception
    {
        HttpURLConnection httpConn;
        StringBuilder responseContent = new StringBuilder();
        StringBuilder responseCode = new StringBuilder();
        HashMap<String, String> headers = new HashMap<String, String>();

        // Create no content
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repository_config", HttpMethod.POST,
                _systemAdminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check400BadRequestResponse(responseCode.toString(), headers);

        ErrorAO errorAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), ErrorAO.class);
        assertEquals("ChiliLogException:Workbench.RequiredContentError", errorAO.getErrorCode());

        // Create no name
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repository_config", HttpMethod.POST,
                _systemAdminAuthToken);

        RepositoryConfigAO createRepoInfoAO = new RepositoryConfigAO();
        // createRepoInfoAO.setName("test_repoinfo_1");
        createRepoInfoAO.setDisplayName("Repository Test 1");
        createRepoInfoAO.setDescription("description");
        createRepoInfoAO.setStartupStatus(Status.ONLINE);
        createRepoInfoAO.setStoreEntriesIndicator(true);
        createRepoInfoAO.setStorageQueueDurableIndicator(true);
        createRepoInfoAO.setStorageQueueWorkerCount(2);
        createRepoInfoAO.setMaxMemory(10);
        createRepoInfoAO.setMaxMemoryPolicy(MaxMemoryPolicy.BLOCK);
        createRepoInfoAO.setPageSize(2);

        ApiUtils.sendJSON(httpConn, createRepoInfoAO);
        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check400BadRequestResponse(responseCode.toString(), headers);

        errorAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), ErrorAO.class);
        assertEquals("ChiliLogException:Data.MongoDB.MissingRequiredFieldError", errorAO.getErrorCode());

        // Create no class name
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repository_config", HttpMethod.POST,
                _systemAdminAuthToken);

        RepositoryParserConfigAO createRepoParserInfo = new RepositoryParserConfigAO();
        createRepoParserInfo.setName("parser1");
        createRepoParserInfo.setAppliesTo(AppliesTo.All);
        createRepoParserInfo.setClassName("");

        createRepoInfoAO.setDisplayName("Repository Test 1");
        createRepoInfoAO.setParsers(new RepositoryParserConfigAO[]
        { createRepoParserInfo });

        ApiUtils.sendJSON(httpConn, createRepoInfoAO);
        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check400BadRequestResponse(responseCode.toString(), headers);

        errorAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), ErrorAO.class);
        assertEquals("ChiliLogException:Data.MongoDB.MissingRequiredFieldError", errorAO.getErrorCode());

        // Update no content
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repository_config/12341234", HttpMethod.PUT,
                _systemAdminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check400BadRequestResponse(responseCode.toString(), headers);

        errorAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), ErrorAO.class);
        assertEquals("ChiliLogException:Workbench.RequiredContentError", errorAO.getErrorCode());

        // Update no name
        httpConn = ApiUtils.getHttpURLConnection(
                "http://localhost:8989/api/repository_config?name="
                        + URLEncoder.encode("^test_repoinfo[\\w]*$", "UTF-8"), HttpMethod.GET, _systemAdminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        RepositoryConfigAO[] getListResponseAO = JsonTranslator.getInstance().fromJson(responseContent.toString(),
                RepositoryConfigAO[].class);
        assertEquals(1, getListResponseAO.length);

        httpConn = ApiUtils.getHttpURLConnection(
                "http://localhost:8989/api/repository_config/" + getListResponseAO[0].getDocumentID(), HttpMethod.PUT,
                _systemAdminAuthToken);

        getListResponseAO[0].setName(null);

        ApiUtils.sendJSON(httpConn, getListResponseAO[0]);
        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check400BadRequestResponse(responseCode.toString(), headers);

        errorAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), ErrorAO.class);
        assertEquals("ChiliLogException:Data.MongoDB.MissingRequiredFieldError", errorAO.getErrorCode());

        // Update no doc version
        httpConn = ApiUtils.getHttpURLConnection(
                "http://localhost:8989/api/repository_config/" + getListResponseAO[0].getDocumentID(), HttpMethod.PUT,
                _systemAdminAuthToken);

        getListResponseAO[0].setName("abc");
        getListResponseAO[0].setDocumentVersion(null);

        ApiUtils.sendJSON(httpConn, getListResponseAO[0]);
        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check400BadRequestResponse(responseCode.toString(), headers);

        errorAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), ErrorAO.class);
        assertEquals("ChiliLogException:Workbench.RequiredFieldError", errorAO.getErrorCode());
    }

}
