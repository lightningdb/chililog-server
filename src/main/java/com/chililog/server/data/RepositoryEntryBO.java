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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;

import org.apache.commons.lang.StringUtils;

import com.chililog.server.common.ChiliLogException;
import com.chililog.server.engine.parsers.EntryParser;
import com.mongodb.DBObject;

/**
 * <p>
 * The Business Object encapsulating an entry (record or row) in a repository. All the data is stored and accessed by
 * <code>toDBOject</code>.
 * </p>
 * 
 * @author vibul
 * 
 */
public class RepositoryEntryBO extends BO implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Date _timestamp;
    private Date _savedTimestamp;
    private String _source;
    private String _host;
    private Severity _severity;
    private String _message;
    private ArrayList<String> _keywords = new ArrayList<String>();

    public static final String TIMESTAMP_FIELD_NAME = "c_ts";
    public static final String SAVED_TIMESTAMP_FIELD_NAME = "c_saved_ts";
    public static final String SOURCE_FIELD_NAME = "c_source";
    public static final String HOST_FIELD_NAME = "c_host";
    public static final String SEVERITY_FIELD_NAME = "c_severity";
    public static final String MESSAGE_FIELD_NAME = "c_message";
    public static final String KEYWORDS_FIELD_NAME = "c_keywords";

    /**
     * Basic constructor
     */
    public RepositoryEntryBO()
    {
        return;
    }

    /**
     * Constructor that loads our properties retrieved from the mongoDB dbObject
     * 
     * @param dbObject
     *            database object as retrieved from mongoDB
     * @throws ChiliLogException
     */
    public RepositoryEntryBO(DBObject dbObject) throws ChiliLogException
    {
        super(dbObject);
        _timestamp = MongoUtils.getDate(dbObject, TIMESTAMP_FIELD_NAME, true);
        _savedTimestamp = MongoUtils.getDate(dbObject, SAVED_TIMESTAMP_FIELD_NAME, true);
        _source = MongoUtils.getString(dbObject, SOURCE_FIELD_NAME, true);
        _host = MongoUtils.getString(dbObject, HOST_FIELD_NAME, true);
        _severity = Severity.fromCode(MongoUtils.getLong(dbObject, SEVERITY_FIELD_NAME, true));
        _keywords = MongoUtils.getStringArrayList(dbObject, KEYWORDS_FIELD_NAME, false);
        _message = MongoUtils.getString(dbObject, MESSAGE_FIELD_NAME, true);
        return;
    }

    /**
     * Constructor for a new entry.
     * 
     * @param timestamp
     *            Timestamp for the logged event
     * @param source
     *            Name of the application or service that created this log entry
     * @param host
     *            Identifies the device on which the source application or service is running. Should be full qualified
     *            domain name, static IP address, host name or dynamic IP address.
     * @param severity
     *            Classifies the importance of the entry
     * @param keywords
     *            List of keywords associated with this entry
     * @param parsedFields
     *            Fields as parsed by an {@link EntryParser}.
     * @param message
     *            Free-form message that provides information about the event
     * @throws ChiliLogException
     */
    public RepositoryEntryBO(Date timestamp,
                             String source,
                             String host,
                             Severity severity,
                             ArrayList<String> keywords,
                             String message,
                             DBObject parsedFields) throws ChiliLogException
    {
        super(parsedFields);
        _timestamp = timestamp;
        _savedTimestamp = new Date();
        _source = source;
        _host = host;
        _severity = severity;
        _keywords = keywords;
        _message = message;
        return;
    }

    /**
     * Puts our properties into the mongoDB object so that it can be saved
     * 
     * @param dbObject
     *            mongoDB database object that can be used for saving
     */
    @Override
    protected void savePropertiesToDBObject(DBObject dbObject) throws ChiliLogException
    {
        MongoUtils.setDate(dbObject, TIMESTAMP_FIELD_NAME, _timestamp, true);
        MongoUtils.setDate(dbObject, SAVED_TIMESTAMP_FIELD_NAME, _savedTimestamp, true);
        MongoUtils.setString(dbObject, SOURCE_FIELD_NAME, _source, true);
        MongoUtils.setString(dbObject, HOST_FIELD_NAME, _host, true);
        MongoUtils.setLong(dbObject, SEVERITY_FIELD_NAME, _severity.toCode(), true);
        MongoUtils.setStringArrayList(dbObject, KEYWORDS_FIELD_NAME, _keywords, false);
        MongoUtils.setString(dbObject, MESSAGE_FIELD_NAME, _message, true);
        return;
    }

    /**
     * Returns the date on which the log entry was created at the source; i.e. timestamp for the event that generated
     * the log entry
     */
    public Date getTimestamp()
    {
        return _timestamp;
    }

    public void setTimestamp(Date timestamp)
    {
        _timestamp = timestamp;
    }

    /**
     * Returns the date on which the entry was save into the ChiliLog repository
     */
    public Date getSavedTimestamp()
    {
        return _savedTimestamp;
    }

    public void setSavedTimestamp(Date timestamp)
    {
        _savedTimestamp = timestamp;
    }

    /**
     * Returns the name of the application or service that created this log entry
     */
    public String getSource()
    {
        return _source;
    }

    public void setSource(String source)
    {
        _source = source;
    }

    /**
     * Identifies the device on which the source application or service is running. Should be full qualified domain
     * name, static IP address, host name or dynamic IP address (in this order of preference).
     */
    public String getHost()
    {
        return _host;
    }

    public void setHost(String host)
    {
        _host = host;
    }

    /**
     * Returns the classification of the importance of the entry
     */
    public Severity getSeverity()
    {
        return _severity;
    }

    public void setSeverity(Severity severity)
    {
        _severity = severity;
    }

    /**
     * Free-form message that provides information about the event that triggered this entry
     */
    public String getMessage()
    {
        return _message;
    }

    public void setMessage(String message)
    {
        _message = message;
    }

    /**
     * Returns the list of keywords for this message
     */
    public ArrayList<String> getKeywords()
    {
        return _keywords;
    }

    public void setKeywords(ArrayList<String> keywords)
    {
        _keywords = keywords;
    }

    /**
     * Severity codes taken from syslog format. See http://tools.ietf.org/html/rfc5424.
     */
    public enum Severity
    {
        /**
         * Emergency: system is unusable
         */
        Emergency(0),

        /**
         * Alert: action must be taken immediately
         */
        Action(1),

        /**
         * Critical: critical conditions
         */
        Critical(2),

        /**
         * Error: error conditions
         */
        Error(3),

        /**
         * Warning: warning conditions
         */
        Warning(4),

        /**
         * Notice: normal but significant condition
         */
        Notice(5),

        /**
         * Informational: informational messages
         */
        Information(6),

        /**
         * Debug: debug-level messages
         */
        Debug(7);

        private static Severity[] lookup = null;

        static
        {
            EnumSet<Severity> es = EnumSet.allOf(Severity.class);
            lookup = new Severity[es.size()];
            for (Severity s : es)
            {
                lookup[(int) s.code] = s;
            }
        }

        private long code;

        private Severity(long code)
        {
            this.code = code;
        }

        public Long toCode()
        {
            return code;
        }

        public static Severity fromString(String s)
        {
            return Enum.valueOf(Severity.class, s);
        }

        /**
         * Parses a code or description into a severity. If any errors, default to Information.
         * 
         * @param codeOrDescription
         *            String of code "0-7" or description "Error".
         * @return Severity
         */
        public static Severity parse(String codeOrDescription)
        {
            if (StringUtils.isBlank(codeOrDescription))
            {
                return Severity.Information;
            }

            try
            {
                // It should be quicker if we don't parse
                if (codeOrDescription.equals("0"))
                {
                    return lookup[0];
                }
                if (codeOrDescription.equals("1"))
                {
                    return lookup[1];
                }
                if (codeOrDescription.equals("2"))
                {
                    return lookup[2];
                }
                if (codeOrDescription.equals("3"))
                {
                    return lookup[3];
                }
                if (codeOrDescription.equals("4"))
                {
                    return lookup[4];
                }
                if (codeOrDescription.equals("5"))
                {
                    return lookup[5];
                }
                if (codeOrDescription.equals("6"))
                {
                    return lookup[6];
                }
                if (codeOrDescription.equals("7"))
                {
                    return lookup[7];
                }

                if (codeOrDescription.length() == 1)
                {
                    return lookup[Integer.parseInt(codeOrDescription)];
                }

                return Enum.valueOf(Severity.class, codeOrDescription);
            }
            catch (Exception ex)
            {
                // Just return info and ignore the error
                return Severity.Information;
            }
        }

        public static Severity fromCode(long code)
        {
            try
            {
                return lookup[(int) code];
            }
            catch (Exception ex)
            {
                // Just return info and ignore the error
                return Severity.Information;
            }
        }
    }
}
