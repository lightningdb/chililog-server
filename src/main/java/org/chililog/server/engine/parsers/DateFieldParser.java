//
// Copyright 2010 Cinch Logic Pty Ltd
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.TimeZone;

import org.apache.commons.lang.StringUtils;
import org.chililog.server.data.RepositoryFieldConfigBO;


/**
 * Parses a date field.
 * 
 * @author vibul
 * 
 */
public class DateFieldParser extends FieldParser
{
    private Date _defaultValue = null;
    private String _dateFormat = null;
    private TimeZone _dateTimezone = null;

    /**
     * Constructor
     * 
     * @param repoFieldInfo
     *            Field meta data
     * @throws ParseException
     */
    public DateFieldParser(RepositoryFieldConfigBO repoFieldInfo) throws ParseException
    {
        super(repoFieldInfo);

        Hashtable<String, String> properties = repoFieldInfo.getProperties();

        String defaultValue = properties.get(RepositoryFieldConfigBO.DEFAULT_VALUE_PROPERTY_NAME);
        _dateFormat = properties.get(RepositoryFieldConfigBO.DATE_FORMAT_PROPERTY_NAME);
        if (StringUtils.isBlank(_dateFormat))
        {
            _dateFormat = "yyyy-MM-dd'T'HH:mm:ssZ";
        }

        SimpleDateFormat dateFormatter = new SimpleDateFormat(_dateFormat);
        String t = properties.get(RepositoryFieldConfigBO.DATE_TIMEZONE_PROPERTY_NAME);
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
     * Parses a date field.
     */
    @Override
    public Object parse(String value) throws ParseException
    {
        return parseDate(value);
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
     *            string value to parse into a date
     * @return integer value.
     * @throws ParseException
     */
    private Date parseDate(String value) throws ParseException
    {
        try
        {
            value = preparse(value);
            
            // Simple date format does not recognise Z time zone so make it GMT
            if (value.endsWith("Z"))
            {
                value = value.substring(0, value.length() - 1) + "GMT";
            }
            
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

}
