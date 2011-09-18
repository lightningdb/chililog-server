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

package org.chililog.server.workbench;

/**
 * Codes for strings in the strings.properties file
 * 
 * @author vibul
 * 
 */
public class Strings
{
    public static final String PREFIX = "Workbench.";
    public static final String API_NOT_FOUND_ERROR = PREFIX + "ApiNotFoundError";
    public static final String URI_PATH_PARAMETER_ERROR = PREFIX + "UriPathParameterError";
    public static final String URI_QUERY_STRING_PARAMETER_ERROR = PREFIX + "UriQueryStringParameterError";
    public static final String URI_QUERY_STRING_PARAMETER_OR_HEADER_ERROR = PREFIX + "UriQueryStringParameterOrHeaderError";
    
    public static final String REQUIRED_CONTENT_ERROR = PREFIX + "RequiredContentError";
    public static final String REQUIRED_FIELD_ERROR = PREFIX + "RequiredFieldError";
    public static final String OPTIMISTIC_LOCKING_ERROR = PREFIX + "OptimisticLockingError";

    public static final String AUTHENTICAITON_TOKEN_EXPIRED_ERROR = PREFIX + "AuthenticationTokenExpiredError";
    public static final String AUTHENTICAITON_TOKEN_INVALID_ERROR = PREFIX + "AuthenticationTokenInvalidError";
    public static final String AUTHENTICAITON_BAD_USERNAME_PASSWORD_ERROR = PREFIX + "AuthenticationBadUsernameOrPasswordError";
    public static final String AUTHENTICAITON_ACCOUNT_DISABLED_ERROR = PREFIX + "AuthenticationAccountDisabledError";
    public static final String AUTHENTICAITON_ACCOUNT_LOCKED_ERROR = PREFIX + "AuthenticationAccountLockedError";
    public static final String AUTHENTICAITON_ACCESS_DENIED_ERROR = PREFIX + "AuthenticationAccessDeniedError";
    public static final String NOT_AUTHORIZED_ERROR = PREFIX + "NotAuthorizedError";
    
    public static final String REPOSITORY_NOT_FOUND_ERROR = PREFIX + "RepositoryNotFoundError";
    public static final String REPOSITORY_OFFLINE_ERROR = PREFIX + "RepositoryOfflineError";
    
}
