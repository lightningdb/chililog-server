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
import java.util.regex.Pattern;

import com.chililog.server.common.ChiliLogException;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * <p>
 * This class contains information that describes a repository:
 * <ul>
 * <li>General Details - Name and status</li>
 * <li>Security - authentication information for publisher and subscribers of the repository</li>
 * <li>Storage - how to store log entries in the database</li>
 * <li>Parsers - how information is to be extracted from log entries and stored in the database</li>
 * </ul>
 * </p>
 * <p>
 * HornetQ/Repository/JMS PubSub concepts:
 * <ul>
 * <li>A repository maps to a HornetQ address. A HornetQ address is the same as a JMS Topic.</li>
 * <li>Publishers send or produce log entries to a repository (HornetQ address)</li>
 * <li>Subscribers consume messages from the repository (HornetQ address)</li>
 * <li>Each subscriber is issued with its own HornetQ queue that is bound to the repository's HornetQ address.</li>
 * </ul>
 * </p>
 * <p>
 * Note issue with slow consumers in a pub/sub model.Messages are stored once in the address and references passed into
 * queues bound to that address. If there is a slow queue, un-consumed messages will not be cleared from memory.
 * http://docs.jboss.org/hornetq/2.2.2.Final/user-manual/en/html_single/index.html#d0e5059
 * </p>
 * <p>
 * Paging for a HornetQ address. See
 * http://docs.jboss.org/hornetq/2.2.2.Final/user-manual/en/html_single/index.html#paging.
 * </p>
 * <p>
 * Journal is also implemented individually for an address. See
 * http://docs.jboss.org/hornetq/2.2.2.Final/user-manual/en/html_single/index.html#persistence.
 * </p>
 * 
 * @author vibul
 * 
 */
public class RepositoryInfoBO extends BO implements Serializable
{
    private static final long serialVersionUID = 1L;

    private static Pattern _namePattern = Pattern.compile("^[a-z0-9_]+$");

    private String _name;
    private String _displayName;
    private String _description;
    private Status _startupStatus = Status.ONLINE;

    private boolean _storeEntriesIndicator = false;
    private boolean _storageQueueDurableIndicator = false;
    private long _storageQueueWorkerCount = 1;
    private long _storageMaxKeywords = UNLIMITED_MAX_KEYWORDS;

    private long _maxMemory = 1024 * 1024 * 20; // 20 MB
    private MaxMemoryPolicy _maxMemoryPolicy = MaxMemoryPolicy.PAGE;
    private long _pageSize = 1024 * 1024 * 10; // 10 MB
    private long _pageCountCache = 3; // max 3 pages in memory when paging

    private ArrayList<RepositoryParserInfoBO> _parsers = new ArrayList<RepositoryParserInfoBO>();

    static final String NAME_FIELD_NAME = "name";
    static final String DISPLAY_NAME_FIELD_NAME = "display_name";
    static final String DESCRIPTION_FIELD_NAME = "description";
    static final String STARTUP_STATUS_FIELD_NAME = "startup_status";

    static final String STORE_ENTRIES_INDICATOR_FIELD_NAME = "store_entries_indicator";
    static final String STORAGE_QUEUE_DURABLE_INDICATOR_FIELD_NAME = "storage_queue_durable_indicator";
    static final String STORAGE_QUEUE_WORKER_COUNT_FIELD_NAME = "storage_queue_worker_count";
    static final String STROAGE_MAX_KEYWORDS = "storage_max_keywords";
    public static final long UNLIMITED_MAX_KEYWORDS = -1;

    static final String MAX_MEMORY_FIELD_NAME = "max_memory";
    static final String MAX_MEMORY_POLICY_FIELD_NAME = "max_memory_policy";
    static final String PAGE_SIZE_FIELD_NAME = "page_size";
    static final String PAGE_COUNT_CACHE_FIELD_NAME = "page_count_cache";

    static final String PARSERS_FIELD_NAME = "parsers";

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

        // General
        _name = MongoUtils.getString(dbObject, NAME_FIELD_NAME, true);
        _displayName = MongoUtils.getString(dbObject, DISPLAY_NAME_FIELD_NAME, false);
        _description = MongoUtils.getString(dbObject, DESCRIPTION_FIELD_NAME, false);
        _startupStatus = Status.valueOf(MongoUtils.getString(dbObject, STARTUP_STATUS_FIELD_NAME, true));

