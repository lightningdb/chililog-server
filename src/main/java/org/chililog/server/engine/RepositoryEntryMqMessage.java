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

package org.chililog.server.engine;

import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.regex.Pattern;

import org.chililog.server.data.RepositoryEntryListCriteria;
import org.chililog.server.data.RepositoryEntryBO.Severity;

/**
 * Contains the specification for a log entry HornetQ message
 * 
 * @author vibul
 */
public class RepositoryEntryMqMessage {

    /**
     * HornetQ string property identifying the time stamp of the event log. Format is: 2001-12-31T23:01:01.000Z
     */
    public static final String TIMESTAMP = "Timestamp";

    /**
     * HornetQ string property identifying the application or service that created the log entry
     */
    public static final String SOURCE = "Source";

    /**
     * HornetQ string property identifying the computer name or IP address on which the source created the log entry
     */
    public static final String HOST = "Host";

    /**
     * HornetQ string property identifying the severity of the message. The severity code as a long integer is expected.
     * See {@link Severity}.
     */
    public static final String SEVERITY = "Severity";

    /**
     * <p>
     * HornetQ string property identifying the fields to be stored with this log entry. This is useful for publishers
     * that performs parsing of fixed format data. It means that the storage worker will not have to perform parsing.
     * </p>
     * <p>
     * The string must be in JSON format.
     * </p>
     */
    public static final String FIELDS = "Fields";

    /**
     * Time stamp format for use with {@link SimpleDateFormat} containing a flexible timezone. The format is
     * "yyyy-MM-dd'T'HH:mm:ss.SSSZ".
     */
    public static final String TIMESTAMP_FORMAT = RepositoryEntryListCriteria.DATE_FORMAT;

    /**
     * Create a new date formatter containing the our standard time format and time zone
     * 
     * @return SimpleDateFormat
     */
    public static SimpleDateFormat getDateFormatter() {
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        sf.setTimeZone(TimeZone.getTimeZone("GMT"));
        return sf;
    }

    /**
     * Create a new regular expression to match date and time in our standard format. For example,
     * "2011-09-24T12:34:43.123Z".
     * 
     * @return Regular expression pattern.
     */
    public static Pattern getTimestampPattern() {
        return RepositoryEntryListCriteria.DATE_PATTERN;
    }

    /**
     * Create a new regular expression to match long numbers in JSON. By default, we only parse for int or doubles. This
     * allows long numbers to be specified in JSON. For example: "NumberLong(1)".
     * 
     * @return Regular expression pattern.
     */
    public static Pattern getLongNumberPattern() {
        return RepositoryEntryListCriteria.LONG_NUMBER_PATTERN;
    }

}
