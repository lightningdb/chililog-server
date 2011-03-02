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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.chililog.server.common.AppProperties;
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
    public void testEchoGET2() throws IOException
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
     * 
     * @throws IOException
     */
    @Test(expected = FileNotFoundException.class)
    public void testNotFound() throws IOException
    {
        // Create a URL for the desired page
        URL url = new URL("http://localhost:8989/not/found");
        url.getContent();
    }

    /**
     * We should get back a 404 file not found
     * 
     * @throws IOException
     * @throws ParseException 
     */
    @Test()
    public void testStaticFileCache() throws IOException, ParseException
    {
        String TEXT = "abc\n123";
        
        String dir = AppProperties.getInstance().getWebStaticFilesDirectory();
        String fileName = UUID.randomUUID().toString() + ".txt";
        File file = new File(dir, fileName);

        FileOutputStream fos = new FileOutputStream(file);
        OutputStreamWriter out = new OutputStreamWriter(fos, "UTF-8");
        out.write(TEXT);
        out.close();

        // Refresh
        file = new File(file.getPath());

        // ******************************************************
        // Initial request
        // ******************************************************
        // Create a URL for the desired page
        URL url = new URL("http://localhost:8989/static/" + fileName);
        URLConnection conn = url.openConnection();

        // Read all the text returned by the server
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuffer sb = new StringBuffer();
        String str;
        while ((str = in.readLine()) != null)
        {
            sb.append(str + "\n");
        }
        in.close();

        assertEquals(TEXT, sb.toString().trim());

        // Get headers
        HashMap<String, String> headers = new HashMap<String, String>();
        for (int i = 0;; i++)
        {
            String name = conn.getHeaderFieldKey(i);
            String value = conn.getHeaderField(i);
            if (name == null && value == null)
            {
                break;
            }
            if (name == null)
            {
                _logger.debug("*** Intial Call, Response code: %s", value);
            }
            else
            {
                headers.put(name, value);
                _logger.debug("%s = %s", name, value);
            }
        }
                
        assertEquals("7", headers.get("Content-Length"));
        assertEquals("text/plain", headers.get("Content-Type"));
        
        // Check last modified should be the same as the file's last modified date
        SimpleDateFormat fmt = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
        fmt.setTimeZone(TimeZone.getTimeZone("GMT"));                
        assertEquals(fmt.format(new Date(file.lastModified())), headers.get("Last-Modified"));
        
        // Check Expiry
        Date expires = fmt.parse(headers.get("Expires"));        
        Date serverDate = fmt.parse(headers.get("Date"));        
        Calendar cal = new GregorianCalendar();
        cal.setTime(serverDate);
        cal.add(Calendar.SECOND, AppProperties.getInstance().getWebStaticFilesCacheSeconds());
        assertEquals(cal.getTimeInMillis(), expires.getTime());
                
        // ******************************************************
        // Cache Validation
        // ******************************************************
        url = new URL("http://localhost:8989/static/" + fileName);
        conn = url.openConnection();
        conn.setIfModifiedSince(fmt.parse(headers.get("Last-Modified")).getTime());
        
        in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        sb = new StringBuffer();
        while ((str = in.readLine()) != null)
        {
            sb.append(str + "\n");
        }
        in.close();

        // No content should be returned
        assertEquals("", sb.toString().trim());
        
        HashMap<String, String> headers2 = new HashMap<String, String>();
        String responseCode = "";
        for (int i = 0;; i++)
        {
            String name = conn.getHeaderFieldKey(i);
            String value = conn.getHeaderField(i);
            if (name == null && value == null)
            {
                break;
            }
            if (name == null)
            {
                responseCode = value;
                _logger.debug("*** Cache Call, Response code: %s", value);
            }
            else
            {
                headers2.put(name, value);
                _logger.debug("%s = %s", name, value);
            }
        }
        
        // Should get back a 304
        assertEquals("HTTP/1.1 304 Not Modified", responseCode);
        assertTrue(!StringUtils.isBlank(headers2.get("Date")));
        
        // ******************************************************
        // Finish
        // ******************************************************
        // Clean up
        file.delete();
    }

}
