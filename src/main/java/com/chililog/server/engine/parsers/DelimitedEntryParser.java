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

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;

import com.chililog.server.common.ChiliLogException;
import com.chililog.server.common.Log4JLogger;
import com.chililog.server.common.StringsProperties;
import com.chililog.server.data.RepositoryEntryBO;
import com.chililog.server.data.RepositoryEntryBO.Severity;
import com.chililog.server.data.RepositoryFieldInfoBO;
import com.chililog.server.data.RepositoryParserInfoBO;
import com.chililog.server.engine.Strings;
import com.mongodb.BasicDBObject;

/**
 * <p>
 * Parser to extract field values from delimited log entries. For example,
 * </p>
 * <code>
 * field1|field2|field3
 * </code>
 * <p>
 * The fields are delimited by the pipe character (|).
 * </p>
 * 
 * @author vibul
 * 
 */
public class DelimitedEntryParser extends EntryParser
{
    private static Log4JLogger _logger = Log4JLogger.getLogger(DelimitedEntryParser.class);
    private String _delimiter;
    private ArrayList<DelimitedFieldInfo> _fields = new ArrayList<DelimitedFieldInfo>();

    /**
     * Delimiter repository property denotes the field delimiter character
     */
    public static final String DELIMITER_PROPERTY_NAME = "delimiter";

    /**
     * Position field property denotes the position of this field. Position 1 is the 1st field.
     */
    public static final String POSITION_FIELD_PROPERTY_NAME = "position";

    /**
     * <p>
     * Basic constructor
     * </p>
     * 
     * @param repoName
     *            Name of repository (for reporting errors)
     * @param repoParserInfo
     *            Parser information that we need
     * @throws ChiliLogException
     */
    public DelimitedEntryParser(String repoName, RepositoryParserInfoBO repoParserInfo) throws ChiliLogException
    {
        super(repoName, repoParserInfo);

        try
        {
            Hashtable<String, String> properties = repoParserInfo.getProperties();
            _delimiter = properties.get(DELIMITER_PROPERTY_NAME);
            if (StringUtils.isBlank(_delimiter))
            {
                throw new ChiliLogException(Strings.PARSER_DELIMITER_NOT_SET_ERROR, repoParserInfo.getName(), repoName);
            }

            // Parse our field value so that we don't have to keep on doing it
            for (RepositoryFieldInfoBO f : repoParserInfo.getFields())
            {
                String s = f.getProperties().get(POSITION_FIELD_PROPERTY_NAME);
                Integer i = Integer.parseInt(s) - 1;
                _fields.add(new DelimitedFieldInfo(i, f));
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
                throw new ChiliLogException(ex, Strings.PARSER_INITIALIZATION_ERROR, repoParserInfo.getName(), repoName,
                        ex.getMessage());
            }
        }

        return;
    }

    /**
     * Parse a string for fields. All exceptions are caught and logged. If <code>null</code> is returned, this indicates
     * that the entry should be skipped.
     * 
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
    public RepositoryEntryBO parse(String source, String host, String serverity, String message)
    {
        try
        {
            this.setLastParseError(null);
            checkParseArguments(source, host, serverity, message);
            Severity severity = Severity.parse(serverity);

            BasicDBObject parsedFields = new BasicDBObject();

            String[] ss = StringUtils.split(message, _delimiter);
            for (DelimitedFieldInfo delimitedField : _fields)
            {
                String fieldName = delimitedField.getRepoFieldInfo().getName();
                String fieldStringValue = null;
                Object fieldValue = null;
                try
                {
                    fieldStringValue = ss[delimitedField.getArrayIndex()];
                    fieldValue = delimitedField.getParser().parse(fieldStringValue);
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

            return new RepositoryEntryBO(source, host, severity, message, parsedFields);
        }
        catch (Exception ex)
        {
            this.setLastParseError(ex);
            _logger.error(ex, "Error parsing text entry: " + message);
            return null;
        }
    }

    /**
     * Encapsulates a delimited field
     */
    private static class DelimitedFieldInfo
    {
        private int _arrayIndex;
        private RepositoryFieldInfoBO _repoFieldInfo;
        private FieldParser _parser;

        public DelimitedFieldInfo(int arrayIndex, RepositoryFieldInfoBO repoFieldInfo) throws ParseException
        {
            _arrayIndex = arrayIndex;
            _repoFieldInfo = repoFieldInfo;
            _parser = FieldParserFactory.getParser(repoFieldInfo);
        }

        /**
         * Returns the index position of this field relative to other field. E.g. 0 is the 1st field.
         */
        public int getArrayIndex()
        {
            return _arrayIndex;
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
