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

import java.util.ArrayList;

import org.apache.commons.lang.NullArgumentException;
import org.chililog.server.common.AppProperties;
import org.chililog.server.common.ChiliLogException;
import org.chililog.server.common.Log4JLogger;
import org.chililog.server.data.RepositoryEntryBO;
import org.chililog.server.data.RepositoryEntryController;
import org.chililog.server.data.RepositoryConfigBO;
import org.chililog.server.data.RepositoryConfigBO.Status;


/**
 * <p>
 * Runtime information and controller for a repository.
 * </p>
 * <p>
 * From a logical view point, a repository is a "bucket" within ChiliLog where entries from one or more logs are be
 * stored.
 * </p>
 * <p>
 * From a software view point:
 * </p>
 * <ul>
 * <li>a repository's specification or meta-data is represented by {@link RepositoryConfigBO}.</li>
 * <li>an individual repository entry (a line in a log file) is represented by {@link RepositoryEntryBO} and is stored
 * in mongoDB as a record in a collection.</li>
 * <li>applications communicate with repositories via message queues. Log entries can be deposited in a queue for
 * processing.</li>
 * <li>The worker threads, {@link RepositoryStorageWorker}, reads the queued log entries and writes them to mongoDB
 * using {@link RepositoryEntryController} classes. The exact type of controller is specified as part of the repository
 * definition in {@link RepositoryConfigBO}.</li>
 * </ul>
 * 
 * @author vibul
 * 
 */
public class Repository
{
    static Log4JLogger _logger = Log4JLogger.getLogger(Repository.class);
    private RepositoryConfigBO _repoConfig;
    private ArrayList<RepositoryStorageWorker> _storageWorkers = new ArrayList<RepositoryStorageWorker>();
    private Status _status;
    private boolean _hasStarted = false; 

    /**
     * Constructor specifying the information needed to create a repository
     * 
     * @param repoInfo
     *            Repository meta data
     */
    public Repository(RepositoryConfigBO repoInfo)
    {
        if (repoInfo == null)
        {
            throw new NullArgumentException("repoInfo cannot be null");
        }

        _repoConfig = repoInfo;
        _status = Status.OFFLINE;
        return;
    }

    /**
     * Returns the meta data about this repository
     */
    public RepositoryConfigBO getRepoConfig()
    {
        return _repoConfig;
    }

    /**
     * Updates the repository information
     * 
     * @param repoConfig
     *            new repository configuration
     * @throws ChiliLogException
     */
    public void setRepoConfig(RepositoryConfigBO repoConfig) throws ChiliLogException
    {
        if (repoConfig == null)
        {
            throw new NullArgumentException("repoConfig cannot be null");
        }
        if (_status != Status.OFFLINE)
        {
            throw new ChiliLogException(Strings.REPOSITORY_INFO_UPDATE_ERROR, _repoConfig.getName());
        }

        _repoConfig = repoConfig;
    }

