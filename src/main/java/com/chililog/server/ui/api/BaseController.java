/*
 * Copyright 2009 Red Hat, Inc. Red Hat licenses this file to you under the Apache License, version 2.0 (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the License at:
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 */

package com.chililog.server.ui.api;

import org.jboss.netty.handler.codec.http.HttpResponseStatus;

/**
 * Base API controller class
 */
public class BaseController
{
    private HttpResponseStatus _responseStatus = HttpResponseStatus.OK;
    
    private Object _responseContent = null;

    /**
     * HTTP response status to return to the caller. Defaults to 200 OK. 
     */
    public HttpResponseStatus getResponseStatus()
    {
        return _responseStatus;
    }

    protected void setResponseStatus(HttpResponseStatus responseStatus)
    {
        _responseStatus = responseStatus;
    }

    /**
     * Content to pass back to the caller
     */
    public Object getResponseContent()
    {
        return _responseContent;
    }

    protected void setResponseContent(String responseContent)
    {
        _responseContent = responseContent;
    }
    
    protected void setResponseContent(byte[] responseContent)
    {
        _responseContent = responseContent;
    }
    
    
}
