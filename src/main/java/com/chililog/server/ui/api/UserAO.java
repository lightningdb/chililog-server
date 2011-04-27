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
import com.chililog.server.common.CryptoUtils;
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
    private String _emailAddress;
    private String _password;
    private String[] _roles = null;
    private Status _status = null;
    private String _displayName;
    private String _gravatarMD5Hash;

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
        this(userBO, true);
        return;
    }

    /**
     * Constructor that copies properties form the user business object
     * 
     * @param userBO
     *            User business object from which data will be copied
     * @param copyAllProperties
     *            If true, then all properties will be copied. If false, then only bare essentials (username, display
     *            name and gravatar hash) will be copied. Typically, only administrators get all properties. Non
     *            administrators get bare essentials to help with user lookups.
     */
    public UserAO(UserBO userBO, boolean copyAllProperties)
    {
        // Note: password hash is NEVER supplied for security reasons
        _documentID = userBO.getDocumentID().toString();
        _documentVersion = userBO.getDocumentVersion();
        _username = userBO.getUsername();
        _displayName = userBO.getDisplayName();
        if (!StringUtils.isBlank(_emailAddress))
        {
            try
            {
                _gravatarMD5Hash = CryptoUtils.createMD5Hash(_emailAddress.trim().toLowerCase());
            }
            catch (Exception ex)
            {
                // ignore
            }
        }

        if (copyAllProperties)
        {
            _roles = userBO.getRoles();
            _status = userBO.getStatus();
            _emailAddress = userBO.getEmailAddress();
        }

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
        checkOptimisticLocking(_documentVersion, userBO);

        userBO.setUsername(_username);

        // Password required on create. On update, change the password only if supplied
        if (userBO.isExistingRecord())
        {
            if (!StringUtils.isBlank(_password))
            {
                userBO.setPassword(_password, true);
            }
        }
        else
        {
            userBO.setPassword(_password, true);
        }

        userBO.removeAllRoles();
        if (_roles != null)
        {
            for (String role : _roles)
            {
                userBO.addRole(role);
            }
        }

        userBO.setStatus(_status);

        userBO.setDisplayName(_displayName);

        userBO.setEmailAddress(_emailAddress);
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

    public String getEmailAddress()
    {
        return _emailAddress;
    }

    public void setEmailAddress(String emailAddress)
    {
        _emailAddress = emailAddress;
    }

    public String getGravatarMD5Hash()
    {
        return _gravatarMD5Hash;
    }

    public void setGravatarMD5Hash(String gravatarMD5Hash)
    {
        _gravatarMD5Hash = gravatarMD5Hash;
    }

}
