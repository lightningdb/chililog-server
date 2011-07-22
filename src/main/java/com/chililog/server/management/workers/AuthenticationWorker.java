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
import com.chililog.server.management.Strings;
import com.mongodb.DB;

/**
 * <p>
 * Authentication API handles:
 * <ul>
 * <li>login - HTTP POST /api/authentication</li>
 * <li>logout - HTTP DELETE /api/authentication</li>
 * <li>get user associated with token - HTTP GET /api/authentication</li>
 * <li>update user profile - HTTP PUT /api/authentication?action=update_profile</li>
 * <li>change password - HTTP PUT /api/authentication?action=change_password</li>
 * </p>
 */
public class AuthenticationWorker extends Worker
{
    private static Log4JLogger _logger = Log4JLogger.getLogger(AuthenticationWorker.class);

    public static final String ACTION_URI_QUERYSTRING_PARAMETER_NAME = "action";
    public static final String UPDATE_PROFILE_OPERATION = "update_profile";
    public static final String CHANGE_PASSWORD_OPERATION = "change_password";

    /**
     * Constructor
     */
    public AuthenticationWorker(HttpRequest request)
    {
        super(request);
        return;
    }

    /**
     * Supported HTTP methods
     */
    @Override
    public HttpMethod[] getSupportedMethods()
    {
        return new HttpMethod[]
        { HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE, HttpMethod.GET };
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
        try
        {
            AuthenticationAO requestApiObject = JsonTranslator.getInstance().fromJson(
                    bytesToString((byte[]) requestContent), AuthenticationAO.class);

            // Check request data
            if (StringUtils.isBlank(requestApiObject.getUsername()))
            {
                return new ApiResult(HttpResponseStatus.BAD_REQUEST, new ChiliLogException(
                        Strings.REQUIRED_FIELD_ERROR, "Username"));
            }
            if (StringUtils.isBlank(requestApiObject.getPassword()))
            {
                return new ApiResult(HttpResponseStatus.BAD_REQUEST, new ChiliLogException(
                        Strings.REQUIRED_FIELD_ERROR, "Password"));
            }

            // Check if user exists
            DB db = MongoConnection.getInstance().getConnection();
            UserBO user = UserController.getInstance().tryGetByUsername(db, requestApiObject.getUsername());
            if (user == null)
            {
                user = UserController.getInstance().tryGetByEmailAddress(db, requestApiObject.getUsername());
                if (user == null)
                {
                    _logger.error("Authentication failed. Cannot find username '%s'", requestApiObject.getUsername());
                    return new ApiResult(HttpResponseStatus.UNAUTHORIZED, new ChiliLogException(
                            Strings.AUTHENTICAITON_BAD_USERNAME_PASSWORD_ERROR));
                }
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
                _logger.error("Authentication failed. User '%s' status not enabled: '%s'.",
                        requestApiObject.getUsername(), user.getStatus());
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

            // Check if the user has access (must be system administrator, repo admin or repo workbench user)
            boolean allowed = false;
            for (String role : user.getRoles())
            {
                if (role.equals(UserBO.SYSTEM_ADMINISTRATOR_ROLE_NAME))
                {
                    allowed = true;
                    break;
                }
                else if (role.startsWith(UserBO.REPOSITORY_ROLE_PREFIX))
                {
                    if (role.endsWith(UserBO.REPOSITORY_ADMINISTRATOR_ROLE_SUFFIX)
                            || role.endsWith(UserBO.REPOSITORY_WORKBENCH_ROLE_SUFFIX))
                    {
                        allowed = true;
                        break;
                    }
                }
            }
            if (!allowed)
            {
                return new ApiResult(HttpResponseStatus.UNAUTHORIZED, new ChiliLogException(
                        Strings.AUTHENTICAITON_ACCESS_DENIED_ERROR));
            }

            // Generate token
            AuthenticationTokenAO token = new AuthenticationTokenAO(user, requestApiObject);

            // Return response
            return new ApiResult(token, JSON_CONTENT_TYPE, new AuthenticatedUserAO(user));
        }
        catch (Exception ex)
        {
            return new ApiResult(HttpResponseStatus.BAD_REQUEST, ex);
        }
    }

    /**
     * Update user profile or password.
     * 
     * @throws Exception
     */
    @Override
    public ApiResult processPut(Object requestContent) throws Exception
    {
        try
        {
            UserBO user = this.getAuthenticatedUser();
            String action = this.getUriQueryStringParameter(ACTION_URI_QUERYSTRING_PARAMETER_NAME, false);

            if (action.equalsIgnoreCase(UPDATE_PROFILE_OPERATION))
            {
                AuthenticatedUserAO requestApiObject = JsonTranslator.getInstance().fromJson(
                        bytesToString((byte[]) requestContent), AuthenticatedUserAO.class);

                // The logged in user must be doing this operation
                if (!requestApiObject.getDocumentID().equals(user.getDocumentID().toString()))
                {
                    return new ApiResult(HttpResponseStatus.UNAUTHORIZED, new ChiliLogException(
                            Strings.NOT_AUTHORIZED_ERROR));
                }

                // Update profile details
                requestApiObject.toBO(user);
            }
            else if (action.equalsIgnoreCase(CHANGE_PASSWORD_OPERATION))
            {
                AuthenticatedUserPasswordAO requestApiObject = JsonTranslator.getInstance().fromJson(
                        bytesToString((byte[]) requestContent), AuthenticatedUserPasswordAO.class);

                // The logged in user must be doing this operation
                if (!requestApiObject.getDocumentID().equals(user.getDocumentID().toString()))
                {
                    return new ApiResult(HttpResponseStatus.UNAUTHORIZED, new ChiliLogException(
                            Strings.NOT_AUTHORIZED_ERROR));
                }

                // Check old password
                if (!user.validatePassword(requestApiObject.getOldPassword()))
                {
                    _logger.error("Authentication failed. Invalid password for user '%s'", user.getUsername());
                    return new ApiResult(HttpResponseStatus.UNAUTHORIZED, new ChiliLogException(
                            Strings.AUTHENTICAITON_BAD_USERNAME_PASSWORD_ERROR));
                }

                // Check new passwords
                if (!requestApiObject.getNewPassword().equals(requestApiObject.getConfirmNewPassword()))
                {
                    _logger.error("New password confirmation failed.", user.getUsername());
                    return new ApiResult(HttpResponseStatus.UNAUTHORIZED, new ChiliLogException(
                            Strings.AUTHENTICAITON_BAD_USERNAME_PASSWORD_ERROR));
                }

                // Update
                user.setPassword(requestApiObject.getNewPassword(), true);
            }
            else
            {
                throw new UnsupportedOperationException(String.format("Action '%s' not supported.", action));
            }

            // Save changes
            DB db = MongoConnection.getInstance().getConnection();
            UserController.getInstance().save(db, user);

            // Return updated user details
            return new ApiResult(this.getAuthenticationToken(), JSON_CONTENT_TYPE, new AuthenticatedUserAO(user));
        }
        catch (Exception ex)
        {
            return new ApiResult(HttpResponseStatus.BAD_REQUEST, ex);
        }
    }

    /**
     * Get user details associated with a token. Typically used in association with a "remember me" function. The
     * authentication token is saved so that when the user next starts up the browser, the token can be used to retrieve
     * the user's details.
     * 
     * @throws Exception
     */
    @Override
    public ApiResult processGet() throws Exception
    {
        return new ApiResult(this.getAuthenticationToken(), JSON_CONTENT_TYPE, new AuthenticatedUserAO(
                this.getAuthenticatedUser()));
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
