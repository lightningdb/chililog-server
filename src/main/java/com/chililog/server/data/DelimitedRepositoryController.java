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
 * Controller to read/write into a repository with a delimited log format. For example,
 * </p>
 * <code>
 * field1|field2|field3
 * </code>
 * <p>
 * The fields are delimited by the pipe character (|).
 * </p>
 * <p>
 * Unlike other data controllers, this controller is NOT a singleton. This is because many repositories may need to use
 * this class
 * </p>
 * 
 * @author vibul
 * 
 */
public class DelimitedRepositoryController extends RepositoryController
{
    private static Log4JLogger _logger = Log4JLogger.getLogger(App.class);
    private String _mongoDBCollectionName;
    private String _delimiter;
    private RepositoryInfoBO _repoInfo;
    private ArrayList<DelimitedFieldInfo> _fields = new ArrayList<DelimitedFieldInfo>();
    private Exception _lastParseError = null;

    /**
     * Delimiter repository property denotes the field delimiter character
     */
    public static final String DELIMITER_REPO_PROPERTY_NAME = "delimiter";

    /**
     * Position field property denotes the position of this field. Position 1 is the 1st field.
     */
    public static final String POSITION_REPO_FIELD_PROPERTY_NAME = "position";

    /**
     * <p>
     * Basic constructor
     * </p>
     * 
     * @param repoInfo
     *            information on the repository we are going to read/write
     * @throws ChiliLogException
     */
    public DelimitedRepositoryController(RepositoryInfoBO repoInfo) throws ChiliLogException
    {
        _repoInfo = repoInfo;

        _mongoDBCollectionName = repoInfo.getMongoDBCollectionName();
        if (StringUtils.isBlank(repoInfo.getName()))
        {
            throw new ChiliLogException(Strings.REPO_NAME_NOT_SET_ERROR);
        }

        Hashtable<String, String> properties = repoInfo.getProperties();
        _delimiter = properties.get(DELIMITER_REPO_PROPERTY_NAME);
        if (StringUtils.isBlank(_delimiter))
        {
            throw new ChiliLogException(Strings.REPO_DELIMITER_NOT_SET_ERROR, repoInfo.getName());
        }

        // Parse our field value so that we don't have to keep on doing it
        for (RepositoryFieldInfoBO f : _repoInfo.getFields())
        {
            String s = f.getProperties().get(POSITION_REPO_FIELD_PROPERTY_NAME);
            Integer i = Integer.parseInt(s) - 1;
            _fields.add(new DelimitedFieldInfo(i, f));
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

            BasicDBObject dbObject = new BasicDBObject();

            String[] ss = StringUtils.split(textEntry, _delimiter);
            for (DelimitedFieldInfo delimitedField : _fields)
            {
                String key = delimitedField.getField().getName();
                String textValue = null;
                Object value = null;
                try
                {
                    textValue = ss[delimitedField.getArrayIndex()];
                    value = delimitedField.getField().parse(textValue);
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

            return new RepositoryEntryBO(dbObject, inputName, inputIpAddress, textEntry);
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
    private static class DelimitedFieldInfo
    {
        public int _arrayIndex;
        public RepositoryFieldInfoBO _field;

        public DelimitedFieldInfo(int arrayIndex, RepositoryFieldInfoBO field)
        {
            _arrayIndex = arrayIndex;
            _field = field;
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
        public RepositoryFieldInfoBO getField()
        {
            return _field;
        }
    }
}
