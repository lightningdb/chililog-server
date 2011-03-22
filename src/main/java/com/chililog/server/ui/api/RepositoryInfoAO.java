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

package com.chililog.server.ui.api;

import java.util.ArrayList;

import com.chililog.server.common.ChiliLogException;
import com.chililog.server.data.RepositoryInfoBO;
import com.chililog.server.data.RepositoryInfoBO.QueueMaxMemoryPolicy;
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
    private String _controllerClassName;
    private Status _startupStatus = Status.ONLINE;
    private boolean _readQueueDurable = false;
    private boolean _writeQueueDurable = false;
    private long _writeQueueWorkerCount = 1;
    private long _writeQueueMaxMemory = 1024 * 1024 * 20; // 20 MB
    private QueueMaxMemoryPolicy _writeQueueMaxMemoryPolicy = QueueMaxMemoryPolicy.PAGE;
    private long _writeQueuePageSize = 1024 * 1024 * 4; // MB

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

        _controllerClassName = repoInfo.getControllerClassName();
        _startupStatus = repoInfo.getStartupStatus();

        _readQueueDurable = repoInfo.isReadQueueDurable();

        _writeQueueDurable = repoInfo.isWriteQueueDurable();
        _writeQueueWorkerCount = repoInfo.getWriteQueueWorkerCount();
        _writeQueueMaxMemory = repoInfo.getWriteQueueMaxMemory();
        _writeQueueMaxMemoryPolicy = repoInfo.getWriteQueueMaxMemoryPolicy();
        _writeQueuePageSize = repoInfo.getWriteQueuePageSize();

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

        repoInfo.setName(checkRequiredField("Name", _name));
        repoInfo.setDisplayName(checkRequiredField("DisplayName", _displayName));
        repoInfo.setDescription(_description);
        repoInfo.setControllerClassName(checkRequiredField("ControllerClassName", _controllerClassName));
        repoInfo.setStartupStatus(_startupStatus);

        repoInfo.setReadQueueDurable(_readQueueDurable);

        repoInfo.setWriteQueueDurable(_writeQueueDurable);
        repoInfo.setWriteQueueWorkerCount(_writeQueueWorkerCount);
        repoInfo.setWriteQueueMaxMemory(_writeQueueMaxMemory);
        repoInfo.setWriteQueueMaxMemoryPolicy(_writeQueueMaxMemoryPolicy);
        repoInfo.setWriteQueuePageSize(_writeQueuePageSize);

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

    public String getControllerClassName()
    {
        return _controllerClassName;
    }

    public void setControllerClassName(String controllerClassName)
    {
        _controllerClassName = controllerClassName;
    }

    public Status getStartupStatus()
    {
        return _startupStatus;
    }

    public void setStartupStatus(Status startupStatus)
    {
        _startupStatus = startupStatus;
    }

    public boolean isReadQueueDurable()
    {
        return _readQueueDurable;
    }

    public void setReadQueueDurable(boolean readQueueDurable)
    {
        _readQueueDurable = readQueueDurable;
    }

    public boolean isWriteQueueDurable()
    {
        return _writeQueueDurable;
    }

    public void setWriteQueueDurable(boolean writeQueueDurable)
    {
        _writeQueueDurable = writeQueueDurable;
    }

    public long getWriteQueueWorkerCount()
    {
        return _writeQueueWorkerCount;
    }

    public void setWriteQueueWorkerCount(long writeQueueWorkerCount)
    {
        _writeQueueWorkerCount = writeQueueWorkerCount;
    }

    public long getWriteQueueMaxMemory()
    {
        return _writeQueueMaxMemory;
    }

    public void setWriteQueueMaxMemory(long writeQueueMaxMemory)
    {
        _writeQueueMaxMemory = writeQueueMaxMemory;
    }

    public QueueMaxMemoryPolicy getWriteQueueMaxMemoryPolicy()
    {
        return _writeQueueMaxMemoryPolicy;
    }

    public void setWriteQueueMaxMemoryPolicy(QueueMaxMemoryPolicy writeQueueMaxMemoryPolicy)
    {
        _writeQueueMaxMemoryPolicy = writeQueueMaxMemoryPolicy;
    }

    public long getWriteQueuePageSize()
    {
        return _writeQueuePageSize;
    }

    public void setWriteQueuePageSize(long writeQueuePageSize)
    {
        _writeQueuePageSize = writeQueuePageSize;
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