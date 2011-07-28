// Copyright 2010 Cinch Logic Pty Ltd.

package com.chililog.server.pubsub;

import static org.junit.Assert.*;

import java.net.URI;
import java.util.regex.Pattern;

import org.jboss.netty.handler.codec.http.websocket.DefaultWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocket.WebSocketFrame;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.chililog.client.websocket.hybi00.WebSocketCallback;
import com.chililog.client.websocket.hybi00.WebSocketClient;
import com.chililog.client.websocket.hybi00.WebSocketClientFactory;
import com.chililog.server.common.JsonTranslator;
import com.chililog.server.common.Log4JLogger;
import com.chililog.server.data.MongoConnection;
import com.chililog.server.data.RepositoryInfoBO;
import com.chililog.server.data.RepositoryInfoController;
import com.chililog.server.data.UserBO;
import com.chililog.server.data.UserController;
import com.chililog.server.engine.MqService;
import com.chililog.server.engine.RepositoryService;
import com.chililog.server.pubsub.jsonhttp.JsonHttpService;
import com.chililog.server.pubsub.jsonhttp.LogEntryAO;
import com.chililog.server.pubsub.jsonhttp.PublicationRequestAO;
import com.chililog.server.pubsub.jsonhttp.PublicationResponseAO;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

/**
 * Test web socket standard hybi 00
 * 
 * @author vibul
 * 
 */
public class JsonWebSocketHybi00Test
{
    private static DB _db;
    private static RepositoryInfoBO _repoInfo;

    private static final String REPOSITORY_NAME = "json_ws_test";
    private static final String MONGODB_COLLECTION_NAME = "repo_json_ws_test";

    @BeforeClass
    public static void classSetup() throws Exception
    {
        // Create repo
        _repoInfo = new RepositoryInfoBO();
        _repoInfo.setName(REPOSITORY_NAME);
        _repoInfo.setDisplayName("Json Web Socket Test");
        _repoInfo.setStoreEntriesIndicator(true);
        _repoInfo.setStorageQueueDurableIndicator(false);
        _repoInfo.setStorageQueueWorkerCount(2);

        // Database
        _db = MongoConnection.getInstance().getConnection();
        assertNotNull(_db);

        // Clean up old users
        DBCollection coll = _db.getCollection(UserController.MONGODB_COLLECTION_NAME);
        Pattern pattern = Pattern.compile("^JsonWsTestUser[\\w]*$");
        DBObject query = new BasicDBObject();
        query.put("username", pattern);
        coll.remove(query);

        // Clean old repository info
        coll = _db.getCollection(RepositoryInfoController.MONGODB_COLLECTION_NAME);
        pattern = Pattern.compile("^" + REPOSITORY_NAME + "$");
        query = new BasicDBObject();
        query.put("name", pattern);
        coll.remove(query);

        // Clean up old test data if any exists
        coll = _db.getCollection(MONGODB_COLLECTION_NAME);
        if (coll != null)
        {
            coll.drop();
        }

        // Create repository record
        RepositoryInfoController.getInstance().save(_db, _repoInfo);

        // Create user that cannot access this repository
        UserBO user = new UserBO();
        user.setUsername("JsonWsTestUser_NoAccess");
        user.setPassword("111", true);
        UserController.getInstance().save(_db, user);

        // Create publisher user
        user = new UserBO();
        user.setUsername("JsonWsTestUser_Publisher");
        user.setPassword("222", true);
        user.addRole(_repoInfo.getPublisherRoleName());
        UserController.getInstance().save(_db, user);

        // Create subscriber user
        user = new UserBO();
        user.setUsername("JsonWsTestUser_Subscriber");
        user.setPassword("333", true);
        user.addRole(_repoInfo.getSubscriberRoleName());
        UserController.getInstance().save(_db, user);

        // Start it up
        MqService.getInstance().start();
        RepositoryService.getInstance().start(true);
        JsonHttpService.getInstance().start();
    }

    @Before
    public void testSetup() throws Exception
    {
        // Drop collection for each test
        DBCollection coll = _db.getCollection(MONGODB_COLLECTION_NAME);
        if (coll != null)
        {
            coll.drop();
        }
    }

    @AfterClass
    public static void classTeardown() throws Exception
    {
        // Stop it all
        JsonHttpService.getInstance().stop();
        RepositoryService.getInstance().stop();
        MqService.getInstance().stop();

        // Drop collection
        DBCollection coll = _db.getCollection(MONGODB_COLLECTION_NAME);
        if (coll != null)
        {
            coll.drop();
        }
    }

