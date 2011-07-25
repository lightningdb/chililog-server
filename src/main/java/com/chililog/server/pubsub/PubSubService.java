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

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.ChannelGroupFuture;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.execution.ExecutionHandler;
import org.jboss.netty.handler.execution.OrderedMemoryAwareThreadPoolExecutor;

import com.chililog.server.common.AppProperties;
import com.chililog.server.common.Log4JLogger;

/**
 * <p>
 * The PubSubService controls the embedded Netty web server used to provide publication and sububscription services
 * using JSON over HTTP and HTTP web sockets.
 * </p>
 * 
 * <pre class="example">
 * // Start web server
 * PubSubServicegetInstance().start();
 * 
 * // Stop web server
 * PubSubService.getInstance().stop();
 * </pre>
 * 
 * <p>
 * The web server's request handling pipeline is setup by {@link JsonHttpServerPipelineFactory}. <a
 * href="http://www.jboss.org/netty/community#nabble-td3823513">Three</a> Netty thread pools are used:
 * <ul>
 * <li>One for the channel bosses (the server channel acceptors). See NioServerSocketChannelFactory javadoc.</li>
 * <li>One for the accepted channels (called workers). See NioServerSocketChannelFactory javadoc.</li>
 * <li>One for task processing, after the request has been decoded and understood. See ExecutionHandler and
 * OrderedMemoryAwareThreadPoolExecutor javadoc.</li>
 * </ul>
 * </p>
 * <p>
 * Here's a description of how it all works from http://www.jboss.org/netty/community#nabble-td3434933.
 * </p>
 * 
 * <pre class="example">
 * For posterity, updated notes on Netty's concurrency architecture:
 * 
 * After calling ServerBootstrap.bind(), Netty starts a boss thread that just accepts new connections and registers them
 * with one of the workers from the worker pool in round-robin fashion (pool size defaults to CPU count). Each worker
 * runs its own select loop over just the set of keys that have been registered with it. Workers start lazily on demand
 * and run only so long as there are interested fd's/keys. All selected events are handled in the same thread and sent
 * up the pipeline attached to the channel (this association is established by the boss as soon as a new connection is
 * accepted).
 * 
 * All workers, and the boss, run via the executor thread pool; hence, the executor must support at least two
 * simultaneous threads.
 * 
 * A pipeline implements the intercepting filter pattern. A pipeline is a sequence of handlers. Whenever a packet is
 * read from the wire, it travels up the stream, stopping at each handler that can handle upstream events. Vice-versa
 * for writes. Between each filter, control flows back through the centralized pipeline, and a linked list of contexts
 * keeps track of where we are in the pipeline (one context object per handler).
 * </pre>
 * 
 * @author vibul
 * 
 */
public class PubSubService
{
    private static Log4JLogger _logger = Log4JLogger.getLogger(PubSubService.class);
    private static final ChannelGroup _allChannels = new DefaultChannelGroup("PubSubJsonHttpWebServerManager");
    private ChannelFactory _channelFactory = null;
    private MqProducerSessionPool _mqProducerSessionPool = null;

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
        // Create producer session pool equivalent to the number of threads for Json HTTP so that each thread does not
        // have to wait for a session
        _mqProducerSessionPool = new MqProducerSessionPool(AppProperties.getInstance()
                .getPubSubJsonHttpProtocolTaskThreadPoolSize());

        startJsonHttp();
    }

    /**
     * Stop all pubsub services
     */
    public void stop()
    {
        if (_mqProducerSessionPool != null)
        {
            _mqProducerSessionPool.cleanup();
            _mqProducerSessionPool = null;
        }

        stopJsonHttp();
    }

    /**
     * Start the JSON HTTP pubsub service
     */
    public void startJsonHttp()
    {
        AppProperties appProperties = AppProperties.getInstance();

        if (_channelFactory != null)
        {
            _logger.info("PubSub JSON HTTP Web Sever Already Started.");
            return;
        }

        _logger.info("Starting PubSub JSON HTTP  Web Sever on " + appProperties.getPubSubJsonHttpProtocolHost() + ":"
                + appProperties.getPubSubJsonHttpProtocolPort() + "...");

        _channelFactory = new NioServerSocketChannelFactory(Executors.newCachedThreadPool(),
                Executors.newCachedThreadPool());

        // Configure the server.
        ServerBootstrap bootstrap = new ServerBootstrap(_channelFactory);

        // Set up the event pipeline factory.
        ExecutionHandler executionHandler = new ExecutionHandler(new OrderedMemoryAwareThreadPoolExecutor(
                appProperties.getPubSubJsonHttpProtocolTaskThreadPoolSize(),
                appProperties.getPubSubJsonHttpProtocolTaskThreadPoolMaxChannelMemorySize(),
                appProperties.getPubSubJsonHttpProtocolTaskThreadPoolMaxThreadMemorySize(),
                appProperties.getPubSubJsonHttpProtocolTaskThreadPoolKeepAliveSeconds(), TimeUnit.SECONDS));

        bootstrap.setPipelineFactory(new JsonHttpServerPipelineFactory(executionHandler));

        // Bind and start to accept incoming connections.
        InetSocketAddress socket = new InetSocketAddress(AppProperties.getInstance().getPubSubJsonHttpProtocolHost(),
                AppProperties.getInstance().getPubSubJsonHttpProtocolPort());
        Channel channel = bootstrap.bind(socket);

        _allChannels.add(channel);

        _logger.info("PubSub JSON HTTP Web Sever Started.");
    }

    /**
     * Stop the pubsub services
     */
    public void stopJsonHttp()
    {
        _logger.info("Stopping PubSub JSON HTTP Sever ...");

        ChannelGroupFuture future = _allChannels.close();
        future.awaitUninterruptibly();

        _channelFactory.releaseExternalResources();
        _channelFactory = null;

        _logger.info("PubSub JSON HTTP Web Sever Stopped.");
    }

    /**
     * Returns the producer session pool
     */
    MqProducerSessionPool getMqProducerSessionPool()
    {
        return _mqProducerSessionPool;
    }
}
