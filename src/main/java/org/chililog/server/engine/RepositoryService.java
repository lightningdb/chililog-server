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
public class RepositoryService {
    static Log4JLogger _logger = Log4JLogger.getLogger(RepositoryService.class);

    /**
     * List of repositories that are readonly or online
     */
    private ArrayList<Repository> _activeRepositories = new ArrayList<Repository>();

    /**
     * Returns the singleton instance for this class
     */
    public static RepositoryService getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * SingletonHolder is loaded on the first execution of Singleton.getInstance() or the first access to
     * SingletonHolder.INSTANCE, not before.
     * 
     * @see http://en.wikipedia.org/wiki/Singleton_pattern
     */
    private static class SingletonHolder {
        public static final RepositoryService INSTANCE = new RepositoryService();
    }

    /**
     * <p>
     * Singleton constructor
     * </p>
     */
    private RepositoryService() {
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
    public synchronized void start() throws ChiliLogException {
        ArrayList<RepositoryConfigBO> repoConfigList = this.getRepositoryConfigList();
        for (RepositoryConfigBO repoConfig : repoConfigList) {
            if (repoConfig.getStartupStatus() == Status.ONLINE) {
                bringRepositoryOnline(repoConfig);
            }
            else if (repoConfig.getStartupStatus() == Status.READONLY) {
                makeRepositoryReadOnly(repoConfig);
            }
        }
        return;
    }

    /**
     * Stops this service
     * 
     * @throws Exception
     */
    public synchronized void stop() throws Exception {
        takeAllRepositoriesOffline();
        return;
    }

    /**
     * <p>
     * Brings all repositories online
     * </p>
     */
    public synchronized void bringAllRepositoriesOnline() throws ChiliLogException {
        ArrayList<RepositoryConfigBO> repoList = this.getRepositoryConfigList();
        for (RepositoryConfigBO repoConfig : repoList) {
            // If offline or readonly, enable it
            Repository repo = getActiveRepository(repoConfig.getDocumentID());
            if (repo == null) {
                bringRepositoryOnline(repoConfig);
            }
            else if (repo.getStatus() == Status.READONLY) {
                // Reload readonly repositories so that it gets the latest definition
                takeRepositoryOffline(repo);
                makeRepositoryReadOnly(repoConfig);
            }
        }
        return;
    }

    /**
     * <p>
     * Make all repositories read only
     * </p>
     */
    public synchronized void makeAllRepositoriesReadonly() throws ChiliLogException {
        ArrayList<RepositoryConfigBO> repoList = this.getRepositoryConfigList();
        for (RepositoryConfigBO repoConfig : repoList) {
            // If offline or readonly, enable it
            Repository repo = getActiveRepository(repoConfig.getDocumentID());
            if (repo == null) {
                makeRepositoryReadOnly(repoConfig);
            }
            else if (repo.getStatus() == Status.ONLINE) {
                repo.makeReadonly();
            }
        }

        return;
    }

    /**
     * <p>
     * Take all repositories offline
     * </p>
     */
    public synchronized void takeAllRepositoriesOffline() throws ChiliLogException {
        for (Repository repo : _activeRepositories) {
            repo.takeOffline();
        }
        _activeRepositories.clear();
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
    public synchronized Repository bringRepositoryOnline(ObjectId repositoryInfoDocumentID) throws ChiliLogException {
        Repository repo = this.getActiveRepository(repositoryInfoDocumentID);
        if (repo != null) {
            if (repo.getStatus() == Status.ONLINE) {
                // Repository is already online
                return repo;
            }
            else if (repo.getStatus() == Status.READONLY) {
                // Remove the repository so we can bring online with the latest definition
                _activeRepositories.remove(repo);
            }
        }

        DB db = MongoConnection.getInstance().getConnection();
        RepositoryConfigBO repoInfo = RepositoryConfigController.getInstance().get(db, repositoryInfoDocumentID);
        return bringRepositoryOnline(repoInfo);
    }

    /**
     * Creates the specified repository and brings it online
     * 
     * @param repoConfig
     *            Meta data of repository to bring online
     * @return Repository runtime information
     * @throws ChiliLogException
     */
    private Repository bringRepositoryOnline(RepositoryConfigBO repoConfig) throws ChiliLogException {
        Repository repo = new Repository(repoConfig);
        repo.bringOnline();
        _activeRepositories.add(repo);
        return repo;
    }

    /**
     * Creates a repository and make it readonly
     * 
     * @param repositoryInfoDocumentID
     *            Document ID of the repository to stop
     * @return Repository runtime information
     * @throws ChiliLogException
     */
    public synchronized Repository makeRepositoryReadOnly(ObjectId repositoryInfoDocumentID) throws ChiliLogException {
        Repository repo = this.getActiveRepository(repositoryInfoDocumentID);
        if (repo != null) {
            if (repo.getStatus() == Status.ONLINE) {
                // Make repository readonly
                repo.makeReadonly();
                return repo;
            }
            else if (repo.getStatus() == Status.READONLY) {
                // Repo already readonly so just return it
                return repo;
            }
        }

        DB db = MongoConnection.getInstance().getConnection();
        RepositoryConfigBO repoInfo = RepositoryConfigController.getInstance().get(db, repositoryInfoDocumentID);
        return makeRepositoryReadOnly(repoInfo);
    }

    /**
     * Creates a repository and make it readonly
     * 
     * @param repoConfig
     *            Runtime information of repository to make readonly
     * @return Repository runtime information
     * @throws ChiliLogException
     */
    private synchronized Repository makeRepositoryReadOnly(RepositoryConfigBO repoConfig) throws ChiliLogException {
        Repository repo = new Repository(repoConfig);
        repo.makeReadonly();
        _activeRepositories.add(repo);
        return repo;
    }

    /**
     * Take the specified repository offline. Streaming of log entries will be disabled and new log entries will not be
     * stored. Also, searching is prohibited on the workbench.
     * 
     * @param repositoryInfoDocumentID
     *            Document ID of the repository to stop
     * @throws ChiliLogException
     */
    public synchronized void takeRepositoryOffline(ObjectId repositoryInfoDocumentID) throws ChiliLogException {
        Repository repo = getActiveRepository(repositoryInfoDocumentID);
        if (repo != null) {
            takeRepositoryOffline(repo);
        }
    }

    /**
     * Take the specified repository off line. Streaming of log entries will be disabled and new log entries will not be
     * stored. Also, searching is prohibited on the workbench.
     * 
     * @param repo
     *            Runtime information of repository to stop
     * @throws ChiliLogException
     */
    private synchronized void takeRepositoryOffline(Repository repo) throws ChiliLogException {
        repo.takeOffline();
        _activeRepositories.remove(repo);
        return;
    }

    /**
     * Finds an online repository with a matching repository configuration document id
     * 
     * @param repositoryConfigDocumentID
     *            ID of repository configuration document of repository to find
     * @return Matching repository. <code>null</code> if not found
     */
    private Repository getActiveRepository(ObjectId repositoryConfigDocumentID) {
        for (Repository repo : _activeRepositories) {
            if (repo.getRepoConfig().getDocumentID().equals(repositoryConfigDocumentID)) {
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
    private ArrayList<RepositoryConfigBO> getRepositoryConfigList() throws ChiliLogException {
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
    public synchronized Repository getRepository(ObjectId id) throws ChiliLogException {
        // Check it see if repository is online
        for (Repository repo : _activeRepositories) {
            if (repo.getRepoConfig().getDocumentID().equals(id)) {
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
    public synchronized Repository[] getRepositories() throws ChiliLogException {
        ArrayList<Repository> list = new ArrayList<Repository>();
        ArrayList<RepositoryConfigBO> repoConfigList = this.getRepositoryConfigList();
        for (RepositoryConfigBO repoConfig : repoConfigList) {
            Repository repo = getActiveRepository(repoConfig.getDocumentID());
            if (repo == null) {
                list.add(new Repository(repoConfig));
            }
            else {
                // Online so send that one
                list.add(repo);
            }
        }

        return list.toArray(new Repository[] {});
    }
}
