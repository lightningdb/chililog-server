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

package org.chililog.server.engine.parsers;

import static org.junit.Assert.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.chililog.server.common.ChiliLogException;
import org.chililog.server.data.MongoConnection;
import org.chililog.server.data.RepositoryEntryBO;
import org.chililog.server.data.RepositoryEntryController;
import org.chililog.server.data.RepositoryConfigBO;
import org.chililog.server.data.RepositoryParserConfigBO;
import org.chililog.server.data.RepositoryEntryBO.Severity;
import org.chililog.server.data.RepositoryParserConfigBO.AppliesTo;
import org.chililog.server.data.RepositoryParserConfigBO.ParseFieldErrorHandling;
import org.chililog.server.engine.parsers.JsonEntryParser;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

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
public class JsonEntryParserTest {

    private static DB _db;

    @BeforeClass
    public static void classSetup() throws Exception {
        _db = MongoConnection.getInstance().getConnection();
        assertNotNull(_db);
    }

    @AfterClass
    public static void classTeardown() throws Exception {
        // Clean up old test data if any exists
        DBCollection coll = _db.getCollection("repo_json_test");
        coll.drop();
    }

    @Before
    public void testSetup() throws Exception {
        classTeardown();
    }

    @Test
    public void testOK() throws ChiliLogException, ParseException {
        RepositoryConfigBO repoInfo = new RepositoryConfigBO();
        repoInfo.setName("json_test");
        repoInfo.setDisplayName("Json Test 1");

        RepositoryParserConfigBO repoParserInfo = new RepositoryParserConfigBO();
        repoParserInfo.setName("parser1");
        repoParserInfo.setAppliesTo(AppliesTo.All);
        repoParserInfo.setClassName(JsonEntryParser.class.getName());
        repoParserInfo.setParseFieldErrorHandling(ParseFieldErrorHandling.SkipEntry);
        repoParserInfo.getProperties().put(JsonEntryParser.DATE_PATTERN_PROPERTY_NAME,
                "^([0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}Z)$");
        repoParserInfo.getProperties().put(JsonEntryParser.DATE_FORMAT_PROPERTY_NAME, "yyyy-MM-dd'T'HH:mm:ssZ");
        repoParserInfo.getProperties().put(JsonEntryParser.LONG_NUMBER_PATTERN_PROPERTY_NAME,
                "^NumberLong\\(([0-9]+)\\)$");
        repoInfo.getParsers().add(repoParserInfo);

        RepositoryEntryController c = RepositoryEntryController.getInstance(repoInfo);
        JsonEntryParser p = new JsonEntryParser(repoInfo, repoParserInfo);

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
        RepositoryEntryBO entry = p.parse("2010-11-29T19:41:46.0Z", "log1", "127.0.0.1", Severity.Critical.toString(),
                null, sb.toString());
        assertNotNull(entry);
        DBObject dbObject = entry.toDBObject();
        c.save(_db, entry);

        // Get
        DBCollection coll = _db.getCollection("repo_json_test");
        DBObject query = new BasicDBObject();
        query.put("_id", entry.toDBObject().get("_id"));
        dbObject = coll.findOne(query);

        GregorianCalendar cal = new GregorianCalendar();
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
        cal.set(2010, 10, 29, 19, 41, 46);
        cal.set(Calendar.MILLISECOND, 0);

        assertNotNull(dbObject);
        assertEquals(cal.getTime(), dbObject.get(RepositoryEntryBO.TIMESTAMP_FIELD_NAME));
        assertTrue(dbObject.containsField(RepositoryEntryBO.SAVED_TIMESTAMP_FIELD_NAME));
        assertEquals(1, dbObject.get("field1"));
        assertEquals("abc", dbObject.get("field2"));
        assertEquals(true, dbObject.get("field3"));
        assertEquals(8888888888L, dbObject.get("field4"));
        assertEquals(888L, dbObject.get("field5"));
        assertEquals(5.5d, dbObject.get("field6"));
        assertEquals(sf.parse("2010-11-29T19:41:46GMT"), dbObject.get("field7"));
        assertEquals("log1", dbObject.get(RepositoryEntryBO.SOURCE_FIELD_NAME));
        assertEquals("127.0.0.1", dbObject.get(RepositoryEntryBO.HOST_FIELD_NAME));
        assertEquals(Severity.Critical.toCode(), dbObject.get(RepositoryEntryBO.SEVERITY_FIELD_NAME));
        assertEquals(sb.toString(), dbObject.get(RepositoryEntryBO.MESSAGE_FIELD_NAME));

        // Should only be 1 entry
        assertEquals(1, coll.find().count());
    }

    @Test
    public void testParseError() throws ChiliLogException {
        RepositoryConfigBO repoInfo = new RepositoryConfigBO();
        repoInfo.setName("json_test");
        repoInfo.setDisplayName("Json Test 2");

        RepositoryParserConfigBO repoParserInfo = new RepositoryParserConfigBO();
        repoParserInfo.setName("parser1");
        repoParserInfo.setAppliesTo(AppliesTo.All);
        repoParserInfo.setClassName(JsonEntryParser.class.getName());
        repoParserInfo.setParseFieldErrorHandling(ParseFieldErrorHandling.SkipEntry);
        repoParserInfo.getProperties().put(JsonEntryParser.DATE_PATTERN_PROPERTY_NAME,
                "^([0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}Z)$");
        repoParserInfo.getProperties().put(JsonEntryParser.DATE_FORMAT_PROPERTY_NAME, "yyyy-MM-dd'T'HH:mm:ssZ");
        repoParserInfo.getProperties().put(JsonEntryParser.LONG_NUMBER_PATTERN_PROPERTY_NAME,
                "^NumberLong\\(([0-9]+)\\)$");
        repoInfo.getParsers().add(repoParserInfo);

        JsonEntryParser p = new JsonEntryParser(repoInfo, repoParserInfo);

        // Error because xxx is not json format
        RepositoryEntryBO entry = p.parse("2010-11-29T19:41:46Z", "log1", "127.0.0.1", Severity.Emergency.toString(),
                null, "xxx");
        assertNull(entry);
        assertNotNull(p.getLastParseError());
    }

}
