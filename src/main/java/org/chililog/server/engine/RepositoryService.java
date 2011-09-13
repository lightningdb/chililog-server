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

import org.bson.types.ObjectId;
import org.chililog.server.common.ChiliLogException;
import org.chililog.server.common.Log4JLogger;
import org.chililog.server.data.MongoConnection;
import org.chililog.server.data.RepositoryConfigBO;
import org.chililog.server.data.RepositoryConfigController;
import org.chililog.server.data.RepositoryConfigListCriteria;
import org.chililog.server.data.RepositoryConfigBO.Status;

import com.mongodb.DB;

/**
 * <p>
 * The RepositoryService is responsible managing for start/stop/reload all repositories.
 * </p>
 * 
 * <pre>
 * // Start all repositories
 * RepositoryManager.getInstance().start();
 * 
 * // Stop all repositories
 * RepositoryManager.getInstance().stop();
 * </pre>
 * 
 * <p>
 * It is assumed that mongoDB and HornetQ (via {@link MqService}) has been started.
 * </p>
 * 
 * @author vibul
 * 
 */
public class RepositoryService
{
    static Log4JLogger _logger = Log4JLogger.getLogger(RepositoryService.class);

    private ArrayList<Repository> _onlineRepositories = new ArrayList<Repository>();

    /**
     * Returns the singleton instance for this class
     */
    public static RepositoryService getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    /**
     * SingletonHolder is loaded on the first execution of Singleton.getInstance() or the first access to
     * SingletonHolder.INSTANCE, not before.
     * 
     * @see http://en.wikipedia.org/wiki/Singleton_pattern
     */
    private static class SingletonHolder
    {
        public static final RepositoryService INSTANCE = new RepositoryService();
    }

    /**
     * <p>
     * Singleton constructor
     * </p>
     */
    private RepositoryService()
    {
        return;
    }

    /**
     * <p>
     * Starts this service by starting all repositories where the startup status is ONLINE
     * </p>
     * <p>
     * Should be called once at the start of the application
     * </p>
     */
    public synchronized void start() throws ChiliLogException
    {
        ArrayList<RepositoryConfigBO> repoConfigList = this.getRepositoryConfigList();
        for (RepositoryConfigBO repoConfig : repoConfigList)
        {
            if (repoConfig.getStartupStatus() == Status.ONLINE)
            {
                startRepository(repoConfig);
            }
        }
        return;
    }

    /**
     * <p>
     * Starts all repositories
     * </p>
     */
    public synchronized void startAllRepositories() throws ChiliLogException
    {
        ArrayList<RepositoryConfigBO> repoList = this.getRepositoryConfigList();
        for (RepositoryConfigBO repoConfig : repoList)
        {
            // If not started, then start it
            Repository repo = getOnlineRepository(repoConfig.getDocumentID());
            if (repo == null)
            {
                startRepository(repoConfig);
            }
        }
        return;
    }

    /**
     * Starts the specified repository. Once started, a repository becomes "online". Log entries will be streamed and
     * saved (if configured to do so).
     * 
     * @param repositoryInfoDocumentID
     *            Document ID of the repository info to start
     * @return Repository runtime information
     * @throws ChiliLogException
     */
    public synchronized Repository startRepository(ObjectId repositoryInfoDocumentID) throws ChiliLogException
    {
        Repository repo = this.getOnlineRepository(repositoryInfoDocumentID);
        if (repo != null)
        {
            // Repository already started
            return repo;
        }

        DB db = MongoConnection.getInstance().getConnection();
        RepositoryConfigBO repoInfo = RepositoryConfigController.getInstance().get(db, repositoryInfoDocumentID);
        return startRepository(repoInfo);
    }

    /**
     * Starts the specified repository
     * 
     * @param repoInfo
     *            Meta data of repository to start
     * @return Repository runtime information
     * @throws ChiliLogException
     */
    private Repository startRepository(RepositoryConfigBO repoInfo) throws ChiliLogException
    {
        Repository repo = new Repository(repoInfo);
        repo.start();
        _onlineRepositories.add(repo);
        return repo;
    }

