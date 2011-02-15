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
import com.chililog.server.data.RepositoryInfoBO;
import com.chililog.server.data.RepositoryInfoBO.Status;

/**
 * <p>
 * A repository is a collection of message queues and worker threads. The message queues allows to communicate with the
 * outside world while the worker threads does all the processing.
 * </p>
 * 
 * 
 * @author vibul
 * 
 */
public class Repository
{
    static Log4JLogger _logger = Log4JLogger.getLogger(Repository.class);
    private RepositoryInfoBO _repoInfo;
    private ArrayList<RepositoryWriter> _writers = new ArrayList<RepositoryWriter>();
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

        // Bit of a hack but we don't need to start writers for ChiliLog internal log
        // Our internal log4j appender writers directly to mongoDB
        if (_repoInfo.getName().equals(InternalLog4JAppender.REPOSITORY_NAME))
        {
            return;
        }
        
        startQueue();
        startWriters();
        _status = Status.ONLINE;
    }

    /**
     * Start the queue for this repository
     * 
     * @param systemUserClientSession
     *            connection to MQ server
     */
    public void startQueue() throws ChiliLogException
    {
        try
        {
            MqManager mqManager = MqManager.getInstance();
            AppProperties appProperties = AppProperties.getInstance();

            // Setup security
            mqManager.addSecuritySettings(_repoInfo.getWriteQueueAddress(), _repoInfo.getWriteQueueRole(),
                    appProperties.getJaasSystemRole());

            // Setup queue properties. See
            // http://hornetq.sourceforge.net/docs/hornetq-2.1.2.Final/user-manual/en/html_single/index.html#queue-attributes.address-settings
            mqManager
                    .getNativeServer()
                    .getHornetQServerControl()
                    .addAddressSettings(_repoInfo.getWriteQueueAddress(), _repoInfo.getDeadLetterAddress(), null,
                            false, appProperties.getMqRedeliveryMaxAttempts(), _repoInfo.getWriteQueueMaxMemory(),
                            (int) _repoInfo.getWriteQueuePageSize(), appProperties.getMqRedeliveryDelayMilliseconds(),
                            -1, true, _repoInfo.getWriteQueueMaxMemoryPolicy().toString());

            // Create queues
            mqManager.deployQueue(_repoInfo.getWriteQueueAddress(), _repoInfo.getWriteQueueAddress(),
                    _repoInfo.isWriteQueueDurable(), _repoInfo.getDeadLetterAddress());

            mqManager.deployQueue(_repoInfo.getDeadLetterAddress(), _repoInfo.getDeadLetterAddress(),
                    _repoInfo.isWriteQueueDurable(), null);

        }
        catch (Exception ex)
        {
            throw new ChiliLogException(ex, Strings.START_REPOSITORY_QUEUE_ERROR, _repoInfo.getWriteQueueAddress(),
                    _repoInfo.getName(), ex.getMessage());
        }
    }

    /**
     * Start writer threads
     * 
     * @throws ChiliLogException
     */
    public void startWriters() throws ChiliLogException
    {                
        // Make sure existing threads are stopped
        stopWriters();

        // Add writers to list
        try
        {
            for (int i = 1; i <= _repoInfo.getWriteQueueWorkerCount(); i++)
            {
                String name = String.format("%s RepositoryWriter #%s", _repoInfo.getName(), i);
                RepositoryWriter writer = new RepositoryWriter(name, this);
                writer.start();
                _writers.add(writer);
            }
        }
        catch (Exception ex)
        {
            throw new ChiliLogException(ex, Strings.START_REPOSITORY_WRITERS_ERROR, _repoInfo.getName(),
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
        stopWriters();

        _status = Status.OFFLINE;
    }

    /**
     * Start writer threads
     */
    public void stopWriters() throws ChiliLogException
    {
        try
        {
            while (_writers.size() > 0)
            {
                RepositoryWriter writer = _writers.get(0);
                writer.stopRunning();
                _writers.remove(0);
                _logger.debug("Removing '%s' writer", writer.getName());
            }
        }
        catch (Exception ex)
        {
            throw new ChiliLogException(ex, Strings.STOP_REPOSITORY_WRITERS_ERROR, _repoInfo.getName(), ex.getMessage());
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
    ArrayList<RepositoryWriter> getWriters()
    {
        return _writers;
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
