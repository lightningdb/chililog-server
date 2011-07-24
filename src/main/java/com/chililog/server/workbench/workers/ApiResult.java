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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import org.jboss.netty.handler.codec.http.HttpResponseStatus;

import com.chililog.server.common.AppProperties;
import com.chililog.server.common.JsonTranslator;
import com.chililog.server.workbench.workers.AuthenticationAO.ExpiryType;
import com.chililog.server.workbench.workers.Worker.ContentIOStyle;

/**
 * <p>
 * Encapsulates the result of invoking an API worker.
 * </p>
 * <p>
 * It contains the details for constructing an HTTPResponse to return to the caller.
 * </p>
 * 
 * @author vibul
 * 
 */
public class ApiResult
{
    private HttpResponseStatus _responseStatus = HttpResponseStatus.OK;

    private String _responseContentType = Worker.JSON_CONTENT_TYPE;

    private ContentIOStyle _responseContentIOStyle = ContentIOStyle.ByteArray;

    private Object _responseContent = null;

    private HashMap<String, String> _headers = new HashMap<String, String>();

    /**
     * Basic constructor
     */
    public ApiResult()
    {
        return;
    }

    /**
     * Builds an error result
     * 
     * @param status
     *            HTTP Response status
     * @param ex
     *            Exception describing the error
     */
    public ApiResult(HttpResponseStatus status, Throwable ex)
    {
        _responseStatus = status;
        setResponseContentToJson(new ErrorAO(ex));
    }

    /**
     * Builds a successful result with authentication token and content. <code>200 OK</code> is returned unless
     * <code>contentToJsonify</code> is null, in which case <code>204 No Content</code> is returned
     * 
     * @param authenticationToken
     *            The authentication token that was submitted in the request. It will be updated and returned in the
     *            response header for sliding expiry. For absolute expiry, it will not be returned.
     * @param contentType
     *            response content MIME type
     * @param content
     *            Object to convert into JSON format.
     */
    public ApiResult(AuthenticationTokenAO authenticationToken, String contentType, Object content)
    {
        AppProperties appProperties = AppProperties.getInstance();

        _responseStatus = (content == null ? HttpResponseStatus.NO_CONTENT : HttpResponseStatus.OK);
        _responseContentType = contentType;

        // For an sliding expiry token, we want to update the expiry time
        if (authenticationToken.getExpiryType() == ExpiryType.Sliding)
        {
            authenticationToken.updateExpiresOn();
        }
        _headers.put(Worker.AUTHENTICATION_TOKEN_HEADER, authenticationToken.toString());
        _headers.put(Worker.AUTHENTICATION_SERVER_VERSION, appProperties.getAppVersion());
        _headers.put(Worker.AUTHENTICATION_SERVER_BUILD_TIMESTAMP, appProperties.getBuildTimestamp());

        if (content != null)
        {
            if (content instanceof byte[])
            {
                _responseContent = content;
                _responseContentIOStyle = ContentIOStyle.ByteArray;
            }
            else if (content instanceof File)
            {
                _responseContent = content;
                _responseContentIOStyle = ContentIOStyle.File;
            }
            else if (contentType != null && contentType.equals(Worker.JSON_CONTENT_TYPE))
            {
                // Try to turn it into JSON
                setResponseContentToJson(content);
            }
            else
            {
                throw new UnsupportedOperationException("Cannot handled content of type "
                        + content.getClass().getName());
            }
        }
    }

    /**
     * Determines if the call is successful or not
     */
    public boolean isSuccess()
    {
        return _responseStatus == HttpResponseStatus.OK;
    }

    /**
     * The HTTP Response status to return to the caller
     */
    public HttpResponseStatus getResponseStatus()
    {
        return _responseStatus;
    }

    public void setHttpResponseStatus(HttpResponseStatus httpResponseStatus)
    {
        _responseStatus = httpResponseStatus;
    }

    /**
     * The MIME type to return to the caller. Defaults to <code>text/json</code>
     */
    public String getResponseContentType()
    {
        return _responseContentType;
    }

    public void setResponseContentType(String responseContentType)
    {
        _responseContentType = responseContentType;
    }

    /**
     * <p>
     * How the response content can be read.
     * </p>
     * <p>
     * If <code>ByteArray</code>, <code>getResponseContent()</code> will return a <code>byte[]</code>. This is the
     * default.
     * </p>
     * <p>
     * If <code>File</code>, <code>getResponseContent()</code> will return a <code>java.io.File</code>.
     * </p>
     */
    public ContentIOStyle getResponseContentIOStyle()
    {
        return _responseContentIOStyle;
    }

    public void setResponseContentIOStyle(ContentIOStyle responseContentIOStyle)
    {
        _responseContentIOStyle = responseContentIOStyle;
    }

    /**
     * <p>
     * Returns the content to be downloaded to the caller. If null, there is no data.
     * </p>
     * <p>
     * The data type of the return object is either: <code>byte[]</code> or <code>java.io.File</code>
     * </p>
     */
    public Object getResponseContent()
    {
        return _responseContent;
    }

    public void setResponseContent(Object responseContent)
    {
        _responseContent = responseContent;
    }

    /**
     * Translates the specified object <code>o</code> into JSON and sets it as the content
     * 
     * @param contentToJsonify
     *            Object to translate into JSON and then return to the caller
     * @throws UnsupportedEncodingException
     */
    public void setResponseContentToJson(Object contentToJsonify)
    {
        try
        {
            _responseContentType = Worker.JSON_CONTENT_TYPE;
            _responseContentIOStyle = ContentIOStyle.ByteArray;

            if (contentToJsonify == null)
            {
                _responseContent = null;
            }
            else
            {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                PrintStream ps = new PrintStream(baos, true, Worker.JSON_CHARSET);
                JsonTranslator.getInstance().toJson(contentToJsonify, ps);
                ps.close();
                _responseContent = baos.toByteArray();
            }
        }
        catch (Exception ex)
        {
            // We should not get UnsupportedEncodingException ... but you never know.
            // Just throw again
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    /**
     * Headers that will be returned to the caller
     */
    public HashMap<String, String> getHeaders()
    {
        return _headers;
    }

}
