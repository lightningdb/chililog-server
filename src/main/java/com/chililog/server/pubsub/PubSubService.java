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

package com.chililog.server.pubsub;

import com.chililog.server.pubsub.jsonhttp.JsonHttpService;

/**
 * <p>
 * The PubSubService controls all non-HornetQ PubSub services
 * </p>
 * 
 * <pre class="example">
 * // Start web server
 * PubSubService.getInstance().start();
 * 
 * // Stop web server
 * PubSubService.getInstance().stop();
 * </pre>
 * 
 * 
 * @author vibul
 * 
 */
public class PubSubService
{
    /**
     * Returns the singleton instance for this class
     */
    public static PubSubService getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    /**
     * SingletonHolder is loaded on the first execution of Singleton.getInstance() or the first access to
     * SingletonHolder.INSTANCE, not before.
     * 
     * See http://en.wikipedia.org/wiki/Singleton_pattern
     */
    private static class SingletonHolder
    {
        public static final PubSubService INSTANCE = new PubSubService();
    }

    /**
     * <p>
     * Singleton constructor
     * </p>
     * <p>
     * If there is an exception, we log the error and exit because there's no point continuing without MQ client session
     * </p>
     * 
     * @throws Exception
     */
    private PubSubService()
    {
        return;
    }

    /**
     * Start all pubsub services
     */
    public void start()
    {
        JsonHttpService.getInstance().start();
    }

    /**
     * Stop all pubsub services
     */
    public void stop()
    {
        JsonHttpService.getInstance().stop();
    }

}
