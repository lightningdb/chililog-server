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
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.bson.types.ObjectId;
import org.chililog.server.common.ChiliLogException;
import org.chililog.server.common.JsonTranslator;
import org.chililog.server.data.MongoConnection;
import org.chililog.server.data.RepositoryConfigBO;
import org.chililog.server.data.RepositoryConfigController;
import org.chililog.server.data.RepositoryConfigListCriteria;
import org.chililog.server.data.UserBO;
import org.chililog.server.data.UserController;
import org.chililog.server.data.UserListCriteria;
import org.chililog.server.workbench.Strings;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

import com.mongodb.DB;

/**
 * <p>
 * Repository Configuration Worker provides the following API services to define and configure repositories:
 * <ul>
 * <li>create - HTTP POST /api/repository_info</li>
 * <li>read all - HTTP GET /api/repository_info</li>
 * <li>read one - HTTP GET /api/repository_info/{id}</li>
 * <li>update - HTTP PUT /api/repository_info/{id}</li>
 * <li>delete - HTTP DELETE /api/repository_info/{id}</li>
 * </p>
 * <p>
 * Meta information refers to the information that defines a repository - name, parsers, fields, etc.
 * </p>
 */
public class RepositoryConfigWorker extends Worker {

    public static final String NAME_URI_QUERYSTRING_PARAMETER_NAME = "name";

    private static UserBO[] _userListCache = null;

    private static Date _userListCacheExpiry = new Date();

    /**
     * Constructor
     */
    public RepositoryConfigWorker(HttpRequest request) {
        super(request);
        return;
    }

    /**
     * Can only create and delete sessions
     */
    @Override
    public HttpMethod[] getSupportedMethods() {
        return new HttpMethod[] { HttpMethod.POST, HttpMethod.DELETE, HttpMethod.GET, HttpMethod.PUT };
    }

    /**
     * Let's validate if the user is able to access these functions
     */
    @Override
    protected ApiResult validateAuthenticatedUserRole() {
        HttpMethod requestMethod = this.getRequest().getMethod();
        try {
            UserBO user = this.getAuthenticatedUser();

            // Administrators can do it all
            if (user.isSystemAdministrator()) {
                return new ApiResult();
            }

            // Cannot POST or DELETE unless system administrator
            if (requestMethod == HttpMethod.POST || requestMethod == HttpMethod.DELETE) {
                throw new ChiliLogException(Strings.NOT_AUTHORIZED_ERROR);
            }

            // Do checks for PUT and GET when we execute
            return new ApiResult();
        } catch (Exception ex) {
            return new ApiResult(HttpResponseStatus.UNAUTHORIZED, ex);
        }
    }

    /**
     * Create
     * 
     * @throws Exception
     */
    @Override
    public ApiResult processPost(Object requestContent) throws Exception {
        try {
            if (requestContent == null) {
                throw new ChiliLogException(Strings.REQUIRED_CONTENT_ERROR);
            }

            RepositoryConfigAO repoConfigAO = JsonTranslator.getInstance().fromJson(
                    bytesToString((byte[]) requestContent), RepositoryConfigAO.class);

            RepositoryConfigBO repoConfigBO = new RepositoryConfigBO();
            repoConfigAO.toBO(repoConfigBO);

            DB db = MongoConnection.getInstance().getConnection();
            RepositoryConfigController.getInstance().save(db, repoConfigBO);

            // Return response
            return new ApiResult(this.getAuthenticationToken(), JSON_CONTENT_TYPE, new RepositoryConfigAO(repoConfigBO,
                    getAllUsers(db)));
        } catch (Exception ex) {
            return new ApiResult(HttpResponseStatus.BAD_REQUEST, ex);
        }
    }

    /**
     * Delete
     * 
     * @throws Exception
     */
    @Override
    public ApiResult processDelete() throws Exception {
        try {
            String id = this.getUriPathParameters()[ID_URI_PATH_PARAMETER_INDEX];

            DB db = MongoConnection.getInstance().getConnection();
            RepositoryConfigBO repoConfigBO = RepositoryConfigController.getInstance().tryGet(db, new ObjectId(id));
            if (repoConfigBO != null) {
                RepositoryConfigController.getInstance().remove(db, repoConfigBO);
            }

            // Return response
            return new ApiResult(this.getAuthenticationToken(), null, null);
        } catch (Exception ex) {
            return new ApiResult(HttpResponseStatus.BAD_REQUEST, ex);
        }
    }

