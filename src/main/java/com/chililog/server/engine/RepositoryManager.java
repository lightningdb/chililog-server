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

import com.chililog.server.common.ChiliLogException;
import com.chililog.server.common.Log4JLogger;
import com.chililog.server.data.MongoConnection;
import com.chililog.server.data.RepositoryInfoBO;
import com.chililog.server.data.RepositoryInfoBO.Status;
import com.chililog.server.data.RepositoryInfoController;
import com.chililog.server.data.RepositoryInfoListCriteria;
import com.mongodb.DB;

/**
 * <p>
 * The repository manager is responsible managing for start/stop/reload all our repositories (as represented by the 
 * {@link Repository} class).
 * </p>
 * 
 * <pre>
 * // Start all repositories
 * RepositoryManager.getInstance().start();
 * 
 * // Reload individual repositories if repository configuration has been updated
 * RepositoryManager.getInstance().loadRepositories();
 * 
 * // Stop all repositories
 * RepositoryManager.getInstance().stop();
 * </pre>
 * 
 * <p>
 * It is assumed that mongoDB and HornetQ (via {@link MqManager}) has been started.
 * </p>
 * 
 * @author vibul
 * 
 */
public class RepositoryManager
{
    static Log4JLogger _logger = Log4JLogger.getLogger(RepositoryManager.class);

    private ArrayList<Repository> _repositories = new ArrayList<Repository>();

    /**
     * Returns the singleton instance for this class
     */
    public static RepositoryManager getInstance()
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
        public static final RepositoryManager INSTANCE = new RepositoryManager();
    }

    /**
     * <p>
     * Singleton constructor
     * </p>
     */
    private RepositoryManager()
    {
        return;
    }

    /**
     * <p>
     * Loads repositories and starts the ones flagged for starting at startup
     * </p>
     * <p>
     * Should be called once at the start of the application
     * </p>
     */
    public synchronized void start() throws ChiliLogException
    {
        loadRepositories();
        for (Repository repo : _repositories)
        {
            if (repo.getRepoInfo().getStartupStatus() == Status.ONLINE)
            {
                repo.start();
            }
        }
        return;
    }

    /**
     * <p>
     * Loads/Reloads repositories based on the repository information stored in the database.
     * </p>
     * <p>
     * This code assumes that a repository must be off-line before its information can be changed
     * </p>
     * 
     * @throws Exception
     */
    public synchronized void loadRepositories() throws ChiliLogException
    {
        try
        {
            ArrayList<RepositoryInfoBO> repoInfoToAdd = new ArrayList<RepositoryInfoBO>();
            ArrayList<Repository> repoToDelete = new ArrayList<Repository>();

            // Get count connections
            DB db = MongoConnection.getInstance().getConnection();

            // Get list of repositories
            RepositoryInfoListCriteria criteria = new RepositoryInfoListCriteria();
            ArrayList<RepositoryInfoBO> newRepoInfoList = RepositoryInfoController.getInstance().getList(db, criteria);

            // Figure our which repositories have been removed/changed
            for (Repository repo : _repositories)
            {
                RepositoryInfoBO currentRepoInfo = repo.getRepoInfo();
                RepositoryInfoBO newRepoInfo = findMatchingRepositoryInfo(newRepoInfoList, currentRepoInfo);
                if (newRepoInfo == null)
                {
                    repoToDelete.add(repo);
                }
                else if (newRepoInfo.getDocumentVersion() != currentRepoInfo.getDocumentVersion())
                {
                    repoToDelete.add(repo);
                    repoInfoToAdd.add(newRepoInfo);
                }
            }

            // Figure out which repository have been added
            for (RepositoryInfoBO newRepoInfo : newRepoInfoList)
            {
                RepositoryInfoBO currentRepoInfo = findMatchingRepositoryInfo2(_repositories, newRepoInfo);
                if (currentRepoInfo == null)
                {
                    repoInfoToAdd.add(newRepoInfo);
                }
            }

            // Stop removed/changed repositories
            for (Repository repo : repoToDelete)
            {
                repo.stop();
                _repositories.remove(repo);
            }

            // Start new/changed repositories
            for (RepositoryInfoBO newRepoInfo : newRepoInfoList)
            {
                Repository repo = new Repository(newRepoInfo);
                _repositories.add(repo);
            }

            return;
        }
        catch (Exception ex)
        {
            _logger.error(ex, Strings.LOAD_REPOSITORIES_ERROR, ex.getMessage());
            throw new ChiliLogException(ex, Strings.LOAD_REPOSITORIES_ERROR, ex.getMessage());
        }
    }

    /**
     * Finds a matching repository information based on the unique id
     * 
     * @param list
     *            list of repository information to search on
     * @param repoInfoToMatch
     *            repository information to match
     * @return Matching repository information record. <code>null</code> if not found
     */
    private RepositoryInfoBO findMatchingRepositoryInfo(ArrayList<RepositoryInfoBO> list,
                                                        RepositoryInfoBO repoInfoToMatch)
    {
        for (RepositoryInfoBO repoInfo : list)
        {
            if (repoInfo.getDocumentID().equals(repoInfoToMatch.getDocumentID()))
            {
                return repoInfo;
            }
        }

        return null;
    }

    /**
     * Finds a matching repository information based on the unique id
     * 
     * @param list
     *            list of repository information to search on
     * @param repoInfoToMatch
     *            repository information to match
     * @return Matching repository information record. <code>null</code> if not found
     */
    private RepositoryInfoBO findMatchingRepositoryInfo2(ArrayList<Repository> list, RepositoryInfoBO repoInfoToMatch)
    {
        for (Repository repo : list)
        {
            RepositoryInfoBO repoInfo = repo.getRepoInfo();
            if (repoInfo.getDocumentID().equals(repoInfoToMatch.getDocumentID()))
            {
                return repoInfo;
            }
        }

        return null;
    }

    /**
     * Stops our repositories.
     * 
     * @throws Exception
     */
    public synchronized void stop() throws Exception
    {
        for (Repository repo : _repositories)
        {
            repo.stop();
        }
        return;
    }

    /**
     * Gets the named repository
     * 
     * @param name
     *            name of repository as defined in the repository information
     * @return Matching repository. Null if not found.
     */
    public synchronized Repository getRepository(String name)
    {
        for (Repository repo : _repositories)
        {
            if (repo.getRepoInfo().getName().equals(name))
            {
                return repo;
            }
        }
        return null;
    }
}
