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

package com.chililog.server.data;

import static org.junit.Assert.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.chililog.server.common.ChiliLogException;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

/**
 * Test parsing and storing json repository entries
 * 
 * @author vibul
 * 
 */
public class JsonRepositoryControllerTest
{
    private static DB _db;

    @BeforeClass
    public static void classSetup() throws Exception
    {
        _db = MongoConnection.getInstance().getConnection();
        assertNotNull(_db);
    }

    @AfterClass
    public static void classTeardown() throws Exception
    {
        // Clean up old test data if any exists
        DBCollection coll = _db.getCollection("json_test_repository");
        coll.drop();
    }

    @Before
    public void testSetup() throws Exception
    {
        classTeardown();
    }

    @Test
    public void testOK() throws ChiliLogException, ParseException
    {
        RepositoryInfoBO repoInfo = new RepositoryInfoBO();
        repoInfo.setName("json_test");
        repoInfo.setDisplayName("Json Test 1");
        repoInfo.setControllerClassName("com.chililog.server.data.JsonRepositoryController");
        repoInfo.getProperties().put(JsonRepositoryController.DATE_PATTERN_REPO_PROPERTY_NAME,
                "^([0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}Z)$");
        repoInfo.getProperties().put(JsonRepositoryController.DATE_FORMAT_REPO_PROPERTY_NAME, "yyyy-MM-dd'T'HH:mm:ssZ");
        repoInfo.getProperties().put(JsonRepositoryController.LONG_NUMBER_PATTERN_REPO_PROPERTY_NAME,
                "^NumberLong\\(([0-9]+)\\)$");

        JsonRepositoryController c = new JsonRepositoryController(repoInfo);

        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"field1\": 1,"); // Integer
        sb.append("\"field2\": \"abc\","); // String
        sb.append("\"field3\": true,"); // Boolean
        sb.append("\"field4\": 8888888888,"); // Long. 10 - digit numbers converts to long
        sb.append("\"field5\": \"NumberLong(888)\","); // Long. strings of digits suffixed with Long converts to long
        sb.append("\"field6\": 5.5,"); // Double
        sb.append("\"field7\": \"2010-11-29T19:41:46Z\","); // Date - string of iso format with UTC timezone
        sb.append("}");

        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

        // Save OK
        RepositoryEntryBO entry = c.parse("log1", "127.0.0.1", sb.toString());
        assertNotNull(entry);
        DBObject dbObject = entry.toDBObject();
        assertEquals(1, dbObject.get("field1"));
        assertEquals("abc", dbObject.get("field2"));
        assertEquals(true, dbObject.get("field3"));
        assertEquals(8888888888L, dbObject.get("field4"));
        assertEquals(888L, dbObject.get("field5"));
        assertEquals(5.5d, dbObject.get("field6"));
        assertEquals(sf.parse("2010-11-29T19:41:46UTC"), dbObject.get("field7"));
        assertEquals("log1", entry.getEntryInputName());
        assertEquals("127.0.0.1", entry.getEntryInputIpAddress());

        c.save(_db, entry);

        // Get
        DBCollection coll = _db.getCollection("json_test_repository");
        DBObject query = new BasicDBObject();
        query.put("_id", entry.toDBObject().get("_id"));
        dbObject = coll.findOne(query);

        assertNotNull(dbObject);
        assertTrue(dbObject.containsField(RepositoryEntryBO.ENTRY_TIMESTAMP_FIELD_NAME));
        assertEquals(1, dbObject.get("field1"));
        assertEquals("abc", dbObject.get("field2"));
        assertEquals(true, dbObject.get("field3"));
        assertEquals(8888888888L, dbObject.get("field4"));
        assertEquals(888L, dbObject.get("field5"));
        assertEquals(5.5d, dbObject.get("field6"));
        assertEquals(sf.parse("2010-11-29T19:41:46GMT"), dbObject.get("field7"));

        // Should only be 1 entry
        assertEquals(1, coll.find().count());
    }

    @Test
    public void testParseError() throws ChiliLogException
    {
        RepositoryInfoBO repoInfo = new RepositoryInfoBO();
        repoInfo.setName("json_test");
        repoInfo.setDisplayName("Json Test 2");
        repoInfo.setControllerClassName("com.chililog.server.data.JsonRepositoryController");

        JsonRepositoryController c = new JsonRepositoryController(repoInfo);

        // Save OK
        RepositoryEntryBO entry = c.parse("log1", "127.0.0.1", "xxx");
        assertNull(entry);
    }

}
