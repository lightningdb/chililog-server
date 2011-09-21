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

package org.chililog.server.workbench;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.Executors;

import org.apache.commons.lang.StringUtils;
import org.chililog.server.common.AppProperties;
import org.chililog.server.common.Log4JLogger;
import org.hornetq.api.core.TransportConfiguration;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.ChannelGroupFuture;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.execution.OrderedMemoryAwareThreadPoolExecutor;

/**
 * <p>
 * The WorkbenchService controls the embedded Netty web server used to provide ChiliLog's management, analysis and
 * notification functions to users.
 * </p>
 * 
 * <pre class="example">
 * // Start web server
 * WorkbenchService.getInstance().start();
 * 
 * // Stop web server
 * WorkbenchService.getInstance().stop();
 * </pre>
 * 
 * <p>
 * The web server's request handling pipeline is setup by {@link HttpServerPipelineFactory}. Two Netty thread pools are
 * used:
 * <ul>
 * <li>One for the channel bosses (the server channel acceptors). See NioServerSocketChannelFactory javadoc.</li>
 * <li>One for the accepted channels (called workers). See NioServerSocketChannelFactory javadoc.</li>
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
 * <p>
 * The pipeline uses {@link HttpRequestHandler} to route requests to services for processing. Routing is based on the
 * request URI. Example of servers are {@link EchoRequestHandler} and {@link StaticFileRequestHandler}.
 * </p>
 * 
 * <p>
 * We stopped using the {@link OrderedMemoryAwareThreadPoolExecutor} as a third level worker pool because it created <a
 * href="http://www.jboss.org/netty/community#nabble-td6303816">errors with compression</a> and <a
 * href="http://web.archiveorange.com/archive/v/ZVMdIF9d6poqpmuvDOuq">performance issues</a>. We've since removed it.
 * Not sure if we really need to run in another thread pool since we are using the Executors.newCachedThreadPool() which
 * creates a new thread as needed.
 * </p>
 * 
 * @author vibul
 * 
 */
public class WorkbenchService
{
    private static Log4JLogger _logger = Log4JLogger.getLogger(WorkbenchService.class);
    private static final ChannelGroup _allChannels = new DefaultChannelGroup("WorkbenchWebServerManager");
    private ChannelFactory _channelFactory = null;

    /**
     * Returns the singleton instance for this class
     */
    public static WorkbenchService getInstance()
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
        public static final WorkbenchService INSTANCE = new WorkbenchService();
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
    private WorkbenchService()
    {
        return;
    }

    /**
     * Start the web server
     */
    public synchronized void start()
    {
        AppProperties appProperties = AppProperties.getInstance();

        if (_channelFactory != null)
        {
            _logger.info("Workbench Web Sever Already Started.");
            return;
        }

        if (!appProperties.getWorkbenchEnabled())
        {
            _logger.info("Workbench Web Sever not enabled and will not be started.");
            return;
        }

        _logger.info("Starting Workbench Web Sever on " + appProperties.getWorkbenchHost() + ":"
                + appProperties.getWorkbenchPort() + "...");

        _channelFactory = new NioServerSocketChannelFactory(Executors.newCachedThreadPool(),
                Executors.newCachedThreadPool());

        // Configure the server.
        ServerBootstrap bootstrap = new ServerBootstrap(_channelFactory);

        // Set up the event pipeline factory.
        bootstrap.setPipelineFactory(new HttpServerPipelineFactory());

        // Bind and start to accept incoming connections.
        String[] hosts = TransportConfiguration.splitHosts(appProperties.getWorkbenchHost());
        for (String h : hosts)
        {
            if (StringUtils.isBlank(h))
            {
                if (hosts.length == 1)
                {
                    h = "0.0.0.0";
                }
                else
                {
                    continue;
                }
            }

            SocketAddress address = h.equals("0.0.0.0") ? new InetSocketAddress(appProperties.getWorkbenchPort())
                    : new InetSocketAddress(h, appProperties.getWorkbenchPort());
            Channel channel = bootstrap.bind(address);
            _allChannels.add(channel);
        }

        _logger.info("Workbench Web Sever Started.");
    }

    /**
     * Stop the web server
     */
    public synchronized void stop()
    {
        _logger.info("Stopping Workbench Web Sever ...");

        ChannelGroupFuture future = _allChannels.close();
        future.awaitUninterruptibly();

        _channelFactory.releaseExternalResources();
        _channelFactory = null;

        _logger.info("Workbench Web Sever Stopped.");
    }

    /**
     * Returns the group holding all channels so we can shutdown without hanging
     */
    ChannelGroup getAllChannels()
    {
        return _allChannels;
    }

}
