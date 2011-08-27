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
import org.chililog.server.common.SystemProperties;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 * JUnit test cases for <code>BuildProperties</code>
 * 
 * @author vibul
 * 
 */
public class BuildPropertiesTest
{
    private static Logger _logger = Logger.getLogger(BuildPropertiesTest.class);

    @BeforeClass
    public static void testClassInit() throws Exception
    {
        // Reload properties
        SystemProperties.getInstance().loadProperties();
    }

    @Before
    @After
    public void testCleanup() throws Exception
    {
        // Reload properties so that we start with default
        SystemProperties.getInstance().loadProperties();
    }

    @Test
    public void testAppName()
    {
        String s = BuildProperties.getInstance().getAppName();
        assertTrue(s.equalsIgnoreCase("ChiliLog Server"));
    }

    @Test
    public void testBuildTimestamp()
    {
        String s = BuildProperties.getInstance().getBuildTimestamp();
        assertTrue(StringUtils.isNotBlank(s));
    }

    @Test
    public void testBuildMachineName() throws UnknownHostException
    {
        String s = BuildProperties.getInstance().getBuildMachineName();
        assertEquals(java.net.InetAddress.getLocalHost().getHostName(), s);
    }

    @Test
    public void testBuildUserName() throws UnknownHostException
    {
        String s = BuildProperties.getInstance().getBuildUserName();
        assertEquals(System.getProperty("user.name"), s);
    }
    
    @Test
    public void testToString()
    {
        String s = BuildProperties.getInstance().toString();
        assertTrue(StringUtils.isNotBlank(s));
        _logger.debug("\n" + s);
    }

}
