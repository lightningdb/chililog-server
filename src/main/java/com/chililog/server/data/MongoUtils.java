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

import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;

import com.chililog.server.common.ChiliLogException;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * mongoDB functions for loading and saving fields
 * 
 * @author vibul
 * 
 */
public class MongoUtils
{
    /**
     * Loads a string field from the mongoDB object
     * 
     * @param dbObject
     *            mongoDB DBObject from which data is to be loaded
     * @param fieldName
     *            name of field to load
     * @param isRequired
     *            flag to indicate if field is required.
     * @return String value of field
     * @throws ChiliLogException
     *             if field is required and it is not found or contains blanks
     */
    public static String getString(DBObject dbObject, String fieldName, boolean isRequired) throws ChiliLogException
    {
        if (dbObject == null)
        {
            throw new IllegalArgumentException("dbObject is null");
        }

        String s = (String) dbObject.get(fieldName);
        if (isRequired && StringUtils.isBlank(s))
        {
            throw new ChiliLogException(Strings.MONGODB_FIELD_NOT_FOUND_ERROR, fieldName);
        }
        return s;
    }

    /**
     * Saves a string field to the mongoDB object
     * 
     * @param dbObject
     *            mongoDB DBObject to which data will be saved
     * @param fieldName
     *            name of field to save
     * @param fieldValue
     *            value of field to save
     */
    public static void setString(DBObject dbObject, String fieldName, String fieldValue)
    {
        if (dbObject == null)
        {
            throw new IllegalArgumentException("dbObject is null");
        }
        dbObject.put(fieldName, fieldValue);
    }

    /**
     * Loads a Date field from the mongoDB object
     * 
     * @param dbObject
     *            mongoDB DBObject from which data is to be loaded
     * @param fieldName
     *            name of field to load
     * @param isRequired
     *            flag to indicate if field is required.
     * @return String value of field
     * @throws ChiliLogException
     *             if field is required and it is not found or contains blanks
     */
    public static Date getDate(DBObject dbObject, String fieldName, boolean isRequired) throws ChiliLogException
    {
        if (dbObject == null)
        {
            throw new IllegalArgumentException("dbObject is null");
        }

        Date d = (Date) dbObject.get(fieldName);
        if (isRequired && d == null)
        {
            throw new ChiliLogException(Strings.MONGODB_FIELD_NOT_FOUND_ERROR, fieldName);
        }
        return d;
    }

    /**
     * Saves a Date field to the mongoDB object
     * 
     * @param dbObject
     *            mongoDB DBObject to which data will be saved
     * @param fieldName
     *            name of field to save
     * @param fieldValue
     *            value of field to save
     */
    public static void setDate(DBObject dbObject, String fieldName, Date fieldValue)
    {
        if (dbObject == null)
        {
            throw new IllegalArgumentException("dbObject is null");
        }
        dbObject.put(fieldName, fieldValue);
    }

    /**
     * Loads a Boolean field from the mongoDB object
     * 
     * @param dbObject
     *            mongoDB DBObject from which data is to be loaded
     * @param fieldName
     *            name of field to load
     * @param isRequired
     *            flag to indicate if field is required.
     * @return String value of field
     * @throws ChiliLogException
     *             if field is required and it is not found or contains blanks
     */
    public static Boolean getBoolean(DBObject dbObject, String fieldName, boolean isRequired) throws ChiliLogException
    {
        if (dbObject == null)
        {
            throw new IllegalArgumentException("dbObject is null");
        }

        Boolean b = (Boolean) dbObject.get(fieldName);
        if (isRequired && b == null)
        {
            throw new ChiliLogException(Strings.MONGODB_FIELD_NOT_FOUND_ERROR, fieldName);
        }
        return b;
    }

    /**
     * Saves a Boolean field to the mongoDB object
     * 
     * @param dbObject
     *            mongoDB DBObject to which data will be saved
     * @param fieldName
     *            name of field to save
     * @param fieldValue
     *            value of field to save
     */
    public static void setBoolean(DBObject dbObject, String fieldName, Boolean fieldValue)
    {
        if (dbObject == null)
        {
            throw new IllegalArgumentException("dbObject is null");
        }
        dbObject.put(fieldName, fieldValue);
    }

