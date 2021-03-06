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

package org.chililog.server.pubsub;

import static org.junit.Assert.*;

import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.regex.Pattern;

import org.chililog.server.common.JsonTranslator;
import org.chililog.server.common.Log4JLogger;
import org.chililog.server.data.MongoConnection;
import org.chililog.server.data.RepositoryConfigBO;
import org.chililog.server.data.RepositoryConfigController;
import org.chililog.server.data.RepositoryEntryBO;
import org.chililog.server.data.UserBO;
import org.chililog.server.data.UserController;
import org.chililog.server.engine.MqService;
import org.chililog.server.engine.RepositoryService;
import org.chililog.server.pubsub.jsonhttp.JsonHttpService;
import org.chililog.server.pubsub.jsonhttp.LogEntryAO;
import org.chililog.server.pubsub.jsonhttp.PublicationRequestAO;
import org.chililog.server.pubsub.jsonhttp.PublicationResponseAO;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

/**
 * Test JSON HTTP Request publication
 * 
 * @author vibul
 * 
 */
public class JsonHttpPubishTest {

    private static DB _db;
    private static RepositoryConfigBO _repoInfo;

    private static final String REPOSITORY_NAME = "json_http_publish_test";
    private static final String MONGODB_COLLECTION_NAME = "repo_json_http_publish_test";

    @BeforeClass
    public static void classSetup() throws Exception {
        // Create repo
        _repoInfo = new RepositoryConfigBO();
        _repoInfo.setName(REPOSITORY_NAME);
        _repoInfo.setDisplayName("Json Http Pubish Test");
        _repoInfo.setStoreEntriesIndicator(true);
        _repoInfo.setStorageQueueDurableIndicator(false);
        _repoInfo.setStorageQueueWorkerCount(2);

        // Database
        _db = MongoConnection.getInstance().getConnection();
        assertNotNull(_db);

        // Clean up old users
        DBCollection coll = _db.getCollection(UserController.MONGODB_COLLECTION_NAME);
        Pattern pattern = Pattern.compile("^JsonHttpPublishTestUser[\\w]*$");
        DBObject query = new BasicDBObject();
        query.put("username", pattern);
        coll.remove(query);

        // Clean old repository info
        coll = _db.getCollection(RepositoryConfigController.MONGODB_COLLECTION_NAME);
        pattern = Pattern.compile("^" + REPOSITORY_NAME + "$");
        query = new BasicDBObject();
        query.put("name", pattern);
        coll.remove(query);

        // Clean up old test data if any exists
        coll = _db.getCollection(MONGODB_COLLECTION_NAME);
        if (coll != null) {
            coll.drop();
        }

        // Create repository record
        RepositoryConfigController.getInstance().save(_db, _repoInfo);

        // Create user that cannot access this repository
        UserBO user = new UserBO();
        user.setUsername("JsonHttpPublishTestUser_NoAccess");
        user.setPassword("111", true);
        UserController.getInstance().save(_db, user);

        // Create publisher user
        user = new UserBO();
        user.setUsername("JsonHttpPublishTestUser_Publisher");
        user.setPassword("222", true);
        user.addRole(_repoInfo.getPublisherRoleName());
        UserController.getInstance().save(_db, user);

        // Create subscriber user
        user = new UserBO();
        user.setUsername("JsonHttpPublishTestUser_Subscriber");
        user.setPassword("333", true);
        user.addRole(_repoInfo.getSubscriberRoleName());
        UserController.getInstance().save(_db, user);

        // Start it up
        MqService.getInstance().start();
        RepositoryService.getInstance().start();
        JsonHttpService.getInstance().start();
    }

    @Before
    public void testSetup() throws Exception {
        // Drop collection for each test
        DBCollection coll = _db.getCollection(MONGODB_COLLECTION_NAME);
        if (coll != null) {
            coll.drop();
        }
    }

