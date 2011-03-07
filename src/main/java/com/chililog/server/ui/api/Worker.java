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
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;

import com.chililog.server.common.ChiliLogException;
import com.chililog.server.ui.HttpRequestHandler;
import com.chililog.server.ui.Strings;

/**
 * <p>
 * Base API class. Contains the interface for {@link HttpRequestHandler} to use as well as common methods.
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

    public static final int URI_PATH_ID_PARAMETER_INDEX = 0;

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
     * Performs initial validation including authentication.
     * 
     * @param request
     *            HTTP request to process
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
     * @return {@link ApiResult}
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
                sb.append("', ");
            }
            sb.setLength(sb.length() - 2); // remove last comma
            result.getHeaders().put(HttpHeaders.Names.ALLOW, sb.toString());
        }

        return result;
    }

    /**
     * <p>
     * Validates if this request is authenticated. If not, a 403 Forbidden response is returned to the caller.
     * </p>
     * 
     * @return {@link ApiResult}
     */
    protected ApiResult validateAuthenticationToken()
    {
        ApiResult result = new ApiResult();

        return result;
    }

    /**
     * <p>
     * Validates if the URI has all the supplied parts. If not, a 400 Bad Request response is returned to the caller.
     * </p>
     * 
     * @return {@link ApiResult}
     */
    protected ApiResult parseURI()
    {
        ApiResult result = new ApiResult();
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
            result.setHttpResponseStatus(HttpResponseStatus.BAD_REQUEST);
            result.setResponseContentToJson(new ErrorAO(ex));
        }
        return result;
    }

    /**
     * <p>
     * Validates if the URI has all the supplied parts. If not, a 400 Bad Request response is returned to the caller.
     * </p>
     * 
     * @return {@link ApiResult}
     */
    protected ApiResult validateURI()
    {
        ApiResult result = new ApiResult();
        try
        {
            // PUT and DELETE must have a key
            HttpMethod requestMethod = _request.getMethod();
            if (requestMethod == HttpMethod.PUT || requestMethod == HttpMethod.GET)
            {
                if (StringUtils.isBlank(_uriPathParameters[URI_PATH_ID_PARAMETER_INDEX]))
                {
                    throw new ChiliLogException(Strings.URI_PATH_PARAMETER_ERROR, "ID", _request.getUri());
                }
            }
        }
        catch (Exception ex)
        {
            result.setHttpResponseStatus(HttpResponseStatus.BAD_REQUEST);
            result.setResponseContentToJson(new ErrorAO(ex));
        }
        return result;
    }

    /**
     * Process the incoming request.
     * 
     * @param requestContent
     *            If {@link ContentIOStyle} is
     * @return
     */
    public abstract ApiResult process(Object requestContent);

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
