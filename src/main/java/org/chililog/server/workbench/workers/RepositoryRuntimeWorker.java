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

package org.chililog.server.workbench.workers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.naming.OperationNotSupportedException;

import org.apache.commons.lang.StringUtils;
import org.chililog.server.common.ChiliLogException;
import org.chililog.server.data.MongoConnection;
import org.chililog.server.data.MongoJsonSerializer;
import org.chililog.server.data.RepositoryEntryController;
import org.chililog.server.data.RepositoryListCriteria;
import org.chililog.server.data.UserBO;
import org.chililog.server.data.RepositoryListCriteria.QueryType;
import org.chililog.server.engine.Repository;
import org.chililog.server.engine.RepositoryService;
import org.chililog.server.workbench.Strings;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBObject;

/**
 * <p>
 * Repository Runtime Info worker provides the following services to manage repositories at run time:
 * <ul>
 * <li>start all - HTTP POST /api/repositories?action=start</li>
 * <li>start one - HTTP POST /api/repositories/{id}?action=start</li>
 * <li>stop all - HTTP POST /api/repositories?action=stop</li>
 * <li>stop one - HTTP POST /api/repositories/{id}?action=stop</li>
 * <li>reload all - HTTP POST /api/repositories?action=reload</li>
 * <li>read all - HTTP GET /api/repositories</li>
 * <li>read one - HTTP GET /api/repositories/{id}</li>
 * <li>read entry - HTTP GET /api/repositories/{id}/entries?query_type=find</li>
 * </p>
 * <p>
 * Runtime information refers to the current status of an instance of a repository. 
 * </p>
 */
public class RepositoryRuntimeWorker extends Worker
{
    public static final String ACTION_URI_QUERYSTRING_PARAMETER_NAME = "action";
    public static final String START_OPERATION = "start";
    public static final String STOP_OPERATION = "stop";
    public static final String RELOAD_OPERATION = "reload";

    public static final String ENTRY_QUERY_TYPE_URI_QUERYSTRING_PARAMETER_NAME = "query_type";
    public static final String ENTRY_QUERY_FIELDS_URI_QUERYSTRING_PARAMETER_NAME = "fields";
    public static final String ENTRY_QUERY_KEYWORD_USAGE_URI_QUERYSTRING_PARAMETER_NAME = "keyword_usage";
    public static final String ENTRY_QUERY_KEYWORDS_URI_QUERYSTRING_PARAMETER_NAME = "keywords";
    public static final String ENTRY_QUERY_CONDITIONS_URI_QUERYSTRING_PARAMETER_NAME = "conditions";
    public static final String ENTRY_QUERY_ORDER_BY_URI_QUERYSTRING_PARAMETER_NAME = "orderBy";
    public static final String ENTRY_QUERY_INITIAL_URI_QUERYSTRING_PARAMETER_NAME = "initial";
    public static final String ENTRY_QUERY_REDUCE_URI_QUERYSTRING_PARAMETER_NAME = "reduce";
    public static final String ENTRY_QUERY_FINALIZE_URI_QUERYSTRING_PARAMETER_NAME = "finalize";

    public static final String ENTRY_QUERY_TYPE_HEADER_NAME = "X-ChiliLog-Query-Type";
    public static final String ENTRY_QUERY_FIELDS_HEADER_NAME = "X-ChiliLog-Fields";
    public static final String ENTRY_QUERY_KEYWORDS_HEADER_NAME = "X-ChiliLog-Keywords";
    public static final String ENTRY_QUERY_KEYWORD_USAGE_HEADER_NAME = "X-ChiliLog-Keywords-Usage";
    public static final String ENTRY_QUERY_CONDITIONS_HEADER_NAME = "X-ChiliLog-Conditions";
    public static final String ENTRY_QUERY_ORDER_BY_HEADER_NAME = "X-ChiliLog-Order-By";
    public static final String ENTRY_QUERY_INITIAL_HEADER_NAME = "X-ChiliLog-Initial";
    public static final String ENTRY_QUERY_REDUCE_HEADER_NAME = "X-ChiliLog-Reduce";
    public static final String ENTRY_QUERY_FINALIZE_HEADER_NAME = "X-ChiliLog-Finalize";

    /**
     * Constructor
     */
    public RepositoryRuntimeWorker(HttpRequest request)
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
     * Let's validate if the user is able to access these functions
     */
    @Override
    protected ApiResult validateAuthenticatedUserRole()
    {
        // Do checks when we execute
        return new ApiResult();
    }

