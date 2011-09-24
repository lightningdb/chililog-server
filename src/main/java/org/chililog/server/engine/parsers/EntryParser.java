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

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Pattern;

import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.StringUtils;
import org.chililog.server.common.ChiliLogException;
import org.chililog.server.common.TextTokenizer;
import org.chililog.server.data.MongoJsonParser;
import org.chililog.server.data.RepositoryEntryBO;
import org.chililog.server.data.RepositoryConfigBO;
import org.chililog.server.data.RepositoryParserConfigBO;
import org.chililog.server.data.RepositoryEntryBO.Severity;
import org.chililog.server.data.RepositoryParserConfigBO.AppliesTo;
import org.chililog.server.engine.RepositoryEntryMqMessage;

import com.mongodb.BasicDBObject;

/**
 * <p>
 * Parses incoming entries to extract fields and keywords
 * </p>
 * <p>
 * This code is NOT designed for multi-threaded use. It should only be used in 1 thread.
 * </p>
 */
public abstract class EntryParser {

    private String _repoName;
    private long _maxKeywords = 0;
    private RepositoryParserConfigBO _repoParserInfo;
    private Exception _lastParseError = null;

    private Pattern _sourcePattern = null;
    private String[] _sourceCSV = null;
    private Pattern _hostPattern = null;
    private String[] _hostCSV = null;

    private SimpleDateFormat _dateFormat;
    private TextTokenizer _tokenizer;

    private MongoJsonParser _inputFieldsParser = null;

    /**
     * <p>
     * Basic constructor
     * </p>
     * 
     * @param repoInfo
     *            Repository (for reporting errors)
     * @param repoParserInfo
     *            Parser information that we need
     * @throws ChiliLogException
     */
    public EntryParser(RepositoryConfigBO repoInfo, RepositoryParserConfigBO repoParserInfo) {
        if (repoInfo == null) {
            throw new NullArgumentException("repoInfo is null");
        }
        if (repoParserInfo == null) {
            throw new NullArgumentException("repoParserInfo is null");
        }

        _repoName = repoInfo.getName();
        _repoParserInfo = repoParserInfo;
        _maxKeywords = repoInfo.getStorageMaxKeywords();
        if (repoParserInfo.getMaxKeywords() != RepositoryParserConfigBO.MAX_KEYWORDS_INHERITED) {
            _maxKeywords = repoParserInfo.getMaxKeywords();
        }

        // Get our regular expression ready for matching source and host
        if (_repoParserInfo.getAppliesTo() == AppliesTo.AllowFilteredCSV) {
            if (!StringUtils.isBlank(_repoParserInfo.getAppliesToSourceFilter())) {
                _sourceCSV = _repoParserInfo.getAppliesToSourceFilter().split(",");
                for (int i = 0; i < _sourceCSV.length; i++) {
                    _sourceCSV[i] = _sourceCSV[i].trim();
                }
            }
            if (!StringUtils.isBlank(_repoParserInfo.getAppliesToHostFilter())) {
                _hostCSV = _repoParserInfo.getAppliesToHostFilter().split(",");
                for (int i = 0; i < _hostCSV.length; i++) {
                    _hostCSV[i] = _hostCSV[i].trim();
                }
            }
        } else if (_repoParserInfo.getAppliesTo() == AppliesTo.AllowFilteredRegularExpression) {
            if (!StringUtils.isBlank(_repoParserInfo.getAppliesToSourceFilter())) {
                _sourcePattern = Pattern.compile(_repoParserInfo.getAppliesToSourceFilter());
            }
            if (!StringUtils.isBlank(_repoParserInfo.getAppliesToHostFilter())) {
                _hostPattern = Pattern.compile(_repoParserInfo.getAppliesToHostFilter());
            }
        }

        // Dates for parsing timestamp
        _dateFormat = RepositoryEntryMqMessage.getDateFormatter();

        // Tokenizer for keyword extraction
        _tokenizer = TextTokenizer.getInstance();

        return;
    }

    /**
     * Returns the name of the repository to which this parser it attached
     */
    public String getRepoName() {
        return _repoName;
    }

    /**
     * Returns the parser meta data
     */
    public RepositoryParserConfigBO getRepoParserInfo() {
        return _repoParserInfo;
    }

    /**
     * Returns the last error that happened during parsing
     */
    public Exception getLastParseError() {
        return _lastParseError;
    }

    /**
     * Sets the last error
     * 
     * @param lastParseError
     *            Exception thrown during parsing
     */
    protected void setLastParseError(Exception lastParseError) {
        _lastParseError = lastParseError;
    }

