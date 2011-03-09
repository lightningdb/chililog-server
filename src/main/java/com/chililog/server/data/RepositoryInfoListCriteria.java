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
 * Criteria for selecting repository information records
 * 
 * @author vibul
 * 
 */
public class RepositoryInfoListCriteria extends ListCriteria
{
    private String _namePattern = null;
    
    /**
     * Basic constructor
     */
    public RepositoryInfoListCriteria()
    {
        return;
    }

    public String getNamePattern()
    {
        return _namePattern;
    }

    /**
     * Search for all repositories matching this name regular expression pattern 
     */
    public void setNamePattern(String usernamePattern)
    {
        _namePattern = usernamePattern;
    }    
}
