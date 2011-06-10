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

import java.util.ArrayList;

import org.apache.commons.lang.NullArgumentException;

import com.chililog.server.common.AppProperties;
import com.chililog.server.common.ChiliLogException;
import com.chililog.server.common.Log4JLogger;
import com.chililog.server.data.RepositoryEntryController;
import com.chililog.server.data.RepositoryEntryBO;
import com.chililog.server.data.RepositoryInfoBO;
import com.chililog.server.data.RepositoryInfoBO.Status;

/**
 * <p>
 * From a logical view point, a repository is a "bucket" within ChiliLog where entries from one or more logs are be
 * stored.
 * </p>
 * <p>
 * From a software view point:
 * </p>
 * <ul>
 * <li>a repository's specification or meta-data is represented by {@link RepositoryInfoBO}.</li>
 * <li>an individual repository entry (a line in a log file) is represented by {@link RepositoryEntryBO} and is stored
 * in mongoDB as a record in a collection.</li>
 * <li>applications communicate with repositories via message queues. Log entries can be deposited in a queue for
 * processing.</li>
 * <li>The worker threads, {@link RepositoryStorageWorker}, reads the queued log entries and writes them to mongoDB using
 * {@link RepositoryEntryController} classes. The exact type of controller is specified as part of the repository
 * definition in {@link RepositoryInfoBO}.</li>
 * </ul>
 * 
 * @author vibul
 * 
 */
public class Repository
{
    static Log4JLogger _logger = Log4JLogger.getLogger(Repository.class);
    private RepositoryInfoBO _repoInfo;
    private ArrayList<RepositoryStorageWorker> _storageWorkers = new ArrayList<RepositoryStorageWorker>();
    private Status _status;

    /**
     * Constructor specifying the information needed to create a repository
     * 
     * @param repoInfo
     *            Repository meta data
     */
    public Repository(RepositoryInfoBO repoInfo)
    {
        if (repoInfo == null)
        {
            throw new NullArgumentException("repoInfo cannot be null");
        }

        _repoInfo = repoInfo;
        _status = Status.OFFLINE;
        return;
    }

    /**
     * Returns the meta data about this repository
     */
    public RepositoryInfoBO getRepoInfo()
    {
        return _repoInfo;
    }

    /**
     * Updates the repository information
     * 
     * @param repoInfo
     *            new repository information
     * @throws ChiliLogException
     */
    public void setRepoInfo(RepositoryInfoBO repoInfo) throws ChiliLogException
    {
        if (repoInfo == null)
        {
            throw new NullArgumentException("repoInfo cannot be null");
        }
        if (_status != Status.OFFLINE)
        {
            throw new ChiliLogException(Strings.REPOSITORY_INFO_UPDATE_ERROR, _repoInfo.getName());
        }

        _repoInfo = repoInfo;
    }

    /**
     * <p>
     * Starts this repository.
     * </p>
     * <p>
     * The message queues are created and activated. Worker threads are created.
     * </p>
     */
    public synchronized void start() throws ChiliLogException
    {
        if (_status == Status.ONLINE)
        {
            throw new ChiliLogException(Strings.REPOSITORY_ALREADY_STARTED_ERROR, _repoInfo.getName());
        }

        setupHornetQAddress();
        startStorage();
        _status = Status.ONLINE;
    }

