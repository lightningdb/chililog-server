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

package com.chililog.server.engine;

import java.net.InetAddress;
import java.util.Date;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Layout;
import org.apache.log4j.spi.LoggingEvent;

import com.chililog.server.common.ChiliLogException;
import com.chililog.server.data.BO;
import com.chililog.server.data.MongoConnection;
import com.chililog.server.data.MongoUtils;
import com.chililog.server.data.RepositoryEntryBO;
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
public class InternalLog4JAppender extends AppenderSkeleton
{
    private String _machineName;
    private String _machineIpAddress;
    private DB _db;
    private DBCollection _coll;

    static final String REPOSITORY_NAME = "chililog";
    static final String MONGODB_COLLECTION_NAME = "chililog_repository";
    static final String EVENT_TIMESTAMP_FIELD_NAME = "event_timestamp";

    static final String SERVER_NAME_FIELD_NAME = "server_name";
    static final String SERVER_IP_ADDRESS_FIELD_NAME = "server_ip_address";
    static final String THREAD_FIELD_NAME = "thread";

    static final String LEVEL_FIELD_NAME = "level";
    static final String CATEGORY_FIELD_NAME = "category";
    static final String MESSAGE_FIELD_NAME = "message";

    /**
     * Basic Constructor
     * 
     * @throws ChiliLogException
     */
    public InternalLog4JAppender() throws ChiliLogException
    {
        _db = MongoConnection.getInstance().getConnection();
        _coll = _db.getCollection(MONGODB_COLLECTION_NAME);

        try
        {
            InetAddress addr = InetAddress.getLocalHost();
            _machineIpAddress = addr.getHostAddress();
            _machineName = addr.getHostName();
        }
        catch (Exception e)
        {
            _machineIpAddress = "unknown";
            _machineName = "unknown";
        }

        return;
    }

    public boolean requiresLayout()
    {
        return false;
    }

    @Override
    protected void append(LoggingEvent event)
    {
        try
        {
            DBObject dbObject = new BasicDBObject();
            MongoUtils.setDate(dbObject, EVENT_TIMESTAMP_FIELD_NAME, new Date(event.getTimeStamp()));

            MongoUtils.setString(dbObject, SERVER_NAME_FIELD_NAME, _machineName);
            MongoUtils.setString(dbObject, SERVER_IP_ADDRESS_FIELD_NAME, _machineIpAddress);
            MongoUtils.setString(dbObject, THREAD_FIELD_NAME, event.getThreadName());

            MongoUtils.setString(dbObject, LEVEL_FIELD_NAME, event.getLevel().toString());
            MongoUtils.setString(dbObject, CATEGORY_FIELD_NAME, event.getLoggerName());

            StringBuilder msg = new StringBuilder();
            if (event.getMessage() == null)
            {
                MongoUtils.setString(dbObject, MESSAGE_FIELD_NAME, null);
            }
            else
            {
                msg.append(event.getMessage().toString());
            }
            String[] s = event.getThrowableStrRep();
            if (s != null)
            {
                int len = s.length;
                for (int i = 0; i < len; i++)
                {
                    msg.append(s[i]);
                    msg.append(Layout.LINE_SEP);
                }
            }
            MongoUtils.setString(dbObject, MESSAGE_FIELD_NAME, msg.toString());

            MongoUtils.setDate(dbObject, RepositoryEntryBO.ENTRY_TIMESTAMP_FIELD_NAME, new Date());
            MongoUtils.setString(dbObject, RepositoryEntryBO.ENTRY_TEXT_FIELD_NAME, "");

            MongoUtils.setLong(dbObject, BO.RECORD_VERSION_FIELD_NAME, (long) 1);

            _coll.insert(dbObject);

            return;
        }
        catch (Exception ex)
        {
            // ignore it and print to standard error
            ex.printStackTrace();
        }
    }


    public void close()
    {
        // TODO Auto-generated method stub

    }

}