    /**
     * Sends a publish request over ws
     * 
     * @param client
     *            Web socket client
     * @param callbackHandler
     *            Callback handler to handle incoming responses
     * @param msgID
     *            unique message id
     * @param entryCount
     *            number of log entries to send
     * @throws Exception
     */
    public static void sendPublicshRequest(WebSocketClient client,
                                           MyWebSocketCallbackHandler callbackHandler,
                                           String msgID,
                                           int entryCount) throws Exception
    {
        PublicationRequestAO request = new PublicationRequestAO();
        request.setMessageID(msgID);
        request.setUsername("JsonWsTestUser_Publisher");
        request.setPassword("222");
        request.setRepositoryName(REPOSITORY_NAME);

        LogEntryAO[] logEntries = new LogEntryAO[entryCount];

        for (int i = 0; i < entryCount; i++)
        {
            LogEntryAO logEntry = new LogEntryAO();
            logEntry.setTimestamp("2011-01-01T00:00:00.000Z");
            logEntry.setSource("junit");
            logEntry.setHost("localhost");
            logEntry.setSeverity("4");
            logEntry.setMessage("test message " + i);
            logEntries[i] = logEntry;
        }
        request.setLogEntries(logEntries);

        // Send request
        callbackHandler.messageReceived = null;
        String requestJson = JsonTranslator.getInstance().toJson(request);
        client.send(new DefaultWebSocketFrame(requestJson));

        // Wait for it to be processed
        Thread.sleep(1000);

        // Check response
        assertNotNull(callbackHandler.messageReceived);
        PublicationResponseAO response = JsonTranslator.getInstance().fromJson(callbackHandler.messageReceived,
                PublicationResponseAO.class);
        assertEquals(msgID, response.getMessageID());
        assertTrue(response.isSuccess());
        assertNull(response.getErrorMessage());
        assertNull(response.getErrorStackTrace());
    }

    @Test
    public void testPublishOneLogEntry() throws Exception
    {
        MyWebSocketCallbackHandler callbackHandler = new MyWebSocketCallbackHandler();
        WebSocketClientFactory factory = new WebSocketClientFactory();

        WebSocketClient client = factory.newClient(new URI("ws://localhost:61615/websocket-hybi-00"), callbackHandler);

        // Connect
        client.connect().awaitUninterruptibly();
        Thread.sleep(500);
        assertTrue(callbackHandler.connected);

        // Publish
        sendPublicshRequest(client, callbackHandler, "testPublishOneLogEntry", 1);

        // Disconnect
        // Test that all is OK when shutting down server with open connections

        // Check database
        DBCollection coll = _db.getCollection(MONGODB_COLLECTION_NAME);
        assertEquals(1, coll.find().count());

    }

    @Test
    public void testPublishManyLogEntries() throws Exception
    {
        MyWebSocketCallbackHandler callbackHandler = new MyWebSocketCallbackHandler();
        WebSocketClientFactory factory = new WebSocketClientFactory();

        WebSocketClient client = factory.newClient(new URI("ws://localhost:61615/websocket-hybi-00"), callbackHandler);

        // Connect
        client.connect().awaitUninterruptibly();
        Thread.sleep(500);
        assertTrue(callbackHandler.connected);

        // Publish
        sendPublicshRequest(client, callbackHandler, "testPublishManyLogEntries", 10);

        // Disconnect - check that all is OK if shutdown server with no open connections
        client.disconnect();

        // Check database
        DBCollection coll = _db.getCollection(MONGODB_COLLECTION_NAME);
        assertEquals(10, coll.find().count());

    }

    @Test
    public void testPublishSubsequentLogEntries() throws Exception
    {
        MyWebSocketCallbackHandler callbackHandler = new MyWebSocketCallbackHandler();
        WebSocketClientFactory factory = new WebSocketClientFactory();

        WebSocketClient client = factory.newClient(new URI("ws://localhost:61615/websocket-hybi-00"), callbackHandler);

        // Connect
        client.connect().awaitUninterruptibly();
        Thread.sleep(500);
        assertTrue(callbackHandler.connected);

        // Publish
        sendPublicshRequest(client, callbackHandler, "testPublishSubsequentLogEntries", 1);
        sendPublicshRequest(client, callbackHandler, "testPublishSubsequentLogEntries", 2);
        sendPublicshRequest(client, callbackHandler, "testPublishSubsequentLogEntries", 3);

        // Disconnect - check that all is OK if shutdown server with no open connections
        client.disconnect();

        // Check database
        DBCollection coll = _db.getCollection(MONGODB_COLLECTION_NAME);
        assertEquals(6, coll.find().count());
    }

