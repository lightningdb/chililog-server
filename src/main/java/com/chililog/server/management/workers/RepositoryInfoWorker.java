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

import java.util.ArrayList;

import org.bson.types.ObjectId;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

import com.chililog.server.common.ChiliLogException;
import com.chililog.server.common.JsonTranslator;
import com.chililog.server.data.MongoConnection;
import com.chililog.server.data.RepositoryInfoBO;
import com.chililog.server.data.RepositoryInfoController;
import com.chililog.server.data.RepositoryInfoListCriteria;
import com.chililog.server.data.UserBO;
import com.chililog.server.management.Strings;
import com.mongodb.DB;

/**
 * <p>
 * Repository Information Worker provides the following API services:
 * <ul>
 * <li>create - HTTP POST /api/repository_info</li>
 * <li>read all - HTTP GET /api/repository_info</li>
 * <li>read one - HTTP GET /api/repository_info/{id}</li>
 * <li>update - HTTP PUT /api/repository_info/{id}</li>
 * <li>delete - HTTP DELETE /api/repository_info/{id}</li>
 * </p>
 */
public class RepositoryInfoWorker extends Worker
{
    public static final String NAME_URI_QUERYSTRING_PARAMETER_NAME = "name";

    /**
     * Constructor
     */
    public RepositoryInfoWorker(HttpRequest request)
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

            // Cannot POST or DELETE unless system administrator
            if (requestMethod == HttpMethod.POST || requestMethod == HttpMethod.DELETE)
            {
                throw new ChiliLogException(Strings.NOT_AUTHORIZED_ERROR);
            }

            // Do checks for PUT and GET when we execute
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

            RepositoryInfoAO repoInfoAO = JsonTranslator.getInstance().fromJson(bytesToString((byte[]) requestContent),
                    RepositoryInfoAO.class);

            RepositoryInfoBO repoInfoBO = new RepositoryInfoBO();
            repoInfoAO.toBO(repoInfoBO);

            DB db = MongoConnection.getInstance().getConnection();
            RepositoryInfoController.getInstance().save(db, repoInfoBO);

            // Return response
            return new ApiResult(this.getAuthenticationToken(), JSON_CONTENT_TYPE, new RepositoryInfoAO(repoInfoBO));
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
            RepositoryInfoBO repoInfoBO = RepositoryInfoController.getInstance().tryGet(db, new ObjectId(id));
            if (repoInfoBO != null)
            {
                RepositoryInfoController.getInstance().remove(db, repoInfoBO);
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
            RepositoryInfoBO repoInfoBO = RepositoryInfoController.getInstance().get(db, new ObjectId(id));

            // Only system admin and repo admin for this repo can update details
            UserBO user = this.getAuthenticatedUser();
            if (!user.isSystemAdministrator() && !user.hasRole(repoInfoBO.getAdministratorRoleName()))
            {
                return new ApiResult(HttpResponseStatus.UNAUTHORIZED, new ChiliLogException(
                        Strings.NOT_AUTHORIZED_ERROR));
            }

            RepositoryInfoAO repoInfoAO = JsonTranslator.getInstance().fromJson(bytesToString((byte[]) requestContent),
                    RepositoryInfoAO.class);
            repoInfoAO.toBO(repoInfoBO);

            RepositoryInfoController.getInstance().save(db, repoInfoBO);

            // Return response
            return new ApiResult(this.getAuthenticationToken(), JSON_CONTENT_TYPE, new RepositoryInfoAO(repoInfoBO));
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
            UserBO user = this.getAuthenticatedUser();

            if (this.getUriPathParameters() == null || this.getUriPathParameters().length == 0)
            {
                // Get list
                RepositoryInfoListCriteria criteria = new RepositoryInfoListCriteria();
                this.loadBaseListCriteriaParameters(criteria);

                criteria.setNamePattern(this.getUriQueryStringParameter(NAME_URI_QUERYSTRING_PARAMETER_NAME, true));

                // if not system admin, limit result to those repo that the user has access
                if (!user.isSystemAdministrator())
                {
                    criteria.setNameRestrictions(this.getAuthenticatedUserAllowedRepository());
                }

                ArrayList<RepositoryInfoBO> boList = RepositoryInfoController.getInstance().getList(db, criteria);
                if (!boList.isEmpty())
                {
                    ArrayList<RepositoryInfoAO> aoList = new ArrayList<RepositoryInfoAO>();
                    for (RepositoryInfoBO repoInfoBO : boList)
                    {
                        aoList.add(new RepositoryInfoAO(repoInfoBO));
                    }
                    responseContent = aoList.toArray(new RepositoryInfoAO[] {});
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
                // Get specific repository - only allowed for system admin and those who have permission
                String id = this.getUriPathParameters()[ID_URI_PATH_PARAMETER_INDEX];
                RepositoryInfoBO repoInfoBO = RepositoryInfoController.getInstance().get(db, new ObjectId(id));

                if (!user.isSystemAdministrator() && !user.hasRole(repoInfoBO.getAdministratorRoleName())
                        && !user.hasRole(repoInfoBO.getWorkbenchRoleName()))
                {
                    return new ApiResult(HttpResponseStatus.UNAUTHORIZED, new ChiliLogException(
                            Strings.NOT_AUTHORIZED_ERROR));
                }

                responseContent = new RepositoryInfoAO(repoInfoBO);
            }

            // Return response
            return new ApiResult(this.getAuthenticationToken(), JSON_CONTENT_TYPE, responseContent);
        }
        catch (Exception ex)
        {
            return new ApiResult(HttpResponseStatus.BAD_REQUEST, ex);
        }
    }
}
