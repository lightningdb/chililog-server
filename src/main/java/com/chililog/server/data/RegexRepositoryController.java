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

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;

import com.chililog.server.App;
import com.chililog.server.common.ChiliLogException;
import com.chililog.server.common.Log4JLogger;
import com.chililog.server.common.StringsProperties;
import com.chililog.server.data.RepositoryInfoBO.ParseFieldErrorHandling;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;

/**
 * <p>
 * Controller to read/write into a repository using regular expression to parse logs.
 * </p>
 * <p>
 * Unlike other data controllers, this controller is NOT a singleton. This is because many repositories may need to use
 * this class
 * </p>
 * 
 * @author vibul
 * 
 */
public class RegexRepositoryController extends RepositoryController
{
    private static Log4JLogger _logger = Log4JLogger.getLogger(App.class);
    private String _mongoDBCollectionName;
    private Pattern _pattern;
    private RepositoryInfoBO _repoInfo;
    private ArrayList<RegexFieldInfo> _fields = new ArrayList<RegexFieldInfo>();
    private Exception _lastParseError = null;

    /**
     * Pattern to apply to each log entry. If not specified, then a pattern for each field must be specified
     */
    public static final String PATTERN_REPO_PROPERTY_NAME = "pattern";

    /**
     * Optional override pattern to search for a specific field
     */
    public static final String PATTERN_REPO_FIELD_PROPERTY_NAME = "pattern";

    /**
     * Denotes the regular expression group number in which to find the contents of the field. For a pattern,
     * <code>([0-9]{4}) ([0-9]{2})</code> and text <code>1111 22</code>, group 1 is <code>1111</code> and group 2 is
     * <code>22</code>.
     */
    public static final String GROUP_REPO_FIELD_PROPERTY_NAME = "group";

    /**
     * <p>
     * Basic constructor
     * </p>
     * 
     * @param repoInfo
     *            information on the repository we are going to read/write
     * @throws ChiliLogException
     */
    public RegexRepositoryController(RepositoryInfoBO repoInfo) throws ChiliLogException
    {
        _repoInfo = repoInfo;

        _mongoDBCollectionName = repoInfo.getMongoDBCollectionName();
        if (StringUtils.isBlank(repoInfo.getName()))
        {
            throw new ChiliLogException(Strings.REPO_NAME_NOT_SET_ERROR);
        }

        Hashtable<String, String> properties = repoInfo.getProperties();
        String patternString = properties.get(PATTERN_REPO_PROPERTY_NAME);
        if (!StringUtils.isBlank(patternString))
        {
            _pattern = Pattern.compile(patternString);
        }

        // Parse our field value so that we don't have to keep on doing it
        for (RepositoryFieldInfoBO f : _repoInfo.getFields())
        {
            String fieldPatternString = f.getProperties().get(PATTERN_REPO_FIELD_PROPERTY_NAME);
            String groupString = f.getProperties().get(GROUP_REPO_FIELD_PROPERTY_NAME);
            Integer group = Integer.parseInt(groupString);
            _fields.add(new RegexFieldInfo(fieldPatternString, group, f));
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
     * @param textEntry
     *            The text for this entry to parse
     * @return <code>RepositoryEntryBO</code> ready for saving to mongoDB. If the entry cannot be parsed, then null is
     *         returned
     */
    public RepositoryEntryBO parse(String textEntry)
    {
        try
        {
            _lastParseError = null;

            if (StringUtils.isBlank(textEntry))
            {
                throw new ChiliLogException(Strings.REPO_PARSE_BLANK_ERROR, _repoInfo.getName());
            }

            BasicDBObject dbObject = new BasicDBObject();
            Matcher entryMatcher = null;
            boolean entryMatches = false;
            if (_pattern != null)
            {
                entryMatcher = _pattern.matcher(textEntry);
                entryMatches = entryMatcher.matches();
            }

            for (RegexFieldInfo regexField : _fields)
            {
                String key = regexField.getField().getName();
                String textValue = null;
                Object value = null;
                try
                {
                    Matcher fieldMatcher = null;
                    if (regexField.getPattern() != null)
                    {
                        fieldMatcher = regexField.getPattern().matcher(textEntry);
                        if (fieldMatcher.matches())
                        {
                            textValue = fieldMatcher.group(regexField.getGroup());
                        }
                    }
                    else if (entryMatches)
                    {
                        textValue = entryMatcher.group(regexField.getGroup());
                    }

                    value = regexField.getField().parse(textValue);
                    dbObject.put(key, value);
                }
                catch (Exception ex)
                {
                    ParseFieldErrorHandling technique = _repoInfo.getParseFieldErrorHandling();
                    switch (technique)
                    {
                        case SkipField:
                            String msg = StringsProperties.getInstance().getString(
                                    Strings.REPO_PARSE_FIELD_ERROR_SKIP_FIELD);
                            _logger.error(ex, msg, textValue, key, _repoInfo.getName(), textEntry);
                            break;
                        case SkipEntry:
                            throw new ChiliLogException(ex, Strings.REPO_PARSE_FIELD_ERROR_SKIP_ENTRY, textValue, key,
                                    _repoInfo.getName(), textEntry);
                        case SkipFieldIgnoreError:
                            break;// Do nothing
                        default:
                            throw new NotImplementedException("ParseFieldErrorHandling type " + technique.toString());

                    }
                }
            }

            return new RepositoryEntryBO(dbObject, textEntry);
        }
        catch (Exception ex)
        {
            _lastParseError = ex;
            _logger.error(ex, "Error parsing text entry: " + textEntry);
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

    /**
     * Context info to assist with processing
     */
    private static class RegexFieldInfo
    {
        public Pattern _pattern;
        public int _group;
        public RepositoryFieldInfoBO _field;

        /**
         * Basic constructor
         * 
         * @param pattern
         *            optional field specific pattern
         * @param group
         *            group number within the matching pattern containing the string value of this field
         * @param field
         *            meta data
         */
        public RegexFieldInfo(String pattern, int group, RepositoryFieldInfoBO field)
        {
            if (!StringUtils.isBlank(pattern))
            {
                _pattern = Pattern.compile(pattern);
            }
            _group = group;
            _field = field;
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
        public RepositoryFieldInfoBO getField()
        {
            return _field;
        }
    }
}
