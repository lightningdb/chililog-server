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

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;

import com.chililog.server.common.ChiliLogException;
import com.chililog.server.data.ListCriteria;
import com.chililog.server.data.MongoConnection;
import com.chililog.server.data.UserBO;
import com.chililog.server.data.UserController;
import com.chililog.server.management.Strings;

/**
 * <p>
 * Base API class. Contains the interface for <code>HttpRequestHandler</code> to use as well as common methods.
 * </p>
 * <p>
 * All services classes are designed to be single use (re-entry for more than one request is not supported) and single
 * threaded.
 * </p>
 * <p>
 * The request URL is assumed to follow the CRUD convention as specified in
 * http://archive.msdn.microsoft.com/cannonicalRESTEntity.
 * <ul>
 * <li><code>POST /api/{WorkerName}</code> - Create an object</li>
 * <li><code>GET /api/{WorkerName}</code> - Read a list of objects</li>
 * <li><code>GET /api/{WorkerName}/{id} - Read a specific object identified by the {id}</code></li>
 * <li><code>PUT /api/{WorkerName}/{id}</code> - Update a specific object identified by {id}</li>
 * <li><code>DELETE /api/{WorkerName}/{id}</code> - Delete a specific object identified by {id}</li>
 * </ul>
 * </p>
 */
public abstract class Worker
{
    private HttpRequest _request = null;
    private Map<String, List<String>> _uriQueryStringParameters;
    private String[] _uriPathParameters = null;
    private ContentIOStyle _requestContentIOStyle = ContentIOStyle.ByteArray;
    private AuthenticationTokenAO _authenticationToken = null;
    private UserBO _authenticatedUser = null;

    public static final int ID_URI_PATH_PARAMETER_INDEX = 0;

    public static final String RECORDS_PER_PAGE_URI_QUERYSTRING_PARAMETER_NAME = "records_per_page";
    public static final String START_PAGE_URI_QUERYSTRING_PARAMETER_NAME = "start_page";
    public static final String DO_PAGE_COUNT_URI_QUERYSTRING_PARAMETER_NAME = "do_page_count";

    public static final String RECORDS_PER_PAGE_HEADER_NAME = "X-Chililog-Records-Per-Page";
    public static final String START_PAGE_HEADER_NAME = "X-Chililog-Start-Page";
    public static final String DO_PAGE_COUNT_HEADER_NAME = "X-Chililog-Do-Page-Count";

    public static final String AUTHENTICATION_TOKEN_HEADER = "X-Chililog-Authentication";
    public static final String AUTHENTICATION_SERVER_VERSION = "X-Chililog-Version";
    public static final String AUTHENTICATION_SERVER_BUILD_TIMESTAMP = "X-Chililog-Build-Timestamp";
    public static final String PAGE_COUNT_HEADER = "X-Chililog-PageCount";

    public static final String JSON_CONTENT_TYPE = "text/json; charset=UTF-8";
    public static final String JSON_CHARSET = "UTF-8";

    /**
     * Constructor
     * 
     * @param request
     *            HTTP request to process
     */
    public Worker(HttpRequest request)
    {
        _request = request;
    }

    /**
     * Returns the HTTP request that is being processed
     */
    public HttpRequest getRequest()
    {
        return _request;
    }

    /**
     * Determines how the HTTP request content is to be passed into <code>process()</code>.
     */
    public ContentIOStyle getRequestContentIOStyle()
    {
        return _requestContentIOStyle;
    }

    protected void setRequestContentIOStyle(ContentIOStyle requestContentIOStyle)
    {
        _requestContentIOStyle = requestContentIOStyle;
    }

    /**
     * Returns an array of supported HTTP request methods.
     */
    public HttpMethod[] getSupportedMethods()
    {
        return new HttpMethod[]
        { HttpMethod.GET, HttpMethod.PUT, HttpMethod.POST, HttpMethod.DELETE };
    }

    /**
     * <p>
     * Returns array of path parameters.
     * </p>
     * <p>
     * The query string of URI <code>/api/WorkName/123456789?hello=world</code> can be accessed as follows:
     * </p>
     * 
     * <pre>
     * assertEquals(this.getUriPathParameters()[0], &quot;123456789&quot;);
     * </pre>
     */
    public String[] getUriPathParameters()
    {
        return _uriPathParameters;
    }

