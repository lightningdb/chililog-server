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

import com.chililog.server.data.RepositoryInfoBO.Status;
import com.chililog.server.engine.Repository;

/**
 * <p>
 * Repository API Object details the status of a repository
 * </p>
 * 
 * @author vibul
 * 
 */
public class RepositoryAO extends AO
{
    private String _documentID;

    private Long _documentVersion;

    private String _name;

    private Status _status;

    /**
     * Basic constructor
     */
    public RepositoryAO()
    {
        return;
    }

    /**
     * Constructor that copies properties form the business object
     * 
     * @param repo
     *            Repository
     */
    public RepositoryAO(Repository repo)
    {
        _documentID = repo.getRepoInfo().getDocumentID().toString();
        _documentVersion = repo.getRepoInfo().getDocumentVersion();
        _name = repo.getRepoInfo().getName();
        _status = repo.getStatus();
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

    /**
     * Returns the repository name
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
     * Returns the repository status
     */
    public Status getStatus()
    {
        return _status;
    }

    public void setStatus(Status status)
    {
        _status = status;
    }

}
