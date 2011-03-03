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

import org.jboss.netty.handler.codec.http.HttpRequest;

import com.chililog.server.ui.HttpRequestHandler;

/**
 * <p>
 * Base API class. Contains the interface for {@link HttpRequestHandler} to use as well as common methods.
 * </p>
 * <p>
 * All services classes are designed to be single use (re-entry not supported) and single threaded.
 * </p>
 */
public abstract class Worker
{
    private ContentIOStyle _requestContentIOStyle = ContentIOStyle.ByteArray;
    
    /**
     * Performs initial validation including authentication.
     * 
     * @param request
     *            HTTP request to process
     * @return True if successful and False if error.
     */
    public abstract ApiResult initialize(HttpRequest request);

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
     * Process the incoming request.
     * 
     * @param requestContent If {@link ContentIOStyle} is 
     * @return
     */
    public abstract ApiResult process(Object requestContent);


    /**
     * Specifies how request and response content is to be handled with respect to reading and writing.
     */
    public enum ContentIOStyle
    {
        /**
         * Keep content in memory as a byte array
         */
        ByteArray,

        /**
         * Flush content to file.
         */
        File
    }

}
