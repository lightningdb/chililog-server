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
import com.chililog.server.data.UserBO;
import com.chililog.server.data.UserBO.Status;
import com.chililog.server.data.UserController;
import com.chililog.server.ui.api.ErrorAO;
import com.chililog.server.ui.api.UserAO;
import com.chililog.server.ui.api.Worker;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

/**
 * Test the Users API
 * 
 * @author vibul
 * 
 */
public class UsersTest
{
    private static DB _db;
    private static String _adminAuthToken;
    private static String _analystAuthToken;

    @BeforeClass
    public static void classSetup() throws Exception
    {
        _db = MongoConnection.getInstance().getConnection();
        assertNotNull(_db);

        // Clean up old test data if any exists
        DBCollection coll = _db.getCollection(UserController.MONGODB_COLLECTION_NAME);
        Pattern pattern = Pattern.compile("^UsersTest[\\w]*$");
        DBObject query = new BasicDBObject();
        query.put("username", pattern);
        coll.remove(query);

        // Create admin user
        UserBO user = new UserBO();
        user.setUsername("UsersTest_Admin");
        user.setPassword("hello", true);
        user.addRole(Worker.WORKBENCH_ADMINISTRATOR_USER_ROLE);
        UserController.getInstance().save(_db, user);

        // Create analyst user
        user = new UserBO();
        user.setUsername("UsersTest_Analyst");
        user.setPassword("hello", true);
        user.addRole(Worker.WORKBENCH_ANALYST_USER_ROLE);
        UserController.getInstance().save(_db, user);

        // Start web server
        WebServerManager.getInstance().start();

        // Login
        _adminAuthToken = ApiUtils.login("UsersTest_Admin", "hello");
        _analystAuthToken = ApiUtils.login("UsersTest_Analyst", "hello");
    }

