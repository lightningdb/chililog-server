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
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bson.types.ObjectId;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.chililog.server.common.ChiliLogException;
import com.chililog.server.data.RepositoryFieldInfoBO.DataType;
import com.chililog.server.data.RepositoryInfoBO.ParseFieldErrorHandling;
import com.chililog.server.data.RepositoryInfoBO.QueueMaxMemoryPolicy;
import com.chililog.server.data.RepositoryInfoBO.Status;
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
        repoInfo.setControllerClassName("com.chililog.server.data.com.chililog.server.data.DelimitedRepositoryController");
        repoInfo.setReadQueueDurable(true);
        repoInfo.setWriteQueueDurable(true);
        repoInfo.setWriteQueueWorkerCount(10);
        repoInfo.setWriteQueueMaxMemory(1);
        repoInfo.setWriteQueueMaxMemoryPolicy(QueueMaxMemoryPolicy.BLOCK);
        repoInfo.setWriteQueuePageSize(2);
        repoInfo.setParseFieldErrorHandling(ParseFieldErrorHandling.SkipEntry);
        repoInfo.getProperties().put("key1", "value11");
        repoInfo.getProperties().put("key2", "value12");
        repoInfo.getProperties().put("key3", "value13");

        RepositoryFieldInfoBO repoFieldInfo = new RepositoryFieldInfoBO();
        repoFieldInfo.setName("field1");
        repoFieldInfo.setDisplayName("Field Number 1");
        repoFieldInfo.setDescription("description");
        repoFieldInfo.setDataType(RepositoryFieldInfoBO.DataType.String);
        repoFieldInfo.getProperties().put("key1", "value11");
        repoFieldInfo.getProperties().put("key2", "value12");
        repoFieldInfo.getProperties().put("key3", "value13");
        repoInfo.getFields().add(repoFieldInfo);

        repoFieldInfo = new RepositoryFieldInfoBO();
        repoFieldInfo.setName("field2");
        repoFieldInfo.setDisplayName("Field Number 2");
        repoFieldInfo.setDescription("description");
        repoFieldInfo.setDataType(RepositoryFieldInfoBO.DataType.Integer);
        repoFieldInfo.getProperties().put("key1", "value21");
        repoFieldInfo.getProperties().put("key2", "value22");
        repoFieldInfo.getProperties().put("key3", "value23");
        repoInfo.getFields().add(repoFieldInfo);

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
        assertEquals("com.chililog.server.data.com.chililog.server.data.DelimitedRepositoryController", repoInfo2.getControllerClassName());
        assertEquals(Status.ONLINE, repoInfo2.getStartupStatus());
        assertEquals(true, repoInfo2.isReadQueueDurable());
        assertEquals(true, repoInfo2.isWriteQueueDurable());
        assertEquals(10, repoInfo2.getWriteQueueWorkerCount());
        assertEquals(1, repoInfo2.getWriteQueueMaxMemory());
        assertEquals(QueueMaxMemoryPolicy.BLOCK, repoInfo2.getWriteQueueMaxMemoryPolicy());
        assertEquals(2, repoInfo2.getWriteQueuePageSize());
        assertEquals(ParseFieldErrorHandling.SkipEntry, repoInfo2.getParseFieldErrorHandling());
        assertEquals(1, repoInfo2.getDocumentVersion());

        Hashtable<String, String> ht2 = repoInfo2.getProperties();
        assertEquals(3, ht2.keySet().size());
        assertTrue(ht2.containsKey("key1"));
        assertEquals("value11", ht2.get("key1"));
        assertTrue(ht2.containsKey("key2"));
        assertEquals("value12", ht2.get("key2"));
        assertTrue(ht2.containsKey("key3"));
        assertEquals("value13", ht2.get("key3"));

        ArrayList<RepositoryFieldInfoBO> f2 = repoInfo2.getFields();
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
        repoInfo.setControllerClassName("com.chililog.server.data.DeclimitedRepositoryParserX");
        repoInfo.setStartupStatus(Status.OFFLINE);
        repoInfo.setReadQueueDurable(false);
        repoInfo.setWriteQueueDurable(false);
        repoInfo.setWriteQueueWorkerCount(100);
        repoInfo.setWriteQueueMaxMemory(21);
        repoInfo.setWriteQueueMaxMemoryPolicy(QueueMaxMemoryPolicy.DROP);
        repoInfo.setWriteQueuePageSize(22);
        repoInfo.setParseFieldErrorHandling(ParseFieldErrorHandling.SkipFieldIgnoreError);
        repoInfo.getProperties().put("key1", "value11x");
        repoInfo.getProperties().put("key4", "value14x");
        repoInfo.getProperties().remove("key3");

        repoInfo.getFields().remove(1);

        repoFieldInfo = repoInfo.getFields().get(0);
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
        assertEquals("com.chililog.server.data.DeclimitedRepositoryParserX", repoInfo2.getControllerClassName());
        assertEquals(Status.OFFLINE, repoInfo2.getStartupStatus());
        assertEquals(false, repoInfo2.isReadQueueDurable());
        assertEquals(false, repoInfo2.isWriteQueueDurable());
        assertEquals(100, repoInfo2.getWriteQueueWorkerCount());
        assertEquals(21, repoInfo2.getWriteQueueMaxMemory());
        assertEquals(QueueMaxMemoryPolicy.DROP, repoInfo2.getWriteQueueMaxMemoryPolicy());
        assertEquals(22, repoInfo2.getWriteQueuePageSize());
        assertEquals(ParseFieldErrorHandling.SkipFieldIgnoreError, repoInfo2.getParseFieldErrorHandling());
        assertEquals(2, repoInfo2.getDocumentVersion());

        ht2 = repoInfo2.getProperties();
        assertEquals(3, ht2.keySet().size());
        assertTrue(ht2.containsKey("key1"));
        assertEquals("value11x", ht2.get("key1"));
        assertTrue(ht2.containsKey("key2"));
        assertEquals("value12", ht2.get("key2"));
        assertFalse(ht2.containsKey("key3"));
        assertTrue(ht2.containsKey("key4"));
        assertEquals("value14x", ht2.get("key4"));

        f2 = repoInfo2.getFields();
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

            RepositoryFieldInfoBO repoFieldInfo = new RepositoryFieldInfoBO();
            repoFieldInfo.setName("field1");
            repoFieldInfo.setDisplayName("Field Number 1");
            repoFieldInfo.setDescription("description");
            repoFieldInfo.setDataType(RepositoryFieldInfoBO.DataType.String);
            repoFieldInfo.getProperties().put("key1", "value11");
            repoFieldInfo.getProperties().put("key2", "value12");
            repoFieldInfo.getProperties().put("key3", "value13");
            repoInfo.getFields().add(repoFieldInfo);

            repoFieldInfo = new RepositoryFieldInfoBO();
            repoFieldInfo.setName("field1");
            repoFieldInfo.setDisplayName("Field Number 2");
            repoFieldInfo.setDescription("description");
            repoFieldInfo.setDataType(RepositoryFieldInfoBO.DataType.Integer);
            repoFieldInfo.getProperties().put("key1", "value21");
            repoFieldInfo.getProperties().put("key2", "value22");
            repoFieldInfo.getProperties().put("key3", "value23");
            repoInfo.getFields().add(repoFieldInfo);

            RepositoryInfoController.getInstance().save(_db, repoInfo);

            fail("Exception expected");
        }
        catch (ChiliLogException ex)
        {
            assertEquals(Strings.REPO_INFO_DUPLICATE_FIELD_NAME_ERROR, ex.getErrorCode());
        }
    }

    @Test
    public void testDuplicateName() throws ChiliLogException
    {
        try
        {
            // Insert
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

    @Test
    public void testParseString() throws Exception
    {
        RepositoryFieldInfoBO repoFieldInfo = new RepositoryFieldInfoBO();
        repoFieldInfo.setName("field1");
        repoFieldInfo.setDisplayName("Field Number 1");
        repoFieldInfo.setDescription("description");
        repoFieldInfo.setDataType(RepositoryFieldInfoBO.DataType.String);

        repoFieldInfo.loadDataTypeProperties();

        assertEquals("abc", repoFieldInfo.parse("abc"));
        assertEquals("", repoFieldInfo.parse(""));
        assertEquals(null, repoFieldInfo.parse(null));
    }

    @Test
    public void testParseInteger() throws Exception
    {
        RepositoryFieldInfoBO repoFieldInfo = new RepositoryFieldInfoBO();
        repoFieldInfo.setName("field1");
        repoFieldInfo.setDisplayName("Field Number 1");
        repoFieldInfo.setDescription("description");
        repoFieldInfo.setDataType(RepositoryFieldInfoBO.DataType.Integer);

        repoFieldInfo.loadDataTypeProperties();

        assertEquals(123, repoFieldInfo.parse("123"));
        assertEquals(123, repoFieldInfo.parse(" 123 "));

        try
        {
            repoFieldInfo.parse("123.45");
            fail();
        }
        catch (Exception ex)
        {
            assertEquals(NumberFormatException.class, ex.getClass());
        }

        try
        {
            repoFieldInfo.parse("");
            fail();
        }
        catch (Exception ex)
        {
            assertEquals(NumberFormatException.class, ex.getClass());
        }

        try
        {
            repoFieldInfo.parse("123adb");
            fail();
        }
        catch (Exception ex)
        {
            assertEquals(NumberFormatException.class, ex.getClass());
        }

        try
        {
            repoFieldInfo.parse(null);
            fail();
        }
        catch (Exception ex)
        {
            assertEquals(NumberFormatException.class, ex.getClass());
        }

        // Default values
        repoFieldInfo.getProperties().put(RepositoryFieldInfoBO.DEFAULT_VALUE_PROPERTY_NAME, "1");
        repoFieldInfo.loadDataTypeProperties();

        assertEquals(123, repoFieldInfo.parse("123"));
        assertEquals(123, repoFieldInfo.parse(" 123 "));
        assertEquals(1, repoFieldInfo.parse("abc"));
        assertEquals(1, repoFieldInfo.parse("123abc"));
        assertEquals(1, repoFieldInfo.parse(""));
        assertEquals(1, repoFieldInfo.parse(null));
    }

    @Test
    public void testParseIntegerNumberFormat() throws Exception
    {
        RepositoryFieldInfoBO repoFieldInfo = new RepositoryFieldInfoBO();
        repoFieldInfo.setName("field1");
        repoFieldInfo.setDisplayName("Field Number 1");
        repoFieldInfo.setDescription("description");
        repoFieldInfo.setDataType(RepositoryFieldInfoBO.DataType.Integer);
        repoFieldInfo.getProperties().put(RepositoryFieldInfoBO.NUMBER_FORMAT_PROPERTY_NAME, "#,##0");
        repoFieldInfo.loadDataTypeProperties();

        assertEquals(222222222, repoFieldInfo.parse("222222222"));
        assertEquals(1234, repoFieldInfo.parse("1,234"));
        assertEquals(123, repoFieldInfo.parse("123"));
        assertEquals(123, repoFieldInfo.parse(" 123 "));
        assertEquals(2222, repoFieldInfo.parse("2222d df22222"));
        assertEquals(123, repoFieldInfo.parse("123.11"));
        assertEquals(123, repoFieldInfo.parse("123.99"));

        try
        {
            repoFieldInfo.parse("");
            fail();
        }
        catch (Exception ex)
        {
            assertEquals(ParseException.class, ex.getClass());
        }

        try
        {
            repoFieldInfo.parse(null);
            fail();
        }
        catch (Exception ex)
        {
            assertEquals(NullPointerException.class, ex.getClass());
        }

        // Default values
        repoFieldInfo.getProperties().put(RepositoryFieldInfoBO.DEFAULT_VALUE_PROPERTY_NAME, "1");
        repoFieldInfo.loadDataTypeProperties();

        assertEquals(222222222, repoFieldInfo.parse("222222222"));
        assertEquals(1234, repoFieldInfo.parse("1,234"));
        assertEquals(123, repoFieldInfo.parse("123"));
        assertEquals(123, repoFieldInfo.parse(" 123 "));
        assertEquals(2222, repoFieldInfo.parse("2222d df22222"));
        assertEquals(1, repoFieldInfo.parse("abc"));
        assertEquals(123, repoFieldInfo.parse("123abc"));
        assertEquals(1, repoFieldInfo.parse(""));
        assertEquals(1, repoFieldInfo.parse(null));

    }

    @Test
    public void testParseLong() throws Exception
    {
        RepositoryFieldInfoBO repoFieldInfo = new RepositoryFieldInfoBO();
        repoFieldInfo.setName("field1");
        repoFieldInfo.setDisplayName("Field Number 1");
        repoFieldInfo.setDescription("description");
        repoFieldInfo.setDataType(RepositoryFieldInfoBO.DataType.Long);

        repoFieldInfo.loadDataTypeProperties();

        assertEquals(123L, repoFieldInfo.parse("123"));
        assertEquals(123L, repoFieldInfo.parse(" 123 "));

        try
        {
            repoFieldInfo.parse("123.45");
            fail();
        }
        catch (Exception ex)
        {
            assertEquals(NumberFormatException.class, ex.getClass());
        }

        try
        {
            repoFieldInfo.parse("");
            fail();
        }
        catch (Exception ex)
        {
            assertEquals(NumberFormatException.class, ex.getClass());
        }

        try
        {
            repoFieldInfo.parse("123adb");
            fail();
        }
        catch (Exception ex)
        {
            assertEquals(NumberFormatException.class, ex.getClass());
        }

        try
        {
            repoFieldInfo.parse(null);
            fail();
        }
        catch (Exception ex)
        {
            assertEquals(NumberFormatException.class, ex.getClass());
        }

        // Default values
        repoFieldInfo.getProperties().put(RepositoryFieldInfoBO.DEFAULT_VALUE_PROPERTY_NAME, "1");
        repoFieldInfo.loadDataTypeProperties();

        assertEquals(123L, repoFieldInfo.parse("123"));
        assertEquals(123L, repoFieldInfo.parse(" 123 "));
        assertEquals(1L, repoFieldInfo.parse("abc"));
        assertEquals(1L, repoFieldInfo.parse("123abc"));
        assertEquals(1L, repoFieldInfo.parse(""));
        assertEquals(1L, repoFieldInfo.parse(null));
    }

    @Test
    public void testParseLongNumberFormat() throws Exception
    {
        RepositoryFieldInfoBO repoFieldInfo = new RepositoryFieldInfoBO();
        repoFieldInfo.setName("field1");
        repoFieldInfo.setDisplayName("Field Number 1");
        repoFieldInfo.setDescription("description");
        repoFieldInfo.setDataType(RepositoryFieldInfoBO.DataType.Long);
        repoFieldInfo.getProperties().put(RepositoryFieldInfoBO.NUMBER_FORMAT_PROPERTY_NAME, "#,##0");

        repoFieldInfo.loadDataTypeProperties();

        assertEquals(222222222L, repoFieldInfo.parse("222222222"));
        assertEquals(1234L, repoFieldInfo.parse("1,234"));
        assertEquals(123L, repoFieldInfo.parse("123"));
        assertEquals(123L, repoFieldInfo.parse(" 123 "));
        assertEquals(2222L, repoFieldInfo.parse("2222d df22222"));
        assertEquals(123L, repoFieldInfo.parse("123.45"));
        assertEquals(123L, repoFieldInfo.parse("123.99"));

        try
        {
            repoFieldInfo.parse("");
            fail();
        }
        catch (Exception ex)
        {
            assertEquals(ParseException.class, ex.getClass());
        }

        try
        {
            repoFieldInfo.parse(null);
            fail();
        }
        catch (Exception ex)
        {
            assertEquals(NullPointerException.class, ex.getClass());
        }

        // Default values
        repoFieldInfo.getProperties().put(RepositoryFieldInfoBO.DEFAULT_VALUE_PROPERTY_NAME, "1");
        repoFieldInfo.loadDataTypeProperties();

        assertEquals(222222222L, repoFieldInfo.parse("222222222"));
        assertEquals(1234L, repoFieldInfo.parse("1,234"));
        assertEquals(123L, repoFieldInfo.parse("123"));
        assertEquals(123L, repoFieldInfo.parse(" 123 "));
        assertEquals(2222L, repoFieldInfo.parse("2222d df22222"));
        assertEquals(1L, repoFieldInfo.parse("abc"));
        assertEquals(123L, repoFieldInfo.parse("123abc"));
        assertEquals(1L, repoFieldInfo.parse(""));
        assertEquals(1L, repoFieldInfo.parse(null));

    }

    @Test
    public void testParseDouble() throws Exception
    {
        RepositoryFieldInfoBO repoFieldInfo = new RepositoryFieldInfoBO();
        repoFieldInfo.setName("field1");
        repoFieldInfo.setDisplayName("Field Number 1");
        repoFieldInfo.setDescription("description");
        repoFieldInfo.setDataType(RepositoryFieldInfoBO.DataType.Double);

        repoFieldInfo.loadDataTypeProperties();

        assertEquals(123d, repoFieldInfo.parse("123"));
        assertEquals(123d, repoFieldInfo.parse(" 123 "));
        assertEquals(123.45, repoFieldInfo.parse("123.45"));
        assertEquals(123.99, repoFieldInfo.parse("123.99"));

        try
        {
            repoFieldInfo.parse("");
            fail();
        }
        catch (Exception ex)
        {
            assertEquals(NumberFormatException.class, ex.getClass());
        }

        try
        {
            repoFieldInfo.parse("123adb");
            fail();
        }
        catch (Exception ex)
        {
            assertEquals(NumberFormatException.class, ex.getClass());
        }

        try
        {
            repoFieldInfo.parse(null);
            fail();
        }
        catch (Exception ex)
        {
            assertEquals(NullPointerException.class, ex.getClass());
        }

        // Default values
        repoFieldInfo.getProperties().put(RepositoryFieldInfoBO.DEFAULT_VALUE_PROPERTY_NAME, "1");
        repoFieldInfo.loadDataTypeProperties();

        assertEquals(123d, repoFieldInfo.parse("123"));
        assertEquals(123d, repoFieldInfo.parse(" 123 "));
        assertEquals(1d, repoFieldInfo.parse("abc"));
        assertEquals(1d, repoFieldInfo.parse("123abc"));
        assertEquals(1d, repoFieldInfo.parse(""));
        assertEquals(1d, repoFieldInfo.parse(null));
    }

    @Test
    public void testParseDoubleNumberFormat() throws Exception
    {
        RepositoryFieldInfoBO repoFieldInfo = new RepositoryFieldInfoBO();
        repoFieldInfo.setName("field1");
        repoFieldInfo.setDisplayName("Field Number 1");
        repoFieldInfo.setDescription("description");
        repoFieldInfo.setDataType(RepositoryFieldInfoBO.DataType.Double);
        repoFieldInfo.getProperties().put(RepositoryFieldInfoBO.NUMBER_FORMAT_PROPERTY_NAME, "#,##0");

        repoFieldInfo.loadDataTypeProperties();

        assertEquals(222222222d, repoFieldInfo.parse("222222222"));
        assertEquals(1234d, repoFieldInfo.parse("1,234"));
        assertEquals(123d, repoFieldInfo.parse("123"));
        assertEquals(123d, repoFieldInfo.parse(" 123 "));
        assertEquals(2222d, repoFieldInfo.parse("2222d df22222"));
        assertEquals(123.45, repoFieldInfo.parse("123.45"));
        assertEquals(123.99, repoFieldInfo.parse("123.99"));

        try
        {
            repoFieldInfo.parse("");
            fail();
        }
        catch (Exception ex)
        {
            assertEquals(ParseException.class, ex.getClass());
        }

        try
        {
            repoFieldInfo.parse(null);
            fail();
        }
        catch (Exception ex)
        {
            assertEquals(NullPointerException.class, ex.getClass());
        }

        // Default values
        repoFieldInfo.getProperties().put(RepositoryFieldInfoBO.DEFAULT_VALUE_PROPERTY_NAME, "1");
        repoFieldInfo.loadDataTypeProperties();

        assertEquals(222222222d, repoFieldInfo.parse("222222222"));
        assertEquals(1234d, repoFieldInfo.parse("1,234"));
        assertEquals(123d, repoFieldInfo.parse("123"));
        assertEquals(123d, repoFieldInfo.parse(" 123 "));
        assertEquals(2222d, repoFieldInfo.parse("2222d df22222"));
        assertEquals(1d, repoFieldInfo.parse("abc"));
        assertEquals(123d, repoFieldInfo.parse("123abc"));
        assertEquals(1d, repoFieldInfo.parse(""));
        assertEquals(1d, repoFieldInfo.parse(null));

    }

    @Test
    public void testParseBoolean() throws Exception
    {
        RepositoryFieldInfoBO repoFieldInfo = new RepositoryFieldInfoBO();
        repoFieldInfo.setName("field1");
        repoFieldInfo.setDisplayName("Field Number 1");
        repoFieldInfo.setDescription("description");
        repoFieldInfo.setDataType(RepositoryFieldInfoBO.DataType.Boolean);
        repoFieldInfo.loadDataTypeProperties();

        assertEquals(true, repoFieldInfo.parse("true"));
        assertEquals(true, repoFieldInfo.parse("True"));
        assertEquals(true, repoFieldInfo.parse("TRUE"));
        assertEquals(false, repoFieldInfo.parse("asfd"));
        assertEquals(false, repoFieldInfo.parse(""));

        try
        {
            repoFieldInfo.parse(null);
            fail();
        }
        catch (Exception ex)
        {
            assertEquals(NullPointerException.class, ex.getClass());
        }
    }

    @Test
    public void testParseBooleanFormat() throws Exception
    {
        RepositoryFieldInfoBO repoFieldInfo = new RepositoryFieldInfoBO();
        repoFieldInfo.setName("field1");
        repoFieldInfo.setDisplayName("Field Number 1");
        repoFieldInfo.setDescription("description");
        repoFieldInfo.setDataType(RepositoryFieldInfoBO.DataType.Boolean);
        repoFieldInfo.getProperties().put(RepositoryFieldInfoBO.TRUE_PATTERN_PROPERTY_NAME, "[\\s]*[A-Z]+[\\s]*");
        repoFieldInfo.loadDataTypeProperties();

        assertEquals(true, repoFieldInfo.parse("TRUE"));
        assertEquals(true, repoFieldInfo.parse("AAAAAA"));
        assertEquals(false, repoFieldInfo.parse("true"));
        assertEquals(false, repoFieldInfo.parse("True"));
        assertEquals(false, repoFieldInfo.parse("asfd123"));
        assertEquals(false, repoFieldInfo.parse(""));

        try
        {
            repoFieldInfo.parse(null);
            fail();
        }
        catch (Exception ex)
        {
            assertEquals(NullPointerException.class, ex.getClass());
        }
    }

    @Test
    public void testParseDate() throws Exception
    {
        RepositoryFieldInfoBO repoFieldInfo = new RepositoryFieldInfoBO();
        repoFieldInfo.setName("field1");
        repoFieldInfo.setDisplayName("Field Number 1");
        repoFieldInfo.setDescription("description");
        repoFieldInfo.setDataType(RepositoryFieldInfoBO.DataType.Date);
        repoFieldInfo.loadDataTypeProperties();

        assertEquals(new GregorianCalendar(2011, 0, 2, 3, 4, 5).getTime(), repoFieldInfo.parse("2011-01-02 03:04:05"));
        assertEquals(new GregorianCalendar(2011, 0, 2, 3, 4, 5).getTime(), repoFieldInfo.parse("2011-1-2 3:4:5"));
        assertEquals(new GregorianCalendar(2011, 0, 2, 3, 4, 5).getTime(),
                repoFieldInfo.parse("2011-1-2 3:4:5 this is not parsed"));

        try
        {
            repoFieldInfo.parse("xx 2011-1-2 3:4:5 zzz");
            fail();
        }
        catch (Exception ex)
        {
            assertEquals(ParseException.class, ex.getClass());
        }

        try
        {
            repoFieldInfo.parse("2011-01-02");
            fail();
        }
        catch (Exception ex)
        {
            assertEquals(ParseException.class, ex.getClass());
        }

        try
        {
            repoFieldInfo.parse("");
            fail();
        }
        catch (Exception ex)
        {
            assertEquals(ParseException.class, ex.getClass());
        }

        try
        {
            repoFieldInfo.parse(null);
            fail();
        }
        catch (Exception ex)
        {
            assertEquals(NullPointerException.class, ex.getClass());
        }

        // Default values
        repoFieldInfo.getProperties().put(RepositoryFieldInfoBO.DEFAULT_VALUE_PROPERTY_NAME, "2011-01-02 03:04:05");
        repoFieldInfo.loadDataTypeProperties();

        assertEquals(new GregorianCalendar(2011, 0, 2, 3, 4, 5).getTime(), repoFieldInfo.parse("123"));
    }

    @Test
    public void testParseDateFormat() throws Exception
    {
        RepositoryFieldInfoBO repoFieldInfo = new RepositoryFieldInfoBO();
        repoFieldInfo.setName("field1");
        repoFieldInfo.setDisplayName("Field Number 1");
        repoFieldInfo.setDescription("description");
        repoFieldInfo.setDataType(RepositoryFieldInfoBO.DataType.Date);
        repoFieldInfo.getProperties().put(RepositoryFieldInfoBO.DATE_FORMAT_PROPERTY_NAME, "yyyy-MM-dd HH:mm:ss,SSS");
        repoFieldInfo.loadDataTypeProperties();

        Date d = new GregorianCalendar(2011, 0, 2, 3, 4, 5).getTime();
        d.setTime(d.getTime() + 123);
        assertEquals(d, repoFieldInfo.parse("2011-01-02 03:04:05,123"));

        try
        {
            repoFieldInfo.parse("2011-01-02");
            fail();
        }
        catch (Exception ex)
        {
            assertEquals(ParseException.class, ex.getClass());
        }

        try
        {
            repoFieldInfo.parse("");
            fail();
        }
        catch (Exception ex)
        {
            assertEquals(ParseException.class, ex.getClass());
        }

        try
        {
            repoFieldInfo.parse(null);
            fail();
        }
        catch (Exception ex)
        {
            assertEquals(NullPointerException.class, ex.getClass());
        }

        // Default values
        repoFieldInfo.getProperties().put(RepositoryFieldInfoBO.DEFAULT_VALUE_PROPERTY_NAME, "2011-01-02 03:04:05,123");
        repoFieldInfo.loadDataTypeProperties();

        d = new GregorianCalendar(2011, 0, 2, 3, 4, 5).getTime();
        d.setTime(d.getTime() + 123);

        assertEquals(d, repoFieldInfo.parse("123"));
        assertEquals(d, repoFieldInfo.parse("xxxxx"));
    }

    @Test
    public void testParseDateFormatTimezone() throws Exception
    {
        RepositoryFieldInfoBO repoFieldInfo = new RepositoryFieldInfoBO();
        repoFieldInfo.setName("field1");
        repoFieldInfo.setDisplayName("Field Number 1");
        repoFieldInfo.setDescription("description");
        repoFieldInfo.setDataType(RepositoryFieldInfoBO.DataType.Date);
        repoFieldInfo.getProperties().put(RepositoryFieldInfoBO.DATE_FORMAT_PROPERTY_NAME, "yyyy-MM-dd HH:mm:ss,SSSZ");
        repoFieldInfo.loadDataTypeProperties();

        Date d = new GregorianCalendar(2011, 0, 2, 3, 4, 5).getTime();
        d.setTime(d.getTime() + 123);
        assertEquals(d, repoFieldInfo.parse("2011-01-02 03:04:05,123+1100"));

        // Set default timezone as UTC - i.e. all time is assumed to be UTC
        repoFieldInfo.getProperties().put(RepositoryFieldInfoBO.DATE_FORMAT_PROPERTY_NAME, "yyyy-MM-dd HH:mm:ss,SSS");
        repoFieldInfo.getProperties().put(RepositoryFieldInfoBO.DATE_TIMEZONE_PROPERTY_NAME, "UTC");
        repoFieldInfo.loadDataTypeProperties();

        GregorianCalendar c = new GregorianCalendar(2011, 0, 2, 3, 4, 5);
        c.setTimeZone(TimeZone.getTimeZone("UTC"));
        d = c.getTime();
        d.setTime(d.getTime() + 123);
        assertEquals(d, repoFieldInfo.parse("2011-01-02 03:04:05,123"));
    }

    @Test
    public void testPreParsingExamples()
    {
        // Strip white spaces
        Pattern p = Pattern.compile("[\\s]*([A-Z]+)[\\s]*");
        Matcher m = p.matcher("ABC");
        assertTrue(m.find());
        assertEquals("ABC", m.group(1));

        // http://download.oracle.com/javase/6/docs/api/java/util/regex/Pattern.html
        // Capturing groups are numbered by counting their opening parentheses from left to right. In the expression
        // ((A)(B(C))), for example, there are four such groups:
        //
        // 1 ((A)(B(C)))
        // 2 (A)
        // 3 (B(C))
        // 4 (C)
        // Group zero always stands for the entire expression.

        // Matching by group non capturing group
        p = Pattern.compile("^(?:[0-9]+ [\\w\\.\\[\\]]+ )([\\w\\.]+) ");
        m = p.matcher("913745345 [Main] com.test.abc - test");
        assertTrue(m.find());
        assertEquals("com.test.abc", m.group(1));

        m = p.matcher("test");
        assertFalse(m.find());

    }
    
    
}