    /**
     * <p>
     * Starts this repository. Log entries can be produced and consumed.
     * </p>
     */
    synchronized void start() throws ChiliLogException
    {
        if (_status == Status.ONLINE)
        {
            throw new ChiliLogException(Strings.REPOSITORY_ALREADY_STARTED_ERROR, _repoConfig.getName());
        }
        if (_hasStarted)
        {
            // This should not happen when used via the RepositoryService as intended.
            throw new UnsupportedOperationException("Restarting a repository is not supported. Instance a new Repository and start it instead.");
        }
        
        try
        {
            _logger.info("Starting Repository '%s'", _repoConfig.getName());

            MqService mqManager = MqService.getInstance();
            AppProperties appProperties = AppProperties.getInstance();

            // Setup permissions
            StringBuilder publisherRoles = new StringBuilder();
            publisherRoles.append(_repoConfig.getAdministratorRoleName()).append(",");
            publisherRoles.append(_repoConfig.getPublisherRoleName());

            StringBuilder subscriberRoles = new StringBuilder();
            subscriberRoles.append(_repoConfig.getAdministratorRoleName()).append(",");
            subscriberRoles.append(_repoConfig.getWorkbenchRoleName()).append(",");
            subscriberRoles.append(_repoConfig.getSubscriberRoleName());

            mqManager.addSecuritySettings(_repoConfig.getPubSubAddress(), publisherRoles.toString(),
                    subscriberRoles.toString());

            // Update address properties. See
            // http://hornetq.sourceforge.net/docs/hornetq-2.1.2.Final/user-manual/en/html_single/index.html#queue-attributes.address-settings
            mqManager.addAddressSettings(_repoConfig.getPubSubAddress(), appProperties.getMqDeadLetterAddress(), null,
                    false, appProperties.getMqRedeliveryMaxAttempts(), _repoConfig.getMaxMemory(), (int) _repoConfig
                            .getPageSize(), (int) _repoConfig.getPageCountCache(), appProperties
                            .getMqRedeliveryDelayMilliseconds(), -1, true, _repoConfig.getMaxMemoryPolicy().toString());

            // If we want to store entries, then start queue on the address and workers to consume and write entries
            if (_repoConfig.getStoreEntriesIndicator())
            {
                // Create queue
                mqManager.deployQueue(_repoConfig.getPubSubAddress(), _repoConfig.getStorageQueueName(),
                        _repoConfig.getStorageQueueDurableIndicator());

                // Start our storage workers
                startStorageWorkers();
            }

            // Finish
            _status = Status.ONLINE;
            _logger.info("Repository '%s' started.", _repoConfig.getName());
            _hasStarted = true;
            return;
        }
        catch (Exception ex)
        {
            throw new ChiliLogException(ex, Strings.START_REPOSITORY_ERROR, _repoConfig.getPubSubAddress(),
                    _repoConfig.getName(), ex.getMessage());
        }
    }

    /**
     * Start storage worker threads
     * 
     * @throws ChiliLogException
     */
    void startStorageWorkers() throws ChiliLogException
    {
        // Make sure existing worker threads are stopped
        stopStorageWorkers();

        // Add workers to list
        try
        {
            for (int i = 1; i <= _repoConfig.getStorageQueueWorkerCount(); i++)
            {
                String name = String.format("%s StorageWorker #%s", _repoConfig.getName(), i);
                RepositoryStorageWorker worker = new RepositoryStorageWorker(name, this);
                worker.start();
                _storageWorkers.add(worker);
            }
        }
        catch (Exception ex)
        {
            throw new ChiliLogException(ex, Strings.START_REPOSITORY_STORAGE_WORKER_ERROR, _repoConfig.getName(),
                    ex.getMessage());
        }
    }

    /**
     * <p>
     * Stops this repository. Log entries cannot be produced or consumed.
     * </p>
     */
    synchronized void stop() throws ChiliLogException
    {
        try
        {
            _logger.info("Stopping Repository '%s'", _repoConfig.getName());

            MqService mqManager = MqService.getInstance();

            // Remove permissions so that nobody
            mqManager.addSecuritySettings(_repoConfig.getPubSubAddress(), null, null);

            // Stop workers
            stopStorageWorkers();

            // Disconnect remote clients

            // Finish
            _status = Status.OFFLINE;
            _logger.info("Repository '%s' stopped.", _repoConfig.getName());
            return;
        }
        catch (Exception ex)
        {
            throw new ChiliLogException(ex, Strings.STOP_REPOSITORY_ERROR, _repoConfig.getPubSubAddress(),
                    _repoConfig.getName(), ex.getMessage());
        }
    }

    /**
     * Start writer threads
     */
    void stopStorageWorkers() throws ChiliLogException
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
            throw new ChiliLogException(ex, Strings.STOP_REPOSITORY_STORAGE_WORKER_ERROR, _repoConfig.getName(),
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