    @AfterClass
    public static void classTeardown()
    {
        // Clean up old test data if any exists
        DBCollection coll = _db.getCollection(UserController.MONGODB_COLLECTION_NAME);
        Pattern pattern = Pattern.compile("^UsersTest[\\w]*$");
        DBObject query = new BasicDBObject();
        query.put("username", pattern);
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
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/users", HttpMethod.POST, _adminAuthToken);

        UserAO createRequestAO = new UserAO();
        createRequestAO.setUsername("UsersTest_crud");
        createRequestAO.setPassword("test");
        createRequestAO.setRoles(new String[]
        { Worker.WORKBENCH_ANALYST_USER_ROLE });

        OutputStreamWriter out = new OutputStreamWriter(httpConn.getOutputStream());
        JsonTranslator.getInstance().toJson(createRequestAO, out);
        out.close();

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        UserAO createResponseAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), UserAO.class);
        assertEquals("UsersTest_crud", createResponseAO.getUsername());
        assertNull(createResponseAO.getPassword());
        assertEquals(1, createResponseAO.getRoles().length);
        assertEquals(Worker.WORKBENCH_ANALYST_USER_ROLE, createResponseAO.getRoles()[0]);
        assertNotNull(createResponseAO.getDocumentID());
        assertEquals(new Long(1), createResponseAO.getDocumentVersion());
        assertEquals(Status.Enabled, createResponseAO.getStatus());

        // Try to login
        ApiUtils.login("UsersTest_crud", "test");

        // Read one record
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/users/" + createResponseAO.getDocumentID(),
                HttpMethod.GET, _adminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        UserAO readResponseAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), UserAO.class);
        assertEquals("UsersTest_crud", readResponseAO.getUsername());
        assertNull(readResponseAO.getPassword());
        assertEquals(1, readResponseAO.getRoles().length);
        assertEquals(Worker.WORKBENCH_ANALYST_USER_ROLE, readResponseAO.getRoles()[0]);
        assertNotNull(readResponseAO.getDocumentID());
        assertEquals(new Long(1), readResponseAO.getDocumentVersion());
        assertEquals(Status.Enabled, readResponseAO.getStatus());

        // Update
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/users/" + createResponseAO.getDocumentID(),
                HttpMethod.PUT, _adminAuthToken);

        readResponseAO.setUsername("UsersTest_crud_after_update");
        readResponseAO.setRoles(new String[]
        { Worker.WORKBENCH_ANALYST_USER_ROLE, Worker.WORKBENCH_OPERATOR_USER_ROLE });
        readResponseAO.setStatus(Status.Disabled);

        out = new OutputStreamWriter(httpConn.getOutputStream());
        JsonTranslator.getInstance().toJson(readResponseAO, out);
        out.close();

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        UserAO updateResponseAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), UserAO.class);
        assertEquals("UsersTest_crud_after_update", updateResponseAO.getUsername());
        assertNull(updateResponseAO.getPassword());
        assertEquals(2, updateResponseAO.getRoles().length);
        assertEquals(Worker.WORKBENCH_ANALYST_USER_ROLE, updateResponseAO.getRoles()[0]);
        assertEquals(Worker.WORKBENCH_OPERATOR_USER_ROLE, updateResponseAO.getRoles()[1]);
        assertNotNull(updateResponseAO.getDocumentID());
        assertEquals(new Long(2), updateResponseAO.getDocumentVersion());
        assertEquals(Status.Disabled, updateResponseAO.getStatus());

        // Try to login - password not changed
        ApiUtils.login("UsersTest_crud_after_update", "test");

        // Get list
        httpConn = ApiUtils.getHttpURLConnection(
                "http://localhost:8989/api/users?username=" + URLEncoder.encode("^UsersTest[\\w]*$", "UTF-8"),
                HttpMethod.GET, _adminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        UserAO[] getListResponseAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), UserAO[].class);
        assertEquals(3, getListResponseAO.length);

        // Delete
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/users/" + createResponseAO.getDocumentID(),
                HttpMethod.DELETE, _adminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check204NoContentResponse(responseCode.toString(), headers);

        // Get record to check if it is gone
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/users/" + createResponseAO.getDocumentID(),
                HttpMethod.GET, _adminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check400BadRequestResponse(responseCode.toString(), headers);

        ErrorAO errorAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), ErrorAO.class);
        assertEquals("ChiliLogException:Data.User.NotFoundError", errorAO.getErrorCode());
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
                "http://localhost:8989/api/users?username=" + URLEncoder.encode("^UsersTest[\\w]*$", "UTF-8"),
                HttpMethod.GET, _adminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        UserAO[] getListResponseAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), UserAO[].class);
        assertEquals(2, getListResponseAO.length);

        // Create - not authroized
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/users", HttpMethod.POST, _analystAuthToken);

        UserAO createRequestAO = new UserAO();
        createRequestAO.setUsername("UsersTest_crud_notauthorised");
        createRequestAO.setPassword("test");
        createRequestAO.setRoles(new String[]
        { Worker.WORKBENCH_ANALYST_USER_ROLE });

        OutputStreamWriter out = new OutputStreamWriter(httpConn.getOutputStream());
        JsonTranslator.getInstance().toJson(createRequestAO, out);
        out.close();

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check401UnauthorizedResponse(responseCode.toString(), headers);

        ErrorAO errorAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), ErrorAO.class);
        assertEquals("ChiliLogException:UI.NotAuthorizedError", errorAO.getErrorCode());

        // Update
        httpConn = ApiUtils.getHttpURLConnection(
                "http://localhost:8989/api/users/" + getListResponseAO[0].getDocumentID(), HttpMethod.PUT,
                _analystAuthToken);

        out = new OutputStreamWriter(httpConn.getOutputStream());
        JsonTranslator.getInstance().toJson(createRequestAO, out);
        out.close();

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check401UnauthorizedResponse(responseCode.toString(), headers);

        errorAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), ErrorAO.class);
        assertEquals("ChiliLogException:UI.NotAuthorizedError", errorAO.getErrorCode());

        // Delete
        httpConn = ApiUtils.getHttpURLConnection(
                "http://localhost:8989/api/users/" + getListResponseAO[0].getDocumentID(), HttpMethod.DELETE,
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

        // Get list - OK
        httpConn = ApiUtils.getHttpURLConnection(
                "http://localhost:8989/api/users?username=" + URLEncoder.encode("^UsersTest[\\w]*$", "UTF-8"),
                HttpMethod.GET, _adminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        UserAO[] getListResponseAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), UserAO[].class);
        assertEquals(2, getListResponseAO.length);

        // Update
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/users", HttpMethod.PUT, _adminAuthToken);

        OutputStreamWriter out = new OutputStreamWriter(httpConn.getOutputStream());
        JsonTranslator.getInstance().toJson(getListResponseAO[0], out);
        out.close();

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check400BadRequestResponse(responseCode.toString(), headers);

        ErrorAO errorAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), ErrorAO.class);
        assertEquals("ChiliLogException:UI.UriPathParameterError", errorAO.getErrorCode());

        // Delete
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/users", HttpMethod.DELETE, _adminAuthToken);

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
                "http://localhost:8989/api/users?username=" + URLEncoder.encode("^xxxxxxxxx[\\w]*$", "UTF-8"),
                HttpMethod.GET, _adminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check204NoContentResponse(responseCode.toString(), headers);
        assertEquals("", responseContent.toString());

        // Get list - page 1
        httpConn = ApiUtils.getHttpURLConnection(
                "http://localhost:8989/api/users?records_per_page=1&start_page=1&username="
                        + URLEncoder.encode("^UsersTest[\\w]*$", "UTF-8"), HttpMethod.GET, _adminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        UserAO[] getListResponseAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), UserAO[].class);
        assertEquals(1, getListResponseAO.length);

        // Get list - page 2
        httpConn = ApiUtils.getHttpURLConnection(
                "http://localhost:8989/api/users?records_per_page=1&start_page=2&username="
                        + URLEncoder.encode("^UsersTest[\\w]*$", "UTF-8"), HttpMethod.GET, _adminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        getListResponseAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), UserAO[].class);
        assertEquals(1, getListResponseAO.length);
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

        // Create
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/users", HttpMethod.POST, _adminAuthToken);

        UserAO createRequestAO = new UserAO();
        createRequestAO.setUsername("UsersTest_change_password");
        createRequestAO.setPassword("test");
        createRequestAO.setRoles(new String[]
        { Worker.WORKBENCH_ANALYST_USER_ROLE });

        OutputStreamWriter out = new OutputStreamWriter(httpConn.getOutputStream());
        JsonTranslator.getInstance().toJson(createRequestAO, out);
        out.close();

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        UserAO createResponseAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), UserAO.class);
        assertEquals("UsersTest_change_password", createResponseAO.getUsername());

        // Try to login
        ApiUtils.login("UsersTest_change_password", "test");

        // Update
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/users/" + createResponseAO.getDocumentID(),
                HttpMethod.PUT, _adminAuthToken);

        createResponseAO.setPassword("newpassword");

        out = new OutputStreamWriter(httpConn.getOutputStream());
        JsonTranslator.getInstance().toJson(createResponseAO, out);
        out.close();

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        // Try to login - password changed
        ApiUtils.login("UsersTest_change_password", "newpassword");
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
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/users", HttpMethod.POST, _adminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check400BadRequestResponse(responseCode.toString(), headers);

        ErrorAO errorAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), ErrorAO.class);
        assertEquals("ChiliLogException:UI.RequiredContentError", errorAO.getErrorCode());

        // Create no username
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/users", HttpMethod.POST, _adminAuthToken);

        UserAO createRequestAO = new UserAO();
        createRequestAO.setPassword("test");
        createRequestAO.setRoles(new String[]
        { Worker.WORKBENCH_ANALYST_USER_ROLE });

        OutputStreamWriter out = new OutputStreamWriter(httpConn.getOutputStream());
        JsonTranslator.getInstance().toJson(createRequestAO, out);
        out.close();

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check400BadRequestResponse(responseCode.toString(), headers);

        errorAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), ErrorAO.class);
        assertEquals("ChiliLogException:UI.RequiredFieldError", errorAO.getErrorCode());

        // Create no password
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/users", HttpMethod.POST, _adminAuthToken);

        createRequestAO = new UserAO();
        createRequestAO.setUsername("UsersTest_bad_content");
        createRequestAO.setRoles(new String[]
        { Worker.WORKBENCH_ANALYST_USER_ROLE });

        out = new OutputStreamWriter(httpConn.getOutputStream());
        JsonTranslator.getInstance().toJson(createRequestAO, out);
        out.close();

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check400BadRequestResponse(responseCode.toString(), headers);

        errorAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), ErrorAO.class);
        assertEquals("ChiliLogException:UI.RequiredFieldError", errorAO.getErrorCode());

        // Update no content
        httpConn = ApiUtils.getHttpURLConnection("http://localhost:8989/api/users/12341234", HttpMethod.PUT,
                _adminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check400BadRequestResponse(responseCode.toString(), headers);

        errorAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), ErrorAO.class);
        assertEquals("ChiliLogException:UI.RequiredContentError", errorAO.getErrorCode());

        // Update no username
        httpConn = ApiUtils.getHttpURLConnection(
                "http://localhost:8989/api/users?records_per_page=1&start_page=1&username="
                        + URLEncoder.encode("^UsersTest[\\w]*$", "UTF-8"), HttpMethod.GET, _adminAuthToken);

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check200OKResponse(responseCode.toString(), headers);

        UserAO[] getListResponseAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), UserAO[].class);
        assertEquals(1, getListResponseAO.length);

        httpConn = ApiUtils.getHttpURLConnection(
                "http://localhost:8989/api/users/" + getListResponseAO[0].getDocumentID(), HttpMethod.PUT,
                _adminAuthToken);

        getListResponseAO[0].setUsername(null);

        out = new OutputStreamWriter(httpConn.getOutputStream());
        JsonTranslator.getInstance().toJson(getListResponseAO[0], out);
        out.close();

        ApiUtils.getResponse(httpConn, responseContent, responseCode, headers);
        ApiUtils.check400BadRequestResponse(responseCode.toString(), headers);

        errorAO = JsonTranslator.getInstance().fromJson(responseContent.toString(), ErrorAO.class);
        assertEquals("ChiliLogException:UI.RequiredFieldError", errorAO.getErrorCode());

        // Update no doc version
        httpConn = ApiUtils.getHttpURLConnection(
                "http://localhost:8989/api/users/" + getListResponseAO[0].getDocumentID(), HttpMethod.PUT,
                _adminAuthToken);

        getListResponseAO[0].setUsername("abc");
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
