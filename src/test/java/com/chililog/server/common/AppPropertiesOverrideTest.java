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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
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
 * <p>
 * JUnit test cases for <code>AppProperties</code> for when values are overridden in a custom app.properties file.
 * </p>
 * <p>
 * This test MUST BE EXCLUDED from a batch run because it changes auto-generated values like jaas.system_username. These
 * values are cached by some singletons; and once changed the cached values are wrong. We cache for speed so we don't
 * want to give up speed.
 * </p>
 * 
 * @author vibul
 * 
 */
public class AppPropertiesOverrideTest
{
    private static Logger _logger = Logger.getLogger(AppPropertiesOverrideTest.class);

    private static File _tempDir = null;
    private static File _overrideFile = null;

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

        _overrideFile = new File(_tempDir, "app.properties");

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
    public void testAppName_Override() throws Exception
    {
        loadOverride(String.format("%s=%s\n", AppProperties.APP_NAME, "test123"));

        String s = AppProperties.getInstance().getAppName();
        assertTrue(s.equalsIgnoreCase("test123"));
    }

    @Test
    public void testToString()
    {
        String s = AppProperties.getInstance().toString();
        assertTrue(StringUtils.isNotBlank(s));
        _logger.debug("\n" + s);
    }

    /**
     * Load override variables
     * 
     * @param overrideProperties
     * @throws Exception
     */
    private void loadOverride(String overrideProperties) throws Exception
    {
        BufferedWriter writer = new BufferedWriter(new FileWriter(_overrideFile));
        writer.write(overrideProperties);
        writer.close();

        // Reload system properties
        System.setProperty(SystemProperties.CHILILOG_CONFIG_DIRECTORY, _tempDir.getPath());
        SystemProperties.getInstance().loadProperties();

        // Reload app properties
        AppProperties.getInstance().loadProperties();
    }
}
