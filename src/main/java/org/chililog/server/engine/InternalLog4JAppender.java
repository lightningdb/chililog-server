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

import java.net.InetAddress;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;
import org.chililog.server.common.ChiliLogException;
import org.chililog.server.common.TextTokenizer;
import org.chililog.server.data.BO;
import org.chililog.server.data.MongoConnection;
import org.chililog.server.data.MongoUtils;
import org.chililog.server.data.RepositoryEntryBO;
import org.chililog.server.data.RepositoryEntryBO.Severity;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

/**
 * <p>
 * Internal log4j appender that directory writes to mongoDB without going through the message queue
 * </p>
 * <p>
 * Should be used with the Log4J AsyncAppender.
 * </p>
 * 
 * @author vibul
 * 
 */
public class InternalLog4JAppender extends AppenderSkeleton {

    private TextTokenizer _tokenizer;
    private String _host;
    private DB _db;
    private DBCollection _coll;

    static final String REPOSITORY_NAME = "chililog";
    static final String MONGODB_COLLECTION_NAME = "repo_chililog";

    // fld_ prefix is used to make these fields
    static final String THREAD_FIELD_NAME = "fld_thread";
    static final String CATEGORY_FIELD_NAME = "fld_category";

    /**
     * Basic Constructor
     * 
     * @throws ChiliLogException
     */
    public InternalLog4JAppender() throws ChiliLogException {
        _tokenizer = TextTokenizer.getInstance();
        _db = MongoConnection.getInstance().getConnection();
        _coll = _db.getCollection(MONGODB_COLLECTION_NAME);

        try {
            InetAddress addr = InetAddress.getLocalHost();
            _host = addr.getHostName();
            if (StringUtils.isBlank(_host)) {
                _host = addr.getHostAddress();
            }
        } catch (Exception e) {
            _host = "unknown";
        }

        return;
    }

    public boolean requiresLayout() {
        return false;
    }

    @Override
    protected void append(LoggingEvent event) {
        try {
            // If not message, then there's nothing to record
            if (event.getMessage() == null) {
                return;
            }

            DBObject dbObject = new BasicDBObject();

            // Custom Fields
            MongoUtils.setString(dbObject, THREAD_FIELD_NAME, event.getThreadName(), true);
            MongoUtils.setString(dbObject, CATEGORY_FIELD_NAME, event.getLoggerName(), true);

            // Message Field
            StringBuilder sb = new StringBuilder(event.getMessage().toString());
            String[] s = event.getThrowableStrRep();
            if (s != null) {
                int len = s.length;
                for (int i = 0; i < len; i++) {
                    sb.append(s[i]);
                    sb.append(Layout.LINE_SEP);
                }
            }

            // Severity
            Severity severity = Severity.Information;
            Level level = event.getLevel();
            if (level == Level.DEBUG || level == Level.TRACE) {
                severity = Severity.Debug;
            } else if (level == Level.WARN) {
                severity = Severity.Warning;
            } else if (level == Level.ERROR) {
                severity = Severity.Error;
            } else if (level == Level.FATAL) {
                severity = Severity.Emergency;
            }

            MongoUtils.setDate(dbObject, RepositoryEntryBO.TIMESTAMP_FIELD_NAME, new Date(event.getTimeStamp()), true);
            MongoUtils.setDate(dbObject, RepositoryEntryBO.SAVED_TIMESTAMP_FIELD_NAME, new Date(), true);
            MongoUtils.setString(dbObject, RepositoryEntryBO.SOURCE_FIELD_NAME, "ChiliLogServer", true);
            MongoUtils.setString(dbObject, RepositoryEntryBO.HOST_FIELD_NAME, _host, true);
            MongoUtils.setLong(dbObject, RepositoryEntryBO.SEVERITY_FIELD_NAME, severity.toCode(), true);

            String msg = sb.toString();
            MongoUtils.setStringArrayList(dbObject, RepositoryEntryBO.KEYWORDS_FIELD_NAME,
                    _tokenizer.tokenize(msg, 20), true);
            MongoUtils.setString(dbObject, RepositoryEntryBO.MESSAGE_FIELD_NAME, msg, true);

            MongoUtils.setLong(dbObject, BO.DOCUMENT_VERSION_FIELD_NAME, (long) 1, true);

            _coll.insert(dbObject);

            return;
        } catch (Exception ex) {
            // ignore it and print to standard error
            ex.printStackTrace();
        }
    }

    public void close() {
        // TODO Auto-generated method stub

    }

}
