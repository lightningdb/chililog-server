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

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Hashtable;

import com.chililog.server.common.ChiliLogException;
import com.mongodb.DBObject;

/**
 * <p>
 * This class contains information that describes a field in a repository.
 * </p>
 * 
 * @author vibul
 * 
 */
public class RepositoryFieldInfoBO extends BO implements Serializable
{
    private static final long serialVersionUID = 1L;

    // Properties stored in mongoDB
    private String _name;
    private String _displayName;
    private String _description;
    private DataType _dataType;
    private Hashtable<String, String> _properties = new Hashtable<String, String>();

    static final String NAME_FIELD_NAME = "name";
    static final String DISPLAY_NAME_FIELD_NAME = "display_name";
    static final String DESCRIPTION_FIELD_NAME = "description";
    static final String DATA_TYPE_FIELD_NAME = "data_type";
    static final String PROPERTIES_FIELD_NAME = "properties";

    /**
     * Value to use if field cannot be parsed. Applies to all fields.
     */
    public static final String DEFAULT_VALUE_PROPERTY_NAME = "default_value";

    /**
     * Format of date as specified in {@link SimpleDateFormat}. If not supplied, defaults to "yyyy-MM-dd'T'HH:mm:ssZ".
     * For example, "2011-01-01T09:12:34GMT". Applies to Date fields.
     */
    public static final String DATE_FORMAT_PROPERTY_NAME = "date_format";

    /**
     * Timezone of date as specified in {@link SimpleDateFormat}. If not supplied, then the local timezone is assumed
     * (unless the timezone is supplied in the date format. Applies to Date fields.
     */
    public static final String DATE_TIMEZONE_PROPERTY_NAME = "date_timezone";

    /**
     * Format of the number as specified by {@link DecimalFormat}. If not set, standard number parsing will be used and
     * only digits are allowed. Applies to Integer and Long fields.
     */
    public static final String NUMBER_FORMAT_PROPERTY_NAME = "number_format";

    /**
     * For boolean fields, if there is a match with this regular expression pattern, True will be returned. Defaults to
     * case insensitive "true", Applies to Boolean fields.
     */
    public static final String TRUE_PATTERN_PROPERTY_NAME = "true_pattern";

    /**
     * Optional regular expression that can be used to extract a part of the string to parse. For example, if
     * <code>[1]</code> is the string value, then a pattern <code>^\[([0-9])\]$</code> will extract <code>1</code> for
     * parsing. Applies to all fields.
     */
    public static final String PREPARSE_PATTERN_PROPERTY_NAME = "preparse_pattern";

    /**
     * The group number of the text to extract from the preparse pattern. If not supplied, group 1 is assumed. Applies
     * to all fields.
     */
    public static final String PREPARSE_PATTERN_GROUP_PROPERTY_NAME = "preparse_pattern_group";

    /**
     * Basic constructor
     */
    public RepositoryFieldInfoBO()
    {
        return;
    }

    /**
     * Constructor that loads our properties retrieved from the mongoDB dbObject
     * 
     * @param repoInfo
     *            database object as retrieved from mongoDB
     * @param dbObject
     *            database object as retrieved from mongoDB
     * @throws ChiliLogException
     */
    RepositoryFieldInfoBO(DBObject dbObject) throws ChiliLogException
    {
        super(dbObject);
        _name = MongoUtils.getString(dbObject, NAME_FIELD_NAME, true);
        _displayName = MongoUtils.getString(dbObject, DISPLAY_NAME_FIELD_NAME, false);
        _description = MongoUtils.getString(dbObject, DESCRIPTION_FIELD_NAME, false);
        _dataType = DataType.valueOf(MongoUtils.getString(dbObject, DATA_TYPE_FIELD_NAME, true));
        _properties = MongoUtils.getKeyValuePairs(dbObject, PROPERTIES_FIELD_NAME, false);

        return;
    }

