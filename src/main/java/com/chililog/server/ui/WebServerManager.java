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

package com.chililog.server.ui;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.ChannelGroupFuture;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import com.chililog.server.common.AppProperties;
import com.chililog.server.common.Log4JLogger;

/**
 * <p>
 * Manages the web server used to serve up REST API and WorkBench to our users.
 * </p>
 * <p>
 * The web server is the user's gateway to ChiliLog's management, analysis and notification functions.
 * </p>
 * 
 * @author vibul
 * 
 */
public class WebServerManager
{
    private static Log4JLogger _logger = Log4JLogger.getLogger(WebServerManager.class);
    private static final ChannelGroup _allChannels = new DefaultChannelGroup("WebServerManager");
    private ChannelFactory _channelFactory = null;

    /**
     * Returns the singleton instance for this class
     */
    public static WebServerManager getInstance()
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
        public static final WebServerManager INSTANCE = new WebServerManager();
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
    private WebServerManager()
    {
        return;
    }

    /**
     * Start the web server
     */
    public void start()
    {
        if (_channelFactory != null)
        {
            _logger.info("Web Sever Already Started.");
            return;
        }

        _logger.info("Starting Web Sever ...");

        _channelFactory = new NioServerSocketChannelFactory(Executors.newCachedThreadPool(),
                Executors.newCachedThreadPool());

        // Configure the server.
        ServerBootstrap bootstrap = new ServerBootstrap(_channelFactory);

        // Set up the event pipeline factory.
        bootstrap.setPipelineFactory(new HttpServerPipelineFactory());

        // Bind and start to accept incoming connections.
        InetSocketAddress socket = new InetSocketAddress(AppProperties.getInstance().getWebIpAddress(), AppProperties
                .getInstance().getWebIpPort());
        Channel channel = bootstrap.bind(socket);

        _allChannels.add(channel);

        _logger.info("Starting Web Sever Started.");
    }

    /**
     * Stop the web server
     */
    public void stop()
    {
        _logger.info("Stopping Web Sever ...");

        ChannelGroupFuture future = _allChannels.close();
        future.awaitUninterruptibly();

        _channelFactory.releaseExternalResources();
        _channelFactory = null;

        _logger.info("Web Sever Stopped.");
    }
}
