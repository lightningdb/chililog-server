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

import org.jboss.netty.handler.codec.http.HttpResponseStatus;

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
    private HttpResponseStatus _httpResponseStatus = HttpResponseStatus.OK;

    private String _responseContentType = "text/json; charset=UTF-8";

    private ContentIOStyle _responseContentIOStyle = ContentIOStyle.ByteArray;

    private Object _responseContent = null;

    /**
     * Determines if the call is successful or not
     */
    public boolean isSuccess()
    {
        return _httpResponseStatus == HttpResponseStatus.OK;
    }

    /**
     * The HTTP Response status to return to the caller
     */
    public HttpResponseStatus getResponseStatus()
    {
        return _httpResponseStatus;
    }

    public void setHttpResponseStatus(HttpResponseStatus httpResponseStatus)
    {
        _httpResponseStatus = httpResponseStatus;
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

}
