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

import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

import com.chililog.server.data.MongoConnection;
import com.chililog.server.data.RepositoryController;
import com.chililog.server.data.RepositoryControllerFactory;
import com.chililog.server.data.RepositoryListCriteria;
import com.chililog.server.engine.Repository;
import com.chililog.server.engine.RepositoryManager;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

/**
 * <p>
 * Repositories worker provides the following services:
 * <ul>
 * <li>start all - HTTP POST /api/repositories?action=start</li>
 * <li>start one - HTTP POST /api/repositories/{id}?action=start</li>
 * <li>stop all - HTTP POST /api/repositories?action=stop</li>
 * <li>stop one - HTTP POST /api/repositories/{id}?action=stop</li>
 * <li>reload all - HTTP POST /api/repositories?action=reload</li>
 * <li>read all - HTTP GET /api/repositories</li>
 * <li>read one - HTTP GET /api/repositories/{id}</li>
 * <li>read entry - HTTP GET /api/repositories/{id}/entries</li>
 * </p>
 */
public class RepositoriesWorker extends Worker
{
    public static final String ACTION_URI_QUERYSTRING_PARAMETER_NAME = "action";
    public static final String START_OPERATION = "start";
    public static final String STOP_OPERATION = "stop";
    public static final String RELOAD_OPERATION = "reload";
    
    /**
     * Constructor
     */
    public RepositoriesWorker(HttpRequest request)
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
        { HttpMethod.GET, HttpMethod.POST };
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
            String action = this.getUriQueryStringParameter(ACTION_URI_QUERYSTRING_PARAMETER_NAME, false);
            if (this.getUriPathParameters() == null || this.getUriPathParameters().length == 0)
            {
                if (action.equalsIgnoreCase(START_OPERATION))
                {
                    RepositoryManager.getInstance().start();
                }
                else if (action.equalsIgnoreCase(STOP_OPERATION))
                {
                    RepositoryManager.getInstance().stop();
                }
                else if (action.equalsIgnoreCase(RELOAD_OPERATION))
                {
                    RepositoryManager.getInstance().loadRepositories();
                }
                else
                {
                    throw new UnsupportedOperationException(String.format("Action '%s' not supported.", action));
                }
            }
            else
            {
                String id = this.getUriPathParameters()[ID_URI_PATH_PARAMETER_INDEX];
                Repository repo = RepositoryManager.getInstance().getRepository(id);
                
                if (action.equalsIgnoreCase(START_OPERATION))
                {
                    repo.start();
                }
                else if (action.equalsIgnoreCase(STOP_OPERATION))
                {
                    repo.stop();
                }
                else
                {
                    throw new UnsupportedOperationException(String.format("Action '%s' not supported.", action));
                }
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
                Repository[] list = RepositoryManager.getInstance().getRepositories();
                if (list != null && list.length > 0)
                {
                    ArrayList<RepositoryAO> aoList = new ArrayList<RepositoryAO>();
                    for (Repository repo : list)
                    {
                        aoList.add(new RepositoryAO(repo));
                    }
                    responseContent = aoList.toArray(new RepositoryAO[] {});
                }
            }
            else if (this.getUriPathParameters().length == 1)
            {
                // Get specified repository
                String id = this.getUriPathParameters()[ID_URI_PATH_PARAMETER_INDEX];
                Repository repo = RepositoryManager.getInstance().getRepository(id);
                responseContent = new RepositoryAO(repo);
            }
            else if (this.getUriPathParameters().length == 2)
            {
                // Get entries for a specific repository
                String id = this.getUriPathParameters()[ID_URI_PATH_PARAMETER_INDEX];
                Repository repo = RepositoryManager.getInstance().getRepository(id);

                RepositoryListCriteria criteria = new RepositoryListCriteria();
                this.loadBaseListCriteriaParameters(criteria);

                RepositoryController controller = RepositoryControllerFactory.make(repo.getRepoInfo());
                ArrayList<DBObject> list = controller.getDBObjectList(db, criteria);
                if (list != null && !list.isEmpty())
                {
                    // Convert to JSON ourselves because this is not a simple AO object
                    StringBuilder sb = new StringBuilder();
                    sb.append("[");
                    for (DBObject e : list)
                    {
                        JSON.serialize(e, sb);
                        sb.append(", ");
                    }
                    sb.setLength(sb.length() - 2);
                    sb.append("]");

                    responseContent = sb.toString().getBytes(Worker.JSON_CHARSET);
                    ApiResult result = new ApiResult(this.getAuthenticationToken(), JSON_CONTENT_TYPE, responseContent);
                    if (criteria.getDoPageCount())
                    {
                        result.getHeaders().put(PAGE_COUNT_HEADER, new Integer(criteria.getPageCount()).toString());
                    }
                    return result;
                }
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
