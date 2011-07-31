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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;

import org.chililog.server.common.ChiliLogException;
import org.chililog.server.engine.parsers.EntryParser;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * <p>
 * Details how to parse incoming text entries
 * </p>
 * 
 * @author vibul
 * 
 */
public class RepositoryParserInfoBO extends BO implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String _name;
    private AppliesTo _appliesTo = AppliesTo.None;
    private String _appliesToSourceFilter;
    private String _appliesToHostFilter;
    private String _className;
    private long _maxKeywords = -2;
    private ParseFieldErrorHandling _parseFieldErrorHandling = ParseFieldErrorHandling.SkipField;
    private ArrayList<RepositoryFieldInfoBO> _fields = new ArrayList<RepositoryFieldInfoBO>();
    private Hashtable<String, String> _properties = new Hashtable<String, String>();

    static final String NAME_FIELD_NAME = "name";
    static final String APPLIES_TO_FIELD_NAME = "applies_to_source";
    static final String APPLIES_TO_SOURCE_FILTER_FIELD_NAME = "applies_to_source_filter";
    static final String APPLIES_TO_HOST_FILTER_FIELD_NAME = "applies_to_host_filter";
    static final String CLASS_NAME_FIELD_NAME = "class_name";
    static final String MAX_KEYWORDS = "max_keywords";
    static final String PARSE_FIELD_ERROR_HANDLING_FIELD_NAME = "parse_field_error_handling";
    static final String FIELDS_FIELD_NAME = "fields";
    static final String PROPERTIES_FIELD_NAME = "properties";

    public static final long MAX_KEYWORDS_UNLIMITED = -1;
    public static final long MAX_KEYWORDS_INHERITED = -2;

    
    /**
     * Basic constructor
     */
    public RepositoryParserInfoBO()
    {
        return;
    }

    /**
     * Constructor that loads our properties retrieved from the mongoDB dbObject
     * 
     * @param dbObject
     *            database object as retrieved from mongoDB
     * @throws ChiliLogException
     */
    public RepositoryParserInfoBO(DBObject dbObject) throws ChiliLogException
    {
        super(dbObject);
        _name = MongoUtils.getString(dbObject, NAME_FIELD_NAME, true);

        _appliesTo = Enum.valueOf(AppliesTo.class, MongoUtils.getString(dbObject, APPLIES_TO_FIELD_NAME, true));
        _appliesToSourceFilter = MongoUtils.getString(dbObject, APPLIES_TO_SOURCE_FILTER_FIELD_NAME, false);
        _appliesToHostFilter = MongoUtils.getString(dbObject, APPLIES_TO_HOST_FILTER_FIELD_NAME, false);

        _className = MongoUtils.getString(dbObject, CLASS_NAME_FIELD_NAME, true);

        _maxKeywords = MongoUtils.getLong(dbObject, MAX_KEYWORDS, true);

        _parseFieldErrorHandling = ParseFieldErrorHandling.valueOf(MongoUtils.getString(dbObject,
                PARSE_FIELD_ERROR_HANDLING_FIELD_NAME, true));

        BasicDBList list = (BasicDBList) dbObject.get(FIELDS_FIELD_NAME);
        ArrayList<RepositoryFieldInfoBO> fieldList = new ArrayList<RepositoryFieldInfoBO>();
        if (list != null && list.size() > 0)
        {
            for (Object item : list)
            {
                RepositoryFieldInfoBO field = new RepositoryFieldInfoBO((DBObject) item);
                fieldList.add(field);
            }
        }
        _fields = fieldList;

        _properties = MongoUtils.getKeyValuePairs(dbObject, PROPERTIES_FIELD_NAME, false);

        return;
    }

    /**
     * Puts our properties into the mongoDB object so that it can be saved
     * 
     * @param dbObject
     *            mongoDB database object that can be used for saving
     * @throws ChiliLogException
     */
    @Override
    protected void savePropertiesToDBObject(DBObject dbObject) throws ChiliLogException
    {
        MongoUtils.setString(dbObject, NAME_FIELD_NAME, _name, true);

        MongoUtils.setString(dbObject, APPLIES_TO_FIELD_NAME, _appliesTo.toString(), true);
        MongoUtils.setString(dbObject, APPLIES_TO_SOURCE_FILTER_FIELD_NAME, _appliesToSourceFilter, false);
        MongoUtils.setString(dbObject, APPLIES_TO_HOST_FILTER_FIELD_NAME, _appliesToHostFilter, false);

        MongoUtils.setString(dbObject, CLASS_NAME_FIELD_NAME, _className, true);

        MongoUtils.setLong(dbObject, MAX_KEYWORDS, _maxKeywords, true);

        MongoUtils.setString(dbObject, PARSE_FIELD_ERROR_HANDLING_FIELD_NAME, _parseFieldErrorHandling.toString(), true);

        ArrayList<DBObject> fieldList = new ArrayList<DBObject>();
        for (RepositoryFieldInfoBO field : _fields)
        {
            BasicDBObject obj = new BasicDBObject();
            field.savePropertiesToDBObject(obj);
            fieldList.add(obj);
        }
        dbObject.put(FIELDS_FIELD_NAME, fieldList);

        MongoUtils.setKeyValuePairs(dbObject, PROPERTIES_FIELD_NAME, _properties, false);
        return;
    }

    /**
     * Name of this instance of this parser. Helps to identify the parser in the event of an error.
     */
    public String getName()
    {
        return _name;
    }

    public void setName(String name)
    {
        _name = name;
    }

    /**
     * Returns if this parser applies to sources, and if so how it is to be applied.
     */
    public AppliesTo getAppliesTo()
    {
        return _appliesTo;
    }

    public void setAppliesTo(AppliesTo appliesTo)
    {
        _appliesTo = appliesTo;
    }

    /**
     * Returns the filters to limit the sources to which this parser is to apply
     */
    public String getAppliesToSourceFilter()
    {
        return _appliesToSourceFilter;
    }

    public void setAppliesToSourceFilter(String appliesToSourceFilter)
    {
        _appliesToSourceFilter = appliesToSourceFilter;
    }

    /**
     * Returns the filters to limit the hosts to which this parser is to apply
     */
    public String getAppliesToHostFilter()
    {
        return _appliesToHostFilter;
    }

    public void setAppliesToHostFilter(String appliesToHostFilter)
    {
        _appliesToHostFilter = appliesToHostFilter;
    }

    /**
     * Full name of class to use for parsing. Class must extend {@link EntryParser}. For example,
     * <code>com.chililog.server.engine.parsers.DelimitedEntryParser</code>
     */
    public String getClassName()
    {
        return _className;
    }

    public void setClassName(String className)
    {
        _className = className;
    }

    /**
     * Maximum number of keywords to be stored per entry
     */
    public long getMaxKeywords()
    {
        return _maxKeywords;
    }

    public void setMaxKeywords(long maxKeywords)
    {
        _maxKeywords = maxKeywords;
    }

    /**
     * Returns a list fields that is to be parsed and stored in this repository
     */
    public ArrayList<RepositoryFieldInfoBO> getFields()
    {
        return _fields;
    }

    /**
     * Returns a list of parser specific properties for this repository
     */
    public Hashtable<String, String> getProperties()
    {
        return _properties;
    }

    /**
     * Returns the error handling technique to use when parsing a field
     */
    public ParseFieldErrorHandling getParseFieldErrorHandling()
    {
        return _parseFieldErrorHandling;
    }

    public void setParseFieldErrorHandling(ParseFieldErrorHandling parseFieldErrorHandling)
    {
        _parseFieldErrorHandling = parseFieldErrorHandling;
    }

    /**
     * Technique to use if there is an error during parsing a field in a repository entry
     */
    public static enum ParseFieldErrorHandling
    {
        /**
         * The field will not be written as part of the log entry in the repository and n log entry will be written to
         * ChiliLog
         */
        SkipFieldIgnoreError,

        /**
         * The field will not be written as part of the log entry in the repository and a log entry will be written to
         * ChiliLog
         */
        SkipField,

        /**
         * The entire log entry will not be written to the repository and a log entry will be written to ChiliLog
         */
        SkipEntry,

    }

    /**
     * Defines if a parser is to be applied to a source or host
     */
    public static enum AppliesTo
    {
        /**
         * Parser is NOT to be used for any sources and/or hosts
         */
        None,

        /**
         * Parser is to be used for all sources and/or hosts
         */
        All,

        /**
         * Filtered Source Comma Separated Values. For example, <code>source1, source2</code> in the filter specifies
         * that the parser will only be used for "source1" and "source2".
         */
        AllowFilteredCSV,

        /**
         * Regular expression pattern to match the name of the source. For example, <code>^source[1-2]$</code> will
         * match sources named "source1" and "source2".
         */
        AllowFilteredRegularExpression

    }
}
