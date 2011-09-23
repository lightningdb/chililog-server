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
import java.util.Hashtable;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.chililog.server.data.RepositoryFieldConfigBO;

/**
 * Parses a boolean field.
 * 
 * @author vibul
 * 
 */
public class BooleanFieldParser extends FieldParser {
    private Boolean _defaultValue = null;
    private Pattern _truePattern = null;

    /**
     * Constructor
     * 
     * @param repoFieldInfo
     *            Field meta data
     * @throws ParseException
     */
    public BooleanFieldParser(RepositoryFieldConfigBO repoFieldInfo) throws ParseException {
        super(repoFieldInfo);

        Hashtable<String, String> properties = repoFieldInfo.getProperties();

        String s = properties.get(RepositoryFieldConfigBO.TRUE_PATTERN_PROPERTY_NAME);
        if (StringUtils.isBlank(s)) {
            s = "[Tt][Rr][Uu][Ee]";
        }
        _truePattern = Pattern.compile(s);

        String defaultValue = properties.get(RepositoryFieldConfigBO.DEFAULT_VALUE_PROPERTY_NAME);
        if (!StringUtils.isBlank(defaultValue)) {
            _defaultValue = parseBoolean(defaultValue);
        }
    }

    /**
     * Parses a date field.
     */
    @Override
    public Object parse(String value) throws ParseException {
        return parseBoolean(value);
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
    private Boolean parseBoolean(String value) throws ParseException {
        value = preparse(value);

        if (value == null) {
            return _defaultValue == null ? false : _defaultValue;
        }

        return _truePattern.matcher(value).matches();
    }

}