    /**
     * Returns the specified parameter from the uri path
     * 
     * @param paramterName
     *            Name of paramter to use in error message
     * @param parameterIndex
     *            Index of the parameter. 0 is the index of the parameter after the worker name in the uri.
     * @return Value of specified parameter
     * @throws ChiliLogException
     */
    public String getUriPathParameter(String paramterName, int parameterIndex) throws ChiliLogException
    {
        if (_uriPathParameters == null || parameterIndex < 0 || parameterIndex >= _uriPathParameters.length)
        {
            throw new ChiliLogException(Strings.URI_PATH_PARAMETER_ERROR, paramterName, _request.getUri());
        }
        return _uriPathParameters[parameterIndex];
    }

    /**
     * <p>
     * Returns the Query String parameters
     * </p>
     * <p>
     * The query string of URI <code>/api/WorkName?hello=world</code> can be accessed as follows:
     * </p>
     * 
     * <pre>
     * assertEquals(this.getUriQueryStringParameters().get(&quot;hello&quot;).get(0), &quot;world&quot;);
     * </pre>
     */
    public Map<String, List<String>> getUriQueryStringParameters()
    {
        return _uriQueryStringParameters;
    }

    /**
     * Returns the specified query string parameter
     * 
     * @param parameterName
     *            Name of query string parameter
     * @param isOptional
     *            if True then exception will NOT be thrown if parameter does not exist
     * @return Query string parameter value
     * @throws ChiliLogException
     */
    public String getUriQueryStringParameter(String parameterName, boolean isOptional) throws ChiliLogException
    {
        String value = null;
        if (_uriQueryStringParameters.containsKey(parameterName))
        {
            List<String> l = _uriQueryStringParameters.get(parameterName);
            if (l != null && !l.isEmpty())
            {
                value = l.get(0);
            }
        }

        if (StringUtils.isBlank(value))
        {
            if (isOptional)
            {
                return null;
            }
            throw new ChiliLogException(Strings.URI_QUERY_STRING_PARAMETER_ERROR, parameterName, _request.getUri());
        }

        return value;
    }

    /**
     * Load base list criteria values, "records per page", "start page" and "do page count", from the query string
     * parameters
     * 
     * @param listCritiera
     *            list criteria to load query string values into
     * @throws ChiliLogException
     */
    protected void loadBaseListCriteriaParameters(ListCriteria listCritiera) throws ChiliLogException
    {
        String recordsPerPage = this.getQueryStringOrHeaderValue(RECORDS_PER_PAGE_URI_QUERYSTRING_PARAMETER_NAME,
                RECORDS_PER_PAGE_HEADER_NAME, true);
        if (!StringUtils.isBlank(recordsPerPage))
        {
            listCritiera.setRecordsPerPage(Integer.parseInt(recordsPerPage));
        }

        String startPage = this.getQueryStringOrHeaderValue(START_PAGE_URI_QUERYSTRING_PARAMETER_NAME,
                START_PAGE_HEADER_NAME, true);
        if (!StringUtils.isBlank(startPage))
        {
            listCritiera.setStartPage(Integer.parseInt(startPage));
        }

        String doPageCount = this.getQueryStringOrHeaderValue(DO_PAGE_COUNT_URI_QUERYSTRING_PARAMETER_NAME,
                DO_PAGE_COUNT_HEADER_NAME, true);
        if (!StringUtils.isBlank(doPageCount))
        {
            listCritiera.setDoPageCount(doPageCount.equalsIgnoreCase("true"));
        }
    }

    /**
     * Looks for a value in the query string parameter or the header
     * 
     * @param queryStringParameterName
     *            Name of query string parameter
     * @param headerName
     *            Name in the header
     * @param isOptional
     *            If not optional, then error thrown if value is blank.
     * @return String value
     * @throws ChiliLogException
     */
    protected String getQueryStringOrHeaderValue(String queryStringParameterName, String headerName, boolean isOptional)
            throws ChiliLogException
    {
        String s = this.getUriQueryStringParameter(queryStringParameterName, true);
        if (StringUtils.isBlank(s))
        {
            s = _request.getHeader(headerName);
            if (StringUtils.isBlank(s) && !isOptional)
            {
                throw new ChiliLogException(Strings.URI_QUERY_STRING_PARAMETER_OR_HEADER_ERROR,
                        queryStringParameterName, headerName, _request.getUri());
            }
        }

        return StringUtils.isBlank(s) ? null : s;
    }

