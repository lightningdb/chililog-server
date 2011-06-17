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

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Pattern;

import org.bson.types.ObjectId;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.chililog.server.common.ChiliLogException;
import com.chililog.server.data.RepositoryFieldInfoBO.DataType;
import com.chililog.server.data.RepositoryInfoBO.MaxMemoryPolicy;
import com.chililog.server.data.RepositoryInfoBO.Status;
import com.chililog.server.data.RepositoryParserInfoBO.AppliesTo;
import com.chililog.server.data.RepositoryParserInfoBO.ParseFieldErrorHandling;
import com.chililog.server.engine.parsers.DelimitedEntryParser;
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
public class RepositoryInfoTest
{
    private static DB _db;

    @BeforeClass
    public static void classSetup() throws Exception
    {
        _db = MongoConnection.getInstance().getConnection();
        assertNotNull(_db);

        // Clean up old test data if any exists
        DBCollection coll = _db.getCollection(RepositoryInfoController.MONGODB_COLLECTION_NAME);
        Pattern pattern = Pattern.compile("^repo_info_test[\\w]*$");
        DBObject query = new BasicDBObject();
        query.put(RepositoryInfoBO.NAME_FIELD_NAME, pattern);
        coll.remove(query);
    }

    @AfterClass
    public static void classTeardown()
    {
        // Clean up old test data if any exists
        DBCollection coll = _db.getCollection(RepositoryInfoController.MONGODB_COLLECTION_NAME);
        Pattern pattern = Pattern.compile("^repo_info_test[\\w]*$");
        DBObject query = new BasicDBObject();
        query.put(RepositoryInfoBO.NAME_FIELD_NAME, pattern);
        coll.remove(query);
    }

    @Test(expected = ChiliLogException.class)
    public void testGetNotFound() throws ChiliLogException
    {
        RepositoryInfoController.getInstance().getByName(_db, "notfound");
    }

    @Test
    public void testTryGetNotFound() throws ChiliLogException
    {
        RepositoryInfoBO repoInfo = RepositoryInfoController.getInstance().tryGetByName(_db, "notfound");
        assertNull(repoInfo);
    }

