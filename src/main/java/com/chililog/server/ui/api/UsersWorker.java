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

import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

import com.chililog.server.common.JsonTranslator;
import com.chililog.server.data.MongoConnection;
import com.chililog.server.data.UserBO;
import com.chililog.server.data.UserBO.Status;
import com.chililog.server.data.UserController;
import com.chililog.server.data.UserListCriteria;
import com.mongodb.DB;

/**
 * <p>
 * Authentication API handles:
 * <ul>
 * <li>create - HTTP POST /api/Users</li>
 * <li>read all - HTTP GET /api/Users</li>
 * <li>read one - HTTP GET /api/Users/{id}</li>
 * <li>update - HTTP PUT /api/Users/{id}</li>
 * <li>delete - HTTP DELETE /api/Users/{id}</li>
 * </p>
 */
public class UsersWorker extends Worker
{
    public static final String USERNAME_URI_QUERYSTRING_PARAMETER_NAME = "username";
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
     * Create
     * 
     * @throws Exception
     */
    @Override
    public ApiResult processPost(Object requestContent) throws Exception
    {
        try
        {
            UserAO userAO = JsonTranslator.getInstance().fromJson(bytesToString((byte[]) requestContent), UserAO.class);

            UserBO userBO = new UserBO();
            userAO.toBO(userBO);

            DB db = MongoConnection.getInstance().getConnection();
            UserController.getInstance().save(db, userBO);

            // Return response
            return new ApiResult(this.getAuthenticationToken(), new UserAO(userBO));
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
            return new ApiResult(this.getAuthenticationToken(), null);
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
            String id = this.getUriPathParameters()[ID_URI_PATH_PARAMETER_INDEX];

            DB db = MongoConnection.getInstance().getConnection();
            UserBO userBO = UserController.getInstance().get(db, new ObjectId(id));

            UserAO userAO = JsonTranslator.getInstance().fromJson(bytesToString((byte[]) requestContent), UserAO.class);
            userAO.toBO(userBO);

            UserController.getInstance().save(db, userBO);

            // Return response
            return new ApiResult(this.getAuthenticationToken(), null);
        }
        catch (Exception ex)
        {
            return new ApiResult(HttpResponseStatus.BAD_REQUEST, ex);
        }
    }

    /**
     * Read
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

            if (this.getUriPathParameters().length == 0)
            {
                // Get list
                UserListCriteria criteria = new UserListCriteria();
                this.loadBaseListCriteriaParameters(criteria);

                criteria.setUsernamePattern(this.getUriQueryStringParameter(USERNAME_URI_QUERYSTRING_PARAMETER_NAME,
                        true));

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
                        aoList.add(new UserAO(userBO));
                    }
                    responseContent = aoList.toArray(new UserAO[] {});
                }
            }
            else
            {
                // Get specific user
                String id = this.getUriPathParameters()[ID_URI_PATH_PARAMETER_INDEX];

                responseContent = UserController.getInstance().get(db, new ObjectId(id));
            }

            // Return response
            return new ApiResult(this.getAuthenticationToken(), responseContent);
        }
        catch (Exception ex)
        {
            return new ApiResult(HttpResponseStatus.BAD_REQUEST, ex);
        }
    }
}