    /**
     * Stops this service
     * 
     * @throws Exception
     */
    public synchronized void stop() throws Exception
    {
        stopAllRepositories();
        return;
    }

    /**
     * <p>
     * Starts all repositories but not this service
     * </p>
     */
    public synchronized void stopAllRepositories() throws ChiliLogException
    {
        for (Repository repo : _onlineRepositories)
        {
            repo.stop();
        }
        _onlineRepositories.clear();
        return;
    }

    /**
     * Stops the specified repository. Streaming of log entries will be disabled and new log entries will not be stored.
     * 
     * @param repositoryInfoDocumentID
     *            Document ID of the repository to stop
     * @throws ChiliLogException
     */
    public synchronized void stopRepository(ObjectId repositoryInfoDocumentID) throws ChiliLogException
    {
        Repository repo = getOnlineRepository(repositoryInfoDocumentID);
        if (repo != null)
        {
            stopRepository(repo);
        }
    }

    /**
     * Stops the specified repository.
     * 
     * @param repo
     *            Runtime information of repository to stop
     * @throws ChiliLogException
     */
    private synchronized void stopRepository(Repository repo) throws ChiliLogException
    {
        repo.stop();
        _onlineRepositories.remove(repo);
        return;
    }

    /**
     * Finds an online repository with a matching repository configuration document id
     * 
     * @param repositoryConfigDocumentID
     *            ID of repository configuration document of repository to find 
     * @return Matching repository. <code>null</code> if not found
     */
    private Repository getOnlineRepository(ObjectId repositoryConfigDocumentID)
    {
        for (Repository repo : _onlineRepositories)
        {
            if (repo.getRepoConfig().getDocumentID().equals(repositoryConfigDocumentID))
            {
                return repo;
            }
        }

        return null;
    }

    /**
     * <p>
     * Returns a list of repository configuration
     * </p>
     * 
     * @throws ChiliLogException
     */
    private ArrayList<RepositoryConfigBO> getRepositoryConfigList() throws ChiliLogException
    {
        // Get count connections
        DB db = MongoConnection.getInstance().getConnection();

        // Get list of repositories
        RepositoryConfigListCriteria criteria = new RepositoryConfigListCriteria();
        ArrayList<RepositoryConfigBO> list = RepositoryConfigController.getInstance().getList(db, criteria);
        return list;
    }

    /**
     * Returns the latest runtime information for the specified repository info id.
     * 
     * @param id
     *            Repository config document id of the repository to retrieve
     * @return Matching repository.
     * @throws ChiliLogException
     *             - if error or not found
     */
    public synchronized Repository getRepository(ObjectId id) throws ChiliLogException
    {
        // Check it see if repository is online
        for (Repository repo : _onlineRepositories)
        {
            if (repo.getRepoConfig().getDocumentID().equals(id))
            {
                return repo;
            }
        }
       
        // Get count connections
        DB db = MongoConnection.getInstance().getConnection();
        RepositoryConfigBO repoConfig = RepositoryConfigController.getInstance().get(db, id);
        return repoConfig == null ? null : new Repository(repoConfig);
    }

    /**
     * Returns the latest runtime information for all repositories
     * 
     * @throws ChiliLogException
     */
    public synchronized Repository[] getRepositories() throws ChiliLogException
    {
        ArrayList<Repository> list = new ArrayList<Repository>();
        ArrayList<RepositoryConfigBO> repoConfigList = this.getRepositoryConfigList();
        for (RepositoryConfigBO repoConfig : repoConfigList)
        {
            Repository repo = getOnlineRepository(repoConfig.getDocumentID());
            if (repo == null)
            {
                list.add(new Repository(repoConfig));
            }
            else
            {
                // Online so send that one
                list.add(repo);
            }
        }

        return list.toArray(new Repository[] {});
    }
}
