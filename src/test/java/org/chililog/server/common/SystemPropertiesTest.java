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

import java.io.File;
import java.io.FileNotFoundException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.chililog.server.common.SystemProperties;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


/**
 * Test cases for <code>SystemProperties</code>
 * 
 * @author vibul
 * 
 */
public class SystemPropertiesTest
{	
	private static Logger _logger = Logger.getLogger(SystemPropertiesTest.class);

	@Before
	@After
	public void testCleanup() throws Exception
	{
		System.setProperty(SystemProperties.CHILILOG_CONFIG_DIRECTORY, StringUtils.EMPTY);
		
		// Reload properties
		SystemProperties.getInstance().loadProperties();
	}
	
	@Test(expected = FileNotFoundException.class)
	public void testLoadChiliLogConfigDirectory_DirectoryNotFound() throws Exception
	{
		System.setProperty(SystemProperties.CHILILOG_CONFIG_DIRECTORY, "baddirectoryname");
		SystemProperties.loadChiliLogConfigDirectory();
	}
	
	@Test(expected = FileNotFoundException.class)
	public void testLoadChiliLogConfigDirectory_NotADirectory() throws Exception
	{
		String s = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
		if (System.getProperty("os.name").contains("windows"))
		{
			s = s + ".exe";
		}
		_logger.debug("Test File: " + s.toString());
		
		System.setProperty(SystemProperties.CHILILOG_CONFIG_DIRECTORY, s);
		SystemProperties.loadChiliLogConfigDirectory();
	}
	
	@Test
	public void testLoadChiliLogConfigDirectory_NotSet() throws Exception
	{
		File f = SystemProperties.loadChiliLogConfigDirectory();
		assertNull(f);
	}
	
	@Test
	public void testLoadChiliLogConfigDirectory_OK() throws Exception
	{
		String s = System.getProperty("java.home");
		System.setProperty(SystemProperties.CHILILOG_CONFIG_DIRECTORY, s);

		File f = SystemProperties.loadChiliLogConfigDirectory();
		assertNotNull(f);
		assertTrue(s.toString().equalsIgnoreCase(f.getPath()));
	}	

	@Test
	public void testGetChiliLogConfigDirectory_Empty() throws Exception
	{
		File f = SystemProperties.getInstance().getChiliLogConfigDirectory();
		assertNull(f);
	}	

	@Test
	public void testGetChiliLogConfigDirectory_OK() throws Exception
	{
		String s = System.getProperty("java.home");
		System.setProperty(SystemProperties.CHILILOG_CONFIG_DIRECTORY, s);

		// Load properties
		SystemProperties.getInstance().loadProperties();

		File f = SystemProperties.getInstance().getChiliLogConfigDirectory();
		assertNotNull(f);
		assertTrue(s.toString().equalsIgnoreCase(f.getPath()));
	}	

	
	@Test
	public void testToString()
	{
		String s = SystemProperties.getInstance().toString();
		assertTrue(StringUtils.isNotBlank(s));
		_logger.debug("\n" + s);
	}
	
}
