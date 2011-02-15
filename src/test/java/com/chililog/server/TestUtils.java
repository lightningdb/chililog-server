
package com.chililog.server;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Date;

import com.chililog.server.common.AppProperties;
import com.chililog.server.common.Log4JLogger;
import com.chililog.server.common.SystemProperties;

public class TestUtils
{
    private static Log4JLogger _logger = Log4JLogger.getLogger(TestUtils.class);

    /**
     * Load override application property variables for testing
     * 
     * @param overrideProperties
     *            name=value string of override properties
     * @throws Exception
     */
    public static void overrideAppProperties(String overrideProperties) throws Exception
    {
        File tempDir = null;
        File overrideFile = null;

        String baseTempPath = System.getProperty("java.io.tmpdir");
        tempDir = new File(baseTempPath + File.separator + "tempDir_" + new Date().getTime());
        if (tempDir.exists() == false)
        {
            tempDir.mkdir();
        }
        tempDir.deleteOnExit();

        overrideFile = new File(tempDir, "app.properties");

        BufferedWriter writer = new BufferedWriter(new FileWriter(overrideFile));
        writer.write(overrideProperties);
        writer.close();

        // Reload system properties
        System.setProperty(SystemProperties.CHILILOG_CONFIG_DIRECTORY, tempDir.getPath());
        SystemProperties.getInstance().loadProperties();

        // Reload app properties
        AppProperties.getInstance().loadProperties();
        
        _logger.debug("Overriding App Properties: %s", overrideProperties);
    }
}
