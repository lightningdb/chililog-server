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

package org.chililog.server.common;

import static org.junit.Assert.*;

import java.net.UnknownHostException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.chililog.server.common.AppProperties;
import org.chililog.server.common.SystemProperties;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * JUnit test cases for <code>AppProperties</code>
 * 
 * @author vibul
 * 
 */
public class AppPropertiesTest {

    private static Logger _logger = Logger.getLogger(AppPropertiesTest.class);

    @BeforeClass
    public static void testClassInit() throws Exception {
        // Reload properties
        SystemProperties.getInstance().loadProperties();
    }

    @Before
    @After
    public void testCleanup() throws Exception {
        // Reload properties so that we start with default
        SystemProperties.getInstance().loadProperties();
    }

    @Test
    public void testDbIpAddress() throws UnknownHostException {
        String s = AppProperties.getInstance().getDbIpAddress();
        assertEquals("localhost", s);
    }

    @Test
    public void testDbIpPort() throws UnknownHostException {
        int s = AppProperties.getInstance().getDbIpPort();
        assertEquals(27017, s);
    }

    @Test
    public void testDbName() throws UnknownHostException {
        String s = AppProperties.getInstance().getDbName();
        assertEquals("chililog", s);
    }

    @Test
    public void testDbUserName() throws UnknownHostException {
        String s = AppProperties.getInstance().getDbUserName();
        assertEquals("chililog", s);
    }

    @Test
    public void testDbPassowrd() throws UnknownHostException {
        String s = AppProperties.getInstance().getDbPassword();
        assertEquals("chililog12", s);
    }

    @Test
    public void testDbConnectionsPerHost() throws UnknownHostException {
        int i = AppProperties.getInstance().getDbConnectionsPerHost();
        assertEquals(10, i);
    }

    @Test
    public void testMqJournallingEnabled() throws UnknownHostException {
        assertFalse(AppProperties.getInstance().getMqJournallingEnabled());
    }

    @Test
    public void testMqJournalDirectory() throws UnknownHostException {
        String s = AppProperties.getInstance().getMqJournalDirectory();
        assertEquals("/tmp/chililog/journal", s);
    }

    @Test
    public void testMqPagingDirectory() throws UnknownHostException {
        String s = AppProperties.getInstance().getMqPagingDirectory();
        assertEquals("/tmp/chililog/paging", s);
    }

    @Test
    public void testMqSecurityInvalidationInterval() throws UnknownHostException {
        int i = AppProperties.getInstance().getMqSecurityInvalidationInterval();
        assertEquals(0, i);
    }

    @Test
    public void testMqClusteredEnabled() throws UnknownHostException {
        assertFalse(AppProperties.getInstance().getMqClusteredEnabled());
    }

    @Test
    public void testMqRedeliveryMaxAttempts() throws UnknownHostException {
        assertEquals(3, AppProperties.getInstance().getMqRedeliveryMaxAttempts());
    }

    @Test
    public void testMqRedeliveryDelayMilliSeconds() throws UnknownHostException {
        assertEquals(3000, AppProperties.getInstance().getMqRedeliveryDelayMilliseconds());
    }

    @Test
    public void testPubSubCoreProtocolEnabled() throws UnknownHostException {
        assertTrue(AppProperties.getInstance().getPubSubCoreProtocolEnabled());
    }

    @Test
    public void testPubSubJsonHttpEnabled() throws UnknownHostException {
        assertTrue(AppProperties.getInstance().getPubSubJsonHttpEnabled());
    }

    @Test
    public void testPubSubJsonHttpHost() throws UnknownHostException {
        String s = AppProperties.getInstance().getPubSubJsonHttpHost();
        assertEquals("0.0.0.0", s);
        assertTrue(AppProperties.getInstance().toString().contains(AppProperties.PUB_SUB_JSON_HTTP_HOST));
    }

    @Test
    public void testPubSubJsonHttpPort() throws UnknownHostException {
        int s = AppProperties.getInstance().getPubSubJsonHttpPort();
        assertEquals(61615, s);
        assertTrue(AppProperties.getInstance().toString().contains(AppProperties.PUB_SUB_JSON_HTTP_PORT));
    }

    @Test
    public void testPubSubJsonHttpNettyWorkerThreadPoolSize() throws UnknownHostException {
        int s = AppProperties.getInstance().getPubSubJsonHttpNettyWorkerThreadPoolSize();
        assertEquals(0, s);
        assertTrue(AppProperties.getInstance().toString()
                .contains(AppProperties.PUB_SUB_JSON_HTTP_NETTY_WORKER_THREAD_POOL_SIZE));
    }
    
