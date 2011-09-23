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

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Pattern;

import org.bson.types.ObjectId;
import org.chililog.server.common.ChiliLogException;
import org.chililog.server.data.MongoConnection;
import org.chililog.server.data.RepositoryFieldConfigBO;
import org.chililog.server.data.RepositoryConfigBO;
import org.chililog.server.data.RepositoryConfigController;
import org.chililog.server.data.RepositoryConfigListCriteria;
import org.chililog.server.data.RepositoryParserConfigBO;
import org.chililog.server.data.Strings;
import org.chililog.server.data.RepositoryFieldConfigBO.DataType;
import org.chililog.server.data.RepositoryConfigBO.MaxMemoryPolicy;
import org.chililog.server.data.RepositoryConfigBO.Status;
import org.chililog.server.data.RepositoryParserConfigBO.AppliesTo;
import org.chililog.server.data.RepositoryParserConfigBO.ParseFieldErrorHandling;
import org.chililog.server.engine.parsers.DelimitedEntryParser;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

/**
 * Tests RepositoryInfoBO and RepositoryInfoController
 * 
 * @author vibul
 * 
 */
public class RepositoryConfigTest {
    private static DB _db;

    @BeforeClass
    public static void classSetup() throws Exception {
        _db = MongoConnection.getInstance().getConnection();
        assertNotNull(_db);

        // Clean up old test data if any exists
        DBCollection coll = _db.getCollection(RepositoryConfigController.MONGODB_COLLECTION_NAME);
        Pattern pattern = Pattern.compile("^repo_info_test[\\w]*$");
        DBObject query = new BasicDBObject();
        query.put(RepositoryConfigBO.NAME_FIELD_NAME, pattern);
        coll.remove(query);
    }

    @AfterClass
    public static void classTeardown() {
        // Clean up old test data if any exists
        DBCollection coll = _db.getCollection(RepositoryConfigController.MONGODB_COLLECTION_NAME);
        Pattern pattern = Pattern.compile("^repo_info_test[\\w]*$");
        DBObject query = new BasicDBObject();
        query.put(RepositoryConfigBO.NAME_FIELD_NAME, pattern);
        coll.remove(query);
    }

    @Test(expected = ChiliLogException.class)
    public void testGetNotFound() throws ChiliLogException {
        RepositoryConfigController.getInstance().getByName(_db, "notfound");
    }

    @Test
    public void testTryGetNotFound() throws ChiliLogException {
        RepositoryConfigBO repoInfo = RepositoryConfigController.getInstance().tryGetByName(_db, "notfound");
        assertNull(repoInfo);
    }