    /**
     * Checks if this parser is applicable to the specified source and host
     * 
     * @param source
     *            Application or service that created the log entry
     * @param host
     *            Computer name or IP address
     * @return True if this parser is to be used, False if not
     */
    public boolean isApplicable(String source, String host) {
        if (_repoParserInfo.getAppliesTo() == AppliesTo.All) {
            return true;
        } else if (_repoParserInfo.getAppliesTo() == AppliesTo.AllowFilteredCSV) {
            if (!StringUtils.isBlank(source) && _sourceCSV != null) {
                for (String s : _sourceCSV) {
                    if (s.equalsIgnoreCase(source)) {
                        return true;
                    }
                }
            }
            if (!StringUtils.isBlank(host) && _hostCSV != null) {
                for (String s : _hostCSV) {
                    if (s.equalsIgnoreCase(host)) {
                        return true;
                    }
                }
            }
        } else if (_repoParserInfo.getAppliesTo() == AppliesTo.AllowFilteredRegularExpression) {
            if (!StringUtils.isBlank(source) && _sourcePattern != null) {
                return _sourcePattern.matcher(source).matches();
            }
            if (!StringUtils.isBlank(host) && _hostPattern != null) {
                return _hostPattern.matcher(host).matches();
            }
        }
        return false;
    }

    /**
     * Checks the validity of our arguments before parsing
     * 
     * @param source
     * @param host
     * @param serverity
     * @param message
     */
    protected void checkParseArguments(String timestamp, String source, String host, String serverity, String message) {
        if (StringUtils.isBlank(timestamp)) {
            throw new IllegalArgumentException("Entry timestamp is blank");
        }
        if (StringUtils.isBlank(source)) {
            throw new IllegalArgumentException("Entry source is blank");
        }
        if (StringUtils.isBlank(host)) {
            throw new IllegalArgumentException("Entry host is blank");
        }
        if (StringUtils.isBlank(serverity)) {
            throw new IllegalArgumentException("Entry serverity is blank");
        }
        if (StringUtils.isBlank(message)) {
            throw new IllegalArgumentException("Entry message is blank");
        }
    }

    /**
     * Parses the timestamp. Assumes the format is '2011-12-31T23:01:01.123Z'.
     * 
     * @param timestamp
     * @return Date
     * @throws ParseException
     */
    protected Date parseTimestamp(String timestamp) throws ParseException {
        return _dateFormat.parse(timestamp);
    }

    /**
     * Parses our message to look for keywords
     * 
     * @param message
     *            Message to parse
     * @return List of keywords
     * @throws IOException
     */
    protected ArrayList<String> parseKeywords(String source, String host, Severity severity, String message)
            throws IOException {
        StringBuilder sb = new StringBuilder();
        ArrayList<String> l = _tokenizer.tokenize(message, _maxKeywords);

        // Add source to keywords
        sb.append("s=").append(source);
        l.add(sb.toString());

        // Add host to keywords
        sb.setLength(0);
        sb.append("h=").append(host);
        l.add(sb.toString());

        // Add severity to keywords
        sb.setLength(0);
        sb.append("v=").append(severity.toCode());
        l.add(sb.toString());

        return l;
    }

    /**
     * Read the pre-parsed input fields. Convert the JSON format into DBObject that can be stored by mongo.
     * 
     * @param fields
     *            Fields pre-parsed by publishers in JSON format.
     * @return Fields as mongo DBObject. If fields is null or empty, then an empty DBObject is returned.
     */
    protected BasicDBObject readPreparsedFields(String fields) {
        if (StringUtils.isBlank(fields)) {
            return new BasicDBObject();
        }
        
        if (_inputFieldsParser == null) {
            Pattern datePattern = RepositoryEntryMqMessage.getTimestampPattern();
            Pattern longNumberPattern = RepositoryEntryMqMessage.getLongNumberPattern();
            _inputFieldsParser = new MongoJsonParser(fields, datePattern, RepositoryEntryMqMessage.TIMESTAMP_FORMAT,
                    longNumberPattern);
        }

        return (BasicDBObject) _inputFieldsParser.parse();
    }

    /**
     * Parse a string for fields. All exceptions are caught and logged. If <code>null</code> is returned, this indicates
     * that the entry should be skipped.
     * 
     * @param timetstamp
     *            Time when this log entry was created at the source on the host.
     * @param source
     *            Name of the application or service that created this log entry
     * @param host
     *            Identifies the device on which the source application or service is running. Should be full qualified
     *            domain name, static IP address, host name or dynamic IP address.
     * @param severity
     *            Classifies the importance of the entry. Can be the severity code (0-7) or text.
     * @param preparsedFields
     *            Pre-parsed fields in JSON format
     * @param message
     *            Free-form message that provides information about the event
     * @return <code>RepositoryEntryBO</code> ready for saving to mongoDB. If the entry is to be skipped and not written
     *         to mongoDB, then null is returned
     */
    public abstract RepositoryEntryBO parse(String timetstamp,
                                            String source,
                                            String host,
                                            String severity,
                                            String preparsedFields,
                                            String message);

}