    /**
     * Update
     * 
     * @throws Exception
     */
    @Override
    public ApiResult processPut(Object requestContent) throws Exception {
        try {
            if (requestContent == null) {
                throw new ChiliLogException(Strings.REQUIRED_CONTENT_ERROR);
            }

            String id = this.getUriPathParameters()[ID_URI_PATH_PARAMETER_INDEX];

            DB db = MongoConnection.getInstance().getConnection();
            RepositoryConfigBO repoConfigBO = RepositoryConfigController.getInstance().get(db, new ObjectId(id));

            // Only system admin and repo admin for this repo can update details
            UserBO user = this.getAuthenticatedUser();
            if (!user.isSystemAdministrator() && !user.hasRole(repoConfigBO.getAdministratorRoleName())) {
                return new ApiResult(HttpResponseStatus.UNAUTHORIZED, new ChiliLogException(
                        Strings.NOT_AUTHORIZED_ERROR));
            }

            RepositoryConfigAO repoConfigAO = JsonTranslator.getInstance().fromJson(
                    bytesToString((byte[]) requestContent), RepositoryConfigAO.class);
            repoConfigAO.toBO(repoConfigBO);

            RepositoryConfigController.getInstance().save(db, repoConfigBO);

            // Return response
            return new ApiResult(this.getAuthenticationToken(), JSON_CONTENT_TYPE, new RepositoryConfigAO(repoConfigBO,
                    getAllUsers(db)));
        } catch (Exception ex) {
            return new ApiResult(HttpResponseStatus.BAD_REQUEST, ex);
        }
    }

    /**
     * Read
     * 
     * @throws Exception
     */
    @Override
    public ApiResult processGet() throws Exception {
        try {
            DB db = MongoConnection.getInstance().getConnection();
            Object responseContent = null;
            UserBO user = this.getAuthenticatedUser();

            if (this.getUriPathParameters() == null || this.getUriPathParameters().length == 0) {
                // Get list
                RepositoryConfigListCriteria criteria = new RepositoryConfigListCriteria();
                this.loadBaseListCriteriaParameters(criteria);

                criteria.setNamePattern(this.getUriQueryStringParameter(NAME_URI_QUERYSTRING_PARAMETER_NAME, true));

                // if not system admin, limit result to those repo that the user has access
                if (!user.isSystemAdministrator()) {
                    criteria.setNameRestrictions(this.getAuthenticatedUserAllowedRepository());
                }

                ArrayList<RepositoryConfigBO> boList = RepositoryConfigController.getInstance().getList(db, criteria);
                if (!boList.isEmpty()) {
                    ArrayList<RepositoryConfigAO> aoList = new ArrayList<RepositoryConfigAO>();
                    for (RepositoryConfigBO repoConfigBO : boList) {
                        aoList.add(new RepositoryConfigAO(repoConfigBO, getAllUsers(db)));
                    }
                    responseContent = aoList.toArray(new RepositoryConfigAO[] {});
                    ApiResult result = new ApiResult(this.getAuthenticationToken(), JSON_CONTENT_TYPE, responseContent);
                    if (criteria.getDoPageCount()) {
                        result.getHeaders().put(PAGE_COUNT_HEADER, new Integer(criteria.getPageCount()).toString());
                    }
                    return result;
                }
            } else {
                // Get specific repository - only allowed for system admin and those who have permission
                String id = this.getUriPathParameters()[ID_URI_PATH_PARAMETER_INDEX];
                RepositoryConfigBO repoConfigBO = RepositoryConfigController.getInstance().get(db, new ObjectId(id));

                if (!user.isSystemAdministrator() && !user.hasRole(repoConfigBO.getAdministratorRoleName())
                        && !user.hasRole(repoConfigBO.getWorkbenchRoleName())) {
                    return new ApiResult(HttpResponseStatus.UNAUTHORIZED, new ChiliLogException(
                            Strings.NOT_AUTHORIZED_ERROR));
                }

                responseContent = new RepositoryConfigAO(repoConfigBO, getAllUsers(db));
            }

            // Return response
            return new ApiResult(this.getAuthenticationToken(), JSON_CONTENT_TYPE, responseContent);
        } catch (Exception ex) {
            return new ApiResult(HttpResponseStatus.BAD_REQUEST, ex);
        }
    }

    /**
     * <p>
     * Get a list of all users so we can figure out which user can access which repository
     * </p>
     * <p>
     * Note that we cache for 30 seconds.
     * </p>
     * 
     * @param db
     *            Database connection
     * @return List of all users
     * @throws ChiliLogException
     */
    private static synchronized UserBO[] getAllUsers(DB db) throws ChiliLogException {
        if (_userListCache == null || _userListCacheExpiry.before(new Date())) {
            UserListCriteria criteria = new UserListCriteria();
            ArrayList<UserBO> list = UserController.getInstance().getList(db, criteria);
            _userListCache = list.toArray(new UserBO[] {});

            GregorianCalendar cal = new GregorianCalendar();
            cal.add(Calendar.SECOND, 10);
            _userListCacheExpiry = cal.getTime();
        }

        return _userListCache;
    }
}