    /**
     * Returns the authentication token
     */
    protected AuthenticationTokenAO getAuthenticationToken()
    {
        return _authenticationToken;
    }

    /**
     * Sets the authentication token
     * 
     * @param authenticationToken
     */
    protected void setAuthenticationToken(AuthenticationTokenAO authenticationToken)
    {
        _authenticationToken = authenticationToken;
    }

    /**
     * Returns the business object representing the authenticated user
     */
    protected UserBO getAuthenticatedUser()
    {
        return _authenticatedUser;
    }

    protected void setAuthenticatedUser(UserBO authenticatedUser)
    {
        _authenticatedUser = authenticatedUser;
    }

    /**
     * Returns an array of repository names to which the authenticated user has workbench access
     * 
     * @return Array of repository names
     */
    protected String[] getAuthenticatedUserAllowedRepository()
    {
        ArrayList<String> l = new ArrayList<String>();
        for (String role : _authenticatedUser.getRoles())
        {
            if (role.endsWith(UserBO.REPOSITORY_ADMINISTRATOR_ROLE_SUFFIX)
                    || role.endsWith(UserBO.REPOSITORY_WORKBENCH_ROLE_SUFFIX))
            {
                String repoName = UserBO.extractRepositoryNameFromRole(role);
                if (!StringUtils.isBlank(repoName) && !l.contains(repoName))
                {
                    l.add(repoName);
                }
            }
        }
        return l.toArray(new String[l.size()]);
    }

    /**
     * Performs initial validation including authentication.
     * 
     * @return True if successful and False if error.
     */
    public ApiResult validate()
    {
        ApiResult result = validateSupportedMethod();
        if (!result.isSuccess())
        {
            return result;
        }

        result = parseURI();
        if (!result.isSuccess())
        {
            return result;
        }

        result = validateURI();
        if (!result.isSuccess())
        {
            return result;
        }

        result = validateAuthenticationToken();
        if (!result.isSuccess())
        {
            return result;
        }

        result = validateAuthenticatedUserRole();
        if (!result.isSuccess())
        {
            return result;
        }

        // Return success
        return new ApiResult();
    }

    /**
     * <p>
     * Validates if the request method is supported. Returns a 405 Method Not Allowed response if there is an error.
     * </p>
     * <p>
     * According to the HTTP specs, a 405 Method Not Allowed response MUST include an Allow header containing a list of
     * valid methods for the requested resource.
     * </p>
     * 
     * <pre>
     * Allow: GET, HEAD, PUT
     * </pre>
     * 
     * @return ApiResult
     */
    protected ApiResult validateSupportedMethod()
    {
        HttpMethod requestMethod = _request.getMethod();
        HttpMethod[] supportedMethods = this.getSupportedMethods();
        ApiResult result = new ApiResult();
        boolean found = false;

        for (HttpMethod supportedMethod : supportedMethods)
        {
            if (supportedMethod == requestMethod)
            {
                found = true;
                break;
            }
        }

        if (!found)
        {
            result.setHttpResponseStatus(HttpResponseStatus.METHOD_NOT_ALLOWED);

            StringBuilder sb = new StringBuilder();
            for (HttpMethod supportedMethod : supportedMethods)
            {
                sb.append(supportedMethod.toString());
                sb.append(", ");
            }
            sb.setLength(sb.length() - 2); // remove last comma
            result.getHeaders().put(HttpHeaders.Names.ALLOW, sb.toString());
        }

        return result;
    }

    /**
     * <p>
     * Validates if this request is authenticated. If not, a "401 Unauthorized" response is returned to the caller.
     * </p>
     * 
     * @return ApiResult
     */
    protected ApiResult validateAuthenticationToken()
    {
        try
        {
            _authenticationToken = AuthenticationTokenAO.fromString(_request.getHeader(AUTHENTICATION_TOKEN_HEADER));

            // TODO some caching!
            _authenticatedUser = UserController.getInstance().get(MongoConnection.getInstance().getConnection(),
                    new ObjectId(_authenticationToken.getUserID()));

        }
        catch (Exception ex)
        {
            return new ApiResult(HttpResponseStatus.UNAUTHORIZED, ex);
        }
        return new ApiResult();
    }

