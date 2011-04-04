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
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

import com.chililog.server.common.ChiliLogException;
import com.chililog.server.common.JsonTranslator;
import com.chililog.server.common.Log4JLogger;
import com.chililog.server.data.MongoConnection;
import com.chililog.server.data.UserBO;
import com.chililog.server.data.UserBO.Status;
import com.chililog.server.data.UserController;
import com.chililog.server.ui.Strings;
import com.mongodb.DB;

/**
 * <p>
 * Authentication API handles:
 * <ul>
 * <li>login - HTTP POST method</li>
 * <li>logout - HTTP DELETE method</li>
 * </p>
 */
public class AuthenticationWorker extends Worker
{
    private static Log4JLogger _logger = Log4JLogger.getLogger(AuthenticationWorker.class);

    /**
     * Constructor
     */
    public AuthenticationWorker(HttpRequest request)
    {
        super(request);
        return;
    }

    /**
     * Can only create and delete sessions
     */
    @Override
    public HttpMethod[] getSupportedMethods()
    {
        return new HttpMethod[]
        { HttpMethod.POST, HttpMethod.DELETE };
    }

    /**
     * Need special processing because for POST (login), there is no authentication token as yet
     */
    @Override
    protected ApiResult validateAuthenticationToken()
    {
        if (this.getRequest().getMethod() == HttpMethod.POST)
        {
            return new ApiResult();
        }
        return super.validateAuthenticationToken();
    }

    /**
     * Anyone can login/logout. No need check authorization.
     * 
     * @return {@link ApiResult}
     */
    @Override
    protected ApiResult validateAuthenticatedUserRole()
    {
        return new ApiResult();
    }

    /**
     * There are no URI parameters so no need to check
     * 
     * @return {@link ApiResult}
     */
    @Override
    protected ApiResult validateURI()
    {
        return new ApiResult();
    }

    /**
     * Login. If error, 401 Unauthorized is returned to the caller.
     * 
     * @throws Exception
     */
    @Override
    public ApiResult processPost(Object requestContent) throws Exception
    {
        AuthenticationAO requestApiObject = JsonTranslator.getInstance().fromJson(
                bytesToString((byte[]) requestContent), AuthenticationAO.class);

        // Check request data
        if (StringUtils.isBlank(requestApiObject.getUsername()))
        {
            return new ApiResult(HttpResponseStatus.BAD_REQUEST, new ChiliLogException(Strings.REQUIRED_FIELD_ERROR,
                    "Username"));
        }
        if (StringUtils.isBlank(requestApiObject.getPassword()))
        {
            return new ApiResult(HttpResponseStatus.BAD_REQUEST, new ChiliLogException(Strings.REQUIRED_FIELD_ERROR,
                    "Password"));
        }

        // Check if user exists
        DB db = MongoConnection.getInstance().getConnection();
        UserBO user = UserController.getInstance().tryGetByUsername(db, requestApiObject.getUsername());
        if (user == null)
        {
            _logger.error("Authentication failed. Cannot find username '%s'", requestApiObject.getUsername());
            return new ApiResult(HttpResponseStatus.UNAUTHORIZED, new ChiliLogException(
                    Strings.AUTHENTICAITON_BAD_USERNAME_PASSWORD_ERROR));
        }

        // Check password
        if (!user.validatePassword(requestApiObject.getPassword()))
        {
            // TODO lockout user

            _logger.error("Authentication failed. Invalid password for user '%s'", requestApiObject.getUsername());
            return new ApiResult(HttpResponseStatus.UNAUTHORIZED, new ChiliLogException(
                    Strings.AUTHENTICAITON_BAD_USERNAME_PASSWORD_ERROR));
        }

        // Check if the user is enabled
        if (user.getStatus() != Status.Enabled)
        {
            _logger.error("Authentication failed. User '%s' status not enabled: '%s'.", requestApiObject.getUsername(),
                    user.getStatus());
            if (user.getStatus() == Status.Disabled)
            {
                return new ApiResult(HttpResponseStatus.UNAUTHORIZED, new ChiliLogException(
                        Strings.AUTHENTICAITON_ACCOUNT_DISABLED_ERROR));
            }
            else if (user.getStatus() == Status.Locked)
            {
                return new ApiResult(HttpResponseStatus.UNAUTHORIZED, new ChiliLogException(
                        Strings.AUTHENTICAITON_ACCOUNT_LOCKED_ERROR));
            }
            else
            {
                // Catch all just in-case
                return new ApiResult(HttpResponseStatus.UNAUTHORIZED, new ChiliLogException(
                        Strings.AUTHENTICAITON_BAD_USERNAME_PASSWORD_ERROR));
            }
        }

        // Generate token
        AuthenticationTokenAO token = new AuthenticationTokenAO(user, requestApiObject);

        // Return response
        return new ApiResult(token, JSON_CONTENT_TYPE, new UserAO(user));
    }

    /**
     * Placeholder API for if we ever decide to keep server side sessions. DELETE will remove the session data.
     */
    @Override
    public ApiResult processDelete() throws Exception
    {
        return new ApiResult(this.getAuthenticationToken(), null, null);
    }

}