    @AfterClass
    public static void classTeardown() throws Exception {
        // Stop it all
        JsonHttpService.getInstance().stop();
        RepositoryService.getInstance().stop();
        MqService.getInstance().stop();

        // Drop collection
        DBCollection coll = _db.getCollection(MONGODB_COLLECTION_NAME);
        if (coll != null) {
            coll.drop();
        }

        // Drop users
        coll = _db.getCollection(UserController.MONGODB_COLLECTION_NAME);
        Pattern pattern = Pattern.compile("^JsonHttpPublishTestUser[\\w]*$");
        DBObject query = new BasicDBObject();
        query.put("username", pattern);
        coll.remove(query);

        // Clean old repository info
        coll = _db.getCollection(RepositoryConfigController.MONGODB_COLLECTION_NAME);
        pattern = Pattern.compile("^" + REPOSITORY_NAME + "$");
        query = new BasicDBObject();
        query.put("name", pattern);
        coll.remove(query);
    }

    /**
     * Send valid log entry request to the server for processing
     * 
     * @param msgID
     *            message ID
     * @param entryCount
     *            Number of log entries to send
     * @param includePreparsedFields
     *            If true, fields will be sent
     * @throws Exception
     *             if error
     */
    public static void sendPublishRequest(String msgID, int entryCount, boolean includePreparsedFields)
            throws Exception {
        HttpURLConnection httpConn;
        StringBuilder responseContent = new StringBuilder();
        StringBuilder responseCode = new StringBuilder();
        HashMap<String, String> headers = new HashMap<String, String>();

        // Create
        httpConn = TestUtils.getHttpURLConnection("http://localhost:61615/publish", HttpMethod.POST);

        PublicationRequestAO request = new PublicationRequestAO();
        request.setMessageID(msgID);
        request.setUsername("JsonHttpPublishTestUser_Publisher");
        request.setPassword("222");
        request.setRepositoryName(REPOSITORY_NAME);

        LogEntryAO[] logEntries = new LogEntryAO[entryCount];

        StringBuilder preparsedFields = new StringBuilder();
        preparsedFields.append("{");
        preparsedFields.append("\"fld_field1\": 1,"); // Integer
        preparsedFields.append("\"fld_field2\": \"abc\","); // String
        preparsedFields.append("\"fld_field3\": true,"); // Boolean
        preparsedFields.append("\"fld_field4\": 8888888888,"); // Long. 10 - digit numbers converts to long
        preparsedFields.append("\"fld_field5\": \"NumberLong(888)\",");
        preparsedFields.append("\"fld_field6\": 5.5,"); // Double
        preparsedFields.append("\"fld_field7\": \"2010-11-29T19:41:46.000Z\",");
        preparsedFields.append("}");

        for (int i = 0; i < entryCount; i++) {
            LogEntryAO logEntry = new LogEntryAO();
            logEntry.setTimestamp("2011-01-01T00:00:00.000Z");
            logEntry.setSource("junit");
            logEntry.setHost("localhost");
            logEntry.setSeverity("4");
            if (includePreparsedFields) {
                logEntry.setFields(preparsedFields.toString());
            }
            logEntry.setMessage("test message " + i);
            logEntries[i] = logEntry;
        }

        request.setLogEntries(logEntries);

        TestUtils.postJSON(httpConn, request);
        TestUtils.getResponse(httpConn, responseContent, responseCode, headers);
        TestUtils.check200OKResponse(responseCode.toString(), headers);

        PublicationResponseAO response = JsonTranslator.getInstance().fromJson(responseContent.toString(),
                PublicationResponseAO.class);
        assertEquals(msgID, response.getMessageID());
        assertTrue(response.isSuccess());
        assertNull(response.getErrorMessage());
        assertNull(response.getErrorStackTrace());
    }

