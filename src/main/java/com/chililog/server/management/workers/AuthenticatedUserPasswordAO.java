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

/**
 * <p>
 * API Object for users to change their passwords
 * </p>
 * 
 * @author vibul
 * 
 */
public class AuthenticatedUserPasswordAO extends AO
{
    private String _documentID;
    private String _oldPassword;
    private String _newPassword;
    private String _confirmNewPassword;

    /**
     * Basic constructor
     */
    public AuthenticatedUserPasswordAO()
    {
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

    public String getOldPassword()
    {
        return _oldPassword;
    }

    public void setOldPassword(String oldPassword)
    {
        _oldPassword = oldPassword;
    }

    public String getNewPassword()
    {
        return _newPassword;
    }

    public void setNewPassword(String newPassword)
    {
        _newPassword = newPassword;
    }

    public String getConfirmNewPassword()
    {
        return _confirmNewPassword;
    }

    public void setConfirmNewPassword(String confirmNewPassword)
    {
        _confirmNewPassword = confirmNewPassword;
    }

  
}
