/*
 * Copyright 2009 Red Hat, Inc. Red Hat licenses this file to you under the Apache License, version 2.0 (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the License at:
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 */

package com.chililog.server.ui.api;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;

/**
 * 
 */
public class ApiRequestContext
{
    private HttpRequest _request;
    private String[] _uriPath;
    private Map<String, List<String>> _uriQueryString;
    private byte[] _requestContent;

    /**
     * Constructor
     * 
     * @param request
     *            HTTP request
     * @param requestContent
     *            data in the HTTP request body
     */
    public ApiRequestContext(HttpRequest request, byte[] requestContent)
    {
        _request = request;
        _requestContent = requestContent;
        
        QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.getUri());
        _uriPath = queryStringDecoder.getPath().split("/");
        _uriQueryString = queryStringDecoder.getParameters();
        
        return;
    }

    /**
     * Returns the HTTP request
     */
    public HttpRequest getRequest()
    {
        return _request;
    }

    /**
     * <p>
     * Returns the path of a URI. If the URI is <code>/hello?recipient=world</code>
     * </p>
     * <code>
     * assertEquals(getUriPath()[0], "hello"))
     * </code>
     */
    public String[] getUriPath()
    {
        return _uriPath;
    }

    /**
     * <p>
     * Returns the query string parameters of a URI. If the URI is <code>/hello?recipient=world</code>
     * </p>
     * <code>
     * assertEquals(this.getUriQueryString().get("recipient")[0], "world")
     * </code>
     */
    public Map<String, List<String>> getUriQueryString()
    {
        return _uriQueryString;
    }

    /**
     * Content of the request that was posted
     */
    public byte[] getRequestContent()
    {
        return _requestContent;
    }

    /**
     * Content of the request that was posted
     * @throws UnsupportedEncodingException 
     */
    public String getRequestContentAsString() throws UnsupportedEncodingException
    {
        if (_requestContent == null || _requestContent.length == 0)
        { return null; }
        
        return new String(_requestContent, "UTF-8");
    }
}