    @Test
    public void testCRUD() throws ChiliLogException {
        // Insert
        RepositoryConfigBO repoConfig = new RepositoryConfigBO();
        repoConfig.setName("repo_info_test1");
        repoConfig.setDisplayName("Test 1");
        repoConfig.setDescription("description");
        repoConfig.setStoreEntriesIndicator(true);
        repoConfig.setStorageQueueDurableIndicator(true);
        repoConfig.setStorageQueueWorkerCount(10);
        repoConfig.setStorageMaxKeywords(100);
        repoConfig.setMaxMemory(100);
        repoConfig.setMaxMemoryPolicy(MaxMemoryPolicy.BLOCK);
        repoConfig.setPageSize(2);
        repoConfig.setPageCountCache(1);

        RepositoryParserConfigBO repoParserConfig = new RepositoryParserConfigBO();
        repoParserConfig.setName("parser1");
        repoParserConfig.setAppliesTo(AppliesTo.All);
        repoParserConfig.setClassName(DelimitedEntryParser.class.getName());
        repoParserConfig.setMaxKeywords(1L);
        repoParserConfig.setParseFieldErrorHandling(ParseFieldErrorHandling.SkipEntry);
        repoParserConfig.getProperties().put("key1", "value11");
        repoParserConfig.getProperties().put("key2", "value12");
        repoParserConfig.getProperties().put("key3", "value13");
        repoConfig.getParsers().add(repoParserConfig);

        RepositoryFieldConfigBO repoFieldConfig = new RepositoryFieldConfigBO();
        repoFieldConfig.setName("field1");
        repoFieldConfig.setDisplayName("Field Number 1");
        repoFieldConfig.setDescription("description");
        repoFieldConfig.setDataType(RepositoryFieldConfigBO.DataType.String);
        repoFieldConfig.getProperties().put("key1", "value11");
        repoFieldConfig.getProperties().put("key2", "value12");
        repoFieldConfig.getProperties().put("key3", "value13");
        repoParserConfig.getFields().add(repoFieldConfig);

        repoFieldConfig = new RepositoryFieldConfigBO();
        repoFieldConfig.setName("field2");
        repoFieldConfig.setDisplayName("Field Number 2");
        repoFieldConfig.setDescription("description");
        repoFieldConfig.setDataType(RepositoryFieldConfigBO.DataType.Integer);
        repoFieldConfig.getProperties().put("key1", "value21");
        repoFieldConfig.getProperties().put("key2", "value22");
        repoFieldConfig.getProperties().put("key3", "value23");
        repoParserConfig.getFields().add(repoFieldConfig);

        assertFalse(repoConfig.isExistingRecord());
        assertNull(repoConfig.getDocumentID());
        assertEquals(-1, repoConfig.getDocumentVersion());

        RepositoryConfigController.getInstance().save(_db, repoConfig);
        assertTrue(repoConfig.isExistingRecord());
        assertNotNull(repoConfig.getDocumentID());
        assertEquals(1, repoConfig.getDocumentVersion());

        // Get
        RepositoryConfigBO repoConfig2 = RepositoryConfigController.getInstance().getByName(_db, "repo_info_test1");
        assertEquals("repo_info_test1", repoConfig2.getName());
        assertEquals("Test 1", repoConfig2.getDisplayName());
        assertEquals("description", repoConfig2.getDescription());
        assertEquals("repo.repo_info_test1", repoConfig2.getPubSubAddress());
        assertEquals("repo.repo_info_test1.storage", repoConfig2.getStorageQueueName());
        assertEquals(Status.ONLINE, repoConfig2.getStartupStatus());
        assertEquals(true, repoConfig2.getStoreEntriesIndicator());
        assertEquals(true, repoConfig2.getStorageQueueDurableIndicator());
        assertEquals(10, repoConfig2.getStorageQueueWorkerCount());
        assertEquals(100L, repoConfig2.getStorageMaxKeywords());
        assertEquals(100, repoConfig2.getMaxMemory());
        assertEquals(MaxMemoryPolicy.BLOCK, repoConfig2.getMaxMemoryPolicy());
        assertEquals(2, repoConfig2.getPageSize());
        assertEquals(1, repoConfig2.getPageCountCache());
        assertEquals(1, repoConfig2.getParsers().size());

        RepositoryParserConfigBO repoParserConfig2 = repoConfig2.getParsers().get(0);
        assertEquals("parser1", repoParserConfig2.getName());
        assertEquals(AppliesTo.All, repoParserConfig2.getAppliesTo());
        assertEquals(DelimitedEntryParser.class.getName(), repoParserConfig2.getClassName());
        assertEquals(1L, repoParserConfig2.getMaxKeywords());
        assertEquals(ParseFieldErrorHandling.SkipEntry, repoParserConfig2.getParseFieldErrorHandling());

        Hashtable<String, String> ht2 = repoParserConfig2.getProperties();
        assertEquals(3, ht2.keySet().size());
        assertTrue(ht2.containsKey("key1"));
        assertEquals("value11", ht2.get("key1"));
        assertTrue(ht2.containsKey("key2"));
        assertEquals("value12", ht2.get("key2"));
        assertTrue(ht2.containsKey("key3"));
        assertEquals("value13", ht2.get("key3"));

        ArrayList<RepositoryFieldConfigBO> f2 = repoParserConfig2.getFields();
        assertEquals(2, f2.size());
        assertEquals("field1", f2.get(0).getName());
        assertEquals("Field Number 1", f2.get(0).getDisplayName());
        assertEquals("description", f2.get(0).getDescription());
        assertEquals(DataType.String, f2.get(0).getDataType());
        ht2 = f2.get(0).getProperties();
        assertEquals(3, ht2.keySet().size());
        assertTrue(ht2.containsKey("key1"));
        assertEquals("value11", ht2.get("key1"));
        assertTrue(ht2.containsKey("key2"));
        assertEquals("value12", ht2.get("key2"));
        assertTrue(ht2.containsKey("key3"));
        assertEquals("value13", ht2.get("key3"));

        assertEquals("field2", f2.get(1).getName());
        assertEquals("Field Number 2", f2.get(1).getDisplayName());
        assertEquals("description", f2.get(1).getDescription());
        assertEquals(DataType.Integer, f2.get(1).getDataType());
        ht2 = f2.get(1).getProperties();
        assertEquals(3, ht2.keySet().size());
        assertTrue(ht2.containsKey("key1"));
        assertEquals("value21", ht2.get("key1"));
        assertTrue(ht2.containsKey("key2"));
        assertEquals("value22", ht2.get("key2"));
        assertTrue(ht2.containsKey("key3"));
        assertEquals("value23", ht2.get("key3"));

        // Update
        repoConfig.setName("repo_info_test1x");
        repoConfig.setDisplayName("Test 1x");
        repoConfig.setDescription("description x");
        repoConfig.setStartupStatus(Status.OFFLINE);
        repoConfig.setStoreEntriesIndicator(false);
        repoConfig.setStorageQueueDurableIndicator(false);
        repoConfig.setStorageQueueWorkerCount(100);
        repoConfig.setStorageMaxKeywords(200);
        repoConfig.setMaxMemory(210);
        repoConfig.setMaxMemoryPolicy(MaxMemoryPolicy.DROP);
        repoConfig.setPageSize(22);
        repoConfig.setPageCountCache(10);

        repoParserConfig.setClassName("com.chililog.server.data.DeclimitedRepositoryParserX");
        repoParserConfig.setMaxKeywords(2);
        repoParserConfig.setParseFieldErrorHandling(ParseFieldErrorHandling.SkipFieldIgnoreError);
        repoParserConfig.getProperties().put("key1", "value11x");
        repoParserConfig.getProperties().put("key4", "value14x");
        repoParserConfig.getProperties().remove("key3");

        repoParserConfig.getFields().remove(1);

        repoFieldConfig = repoParserConfig.getFields().get(0);
        repoFieldConfig.setName("field1x");
        repoFieldConfig.setDisplayName("Field Number 1x");
        repoFieldConfig.setDescription("description x");
        repoFieldConfig.setDataType(RepositoryFieldConfigBO.DataType.Boolean);
        repoFieldConfig.getProperties().put("key1", "value11x");
        repoFieldConfig.getProperties().put("key4", "value14x");
        repoFieldConfig.getProperties().remove("key3");

        RepositoryConfigController.getInstance().save(_db, repoConfig);
        assertEquals(2, repoConfig.getDocumentVersion());

        repoConfig2 = RepositoryConfigController.getInstance().getByName(_db, "repo_info_test1x");
        assertEquals("repo_info_test1x", repoConfig2.getName());
        assertEquals("Test 1x", repoConfig2.getDisplayName());
        assertEquals("description x", repoConfig2.getDescription());
        assertEquals(Status.OFFLINE, repoConfig2.getStartupStatus());
        assertEquals(false, repoConfig2.getStoreEntriesIndicator());
        assertEquals(false, repoConfig2.getStorageQueueDurableIndicator());
        assertEquals(100, repoConfig2.getStorageQueueWorkerCount());
        assertEquals(200L, repoConfig2.getStorageMaxKeywords());
        assertEquals(210, repoConfig2.getMaxMemory());
        assertEquals(MaxMemoryPolicy.DROP, repoConfig2.getMaxMemoryPolicy());
        assertEquals(22, repoConfig2.getPageSize());
        assertEquals(10, repoConfig2.getPageCountCache());
        assertEquals(2, repoConfig2.getDocumentVersion());

        repoParserConfig2 = repoConfig2.getParsers().get(0);
        assertEquals("parser1", repoParserConfig2.getName());
        assertEquals(AppliesTo.All, repoParserConfig2.getAppliesTo());
        assertEquals("com.chililog.server.data.DeclimitedRepositoryParserX", repoParserConfig2.getClassName());
        assertEquals(2L, repoParserConfig2.getMaxKeywords());
        assertEquals(ParseFieldErrorHandling.SkipFieldIgnoreError, repoParserConfig2.getParseFieldErrorHandling());

        ht2 = repoParserConfig2.getProperties();
        assertEquals(3, ht2.keySet().size());
        assertTrue(ht2.containsKey("key1"));
        assertEquals("value11x", ht2.get("key1"));
        assertTrue(ht2.containsKey("key2"));
        assertEquals("value12", ht2.get("key2"));
        assertFalse(ht2.containsKey("key3"));
        assertTrue(ht2.containsKey("key4"));
        assertEquals("value14x", ht2.get("key4"));

        f2 = repoParserConfig2.getFields();
        assertEquals(1, f2.size());
        assertEquals("field1x", f2.get(0).getName());
        assertEquals("Field Number 1x", f2.get(0).getDisplayName());
        assertEquals("description x", f2.get(0).getDescription());
        assertEquals(DataType.Boolean, f2.get(0).getDataType());
        ht2 = f2.get(0).getProperties();
        assertEquals(3, ht2.keySet().size());
        assertTrue(ht2.containsKey("key1"));
        assertEquals("value11x", ht2.get("key1"));
        assertTrue(ht2.containsKey("key2"));
        assertEquals("value12", ht2.get("key2"));
        assertFalse(ht2.containsKey("key3"));
        assertTrue(ht2.containsKey("key4"));
        assertEquals("value14x", ht2.get("key4"));

        // Get by id
        String id = repoConfig2.getDocumentID().toString();
        RepositoryConfigBO repoInfo3 = RepositoryConfigController.getInstance().get(_db, new ObjectId(id));
        assertEquals("repo_info_test1x", repoInfo3.getName());

        // Remove
        RepositoryConfigController.getInstance().remove(_db, repoConfig);

        // Get again
        repoConfig2 = RepositoryConfigController.getInstance().tryGetByName(_db, "repo_info_test1");
        assertNull(repoConfig2);
        repoConfig2 = RepositoryConfigController.getInstance().tryGetByName(_db, "repo_info_test2");
        assertNull(repoConfig2);

        // Remove again should not throw an error
        RepositoryConfigController.getInstance().remove(_db, repoConfig);
    }

