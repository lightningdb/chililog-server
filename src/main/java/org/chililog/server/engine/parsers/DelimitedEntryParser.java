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

package org.chililog.server.engine.parsers;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Hashtable;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.chililog.server.common.ChiliLogException;
import org.chililog.server.common.Log4JLogger;
import org.chililog.server.common.StringsProperties;
import org.chililog.server.data.RepositoryEntryBO;
import org.chililog.server.data.RepositoryFieldConfigBO;
import org.chililog.server.data.RepositoryConfigBO;
import org.chililog.server.data.RepositoryParserConfigBO;
import org.chililog.server.data.RepositoryEntryBO.Severity;
import org.chililog.server.engine.Strings;

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
public class DelimitedEntryParser extends EntryParser {
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
     * @param repoInfo
     *            Repository meta data
     * @param repoParserInfo
     *            Parser information that we need
     * @throws ChiliLogException
     */
    public DelimitedEntryParser(RepositoryConfigBO repoInfo, RepositoryParserConfigBO repoParserInfo)
            throws ChiliLogException {
        super(repoInfo, repoParserInfo);

        try {
            Hashtable<String, String> properties = repoParserInfo.getProperties();
            _delimiter = properties.get(DELIMITER_PROPERTY_NAME);
            if (StringUtils.isBlank(_delimiter)) {
                throw new ChiliLogException(Strings.PARSER_DELIMITER_NOT_SET_ERROR, repoParserInfo.getName(),
                        repoInfo.getName());
            }

            // Parse our field value so that we don't have to keep on doing it
            for (RepositoryFieldConfigBO f : repoParserInfo.getFields()) {
                String s = f.getProperties().get(POSITION_FIELD_PROPERTY_NAME);
                Integer i = Integer.parseInt(s) - 1;
                _fields.add(new DelimitedFieldInfo(i, f));
            }
        }
        catch (Exception ex) {
            if (ex instanceof ChiliLogException) {
                throw (ChiliLogException) ex;
            }
            else {
                throw new ChiliLogException(ex, Strings.PARSER_INITIALIZATION_ERROR, repoParserInfo.getName(),
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
    public RepositoryEntryBO parse(String timestamp, String source, String host, String severity, String message) {
        try {
            this.setLastParseError(null);
            checkParseArguments(timestamp, source, host, severity, message);

            BasicDBObject parsedFields = new BasicDBObject();

            String[] ss = StringUtils.split(message, _delimiter);
            for (DelimitedFieldInfo delimitedField : _fields) {
                String fieldName = delimitedField.getRepoFieldInfo().getDbObjectName();
                String fieldStringValue = null;
                Object fieldValue = null;
                try {
                    fieldStringValue = ss[delimitedField.getArrayIndex()];
                    fieldValue = delimitedField.getParser().parse(fieldStringValue);
                    parsedFields.put(fieldName, fieldValue);
                }
                catch (Exception ex) {
                    switch (this.getRepoParserInfo().getParseFieldErrorHandling()) {
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
        catch (Exception ex) {
            this.setLastParseError(ex);
            _logger.error(ex, "Error parsing text entry: " + message);
            return null;
        }
    }

    /**
     * Encapsulates a delimited field
     */
    private static class DelimitedFieldInfo {
        private int _arrayIndex;
        private RepositoryFieldConfigBO _repoFieldInfo;
        private FieldParser _parser;

        public DelimitedFieldInfo(int arrayIndex, RepositoryFieldConfigBO repoFieldInfo) throws ParseException {
            _arrayIndex = arrayIndex;
            _repoFieldInfo = repoFieldInfo;
            _parser = FieldParserFactory.getParser(repoFieldInfo);
        }

        /**
         * Returns the index position of this field relative to other field. E.g. 0 is the 1st field.
         */
        public int getArrayIndex() {
            return _arrayIndex;
        }

        /**
         * Returns the field meta data
         */
        public RepositoryFieldConfigBO getRepoFieldInfo() {
            return _repoFieldInfo;
        }

        /**
         * Returns the field value parser
         */
        public FieldParser getParser() {
            return _parser;
        }

    }

}
