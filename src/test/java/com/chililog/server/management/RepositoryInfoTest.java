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

package com.chililog.server.management;

import static org.junit.Assert.*;

import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.regex.Pattern;

import org.jboss.netty.handler.codec.http.HttpMethod;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.chililog.server.common.JsonTranslator;
import com.chililog.server.data.MongoConnection;
import com.chililog.server.data.RepositoryFieldInfoBO;
import com.chililog.server.data.RepositoryInfoBO;
import com.chililog.server.data.RepositoryParserInfoBO;
import com.chililog.server.data.RepositoryInfoBO.Status;
import com.chililog.server.data.RepositoryInfoController;
import com.chililog.server.data.UserBO;
import com.chililog.server.data.RepositoryInfoBO.MaxMemoryPolicy;
import com.chililog.server.data.RepositoryParserInfoBO.AppliesTo;
import com.chililog.server.data.RepositoryParserInfoBO.ParseFieldErrorHandling;
import com.chililog.server.data.UserController;
import com.chililog.server.engine.parsers.DelimitedEntryParser;
import com.chililog.server.management.ManagementInterfaceManager;
import com.chililog.server.management.workers.ErrorAO;
import com.chililog.server.management.workers.RepositoryFieldInfoAO;
import com.chililog.server.management.workers.RepositoryInfoAO;
import com.chililog.server.management.workers.RepositoryParserInfoAO;
import com.chililog.server.management.workers.RepositoryPropertyInfoAO;
import com.chililog.server.management.workers.UserAO;
import com.chililog.server.management.workers.Worker;
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
public class RepositoryInfoTest
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
        coll = _db.getCollection(RepositoryInfoController.MONGODB_COLLECTION_NAME);
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
        user.setStatus(com.chililog.server.data.UserBO.Status.Enabled);
        UserController.getInstance().save(_db, user);

        // Create repository admin user
        user = new UserBO();
        user.setUsername("TestRepoInfo_RepoAdmin");
        user.setPassword("hello", true);
        user.setEmailAddress("TestRepoInfo_RepoAdmin@chililog.com");
        user.addRole("repo.test_repoinfo_common.administrator");
        user.setStatus(com.chililog.server.data.UserBO.Status.Enabled);
        UserController.getInstance().save(_db, user);

        // Create repository workbench user
        user = new UserBO();
        user.setUsername("TestRepoInfo_RepoWorkbench");
        user.setPassword("hello", true);
        user.setEmailAddress("TestRepoInfo_RepoWorkbench@chililog.com");
        user.addRole("repo.test_repoinfo_common.workbench");
        user.setStatus(com.chililog.server.data.UserBO.Status.Enabled);
        UserController.getInstance().save(_db, user);

        // Create test repo
        RepositoryInfoBO repoInfo = new RepositoryInfoBO();
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

        RepositoryParserInfoBO repoParserInfo = new RepositoryParserInfoBO();
        repoParserInfo.setName("parser1");
        repoParserInfo.setAppliesTo(AppliesTo.All);
        repoParserInfo.setClassName(DelimitedEntryParser.class.getName());
        repoParserInfo.setMaxKeywords(101);
        repoParserInfo.setParseFieldErrorHandling(ParseFieldErrorHandling.SkipEntry);
        repoParserInfo.getProperties().put(DelimitedEntryParser.DELIMITER_PROPERTY_NAME, "|");
        repoInfo.getParsers().add(repoParserInfo);

        RepositoryFieldInfoBO repoFieldInfo = new RepositoryFieldInfoBO();
        repoFieldInfo.setName("field1");
        repoFieldInfo.setDataType(RepositoryFieldInfoBO.DataType.String);
        repoFieldInfo.getProperties().put(DelimitedEntryParser.POSITION_FIELD_PROPERTY_NAME, "1");
        repoParserInfo.getFields().add(repoFieldInfo);

        RepositoryInfoController.getInstance().save(_db, repoInfo);

        // Start web server
        ManagementInterfaceManager.getInstance().start();

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
        coll = _db.getCollection(RepositoryInfoController.MONGODB_COLLECTION_NAME);
        pattern = Pattern.compile("^test_repoinfo[\\w]*$");
        query = new BasicDBObject();
        query.put("name", pattern);
        coll.remove(query);
        
        ManagementInterfaceManager.getInstance().stop();
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
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repository_info", HttpMethod.POST,
                _systemAdminAuthToken);

        RepositoryFieldInfoAO f1 = new RepositoryFieldInfoAO();
        f1.setName("field1");
        f1.setDataType(RepositoryFieldInfoBO.DataType.String);
        f1.setProperties(new RepositoryPropertyInfoAO[]
        { new RepositoryPropertyInfoAO("F1", "F2"), new RepositoryPropertyInfoAO("F3", "F4") });

        RepositoryFieldInfoAO f2 = new RepositoryFieldInfoAO();
        f2.setName("field2");
        f2.setDataType(RepositoryFieldInfoBO.DataType.Integer);
        f2.setProperties(new RepositoryPropertyInfoAO[]
        { new RepositoryPropertyInfoAO("F5", "F6"), new RepositoryPropertyInfoAO("F7", "F8") });

        RepositoryInfoAO createRepoInfoAO = new RepositoryInfoAO();
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

        RepositoryParserInfoAO createRepoParserInfo = new RepositoryParserInfoAO();
        createRepoParserInfo.setName("parser1");
        createRepoParserInfo.setAppliesTo(AppliesTo.All);
        createRepoParserInfo.setAppliesToSourceFilter("sss");
        createRepoParserInfo.setAppliesToHostFilter("hhh");
        createRepoParserInfo.setClassName(DelimitedEntryParser.class.getName());
        createRepoParserInfo.setMaxKeywords(101);
        createRepoParserInfo.setParseFieldErrorHandling(ParseFieldErrorHandling.SkipEntry);
        createRepoInfoAO.setParsers(new RepositoryParserInfoAO[]
        { createRepoParserInfo });

        createRepoParserInfo.setFields(new RepositoryFieldInfoAO[]
        { f1, f2 });
        createRepoParserInfo.setProperties(new RepositoryPropertyInfoAO[]
        { new RepositoryPropertyInfoAO("1", "2"), new RepositoryPropertyInfoAO("3", "4") });

        ApiUtils.sendJSON(httpConn, createRepoInfoAO);
        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        RepositoryInfoAO createResponseAO = JsonTranslator.getInstance().fromJson(responseContent.toString(),
                RepositoryInfoAO.class);
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

        RepositoryParserInfoAO createParserResponseAO = createResponseAO.getParsers()[0];
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
        assertEquals(RepositoryFieldInfoBO.DataType.String, createParserResponseAO.getFields()[0].getDataType());
        assertEquals(2, createParserResponseAO.getFields()[0].getProperties().length);

        assertEquals("field2", createParserResponseAO.getFields()[1].getName());
        assertEquals(RepositoryFieldInfoBO.DataType.Integer, createParserResponseAO.getFields()[1].getDataType());
        assertEquals(2, createParserResponseAO.getFields()[1].getProperties().length);

        // Read one record
        httpConn = ApiUtils.getHttpURLConnection(
                "http://localhost:8989/api/repository_info/" + createResponseAO.getDocumentID(), HttpMethod.GET,
                _systemAdminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        RepositoryInfoAO readResponseAO = JsonTranslator.getInstance().fromJson(responseContent.toString(),
                RepositoryInfoAO.class);
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
        assertEquals(RepositoryFieldInfoBO.DataType.String, createParserResponseAO.getFields()[0].getDataType());
        assertEquals(2, createParserResponseAO.getFields()[0].getProperties().length);

        assertEquals("field2", createParserResponseAO.getFields()[1].getName());
        assertEquals(RepositoryFieldInfoBO.DataType.Integer, createParserResponseAO.getFields()[1].getDataType());
        assertEquals(2, createParserResponseAO.getFields()[1].getProperties().length);

        // Update
        httpConn = ApiUtils.getHttpURLConnection(
                "http://localhost:8989/api/repository_info/" + createResponseAO.getDocumentID(), HttpMethod.PUT,
                _systemAdminAuthToken);

        readResponseAO.setName("test_repoinfo_1_update");
        readResponseAO.setStorageMaxKeywords(200);
        readResponseAO.getParsers()[0].setMaxKeywords(201);
        readResponseAO.getParsers()[0].setFields(new RepositoryFieldInfoAO[]
        { f1 });
        readResponseAO.getParsers()[0].setProperties(new RepositoryPropertyInfoAO[]
        { new RepositoryPropertyInfoAO("1", "2") });

        ApiUtils.sendJSON(httpConn, readResponseAO);
        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        RepositoryInfoAO updateResponseAO = JsonTranslator.getInstance().fromJson(responseContent.toString(),
                RepositoryInfoAO.class);
        assertEquals("test_repoinfo_1_update", updateResponseAO.getName());
        assertEquals(200, updateResponseAO.getStorageMaxKeywords());
        assertEquals(201, readResponseAO.getParsers()[0].getMaxKeywords());
        assertEquals(1, readResponseAO.getParsers()[0].getProperties().length);
        assertEquals(1, readResponseAO.getParsers()[0].getFields().length);

        // Get list
        httpConn = ApiUtils.getHttpURLConnection(
                "http://localhost:8989/api/repository_info?name="
                        + URLEncoder.encode("^test_repoinfo[\\w]*$", "UTF-8"), HttpMethod.GET, _systemAdminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        RepositoryInfoAO[] getListResponseAO = JsonTranslator.getInstance().fromJson(responseContent.toString(),
                RepositoryInfoAO[].class);
        assertEquals(2, getListResponseAO.length);

        // Delete
        httpConn = ApiUtils.getHttpURLConnection(
                "http://localhost:8989/api/repository_info/" + createResponseAO.getDocumentID(), HttpMethod.DELETE,
                _systemAdminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check204NoContentResponse(responseCode.toString(), headers);

        // Get record to check if it is gone
        httpConn = ApiUtils.getHttpURLConnection(
                "http://localhost:8989/api/repository_info/" + createResponseAO.getDocumentID(), HttpMethod.GET,
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
                "http://localhost:8989/api/repository_info?name=chililog", HttpMethod.GET, _systemAdminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        RepositoryInfoAO[] getListResponseAO = JsonTranslator.getInstance().fromJson(responseContent.toString(),
                RepositoryInfoAO[].class);
        assertEquals(1, getListResponseAO.length);
        RepositoryInfoAO chililogRepoInfoAO = getListResponseAO[0];

        // Get list - should only get back repositories we can access
        httpConn = ApiUtils.getHttpURLConnection(
                "http://localhost:8989/api/repository_info?", HttpMethod.GET, _repoAdminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        getListResponseAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), RepositoryInfoAO[].class);
        assertEquals(1, getListResponseAO.length);
        assertEquals("test_repoinfo_common", getListResponseAO[0].getName());

        // Create - not authroized
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repository_info", HttpMethod.POST,
                _repoAdminAuthToken);

        RepositoryInfoAO repoInfoAO = new RepositoryInfoAO();
        repoInfoAO.setName("test_repoinfo_1");
        repoInfoAO.setDisplayName("Repository Test 1");
        repoInfoAO.setDescription("description");

        ApiUtils.sendJSON(httpConn, repoInfoAO);
        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check401UnauthorizedResponse(responseCode.toString(), headers);

        ErrorAO errorAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), ErrorAO.class);
        assertEquals("ChiliLogException:UI.NotAuthorizedError", errorAO.getErrorCode());

        // Update - authorized for repo on which we have permission
        httpConn = ApiUtils.getHttpURLConnection(
                "http://localhost:8989/api/repository_info/" + getListResponseAO[0].getDocumentID(), HttpMethod.PUT,
                _repoAdminAuthToken);

        ApiUtils.sendJSON(httpConn, getListResponseAO[0]);
        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        // Update - not authorized for repo with no permission
        httpConn = ApiUtils.getHttpURLConnection(
                "http://localhost:8989/api/repository_info/" + chililogRepoInfoAO.getDocumentID(), HttpMethod.PUT,
                _repoAdminAuthToken);

        ApiUtils.sendJSON(httpConn, chililogRepoInfoAO);
        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check401UnauthorizedResponse(responseCode.toString(), headers);

        errorAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), ErrorAO.class);
        assertEquals("ChiliLogException:UI.NotAuthorizedError", errorAO.getErrorCode());

        // Delete - not authorized
        httpConn = ApiUtils.getHttpURLConnection(
                "http://localhost:8989/api/repository_info/" + getListResponseAO[0].getDocumentID(), HttpMethod.DELETE,
                _repoAdminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check401UnauthorizedResponse(responseCode.toString(), headers);

        errorAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), ErrorAO.class);
        assertEquals("ChiliLogException:UI.NotAuthorizedError", errorAO.getErrorCode());
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
                "http://localhost:8989/api/repository_info?name=chililog", HttpMethod.GET, _systemAdminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        RepositoryInfoAO[] getListResponseAO = JsonTranslator.getInstance().fromJson(responseContent.toString(),
                RepositoryInfoAO[].class);
        assertEquals(1, getListResponseAO.length);
        RepositoryInfoAO chililogRepoInfoAO = getListResponseAO[0];

        // Get list - should only get back repositories we can access
        httpConn = ApiUtils.getHttpURLConnection(
                "http://localhost:8989/api/repository_info?", HttpMethod.GET, _repoWorkbenchUserAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        getListResponseAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), RepositoryInfoAO[].class);
        assertEquals(1, getListResponseAO.length);
        assertEquals("test_repoinfo_common", getListResponseAO[0].getName());

        // Create - not authroized
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repository_info", HttpMethod.POST,
                _repoWorkbenchUserAuthToken);

        RepositoryInfoAO repoInfoAO = new RepositoryInfoAO();
        repoInfoAO.setName("test_repoinfo_1");
        repoInfoAO.setDisplayName("Repository Test 1");
        repoInfoAO.setDescription("description");

        ApiUtils.sendJSON(httpConn, repoInfoAO);
        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check401UnauthorizedResponse(responseCode.toString(), headers);

        ErrorAO errorAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), ErrorAO.class);
        assertEquals("ChiliLogException:UI.NotAuthorizedError", errorAO.getErrorCode());

        // Update - not authorized for repo on which we have permission
        httpConn = ApiUtils.getHttpURLConnection(
                "http://localhost:8989/api/repository_info/" + getListResponseAO[0].getDocumentID(), HttpMethod.PUT,
                _repoWorkbenchUserAuthToken);

        ApiUtils.sendJSON(httpConn, getListResponseAO[0]);
        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check401UnauthorizedResponse(responseCode.toString(), headers);

        errorAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), ErrorAO.class);
        assertEquals("ChiliLogException:UI.NotAuthorizedError", errorAO.getErrorCode());

        // Update - not authorized for repo with no permission
        httpConn = ApiUtils.getHttpURLConnection(
                "http://localhost:8989/api/repository_info/" + chililogRepoInfoAO.getDocumentID(), HttpMethod.PUT,
                _repoWorkbenchUserAuthToken);

        ApiUtils.sendJSON(httpConn, chililogRepoInfoAO);
        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check401UnauthorizedResponse(responseCode.toString(), headers);

        errorAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), ErrorAO.class);
        assertEquals("ChiliLogException:UI.NotAuthorizedError", errorAO.getErrorCode());

        // Delete - not authorized
        httpConn = ApiUtils.getHttpURLConnection(
                "http://localhost:8989/api/repository_info/" + getListResponseAO[0].getDocumentID(), HttpMethod.DELETE,
                _repoWorkbenchUserAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check401UnauthorizedResponse(responseCode.toString(), headers);

        errorAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), ErrorAO.class);
        assertEquals("ChiliLogException:UI.NotAuthorizedError", errorAO.getErrorCode());
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
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repository_info", HttpMethod.PUT,
                _systemAdminAuthToken);

        RepositoryInfoAO repoInfoAO = new RepositoryInfoAO();
        repoInfoAO.setName("test_repoinfo_3");
        repoInfoAO.setDisplayName("Repository Test 3");
        repoInfoAO.setDescription("description");

        ApiUtils.sendJSON(httpConn, repoInfoAO);
        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check400BadRequestResponse(responseCode.toString(), headers);

        ErrorAO errorAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), ErrorAO.class);
        assertEquals("ChiliLogException:UI.UriPathParameterError", errorAO.getErrorCode());

        // Delete
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repository_info", HttpMethod.DELETE,
                _systemAdminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check400BadRequestResponse(responseCode.toString(), headers);

        errorAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), ErrorAO.class);
        assertEquals("ChiliLogException:UI.UriPathParameterError", errorAO.getErrorCode());
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
                "http://localhost:8989/api/repository_info?name=" + URLEncoder.encode("^xxxxxxxxx[\\w]*$", "UTF-8"),
                HttpMethod.GET, _systemAdminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check204NoContentResponse(responseCode.toString(), headers);
        assertEquals("", responseContent.toString());
        assertFalse(headers.containsKey(Worker.PAGE_COUNT_HEADER));

        // Get list - page 1
        httpConn = ApiUtils.getHttpURLConnection(
                "http://localhost:8989/api/repository_info?records_per_page=1&start_page=1&do_page_count=true&name="
                        + URLEncoder.encode("^test_repoinfo[\\w]*$", "UTF-8"), HttpMethod.GET, _systemAdminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        UserAO[] getListResponseAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), UserAO[].class);
        assertEquals(1, getListResponseAO.length);

        String pageCountHeader = headers.get(Worker.PAGE_COUNT_HEADER);
        assertEquals("1", pageCountHeader);

        // Get list - page 2 (no more records)
        httpConn = ApiUtils.getHttpURLConnection(
                "http://localhost:8989/api/repository_info?records_per_page=1&start_page=2&name="
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
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repository_info", HttpMethod.POST,
                _systemAdminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check400BadRequestResponse(responseCode.toString(), headers);

        ErrorAO errorAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), ErrorAO.class);
        assertEquals("ChiliLogException:UI.RequiredContentError", errorAO.getErrorCode());

        // Create no name
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repository_info", HttpMethod.POST,
                _systemAdminAuthToken);

        RepositoryInfoAO createRepoInfoAO = new RepositoryInfoAO();
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
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repository_info", HttpMethod.POST,
                _systemAdminAuthToken);

        RepositoryParserInfoAO createRepoParserInfo = new RepositoryParserInfoAO();
        createRepoParserInfo.setName("parser1");
        createRepoParserInfo.setAppliesTo(AppliesTo.All);
        createRepoParserInfo.setClassName("");

        createRepoInfoAO.setDisplayName("Repository Test 1");
        createRepoInfoAO.setParsers(new RepositoryParserInfoAO[]
        { createRepoParserInfo });

        ApiUtils.sendJSON(httpConn, createRepoInfoAO);
        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check400BadRequestResponse(responseCode.toString(), headers);

        errorAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), ErrorAO.class);
        assertEquals("ChiliLogException:Data.MongoDB.MissingRequiredFieldError", errorAO.getErrorCode());

        // Update no content
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repository_info/12341234", HttpMethod.PUT,
                _systemAdminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check400BadRequestResponse(responseCode.toString(), headers);

        errorAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), ErrorAO.class);
        assertEquals("ChiliLogException:UI.RequiredContentError", errorAO.getErrorCode());

        // Update no name
        httpConn = ApiUtils.getHttpURLConnection(
                "http://localhost:8989/api/repository_info?name="
                        + URLEncoder.encode("^test_repoinfo[\\w]*$", "UTF-8"), HttpMethod.GET, _systemAdminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        RepositoryInfoAO[] getListResponseAO = JsonTranslator.getInstance().fromJson(responseContent.toString(),
                RepositoryInfoAO[].class);
        assertEquals(1, getListResponseAO.length);

        httpConn = ApiUtils.getHttpURLConnection(
                "http://localhost:8989/api/repository_info/" + getListResponseAO[0].getDocumentID(), HttpMethod.PUT,
                _systemAdminAuthToken);

        getListResponseAO[0].setName(null);

        ApiUtils.sendJSON(httpConn, getListResponseAO[0]);
        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check400BadRequestResponse(responseCode.toString(), headers);

        errorAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), ErrorAO.class);
        assertEquals("ChiliLogException:Data.MongoDB.MissingRequiredFieldError", errorAO.getErrorCode());

        // Update no doc version
        httpConn = ApiUtils.getHttpURLConnection(
                "http://localhost:8989/api/repository_info/" + getListResponseAO[0].getDocumentID(), HttpMethod.PUT,
                _systemAdminAuthToken);

        getListResponseAO[0].setName("abc");
        getListResponseAO[0].setDocumentVersion(null);

        ApiUtils.sendJSON(httpConn, getListResponseAO[0]);
        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check400BadRequestResponse(responseCode.toString(), headers);

        errorAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), ErrorAO.class);
        assertEquals("ChiliLogException:UI.RequiredFieldError", errorAO.getErrorCode());
    }

}