        // PubSub settings
        _maxMemory = MongoUtils.getLong(dbObject, MAX_MEMORY_FIELD_NAME, true);
        _maxMemoryPolicy = MaxMemoryPolicy.valueOf(MongoUtils.getString(dbObject, MAX_MEMORY_POLICY_FIELD_NAME, true));
        _pageSize = MongoUtils.getLong(dbObject, PAGE_SIZE_FIELD_NAME, true);
        _pageCountCache = MongoUtils.getLong(dbObject, PAGE_COUNT_CACHE_FIELD_NAME, true);

        // Storage
        _storeEntriesIndicator = MongoUtils.getBoolean(dbObject, STORE_ENTRIES_INDICATOR_FIELD_NAME, true);
        _storageQueueDurableIndicator = MongoUtils.getBoolean(dbObject, STORAGE_QUEUE_DURABLE_INDICATOR_FIELD_NAME,
                true);
        _storageQueueWorkerCount = MongoUtils.getLong(dbObject, STORAGE_QUEUE_WORKER_COUNT_FIELD_NAME, true);
        _storageMaxKeywords = MongoUtils.getLong(dbObject, STROAGE_MAX_KEYWORDS, true);

        // Parser
        BasicDBList list = (BasicDBList) dbObject.get(PARSERS_FIELD_NAME);
        ArrayList<RepositoryParserInfoBO> parserList = new ArrayList<RepositoryParserInfoBO>();
        if (list != null && list.size() > 0)
        {
            for (Object item : list)
            {
                RepositoryParserInfoBO field = new RepositoryParserInfoBO((DBObject) item);
                parserList.add(field);
            }
        }
        _parsers = parserList;

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
        MongoUtils.setString(dbObject, NAME_FIELD_NAME, _name, true);

        // Check name format
        if (!_namePattern.matcher(_name).matches())
        {
            throw new ChiliLogException(Strings.REPO_INFO_NAME_FORMAT_ERROR, _name);
        }

        // Check that page file size is less than max memory
        if (_pageSize > _maxMemory)
        {
            throw new ChiliLogException(Strings.REPO_INFO_PAGE_FILE_SIZE_ERROR, _pageSize, _maxMemory);
        }

        // General
        MongoUtils.setString(dbObject, DISPLAY_NAME_FIELD_NAME, _displayName, false);
        MongoUtils.setString(dbObject, DESCRIPTION_FIELD_NAME, _description, false);
        MongoUtils.setString(dbObject, STARTUP_STATUS_FIELD_NAME, _startupStatus.toString(), true);

        // PubSub settings
        MongoUtils.setLong(dbObject, MAX_MEMORY_FIELD_NAME, _maxMemory, true);
        MongoUtils.setString(dbObject, MAX_MEMORY_POLICY_FIELD_NAME, _maxMemoryPolicy.toString(), true);
        MongoUtils.setLong(dbObject, PAGE_SIZE_FIELD_NAME, _pageSize, true);
        MongoUtils.setLong(dbObject, PAGE_COUNT_CACHE_FIELD_NAME, _pageCountCache, true);

        // Storage
        MongoUtils.setBoolean(dbObject, STORE_ENTRIES_INDICATOR_FIELD_NAME, _storeEntriesIndicator, true);
        MongoUtils
                .setBoolean(dbObject, STORAGE_QUEUE_DURABLE_INDICATOR_FIELD_NAME, _storageQueueDurableIndicator, true);
        MongoUtils.setLong(dbObject, STORAGE_QUEUE_WORKER_COUNT_FIELD_NAME, _storageQueueWorkerCount, true);
        MongoUtils.setLong(dbObject, STROAGE_MAX_KEYWORDS, _storageMaxKeywords, true);

        // Parsers
        ArrayList<DBObject> fieldList = new ArrayList<DBObject>();
        for (RepositoryParserInfoBO parser : _parsers)
        {
            BasicDBObject obj = new BasicDBObject();
            parser.savePropertiesToDBObject(obj);
            fieldList.add(obj);
        }
        dbObject.put(PARSERS_FIELD_NAME, fieldList);
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
        return String.format("repo_%s", _name);
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
    public ArrayList<RepositoryParserInfoBO> getParsers()
    {
        return _parsers;
    }

    /**
     * Returns the address for publishers to use for sending in log entries
     */
    public String getPubSubAddress()
    {
        return String.format("repo.%s", _name);
    }

    /**
     * Returns the address of the message queue that used for storing incoming log entries
     */
    public String getStorageQueueName()
    {
        return String.format("repo.%s.storage", _name);
    }