    /**
     * <p>
     * Loads a Long Integer field from the mongoDB object.
     * </p>
     * <p>
     * We extensively use long integers because as of version 1.8, 32 bit integers are not supported in the mongo shell.
     * </p>
     * 
     * @param dbObject
     *            mongoDB DBObject from which data is to be loaded
     * @param fieldName
     *            name of field to load
     * @param isRequired
     *            flag to indicate if field is required.
     * @return String value of field
     * @throws ChiliLogException
     *             if field is required and it is not found or contains blanks
     */
    public static Long getLong(DBObject dbObject, String fieldName, boolean isRequired) throws ChiliLogException
    {
        if (dbObject == null)
        {
            throw new IllegalArgumentException("dbObject is null");
        }

        Long i = (Long) dbObject.get(fieldName);
        if (isRequired && i == null)
        {
            throw new ChiliLogException(Strings.MONGODB_FIELD_NOT_FOUND_ERROR, fieldName);
        }
        return i;
    }

    /**
     * Saves a Long Integer field to the mongoDB object
     * 
     * @param dbObject
     *            mongoDB DBObject to which data will be saved
     * @param fieldName
     *            name of field to save
     * @param fieldValue
     *            value of field to save
     */
    public static void setLong(DBObject dbObject, String fieldName, Long fieldValue)
    {
        if (dbObject == null)
        {
            throw new IllegalArgumentException("dbObject is null");
        }
        dbObject.put(fieldName, fieldValue);
    }

    /**
     * Loads a string array list field from the mongoDB object
     * 
     * @param dbObject
     *            mongoDB DBObject to load string
     * @param fieldName
     *            name of field to load
     * @param isRequired
     *            flag to indicate if field is required.
     * @return Value of field as a <code>ArrayList&lt;String&gt;</code>
     * @throws ChiliLogException
     *             if field is required and it is not found
     */
    public static ArrayList<String> getStringArrayList(DBObject dbObject, String fieldName, boolean isRequired)
            throws ChiliLogException
    {
        BasicDBList list = (BasicDBList) dbObject.get(fieldName);
        if (isRequired && list == null)
        {
            throw new ChiliLogException(Strings.MONGODB_FIELD_NOT_FOUND_ERROR, fieldName);
        }

        ArrayList<String> outList = new ArrayList<String>();
        if (list != null && list.size() > 0)
        {
            for (Object item : list)
            {
                outList.add((String) item);
            }
        }
        return outList;
    }

    /**
     * Saves a string array list field to the mongoDB object
     * 
     * @param dbObject
     *            mongoDB DBObject to which data will be saved
     * @param fieldName
     *            name of field to save
     * @param fieldValue
     *            value of field to save
     */
    public static void setStringArrayList(DBObject dbObject, String fieldName, ArrayList<String> fieldValue)
    {
        if (dbObject == null)
        {
            throw new IllegalArgumentException("dbObject is null");
        }
        dbObject.put(fieldName, fieldValue);
    }

    /**
     * <p>
     * Loads a key-value pair as a hashtable. Javascript representation is
     * </p>
     * <p>
     * <code>
     * fieldName: { key1: value1, key2: value2 },
     * </code>
     * </p>
     * 
     * @param dbObject
     *            mongoDB DBObject to load string
     * @param fieldName
     *            name of field to load
     * @param isRequired
     *            flag to indicate if field is required.
     * @return Value of field as a <code>Hashtable&lt;String, String&gt;</code>
     * @throws ChiliLogException
     *             if field is required and it is not found
     */
    public static Hashtable<String, String> getKeyValuePairs(DBObject dbObject, String fieldName, boolean isRequired)
            throws ChiliLogException
    {
        BasicDBObject obj = (BasicDBObject) dbObject.get(fieldName);
        if (isRequired && obj == null)
        {
            throw new ChiliLogException(Strings.MONGODB_FIELD_NOT_FOUND_ERROR, fieldName);
        }

        Hashtable<String, String> hashtable = new Hashtable<String, String>();
        if (obj != null)
        {
            for (Entry<String, Object> s : obj.entrySet())
            {
                String key = s.getKey();
                String value = s.getValue().toString();
                hashtable.put(key, value);
            }
        }
        return hashtable;
    }

    /**
     * Saves a key-value map to the mongoDB object
     * 
     * @param dbObject
     *            mongoDB DBObject to which data will be saved
     * @param fieldName
     *            name of field to save
     * @param fieldValue
     *            value of field to save
     */
    public static void setKeyValuePairs(DBObject dbObject, String fieldName, Hashtable<String, String> fieldValue)
    {
        if (dbObject == null)
        {
            throw new IllegalArgumentException("dbObject is null");
        }
        BasicDBObject obj = new BasicDBObject();
        for (Entry<String, String> s : fieldValue.entrySet())
        {
            String key = s.getKey();
            String value = s.getValue();
            obj.put(key, value);
        }
        dbObject.put(fieldName, obj);
    }
}
