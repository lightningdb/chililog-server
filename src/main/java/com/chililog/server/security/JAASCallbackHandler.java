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

package com.chililog.server.security;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

/**
 * <p>
 * This class is implemented in order to follow JAAS standards.
 * </p>
 * <p>
 * We don't use this class because HornetQ passes credentials directly to the LoginModule via the subject property
 * </p>
 * 
 * @author vibul
 * 
 */
public class JAASCallbackHandler implements CallbackHandler
{
    public void handle(final Callback[] callbacks) throws IOException, UnsupportedCallbackException
    {
        // do nothing, authentication is done
        // by passing credentials directly to the LoginModule via the subject property
    }
}