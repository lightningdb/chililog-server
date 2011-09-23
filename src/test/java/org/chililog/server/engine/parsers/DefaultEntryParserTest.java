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
import org.chililog.server.engine.parsers.DefaultEntryParser;
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
public class DefaultEntryParserTest {

    private static DB _db;

    @BeforeClass
    public static void classSetup() throws Exception {
        _db = MongoConnection.getInstance().getConnection();
        assertNotNull(_db);
    }

    @AfterClass
    public static void classTeardown() throws Exception {
        // Clean up old test data if any exists
        DBCollection coll = _db.getCollection("repo_default_test");
        coll.drop();
    }

    @Before
    public void testSetup() throws Exception {
        classTeardown();
    }

    @Test
    public void testOK() throws ChiliLogException, ParseException {
        RepositoryConfigBO repoInfo = new RepositoryConfigBO();
        repoInfo.setName("default_test");
        repoInfo.setDisplayName("Default Test 1");

        RepositoryParserConfigBO repoParserInfo = new RepositoryParserConfigBO();
        repoParserInfo.setName("parser1");
        repoParserInfo.setAppliesTo(AppliesTo.All);
        repoParserInfo.setClassName(DefaultEntryParser.class.getName());
        repoParserInfo.setParseFieldErrorHandling(ParseFieldErrorHandling.SkipEntry);
        repoInfo.getParsers().add(repoParserInfo);

        RepositoryEntryController c = RepositoryEntryController.getInstance(repoInfo);
        DefaultEntryParser p = new DefaultEntryParser(repoInfo, repoParserInfo);

        StringBuilder sb = new StringBuilder();
        sb.append("hello");

        // Save OK
        RepositoryEntryBO entry = p.parse("2010-11-29T19:41:46.0Z", "log1", "127.0.0.1", Severity.Critical.toString(),
                sb.toString());
        assertNotNull(entry);
        DBObject dbObject = entry.toDBObject();
        c.save(_db, entry);

        // Get
        DBCollection coll = _db.getCollection("repo_default_test");
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
        assertEquals("log1", dbObject.get(RepositoryEntryBO.SOURCE_FIELD_NAME));
        assertEquals("127.0.0.1", dbObject.get(RepositoryEntryBO.HOST_FIELD_NAME));
        assertEquals(Severity.Critical.toCode(), dbObject.get(RepositoryEntryBO.SEVERITY_FIELD_NAME));
        assertEquals(sb.toString(), dbObject.get(RepositoryEntryBO.MESSAGE_FIELD_NAME));

        // Should only be 1 entry
        assertEquals(1, coll.find().count());
    }

}
