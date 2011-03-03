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

/**
 * <p>
 * Session API handles:
 * <ul>
 * <li>login - HTTP POST method</li>
 * <li>logout - HTTP DELETE method</li>
 * </p>
 */
public class SessionWorker extends Worker
{
    /**
     * Constructor
     */
    public SessionWorker()
    {
        return;
    }

    /**
     * Validate request to make sure we can process it
     */
    @Override
    public ApiResult initialize(HttpRequest request)
    {
        ApiResult result = new ApiResult();
        try
        {

        }
        catch (Exception ex)
        {

        }
        return result;
    }

    /**
     * Process incoming message
     */
    @Override
    public ApiResult process(Object requestContent)
    {
        ApiResult result = new ApiResult();
        try
        {

        }
        catch (Exception ex)
        {

        }
        return result;
    }

}
