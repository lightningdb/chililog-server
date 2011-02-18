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

package com.chililog.server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.logging.InternalLoggerFactory;
import org.jboss.netty.logging.Log4JLoggerFactory;

import com.chililog.server.common.AppProperties;
import com.chililog.server.common.Log4JLogger;
import com.chililog.server.common.SystemProperties;
import com.chililog.server.engine.MqManager;
import com.chililog.server.engine.RepositoryManager;

/**
 * ChiliLog Server Application.
 * <p>
 * Starts:
 * <ul>
 * <li>HornetQ to receive incoming data</li>
 * <li>Worker threads to dump the data into our database</li>
 * <li>Worker threads to monitor data in the database</li>
 * <li>Admin web app to administer HornetQ and worker threads</li>
 * <li>User web app to view data</li>
 * 
 */
public class App
{
    static Log4JLogger _logger = Log4JLogger.getLogger(App.class);

    /**
     * Big Bang method. It all starts here!
     * 
     * @param args
     *            Command Line arguments
     */
    public static void main(String[] args)
    {
        try
        {

            // Setup our shutdown hooks
            Runtime.getRuntime().addShutdownHook(new Thread()
            {
                public void run()
                {
                    try
                    {
                        stopChiliLogServer();
                    }
                    catch (Exception e)
                    {
                        _logger.error("Shutdown error: " + e.getMessage(), e);
                    }
                }
            });

            startChiliLogServer();

            // Wait for input to stop (the read is required for Eclipse)
            // System.out.println("Press any key to stop.\n\n");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
            bufferedReader.read();
            System.exit(0);

            // Finish
            return;
        }
        catch (Exception e)
        {
            _logger.error(e, "Error starting CHILILOG Server");
            e.printStackTrace();
            System.exit(1);
        }

        return;
    }

    /**
     * Start ChiliLog server
     * 
     * @throws Exception
     */
    public static void startChiliLogServer() throws Exception
    {
        _logger.info("CHILILOG Server Starting Up...");
        _logger.info("System Properties\n" + SystemProperties.getInstance().toString());
        _logger.info("App Properties\n" + AppProperties.getInstance().toString());

        MqManager.getInstance().start();
        RepositoryManager.getInstance().startup();
        
        Thread.sleep(2000);

        _logger.info("CHILILOG Server Started");
    }

    /**
     * Stop ChiliLog server
     * 
     * @throws Exception
     */
    public static void stopChiliLogServer() throws Exception
    {
        _logger.info("CHILILOG Server shutting down.");

        RepositoryManager.getInstance().shutdown();

        // Wait 2 seconds for everything to stop properly
        Thread.sleep(2000);

        _logger.info("CHILILOG Server successfully shutdown.");
    }

    /**
     * Configure Netty
     */
    static void startNetty()
    {
        InternalLoggerFactory.setDefaultFactory(new Log4JLoggerFactory());

        // Configure the server.
        ServerBootstrap bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(
                Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));

        // Set up the event pipeline factory.
        bootstrap.setPipelineFactory(new HttpServerPipelineFactory());

        // Bind and start to accept incoming connections.
        bootstrap.bind(new InetSocketAddress(8080));
    }
}
