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

import java.util.ArrayList;
import java.util.Date;

import org.chililog.server.common.ChiliLogException;
import org.chililog.server.common.Log4JLogger;
import org.chililog.server.data.RepositoryEntryBO;
import org.chililog.server.data.RepositoryConfigBO;
import org.chililog.server.data.RepositoryParserConfigBO;
import org.chililog.server.data.RepositoryEntryBO.Severity;

/**
 * <p>
 * Default parser that does not extract any fields. Just create a {@link RepositoryEntryBO} with keywords.
 * </p>
 * 
 * @author vibul
 * 
 */
public class DefaultEntryParser extends EntryParser {

    private static Log4JLogger _logger = Log4JLogger.getLogger(DefaultEntryParser.class);

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
    public DefaultEntryParser(RepositoryConfigBO repoInfo, RepositoryParserConfigBO repoParserInfo)
            throws ChiliLogException {
        super(repoInfo, repoParserInfo);
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

            Severity sev = Severity.parse(severity);
            ArrayList<String> keywords = parseKeywords(source, host, sev, message);

            RepositoryEntryBO entry = new RepositoryEntryBO();
            entry.setTimestamp(parseTimestamp(timestamp));
            entry.setSavedTimestamp(new Date());
            entry.setSource(source);
            entry.setHost(host);
            entry.setSeverity(sev);
            entry.setKeywords(keywords);
            entry.setMessage(message);
            return entry;
        } catch (Exception ex) {
            this.setLastParseError(ex);
            _logger.error(ex, "Error parsing text entry: " + message);
            return null;
        }
    }

}