    /**
     * Set security upon the pub/sub address
     */
    public void setupHornetQAddress() throws ChiliLogException
    {
        try
        {
            MqManager mqManager = MqManager.getInstance();
            AppProperties appProperties = AppProperties.getInstance();

            // Security
            mqManager.addSecuritySettings(_repoInfo.getPubSubAddress(), _repoInfo.getPublisherRoleName(),
                    _repoInfo.getSubscriberRoleName());

            // Setup queue properties. See
            // http://hornetq.sourceforge.net/docs/hornetq-2.1.2.Final/user-manual/en/html_single/index.html#queue-attributes.address-settings
            mqManager
                    .getNativeServer()
                    .getHornetQServerControl()
                    .addAddressSettings(_repoInfo.getStorageQueueName(), appProperties.getMqDeadLetterAddress(), null,
                            false, appProperties.getMqRedeliveryMaxAttempts(), _repoInfo.getMaxMemory(),
                            (int) _repoInfo.getPageSize(), (int) _repoInfo.getPageCountCache(),
                            appProperties.getMqRedeliveryDelayMilliseconds(), -1, true,
                            _repoInfo.getMaxMemoryPolicy().toString());
        }
        catch (Exception ex)
        {
            throw new ChiliLogException(ex, Strings.START_REPOSITORY_STORAGE_QUEUE_ERROR, _repoInfo.getPubSubAddress(),
                    _repoInfo.getName(), ex.getMessage());
        }
    }

    /**
     * Start storage queue and workers (if required)
     */
    public void startStorage() throws ChiliLogException
    {
        try
        {
            if (!_repoInfo.getStoreEntriesIndicator())
            {
                return;
            }

            // Create queue
            MqManager mqManager = MqManager.getInstance();
            mqManager.deployQueue(_repoInfo.getPubSubAddress(), _repoInfo.getStorageQueueName(),
                    _repoInfo.getStorageQueueDurableIndicator());

            startStorageWorkers();

        }
        catch (Exception ex)
        {
            throw new ChiliLogException(ex, Strings.START_REPOSITORY_STORAGE_QUEUE_ERROR, _repoInfo.getPubSubAddress(),
                    _repoInfo.getName(), ex.getMessage());
        }
    }

    /**
     * Start storage worker threads
     * 
     * @throws ChiliLogException
     */
    public void startStorageWorkers() throws ChiliLogException
    {
        // Make sure existing worker threads are stopped
        stopStorageWorkers();

        // Add workers to list
        try
        {
            for (int i = 1; i <= _repoInfo.getStorageQueueWorkerCount(); i++)
            {
                String name = String.format("%s StorageWorker #%s", _repoInfo.getName(), i);
                RepositoryStorageWorker worker = new RepositoryStorageWorker(name, this);
                worker.start();
                _storageWorkers.add(worker);
            }
        }
        catch (Exception ex)
        {
            throw new ChiliLogException(ex, Strings.START_REPOSITORY_STORAGE_WORKER_ERROR, _repoInfo.getName(),
                    ex.getMessage());
        }
    }

    /**
     * <p>
     * Stops this repository.
     * </p>
     * <p>
     * We really just stop the workers. No need to delete the queue unless changes are made and the repository is
     * started again (see <code>MqManager.deployQueue()</code>). In this way, unprocessed messages are kept in the
     * queue.
     * </p>
     * 
     * @throws InterruptedException
     */
    public synchronized void stop() throws ChiliLogException, InterruptedException
    {
        stopStorageWorkers();

        _status = Status.OFFLINE;
    }

    /**
     * Start writer threads
     */
    public void stopStorageWorkers() throws ChiliLogException
    {
        try
        {
            while (_storageWorkers.size() > 0)
            {
                RepositoryStorageWorker worker = _storageWorkers.get(0);
                worker.stopRunning();
                _storageWorkers.remove(0);
            }
        }
        catch (Exception ex)
        {
            throw new ChiliLogException(ex, Strings.STOP_REPOSITORY_STORAGE_WORKER_ERROR, _repoInfo.getName(),
                    ex.getMessage());
        }
    }

    /**
     * Returns flag to indicate if this repository has started or not
     */
    public synchronized Status getStatus()
    {
        return _status;
    }

    /**
     * Returns the array of writer threads. This method should only be used for our unit testing!
     */
    ArrayList<RepositoryStorageWorker> getWriters()
    {
        return _storageWorkers;
    }

    /**
     * Make sure we stop
     */
    protected void finalize() throws Throwable
    {
        try
        {
            stop();
        }
        finally
        {
            super.finalize();
        }
    }
}
