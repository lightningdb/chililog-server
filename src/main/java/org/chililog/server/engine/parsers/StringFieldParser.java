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

import org.apache.commons.lang.StringUtils;
import org.chililog.server.data.RepositoryFieldConfigBO;

/**
 * Parses a string field.
 * 
 * @author vibul
 * 
 */
public class StringFieldParser extends FieldParser {

    private String _defaultValue = null;

    public StringFieldParser(RepositoryFieldConfigBO repoFieldInfo) {
        super(repoFieldInfo);

        Hashtable<String, String> properties = repoFieldInfo.getProperties();
        _defaultValue = properties.get(RepositoryFieldConfigBO.DEFAULT_VALUE_PROPERTY_NAME);
    }

    /**
     * Parses a string. If string is blank the default value is returned (if it was specified).
     */
    @Override
    public Object parse(String value) throws ParseException {
        return parseString(value);
    }

    /**
     * Parse a string
     * 
     * @param value
     *            value to parse
     * @return parsed string
     */
    private String parseString(String value) {
        value = preparse(value);
        if (StringUtils.isBlank(value) && _defaultValue != null) {
            return (String) _defaultValue;
        }
        return value;
    }

}
