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
import com.chililog.server.ui.api.AuthenticationAO;
import com.chililog.server.ui.api.AuthenticationAO.ExpiryType;
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
    private static String _authToken;
    
    @BeforeClass
    public static void classSetup() throws Exception
    {
        _db = MongoConnection.getInstance().getConnection();
        assertNotNull(_db);
        
        // Create writer user
        UserBO user = new UserBO();
        user.setUsername("AuthenticationTest");
        user.setPassword("hello there", true);
        user.addRole("Admin");
        UserController.getInstance().save(_db, user);
        
        WebServerManager.getInstance().start();
        
        // Login
        _authToken = ApiUtils.login("AuthenticationTest", "hello there", ExpiryType.Absolute, -1);

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
     * Create, Get, Update, Delete
     * 
     * @throws IOException
     */
    @Test
    public void testCRUD() throws IOException
    {
        // Login
        
        // Insert
        URL url = new URL("http://localhost:8989/api/Users");
        URLConnection conn = url.openConnection();

        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", Worker.JSON_CONTENT_TYPE);

        
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
}
