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

package com.chililog.server.workbench.workers;

import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

import com.chililog.server.common.ChiliLogException;
import com.chililog.server.common.JsonTranslator;
import com.chililog.server.data.MongoConnection;
import com.chililog.server.data.UserBO;
import com.chililog.server.data.UserBO.Status;
import com.chililog.server.data.UserController;
import com.chililog.server.data.UserListCriteria;
import com.chililog.server.workbench.Strings;
import com.mongodb.DB;

/**
 * <p>
 * Users worker provides the following services:
 * <ul>
 * <li>create - HTTP POST /api/users</li>
 * <li>read all - HTTP GET /api/users</li>
 * <li>read one - HTTP GET /api/users/{id}</li>
 * <li>update - HTTP PUT /api/users/{id}</li>
 * <li>delete - HTTP DELETE /api/users/{id}</li>
 * </p>
 */
public class UsersWorker extends Worker
{
    public static final String USERNAME_URI_QUERYSTRING_PARAMETER_NAME = "username";
    public static final String EMAIL_ADDRESS_URI_QUERYSTRING_PARAMETER_NAME = "email";
    public static final String ROLE_URI_QUERYSTRING_PARAMETER_NAME = "role";
    public static final String STATUS_URI_QUERYSTRING_PARAMETER_NAME = "status";

    /**
     * Constructor
     */
    public UsersWorker(HttpRequest request)
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
        { HttpMethod.POST, HttpMethod.DELETE, HttpMethod.GET, HttpMethod.PUT };
    }

    /**
     * Let's validate if the user is able to access these functions
     */
    @Override
    protected ApiResult validateAuthenticatedUserRole()
    {
        HttpMethod requestMethod = this.getRequest().getMethod();
        try
        {
            UserBO user = this.getAuthenticatedUser();

            // Administrators can do it all
            if (user.isSystemAdministrator())
            {
                return new ApiResult();
            }

            // Cannot PUT, POST or DELETE
            if (requestMethod == HttpMethod.PUT || requestMethod == HttpMethod.POST || requestMethod == HttpMethod.DELETE)
            {
                throw new ChiliLogException(Strings.NOT_AUTHORIZED_ERROR);
            }

            // Allow GET
            return new ApiResult();
        }
        catch (Exception ex)
        {
            return new ApiResult(HttpResponseStatus.UNAUTHORIZED, ex);
        }

    }

    /**
     * Create
     * 
     * @throws Exception
     */
    @Override
    public ApiResult processPost(Object requestContent) throws Exception
    {
        try
        {
            if (requestContent == null)
            {
                throw new ChiliLogException(Strings.REQUIRED_CONTENT_ERROR);
            }

            UserAO userAO = JsonTranslator.getInstance().fromJson(bytesToString((byte[]) requestContent), UserAO.class);

            UserBO userBO = new UserBO();
            userAO.toBO(userBO);

            DB db = MongoConnection.getInstance().getConnection();
            UserController.getInstance().save(db, userBO);

            // Return response
            return new ApiResult(this.getAuthenticationToken(), JSON_CONTENT_TYPE, new UserAO(userBO));
        }
        catch (Exception ex)
        {
            return new ApiResult(HttpResponseStatus.BAD_REQUEST, ex);
        }
    }

    /**
     * Delete
     * 
     * @throws Exception
     */
    @Override
    public ApiResult processDelete() throws Exception
    {
        try
        {
            String id = this.getUriPathParameters()[ID_URI_PATH_PARAMETER_INDEX];

            DB db = MongoConnection.getInstance().getConnection();
            UserBO userBO = UserController.getInstance().tryGet(db, new ObjectId(id));
            if (userBO != null)
            {
                UserController.getInstance().remove(db, userBO);
            }

            // Return response
            return new ApiResult(this.getAuthenticationToken(), null, null);
        }
        catch (Exception ex)
        {
            return new ApiResult(HttpResponseStatus.BAD_REQUEST, ex);
        }
    }

    /**
     * Update
     * 
     * @throws Exception
     */
    @Override
    public ApiResult processPut(Object requestContent) throws Exception
    {
        try
        {
            if (requestContent == null)
            {
                throw new ChiliLogException(Strings.REQUIRED_CONTENT_ERROR);
            }

            String id = this.getUriPathParameters()[ID_URI_PATH_PARAMETER_INDEX];

            DB db = MongoConnection.getInstance().getConnection();
            UserBO userBO = UserController.getInstance().get(db, new ObjectId(id));

            UserAO userAO = JsonTranslator.getInstance().fromJson(bytesToString((byte[]) requestContent), UserAO.class);
            userAO.toBO(userBO);

            UserController.getInstance().save(db, userBO);

            // Return response
            return new ApiResult(this.getAuthenticationToken(), JSON_CONTENT_TYPE, new UserAO(userBO));
        }
        catch (Exception ex)
        {
            return new ApiResult(HttpResponseStatus.BAD_REQUEST, ex);
        }
    }

    /**
     * Read. Anyone is allowed to get a list of users. This helps the client side link usernames with display names and 
     * gravatars. However, unless you are the system administrator, you don't get roles and email addresses.
     * 
     * @throws Exception
     */
    @Override
    public ApiResult processGet() throws Exception
    {
        try
        {
            DB db = MongoConnection.getInstance().getConnection();
            Object responseContent = null;
            boolean isSysAdmin = this.getAuthenticatedUser().isSystemAdministrator();

            if (this.getUriPathParameters() == null || this.getUriPathParameters().length == 0)
            {
                UserListCriteria criteria = new UserListCriteria();
                this.loadBaseListCriteriaParameters(criteria);

                criteria.setUsernamePattern(this.getUriQueryStringParameter(USERNAME_URI_QUERYSTRING_PARAMETER_NAME,
                        true));

                criteria.setEmailAddressPattern(this.getUriQueryStringParameter(
                        EMAIL_ADDRESS_URI_QUERYSTRING_PARAMETER_NAME, true));

                criteria.setRole(this.getUriQueryStringParameter(ROLE_URI_QUERYSTRING_PARAMETER_NAME, true));

                String status = this.getUriQueryStringParameter(STATUS_URI_QUERYSTRING_PARAMETER_NAME, true);
                if (!StringUtils.isBlank(status))
                {
                    criteria.setStatus(Enum.valueOf(Status.class, status));
                }

                ArrayList<UserBO> boList = UserController.getInstance().getList(db, criteria);
                if (!boList.isEmpty())
                {
                    ArrayList<UserAO> aoList = new ArrayList<UserAO>();
                    for (UserBO userBO : boList)
                    {
                        aoList.add(new UserAO(userBO, isSysAdmin));
                    }
                    responseContent = aoList.toArray(new UserAO[] {});

                    ApiResult result = new ApiResult(this.getAuthenticationToken(), JSON_CONTENT_TYPE, responseContent);
                    if (criteria.getDoPageCount())
                    {
                        result.getHeaders().put(PAGE_COUNT_HEADER, new Integer(criteria.getPageCount()).toString());
                    }
                    return result;
                }
            }
            else
            {
                // Get specific user
                String id = this.getUriPathParameters()[ID_URI_PATH_PARAMETER_INDEX];
                responseContent = new UserAO(UserController.getInstance().get(db, new ObjectId(id)), isSysAdmin);
            }
            return new ApiResult(this.getAuthenticationToken(), JSON_CONTENT_TYPE, responseContent);
        }
        catch (Exception ex)
        {
            return new ApiResult(HttpResponseStatus.BAD_REQUEST, ex);
        }
    }
}