    /**
     * Puts our properties into the mongoDB object so that it can be saved
     * 
     * @param dbObject
     *            mongoDB database object that can be used for saving
     */
    @Override
    protected void savePropertiesToDBObject(DBObject dbObject) throws ChiliLogException
    {
        MongoUtils.setString(dbObject, NAME_FIELD_NAME, _name);
        MongoUtils.setString(dbObject, DISPLAY_NAME_FIELD_NAME, _displayName);
        MongoUtils.setString(dbObject, DESCRIPTION_FIELD_NAME, _description);
        MongoUtils.setString(dbObject, DATA_TYPE_FIELD_NAME, _dataType.toString());
        MongoUtils.setKeyValuePairs(dbObject, PROPERTIES_FIELD_NAME, _properties);
    }

    /**
     * Returns the unique name for this field. This name is used for storing the field in mongoDB
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
     * Returns the user friendly display name for this field. This is used when display the field on the UI.
     */
    public String getDisplayName()
    {
        return _displayName;
    }

    public void setDisplayName(String displayName)
    {
        _displayName = displayName;
    }

    /**
     * Returns a description of this field
     */
    public String getDescription()
    {
        return _description;
    }

    public void setDescription(String description)
    {
        _description = description;
    }

    /**
     * Returns the <code>DataType</code> for this field
     */
    public DataType getDataType()
    {
        return _dataType;
    }

    public void setDataType(DataType dataType)
    {
        _dataType = dataType;
    }

    /**
     * Returns a list of parser specific properties for this repository field
     */
    public Hashtable<String, String> getProperties()
    {
        return _properties;
    }

    /**
     * Data type for this field
     * 
     * @author vibul
     * 
     */
    public enum DataType
    {
        /**
         * <p>
         * The field is saved into mongoDB as a string.
         * </p>
         * <p>
         * Properties:
         * <dl>
         * <dt>preparse_pattern</dt>
         * <dd>Regular expression to match before performing parsing. This can be used to strip out unwanted text before
         * parsing. If not matches found, the pre-parsing is ignored and the text value is parsed without modification.
         * If not set, pre-parsing is not performed.</dd>
         * 
         * <dt>preparse_pattern_group</dt>
         * <dd>The group number of the matching regular expression pattern to extract. Defaults to 1 - the first group.</dd>
         * 
         * <dt>default_value</dt>
         * <dd>If set, this value is used if the string is blank (null, empty or only contains whitespaces.</dd>
         * </dl>
         * </p>
         */
        String,

        /**
         * <p>
         * The field is saved into mongoDB as an integer.
         * </p>
         * <p>
         * Properties:
         * <dl>
         * <dt>preparse_pattern</dt>
         * <dd>Regular expression to match before performing parsing. This can be used to strip out unwanted text before
         * parsing. If not matches found, the pre-parsing is ignored and the text value is parsed without modification.
         * If not set, pre-parsing is not performed.</dd>
         * 
         * <dt>preparse_pattern_group</dt>
         * <dd>The group number of the matching regular expression pattern to extract. Defaults to 1 - the first group.</dd>
         * 
         * <dt>number_format</dt>
         * <dd>The number format pattern as defined by the Java <code>DecimalFormat</code> class. If set, this pattern
         * is used for parsing. It takes into account formatting characters like the thousand separators. If not set,
         * then an parsing is performed using <code>Integer.parseInteger</code> and only digits are permitted.</dd>
         * 
         * <dt>default_value</dt>
         * <dd>If set, this value is used if the double value cannot be parsed. If <code>number_format</code> is set,
         * the default value must match that format.</dd>
         * </dl>
         * </p>
         */
        Integer,

