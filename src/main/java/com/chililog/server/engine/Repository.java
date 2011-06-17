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
 * <li>The worker threads, {@link RepositoryStorageWorker}, reads the queued log entries and writes them to mongoDB
 * using {@link RepositoryEntryController} classes. The exact type of controller is specified as part of the repository
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
     * Starts this repository. Log entries can be produced and consumed.
     * </p>
     */
    public synchronized void start() throws ChiliLogException
    {
        if (_status == Status.ONLINE)
        {
            throw new ChiliLogException(Strings.REPOSITORY_ALREADY_STARTED_ERROR, _repoInfo.getName());
        }

        try
        {
            _logger.info("Starting Repository '%s'", _repoInfo.getName());

            MqManager mqManager = MqManager.getInstance();
            AppProperties appProperties = AppProperties.getInstance();

            // Setup permissions
            StringBuilder publisherRoles = new StringBuilder();
            publisherRoles.append(_repoInfo.getAdministratorRoleName()).append(",");
            publisherRoles.append(_repoInfo.getPublisherRoleName());

            StringBuilder subscriberRoles = new StringBuilder();
            subscriberRoles.append(_repoInfo.getAdministratorRoleName()).append(",");
            subscriberRoles.append(_repoInfo.getWorkbenchRoleName()).append(",");
            subscriberRoles.append(_repoInfo.getSubscriberRoleName());

            mqManager.addSecuritySettings(_repoInfo.getPubSubAddress(), publisherRoles.toString(),
                    subscriberRoles.toString());

            // Update address properties. See
            // http://hornetq.sourceforge.net/docs/hornetq-2.1.2.Final/user-manual/en/html_single/index.html#queue-attributes.address-settings
            mqManager.addAddressSettings(_repoInfo.getPubSubAddress(), appProperties.getMqDeadLetterAddress(), null,
                    false, appProperties.getMqRedeliveryMaxAttempts(), _repoInfo.getMaxMemory(), (int) _repoInfo
                            .getPageSize(), (int) _repoInfo.getPageCountCache(), appProperties
                            .getMqRedeliveryDelayMilliseconds(), -1, true, _repoInfo.getMaxMemoryPolicy().toString());

            // If we want to store entries, then start queue on the address and workers to consume and write entries
            if (_repoInfo.getStoreEntriesIndicator())
            {
                // Create queue
                mqManager.deployQueue(_repoInfo.getPubSubAddress(), _repoInfo.getStorageQueueName(),
                        _repoInfo.getStorageQueueDurableIndicator());

                // Start our storage workers
                startStorageWorkers();
            }

            // Finish
            _status = Status.ONLINE;
            _logger.info("Repository '%s' started.", _repoInfo.getName());
            return;
        }
        catch (Exception ex)
        {
            throw new ChiliLogException(ex, Strings.START_REPOSITORY_ERROR, _repoInfo.getPubSubAddress(),
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
     * Stops this repository. Log entries cannot be produced or consumed.
     * </p>
     */
    public synchronized void stop() throws ChiliLogException
    {
        try
        {
            _logger.info("Stopping Repository '%s'", _repoInfo.getName());

            MqManager mqManager = MqManager.getInstance();

            // Remove permissions so that nobody
            mqManager.addSecuritySettings(_repoInfo.getPubSubAddress(), null, null);

            // Stop workers
            stopStorageWorkers();

            // Disconnect remote clients

            // Finish
            _status = Status.OFFLINE;
            _logger.info("Repository '%s' stopped.", _repoInfo.getName());
            return;
        }
        catch (Exception ex)
        {
            throw new ChiliLogException(ex, Strings.STOP_REPOSITORY_ERROR, _repoInfo.getPubSubAddress(),
                    _repoInfo.getName(), ex.getMessage());
        }
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
     * Returns the array of storage worker threads. This method should only be used for our unit testing!
     */
    ArrayList<RepositoryStorageWorker> getStorageWorkers()
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
