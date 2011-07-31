//
// Copyright 2011 Cinch Logic Pty Ltd.
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

package org.chililog.server.pubsub;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.chililog.server.common.ChiliLogException;
import org.chililog.server.common.Log4JLogger;
import org.chililog.server.engine.MqService;
import org.hornetq.api.core.HornetQException;
import org.hornetq.api.core.client.ClientProducer;
import org.hornetq.api.core.client.ClientSession;


/**
 * <p>
 * Pool of HornetQ producers used for publishing messages. This assumes that MqService has started.
 * </p>
 * <p>
 * Each producer has its own session.
 * </p>
 * <p>
 * All sessions use the "system user" credentials because it has access to all queues. Authentication is assumed to be
 * performed prior to using a session.
 * </p>
 * 
 * <pre>
 * // Create pool
 * MqProducerSessionPool pool = new MqProducerSessionPool();
 * 
 * // Get a session from the pool
 * MqProducerSessionPool p = pool.getPooled();
 * 
 * try
 * {
 *     // Do some work
 * 
 *     // Returning a session to the pool
 *     pool.returnPooled(p);
 * }
 * catch (Exception ex)
 * {
 *     // In the event of an error, close the session and add a new item to the pool
 *     try
 *     {
 *         p.session.close();
 *     }
 *     catch (HornetQException e)
 *     {
 *     }
 *     pool.addPooled();
 *     throw ex;
 * }
 * 
 * // Close connections
 * pool.cleanup();
 * </pre>
 * 
 * <p>
 * Pattern copied from <a href=
 * "http://source.jboss.org/browse/HornetQ/trunk/hornetq-rest/hornetq-rest/src/main/java/org/hornetq/rest/queue/PostMessage.java?r=10416&r=10416&r=10416"
 * >org.hornetq.rest.queue.PostMessage</a>
 * </p>
 * 
 * @author vibul
 */
public class MqProducerSessionPool
{
    static Log4JLogger _logger = Log4JLogger.getLogger(MqProducerSessionPool.class);

    protected ArrayBlockingQueue<Pooled> _pool = null;

    /**
     * <p>
     * Singleton constructor
     * </p>
     * <p>
     * If there is an exception, we log the error and exit because there's no point continuing without MQ client session
     * </p>
     * 
     * @param poolSize
     *            number of session to pool
     * @throws Exception
     */
    public MqProducerSessionPool(int poolSize)
    {
        try
        {
            _pool = new ArrayBlockingQueue<Pooled>(poolSize);
            for (int i = 0; i < poolSize; i++)
            {
                addPooled();
            }
            return;
        }
        catch (Exception e)
        {
            _logger.error("Error loading Publisher Session Pool: " + e.getMessage(), e);
            System.exit(1);

        }
    }

    /**
     * A pooled session and its associated producer
     */
    public static class Pooled
    {
        public ClientSession session;
        public ClientProducer producer;

        private Pooled(ClientSession session, ClientProducer producer)
        {
            this.session = session;
            this.producer = producer;
        }
    }

    /**
     * Adds a new pooled item (session and its associated producer) to the pool
     * 
     * @throws Exception
     */
    public void addPooled() throws Exception
    {
        ClientSession session = MqService.getInstance().getNonTransactionalSystemClientSession();
        ClientProducer producer = session.createProducer();
        session.start();
        _pool.add(new Pooled(session, producer));
    }

    /**
     * Returns and removes a pooled session from the pool.
     * 
     * @return a pooled item (session and its associated producer)
     * @throws InterruptedException
     *             if interrupted while waiting for a session
     * @throws ChiliLogException
     *             if no session are available after a 1 second wait
     */
    public Pooled getPooled() throws Exception
    {
        Pooled pooled = _pool.poll(1, TimeUnit.SECONDS);
        if (pooled == null)
        {
            throw new ChiliLogException(Strings.GET_POOLED_PUBLISHER_SESSION_TIMEOUT_ERROR);
        }
        return pooled;
    }

    /**
     * Returns a pooled item that was retrieved using getPooled() to the pool.
     * 
     * @param pooled
     *            pooled item retrieved using getPooled()
     */
    public void returnPooled(Pooled pooled)
    {
        _pool.add(pooled);
    }

    /**
     * Used during shutdown to close all sessions
     */
    public void cleanup()
    {
        if (_pool == null)
        {
            return;
        }

        for (Pooled pooled : _pool)
        {
            try
            {
                pooled.session.close();
            }
            catch (HornetQException e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Returns the number of connections in the pool
     */
    public int size()
    {
        return _pool.size();
    }

}
