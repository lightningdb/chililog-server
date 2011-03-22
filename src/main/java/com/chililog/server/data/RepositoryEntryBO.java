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

    private Date _entryTimestamp;
    private String _entrySource;
    private String _entryHost;
    private Severity _entrySeverity;
    private String _entryMessage;
    private ArrayList<String> _entryKeywords = new ArrayList<String>();

    public static final String ENTRY_TIMESTAMP_FIELD_NAME = "entry_timestamp";
    public static final String ENTRY_SOURCE_FIELD_NAME = "entry_source";
    public static final String ENTRY_HOST_FIELD_NAME = "entry_host";
    public static final String ENTRY_SEVERITY_FIELD_NAME = "entry_severity";
    public static final String ENTRY_MESSAGE_FIELD_NAME = "entry_message";
    public static final String ENTRY_KEYWORDS_FIELD_NAME = "entry_keywords";

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
        _entryTimestamp = MongoUtils.getDate(dbObject, ENTRY_TIMESTAMP_FIELD_NAME, true);
        _entrySource = MongoUtils.getString(dbObject, ENTRY_SOURCE_FIELD_NAME, true);
        _entryHost = MongoUtils.getString(dbObject, ENTRY_HOST_FIELD_NAME, true);
        _entrySeverity = Severity.fromCode(MongoUtils.getLong(dbObject, ENTRY_SEVERITY_FIELD_NAME, true));
        _entryMessage = MongoUtils.getString(dbObject, ENTRY_MESSAGE_FIELD_NAME, true);
        _entryKeywords = MongoUtils.getStringArrayList(dbObject, ENTRY_KEYWORDS_FIELD_NAME, false);
        return;
    }

    /**
     * Constructor for a new entry.
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
     * @throws ChiliLogException
     */
    public RepositoryEntryBO(String source, String host, Severity severity, String message, DBObject parsedFields)
            throws ChiliLogException
    {
        super(parsedFields);
        _entryTimestamp = new Date();
        _entrySource = source;
        _entryHost = host;
        _entrySeverity = severity;
        _entryMessage = message;

        // TODO - get keywords

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
        MongoUtils.setDate(dbObject, ENTRY_TIMESTAMP_FIELD_NAME, _entryTimestamp);
        MongoUtils.setString(dbObject, ENTRY_SOURCE_FIELD_NAME, _entrySource);
        MongoUtils.setString(dbObject, ENTRY_HOST_FIELD_NAME, _entryHost);
        MongoUtils.setLong(dbObject, ENTRY_SEVERITY_FIELD_NAME, _entrySeverity.toCode());
        MongoUtils.setString(dbObject, ENTRY_MESSAGE_FIELD_NAME, _entryMessage);
        MongoUtils.setStringArrayList(dbObject, ENTRY_KEYWORDS_FIELD_NAME, _entryKeywords);
        return;
    }

    /**
     * Returns the date on which the entry was save into the ChiliLog repository
     */
    public Date getEntryTimestamp()
    {
        return _entryTimestamp;
    }

    public void setEntryTimestamp(Date chililoggedOn)
    {
        _entryTimestamp = chililoggedOn;
    }

    /**
     * Returns the name of the application or service that created this log entry
     */
    public String getEntrySource()
    {
        return _entrySource;
    }

    public void setEntrySource(String entrySource)
    {
        _entrySource = entrySource;
    }

    /**
     * Identifies the device on which the source application or service is running. Should be full qualified domain
     * name, static IP address, host name or dynamic IP address (in this order of preference).
     */
    public String getEntryHost()
    {
        return _entryHost;
    }

    public void setEntryHost(String entryHost)
    {
        _entryHost = entryHost;
    }

    /**
     * Returns the classification of the importance of the entry
     */
    public Severity getEntrySeverity()
    {
        return _entrySeverity;
    }

    public void setEntrySeverity(Severity entrySeverity)
    {
        _entrySeverity = entrySeverity;
    }

    /**
     * Free-form message that provides information about the event that triggered this entry
     */
    public String getEntryMessage()
    {
        return _entryMessage;
    }

    public void setEntryMessage(String entryMessage)
    {
        _entryMessage = entryMessage;
    }

    /**
     * Returns the list of keywords for this message
     */
    public ArrayList<String> getEntryKeywords()
    {
        return _entryKeywords;
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

        public static Severity fromCode(String code)
        {
            return lookup[Integer.parseInt(code)];
        }
        
        public static Severity fromCode(long code)
        {
            return lookup[(int) code];
        }
    }
}
