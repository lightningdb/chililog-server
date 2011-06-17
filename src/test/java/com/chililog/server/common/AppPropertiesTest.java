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

package com.chililog.server.common;

import static org.junit.Assert.*;

import java.io.File;
import java.net.UnknownHostException;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.chililog.server.common.AppProperties;
import com.chililog.server.common.SystemProperties;

/**
 * JUnit test cases for <code>AppProperties</code>
 * 
 * @author vibul
 * 
 */
public class AppPropertiesTest
{
    private static Logger _logger = Logger.getLogger(AppPropertiesTest.class);

    private static File _tempDir = null;

    @BeforeClass
    public static void testClassInit() throws Exception
    {
        String baseTempPath = System.getProperty("java.io.tmpdir");
        _tempDir = new File(baseTempPath + File.separator + "tempDir_" + new Date().getTime());
        if (_tempDir.exists() == false)
        {
            _tempDir.mkdir();
        }
        _tempDir.deleteOnExit();
        _logger.debug("_tempDir=" + _tempDir.getPath());

        System.setProperty(SystemProperties.CHILILOG_CONFIG_DIRECTORY, StringUtils.EMPTY);

        // Reload properties
        SystemProperties.getInstance().loadProperties();
    }

    @Before
    @After
    public void testCleanup() throws Exception
    {
        // No override
        System.setProperty(SystemProperties.CHILILOG_CONFIG_DIRECTORY, StringUtils.EMPTY);
        for (File f : _tempDir.listFiles())
        {
            f.delete();
        }

        // Reload properties so that we start with default
        SystemProperties.getInstance().loadProperties();
    }

    @Test
    public void testAppName()
    {
        String s = AppProperties.getInstance().getAppName();
        assertTrue(s.equalsIgnoreCase("ChiliLog Server"));
    }

    @Test
    public void testBuildTimestamp()
    {
        String s = AppProperties.getInstance().getBuildTimestamp();
        assertTrue(StringUtils.isNotBlank(s));
    }

    @Test
    public void testBuildMachineName() throws UnknownHostException
    {
        String s = AppProperties.getInstance().getBuildMachineName();
        assertEquals(java.net.InetAddress.getLocalHost().getHostName(), s);
    }

    @Test
    public void testBuildUserName() throws UnknownHostException
    {
        String s = AppProperties.getInstance().getBuildUserName();
        assertEquals(System.getProperty("user.name"), s);
    }
    
    @Test
    public void testDbIpAddress() throws UnknownHostException
    {
        String s = AppProperties.getInstance().getDbIpAddress();
        assertEquals("localhost", s);
    }

    @Test
    public void testDbIpPort() throws UnknownHostException
    {
        int s = AppProperties.getInstance().getDbIpPort();
        assertEquals(27017, s);
    }

    @Test
    public void testDbName() throws UnknownHostException
    {
        String s = AppProperties.getInstance().getDbName();
        assertEquals("chililog", s);
    }

    @Test
    public void testDbUserName() throws UnknownHostException
    {
        String s = AppProperties.getInstance().getDbUserName();
        assertEquals("chililog", s);
    }

    @Test
    public void testDbPassowrd() throws UnknownHostException
    {
        String s = AppProperties.getInstance().getDbPassword();
        assertEquals("chililog12", s);
    }

    @Test
    public void testDbConnectionsPerHost() throws UnknownHostException
    {
        int i = AppProperties.getInstance().getDbConnectionsPerHost();
        assertEquals(10, i);
    }
    
    @Test
    public void testMqJournallingEnabled() throws UnknownHostException
    {
        assertFalse(AppProperties.getInstance().getMqJournallingEnabled());
    }

    @Test
    public void testMqJournalDirectory() throws UnknownHostException
    {
        String s = AppProperties.getInstance().getMqJournalDirectory();
        assertEquals("/tmp/chililog/journal", s);
    }

    @Test
    public void testMqPagingDirectory() throws UnknownHostException
    {
        String s = AppProperties.getInstance().getMqPagingDirectory();
        assertEquals("/tmp/chililog/paging", s);
    }

