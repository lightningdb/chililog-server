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

package com.chililog.server.engine.parsers;

import java.text.ParseException;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import com.chililog.server.data.RepositoryFieldInfoBO;

/**
 * Parses the string representation of a field's value and returns its as a strongly typed object
 * 
 * @author vibul
 * 
 */
public abstract class FieldParser
{
    private RepositoryFieldInfoBO _repoFieldInfo;
    private Pattern _preparsePattern = null;
    private int _preparsePatternGroup = 1;

    /**
     * Basic constructor
     * 
     * @param repoFieldInfo
     *            Field meta data
     */
    public FieldParser(RepositoryFieldInfoBO repoFieldInfo)
    {
        _repoFieldInfo = repoFieldInfo;

        Hashtable<String, String> properties = _repoFieldInfo.getProperties();
        String s = properties.get(RepositoryFieldInfoBO.PREPARSE_PATTERN_PROPERTY_NAME);
        if (!StringUtils.isBlank(s))
        {
            _preparsePattern = Pattern.compile(s);
        }

        String g = properties.get(RepositoryFieldInfoBO.PREPARSE_PATTERN_GROUP_PROPERTY_NAME);
        if (!StringUtils.isBlank(g))
        {
            _preparsePatternGroup = Integer.parseInt(g);
        }
    }

    /**
     * Returns the field meta data for this field parser
     */
    public RepositoryFieldInfoBO getRepoFieldInfo()
    {
        return _repoFieldInfo;
    }

    /**
     * Extracts the relevant part of a string for parsing
     * 
     * @param value
     *            value to pre-parse
     * @return String for parsing
     */
    protected String preparse(String value)
    {
        if (_preparsePattern != null && !StringUtils.isBlank(value))
        {
            Matcher m = _preparsePattern.matcher(value);
            if (m.find())
            {
                return m.group(_preparsePatternGroup);
            }
        }
        return value;
    }

    /**
     * Parse a field and returns a strongly-typed object
     * 
     * @param value
     *            string representation of a field to parser
     * @return strongly typed value of the field
     * @throws ParseException
     */
    public abstract Object parse(String value) throws ParseException;
}