        /**
         * <p>
         * The field is saved into mongoDB as a long integer.
         * </p>
         * <p>
         * Properties:
         * <dl>
         * <dt>preparse_pattern</dt>
         * <dd>Regular expression to match before performing parsing. This can be used to strip out unwanted text before
         * parsing. If not matches found, the pre-parsing is ignored and the text value is parsed without modification.
         * If not set, pre-parsing is not performed.</dd>
         * 
         * <dt>preparse_pattern_group</dt>
         * <dd>The group number of the matching regular expression pattern to extract. Defaults to 1 - the first group.</dd>
         * 
         * <dt>number_format</dt>
         * <dd>The number format pattern as defined by the Java <code>DecimalFormat</code> class. If set, this pattern
         * is used for parsing. It takes into account formatting characters like the thousand separators. If not set,
         * then an parsing is performed using <code>Long.parseLong</code> and only digits are permitted.</dd>
         * 
         * <dt>default_value</dt>
         * <dd>If set, this value is used if the double value cannot be parsed. If <code>number_format</code> is set,
         * the default value must match that format.</dd>
         * </dl>
         * </p>
         */
        Long,

        /**
         * <p>
         * The field is saved into mongoDB as a date
         * </p>
         * <p>
         * Properties:
         * <dl>
         * <dt>preparse_pattern</dt>
         * <dd>Regular expression to match before performing parsing. This can be used to strip out unwanted text before
         * parsing. If not matches found, the pre-parsing is ignored and the text value is parsed without modification.
         * If not set, pre-parsing is not performed.</dd>
         * 
         * <dt>preparse_pattern_group</dt>
         * <dd>The group number of the matching regular expression pattern to extract. Defaults to 1 - the first group.</dd>
         * 
         * <dt>date_format</dt>
         * <dd>The date format pattern as defined by the Java <code>java.text.SimpleDateFormat</code> class. If set,
         * this pattern is used for parsing. If not set, the default "<code>yyyy-MM-dd HH:mm:ss</code>" format is used.</dd>
         * 
         * <dt>date_timezone</dt>
         * <dd>The assumed timezone of a date as defined by the <code>java.util.TimeZone</code> class. If not set, the
         * current JVM local timezone is assumed.</dd>
         * 
         * <dt>default_value</dt>
         * <dd>If set, this value is used if the double value cannot be parsed. If <code>date_format</code> is set, the
         * default value must match that format.</dd>
         * </dl>
         * </p>
         */
        Date,

        /**
         * <p>
         * The field is saved into mongoDB as a boolean (True/False)
         * </p>
         * <p>
         * Properties:
         * <dl>
         * <dt>preparse_pattern</dt>
         * <dd>Regular expression to match before performing parsing. This can be used to strip out unwanted text before
         * parsing. If not matches found, the pre-parsing is ignored and the text value is parsed without modification.
         * If not set, pre-parsing is not performed.</dd>
         * 
         * <dt>preparse_pattern_group</dt>
         * <dd>The group number of the matching regular expression pattern to extract. Defaults to 1 - the first group.</dd>
         * 
         * <dt>true_pattern</dt>
         * <dd>Regular expression to match against the string. A match returns a "true". If not specified, then the
         * match pattern is set to "[Tt][Rr][Uu][Ee]" - "true" ignoring case.</dd>
         * </dl>
         * </p>
         */
        Boolean,

        /**
         * <p>
         * The field is saved into mongoDB as a long integer.
         * </p>
         * <p>
         * Properties:
         * <dl>
         * <dt>preparse_pattern</dt>
         * <dd>Regular expression to match before performing parsing. This can be used to strip out unwanted text before
         * parsing. If not matches found, the pre-parsing is ignored and the text value is parsed without modification.
         * If not set, pre-parsing is not performed.</dd>
         * 
         * <dt>preparse_pattern_group</dt>
         * <dd>The group number of the matching regular expression pattern to extract. Defaults to 1 - the first group.</dd>
         * 
         * <dt>number_format</dt>
         * <dd>The number format pattern as defined by the Java <code>DecimalFormat</code> class. If set, this pattern
         * is used for parsing. It takes into account formatting characters like the thousand separators. If not set,
         * then an parsing is performed using <code>Double.parseDouble</code> and only digits and decimal points are
         * permitted.</dd>
         * 
         * <dt>default_value</dt>
         * <dd>If set, this value is used if the double value cannot be parsed. If <code>number_format</code> is set,
         * the default value must match that format.</dd>
         * </dl>
         * </p>
         */
        Double
    }

}
