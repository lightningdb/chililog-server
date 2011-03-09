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

import org.apache.commons.lang.StringUtils;

import com.chililog.server.common.ChiliLogException;
import com.chililog.server.data.UserBO;
import com.chililog.server.data.UserBO.Status;

/**
 * <p>
 * User API Object is used as part of the User API service to communicate user information with API callers.
 * </p>
 * 
 * @author vibul
 * 
 */
public class UserAO extends AO
{
    private String _documentID;
    private Long _documentVersion;
    private String _username;
    private String _password;
    private String[] _roles = null;
    private Status _status = Status.Enabled;
    private String _displayName;

    /**
     * Basic constructor
     */
    public UserAO()
    {
        return;
    }

    /**
     * Constructor that copies properties form the user business object
     * 
     * @param userBO
     *            User business object from which data will be copied
     */
    public UserAO(UserBO userBO)
    {
        // Note: password is NEVER supplied
        _documentID = userBO.getDocumentID().toString();
        _documentVersion = userBO.getDocumentVersion();
        _username = userBO.getUsername();
        _roles = userBO.getRoles();
        _status = userBO.getStatus();
        _displayName = userBO.getDisplayName();
        return;
    }

    /**
     * Updates the business object with the information from this API object
     * 
     * @param userBO
     * @throws ChiliLogException
     */
    public void toBO(UserBO userBO) throws ChiliLogException
    {
        // Optimistic locking check
        checkOptimisticLocking(_documentVersion,  userBO);

        userBO.setUsername(checkRequiredString("Username", _username));

        // Update the password only if supplied
        if (!StringUtils.isBlank(_password))
        {
            userBO.setPassword(_password, true);
        }

        userBO.removeAllRoles();
        for (String role : _roles)
        {
            userBO.addRole(role);
        }

        userBO.setStatus(_status);

        userBO.setDisplayName(_displayName);
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

    public String getUsername()
    {
        return _username;
    }

    public void setUsername(String username)
    {
        _username = username;
    }

    public String getPassword()
    {
        return _password;
    }

    public void setPassword(String password)
    {
        _password = password;
    }

    public String[] getRoles()
    {
        return _roles;
    }

    public void setRoles(String[] roles)
    {
        _roles = roles;
    }

    public Status getStatus()
    {
        return _status;
    }

    public void setStatus(Status status)
    {
        _status = status;
    }

    public String getDisplayName()
    {
        return _displayName;
    }

    public void setDisplayName(String displayName)
    {
        _displayName = displayName;
    }

}