    @Test
    public void testDuplicateFieldName() throws ChiliLogException {
        try {
            // Insert
            RepositoryConfigBO repoConfig = new RepositoryConfigBO();
            repoConfig.setName("repo_info_test4");

            RepositoryParserConfigBO repoParserConfig = new RepositoryParserConfigBO();
            repoParserConfig.setName("parser1");
            repoParserConfig.setAppliesTo(AppliesTo.All);
            repoParserConfig.setClassName(DelimitedEntryParser.class.getName());
            repoConfig.getParsers().add(repoParserConfig);

            RepositoryFieldConfigBO repoFieldConfig = new RepositoryFieldConfigBO();
            repoFieldConfig.setName("field1");
            repoFieldConfig.setDisplayName("Field Number 1");
            repoFieldConfig.setDescription("description");
            repoFieldConfig.setDataType(RepositoryFieldConfigBO.DataType.String);
            repoFieldConfig.getProperties().put("key1", "value11");
            repoFieldConfig.getProperties().put("key2", "value12");
            repoFieldConfig.getProperties().put("key3", "value13");
            repoParserConfig.getFields().add(repoFieldConfig);

            repoFieldConfig = new RepositoryFieldConfigBO();
            repoFieldConfig.setName("field1");
            repoFieldConfig.setDisplayName("Field Number 2");
            repoFieldConfig.setDescription("description");
            repoFieldConfig.setDataType(RepositoryFieldConfigBO.DataType.Integer);
            repoFieldConfig.getProperties().put("key1", "value21");
            repoFieldConfig.getProperties().put("key2", "value22");
            repoFieldConfig.getProperties().put("key3", "value23");
            repoParserConfig.getFields().add(repoFieldConfig);

            RepositoryConfigController.getInstance().save(_db, repoConfig);

            fail("Exception expected");
        }
        catch (ChiliLogException ex) {
            assertEquals(Strings.REPO_INFO_DUPLICATE_FIELD_NAME_ERROR, ex.getErrorCode());
        }
    }

