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

import java.util.regex.Pattern;

import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.StringUtils;

import com.chililog.server.common.ChiliLogException;
import com.chililog.server.data.RepositoryEntryBO;
import com.chililog.server.data.RepositoryParserInfoBO;
import com.chililog.server.data.RepositoryParserInfoBO.AppliesTo;

/**
 * Parses incoming entries to extract fields and keywords
 */
public abstract class EntryParser
{
    private String _repoName;
    private RepositoryParserInfoBO _repoParserInfo;
    private Exception _lastParseError = null;

    private Pattern _sourcePattern = null;
    private String[] _sourceCSV = null;
    private Pattern _hostPattern = null;
    private String[] _hostCSV = null;

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
    public EntryParser(String repoName, RepositoryParserInfoBO repoParserInfo)
    {
        if (StringUtils.isBlank(repoName))
        {
            throw new IllegalArgumentException("repoName is blank");
        }
        if (repoParserInfo == null)
        {
            throw new NullArgumentException("repoParserInfo is null");
        }

        _repoName = repoName;
        _repoParserInfo = repoParserInfo;

        // Get our regular expression ready for matching source and host
        if (_repoParserInfo.getAppliesTo() == AppliesTo.AllowFilteredCSV)
        {
            if (!StringUtils.isBlank(_repoParserInfo.getAppliesToSourceFilter()))
            {
                _sourceCSV = _repoParserInfo.getAppliesToSourceFilter().split(",");
                for (int i = 0; i < _sourceCSV.length; i++)
                {
                    _sourceCSV[i] = _sourceCSV[i].trim();
                }
            }
            if (!StringUtils.isBlank(_repoParserInfo.getAppliesToHostFilter()))
            {
                _hostCSV = _repoParserInfo.getAppliesToHostFilter().split(",");
                for (int i = 0; i < _hostCSV.length; i++)
                {
                    _hostCSV[i] = _hostCSV[i].trim();
                }
            }
        }
        else if (_repoParserInfo.getAppliesTo() == AppliesTo.AllowFilteredRegularExpression)
        {
            if (!StringUtils.isBlank(_repoParserInfo.getAppliesToSourceFilter()))
            {
                _sourcePattern = Pattern.compile(_repoParserInfo.getAppliesToSourceFilter());
            }
            if (!StringUtils.isBlank(_repoParserInfo.getAppliesToHostFilter()))
            {
                _hostPattern = Pattern.compile(_repoParserInfo.getAppliesToHostFilter());
            }
        }

        return;
    }

    /**
     * Returns the name of the repository to which this parser it attached
     */
    public String getRepoName()
    {
        return _repoName;
    }

    /**
     * Returns the parser meta data
     */
    public RepositoryParserInfoBO getRepoParserInfo()
    {
        return _repoParserInfo;
    }

    /**
     * Returns the last error that happened during parsing
     * 
     * @return
     */
    public Exception getLastParseError()
    {
        return _lastParseError;
    }

    /**
     * Sets the last error
     * 
     * @param lastParseError
     *            Exception thrown during parsing
     */
    protected void setLastParseError(Exception lastParseError)
    {
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
    public boolean isApplicable(String source, String host)
    {
        if (_repoParserInfo.getAppliesTo() == AppliesTo.All)
        {
            return true;
        }
        else if (_repoParserInfo.getAppliesTo() == AppliesTo.AllowFilteredCSV)
        {
            if (!StringUtils.isBlank(source) && _sourceCSV != null)
            {
                for (String s : _sourceCSV)
                {
                    if (s.equalsIgnoreCase(source))
                    {
                        return true;
                    }
                }
            }
            if (!StringUtils.isBlank(host) && _hostCSV != null)
            {
                for (String s : _hostCSV)
                {
                    if (s.equalsIgnoreCase(host))
                    {
                        return true;
                    }
                }
            }
        }
        else if (_repoParserInfo.getAppliesTo() == AppliesTo.AllowFilteredRegularExpression)
        {
            if (!StringUtils.isBlank(source) && _sourcePattern != null)
            {
                return _sourcePattern.matcher(source).matches();
            }
            if (!StringUtils.isBlank(host) && _hostPattern != null)
            {
                return _hostPattern.matcher(host).matches();
            }
        }
        return false;
    }

    /**
     * Parse a string for fields. All exceptions are caught and logged. If <code>null</code> is returned, this indicates
     * that the entry should be skipped.
     * 
     * @param source
     *            Name of the application or service that created this log entry
     * @param host
     *            Identifies the device on which the source application or service is running. Should be full qualified
     *            domain name, static IP address, host name or dynamic IP address.
     * @param severity
     *            Classifies the importance of the entry
     * @param parsedFields
     *            Fields as parsed by an {@link EntryParser}.
     * @param message
     *            Free-form message that provides information about the event
     * @return <code>RepositoryEntryBO</code> ready for saving to mongoDB. If the entry is to be skipped and not written
     *         to mongoDB, then null is returned
     */
    public abstract RepositoryEntryBO parse(String source, String host, long serverity, String message);

}
