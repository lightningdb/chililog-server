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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.regex.Pattern;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;

import com.chililog.server.common.ChiliLogException;
import com.chililog.server.common.Log4JLogger;
import com.chililog.server.data.RepositoryEntryBO;
import com.chililog.server.data.RepositoryEntryBO.Severity;
import com.chililog.server.data.MongoJsonParser;
import com.chililog.server.data.RepositoryInfoBO;
import com.chililog.server.data.RepositoryParserInfoBO;
import com.chililog.server.engine.Strings;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * <p>
 * Parser to extract field values from JSON log entries. For example,
 * </p>
 * <code>
 * { "field1": 1, "field2": "abc", "field3": 123 }
 * </code>
 * <p>
 * Field definitions are not required because it is defined in the JSON format.
 * </p>
 * 
 * @author vibul
 * 
 */
public class JsonEntryParser extends EntryParser
{
    private static Log4JLogger _logger = Log4JLogger.getLogger(JsonEntryParser.class);

    private Pattern _datePattern = null;
    private String _dateFormat = null;
    private Pattern _longNumberPattern = null;

    /**
     * <p>
     * Regular expression pattern to use in order to identify a date within a string. For example, if
     * "^([0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}Z)$", then "2000-01-01T01:01:01Z" will be treated as a
     * Date.
     * </p>
     * <p>
     * If not set, date parsing will not be performed.
     * </p>
     */
    public static final String DATE_PATTERN_PROPERTY_NAME = "date_pattern";

    /**
     * <p>
     * Date format to use with {@link SimpleDateFormat} when parsing for a date format. For example,
     * "yyyy-MM-dd'T'HH:mm:ssZ". Parsing is only performed on a {@link String} if it passes the string length and
     * pattern checks.
     * </p>
     * <p>
     * If not set, date parsing will not be performed.
     * </p>
     */
    public static final String DATE_FORMAT_PROPERTY_NAME = "date_format";

    /**
     * <p>
     * Regular expression pattern to use in order to identify a long within a string. For example, if "^([0-9]+L)$",
     * then "88888L" will be treated as a Long.
     * </p>
     * <p>
     * If not set, long number parsing on a string will not be performed.
     * </p>
     */
    public static final String LONG_NUMBER_PATTERN_PROPERTY_NAME = "long_suffix";

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
    public JsonEntryParser(RepositoryInfoBO repoInfo, RepositoryParserInfoBO repoParserInfo) throws ChiliLogException
    {
        super(repoInfo, repoParserInfo);

        try
        {
            Hashtable<String, String> properties = repoParserInfo.getProperties();
            String s = properties.get(DATE_PATTERN_PROPERTY_NAME);
            if (!StringUtils.isBlank(s))
            {
                _datePattern = Pattern.compile(s);
            }

            _dateFormat = properties.get(DATE_FORMAT_PROPERTY_NAME);

            s = properties.get(LONG_NUMBER_PATTERN_PROPERTY_NAME);
            if (!StringUtils.isBlank(s))
            {
                _longNumberPattern = Pattern.compile(s);
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
                throw new ChiliLogException(Strings.PARSER_INITIALIZATION_ERROR, repoParserInfo.getName(),
                        repoInfo.getName(), ex.getMessage());
            }
        }

        return;
    }

    /**
     * Parse a string for fields. All exceptions are caught and logged. If <code>null</code> is returned, this indicates
     * that the entry should be skipped.
     * 
     * @param timestamp
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

            MongoJsonParser parser = new MongoJsonParser(message, _datePattern, _dateFormat, _longNumberPattern);
            DBObject fieldsDBObject = new BasicDBObject();
            try
            {
                fieldsDBObject = (DBObject) parser.parse();
            }
            catch (Exception ex)
            {
                switch (this.getRepoParserInfo().getParseFieldErrorHandling())
                {
                    case SkipField:
                    case SkipEntry:
                        throw new ChiliLogException(ex, Strings.PARSER_JSON_ERROR_SKIP_ENTRY, this.getRepoName(),
                                ex.getMessage(), message);
                    case SkipFieldIgnoreError:
                        break;// Do nothing
                    default:
                        throw new NotImplementedException("ParseFieldErrorHandling type "
                                + this.getRepoParserInfo().getParseFieldErrorHandling().toString());

                }
            }

            Severity sev = Severity.parse(severity);
            ArrayList<String> keywords = parseKeywords(source, host, sev, message);

            return new RepositoryEntryBO(parseTimestamp(timestamp), source, host, sev, keywords, message,
                    fieldsDBObject);
        }
        catch (Exception ex)
        {
            this.setLastParseError(ex);
            _logger.error(ex, "Error parsing JSON entry: " + message);
            return null;
        }
    }

}
