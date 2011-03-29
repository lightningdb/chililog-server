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

package com.chililog.server.engine.parsers;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;

import com.chililog.server.common.ChiliLogException;
import com.chililog.server.common.Log4JLogger;
import com.chililog.server.common.StringsProperties;
import com.chililog.server.data.RepositoryEntryBO;
import com.chililog.server.data.RepositoryEntryBO.Severity;
import com.chililog.server.data.RepositoryFieldInfoBO;
import com.chililog.server.data.RepositoryInfoBO;
import com.chililog.server.data.RepositoryParserInfoBO;
import com.chililog.server.engine.Strings;
import com.mongodb.BasicDBObject;

/**
 * <p>
 * Parser to extract field values from free format log entries using regular expression. For example,
 * </p>
 * 
 * @author vibul
 * 
 */
public class RegexEntryParser extends EntryParser
{
    private static Log4JLogger _logger = Log4JLogger.getLogger(RegexEntryParser.class);
    private Pattern _pattern;
    private ArrayList<RegexFieldInfo> _fields = new ArrayList<RegexFieldInfo>();

    /**
     * Pattern to apply to each log entry. If not specified, then a pattern for each field must be specified
     */
    public static final String PATTERN_PROPERTY_NAME = "pattern";

    /**
     * Optional override pattern to search for a specific field
     */
    public static final String PATTERN_FIELD_PROPERTY_NAME = "pattern";

    /**
     * Denotes the regular expression group number in which to find the contents of the field. For a pattern,
     * <code>([0-9]{4}) ([0-9]{2})</code> and text <code>1111 22</code>, group 1 is <code>1111</code> and group 2 is
     * <code>22</code>.
     */
    public static final String GROUP_FIELD_PROPERTY_NAME = "group";

    /**
     * <p>
     * Basic constructor
     * </p>
     * 
     * @param repoInfo
     *            Repository meta data
     * @param repoParserInfo
     *            Parser information that we need
     * @throws ChiliLogException
     */
    public RegexEntryParser(RepositoryInfoBO repoInfo, RepositoryParserInfoBO repoParserInfo) throws ChiliLogException
    {
        super(repoInfo, repoParserInfo);

        try
        {
            Hashtable<String, String> properties = repoParserInfo.getProperties();
            String patternString = properties.get(PATTERN_PROPERTY_NAME);
            if (!StringUtils.isBlank(patternString))
            {
                _pattern = Pattern.compile(patternString);
            }

            // Parse our field value so that we don't have to keep on doing it
            for (RepositoryFieldInfoBO f : repoParserInfo.getFields())
            {
                String fieldPatternString = f.getProperties().get(PATTERN_FIELD_PROPERTY_NAME);
                String groupString = f.getProperties().get(GROUP_FIELD_PROPERTY_NAME);
                Integer group = Integer.parseInt(groupString);
                _fields.add(new RegexFieldInfo(fieldPatternString, group, f));
            }
        }
        catch (Exception ex)
        {
            if (ex instanceof ChiliLogException)
            {
                throw (ChiliLogException) ex;
            }
            else
            {
                throw new ChiliLogException(Strings.PARSER_INITIALIZATION_ERROR, repoParserInfo.getName(), repoInfo.getName(),
                        ex.getMessage());
            }
        }

        return;
    }

