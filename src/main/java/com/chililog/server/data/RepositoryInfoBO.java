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
import java.util.Hashtable;

import com.chililog.server.common.ChiliLogException;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * <p>
 * This class contains information that describes a repository
 * </p>
 * 
 * @author vibul
 * 
 */
public class RepositoryInfoBO extends BO implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String _name;
    private String _displayName;
    private String _description;
    private String _controllerClassName;
    private Status _startupStatus = Status.ONLINE;
    private boolean _readQueueDurable = false;
    private boolean _writeQueueDurable = false;
    private long _writeQueueWorkerCount = 1;
    private long _writeQueueMaxMemory = 1024 * 1024 * 20; // 20 MB
    private QueueMaxMemoryPolicy _writeQueueMaxMemoryPolicy = QueueMaxMemoryPolicy.PAGE;
    private long _writeQueuePageSize = 1024 * 1024 * 4; // MB
    private ParseFieldErrorHandling _parseFieldErrorHandling = ParseFieldErrorHandling.SkipField;
    private ArrayList<RepositoryFieldInfoBO> _fields = new ArrayList<RepositoryFieldInfoBO>();
    private Hashtable<String, String> _properties = new Hashtable<String, String>();

    static final String NAME_FIELD_NAME = "name";
    static final String DISPLAY_NAME_FIELD_NAME = "display_name";
    static final String DESCRIPTION_FIELD_NAME = "description";
    static final String CONTROLLER_CLASS_NAME_FIELD_NAME = "controller_class_name";
    static final String STARTUP_STATUS_FIELD_NAME = "startup_status";
    static final String DURABLE_READ_QUEUE_FIELD_NAME = "is_read_queue_durable";
    static final String WRITE_QUEUE_DURABLE_FIELD_NAME = "is_write_queue_durable";
    static final String WRITE_QUEUE_WORKER_COUNT_FIELD_NAME = "write_queue_worker_count";
    static final String WRITE_QUEUE_MAX_MEMORY_FIELD_NAME = "write_queue_max_memory";
    static final String WRITE_QUEUE_MAX_MEMORY_POLICY_FIELD_NAME = "write_queue_max_memory_policy";
    static final String WRITE_QUEUE_PAGE_SIZE_FIELD_NAME = "write_queue_page_size";
    static final String FIELDS_FIELD_NAME = "fields";
    static final String PROPERTIES_FIELD_NAME = "properties";
    static final String PARSE_FIELD_ERROR_HANDLING_FIELD_NAME = "parse_field_error_handling";

    /**
     * Basic constructor
     */
    public RepositoryInfoBO()
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
    public RepositoryInfoBO(DBObject dbObject) throws ChiliLogException
    {
        super(dbObject);
        _name = MongoUtils.getString(dbObject, NAME_FIELD_NAME, true);
        _displayName = MongoUtils.getString(dbObject, DISPLAY_NAME_FIELD_NAME, false);
        _description = MongoUtils.getString(dbObject, DESCRIPTION_FIELD_NAME, false);
        _controllerClassName = MongoUtils.getString(dbObject, CONTROLLER_CLASS_NAME_FIELD_NAME, false);
        _startupStatus = Status.valueOf(MongoUtils.getString(dbObject, STARTUP_STATUS_FIELD_NAME, true));

        _readQueueDurable = MongoUtils.getBoolean(dbObject, DURABLE_READ_QUEUE_FIELD_NAME, true);

        _writeQueueDurable = MongoUtils.getBoolean(dbObject, WRITE_QUEUE_DURABLE_FIELD_NAME, true);
        _writeQueueWorkerCount = MongoUtils.getLong(dbObject, WRITE_QUEUE_WORKER_COUNT_FIELD_NAME, true);
        _writeQueueMaxMemory = MongoUtils.getLong(dbObject, WRITE_QUEUE_MAX_MEMORY_FIELD_NAME, true);
        _writeQueueMaxMemoryPolicy = QueueMaxMemoryPolicy.valueOf(MongoUtils.getString(dbObject,
                WRITE_QUEUE_MAX_MEMORY_POLICY_FIELD_NAME, true));
        _writeQueuePageSize = MongoUtils.getLong(dbObject, WRITE_QUEUE_PAGE_SIZE_FIELD_NAME, true);

        _parseFieldErrorHandling = ParseFieldErrorHandling.valueOf(MongoUtils.getString(dbObject,
                PARSE_FIELD_ERROR_HANDLING_FIELD_NAME, true));

        _properties = MongoUtils.getKeyValuePairs(dbObject, PROPERTIES_FIELD_NAME, false);

        BasicDBList list = (BasicDBList) dbObject.get(FIELDS_FIELD_NAME);
        ArrayList<RepositoryFieldInfoBO> fieldList = new ArrayList<RepositoryFieldInfoBO>();
        if (list != null && list.size() > 0)
        {
            for (Object item : list)
            {
                RepositoryFieldInfoBO field = new RepositoryFieldInfoBO((DBObject) item);
                fieldList.add(field);
            }
        }
        _fields = fieldList;
        loadFieldDataTypeProperties();

        return;
    }

    /**
     * Puts our properties into the mongoDB object so that it can be saved
     * 
     * @param dbObject
     *            mongoDB database object that can be used for saving
     * @throws ChiliLogException
     */
    @Override
    protected void savePropertiesToDBObject(DBObject dbObject) throws ChiliLogException
    {
        loadFieldDataTypeProperties();

        MongoUtils.setString(dbObject, NAME_FIELD_NAME, _name);
        MongoUtils.setString(dbObject, DISPLAY_NAME_FIELD_NAME, _displayName);
        MongoUtils.setString(dbObject, DESCRIPTION_FIELD_NAME, _description);
        MongoUtils.setString(dbObject, CONTROLLER_CLASS_NAME_FIELD_NAME, _controllerClassName);
        MongoUtils.setString(dbObject, STARTUP_STATUS_FIELD_NAME, _startupStatus.toString());

        MongoUtils.setBoolean(dbObject, DURABLE_READ_QUEUE_FIELD_NAME, _readQueueDurable);

        MongoUtils.setBoolean(dbObject, WRITE_QUEUE_DURABLE_FIELD_NAME, _writeQueueDurable);
        MongoUtils.setLong(dbObject, WRITE_QUEUE_WORKER_COUNT_FIELD_NAME, _writeQueueWorkerCount);
        MongoUtils.setLong(dbObject, WRITE_QUEUE_MAX_MEMORY_FIELD_NAME, _writeQueueMaxMemory);
        MongoUtils.setString(dbObject, WRITE_QUEUE_MAX_MEMORY_POLICY_FIELD_NAME, _writeQueueMaxMemoryPolicy.toString());
        MongoUtils.setLong(dbObject, WRITE_QUEUE_PAGE_SIZE_FIELD_NAME, _writeQueuePageSize);

        MongoUtils.setString(dbObject, PARSE_FIELD_ERROR_HANDLING_FIELD_NAME, _parseFieldErrorHandling.toString());
        MongoUtils.setKeyValuePairs(dbObject, PROPERTIES_FIELD_NAME, _properties);

        ArrayList<DBObject> fieldList = new ArrayList<DBObject>();
        for (RepositoryFieldInfoBO field : _fields)
        {
            BasicDBObject obj = new BasicDBObject();
            field.savePropertiesToDBObject(obj);
            fieldList.add(obj);
        }
        dbObject.put(FIELDS_FIELD_NAME, fieldList);
    }

    /**
     * Load properties for our fields before parsing
     * 
     * @throws ChiliLogException
     */
    public void loadFieldDataTypeProperties() throws ChiliLogException
    {
        for (RepositoryFieldInfoBO f : _fields)
        {
            f.loadDataTypeProperties();
        }
    }

    /**
     * <p>
     * Returns the unique name for this repository. This name forms part the mongoDB collection for storing data for
     * this repository.
     * </p>
     * <p>
     * If the name is "xxx", then the mongoDB collection name is "xxx_repository"
     * </p>
     */
    public String getName()
    {
        return _name;
    }

    public void setName(String name)
    {
        _name = name;
    }

    /**
     * Returns the name of the collection in mongoDB where repository entries will be stored.
     */
    public String getMongoDBCollectionName()
    {
        return String.format("%s_repository", _name);
    }

    /**
     * Returns user friendly display name for this repository
     */
    public String getDisplayName()
    {
        return _displayName;
    }

    public void setDisplayName(String displayName)
    {
        _displayName = displayName;
    }

    /**
     * Returns the description for this repository
     */
    public String getDescription()
    {
        return _description;
    }

    public void setDescription(String description)
    {
        _description = description;
    }

    /**
     * Returns the full class name of the class that reads and writes log entries for this repository
     */
    public String getControllerClassName()
    {
        return _controllerClassName;
    }

    public void setControllerClassName(String writeClassName)
    {
        _controllerClassName = writeClassName;
    }

    /**
     * Returns the status of the repository
     */
    public Status getStartupStatus()
    {
        return _startupStatus;
    }

    public void setStartupStatus(Status status)
    {
        _startupStatus = status;
    }

    /**
     * Returns a list fields that is to be parsed and stored in this repository
     */
    public ArrayList<RepositoryFieldInfoBO> getFields()
    {
        return _fields;
    }

    /**
     * Returns a list of parser specific properties for this repository
     */
    public Hashtable<String, String> getProperties()
    {
        return _properties;
    }

    /**
     * Returns the error handling technique to use when parsing a field
     */
    public ParseFieldErrorHandling getParseFieldErrorHandling()
    {
        return _parseFieldErrorHandling;
    }

    public void setParseFieldErrorHandling(ParseFieldErrorHandling parseFieldErrorHandling)
    {
        _parseFieldErrorHandling = parseFieldErrorHandling;
    }

    /**
     * Returns the address to use for the message queue that handles dead letters
     */
    public String getDeadLetterAddress()
    {
        return String.format("repository.%s.dead_letters", _name);
    }

    /**
     * Returns the address to use for the message queue that handles incoming entries
     */
    public String getWriteQueueAddress()
    {
        return String.format("repository.%s.write", _name);
    }

    /**
     * Returns the name of the role in which a user must be a member before permission is granted to write (produce)
     * incoming entries to this repository
     */
    public String getWriteQueueRole()
    {
        return String.format("repository.%s.writer", _name);
    }

    /**
     * Returns the address to use for the message queue that handles outgoing entries
     */
    public String getReadQueueAddress()
    {
        return String.format("repository.%s.read", _name);
    }

    /**
     * Returns the name of the role in which a user must be a member before permission is granted to read (consume)
     * outgoing entries to this repository
     */
    public String getReadQueueRole()
    {
        return String.format("repository.%s.reader", _name);
    }

    /**
     * Returns a flag indicating if the read queue for this repository is to be durable. For this to take effect, the
     * app.properties mq.persistence_enabled must also be set to true.
     */
    public boolean isReadQueueDurable()
    {
        return _readQueueDurable;
    }

    public void setReadQueueDurable(boolean durableReadQueue)
    {
        _readQueueDurable = durableReadQueue;
    }

    /**
     * Returns a flag indicating if the write queue for this repository is to be durable. For this to take effect, the
     * app.properties mq.persistence_enabled must also be set to true.
     */
    public boolean isWriteQueueDurable()
    {
        return _writeQueueDurable;
    }

    public void setWriteQueueDurable(boolean durableWriteQueue)
    {
        _writeQueueDurable = durableWriteQueue;
    }

    /**
     * Returns the number of writer worker threads that will be created to processing incoming entries.
     */
    public long getWriteQueueWorkerCount()
    {
        return _writeQueueWorkerCount;
    }

    public void setWriteQueueWorkerCount(long writeWorkerCount)
    {
        _writeQueueWorkerCount = writeWorkerCount;
    }

    /**
     * The maximum amount of memory (in bytes) that will be used by this queue. <code>-1</code> means no limit.
     */
    public long getWriteQueueMaxMemory()
    {
        return _writeQueueMaxMemory;
    }

    public void setWriteQueueMaxMemory(long writeQueueMaxMemory)
    {
        _writeQueueMaxMemory = writeQueueMaxMemory;
    }

    /**
     * Determines what happens when WriteQueueMaxMemory is reached.
     */
    public QueueMaxMemoryPolicy getWriteQueueMaxMemoryPolicy()
    {
        return _writeQueueMaxMemoryPolicy;
    }

    public void setWriteQueueMaxMemoryPolicy(QueueMaxMemoryPolicy writeQueueMaxMemoryPolicy)
    {
        _writeQueueMaxMemoryPolicy = writeQueueMaxMemoryPolicy;
    }

    /**
     * If WriteQueueMaxMemoryPolicy is set to PAGE, then this value determines the size of each page file on the hard
     * disk in bytes.
     */
    public long getWriteQueuePageSize()
    {
        return _writeQueuePageSize;
    }

    public void setWriteQueuePageSize(long writeQueuePageSize)
    {
        _writeQueuePageSize = writeQueuePageSize;
    }

    /**
     * Technique to use if there is an error during parsing a field in a repository entry
     */
    public enum ParseFieldErrorHandling
    {
        /**
         * The field will not be written as part of the log entry in the repository and n log entry will be written to
         * ChiliLog
         */
        SkipFieldIgnoreError,

        /**
         * The field will not be written as part of the log entry in the repository and a log entry will be written to
         * ChiliLog
         */
        SkipField,

        /**
         * The entire log entry will not be written to the repository and a log entry will be written to ChiliLog
         */
        SkipEntry,

    }

    /**
     * Repository status
     * 
     * @author vibul
     * 
     */
    public enum Status
    {
        /**
         * Users with permission can read from and write to this repository
         */
        ONLINE,

        /**
         * Nobody can read from or write to this repository
         */
        OFFLINE
    }

    /**
     * Policy to follow once a queue's max memory is reached
     * 
     * @author vibul
     * 
     */
    public enum QueueMaxMemoryPolicy
    {
        /**
         * Messages will be pushed to page files on the hard disk
         */
        PAGE,

        /**
         * Old messages will be dropped
         */
        DROP,

        /**
         * Force producers to block and wait before new messages can be sent
         */
        BLOCK
    }
}