    @Test
    public void testOneLogEntry() throws Exception {
        sendPublishRequest("testOneLogEntry", 1, true);

        // Wait a moment for log entry to be processed
        Thread.sleep(1000);

        // Check that the entry is written to the log
        DBCollection coll = _db.getCollection(MONGODB_COLLECTION_NAME);
        assertEquals(1, coll.find().count());

        DBObject dbObject = coll.findOne();

        assertTrue(dbObject.containsField(RepositoryEntryBO.TIMESTAMP_FIELD_NAME));
        assertTrue(dbObject.containsField(RepositoryEntryBO.SAVED_TIMESTAMP_FIELD_NAME));
        assertEquals("junit", dbObject.get(RepositoryEntryBO.SOURCE_FIELD_NAME));
        assertEquals("localhost", dbObject.get(RepositoryEntryBO.HOST_FIELD_NAME));
        assertEquals(4L, dbObject.get(RepositoryEntryBO.SEVERITY_FIELD_NAME));
        assertTrue(dbObject.containsField(RepositoryEntryBO.MESSAGE_FIELD_NAME));

        assertEquals(1, dbObject.get("fld_field1"));
        assertEquals("abc", dbObject.get("fld_field2"));
        assertEquals(true, dbObject.get("fld_field3"));
        assertEquals(8888888888L, dbObject.get("fld_field4"));
        assertEquals(888L, dbObject.get("fld_field5"));
        assertEquals(5.5d, dbObject.get("fld_field6"));
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        assertEquals(sf.parse("2010-11-29T19:41:46GMT"), dbObject.get("fld_field7"));
    }

    /**
     * Test 2 requests on same Keep Alive connection
     * 
     * @throws Exception
     */
    @Test
    public void testSubsequentRequests() throws Exception {
        sendPublishRequest("testSubsequentRequests", 1, false);
        sendPublishRequest("testSubsequentRequests", 2, false);
        sendPublishRequest("testSubsequentRequests", 3, false);

        // Wait a moment for log entry to be processed
        Thread.sleep(1000);

        // Check that the entry is written to the log
        DBCollection coll = _db.getCollection(MONGODB_COLLECTION_NAME);
        assertEquals(6, coll.find().count());
    }

    @Test
    public void testManyLogEntries() throws Exception {
        sendPublishRequest("testManyLogEntries", 100, false);

        // Wait a moment for log entry to be processed
        Thread.sleep(2000);

        // Check that the entry is written to the log
        DBCollection coll = _db.getCollection(MONGODB_COLLECTION_NAME);
        assertEquals(100, coll.find().count());
    }

    @Test
    public void testMultipleConnections() throws Exception {
        // 20 threads each adding 2 log entries = 40 log entries in total
        for (int i = 0; i < 20; i++) {
            PublishThread runnable = new PublishThread();
            Thread thread = new Thread(runnable);
            thread.start();
        }

        // Wait a moment for log entry to be processed
        Thread.sleep(5000);

        // Check that the entry is written to the log
        DBCollection coll = _db.getCollection(MONGODB_COLLECTION_NAME);
        assertEquals(40, coll.find().count());

    }

    @Test
    public void testBadUsername() throws Exception {
        HttpURLConnection httpConn;
        StringBuilder responseContent = new StringBuilder();
        StringBuilder responseCode = new StringBuilder();
        HashMap<String, String> headers = new HashMap<String, String>();

        // Create
        httpConn = TestUtils.getHttpURLConnection("http://localhost:61615/publish", HttpMethod.POST);

        PublicationRequestAO request = new PublicationRequestAO();
        request.setMessageID("testBadUsername");
        request.setUsername("XXX");
        request.setPassword("222");
        request.setRepositoryName(REPOSITORY_NAME);

        LogEntryAO[] logEntries = new LogEntryAO[1];

        LogEntryAO logEntry = new LogEntryAO();
        logEntry.setTimestamp("2011-01-01T00:00:00.000Z");
        logEntry.setSource("junit");
        logEntry.setHost("localhost");
        logEntry.setSeverity("4");
        logEntry.setMessage("test message 1");
        logEntries[0] = logEntry;

        request.setLogEntries(logEntries);

        TestUtils.postJSON(httpConn, request);
        TestUtils.getResponse(httpConn, responseContent, responseCode, headers);
        TestUtils.check400BadRequestResponse(responseCode.toString(), headers);

        PublicationResponseAO response = JsonTranslator.getInstance().fromJson(responseContent.toString(),
                PublicationResponseAO.class);
        assertEquals("testBadUsername", response.getMessageID());
        assertFalse(response.isSuccess());
        assertEquals("Cannot find user 'XXX'.", response.getErrorMessage());
    }