    /**
     * Parse a string for fields. All exceptions are caught and logged. If <code>null</code> is returned, this indicates
     * that the entry should be skipped.
     * 
     * @param timetstamp
     *            Time when this log entry was created at the source on the host.
     * @param source
     *            Name of the input device or application that created this text entry
     * @param host
     *            IP address of the input device or application that created this text entry
     * @param severity
     *            Classifies the importance of the entry. Can be the severity code (0-7) or text.
     * @param message
     *            The text for this entry to parse
     * @return <code>RepositoryEntryBO</code> ready for saving to mongoDB. If the entry cannot be parsed, then null is
     *         returned
     */
    @Override
    public RepositoryEntryBO parse(String timestamp, String source, String host, String severity, String message)
    {
        try
        {
            this.setLastParseError(null);
            checkParseArguments(timestamp, source, host, severity, message);

            BasicDBObject parsedFields = new BasicDBObject();
            Matcher entryMatcher = null;
            boolean entryMatches = false;
            if (_pattern != null)
            {
                entryMatcher = _pattern.matcher(message);
                entryMatches = entryMatcher.matches();
            }

            for (RegexFieldInfo regexField : _fields)
            {
                String fieldName = regexField.getRepoFieldInfo().getName();
                String fieldStringValue = null;
                Object fieldValue = null;
                try
                {
                    Matcher fieldMatcher = null;
                    if (regexField.getPattern() != null)
                    {
                        fieldMatcher = regexField.getPattern().matcher(message);
                        if (fieldMatcher.matches())
                        {
                            fieldStringValue = fieldMatcher.group(regexField.getGroup());
                        }
                    }
                    else if (entryMatches)
                    {
                        fieldStringValue = entryMatcher.group(regexField.getGroup());
                    }

                    fieldValue = regexField.getParser().parse(fieldStringValue);
                    parsedFields.put(fieldName, fieldValue);
                }
                catch (Exception ex)
                {
                    switch (this.getRepoParserInfo().getParseFieldErrorHandling())
                    {
                        case SkipField:
                            String msg = StringsProperties.getInstance().getString(
                                    Strings.PARSER_FIELD_ERROR_SKIP_FIELD);
                            _logger.error(ex, msg, fieldStringValue, fieldName, this.getRepoName(), ex.getMessage(),
                                    message);
                            break;
                        case SkipEntry:
                            throw new ChiliLogException(ex, Strings.PARSER_FIELD_ERROR_SKIP_ENTRY, fieldStringValue,
                                    fieldName, this.getRepoName(), ex.getMessage(), message);
                        case SkipFieldIgnoreError:
                            break;// Do nothing
                        default:
                            throw new NotImplementedException("ParseFieldErrorHandling type "
                                    + this.getRepoParserInfo().getParseFieldErrorHandling().toString());

                    }
                }
            }

            Severity sev = Severity.parse(severity);
            ArrayList<String> keywords = parseKeywords(source, host, sev, message);

            return new RepositoryEntryBO(parseTimestamp(timestamp), source, host, sev, keywords, message, parsedFields);
        }
        catch (Exception ex)
        {
            this.setLastParseError(ex);
            _logger.error(ex, "Error parsing text entry: " + message);
            return null;
        }
    }

    /**
     * Encapsulates a regular expression field
     */
    private static class RegexFieldInfo
    {
        public Pattern _pattern;
        public int _group;
        private RepositoryFieldInfoBO _repoFieldInfo;
        private FieldParser _parser;

        /**
         * Basic constructor
         * 
         * @param pattern
         *            optional field specific pattern
         * @param group
         *            group number within the matching pattern containing the string value of this field
         * @param repoFieldInfo
         *            meta data
         * @throws ParseException
         */
        public RegexFieldInfo(String pattern, int group, RepositoryFieldInfoBO repoFieldInfo) throws ParseException
        {
            if (!StringUtils.isBlank(pattern))
            {
                _pattern = Pattern.compile(pattern);
            }
            _group = group;
            _repoFieldInfo = repoFieldInfo;
            _parser = FieldParserFactory.getParser(repoFieldInfo);
        }

        /**
         * Returns the optional field specific pattern. If null, then use the repository entry pattern
         */
        public Pattern getPattern()
        {
            return _pattern;
        }

        /**
         * Returns the group number within the matching pattern containing the string value of this field
         */
        public int getGroup()
        {
            return _group;
        }

        /**
         * Returns the field meta data
         */
        public RepositoryFieldInfoBO getRepoFieldInfo()
        {
            return _repoFieldInfo;
        }

        /**
         * Returns the field value parser
         */
        public FieldParser getParser()
        {
            return _parser;
        }

    }

}
