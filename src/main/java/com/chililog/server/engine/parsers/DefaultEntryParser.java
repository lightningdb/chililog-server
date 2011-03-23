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

import java.util.Date;

import com.chililog.server.common.ChiliLogException;
import com.chililog.server.common.Log4JLogger;
import com.chililog.server.data.RepositoryEntryBO;
import com.chililog.server.data.RepositoryEntryBO.Severity;
import com.chililog.server.data.RepositoryParserInfoBO;

/**
 * <p>
 * Default parser that does not extract any fields. Just create a {@link RepositoryEntryBO} with keywords.
 * </p>
 * 
 * @author vibul
 * 
 */
public class DefaultEntryParser extends EntryParser
{
    private static Log4JLogger _logger = Log4JLogger.getLogger(DefaultEntryParser.class);

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
    public DefaultEntryParser(String repoName, RepositoryParserInfoBO repoParserInfo) throws ChiliLogException
    {
        super(repoName, repoParserInfo);
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

            RepositoryEntryBO entry = new RepositoryEntryBO();
            entry.setEntryTimestamp(new Date());
            entry.setEntrySource(source);
            entry.setEntryHost(host);
            entry.setEntrySeverity(severity);
            entry.setEntryMessage(message);
            return entry;
        }
        catch (Exception ex)
        {
            this.setLastParseError(ex);
            _logger.error(ex, "Error parsing text entry: " + message);
            return null;
        }
    }

}