    @Test
    public void testCRUD() throws ChiliLogException
    {
        // Insert
        RepositoryInfoBO repoInfo = new RepositoryInfoBO();
        repoInfo.setName("repo_info_test1");
        repoInfo.setDisplayName("Test 1");
        repoInfo.setDescription("description");
        repoInfo.setStoreEntriesIndicator(true);
        repoInfo.setStorageQueueDurableIndicator(true);
        repoInfo.setStorageQueueWorkerCount(10);
        repoInfo.setStorageMaxKeywords(100);
        repoInfo.setMaxMemory(100);
        repoInfo.setMaxMemoryPolicy(MaxMemoryPolicy.BLOCK);
        repoInfo.setPageSize(2);
        repoInfo.setPageCountCache(1);
        
        RepositoryParserInfoBO repoParserInfo = new RepositoryParserInfoBO();
        repoParserInfo.setName("parser1");
        repoParserInfo.setAppliesTo(AppliesTo.All);
        repoParserInfo.setClassName(DelimitedEntryParser.class.getName());
        repoParserInfo.setMaxKeywords(1L);
        repoParserInfo.setParseFieldErrorHandling(ParseFieldErrorHandling.SkipEntry);
        repoParserInfo.getProperties().put("key1", "value11");
        repoParserInfo.getProperties().put("key2", "value12");
        repoParserInfo.getProperties().put("key3", "value13");
        repoInfo.getParsers().add(repoParserInfo);
        
        RepositoryFieldInfoBO repoFieldInfo = new RepositoryFieldInfoBO();
        repoFieldInfo.setName("field1");
        repoFieldInfo.setDisplayName("Field Number 1");
        repoFieldInfo.setDescription("description");
        repoFieldInfo.setDataType(RepositoryFieldInfoBO.DataType.String);
        repoFieldInfo.getProperties().put("key1", "value11");
        repoFieldInfo.getProperties().put("key2", "value12");
        repoFieldInfo.getProperties().put("key3", "value13");
        repoParserInfo.getFields().add(repoFieldInfo);

        repoFieldInfo = new RepositoryFieldInfoBO();
        repoFieldInfo.setName("field2");
        repoFieldInfo.setDisplayName("Field Number 2");
        repoFieldInfo.setDescription("description");
        repoFieldInfo.setDataType(RepositoryFieldInfoBO.DataType.Integer);
        repoFieldInfo.getProperties().put("key1", "value21");
        repoFieldInfo.getProperties().put("key2", "value22");
        repoFieldInfo.getProperties().put("key3", "value23");
        repoParserInfo.getFields().add(repoFieldInfo);

        assertFalse(repoInfo.isExistingRecord());
        assertNull(repoInfo.getDocumentID());
        assertEquals(-1, repoInfo.getDocumentVersion());

        RepositoryInfoController.getInstance().save(_db, repoInfo);
        assertTrue(repoInfo.isExistingRecord());
        assertNotNull(repoInfo.getDocumentID());
        assertEquals(1, repoInfo.getDocumentVersion());

        // Get
        RepositoryInfoBO repoInfo2 = RepositoryInfoController.getInstance().getByName(_db, "repo_info_test1");
        assertEquals("repo_info_test1", repoInfo2.getName());
        assertEquals("Test 1", repoInfo2.getDisplayName());
        assertEquals("description", repoInfo2.getDescription());
        assertEquals("repo.repo_info_test1", repoInfo2.getPubSubAddress());
        assertEquals("repo.repo_info_test1.storage", repoInfo2.getStorageQueueName());
        assertEquals(Status.ONLINE, repoInfo2.getStartupStatus());
        assertEquals(true, repoInfo2.getStoreEntriesIndicator());
        assertEquals(true, repoInfo2.getStorageQueueDurableIndicator());
        assertEquals(10, repoInfo2.getStorageQueueWorkerCount());
        assertEquals(100L, repoInfo2.getStorageMaxKeywords());
        assertEquals(100, repoInfo2.getMaxMemory());
        assertEquals(MaxMemoryPolicy.BLOCK, repoInfo2.getMaxMemoryPolicy());
        assertEquals(2, repoInfo2.getPageSize());
        assertEquals(1, repoInfo2.getPageCountCache());
        assertEquals(1, repoInfo2.getParsers().size());
        
        RepositoryParserInfoBO repoParserInfo2 = repoInfo2.getParsers().get(0);
        assertEquals("parser1", repoParserInfo2.getName());
        assertEquals(AppliesTo.All, repoParserInfo2.getAppliesTo());
        assertEquals(DelimitedEntryParser.class.getName(), repoParserInfo2.getClassName());
        assertEquals(1L, repoParserInfo2.getMaxKeywords());
        assertEquals(ParseFieldErrorHandling.SkipEntry, repoParserInfo2.getParseFieldErrorHandling());

        Hashtable<String, String> ht2 = repoParserInfo2.getProperties();
        assertEquals(3, ht2.keySet().size());
        assertTrue(ht2.containsKey("key1"));
        assertEquals("value11", ht2.get("key1"));
        assertTrue(ht2.containsKey("key2"));
        assertEquals("value12", ht2.get("key2"));
        assertTrue(ht2.containsKey("key3"));
        assertEquals("value13", ht2.get("key3"));

        ArrayList<RepositoryFieldInfoBO> f2 = repoParserInfo2.getFields();
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
        repoInfo.setName("repo_info_test1x");
        repoInfo.setDisplayName("Test 1x");
        repoInfo.setDescription("description x");
        repoInfo.setStartupStatus(Status.OFFLINE);
        repoInfo.setStoreEntriesIndicator(false);
        repoInfo.setStorageQueueDurableIndicator(false);
        repoInfo.setStorageQueueWorkerCount(100);
        repoInfo.setStorageMaxKeywords(200);
        repoInfo.setMaxMemory(210);
        repoInfo.setMaxMemoryPolicy(MaxMemoryPolicy.DROP);
        repoInfo.setPageSize(22);
        repoInfo.setPageCountCache(10);

        repoParserInfo.setClassName("com.chililog.server.data.DeclimitedRepositoryParserX");
        repoParserInfo.setMaxKeywords(2);
        repoParserInfo.setParseFieldErrorHandling(ParseFieldErrorHandling.SkipFieldIgnoreError);
        repoParserInfo.getProperties().put("key1", "value11x");
        repoParserInfo.getProperties().put("key4", "value14x");
        repoParserInfo.getProperties().remove("key3");

        repoParserInfo.getFields().remove(1);

        repoFieldInfo = repoParserInfo.getFields().get(0);
        repoFieldInfo.setName("field1x");
        repoFieldInfo.setDisplayName("Field Number 1x");
        repoFieldInfo.setDescription("description x");
        repoFieldInfo.setDataType(RepositoryFieldInfoBO.DataType.Boolean);
        repoFieldInfo.getProperties().put("key1", "value11x");
        repoFieldInfo.getProperties().put("key4", "value14x");
        repoFieldInfo.getProperties().remove("key3");

        RepositoryInfoController.getInstance().save(_db, repoInfo);
        assertEquals(2, repoInfo.getDocumentVersion());

        repoInfo2 = RepositoryInfoController.getInstance().getByName(_db, "repo_info_test1x");
        assertEquals("repo_info_test1x", repoInfo2.getName());
        assertEquals("Test 1x", repoInfo2.getDisplayName());
        assertEquals("description x", repoInfo2.getDescription());
        assertEquals(Status.OFFLINE, repoInfo2.getStartupStatus());
        assertEquals(false, repoInfo2.getStoreEntriesIndicator());
        assertEquals(false, repoInfo2.getStorageQueueDurableIndicator());
        assertEquals(100, repoInfo2.getStorageQueueWorkerCount());
        assertEquals(200L, repoInfo2.getStorageMaxKeywords());
        assertEquals(210, repoInfo2.getMaxMemory());
        assertEquals(MaxMemoryPolicy.DROP, repoInfo2.getMaxMemoryPolicy());
        assertEquals(22, repoInfo2.getPageSize());
        assertEquals(10, repoInfo2.getPageCountCache());
        assertEquals(2, repoInfo2.getDocumentVersion());

        repoParserInfo2 = repoInfo2.getParsers().get(0);
        assertEquals("parser1", repoParserInfo2.getName());
        assertEquals(AppliesTo.All, repoParserInfo2.getAppliesTo());
        assertEquals("com.chililog.server.data.DeclimitedRepositoryParserX", repoParserInfo2.getClassName());
        assertEquals(2L, repoParserInfo2.getMaxKeywords());
        assertEquals(ParseFieldErrorHandling.SkipFieldIgnoreError, repoParserInfo2.getParseFieldErrorHandling());
       
        ht2 = repoParserInfo2.getProperties();
        assertEquals(3, ht2.keySet().size());
        assertTrue(ht2.containsKey("key1"));
        assertEquals("value11x", ht2.get("key1"));
        assertTrue(ht2.containsKey("key2"));
        assertEquals("value12", ht2.get("key2"));
        assertFalse(ht2.containsKey("key3"));
        assertTrue(ht2.containsKey("key4"));
        assertEquals("value14x", ht2.get("key4"));

        f2 = repoParserInfo2.getFields();
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
        String id = repoInfo2.getDocumentID().toString();
        RepositoryInfoBO repoInfo3 = RepositoryInfoController.getInstance().get(_db, new ObjectId(id));
        assertEquals("repo_info_test1x", repoInfo3.getName());
        
        // Remove
        RepositoryInfoController.getInstance().remove(_db, repoInfo);

        // Get again
        repoInfo2 = RepositoryInfoController.getInstance().tryGetByName(_db, "repo_info_test1");
        assertNull(repoInfo2);
        repoInfo2 = RepositoryInfoController.getInstance().tryGetByName(_db, "repo_info_test2");
        assertNull(repoInfo2);

        // Remove again should not throw an error
        RepositoryInfoController.getInstance().remove(_db, repoInfo);
    }

