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

package com.chililog.server.management.workers;

import java.util.ArrayList;

import com.chililog.server.common.ChiliLogException;
import com.chililog.server.data.RepositoryInfoBO;
import com.chililog.server.data.RepositoryInfoBO.MaxMemoryPolicy;
import com.chililog.server.data.RepositoryInfoBO.Status;
import com.chililog.server.data.RepositoryParserInfoBO;

/**
 * <p>
 * RepositoryInfo API object is used as part of the repository info service.
 * </p>
 * 
 * @author vibul
 * 
 */
public class RepositoryInfoAO extends AO
{
    private String _documentID;
    private Long _documentVersion;
    private String _name;
    private String _displayName;
    private String _description;
    private Status _startupStatus = Status.ONLINE;

    private boolean _storeEntriesIndicator = false;
    private boolean _storageQueueDurableIndicator = false;
    private long _storageQueueWorkerCount = 1;
    private long _storageMaxKeywords = -1;

    private long _maxMemory = 1024 * 1024 * 20; // 20 MB
    private MaxMemoryPolicy _maxMemoryPolicy = MaxMemoryPolicy.PAGE;
    private long _pageSize = 1024 * 1024 * 10; // 10 MB
    private long _pageCountCache = 3; // max 3 pages in memory when paging

    private RepositoryParserInfoAO[] _parsers = null;

    /**
     * Basic constructor
     */
    public RepositoryInfoAO()
    {
        return;
    }

    /**
     * Constructor that copies properties form the business object
     * 
     * @param repoInfo
     *            Repository info business object
     */
    public RepositoryInfoAO(RepositoryInfoBO repoInfo)
    {
        _documentID = repoInfo.getDocumentID().toString();
        _documentVersion = repoInfo.getDocumentVersion();
        _name = repoInfo.getName();
        _displayName = repoInfo.getDisplayName();
        _description = repoInfo.getDescription();

        _startupStatus = repoInfo.getStartupStatus();

        _storeEntriesIndicator = repoInfo.getStoreEntriesIndicator();
        _storageQueueDurableIndicator = repoInfo.getStorageQueueDurableIndicator();
        _storageQueueWorkerCount = repoInfo.getStorageQueueWorkerCount();
        _storageMaxKeywords = repoInfo.getStorageMaxKeywords();

        _maxMemory = repoInfo.getMaxMemory();
        _maxMemoryPolicy = repoInfo.getMaxMemoryPolicy();
        _pageSize = repoInfo.getPageSize();
        _pageCountCache = repoInfo.getPageCountCache();

        if (repoInfo.getParsers() == null || repoInfo.getParsers().isEmpty())
        {
            _parsers = null;
        }
        else
        {
            ArrayList<RepositoryParserInfoAO> parserList = new ArrayList<RepositoryParserInfoAO>();
            for (RepositoryParserInfoBO parserInfo : repoInfo.getParsers())
            {
                parserList.add(new RepositoryParserInfoAO(parserInfo));
            }
            _parsers = parserList.toArray(new RepositoryParserInfoAO[] {});
        }

        return;
    }

    /**
     * Updates the supplied business object with info from this api object
     * 
     * @param repoInfo
     *            business object to update
     * @throws ChiliLogException
     */
    public void toBO(RepositoryInfoBO repoInfo) throws ChiliLogException
    {
        checkOptimisticLocking(_documentVersion, repoInfo);

        repoInfo.setName(_name);
        repoInfo.setDisplayName(_displayName);
        repoInfo.setDescription(_description);
        repoInfo.setStartupStatus(_startupStatus);

        repoInfo.setStoreEntriesIndicator(_storeEntriesIndicator);
        repoInfo.setStorageQueueDurableIndicator(_storageQueueDurableIndicator);
        repoInfo.setStorageQueueWorkerCount(_storageQueueWorkerCount);
        repoInfo.setStorageMaxKeywords(_storageMaxKeywords);

        repoInfo.setMaxMemory(_maxMemory);
        repoInfo.setMaxMemoryPolicy(_maxMemoryPolicy);
        repoInfo.setPageSize(_pageSize);
        repoInfo.setPageCountCache(_pageCountCache);

        repoInfo.getParsers().clear();
        if (_parsers != null && _parsers.length > 0)
        {
            for (RepositoryParserInfoAO parserInfo : _parsers)
            {
                RepositoryParserInfoBO bo = new RepositoryParserInfoBO();
                parserInfo.toBO(bo);
                repoInfo.getParsers().add(bo);
            }
        }

        return;
    }

    public String getDocumentID()
    {
        return _documentID;
    }

    public void setDocumentID(String documentID)
    {
        _documentID = documentID;
    }

    public Long getDocumentVersion()
    {
        return _documentVersion;
    }

    public void setDocumentVersion(Long documentVersion)
    {
        _documentVersion = documentVersion;
    }

    public String getName()
    {
        return _name;
    }

    public void setName(String name)
    {
        _name = name;
    }

    public String getDisplayName()
    {
        return _displayName;
    }

    public void setDisplayName(String displayName)
    {
        _displayName = displayName;
    }

    public String getDescription()
    {
        return _description;
    }

    public void setDescription(String description)
    {
        _description = description;
    }

    public Status getStartupStatus()
    {
        return _startupStatus;
    }

    public void setStartupStatus(Status startupStatus)
    {
        _startupStatus = startupStatus;
    }

    public boolean getStoreEntriesIndicator()
    {
        return _storeEntriesIndicator;
    }

    public void setStoreEntriesIndicator(boolean storeEntriesIndicator)
    {
        _storeEntriesIndicator = storeEntriesIndicator;
    }

    public boolean getStorageQueueDurableIndicator()
    {
        return _storageQueueDurableIndicator;
    }

    public void setStorageQueueDurableIndicator(boolean storageQueueDurableIndicator)
    {
        _storageQueueDurableIndicator = storageQueueDurableIndicator;
    }

    public long getStorageQueueWorkerCount()
    {
        return _storageQueueWorkerCount;
    }

    public void setStorageQueueWorkerCount(long storageQueueWorkerCount)
    {
        _storageQueueWorkerCount = storageQueueWorkerCount;
    }

    public long getStorageMaxKeywords()
    {
        return _storageMaxKeywords;
    }

    public void setStorageMaxKeywords(long maxKeywords)
    {
        _storageMaxKeywords = maxKeywords;
    }
    
    public long getMaxMemory()
    {
        return _maxMemory;
    }

    public void setMaxMemory(long maxMemory)
    {
        _maxMemory = maxMemory;
    }

    public MaxMemoryPolicy getMaxMemoryPolicy()
    {
        return _maxMemoryPolicy;
    }

    public void setMaxMemoryPolicy(MaxMemoryPolicy maxMemoryPolicy)
    {
        _maxMemoryPolicy = maxMemoryPolicy;
    }

    public long getPageSize()
    {
        return _pageSize;
    }

    public void setPageSize(long pageSize)
    {
        _pageSize = pageSize;
    }

    public long getPageCountCache()
    {
        return _pageCountCache;
    }

    public void setPageCountCache(long pageCountCache)
    {
        _pageCountCache = pageCountCache;
    }

    public RepositoryParserInfoAO[] getParsers()
    {
        return _parsers;
    }

    public void setParsers(RepositoryParserInfoAO[] parsers)
    {
        _parsers = parsers;
    }

}
