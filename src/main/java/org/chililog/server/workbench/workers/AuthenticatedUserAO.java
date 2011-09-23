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

package org.chililog.server.workbench.workers;

import org.apache.commons.lang.StringUtils;
import org.chililog.server.common.ChiliLogException;
import org.chililog.server.common.CryptoUtils;
import org.chililog.server.data.UserBO;

/**
 * <p>
 * User API Object used to return the logged in user's details.
 * </p>
 * 
 * @author vibul
 * 
 */
public class AuthenticatedUserAO extends AO {
    private String _documentID;
    private Long _documentVersion;
    private String _username;
    private String _emailAddress;
    private String _displayName;
    private String[] _roles = null;
    private String _gravatarMD5Hash;

    /**
     * Basic constructor
     */
    public AuthenticatedUserAO() {
        return;
    }

    /**
     * Constructor that copies properties form the user business object
     * 
     * @param userBO
     *            User business object from which data will be copied
     */
    public AuthenticatedUserAO(UserBO userBO) {
        _documentID = userBO.getDocumentID().toString();
        _documentVersion = userBO.getDocumentVersion();
        _username = userBO.getUsername();
        _displayName = userBO.getDisplayName();
        _emailAddress = userBO.getEmailAddress();
        _roles = userBO.getRoles();
        if (!StringUtils.isBlank(_emailAddress)) {
            try {
                _gravatarMD5Hash = CryptoUtils.createMD5Hash(_emailAddress.trim().toLowerCase());
            }
            catch (Exception ex) {
                // ignore
            }
        }
    }

    /**
     * Updates the business object with the information from this API object
     * 
     * @param userBO
     * @throws ChiliLogException
     */
    public void toBO(UserBO userBO) throws ChiliLogException {
        // Optimistic locking check
        checkOptimisticLocking(_documentVersion, userBO);

        userBO.setUsername(_username);

        userBO.setDisplayName(_displayName);

        userBO.setEmailAddress(_emailAddress);

        // Not allowed to update roles under "my account"
    }

    public String getDocumentID() {
        return _documentID;
    }

    public void setDocumentID(String documentID) {
        _documentID = documentID;
    }

    public Long getDocumentVersion() {
        return _documentVersion;
    }

    public void setDocumentVersion(Long documentVersion) {
        _documentVersion = documentVersion;
    }

    public String getUsername() {
        return _username;
    }

    public void setUsername(String username) {
        _username = username;
    }

    public String getDisplayName() {
        return _displayName;
    }

    public void setDisplayName(String displayName) {
        _displayName = displayName;
    }

    public String getEmailAddress() {
        return _emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        _emailAddress = emailAddress;
    }

    public String getGravatarMD5Hash() {
        return _gravatarMD5Hash;
    }

    public void setGravatarMD5Hash(String gravatarMD5Hash) {
        _gravatarMD5Hash = gravatarMD5Hash;
    }

    public String[] getRoles() {
        return _roles;
    }

    public void setRoles(String[] roles) {
        _roles = roles;
    }

}