    @Test
    public void testMultipleConnections() throws Exception
    {
        // 20 threads each adding 2 log entries = 40 log entries in total
        for (int i = 0; i < 20; i++)
        {
            PublishThread runnable = new PublishThread();
            Thread thread = new Thread(runnable);
            thread.start();
        }

        // Wait a moment for log entry to be processed
        Thread.sleep(3000);

        // Check that the entry is written to the log
        DBCollection coll = _db.getCollection(MONGODB_COLLECTION_NAME);
        assertEquals(40, coll.find().count());

    }

    @Test
    public void testBadUsername() throws Exception
    {
        MyWebSocketCallbackHandler callbackHandler = new MyWebSocketCallbackHandler();
        WebSocketClientFactory factory = new WebSocketClientFactory();

        WebSocketClient client = factory.newClient(new URI("ws://localhost:61615/websocket-hybi-00"), callbackHandler);

        // Connect
        client.connect().awaitUninterruptibly();
        Thread.sleep(500);
        assertTrue(callbackHandler.connected);

        // Prepare request
        PublicationRequestAO request = new PublicationRequestAO();
        request.setMessageID("testBadUsername");
        request.setUsername("XXX");
        request.setPassword("222");
        request.setRepositoryName(REPOSITORY_NAME);

        LogEntryAO[] logEntries = new LogEntryAO[1];
        for (int i = 0; i < 1; i++)
        {
            LogEntryAO logEntry = new LogEntryAO();
            logEntry.setTimestamp("2011-01-01T00:00:00.000Z");
            logEntry.setSource("junit");
            logEntry.setHost("localhost");
            logEntry.setSeverity("4");
            logEntry.setMessage("test message " + i);
            logEntries[i] = logEntry;
        }
        request.setLogEntries(logEntries);

        // Send request
        callbackHandler.messageReceived = null;
        String requestJson = JsonTranslator.getInstance().toJson(request);
        client.send(new DefaultWebSocketFrame(requestJson));

        // Wait for it to be processed
        Thread.sleep(1000);

        // Disconnect
        client.disconnect();

        // Check response
        assertNotNull(callbackHandler.messageReceived);
        PublicationResponseAO response = JsonTranslator.getInstance().fromJson(callbackHandler.messageReceived,
                PublicationResponseAO.class);
        assertEquals("testBadUsername", response.getMessageID());
        assertFalse(response.isSuccess());
        assertNotNull(response.getErrorMessage());
        assertNotNull(response.getErrorStackTrace());
        assertEquals("Cannot find user 'XXX'.", response.getErrorMessage());
    }
    
    @Test
    public void testBadPassword() throws Exception
    {
        MyWebSocketCallbackHandler callbackHandler = new MyWebSocketCallbackHandler();
        WebSocketClientFactory factory = new WebSocketClientFactory();

        WebSocketClient client = factory.newClient(new URI("ws://localhost:61615/websocket-hybi-00"), callbackHandler);

        // Connect
        client.connect().awaitUninterruptibly();
        Thread.sleep(500);
        assertTrue(callbackHandler.connected);

        // Prepare request
        PublicationRequestAO request = new PublicationRequestAO();
        request.setMessageID("testBadPassword");
        request.setUsername("JsonWsTestUser_Publisher");
        request.setPassword("bad");
        request.setRepositoryName(REPOSITORY_NAME);

        LogEntryAO[] logEntries = new LogEntryAO[1];
        for (int i = 0; i < 1; i++)
        {
            LogEntryAO logEntry = new LogEntryAO();
            logEntry.setTimestamp("2011-01-01T00:00:00.000Z");
            logEntry.setSource("junit");
            logEntry.setHost("localhost");
            logEntry.setSeverity("4");
            logEntry.setMessage("test message " + i);
            logEntries[i] = logEntry;
        }
        request.setLogEntries(logEntries);

        // Send request
        callbackHandler.messageReceived = null;
        String requestJson = JsonTranslator.getInstance().toJson(request);
        client.send(new DefaultWebSocketFrame(requestJson));

        // Wait for it to be processed
        Thread.sleep(1000);

        // Disconnect
        client.disconnect();

        // Check response
        assertNotNull(callbackHandler.messageReceived);
        PublicationResponseAO response = JsonTranslator.getInstance().fromJson(callbackHandler.messageReceived,
                PublicationResponseAO.class);
        assertEquals("testBadPassword", response.getMessageID());
        assertFalse(response.isSuccess());
        assertNotNull(response.getErrorMessage());
        assertNotNull(response.getErrorStackTrace());
        assertEquals("Access denied.", response.getErrorMessage());
    }
    