    /**
     * Returns the name of the role that has all access to this repository
     */
    public String getAdministratorRoleName()
    {
        return UserBO.createRepositoryAdministratorRoleName(_name);
    }

    /**
     * Returns the name of the role that can use the workbench to access the data in this repository
     */
    public String getWorkbenchRoleName()
    {
        return UserBO.createRepositoryWorkbenchRoleName(_name);
    }

    /**
     * Returns the name of the role that can publish (write) log entries to this repository
     */
    public String getPublisherRoleName()
    {
        return UserBO.createRepositoryPublisherRoleName(_name);
    }

    /**
     * Returns the name of the role that can subscribe to (read) this repository 
     */
    public String getSubscriberRoleName()
    {
        return UserBO.createRepositorySubscriberRoleName(_name);
    }
   
    /**
     * Returns a flag indicating if the log entries published to this repository is to be stored in the database.
     */
    public boolean getStoreEntriesIndicator()
    {
        return _storeEntriesIndicator;
    }

    public void setStoreEntriesIndicator(boolean storeEntriesIndicator)
    {
        _storeEntriesIndicator = storeEntriesIndicator;
    }

    /**
     * <p>
     * Returns a flag indicating if the storage queue for this repository is to be durable. For this to take effect, the
     * app.properties mq.persistence_enabled must also be set to true.
     * </p>
     * <p>
     * A durable queue saves all entries to hard disk. This means that if the server crashes, queued log entries are not
     * lost. However, storing queued log entries slows performance.
     * </p>
     * <p>
     * By default, storage queues are not durable. We recommend that you only make storage queues durable if the log
     * entries contain critical information that cannot be lost.
     * </p>
     */
    public boolean getStorageQueueDurableIndicator()
    {
        return _storageQueueDurableIndicator;
    }

    public void setStorageQueueDurableIndicator(boolean storageQueueDurableIndicator)
    {
        _storageQueueDurableIndicator = storageQueueDurableIndicator;
    }

    /**
     * <p>
     * Returns the number of writer worker threads that will be created to processing queued incoming log entries. The
     * more workers, the quicker log entries are saved to the database and the smaller the storage queue size.
     * </p>
     * <p>
     * The default is 1.
     * </p>
     */
    public long getStorageQueueWorkerCount()
    {
        return _storageQueueWorkerCount;
    }

    public void setStorageQueueWorkerCount(long writeWorkerCount)
    {
        _storageQueueWorkerCount = writeWorkerCount;
    }

    /**
     * The maximum amount of memory (in bytes) that will be used by the storage queue. <code>-1</code> means no limit.
     */
    public long getMaxMemory()
    {
        return _maxMemory;
    }

    public void setMaxMemory(long maxMemory)
    {
        _maxMemory = maxMemory;
    }

    /**
     * Determines what happens when MaxMemory is reached on the storage queue.
     */
    public MaxMemoryPolicy getMaxMemoryPolicy()
    {
        return _maxMemoryPolicy;
    }

    public void setMaxMemoryPolicy(MaxMemoryPolicy maxMemoryPolicy)
    {
        _maxMemoryPolicy = maxMemoryPolicy;
    }

    /**
     * If MaxMemoryPolicy is set to PAGE, then this value determines the size of each page file on the hard
     * disk in bytes.
     */
    public long getPageSize()
    {
        return _pageSize;
    }

    public void setPageSize(long pageSize)
    {
        _pageSize = pageSize;
    }

    /**
     * If MaxMemoryPolicy is set to PAGE, then this value determines the number of pages to be kept in memory
     * during page navigation.
     */
    public long getPageCountCache()
    {
        return _pageCountCache;
    }

    public void setPageCountCache(long writeQueuePageCountCache)
    {
        _pageCountCache = writeQueuePageCountCache;
    }

    /**
     * Returns the maximum number of keywords to store.
     */
    public long getStorageMaxKeywords()
    {
        return _storageMaxKeywords;
    }

    public void setStorageMaxKeywords(long parserMaxKeywords)
    {
        _storageMaxKeywords = parserMaxKeywords;
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
    public enum MaxMemoryPolicy
    {
        /**
         * Messages will be pushed to page files on the hard disk once queue memory limit is reached.
         */
        PAGE,

        /**
         * New messages will be dropped once queue memory limit is reached.
         */
        DROP,

        /**
         * Force producers to block and wait before new messages can be sent once queue memory limit is reached.
         */
        BLOCK
    }

}
