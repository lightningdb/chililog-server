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
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;

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
public class RepositoryFieldInfoBO extends BaseBO implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String _name;
    private String _displayName;
    private String _description;
    private DataType _dataType;
    private Hashtable<String, String> _properties = new Hashtable<String, String>();

    private String _dateFormat = null;
    private TimeZone _dateTimezone = null;
    private Object _defaultValue = null;
    private NumberFormat _numberFormatter = null;
    private Pattern _truePattern = null;
    private Pattern _preparsePattern = null;
    private int _preparsePatternGroup = 1;;

    static final String NAME_FIELD_NAME = "name";
    static final String DISPLAY_NAME_FIELD_NAME = "display_name";
    static final String DESCRIPTION_FIELD_NAME = "description";
    static final String DATA_TYPE_FIELD_NAME = "data_type";
    static final String PROPERTIES_FIELD_NAME = "properties";

    public static final String DATE_FORMAT_PROPERTY_NAME = "date_format";
    public static final String DATE_TIMEZONE_PROPERTY_NAME = "date_timezone";
    public static final String DEFAULT_VALUE_PROPERTY_NAME = "default_value";
    public static final String NUMBER_FORMAT_PROPERTY_NAME = "number_format";
    public static final String TRUE_PATTERN_PROPERTY_NAME = "true_pattern";
    public static final String PREPARSE_PATTERN_PROPERTY_NAME = "preparse_pattern";
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

        loadDataTypeProperties();

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

    public void setDataType(DataType data_type)
    {
        _dataType = data_type;
    }

    /**
     * Returns a list of parser specific properties for this repository field
     */
    public Hashtable<String, String> getProperties()
    {
        return _properties;
    }

    /**
     * Load properties required for parsing
     * 
     * @throws ChiliLogException
     *             if error loading properties for this data type
     */
    public void loadDataTypeProperties() throws ChiliLogException
    {
        try
        {
            String s = _properties.get(PREPARSE_PATTERN_PROPERTY_NAME);
            if (!StringUtils.isBlank(s))
            {
                _preparsePattern = Pattern.compile(s);
            }
            String g = _properties.get(PREPARSE_PATTERN_GROUP_PROPERTY_NAME);
            if (!StringUtils.isBlank(g))
            {
                _preparsePatternGroup = Integer.parseInt(g);
            }

            switch (_dataType)
            {
                case String:
                    loadStringProperties();
                case Integer:
                    loadIntegerProperties();
                    break;
                case Long:
                    loadLongProperties();
                    break;
                case Date:
                    loadDateProperties();
                    break;
                case Boolean:
                    loadBooleanProperties();
                    break;
                case Double:
                    loadDoubleProperties();
                    break;
                default:
                    throw new NotImplementedException("Data type " + _dataType.toString());
            }
        }
        catch (Exception ex)
        {
            throw new ChiliLogException(ex, Strings.REPO_INFO_FIELD_PROPERTIES_ERROR, _name);
        }
    }

    /**
     * <p>
     * Parse some text and return the strongly typed value.
     * </p>
     * <p>
     * Please call <code>loadProperties</code> before parsing to setup properties.
     * </p>
     * 
     * @param textValue
     *            text string to parse
     * @return Strongly typed value as defined by this field's data type
     * @throws Exception
     *             if there is an error during parsing
     */
    public Object parse(String textValue) throws Exception
    {

        if (_preparsePattern != null)
        {
            // Get the 1st matching group
            Matcher m = _preparsePattern.matcher(textValue);
            if (m.find())
            {
                textValue = m.group(_preparsePatternGroup);
            }
        }

        switch (_dataType)
        {
            case String:
                return parseString(textValue);
            case Integer:
                return parseInteger(textValue);
            case Long:
                return parseLong(textValue);
            case Date:
                return parseDate(textValue);
            case Boolean:
                return parseBoolean(textValue);
            case Double:
                return parseDouble(textValue);
            default:
                throw new NotImplementedException("Data type " + _dataType.toString());
        }
    }

    /**
     * Load string properties
     * 
     * @throws ParseException
     */
    private void loadStringProperties() throws ParseException
    {
        _defaultValue = _properties.get(DEFAULT_VALUE_PROPERTY_NAME);
    }

    /**
     * Parse our string
     * 
     * @param value
     *            value to parse
     * @return parsed string
     */
    private String parseString(String value)
    {
        if (StringUtils.isBlank(value) && _defaultValue != null)
        {
            return (String) _defaultValue;
        }
        return value;
    }

    /**
     * Load integer properties
     * 
     * @throws ParseException
     */
    private void loadIntegerProperties() throws ParseException
    {
        String d = _properties.get(DEFAULT_VALUE_PROPERTY_NAME);
        String s = _properties.get(NUMBER_FORMAT_PROPERTY_NAME);
        if (!StringUtils.isBlank(s))
        {
            _numberFormatter = new DecimalFormat(s);
        }

        if (!StringUtils.isBlank(d))
        {
            if (_numberFormatter == null)
            {
                _defaultValue = Integer.parseInt(d);
            }
            else
            {
                _defaultValue = _numberFormatter.parse(d).intValue();
            }
        }
    }

    /**
     * <p>
     * Parse an integer.
     * </p>
     * <p>
     * If a number patter is set, the the number pattern is used for parsing. Otherwise, standard integer parsing is
     * performed where only digits are allowed.
     * </p>
     * <p>
     * If parsing fails and a default value is set, the default value is returned.
     * </p>
     * 
     * @param value
     *            string value to parse into an integer
     * @return integer value.
     * @throws ParseException
     */
    private Integer parseInteger(String value) throws ParseException
    {
        try
        {
            if (!StringUtils.isBlank(value))
            {
                value = value.trim();
            }
            if (_numberFormatter == null)
            {
                return Integer.parseInt(value);
            }
            else
            {
                return _numberFormatter.parse(value).intValue();
            }
        }
        catch (ParseException ex)
        {
            if (_defaultValue != null)
            {
                return (Integer) _defaultValue;
            }
            throw ex;
        }
        catch (NumberFormatException ex2)
        {
            if (_defaultValue != null)
            {
                return (Integer) _defaultValue;
            }
            throw ex2;
        }
        catch (NullPointerException ex3)
        {
            if (_defaultValue != null)
            {
                return (Integer) _defaultValue;
            }
            throw ex3;
        }
    }

    /**
     * Load long integer properties
     * 
     * @throws ParseException
     */
    private void loadLongProperties() throws ParseException
    {
        String d = _properties.get(DEFAULT_VALUE_PROPERTY_NAME);
        String s = _properties.get(NUMBER_FORMAT_PROPERTY_NAME);
        if (!StringUtils.isBlank(s))
        {
            _numberFormatter = new DecimalFormat(s);
        }

        if (!StringUtils.isBlank(d))
        {
            if (_numberFormatter == null)
            {
                _defaultValue = Long.parseLong(d);
            }
            else
            {
                _defaultValue = _numberFormatter.parse(d).longValue();
            }
        }
    }

    /**
     * <p>
     * Parse a long integer.
     * </p>
     * <p>
     * If a number patter is set, the the number pattern is used for parsing. Otherwise, standard integer parsing is
     * performed where only digits are allowed.
     * </p>
     * <p>
     * If parsing fails and a default value is set, the default value is returned.
     * </p>
     * 
     * @param value
     *            string value to parse into an integer
     * @return integer value.
     * @throws ParseException
     */
    private Long parseLong(String value) throws ParseException
    {
        try
        {
            if (!StringUtils.isBlank(value))
            {
                value = value.trim();
            }
            if (_numberFormatter == null)
            {
                return Long.parseLong(value);
            }
            else
            {
                return _numberFormatter.parse(value).longValue();
            }
        }
        catch (ParseException ex)
        {
            if (_defaultValue != null)
            {
                return (Long) _defaultValue;
            }
            throw ex;
        }
        catch (NumberFormatException ex2)
        {
            if (_defaultValue != null)
            {
                return (Long) _defaultValue;
            }
            throw ex2;
        }
        catch (NullPointerException ex3)
        {
            if (_defaultValue != null)
            {
                return (Long) _defaultValue;
            }
            throw ex3;
        }
    }

    /**
     * Load date properties
     * 
     * @throws ParseException
     */
    private void loadDateProperties() throws ParseException
    {
        String defaultValue = _properties.get(DEFAULT_VALUE_PROPERTY_NAME);
        _dateFormat = _properties.get(DATE_FORMAT_PROPERTY_NAME);
        if (StringUtils.isBlank(_dateFormat))
        {
            _dateFormat = "yyyy-MM-dd HH:mm:ss";
        }

        SimpleDateFormat dateFormatter = new SimpleDateFormat(_dateFormat);
        String t = _properties.get(DATE_TIMEZONE_PROPERTY_NAME);
        if (!StringUtils.isBlank(t))
        {
            _dateTimezone = TimeZone.getTimeZone(t);
            dateFormatter.setTimeZone(_dateTimezone);
        }

        if (!StringUtils.isBlank(defaultValue))
        {
            _defaultValue = dateFormatter.parse(defaultValue);
        }
    }

    /**
     * <p>
     * Parse a date.
     * </p>
     * <p>
     * If parsing fails and a default value is set, the default value is returned.
     * </p>
     * 
     * @param value
     *            string value to parse into an integer
     * @return integer value.
     * @throws ParseException
     */
    private Date parseDate(String value) throws ParseException
    {
        try
        {
            // SimpleDateFormat is not thread safe so we have instance it everytime
            SimpleDateFormat dateFormatter = new SimpleDateFormat(_dateFormat);
            if (_dateTimezone != null)
            {
                dateFormatter.setTimeZone(_dateTimezone);
            }

            if (!StringUtils.isBlank(value))
            {
                value = value.trim();
            }
            return dateFormatter.parse(value);
        }
        catch (ParseException ex)
        {
            if (_defaultValue != null)
            {
                return (Date) _defaultValue;
            }
            throw ex;
        }
    }

    /**
     * Load boolean properties
     * 
     * @throws ParseException
     */
    private void loadBooleanProperties() throws ParseException
    {
        String s = _properties.get(TRUE_PATTERN_PROPERTY_NAME);
        if (StringUtils.isBlank(s))
        {
            s = "[Tt][Rr][Uu][Ee]";
        }
        _truePattern = Pattern.compile(s);
    }

    /**
     * <p>
     * Parse a boolean.
     * </p>
     * <p>
     * If parsing fails and a default value is set, the default value is returned.
     * </p>
     * 
     * @param value
     *            string value to parse into an integer
     * @return integer value.
     * @throws ParseException
     */
    private Boolean parseBoolean(String value) throws ParseException
    {
        if (!StringUtils.isBlank(value))
        {
            value = value.trim();
        }
        return _truePattern.matcher(value).matches();
    }

    /**
     * Load double properties
     * 
     * @throws ParseException
     */
    private void loadDoubleProperties() throws ParseException
    {
        String d = _properties.get(DEFAULT_VALUE_PROPERTY_NAME);
        String s = _properties.get(NUMBER_FORMAT_PROPERTY_NAME);
        if (!StringUtils.isBlank(s))
        {
            _numberFormatter = new DecimalFormat(s);
        }

        if (!StringUtils.isBlank(d))
        {
            if (_numberFormatter == null)
            {
                _defaultValue = Double.parseDouble(d);
            }
            else
            {
                _defaultValue = _numberFormatter.parse(d).doubleValue();
            }
        }
    }

    /**
     * <p>
     * Parse a double.
     * </p>
     * <p>
     * If a number patter is set, the the number pattern is used for parsing. Otherwise, standard integer parsing is
     * performed where only digits are allowed.
     * </p>
     * <p>
     * If parsing fails and a default value is set, the default value is returned.
     * </p>
     * 
     * @param value
     *            string value to parse into an integer
     * @return integer value.
     * @throws ParseException
     */
    private Double parseDouble(String value) throws ParseException
    {
        try
        {
            if (!StringUtils.isBlank(value))
            {
                value = value.trim();
            }
            if (_numberFormatter == null)
            {
                return Double.parseDouble(value);
            }
            else
            {
                return _numberFormatter.parse(value).doubleValue();
            }
        }
        catch (ParseException ex)
        {
            if (_defaultValue != null)
            {
                return (Double) _defaultValue;
            }
            throw ex;
        }
        catch (NumberFormatException ex2)
        {
            if (_defaultValue != null)
            {
                return (Double) _defaultValue;
            }
            throw ex2;
        }
        catch (NullPointerException ex3)
        {
            if (_defaultValue != null)
            {
                return (Double) _defaultValue;
            }
            throw ex3;
        }
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