    @Test
    public void testBadRole() throws Exception
    {
        MyWebSocketCallbackHandler callbackHandler = new MyWebSocketCallbackHandler();
        WebSocketClientFactory factory = new WebSocketClientFactory();

        WebSocketClient client = factory.newClient(new URI("ws://localhost:61615/websocket-hybi-00"), callbackHandler);

        // Connect
        client.connect().awaitUninterruptibly();
        Thread.sleep(500);
        assertTrue(callbackHandler.connected);

        // Prepare request
        PublicationRequestAO request = new PublicationRequestAO();
        request.setMessageID("testBadRole");
        request.setUsername("JsonWsTestUser_NoAccess");
        request.setPassword("111");
        request.setRepositoryName(REPOSITORY_NAME);

        LogEntryAO[] logEntries = new LogEntryAO[1];
        for (int i = 0; i < 1; i++)
        {
            LogEntryAO logEntry = new LogEntryAO();
            logEntry.setTimestamp("2011-01-01T00:00:00.000Z");
            logEntry.setSource("junit");
            logEntry.setHost("localhost");
            logEntry.setSeverity("4");
            logEntry.setMessage("test message " + i);
            logEntries[i] = logEntry;
        }
        request.setLogEntries(logEntries);

        // Send request
        callbackHandler.messageReceived = null;
        String requestJson = JsonTranslator.getInstance().toJson(request);
        client.send(new DefaultWebSocketFrame(requestJson));

        // Wait for it to be processed
        Thread.sleep(1000);

        // Disconnect
        client.disconnect();

        // Check response
        assertNotNull(callbackHandler.messageReceived);
        PublicationResponseAO response = JsonTranslator.getInstance().fromJson(callbackHandler.messageReceived,
                PublicationResponseAO.class);
        assertEquals("testBadRole", response.getMessageID());
        assertFalse(response.isSuccess());
        assertNotNull(response.getErrorMessage());
        assertNotNull(response.getErrorStackTrace());
        assertEquals("Access denied.", response.getErrorMessage());
    }
    /**
     * Thread for running in testMultipleConnections
     * 
     * @author vibul
     * 
     */
    public static class PublishThread implements Runnable
    {
        private static Log4JLogger _logger = Log4JLogger.getLogger(PublishThread.class);

        public void run()
        {
            try
            {
                _logger.debug("WS thread " + Thread.currentThread().getName() + " started");

                MyWebSocketCallbackHandler callbackHandler = new MyWebSocketCallbackHandler();
                WebSocketClientFactory factory = new WebSocketClientFactory();

                WebSocketClient client = factory.newClient(new URI("ws://localhost:61615/websocket-hybi-00"),
                        callbackHandler);

                // Connect
                client.connect().awaitUninterruptibly();
                Thread.sleep(500);
                assertTrue(callbackHandler.connected);

                // Publish
                sendPublicshRequest(client, callbackHandler, "PublishThread" + Thread.currentThread().getName(), 2);
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Our web socket callback handler
     * 
     * @author vibul
     * 
     */
    public static class MyWebSocketCallbackHandler implements WebSocketCallback
    {
        private static Log4JLogger _logger = Log4JLogger.getLogger(MyWebSocketCallbackHandler.class);

        public boolean connected = false;
        public String messageReceived = null;

        public MyWebSocketCallbackHandler()
        {
            return;
        }

        @Override
        public void onConnect(WebSocketClient client)
        {
            _logger.debug("WebSocket connected!");
            connected = true;
        }

        @Override
        public void onDisconnect(WebSocketClient client)
        {
            _logger.debug("WebSocket disconnected!");
            connected = false;
        }

        @Override
        public void onMessage(WebSocketClient client, WebSocketFrame frame)
        {
            _logger.debug("WebSocket Received Message:" + frame.getTextData());
            messageReceived = frame.getTextData();
        }

        @Override
        public void onError(Throwable t)
        {
            _logger.error(t, "WebSocket error");
        }

    }
}