    @Test
    public void testDuplicateFieldName() throws ChiliLogException
    {
        try
        {
            // Insert
            RepositoryInfoBO repoInfo = new RepositoryInfoBO();
            repoInfo.setName("repo_info_test4");

            RepositoryParserInfoBO repoParserInfo = new RepositoryParserInfoBO();
            repoParserInfo.setName("parser1");
            repoParserInfo.setAppliesTo(AppliesTo.All);
            repoParserInfo.setClassName(DelimitedEntryParser.class.getName());
            repoInfo.getParsers().add(repoParserInfo);
            
            RepositoryFieldInfoBO repoFieldInfo = new RepositoryFieldInfoBO();
            repoFieldInfo.setName("field1");
            repoFieldInfo.setDisplayName("Field Number 1");
            repoFieldInfo.setDescription("description");
            repoFieldInfo.setDataType(RepositoryFieldInfoBO.DataType.String);
            repoFieldInfo.getProperties().put("key1", "value11");
            repoFieldInfo.getProperties().put("key2", "value12");
            repoFieldInfo.getProperties().put("key3", "value13");
            repoParserInfo.getFields().add(repoFieldInfo);

            repoFieldInfo = new RepositoryFieldInfoBO();
            repoFieldInfo.setName("field1");
            repoFieldInfo.setDisplayName("Field Number 2");
            repoFieldInfo.setDescription("description");
            repoFieldInfo.setDataType(RepositoryFieldInfoBO.DataType.Integer);
            repoFieldInfo.getProperties().put("key1", "value21");
            repoFieldInfo.getProperties().put("key2", "value22");
            repoFieldInfo.getProperties().put("key3", "value23");
            repoParserInfo.getFields().add(repoFieldInfo);

            RepositoryInfoController.getInstance().save(_db, repoInfo);

            fail("Exception expected");
        }
        catch (ChiliLogException ex)
        {
            assertEquals(Strings.REPO_INFO_DUPLICATE_FIELD_NAME_ERROR, ex.getErrorCode());
        }
    }