    @Test
    public void testPubSubJsonHttpNettyHandlerThreadPoolSize() throws UnknownHostException {
        int s = AppProperties.getInstance().getPubSubJsonHttpNettyHandlerThreadPoolSize();
        assertEquals(16, s);
        assertTrue(AppProperties.getInstance().toString()
                .contains(AppProperties.PUB_SUB_JSON_HTTP_NETTY_HANDLER_THREAD_POOL_SIZE));
    }

    @Test
    public void testPubSubJsonHttpNettyHandlerThreadPoolMaxChannelMemorySize() throws UnknownHostException {
        long s = AppProperties.getInstance().getPubSubJsonHttpNettyHandlerThreadPoolMaxChannelMemorySize();
        assertEquals(0, s);
        assertTrue(AppProperties.getInstance().toString()
                .contains(AppProperties.PUB_SUB_JSON_HTTP_NETTY_HANDLER_THREAD_POOL_MAX_CHANNEL_MEMORY_SIZE));
    }

    @Test
    public void testPubSubJsonHttpNettyHandlerThreadPoolMaxTotalMemorySize() throws UnknownHostException {
        long s = AppProperties.getInstance().getPubSubJsonHttpNettyHandlerThreadPoolMaxTotalMemorySize();
        assertEquals(0, s);
        assertTrue(AppProperties.getInstance().toString()
                .contains(AppProperties.PUB_SUB_JSON_HTTP_NETTY_HANDLER_THREAD_POOL_MAX_TOTAL_MEMORY_SIZE));
    }

    @Test
    public void testPubSubJsonHttpNettyHandlerThreadPoolKeepAliveSeconds() throws UnknownHostException {
        int s = AppProperties.getInstance().getPubSubJsonHttpNettyHandlerThreadPoolKeepAliveSeconds();
        assertEquals(3, s);
        assertTrue(AppProperties.getInstance().toString()
                .contains(AppProperties.PUB_SUB_JSON_HTTP_NETTY_HANDLER_THREAD_POOL_KEEP_ALIVE_SECONDS));
    }

    @Test
    public void testPubSubJsonHttpSSLEnabled() throws UnknownHostException {
        boolean b = AppProperties.getInstance().getPubSubJsonHttpSslEnabled();
        assertFalse(b);
        assertTrue(AppProperties.getInstance().toString()
                .contains(AppProperties.PUB_SUB_JSON_HTTP_SSL_ENABLED));
    }

    @Test
    public void testPubSubJsonHttpKeyStorePath() throws UnknownHostException {
        String s = AppProperties.getInstance().getPubSubJsonHttpKeyStorePath();
        assertTrue(StringUtils.isBlank(s));
        assertTrue(AppProperties.getInstance().toString()
                .contains(AppProperties.PUB_SUB_JSON_HTTP_KEY_STORE_PATH));
    }

    @Test
    public void testPubSubJsonHttpKeyStorePassword() throws UnknownHostException {
        String s = AppProperties.getInstance().getPubSubJsonHttpKeyStorePassword();
        assertTrue(StringUtils.isBlank(s));
        assertTrue(AppProperties.getInstance().toString()
                .contains(AppProperties.PUB_SUB_JSON_HTTP_KEY_STORE_PASSWORD));
    }

    @Test
    public void testPubSubJsonHttpKeyStoreKeyPassword() throws UnknownHostException {
        String s = AppProperties.getInstance().getPubSubJsonHttpKeyStoreKeyPassword();
        assertTrue(StringUtils.isBlank(s));
        assertTrue(AppProperties.getInstance().toString()
                .contains(AppProperties.PUB_SUB_JSON_HTTP_KEY_STORE_KEY_PASSWORD));
    }

    @Test
    public void testWorkbenchEnabled() throws UnknownHostException {
        assertTrue(AppProperties.getInstance().getWorkbenchEnabled());
    }

    @Test
    public void testWorkbenchHost() throws UnknownHostException {
        String s = AppProperties.getInstance().getWorkbenchHost();
        assertEquals("0.0.0.0", s);
        assertTrue(AppProperties.getInstance().toString().contains(AppProperties.WORKBENCH_HOST));
    }

    @Test
    public void testWorkbenchPort() throws UnknownHostException {
        int s = AppProperties.getInstance().getWorkbenchPort();
        assertEquals(8989, s);
        assertTrue(AppProperties.getInstance().toString().contains(AppProperties.WORKBENCH_PORT));
    }

