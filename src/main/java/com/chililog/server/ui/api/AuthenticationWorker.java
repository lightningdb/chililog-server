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

import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;

/**
 * <p>
 * Authentication API handles:
 * <ul>
 * <li>login - HTTP POST method</li>
 * <li>logout - HTTP DELETE method</li>
 * </p>
 */
public class AuthenticationWorker extends Worker
{
    /**
     * Constructor
     */
    public AuthenticationWorker(HttpRequest request)
    {
        super(request);
        return;
    }

    /**
     * Can only create and delete sessions
     */
    @Override
    public HttpMethod[] getSupportedMethods()
    {
        return new HttpMethod[]
        { HttpMethod.POST, HttpMethod.DELETE };
    }

    /**
     * Need special processing because for POST (login), there is no authentication token as yet
     */
    @Override
    protected ApiResult validateAuthenticationToken()
    {
        if (this.getRequest().getMethod() == HttpMethod.POST)
        {
            return new ApiResult();
        }
        return super.validateAuthenticationToken();
    }

    @Override
    public ApiResult process(Object requestContent)
    {
        // TODO Auto-generated method stub
        return null;
    }

}