    @Test
    public void testDuplicateParserName() throws ChiliLogException {
        try {
            // Insert
            RepositoryConfigBO repoConfig = new RepositoryConfigBO();
            repoConfig.setName("repo_info_test5");

            RepositoryParserConfigBO repoParserConfig = new RepositoryParserConfigBO();
            repoParserConfig.setName("parser1");
            repoParserConfig.setAppliesTo(AppliesTo.All);
            repoParserConfig.setClassName(DelimitedEntryParser.class.getName());
            repoConfig.getParsers().add(repoParserConfig);

            repoParserConfig = new RepositoryParserConfigBO();
            repoParserConfig.setName("parser1");
            repoParserConfig.setAppliesTo(AppliesTo.All);
            repoParserConfig.setClassName(DelimitedEntryParser.class.getName());
            repoConfig.getParsers().add(repoParserConfig);

            RepositoryConfigController.getInstance().save(_db, repoConfig);

            fail("Exception expected");
        }
        catch (ChiliLogException ex) {
            assertEquals(Strings.REPO_INFO_DUPLICATE_PARSER_NAME_ERROR, ex.getErrorCode());
        }
    }

    @Test
    public void testDuplicateName() throws ChiliLogException {
        try {
            RepositoryConfigBO repoConfig = new RepositoryConfigBO();
            repoConfig.setName("repo_info_test3");
            RepositoryConfigController.getInstance().save(_db, repoConfig);

            RepositoryConfigBO repoConfig2 = new RepositoryConfigBO();
            repoConfig2.setName("repo_info_test3");
            RepositoryConfigController.getInstance().save(_db, repoConfig2);

            fail("Exception expected");
        }
        catch (ChiliLogException ex) {
            assertEquals(Strings.REPO_INFO_DUPLICATE_NAME_ERROR, ex.getErrorCode());
        }
    }