    @Test
    public void testDuplicateParserName() throws ChiliLogException
    {
        try
        {
            // Insert
            RepositoryInfoBO repoInfo = new RepositoryInfoBO();
            repoInfo.setName("repo_info_test5");

            RepositoryParserInfoBO repoParserInfo = new RepositoryParserInfoBO();
            repoParserInfo.setName("parser1");
            repoParserInfo.setAppliesTo(AppliesTo.All);
            repoParserInfo.setClassName(DelimitedEntryParser.class.getName());
            repoInfo.getParsers().add(repoParserInfo);
            
            repoParserInfo = new RepositoryParserInfoBO();
            repoParserInfo.setName("parser1");
            repoParserInfo.setAppliesTo(AppliesTo.All);
            repoParserInfo.setClassName(DelimitedEntryParser.class.getName());
            repoInfo.getParsers().add(repoParserInfo);
            
            RepositoryInfoController.getInstance().save(_db, repoInfo);

            fail("Exception expected");
        }
        catch (ChiliLogException ex)
        {
            assertEquals(Strings.REPO_INFO_DUPLICATE_PARSER_NAME_ERROR, ex.getErrorCode());
        }
    }
    
    @Test
    public void testDuplicateName() throws ChiliLogException
    {
        try
        {
            RepositoryInfoBO repoInfo = new RepositoryInfoBO();
            repoInfo.setName("repo_info_test3");
            RepositoryInfoController.getInstance().save(_db, repoInfo);

            RepositoryInfoBO repoInfo2 = new RepositoryInfoBO();
            repoInfo2.setName("repo_info_test3");
            RepositoryInfoController.getInstance().save(_db, repoInfo2);

            fail("Exception expected");
        }
        catch (ChiliLogException ex)
        {
            assertEquals(Strings.REPO_INFO_DUPLICATE_NAME_ERROR, ex.getErrorCode());
        }
    }

    @Test
    public void testBadName() throws ChiliLogException
    {
        try
        {
            RepositoryInfoBO repoInfo = new RepositoryInfoBO();
            repoInfo.setName("bad name");
            RepositoryInfoController.getInstance().save(_db, repoInfo);

            fail("Exception expected");
        }
        catch (ChiliLogException ex)
        {
            assertEquals(Strings.REPO_INFO_NAME_FORMAT_ERROR, ex.getErrorCode());
        }
    }

    @Test
    public void testBadPageFileSize() throws ChiliLogException
    {
        try
        {
            RepositoryInfoBO repoInfo = new RepositoryInfoBO();
            repoInfo.setName("badfilesize");
            repoInfo.setMaxMemory(1);
            repoInfo.setPageSize(2);
            RepositoryInfoController.getInstance().save(_db, repoInfo);

            fail("Exception expected");
        }
        catch (ChiliLogException ex)
        {
            assertEquals(Strings.REPO_INFO_PAGE_FILE_SIZE_ERROR, ex.getErrorCode());
        }
    }

    
    @Test
    public void testList() throws ChiliLogException
    {
        // Insert
        RepositoryInfoBO repoInfo = new RepositoryInfoBO();
        repoInfo.setName("repo_info_testlist4");
        RepositoryInfoController.getInstance().save(_db, repoInfo);

        RepositoryInfoBO repoInfo2 = new RepositoryInfoBO();
        repoInfo2.setName("repo_info_testlist5");
        RepositoryInfoController.getInstance().save(_db, repoInfo2);

        List<RepositoryInfoBO> list = null;

        // ***************************
        // Name pattern
        // ***************************
        RepositoryInfoListCriteria criteria = new RepositoryInfoListCriteria();
        criteria.setNamePattern("^repo_info_testlist[\\w]*$");
        list = RepositoryInfoController.getInstance().getList(_db, criteria);
        assertEquals(2, list.size());
        assertEquals("repo_info_testlist4", list.get(0).getName());
        assertEquals("repo_info_testlist5", list.get(1).getName());

        criteria = new RepositoryInfoListCriteria();
        criteria.setNamePattern("^no matches for sure[\\w]*$");
        list = RepositoryInfoController.getInstance().getList(_db, criteria);
        assertEquals(0, list.size());
    }
    
}