    @Test
    public void testWorkbenchNettyWorkerThreadPoolSize() throws UnknownHostException {
        int s = AppProperties.getInstance().getWorkbenchNettyWorkerThreadPoolSize();
        assertEquals(0, s);
        assertTrue(AppProperties.getInstance().toString().contains(AppProperties.WORKBENCH_NETTY_WORKER_THREAD_POOL_SIZE));
    }
    
    @Test
    public void testWorkbenchNettyHandlerThreadPoolSize() throws UnknownHostException {
        int s = AppProperties.getInstance().getWorkbenchNettyHandlerThreadPoolSize();
        assertEquals(16, s);
        assertTrue(AppProperties.getInstance().toString().contains(AppProperties.WORKBENCH_NETTY_HANDLER_THREAD_POOL_SIZE));
    }

    @Test
    public void testWorkbenchNettyHandlerThreadPoolMaxChannelMemorySize() throws UnknownHostException {
        long s = AppProperties.getInstance().getWorkbenchNettyHandlerThreadPoolMaxChannelMemorySize();
        assertEquals(0, s);
        assertTrue(AppProperties.getInstance().toString()
                .contains(AppProperties.WORKBENCH_NETTY_HANDLER_THREAD_POOL_MAX_CHANNEL_MEMORY_SIZE));
    }

    @Test
    public void testWorkbenchNettyHandlerThreadPoolMaxTotalMemorySize() throws UnknownHostException {
        long s = AppProperties.getInstance().getWorkbenchNettyHandlerThreadPoolMaxTotalMemorySize();
        assertEquals(0, s);
        assertTrue(AppProperties.getInstance().toString()
                .contains(AppProperties.WORKBENCH_NETTY_HANDLER_THREAD_POOL_MAX_TOTAL_MEMORY_SIZE));
    }

    @Test
    public void testWorkbenchNettyHandlerThreadPoolKeepAliveSeconds() throws UnknownHostException {
        int s = AppProperties.getInstance().getWorkbenchNettyHandlerThreadPoolKeepAliveSeconds();
        assertEquals(3, s);
        assertTrue(AppProperties.getInstance().toString()
                .contains(AppProperties.WORKBENCH_NETTY_HANDLER_THREAD_POOL_KEEP_ALIVE_SECONDS));
    }

    @Test
    public void testWorkbenchSSLEnabled() throws UnknownHostException {
        boolean b = AppProperties.getInstance().getWorkbenchSslEnabled();
        assertFalse(b);
        assertTrue(AppProperties.getInstance().toString().contains(AppProperties.WORKBENCH_SSL_ENABLED));
    }

    @Test
    public void testWorkbenchKeyStorePath() throws UnknownHostException {
        String s = AppProperties.getInstance().getWorkbenchKeyStorePath();
        assertTrue(StringUtils.isBlank(s));
        assertTrue(AppProperties.getInstance().toString().contains(AppProperties.WORKBENCH_KEY_STORE_PATH));
    }

    @Test
    public void testWorkbenchKeyStorePassword() throws UnknownHostException {
        String s = AppProperties.getInstance().getWorkbenchKeyStorePassword();
        assertTrue(StringUtils.isBlank(s));
        assertTrue(AppProperties.getInstance().toString().contains(AppProperties.WORKBENCH_KEY_STORE_PASSWORD));
    }

    @Test
    public void testWorkbenchKeyStoreKeyPassword() throws UnknownHostException {
        String s = AppProperties.getInstance().getWorkbenchKeyStoreKeyPassword();
        assertTrue(StringUtils.isBlank(s));
        assertTrue(AppProperties.getInstance().toString().contains(AppProperties.WORKBENCH_KEY_STORE_KEY_PASSWORD));
    }

    @Test
    public void testWorkbenchKeyStaticFilesDirectory() throws UnknownHostException {
        String s = AppProperties.getInstance().getWorkbenchStaticFilesDirectory();
        assertTrue(s.equals("./src/main/sc2") || s.equals("../workbench")); // cater for debug and release builds
        assertTrue(AppProperties.getInstance().toString().contains(AppProperties.WORKBENCH_STATIC_FILES_DIRECTORY));
    }

    @Test
    public void testWorkbenchStaticFilesCacheSeconds() throws UnknownHostException {
        int s = AppProperties.getInstance().getWorkbenchStaticFilesCacheSeconds();
        assertTrue(s == 3 || s == 31535000); // cater for debug and release builds
        assertTrue(AppProperties.getInstance().toString().contains(AppProperties.WORKBENCH_STATIC_FILES_CACHE_SECONDS));
    }

    @Test
    public void testToString() {
        String s = AppProperties.getInstance().toString();
        assertTrue(StringUtils.isNotBlank(s));
        _logger.debug("\n" + s);
    }

}
