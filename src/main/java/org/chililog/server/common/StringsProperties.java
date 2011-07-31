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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * <p>
 * StringProperties provides access to strings in <code>strings.properties</code> file.
 * </p>
 * 
 * <p>
 * The <code>strings.properties</code> file in the root classpath contains the default configuration.
 * </p>
 * 
 * <h3>Example</h3>
 * 
 * <code>
 * StringsProperties.getInstance().getString("ABC");
 * </code>
 * 
 * @author vibul
 * @since 1.0
 */
public class StringsProperties
{
    private static Logger _logger = Logger.getLogger(StringsProperties.class);
    private static final String PROPERTY_FILE_NAME = "strings.properties";

    private Properties _properties;

    /**
     * Returns the singleton instance for this class
     */
    public static StringsProperties getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    /**
     * SingletonHolder is loaded on the first execution of Singleton.getInstance() or the first access to
     * SingletonHolder.INSTANCE, not before.
     * 
     * See http://en.wikipedia.org/wiki/Singleton_pattern
     */
    private static class SingletonHolder
    {
        public static final StringsProperties INSTANCE = new StringsProperties();
    }

    /**
     * <p>
     * Singleton constructor that parses and loads the required application properties.
     * </p>
     * 
     * <p>
     * If there are any errors, the JVM is terminated. Without valid application properties, we will fall over elsewhere
     * so might as well terminate here.
     * </p>
     */
    private StringsProperties()
    {
        try
        {
            _properties = readPropertiesFile();
        }
        catch (Exception e)
        {
            _logger.error("Error loading application properties: " + e.getMessage(), e);
            System.exit(1);
        }
    }

    /**
     * <p>
     * Loads the configuration information from the <code>app.properties</code> file.
     * </p>
     * 
     * <p>
     * <code>LoadProperties</code> first loads the default settings form the <code>app.properties</code> file the root
     * classpath and then any overrides from the <code>app.properties</code> file located in the in directory specified
     * in the "chililog.config.dir" system property.
     * </p>
     * 
     * @throws IOException
     * @throws FileNotFoundException
     */
    static Properties readPropertiesFile() throws FileNotFoundException, IOException
    {
        FileInputStream fis = null;

        try
        {
            Properties properties = new Properties();

            // Load from class path
            URL url = ClassLoader.getSystemResource(PROPERTY_FILE_NAME);
            fis = new FileInputStream(new File(url.getFile()));
            properties.load(fis);
            fis.close();
            fis = null;

            // Load overrides
            File configDirectory = SystemProperties.getInstance().getChiliLogConfigDirectory();
            if (configDirectory != null)
            {
                File configFile = new File(configDirectory, PROPERTY_FILE_NAME);
                if (configFile.exists())
                {
                    fis = new FileInputStream(configFile);
                    properties.load(fis);
                    fis.close();
                    fis = null;
                }
            }

            return properties;
        }
        finally
        {
            if (fis != null)
            {
                fis.close();
            }
        }
    }

    /**
     * Gets the string identified by <code>stringCode</code>.
     * 
     * @param stringCode
     *            Id of the string in strings.properties file
     * @return String or null if not found.
     */
    public String getString(String stringCode)
    {
        return _properties.getProperty(stringCode);
    }
 
    /**
     * Gets the string identified by <code>stringCode</code>.  If not found, <code>defaultValue</code> is returned.
     * 
     * @param stringCode
     *            Id of the string in strings.properties file
     * @param defaultValue
     *            String to return if the string associated with <code>stringCode</code> is null, empty or whitespaces.
     * @return String or defaultValue if not found.
     */
    public String getString(String stringCode, String defaultValue)
    {
        String s = _properties.getProperty(stringCode);
        return StringUtils.isBlank(s) ? defaultValue : s;
    }
}
