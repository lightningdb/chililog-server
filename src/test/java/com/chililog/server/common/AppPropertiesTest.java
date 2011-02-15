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
    public void testJaasLoginModuleClassName() throws UnknownHostException
    {
        String s = AppProperties.getInstance().getJaasLoginModuleClassName();
        assertEquals("com.chililog.server.security.MongoDBJAASLoginModule", s);        
    }

    @Test
    public void testJaasConfigurationClassName() throws UnknownHostException
    {
        String s = AppProperties.getInstance().getJaasConfigurationClassName();
        assertEquals("com.chililog.server.security.JAASConfiguration", s);        
    }

    @Test
    public void testJaasConfigurationName() throws UnknownHostException
    {
        String s = AppProperties.getInstance().getJaasConfigurationName();
        assertEquals("notused", s);        
    }

    @Test
    public void testJaasCallbackHandlerClassName() throws UnknownHostException
    {
        String s = AppProperties.getInstance().getJaasCallbackHandlerClassName();
        assertEquals("com.chililog.server.security.JAASCallbackHandler", s);        
    }

    @Test
    public void testJaasSystemUsername() throws UnknownHostException
    {
        String s = AppProperties.getInstance().getJaasSystemUsername();
        assertTrue(!StringUtils.isBlank(s));
    }
    
    @Test
    public void testJaasSystemPassword() throws UnknownHostException
    {
        String s = AppProperties.getInstance().getJaasSystemPassword();
        assertTrue(!StringUtils.isBlank(s));
    }

    public void testJaasSystemRole() throws UnknownHostException
    {
        String s = AppProperties.getInstance().getJaasSystemRole();
        assertEquals("system", s);        
    }

    @Test
    public void testMqPersistenceEnabled() throws UnknownHostException
    {
        assertFalse(AppProperties.getInstance().getMqPersistenceEnabled());
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
	public void testToString()
	{
		String s = AppProperties.getInstance().toString();
		assertTrue(StringUtils.isNotBlank(s));
		_logger.debug("\n" + s);
	}
	
}
