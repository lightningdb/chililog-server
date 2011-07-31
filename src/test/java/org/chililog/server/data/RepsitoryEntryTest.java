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

package org.chililog.server.data;

import static org.junit.Assert.*;

import java.util.Date;

import org.chililog.server.common.ChiliLogException;
import org.chililog.server.data.MongoConnection;
import org.chililog.server.data.RepositoryEntryBO;
import org.chililog.server.data.RepositoryEntryController;
import org.chililog.server.data.RepositoryInfoBO;
import org.chililog.server.data.RepositoryEntryBO.Severity;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.mongodb.DB;
import com.mongodb.DBCollection;

/**
 * Test RepositoryEntryBO and RepositoryEntryController
 * 
 * @author vibul
 * 
 */
public class RepsitoryEntryTest
{
    private static DB _db;
    private static RepositoryInfoBO _repoInfo;

    @BeforeClass
    public static void classSetup() throws Exception
    {
        _db = MongoConnection.getInstance().getConnection();
        assertNotNull(_db);

        // Prepare common repo
        _repoInfo = new RepositoryInfoBO();
        _repoInfo.setName("entry_test");
        _repoInfo.setDisplayName("Repo Entry Test");

        // Clean up old test data if any exists
        DBCollection coll = _db.getCollection(_repoInfo.getMongoDBCollectionName());
        coll.drop();
    }

    @AfterClass
    public static void classTeardown()
    {
        // Clean up old test data if any exists
        DBCollection coll = _db.getCollection(_repoInfo.getMongoDBCollectionName());
        coll.drop();
    }

    @Test
    public void testCRUD() throws ChiliLogException
    {
        Date ts = new Date();

        // Insert
        RepositoryEntryBO entry = new RepositoryEntryBO();
        entry.setTimestamp(ts);
        entry.setSavedTimestamp(ts);
        entry.setSource("log1");
        entry.setHost("localhost");
        entry.setSeverity(Severity.Action);
        entry.setMessage("my message");

        assertFalse(entry.isExistingRecord());
        assertNull(entry.getDocumentID());
        assertEquals(-1, entry.getDocumentVersion());

        RepositoryEntryController.getInstance(_repoInfo).save(_db, entry);
        assertTrue(entry.isExistingRecord());
        assertNotNull(entry.getDocumentID());
        assertEquals(1, entry.getDocumentVersion());

        // Get
        RepositoryEntryBO entry2 = RepositoryEntryController.getInstance(_repoInfo).get(_db, entry.getDocumentID());
        assertEquals(ts.getTime(), entry2.getTimestamp().getTime());
        assertEquals(ts.getTime(), entry2.getSavedTimestamp().getTime());
        assertEquals("log1", entry2.getSource());
        assertEquals("localhost", entry2.getHost());
        assertEquals(Severity.Action, entry2.getSeverity());
        assertEquals("my message", entry2.getMessage());
        assertEquals(1, entry2.getDocumentVersion());

        // Update
        entry.setSource("log2");
        entry.setHost("localhost2");
        entry.setSeverity(Severity.Critical);
        entry.setMessage("my message 2");
        RepositoryEntryController.getInstance(_repoInfo).save(_db, entry);

        entry2 = RepositoryEntryController.getInstance(_repoInfo).get(_db, entry.getDocumentID());
        assertEquals(ts.getTime(), entry2.getTimestamp().getTime());
        assertEquals("log2", entry2.getSource());
        assertEquals("localhost2", entry2.getHost());
        assertEquals(Severity.Critical, entry2.getSeverity());
        assertEquals("my message 2", entry2.getMessage());
        assertEquals(2, entry2.getDocumentVersion());

        // Remove
        RepositoryEntryController.getInstance(_repoInfo).remove(_db, entry);

        // Get again
        entry2 = RepositoryEntryController.getInstance(_repoInfo).tryGet(_db, entry.getDocumentID());
        assertNull(entry2);

        // Remove again should not throw an error
        RepositoryEntryController.getInstance(_repoInfo).remove(_db, entry);
    }

    @Test
    public void testSeverity() throws ChiliLogException
    {
        // Invalid parse values must default to info
        assertEquals(Severity.Information, Severity.parse(null));
        assertEquals(Severity.Information, Severity.parse(""));
        assertEquals(Severity.Information, Severity.parse("xxxx"));
        assertEquals(Severity.Information, Severity.parse("8"));
        assertEquals(Severity.Information, Severity.parse("9"));
        assertEquals(Severity.Information, Severity.parse("3333"));

        assertEquals(Severity.Emergency, Severity.parse("0"));
        assertEquals(Severity.Emergency, Severity.parse("Emergency"));

        assertEquals(Severity.Action, Severity.parse("1"));
        assertEquals(Severity.Action, Severity.parse("Action"));

        assertEquals(Severity.Critical, Severity.parse("2"));
        assertEquals(Severity.Critical, Severity.parse("Critical"));

        assertEquals(Severity.Error, Severity.parse("3"));
        assertEquals(Severity.Error, Severity.parse("Error"));

        assertEquals(Severity.Warning, Severity.parse("4"));
        assertEquals(Severity.Warning, Severity.parse("Warning"));

        assertEquals(Severity.Notice, Severity.parse("5"));
        assertEquals(Severity.Notice, Severity.parse("Notice"));

        assertEquals(Severity.Information, Severity.parse("6"));
        assertEquals(Severity.Information, Severity.parse("Information"));

        assertEquals(Severity.Debug, Severity.parse("7"));
        assertEquals(Severity.Debug, Severity.parse("Debug"));

    }
}
