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

import java.io.OutputStreamWriter;
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
import com.chililog.server.data.RepositoryInfoBO.QueueMaxMemoryPolicy;
import com.chililog.server.data.RepositoryParserInfoBO.AppliesTo;
import com.chililog.server.data.RepositoryParserInfoBO.ParseFieldErrorHandling;
import com.chililog.server.data.UserController;
import com.chililog.server.engine.parsers.DelimitedEntryParser;
import com.chililog.server.ui.api.ErrorAO;
import com.chililog.server.ui.api.RepositoryFieldInfoAO;
import com.chililog.server.ui.api.RepositoryInfoAO;
import com.chililog.server.ui.api.RepositoryParserInfoAO;
import com.chililog.server.ui.api.RepositoryPropertyInfoAO;
import com.chililog.server.ui.api.UserAO;
import com.chililog.server.ui.api.Worker;
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
    private static String _adminAuthToken;
    private static String _analystAuthToken;

    @BeforeClass
    public static void classSetup() throws Exception
    {
        _db = MongoConnection.getInstance().getConnection();
        assertNotNull(_db);

        // Clean up old user test data if any exists
        DBCollection coll = _db.getCollection(UserController.MONGODB_COLLECTION_NAME);
        Pattern pattern = Pattern.compile("^RepositoryInfoTest[\\w]*$");
        DBObject query = new BasicDBObject();
        query.put("username", pattern);
        coll.remove(query);

        // Clean up old repository test data if any exists
        coll = _db.getCollection(RepositoryInfoController.MONGODB_COLLECTION_NAME);
        pattern = Pattern.compile("^RepositoryInfoTest[\\w]*$");
        query = new BasicDBObject();
        query.put("name", pattern);
        coll.remove(query);

        // Create admin user
        UserBO user = new UserBO();
        user.setUsername("RepositoryInfoTest_Admin");
        user.setPassword("hello", true);
        user.addRole(Worker.WORKBENCH_ADMINISTRATOR_USER_ROLE);
        UserController.getInstance().save(_db, user);

        // Create analyst user
        user = new UserBO();
        user.setUsername("RepositoryInfoTest_Analyst");
        user.setPassword("hello", true);
        user.addRole(Worker.WORKBENCH_ANALYST_USER_ROLE);
        UserController.getInstance().save(_db, user);

        // Create test repo
        RepositoryInfoBO repoInfo = new RepositoryInfoBO();
        repoInfo.setName("RepositoryInfoTest_common");
        repoInfo.setDisplayName("Test 1");
        repoInfo.setDescription("description");
        repoInfo.setReadQueueDurable(true);
        repoInfo.setWriteQueueDurable(true);
        repoInfo.setWriteQueueWorkerCount(10);
        repoInfo.setWriteQueueMaxMemory(1);
        repoInfo.setWriteQueueMaxMemoryPolicy(QueueMaxMemoryPolicy.BLOCK);
        repoInfo.setWriteQueuePageSize(2);
        repoInfo.setMaxKeywords(100);

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
        WebServerManager.getInstance().start();

        // Login
        _adminAuthToken = ApiUtils.login("RepositoryInfoTest_Admin", "hello");
        _analystAuthToken = ApiUtils.login("RepositoryInfoTest_Analyst", "hello");
    }

    @AfterClass
    public static void classTeardown()
    {
        // Clean up old user test data if any exists
        DBCollection coll = _db.getCollection(UserController.MONGODB_COLLECTION_NAME);
        Pattern pattern = Pattern.compile("^RepositoryInfoTest[\\w]*$");
        DBObject query = new BasicDBObject();
        query.put("username", pattern);
        coll.remove(query);

        // Clean up old repository test data if any exists
        coll = _db.getCollection(RepositoryInfoController.MONGODB_COLLECTION_NAME);
        pattern = Pattern.compile("^RepositoryInfoTest[\\w]*$");
        query = new BasicDBObject();
        query.put("name", pattern);
        coll.remove(query);

        WebServerManager.getInstance().stop();
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
                _adminAuthToken);

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
        createRepoInfoAO.setName("RepositoryInfoTest_1");
        createRepoInfoAO.setDisplayName("Repository Test 1");
        createRepoInfoAO.setDescription("description");
        createRepoInfoAO.setStartupStatus(Status.ONLINE);
        createRepoInfoAO.setReadQueueDurable(true);
        createRepoInfoAO.setWriteQueueDurable(true);
        createRepoInfoAO.setWriteQueueWorkerCount(2);
        createRepoInfoAO.setWriteQueueMaxMemory(10);
        createRepoInfoAO.setWriteQueueMaxMemoryPolicy(QueueMaxMemoryPolicy.BLOCK);
        createRepoInfoAO.setWriteQueuePageSize(2);
        createRepoInfoAO.setMaxKeywords(100);

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

        OutputStreamWriter out = new OutputStreamWriter(httpConn.getOutputStream());
        JsonTranslator.getInstance().toJson(createRepoInfoAO, out);
        out.close();

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        RepositoryInfoAO createResponseAO = JsonTranslator.getInstance().fromJson(responseContent.toString(),
                RepositoryInfoAO.class);
        assertEquals("RepositoryInfoTest_1", createResponseAO.getName());
        assertEquals("Repository Test 1", createResponseAO.getDisplayName());
        assertEquals("description", createResponseAO.getDescription());
        assertEquals(Status.ONLINE, createResponseAO.getStartupStatus());
        assertEquals(true, createResponseAO.isReadQueueDurable());
        assertEquals(true, createResponseAO.isWriteQueueDurable());
        assertEquals(2, createResponseAO.getWriteQueueWorkerCount());
        assertEquals(10, createResponseAO.getWriteQueueMaxMemory());
        assertEquals(QueueMaxMemoryPolicy.BLOCK, createResponseAO.getWriteQueueMaxMemoryPolicy());
        assertEquals(2, createResponseAO.getWriteQueuePageSize());
        assertEquals(100L, createResponseAO.getMaxKeywords());
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
                _adminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        RepositoryInfoAO readResponseAO = JsonTranslator.getInstance().fromJson(responseContent.toString(),
                RepositoryInfoAO.class);
        assertEquals("RepositoryInfoTest_1", readResponseAO.getName());
        assertEquals("Repository Test 1", readResponseAO.getDisplayName());
        assertEquals("description", readResponseAO.getDescription());
        assertEquals(Status.ONLINE, readResponseAO.getStartupStatus());
        assertEquals(true, readResponseAO.isReadQueueDurable());
        assertEquals(true, readResponseAO.isWriteQueueDurable());
        assertEquals(2, readResponseAO.getWriteQueueWorkerCount());
        assertEquals(10, readResponseAO.getWriteQueueMaxMemory());
        assertEquals(QueueMaxMemoryPolicy.BLOCK, readResponseAO.getWriteQueueMaxMemoryPolicy());
        assertEquals(2, readResponseAO.getWriteQueuePageSize());
        assertEquals(100L, createResponseAO.getMaxKeywords());

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
                _adminAuthToken);

        readResponseAO.setName("RepositoryInfoTest_1_update");
        readResponseAO.setMaxKeywords(200);
        readResponseAO.getParsers()[0].setMaxKeywords(201);
        readResponseAO.getParsers()[0].setFields(new RepositoryFieldInfoAO[]
        { f1 });
        readResponseAO.getParsers()[0].setProperties(new RepositoryPropertyInfoAO[]
        { new RepositoryPropertyInfoAO("1", "2") });

        out = new OutputStreamWriter(httpConn.getOutputStream());
        JsonTranslator.getInstance().toJson(readResponseAO, out);
        out.close();

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        RepositoryInfoAO updateResponseAO = JsonTranslator.getInstance().fromJson(responseContent.toString(),
                RepositoryInfoAO.class);
        assertEquals("RepositoryInfoTest_1_update", updateResponseAO.getName());
        assertEquals(200, updateResponseAO.getMaxKeywords());
        assertEquals(201, readResponseAO.getParsers()[0].getMaxKeywords());
        assertEquals(1, readResponseAO.getParsers()[0].getProperties().length);
        assertEquals(1, readResponseAO.getParsers()[0].getFields().length);

        // Get list
        httpConn = ApiUtils.getHttpURLConnection(
                "http://localhost:8989/api/repository_info?name="
                        + URLEncoder.encode("^RepositoryInfoTest[\\w]*$", "UTF-8"), HttpMethod.GET, _adminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        RepositoryInfoAO[] getListResponseAO = JsonTranslator.getInstance().fromJson(responseContent.toString(),
                RepositoryInfoAO[].class);
        assertEquals(2, getListResponseAO.length);

        // Delete
        httpConn = ApiUtils.getHttpURLConnection(
                "http://localhost:8989/api/repository_info/" + createResponseAO.getDocumentID(), HttpMethod.DELETE,
                _adminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check204NoContentResponse(responseCode.toString(), headers);

        // Get record to check if it is gone
        httpConn = ApiUtils.getHttpURLConnection(
                "http://localhost:8989/api/repository_info/" + createResponseAO.getDocumentID(), HttpMethod.GET,
                _adminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check400BadRequestResponse(responseCode.toString(), headers);

        ErrorAO errorAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), ErrorAO.class);
        assertEquals("ChiliLogException:Data.RepoInfo.NotFoundError", errorAO.getErrorCode());
    }

    /**
     * Analyst can only GET
     * 
     * @throws Exception
     */
    @Test
    public void testAnalystReadOnly() throws Exception
    {
        HttpURLConnection httpConn;
        StringBuilder responseContent = new StringBuilder();
        StringBuilder responseCode = new StringBuilder();
        HashMap<String, String> headers = new HashMap<String, String>();

        // Get list - OK
        httpConn = ApiUtils.getHttpURLConnection(
                "http://localhost:8989/api/repository_info?name="
                        + URLEncoder.encode("^RepositoryInfoTest[\\w]*$", "UTF-8"), HttpMethod.GET, _adminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        RepositoryInfoAO[] getListResponseAO = JsonTranslator.getInstance().fromJson(responseContent.toString(),
                RepositoryInfoAO[].class);
        assertEquals(1, getListResponseAO.length);

        // Create - not authroized
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repository_info", HttpMethod.POST,
                _analystAuthToken);

        RepositoryInfoAO repoInfoAO = new RepositoryInfoAO();
        repoInfoAO.setName("RepositoryInfoTest_1");
        repoInfoAO.setDisplayName("Repository Test 1");
        repoInfoAO.setDescription("description");

        OutputStreamWriter out = new OutputStreamWriter(httpConn.getOutputStream());
        JsonTranslator.getInstance().toJson(repoInfoAO, out);
        out.close();

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check401UnauthorizedResponse(responseCode.toString(), headers);

        ErrorAO errorAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), ErrorAO.class);
        assertEquals("ChiliLogException:UI.NotAuthorizedError", errorAO.getErrorCode());

        // Update
        httpConn = ApiUtils.getHttpURLConnection(
                "http://localhost:8989/api/repository_info/" + getListResponseAO[0].getDocumentID(), HttpMethod.PUT,
                _analystAuthToken);

        out = new OutputStreamWriter(httpConn.getOutputStream());
        JsonTranslator.getInstance().toJson(repoInfoAO, out);
        out.close();

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check401UnauthorizedResponse(responseCode.toString(), headers);

        errorAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), ErrorAO.class);
        assertEquals("ChiliLogException:UI.NotAuthorizedError", errorAO.getErrorCode());

        // Delete
        httpConn = ApiUtils.getHttpURLConnection(
                "http://localhost:8989/api/repository_info/" + getListResponseAO[0].getDocumentID(), HttpMethod.DELETE,
                _analystAuthToken);

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
                _adminAuthToken);

        RepositoryInfoAO repoInfoAO = new RepositoryInfoAO();
        repoInfoAO.setName("RepositoryInfoTest_3");
        repoInfoAO.setDisplayName("Repository Test 3");
        repoInfoAO.setDescription("description");

        OutputStreamWriter out = new OutputStreamWriter(httpConn.getOutputStream());
        JsonTranslator.getInstance().toJson(repoInfoAO, out);
        out.close();

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check400BadRequestResponse(responseCode.toString(), headers);

        ErrorAO errorAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), ErrorAO.class);
        assertEquals("ChiliLogException:UI.UriPathParameterError", errorAO.getErrorCode());

        // Delete
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repository_info", HttpMethod.DELETE,
                _adminAuthToken);

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
                HttpMethod.GET, _adminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check204NoContentResponse(responseCode.toString(), headers);
        assertEquals("", responseContent.toString());
        assertFalse(headers.containsKey(Worker.PAGE_COUNT_HEADER));

        // Get list - page 1
        httpConn = ApiUtils.getHttpURLConnection(
                "http://localhost:8989/api/repository_info?records_per_page=1&start_page=1&do_page_count=true&name="
                        + URLEncoder.encode("^RepositoryInfoTest[\\w]*$", "UTF-8"), HttpMethod.GET, _adminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        UserAO[] getListResponseAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), UserAO[].class);
        assertEquals(1, getListResponseAO.length);

        String pageCountHeader = headers.get(Worker.PAGE_COUNT_HEADER);
        assertEquals("1", pageCountHeader);

        // Get list - page 2 (no more records)
        httpConn = ApiUtils.getHttpURLConnection(
                "http://localhost:8989/api/repository_info?records_per_page=1&start_page=2&name="
                        + URLEncoder.encode("^RepositoryInfoTest[\\w]*$", "UTF-8"), HttpMethod.GET, _adminAuthToken);

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
                _adminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check400BadRequestResponse(responseCode.toString(), headers);

        ErrorAO errorAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), ErrorAO.class);
        assertEquals("ChiliLogException:UI.RequiredContentError", errorAO.getErrorCode());

        // Create no name
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repository_info", HttpMethod.POST,
                _adminAuthToken);

        RepositoryInfoAO createRepoInfoAO = new RepositoryInfoAO();
        // createRepoInfoAO.setName("RepositoryInfoTest_1");
        createRepoInfoAO.setDisplayName("Repository Test 1");
        createRepoInfoAO.setDescription("description");
        createRepoInfoAO.setStartupStatus(Status.ONLINE);
        createRepoInfoAO.setReadQueueDurable(true);
        createRepoInfoAO.setWriteQueueDurable(true);
        createRepoInfoAO.setWriteQueueWorkerCount(2);
        createRepoInfoAO.setWriteQueueMaxMemory(10);
        createRepoInfoAO.setWriteQueueMaxMemoryPolicy(QueueMaxMemoryPolicy.BLOCK);
        createRepoInfoAO.setWriteQueuePageSize(2);

        OutputStreamWriter out = new OutputStreamWriter(httpConn.getOutputStream());
        JsonTranslator.getInstance().toJson(createRepoInfoAO, out);
        out.close();

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check400BadRequestResponse(responseCode.toString(), headers);

        errorAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), ErrorAO.class);
        assertEquals("ChiliLogException:UI.RequiredFieldError", errorAO.getErrorCode());

        // Create no display name
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repository_info", HttpMethod.POST,
                _adminAuthToken);

        createRepoInfoAO.setName("RepositoryInfoTest_1");
        createRepoInfoAO.setDisplayName(null);

        out = new OutputStreamWriter(httpConn.getOutputStream());
        JsonTranslator.getInstance().toJson(createRepoInfoAO, out);
        out.close();

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check400BadRequestResponse(responseCode.toString(), headers);

        errorAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), ErrorAO.class);
        assertEquals("ChiliLogException:UI.RequiredFieldError", errorAO.getErrorCode());

        // Create no class name
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repository_info", HttpMethod.POST,
                _adminAuthToken);

        RepositoryParserInfoAO createRepoParserInfo = new RepositoryParserInfoAO();
        createRepoParserInfo.setName("parser1");
        createRepoParserInfo.setAppliesTo(AppliesTo.All);
        createRepoParserInfo.setClassName("");

        createRepoInfoAO.setDisplayName("Repository Test 1");
        createRepoInfoAO.setParsers(new RepositoryParserInfoAO[]
        { createRepoParserInfo });

        out = new OutputStreamWriter(httpConn.getOutputStream());
        JsonTranslator.getInstance().toJson(createRepoInfoAO, out);
        out.close();

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check400BadRequestResponse(responseCode.toString(), headers);

        errorAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), ErrorAO.class);
        assertEquals("ChiliLogException:UI.RequiredFieldError", errorAO.getErrorCode());

        // Update no content
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/repository_info/12341234", HttpMethod.PUT,
                _adminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check400BadRequestResponse(responseCode.toString(), headers);

        errorAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), ErrorAO.class);
        assertEquals("ChiliLogException:UI.RequiredContentError", errorAO.getErrorCode());

        // Update no name
        httpConn = ApiUtils.getHttpURLConnection(
                "http://localhost:8989/api/repository_info?name="
                        + URLEncoder.encode("^RepositoryInfoTest[\\w]*$", "UTF-8"), HttpMethod.GET, _adminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        RepositoryInfoAO[] getListResponseAO = JsonTranslator.getInstance().fromJson(responseContent.toString(),
                RepositoryInfoAO[].class);
        assertEquals(1, getListResponseAO.length);

        httpConn = ApiUtils.getHttpURLConnection(
                "http://localhost:8989/api/repository_info/" + getListResponseAO[0].getDocumentID(), HttpMethod.PUT,
                _adminAuthToken);

        getListResponseAO[0].setName(null);

        out = new OutputStreamWriter(httpConn.getOutputStream());
        JsonTranslator.getInstance().toJson(getListResponseAO[0], out);
        out.close();

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check400BadRequestResponse(responseCode.toString(), headers);

        errorAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), ErrorAO.class);
        assertEquals("ChiliLogException:UI.RequiredFieldError", errorAO.getErrorCode());

        // Update no doc version
        httpConn = ApiUtils.getHttpURLConnection(
                "http://localhost:8989/api/repository_info/" + getListResponseAO[0].getDocumentID(), HttpMethod.PUT,
                _adminAuthToken);

        getListResponseAO[0].setName("abc");
        getListResponseAO[0].setDocumentVersion(null);

        out = new OutputStreamWriter(httpConn.getOutputStream());
        JsonTranslator.getInstance().toJson(getListResponseAO[0], out);
        out.close();

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check400BadRequestResponse(responseCode.toString(), headers);

        errorAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), ErrorAO.class);
        assertEquals("ChiliLogException:UI.RequiredFieldError", errorAO.getErrorCode());
    }

}