    /**
     * Generic user role validate. Assumes the user can read but not write.
     * 
     * @return ApiResult
     */
    protected abstract ApiResult validateAuthenticatedUserRole();

    /**
     * <p>
     * Validates if the URI has all the supplied parts. If not, a "400 Bad Request" response is returned to the caller.
     * </p>
     * 
     * @return ApiResult
     */
    protected ApiResult parseURI()
    {
        try
        {
            // Get query string
            QueryStringDecoder decoder = new QueryStringDecoder(_request.getUri());
            _uriQueryStringParameters = decoder.getParameters();

            // Get
            String[] pathElements = decoder.getPath().split("/");
            if (pathElements.length > 3)
            {
                // Skip 1st blank element and the api and WorkerName elements.
                ArrayList<String> l = new ArrayList<String>();
                for (int i = 3; i < pathElements.length; i++)
                {
                    l.add(pathElements[i]);
                }
                _uriPathParameters = l.toArray(new String[] {});
            }
        }
        catch (Exception ex)
        {
            return new ApiResult(HttpResponseStatus.BAD_REQUEST, ex);
        }

        // Success
        return new ApiResult();
    }

    /**
     * <p>
     * Validates if the URI has all the supplied parts. If not, a 400 Bad Request response is returned to the caller.
     * </p>
     * 
     * @return ApiResult
     */
    protected ApiResult validateURI()
    {
        try
        {
            // PUT and DELETE must have a key
            HttpMethod requestMethod = _request.getMethod();
            if (requestMethod == HttpMethod.PUT || requestMethod == HttpMethod.DELETE)
            {
                if (_uriPathParameters == null || StringUtils.isBlank(_uriPathParameters[ID_URI_PATH_PARAMETER_INDEX]))
                {
                    throw new ChiliLogException(Strings.URI_PATH_PARAMETER_ERROR, "ID", _request.getUri());
                }
            }
        }
        catch (Exception ex)
        {
            return new ApiResult(HttpResponseStatus.BAD_REQUEST, ex);
        }

        // Success
        return new ApiResult();
    }

    /**
     * Process the incoming request.
     * 
     * @param requestContent
     *            If <code>ContentIOStyle</code> is Byte, then <code>byte[]</code> is passed in. If file, then a
     *            <code>File</code> will be passed in.
     * @return ApiResult to indicate the success/false of processing
     */
    public ApiResult processPost(Object requestContent) throws Exception
    {
        throw new UnsupportedOperationException("HTTP POST not supported for this API.");
    }

    /**
     * Process the incoming request.
     * 
     * @return ApiResult to indicate the success/false of processing
     */
    public ApiResult processDelete() throws Exception
    {
        throw new UnsupportedOperationException("HTTP DELETE not supported for this API.");
    }

    /**
     * Process the incoming request.
     * 
     * @return ApiResult to indicate the success/false of processing
     */
    public ApiResult processGet() throws Exception
    {
        throw new UnsupportedOperationException("HTTP GET not supported for this API.");
    }

    /**
     * Override this to implement worker specific processing
     * 
     * @param requestContent
     *            If <code>ContentIOStyle</code> is <code>Byte</code>, then <code>byte[]</code> is passed in. If file,
     *            then a <code>File</code> will be passed in.
     * @return ApiResult to indicate the success/false of processing
     */
    public ApiResult processPut(Object requestContent) throws Exception
    {
        throw new UnsupportedOperationException("HTTP PUT not supported for this API.");
    }

    /**
     * Converts request bytes into a string using the default UTF-8 character set.
     * 
     * @param bytes
     *            Bytes to convert
     * @return String form the bytes. If bytes is null, null is returned.
     * @throws UnsupportedEncodingException
     */
    protected String bytesToString(byte[] bytes) throws UnsupportedEncodingException
    {
        if (bytes == null)
        {
            return null;
        }

        // TODO parse charset
        return new String(bytes, "UTF-8");
    }

    /**
     * Specifies how request and response content is to be handled with respect to reading and writing.
     */
    public static enum ContentIOStyle
    {
        /**
         * Keep content in memory as a byte array
         */
        ByteArray,

        /**
         * - Flush content to file.
         */
        File
    }

}
