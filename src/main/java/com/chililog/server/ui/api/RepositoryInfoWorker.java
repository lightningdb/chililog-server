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
import com.chililog.server.ui.Strings;
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

            RepositoryInfoAO userAO = JsonTranslator.getInstance().fromJson(bytesToString((byte[]) requestContent),
                    RepositoryInfoAO.class);

            RepositoryInfoBO userBO = new RepositoryInfoBO();
            userAO.toBO(userBO);

            DB db = MongoConnection.getInstance().getConnection();
            RepositoryInfoController.getInstance().save(db, userBO);

            // Return response
            return new ApiResult(this.getAuthenticationToken(), new RepositoryInfoAO(userBO));
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
            RepositoryInfoBO userBO = RepositoryInfoController.getInstance().tryGet(db, new ObjectId(id));
            if (userBO != null)
            {
                RepositoryInfoController.getInstance().remove(db, userBO);
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
            if (requestContent == null)
            {
                throw new ChiliLogException(Strings.REQUIRED_CONTENT_ERROR);
            }

            String id = this.getUriPathParameters()[ID_URI_PATH_PARAMETER_INDEX];

            DB db = MongoConnection.getInstance().getConnection();
            RepositoryInfoBO userBO = RepositoryInfoController.getInstance().get(db, new ObjectId(id));

            RepositoryInfoAO userAO = JsonTranslator.getInstance().fromJson(bytesToString((byte[]) requestContent),
                    RepositoryInfoAO.class);
            userAO.toBO(userBO);

            RepositoryInfoController.getInstance().save(db, userBO);

            // Return response
            return new ApiResult(this.getAuthenticationToken(), new RepositoryInfoAO(userBO));
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

            if (this.getUriPathParameters() == null || this.getUriPathParameters().length == 0)
            {
                // Get list
                RepositoryInfoListCriteria criteria = new RepositoryInfoListCriteria();
                this.loadBaseListCriteriaParameters(criteria);

                criteria.setNamePattern(this.getUriQueryStringParameter(NAME_URI_QUERYSTRING_PARAMETER_NAME, true));

                ArrayList<RepositoryInfoBO> boList = RepositoryInfoController.getInstance().getList(db, criteria);
                if (!boList.isEmpty())
                {
                    ArrayList<RepositoryInfoAO> aoList = new ArrayList<RepositoryInfoAO>();
                    for (RepositoryInfoBO userBO : boList)
                    {
                        aoList.add(new RepositoryInfoAO(userBO));
                    }
                    responseContent = aoList.toArray(new RepositoryInfoAO[] {});
                }
            }
            else
            {
                // Get specific user
                String id = this.getUriPathParameters()[ID_URI_PATH_PARAMETER_INDEX];

                responseContent = new RepositoryInfoAO(RepositoryInfoController.getInstance().get(db, new ObjectId(id)));
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
