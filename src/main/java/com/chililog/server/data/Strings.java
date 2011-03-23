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
 * Codes for strings in the strings.properties file
 * 
 * @author vibul
 * 
 */
public class Strings
{
    public static final String PREFIX = "Data.";
    public static final String MONGODB_AUTHENTICATION_ERROR = PREFIX + "MongoDB.AuthenticationError";
    public static final String MONGODB_CONNECTION_ERROR = PREFIX + "MongoDB.ConnectionError";
    public static final String MONGODB_FIELD_NOT_FOUND_ERROR = PREFIX + "MongoDB.FieldNotFoundError";
    public static final String MONGODB_QUERY_ERROR = PREFIX + "MongoDB.QueryError";
    public static final String MONGODB_SAVE_ERROR = PREFIX + "MongoDB.SaveError";
    public static final String MONGODB_REMOVE_ERROR = PREFIX + "MongoDB.RemoveError";
    
    public static final String USER_DUPLICATE_USERNAME_ERROR = PREFIX + "User.DuplicateUsernameError";
    public static final String USER_NOT_FOUND_ERROR = PREFIX + "User.NotFoundError";
  
    public static final String REPO_INFO_DUPLICATE_NAME_ERROR = PREFIX + "RepoInfo.DuplicateNameError";
    public static final String REPO_INFO_DUPLICATE_PARSER_NAME_ERROR = PREFIX + "RepoInfo.DuplicateParserNameError";
    public static final String REPO_INFO_DUPLICATE_FIELD_NAME_ERROR = PREFIX + "RepoInfo.DuplicateFieldNameError";
    public static final String REPO_INFO_NOT_FOUND_ERROR = PREFIX + "RepoInfo.NotFoundError";
    public static final String REPO_INFO_FIELD_PROPERTIES_ERROR = PREFIX + "RepoInfo.FieldPropertiesError";
    
    public static final String REPO_NAME_NOT_SET_ERROR = PREFIX + "Repo.NameNotSetError";

    
}