    @Test
    public void testMqSecurityInvalidationInterval() throws UnknownHostException
    {
        int i = AppProperties.getInstance().getMqSecurityInvalidationInterval();
        assertEquals(0, i);
    }

    @Test
    public void testMqClusteredEnabled() throws UnknownHostException
    {
        assertFalse(AppProperties.getInstance().getMqClusteredEnabled());
    }

    @Test
    public void testMqRedeliveryMaxAttempts() throws UnknownHostException
    {
        assertEquals(3, AppProperties.getInstance().getMqRedeliveryMaxAttempts());
    }

    @Test
    public void testMqRedeliveryDelayMilliSeconds() throws UnknownHostException
    {
        assertEquals(3000, AppProperties.getInstance().getMqRedeliveryDelayMilliseconds());
    }

    @Test
    public void testMqCoreProtocolEnabled() throws UnknownHostException
    {
        assertTrue(AppProperties.getInstance().getMqCoreProtocolEnabled());
    }

    @Test
    public void testMqStompProtocolEnabled() throws UnknownHostException
    {
        assertTrue(AppProperties.getInstance().getMqStompProtocolEnabled());
    }

    @Test
    public void testMqStompWebSocketProtocolEnabled() throws UnknownHostException
    {
        assertFalse(AppProperties.getInstance().getMqStompWebSocketProtocolEnabled());
    }

    @Test
    public void testWebIpAddress() throws UnknownHostException
    {
        String s = AppProperties.getInstance().getWebIpAddress();
        assertEquals("localhost", s);
        assertTrue(AppProperties.getInstance().toString().contains(AppProperties.WEB_IP_ADDRESS));
    }

    @Test
    public void testWebIpPort() throws UnknownHostException
    {
        int s = AppProperties.getInstance().getWebIpPort();
        assertEquals(8989, s);
        assertTrue(AppProperties.getInstance().toString().contains(AppProperties.WEB_IP_PORT));
    }

    @Test
    public void testWebSSLEnabled() throws UnknownHostException
    {
        boolean b = AppProperties.getInstance().getWebSslEnabled();
        assertFalse(b);
        assertTrue(AppProperties.getInstance().toString().contains(AppProperties.WEB_SSL_ENABLED));
    }

    @Test
    public void testWebKeyStorePath() throws UnknownHostException
    {
        String s = AppProperties.getInstance().getWebKeyStorePath();
        assertTrue(StringUtils.isBlank(s));
        assertTrue(AppProperties.getInstance().toString().contains(AppProperties.WEB_KEY_STORE_PATH));
    }

    @Test
    public void testWebKeyStorePassword() throws UnknownHostException
    {
        String s = AppProperties.getInstance().getWebKeyStorePassword();
        assertTrue(StringUtils.isBlank(s));
        assertTrue(AppProperties.getInstance().toString().contains(AppProperties.WEB_KEY_STORE_PASSWORD));
    }

    @Test
    public void testWebKeyStoreKeyPassword() throws UnknownHostException
    {
        String s = AppProperties.getInstance().getWebKeyStoreKeyPassword();
        assertTrue(StringUtils.isBlank(s));
        assertTrue(AppProperties.getInstance().toString().contains(AppProperties.WEB_KEY_STORE_KEY_PASSWORD));
    }

    @Test
    public void testWebKeyStaticFilesDirectory() throws UnknownHostException
    {
        String s = AppProperties.getInstance().getWebStaticFilesDirectory();
        assertTrue(s.equals("/tmp") || s.equals("./static")); // cater for debug and release builds
        assertTrue(AppProperties.getInstance().toString().contains(AppProperties.WEB_STATIC_FILES_DIRECTORY));
    }

    @Test
    public void testWebStaticFilesCacheSeconds() throws UnknownHostException
    {
        int s = AppProperties.getInstance().getWebStaticFilesCacheSeconds();
        assertTrue(s == 3 || s == 31535000); // cater for debug and release builds
        assertTrue(AppProperties.getInstance().toString().contains(AppProperties.WEB_STATIC_FILES_CACHE_SECONDS));
    }

    @Test
    public void testToString()
    {
        String s = AppProperties.getInstance().toString();
        assertTrue(StringUtils.isNotBlank(s));
        _logger.debug("\n" + s);
    }

}