    @Test
    public void testBadName() throws ChiliLogException {
        try {
            RepositoryConfigBO repoConfig = new RepositoryConfigBO();
            repoConfig.setName("bad name");
            RepositoryConfigController.getInstance().save(_db, repoConfig);

            fail("Exception expected");
        }
        catch (ChiliLogException ex) {
            assertEquals(Strings.REPO_INFO_NAME_FORMAT_ERROR, ex.getErrorCode());
        }
    }

    @Test
    public void testBadPageFileSize() throws ChiliLogException {
        try {
            RepositoryConfigBO repoConfig = new RepositoryConfigBO();
            repoConfig.setName("badfilesize");
            repoConfig.setMaxMemory(1);
            repoConfig.setPageSize(2);
            RepositoryConfigController.getInstance().save(_db, repoConfig);

            fail("Exception expected");
        }
        catch (ChiliLogException ex) {
            assertEquals(Strings.REPO_INFO_PAGE_FILE_SIZE_ERROR, ex.getErrorCode());
        }
    }

    @Test
    public void testList() throws ChiliLogException {
        // Insert
        RepositoryConfigBO repoConfig = new RepositoryConfigBO();
        repoConfig.setName("repo_info_testlist4");
        RepositoryConfigController.getInstance().save(_db, repoConfig);

        RepositoryConfigBO repoConfig2 = new RepositoryConfigBO();
        repoConfig2.setName("repo_info_testlist5");
        RepositoryConfigController.getInstance().save(_db, repoConfig2);

        List<RepositoryConfigBO> list = null;

        // ***************************
        // Name pattern
        // ***************************
        RepositoryConfigListCriteria criteria = new RepositoryConfigListCriteria();
        criteria.setNamePattern("^repo_info_testlist[\\w]*$");
        list = RepositoryConfigController.getInstance().getList(_db, criteria);
        assertEquals(2, list.size());
        assertEquals("repo_info_testlist4", list.get(0).getName());
        assertEquals("repo_info_testlist5", list.get(1).getName());

        criteria = new RepositoryConfigListCriteria();
        criteria.setNamePattern("^no matches for sure[\\w]*$");
        list = RepositoryConfigController.getInstance().getList(_db, criteria);
        assertEquals(0, list.size());
    }

}