    /**
     * Start
     * 
     * @throws Exception
     */
    @Override
    public ApiResult processPost(Object requestContent) throws Exception
    {
        try
        {
            UserBO user = this.getAuthenticatedUser();
            String action = this.getUriQueryStringParameter(ACTION_URI_QUERYSTRING_PARAMETER_NAME, false);
            Object responseContent = null;

            if (this.getUriPathParameters() == null || this.getUriPathParameters().length == 0)
            {
                // Start/Stop/Reload all
                // Only available to system administrators

                if (!user.isSystemAdministrator())
                {
                    return new ApiResult(HttpResponseStatus.UNAUTHORIZED, new ChiliLogException(
                            Strings.NOT_AUTHORIZED_ERROR));
                }

                if (action.equalsIgnoreCase(START_OPERATION))
                {
                    RepositoryService.getInstance().start(false);
                }
                else if (action.equalsIgnoreCase(STOP_OPERATION))
                {
                    RepositoryService.getInstance().stop();
                }
                else if (action.equalsIgnoreCase(RELOAD_OPERATION))
                {
                    RepositoryService.getInstance().loadRepositories();
                }
                else
                {
                    throw new UnsupportedOperationException(String.format("Action '%s' not supported.", action));
                }

                Repository[] list = RepositoryService.getInstance().getRepositories();
                if (list != null && list.length > 0)
                {
                    ArrayList<RepositoryStatusAO> aoList = new ArrayList<RepositoryStatusAO>();
                    for (Repository repo : list)
                    {
                        aoList.add(new RepositoryStatusAO(repo));
                    }

                    if (!aoList.isEmpty())
                    {
                        responseContent = aoList.toArray(new RepositoryStatusAO[] {});
                    }
                }
            }
            else
            {
                // Start/Stop/Reload specific one
                // Only available to system administrators and repo admin
                String id = this.getUriPathParameters()[ID_URI_PATH_PARAMETER_INDEX];
                Repository repo = RepositoryService.getInstance().getRepository(id);

                if (!user.isSystemAdministrator() && !user.hasRole(repo.getRepoInfo().getAdministratorRoleName()))
                {
                    return new ApiResult(HttpResponseStatus.UNAUTHORIZED, new ChiliLogException(
                            Strings.NOT_AUTHORIZED_ERROR));
                }

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

                // Return the status of the repository on which the action was performed
                responseContent = new RepositoryStatusAO(repo);
            }

            // Return response
            return new ApiResult(this.getAuthenticationToken(), JSON_CONTENT_TYPE, responseContent);
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
    @SuppressWarnings("rawtypes")
    @Override
    public ApiResult processGet() throws Exception
    {
        try
        {
            UserBO user = this.getAuthenticatedUser();
            List<String> allowedRepositories = Arrays.asList(this.getAuthenticatedUserAllowedRepository());

            DB db = MongoConnection.getInstance().getConnection();
            Object responseContent = null;

            // Get info on all repositories
            // HTTP GET /api/repositories
            if (this.getUriPathParameters() == null || this.getUriPathParameters().length == 0)
            {
                Repository[] list = RepositoryService.getInstance().getRepositories();
                if (list != null && list.length > 0)
                {
                    ArrayList<RepositoryStatusAO> aoList = new ArrayList<RepositoryStatusAO>();
                    for (Repository repo : list)
                    {
                        if (user.isSystemAdministrator() || allowedRepositories.contains(repo.getRepoInfo().getName()))
                        {
                            aoList.add(new RepositoryStatusAO(repo));
                        }
                    }

                    if (!aoList.isEmpty())
                    {
                        responseContent = aoList.toArray(new RepositoryStatusAO[] {});
                    }
                }
            }
            else if (this.getUriPathParameters().length == 1)
            {
                // Get info on specified repository
                // HTTP GET /api/repositories/{id}
                String id = this.getUriPathParameters()[ID_URI_PATH_PARAMETER_INDEX];
                Repository repo = RepositoryService.getInstance().getRepository(id);
                if (repo != null
                        && (user.isSystemAdministrator() || allowedRepositories.contains(repo.getRepoInfo().getName())))
                {
                    responseContent = new RepositoryStatusAO(repo);
                }
                else
                {
                    // Assume not found
                    throw new ChiliLogException(Strings.REPOSITORY_NOT_FOUND_ERROR, id);
                }
            }
            else if (this.getUriPathParameters().length == 2)
            {
                // HTTP GET /api/repositories/{id}/entries?query_type=find
                // Get entries for a specific repository
                String id = this.getUriPathParameters()[ID_URI_PATH_PARAMETER_INDEX];
                Repository repo = RepositoryService.getInstance().getRepository(id);
                if (repo == null)
                {
                    throw new ChiliLogException(Strings.REPOSITORY_NOT_FOUND_ERROR, id);
                }
                if (!user.isSystemAdministrator() && !allowedRepositories.contains(repo.getRepoInfo().getName()))
                {
                    // Assume not found
                    throw new ChiliLogException(Strings.REPOSITORY_NOT_FOUND_ERROR, id);
                }

                // Load criteria
                QueryType queryType = Enum.valueOf(
                        QueryType.class,
                        this.getQueryStringOrHeaderValue(ENTRY_QUERY_TYPE_URI_QUERYSTRING_PARAMETER_NAME,
                                ENTRY_QUERY_TYPE_HEADER_NAME, false).toUpperCase());
                RepositoryListCriteria criteria = loadCriteria();

                // Convert to JSON ourselves because this is not a simple AO object.
                // mongoDB object JSON serialization required
                StringBuilder json = new StringBuilder();

                // Get controller and execute query
                RepositoryEntryController controller = RepositoryEntryController.getInstance(repo.getRepoInfo());
                if (queryType == QueryType.FIND)
                {
                    ArrayList<DBObject> list = controller.executeFindQuery(db, criteria);

                    if (list != null && !list.isEmpty())
                    {
                        MongoJsonSerializer.serialize(new BasicDBObject("find", list), json);
                    }
                }
                else if (queryType == QueryType.COUNT)
                {
                    int count = controller.executeCountQuery(db, criteria);
                    MongoJsonSerializer.serialize(new BasicDBObject("count", count), json);
                }
                else if (queryType == QueryType.DISTINCT)
                {
                    List l = controller.executeDistinctQuery(db, criteria);
                    MongoJsonSerializer.serialize(new BasicDBObject("distinct", l), json);
                }
                else if (queryType == QueryType.GROUP)
                {
                    DBObject groupObject = controller.executeGroupQuery(db, criteria);
                    MongoJsonSerializer.serialize(new BasicDBObject("group", groupObject), json);
                }
                else
                {
                    throw new OperationNotSupportedException("Unsupported query type: " + queryType.toString());
                }

                // If there is no json, skip this and a 204 No Content will be returned
                if (json.length() > 0)
                {
                    responseContent = json.toString().getBytes(Worker.JSON_CHARSET);
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

    /**
     * Load our criteria from query string and headers (in case it is too big for query string)
     * 
     * @returns query criteria
     * @throws ChiliLogException
     */
    private RepositoryListCriteria loadCriteria() throws ChiliLogException
    {
        String s;

        RepositoryListCriteria criteria = new RepositoryListCriteria();
        this.loadBaseListCriteriaParameters(criteria);

        s = this.getQueryStringOrHeaderValue(ENTRY_QUERY_FIELDS_URI_QUERYSTRING_PARAMETER_NAME,
                ENTRY_QUERY_FIELDS_HEADER_NAME, true);
        if (!StringUtils.isBlank(s))
        {
            criteria.setFields(s);
        }

        s = this.getQueryStringOrHeaderValue(ENTRY_QUERY_CONDITIONS_URI_QUERYSTRING_PARAMETER_NAME,
                ENTRY_QUERY_CONDITIONS_HEADER_NAME, true);
        if (!StringUtils.isBlank(s))
        {
            criteria.setConditions(s);
        }

        s = this.getQueryStringOrHeaderValue(ENTRY_QUERY_KEYWORD_USAGE_URI_QUERYSTRING_PARAMETER_NAME,
                ENTRY_QUERY_KEYWORD_USAGE_HEADER_NAME, true);
        if (!StringUtils.isBlank(s))
        {
            criteria.setKeywordUsage(Enum.valueOf(RepositoryListCriteria.KeywordUsage.class, s));
        }

        s = this.getQueryStringOrHeaderValue(ENTRY_QUERY_KEYWORDS_URI_QUERYSTRING_PARAMETER_NAME,
                ENTRY_QUERY_KEYWORDS_HEADER_NAME, true);
        if (!StringUtils.isBlank(s))
        {
            criteria.setKeywords(s);
        }

        s = this.getQueryStringOrHeaderValue(ENTRY_QUERY_CONDITIONS_URI_QUERYSTRING_PARAMETER_NAME,
                ENTRY_QUERY_CONDITIONS_HEADER_NAME, true);
        if (!StringUtils.isBlank(s))
        {
            criteria.setConditions(s);
        }

        s = this.getQueryStringOrHeaderValue(ENTRY_QUERY_ORDER_BY_URI_QUERYSTRING_PARAMETER_NAME,
                ENTRY_QUERY_ORDER_BY_HEADER_NAME, true);
        if (!StringUtils.isBlank(s))
        {
            criteria.setOrderBy(s);
        }

        s = this.getQueryStringOrHeaderValue(ENTRY_QUERY_INITIAL_URI_QUERYSTRING_PARAMETER_NAME,
                ENTRY_QUERY_INITIAL_HEADER_NAME, true);
        if (!StringUtils.isBlank(s))
        {
            criteria.setInitial(s);
        }

        s = this.getQueryStringOrHeaderValue(ENTRY_QUERY_INITIAL_HEADER_NAME, ENTRY_QUERY_REDUCE_HEADER_NAME, true);
        if (!StringUtils.isBlank(s))
        {
            criteria.setReduceFunction(s);
        }

        s = this.getQueryStringOrHeaderValue(ENTRY_QUERY_FINALIZE_URI_QUERYSTRING_PARAMETER_NAME,
                ENTRY_QUERY_FINALIZE_HEADER_NAME, true);
        if (!StringUtils.isBlank(s))
        {
            criteria.setFinalizeFunction(s);
        }

        return criteria;
    }
}
