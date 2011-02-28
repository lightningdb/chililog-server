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

import static org.junit.Assert.*;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.chililog.server.common.Log4JLogger;

/**
 * Test our web server
 * 
 * @author vibul
 * 
 */
public class WebServerManagerTest
{
    private static Log4JLogger _logger = Log4JLogger.getLogger(WebServerManagerTest.class);

    @BeforeClass
    public static void classSetup() throws Exception
    {
        WebServerManager.getInstance().start();
    }

    @AfterClass
    public static void classTeardown()
    {
        WebServerManager.getInstance().stop();
    }

    @Test
    public void testEchoGET() throws IOException
    {
        // Create a URL for the desired page
        URL url = new URL("http://localhost:8989/echo/test");

        // Read all the text returned by the server
        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
        StringBuffer sb = new StringBuffer();
        String str;
        while ((str = in.readLine()) != null)
        {
            sb.append(str + "\n");
        }
        in.close();
        
        _logger.info(sb.toString());
        
        assertTrue(sb.toString().contains("REQUEST_URI: /echo/test"));
    }
    
    /**
     * We should get back a 404 file not found
     * @throws IOException
     */
    @Test(expected = FileNotFoundException.class)
    public void testNotFound() throws IOException
    {
        // Create a URL for the desired page
        URL url = new URL("http://localhost:8989/not/found");
        url.getContent();
    }

}
