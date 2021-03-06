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

package org.chililog.server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Timer;
import java.util.TimerTask;

import org.chililog.server.common.AppProperties;
import org.chililog.server.common.Log4JLogger;
import org.chililog.server.common.StringsProperties;
import org.chililog.server.common.SystemProperties;
import org.chililog.server.engine.MqService;
import org.chililog.server.engine.RepositoryService;
import org.chililog.server.pubsub.PubSubService;
import org.chililog.server.workbench.WorkbenchService;
import org.jboss.netty.logging.InternalLoggerFactory;
import org.jboss.netty.logging.Log4JLoggerFactory;

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
public class App {

    static Log4JLogger _logger = Log4JLogger.getLogger(App.class);
    private static final String STOP_ME_FILENAME = "STOP_ME";

    /**
     * Big Bang method. It all starts here!
     * 
     * @param args
     *            Command Line arguments
     */
    public static void main(String[] args) {
        try {
            start(args);

            addShutdownPoller();

            // Wait for input to stop (the read is required for Eclipse)
            // System.out.println("Press any key to stop.\n\n");
            // BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
            // bufferedReader.read();
            // writeShutdownFile();

            // Finish
            return;
        } catch (Exception e) {
            _logger.error(e, "Error starting CHILILOG Server");
            e.printStackTrace();
            System.exit(1);
        }

        return;
    }

    /**
     * Start ChiliLog server
     * 
     * @param args
     *            Startup command line args
     * @throws Exception
     */
    public static void start(String args[]) throws Exception {
        // Turn on netty logging
        InternalLoggerFactory.setDefaultFactory(new Log4JLoggerFactory());

        _logger.info("CHILILOG Server Starting Up...");
        _logger.info("System Properties\n" + SystemProperties.getInstance().toString());
        _logger.info("App Properties\n" + AppProperties.getInstance().toString());
        _logger.info("Current Directory: " + new File(".").getCanonicalPath());

        // Init strings
        StringsProperties.getInstance();

        MqService.getInstance().start();
        RepositoryService.getInstance().start();
        PubSubService.getInstance().start();
        WorkbenchService.getInstance().start();

        Thread.sleep(2000);

        _logger.info("CHILILOG Server Started");
    }

    /**
     * Stop ChiliLog server
     * 
     * @param args
     *            Parameters for shutdown. Not used. Only present to that this method can be called from procrun.
     * @throws Exception
     */
    public static void stop(String args[]) throws Exception {
        _logger.info("CHILILOG Server shutting down.");

        WorkbenchService.getInstance().stop();
        PubSubService.getInstance().stop();
        RepositoryService.getInstance().stop();
        MqService.getInstance().stop();

        // Wait 2 seconds for everything to stop properly
        Thread.sleep(2000);

        _logger.info("CHILILOG Server successfully shutdown.");
    }

    /**
     * Polls for the shutdown file - and shuts down if one is found
     */
    static void addShutdownPoller() {
        final File file = new File(".", STOP_ME_FILENAME);
        if (file.exists()) {
            file.delete();
        }

        final Timer timer = new Timer("ChiliLog Server Shutdown Timer", true);
        timer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                if (file.exists()) {
                    try {
                        stop(null);
                        timer.cancel();
                    } catch (Exception e) {
                        _logger.error(e, "Shutdown error: " + e.getMessage());
                    } finally {
                        Runtime.getRuntime().exit(0);
                    }
                }
            }
        }, 1000, 1000);
    }

    /**
     * Writes the shutdown file to stop the server
     * 
     * @throws Exception
     */
    static void writeShutdownFile() throws Exception {
        Writer out = new OutputStreamWriter(new FileOutputStream(new File(".", STOP_ME_FILENAME)));
        try {
            out.write("shutdown");
        } finally {
            out.close();
        }
    }
}
