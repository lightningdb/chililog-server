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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import org.jboss.netty.handler.codec.http.HttpResponseStatus;

import com.chililog.server.common.JsonTranslator;
import com.chililog.server.ui.api.AuthenticationAO.ExpiryType;
import com.chililog.server.ui.api.Worker.ContentIOStyle;

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
     * Builds a successful result with authentication token and content
     * 
     * @param authenticationToken
     *            token
     * @param contentToJsonify
     *            Object to convert into JSON format
     */
    public ApiResult(AuthenticationTokenAO authenticationToken, Object contentToJsonify)
    {
        _responseStatus = HttpResponseStatus.OK;

        // For an sliding expiry token, we want to update the expiry time
        if (authenticationToken.getExpiryType() == ExpiryType.Sliding)
        {
            authenticationToken.updateExpiresOn();
        }
        _headers.put(Worker.AUTHENTICATION_TOKEN_HEADER, authenticationToken.toString());

        setResponseContentToJson(contentToJsonify);
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
