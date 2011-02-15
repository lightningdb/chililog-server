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

package com.chililog.server.data;

/**
 * Criteria for selecting user records
 * 
 * @author vibul
 * 
 */
public class UserListCriteria extends BaseListCriteria
{
    private String _usernamePattern = null;
    private String _role = null;
    private UserBO.Status _status = null;
    
    /**
     * Basic constructor
     */
    public UserListCriteria()
    {
        return;
    }

    public String getUsernamePattern()
    {
        return _usernamePattern;
    }

    /**
     * Search for all users matching this user name regular expression pattern 
     */
    public void setUsernamePattern(String usernamePattern)
    {
        _usernamePattern = usernamePattern;
    }

    public String getRole()
    {
        return _role;
    }

    /**
     * Search for all users in the specified role 
     */
    public void setRole(String role)
    {
        _role = role;
    }

    public UserBO.Status getStatus()
    {
        return _status;
    }

    /**
     * Search for all users with the specified status
     */
    public void setStatus(UserBO.Status status)
    {
        _status = status;
    }
    
    
    
}