    @Test
    public void testBadPassword() throws Exception {
        HttpURLConnection httpConn;
        StringBuilder responseContent = new StringBuilder();
        StringBuilder responseCode = new StringBuilder();
        HashMap<String, String> headers = new HashMap<String, String>();

        // Create
        httpConn = TestUtils.getHttpURLConnection("http://localhost:61615/publish", HttpMethod.POST);

        PublicationRequestAO request = new PublicationRequestAO();
        request.setMessageID("testBadPassword");
        request.setUsername("JsonHttpPublishTestUser_Publisher");
        request.setPassword("xxx");
        request.setRepositoryName(REPOSITORY_NAME);

        LogEntryAO[] logEntries = new LogEntryAO[1];

        LogEntryAO logEntry = new LogEntryAO();
        logEntry.setTimestamp("2011-01-01T00:00:00.000Z");
        logEntry.setSource("junit");
        logEntry.setHost("localhost");
        logEntry.setSeverity("4");
        logEntry.setMessage("test message 1");
        logEntries[0] = logEntry;

        request.setLogEntries(logEntries);

        TestUtils.postJSON(httpConn, request);
        TestUtils.getResponse(httpConn, responseContent, responseCode, headers);
        TestUtils.check400BadRequestResponse(responseCode.toString(), headers);

        PublicationResponseAO response = JsonTranslator.getInstance().fromJson(responseContent.toString(),
                PublicationResponseAO.class);
        assertEquals("testBadPassword", response.getMessageID());
        assertFalse(response.isSuccess());
        assertEquals("Access denied.", response.getErrorMessage());
    }

    @Test
    public void testBadRole() throws Exception {
        HttpURLConnection httpConn;
        StringBuilder responseContent = new StringBuilder();
        StringBuilder responseCode = new StringBuilder();
        HashMap<String, String> headers = new HashMap<String, String>();

        // Create
        httpConn = TestUtils.getHttpURLConnection("http://localhost:61615/publish", HttpMethod.POST);

        PublicationRequestAO request = new PublicationRequestAO();
        request.setMessageID("testBadRole");
        request.setUsername("JsonHttpPublishTestUser_NoAccess");
        request.setPassword("111");
        request.setRepositoryName(REPOSITORY_NAME);

        LogEntryAO[] logEntries = new LogEntryAO[1];

        LogEntryAO logEntry = new LogEntryAO();
        logEntry.setTimestamp("2011-01-01T00:00:00.000Z");
        logEntry.setSource("junit");
        logEntry.setHost("localhost");
        logEntry.setSeverity("4");
        logEntry.setMessage("test message 1");
        logEntries[0] = logEntry;

        request.setLogEntries(logEntries);

        TestUtils.postJSON(httpConn, request);
        TestUtils.getResponse(httpConn, responseContent, responseCode, headers);
        TestUtils.check400BadRequestResponse(responseCode.toString(), headers);

        PublicationResponseAO response = JsonTranslator.getInstance().fromJson(responseContent.toString(),
                PublicationResponseAO.class);
        assertEquals("testBadRole", response.getMessageID());
        assertFalse(response.isSuccess());
        assertEquals("Access denied.", response.getErrorMessage());
    }

    /**
     * Thread for running in testMultipleConnections
     * 
     * @author vibul
     * 
     */
    public static class PublishThread implements Runnable {

        private static Log4JLogger _logger = Log4JLogger.getLogger(PublishThread.class);

        public void run() {
            try {
                _logger.debug("HTTP thread " + Thread.currentThread().getName() + " started");
                sendPublishRequest("PublishThread " + Thread.currentThread().getName(), 2, false);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
