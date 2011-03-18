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

import java.util.GregorianCalendar;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.chililog.server.common.ChiliLogException;
import com.chililog.server.data.RepositoryInfoBO.ParseFieldErrorHandling;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

/**
 * Test parsing and storing regular repository entries
 * 
 * @author vibul
 * 
 */
public class RegexRepositoryControllerTest
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
        DBCollection coll = _db.getCollection("regex_test_repository");
        coll.drop();
    }

    @Before
    public void testSetup() throws Exception
    {
        classTeardown();
    }

    @Test
    public void testOK() throws ChiliLogException
    {
        RepositoryInfoBO repoInfo = new RepositoryInfoBO();
        repoInfo.setName("regex_test");
        repoInfo.setDisplayName("JUnit Test 1");
        repoInfo.setControllerClassName("com.chililog.server.data.RegexRepositoryController");
        repoInfo.setParseFieldErrorHandling(ParseFieldErrorHandling.SkipEntry);
        repoInfo.getProperties()
                .put(RegexRepositoryController.PATTERN_REPO_PROPERTY_NAME,
                        "^([0-9a-z]+)~([0-9\\.]+)~([0-9\\.]+)~([0-9\\.]+)~([0-9]{4}-[0-9]{1,2}-[0-9]{1,2} [0-9]{1,2}:[0-9]{1,2}:[0-9]{1,2})~([0-9a-zA-Z]+)$");

        RepositoryFieldInfoBO repoFieldInfo = new RepositoryFieldInfoBO();
        repoFieldInfo.setName("field1");
        repoFieldInfo.setDataType(RepositoryFieldInfoBO.DataType.String);
        repoFieldInfo.getProperties().put(RegexRepositoryController.GROUP_REPO_FIELD_PROPERTY_NAME, "1");
        repoInfo.getFields().add(repoFieldInfo);

        repoFieldInfo = new RepositoryFieldInfoBO();
        repoFieldInfo.setName("field2");
        repoFieldInfo.setDataType(RepositoryFieldInfoBO.DataType.Integer);
        repoFieldInfo.getProperties().put(RegexRepositoryController.GROUP_REPO_FIELD_PROPERTY_NAME, "2");
        repoInfo.getFields().add(repoFieldInfo);

        repoFieldInfo = new RepositoryFieldInfoBO();
        repoFieldInfo.setName("field3");
        repoFieldInfo.setDataType(RepositoryFieldInfoBO.DataType.Long);
        repoFieldInfo.getProperties().put(RegexRepositoryController.GROUP_REPO_FIELD_PROPERTY_NAME, "3");
        repoInfo.getFields().add(repoFieldInfo);

        repoFieldInfo = new RepositoryFieldInfoBO();
        repoFieldInfo.setName("field4");
        repoFieldInfo.setDataType(RepositoryFieldInfoBO.DataType.Double);
        repoFieldInfo.getProperties().put(RegexRepositoryController.GROUP_REPO_FIELD_PROPERTY_NAME, "4");
        repoInfo.getFields().add(repoFieldInfo);

        repoFieldInfo = new RepositoryFieldInfoBO();
        repoFieldInfo.setName("field5");
        repoFieldInfo.setDataType(RepositoryFieldInfoBO.DataType.Date);
        repoFieldInfo.getProperties().put(RegexRepositoryController.GROUP_REPO_FIELD_PROPERTY_NAME, "5");
        repoInfo.getFields().add(repoFieldInfo);

        repoFieldInfo = new RepositoryFieldInfoBO();
        repoFieldInfo.setName("field6");
        repoFieldInfo.setDataType(RepositoryFieldInfoBO.DataType.Boolean);
        repoFieldInfo.getProperties().put(RegexRepositoryController.GROUP_REPO_FIELD_PROPERTY_NAME, "6");
        repoInfo.getFields().add(repoFieldInfo);

        RegexRepositoryController c = new RegexRepositoryController(repoInfo);

        // Save Line 1 OK
        RepositoryEntryBO entry = c.parse("log1", "127.0.0.1", "line1~2~3~4.4~2001-5-5 5:5:5~True");
        assertNotNull(entry);
        DBObject dbObject = entry.toDBObject();
        assertEquals("line1", dbObject.get("field1"));
        assertEquals(2, dbObject.get("field2"));
        assertEquals(3L, dbObject.get("field3"));
        assertEquals(4.4d, dbObject.get("field4"));
        assertEquals(new GregorianCalendar(2001, 4, 5, 5, 5, 5).getTime(), dbObject.get("field5"));
        assertEquals(true, dbObject.get("field6"));

        c.save(_db, entry);

        // Get Line 1
        DBCollection coll = _db.getCollection("regex_test_repository");
        DBObject query = new BasicDBObject();
        query.put("_id", entry.toDBObject().get("_id"));
        dbObject = coll.findOne(query);

        assertNotNull(dbObject);
        assertTrue(dbObject.containsField(RepositoryEntryBO.ENTRY_TIMESTAMP_FIELD_NAME));
        assertEquals("line1", dbObject.get("field1"));
        assertEquals(2, dbObject.get("field2"));
        assertEquals(3L, dbObject.get("field3"));
        assertEquals(4.4d, dbObject.get("field4"));
        assertEquals(new GregorianCalendar(2001, 4, 5, 5, 5, 5).getTime(), dbObject.get("field5"));
        assertEquals(true, dbObject.get("field6"));
        assertEquals("line1~2~3~4.4~2001-5-5 5:5:5~True", dbObject.get(RepositoryEntryBO.ENTRY_TEXT_FIELD_NAME));

        // Save Line 2 OK
        entry = c.parse("log1", "127.0.0.1", "line2~22~23~24.4~2021-5-5 5:5:5~xxx");
        assertNotNull(entry);
        dbObject = entry.toDBObject();
        assertEquals("line2", dbObject.get("field1"));
        assertEquals(22, dbObject.get("field2"));
        assertEquals(23L, dbObject.get("field3"));
        assertEquals(24.4d, dbObject.get("field4"));
        assertEquals(new GregorianCalendar(2021, 4, 5, 5, 5, 5).getTime(), dbObject.get("field5"));
        assertEquals(false, dbObject.get("field6"));
        assertEquals("log1", entry.getEntryInputName());
        assertEquals("127.0.0.1", entry.getEntryInputIpAddress());

        c.save(_db, entry);

        // Get Line 2
        query = new BasicDBObject();
        query.put("_id", entry.toDBObject().get("_id"));
        dbObject = coll.findOne(query);

        assertNotNull(dbObject);
        assertTrue(dbObject.containsField(RepositoryEntryBO.ENTRY_TIMESTAMP_FIELD_NAME));
        assertEquals(22, dbObject.get("field2"));
        assertEquals(23L, dbObject.get("field3"));
        assertEquals(24.4d, dbObject.get("field4"));
        assertEquals(new GregorianCalendar(2021, 4, 5, 5, 5, 5).getTime(), dbObject.get("field5"));
        assertEquals(false, dbObject.get("field6"));
        assertEquals("line2~22~23~24.4~2021-5-5 5:5:5~xxx", dbObject.get(RepositoryEntryBO.ENTRY_TEXT_FIELD_NAME));

        // Should only be 2 entries
        assertEquals(2, coll.find().count());

        // Empty string is ignored
        entry = c.parse("log1", "127.0.0.1", "");
        assertNull(entry);
        assertNotNull(c.getLastParseError());

        // Missing field
        entry = c.parse("log1", "127.0.0.1", "line3");
        assertNull(entry);
        assertNotNull(c.getLastParseError());
    }

}
