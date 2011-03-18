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

import java.text.SimpleDateFormat;
import java.util.Hashtable;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import com.chililog.server.App;
import com.chililog.server.common.ChiliLogException;
import com.chililog.server.common.Log4JLogger;
import com.mongodb.DB;
import com.mongodb.DBObject;

/**
 * <p>
 * Controller to read/write into a repository with a json log format. For example,
 * </p>
 * <code>
 * { "field1": 1, "field2": "abc", "field3": 123 }
 * </code>
 * <p>
 * Field definitions are not required because it is defined in the JSON format.
 * </p>
 * <p>
 * Unlike other data controllers, this controller is NOT a singleton. This is because many repositories may need to use
 * this class
 * </p>
 * 
 * @author vibul
 * 
 */
public class JsonRepositoryController extends RepositoryController
{
    private static Log4JLogger _logger = Log4JLogger.getLogger(App.class);
    private String _mongoDBCollectionName;
    private RepositoryInfoBO _repoInfo;
    private Exception _lastParseError = null;
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
    public static final String DATE_PATTERN_REPO_PROPERTY_NAME = "date_pattern";

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
    public static final String DATE_FORMAT_REPO_PROPERTY_NAME = "date_format";

    /**
     * <p>
     * Regular expression pattern to use in order to identify a long within a string. For example, if "^([0-9]+L)$",
     * then "88888L" will be treated as a Long.
     * </p>
     * <p>
     * If not set, long number parsing on a string will not be performed.
     * </p>
     */
    public static final String LONG_NUMBER_PATTERN_REPO_PROPERTY_NAME = "long_suffix";

    /**
     * <p>
     * Basic constructor
     * </p>
     * 
     * @param repoInfo
     *            information on the repository we are going to read/write
     * @throws ChiliLogException
     */
    public JsonRepositoryController(RepositoryInfoBO repoInfo) throws ChiliLogException
    {
        _repoInfo = repoInfo;

        _mongoDBCollectionName = repoInfo.getMongoDBCollectionName();
        if (StringUtils.isBlank(repoInfo.getName()))
        {
            throw new ChiliLogException(Strings.REPO_NAME_NOT_SET_ERROR);
        }

        Hashtable<String, String> properties = repoInfo.getProperties();

        String s = properties.get(DATE_PATTERN_REPO_PROPERTY_NAME);
        if (!StringUtils.isBlank(s))
        {
            _datePattern = Pattern.compile(s);
        }

        _dateFormat = properties.get(DATE_FORMAT_REPO_PROPERTY_NAME);

        s = properties.get(LONG_NUMBER_PATTERN_REPO_PROPERTY_NAME);
        if (!StringUtils.isBlank(s))
        {
            _longNumberPattern = Pattern.compile(s);
        }

        _repoInfo.loadFieldDataTypeProperties();
        return;
    }

    /**
     * Returns the name of the mongoDB collection for this business object
     */
    @Override
    protected String getDBCollectionName()
    {
        return _mongoDBCollectionName;
    }

    /**
     * Parse a string for fields. All exceptions are caught and logged. If <code>null</code> is returned, this indicates
     * that the entry should be skipped.
     * 
     * @param inputName
     *            Name of the input device or application that created this text entry
     * @param inputIpAddress
     *            IP address of the input device or application that created this text entry
     * @param textEntry
     *            The text for this entry to parse
     * @return <code>RepositoryEntryBO</code> ready for saving to mongoDB. If the entry cannot be parsed, then null is
     *         returned
     */
    public RepositoryEntryBO parse(String inputName, String inputIpAddress, String textEntry)
    {
        try
        {
            _lastParseError = null;

            if (StringUtils.isBlank(textEntry))
            {
                throw new ChiliLogException(Strings.REPO_PARSE_BLANK_ERROR, _repoInfo.getName());
            }

            MongoJsonParser parser = new MongoJsonParser(textEntry, _datePattern, _dateFormat, _longNumberPattern);
            DBObject dbObject = (DBObject) parser.parse();
            return new RepositoryEntryBO(dbObject, inputName, inputIpAddress, textEntry);
        }
        catch (Exception ex)
        {
            _lastParseError = ex;
            _logger.error(ex, "Error parsing json entry: " + textEntry);
            return null;
        }
    }

    /**
     * Saves the repository into mongoDB
     * 
     * @param db
     *            MongoDb connection
     * @param repositoryEntry
     *            Repository entry to save
     * @throws ChiliLogException
     *             if there are errors
     */
    public void save(DB db, RepositoryEntryBO repositoryEntry) throws ChiliLogException
    {
        super.save(db, repositoryEntry);
    }

    /**
     * Removes the specified user from mongoDB
     * 
     * @param db
     *            MongoDb connection
     * @param repositoryEntry
     *            Repository entry to remove
     * @throws ChiliLogException
     *             if there are errors
     */
    public void remove(DB db, RepositoryEntryBO repositoryEntry) throws ChiliLogException
    {
        super.remove(db, repositoryEntry);
    }

    /**
     * Returns the last error processed by <code>parse</code>.
     */
    public Exception getLastParseError()
    {
        return _lastParseError;
    }

}
