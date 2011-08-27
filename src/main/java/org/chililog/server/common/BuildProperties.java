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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;

/**
 * <p>
 * BuildProperties provides strongly typed access to build information in the <code>buildinfo.properties</code> file.
 * </p>
 * 
 * <p>
 * The <code>buildinfo.properties</code> file should be in the chililog-server*.jar
 * </p>
 * 
 * <h3>Example</h3>
 * 
 * <pre>
 * BuildProperties.getInstance().getBuildTimestamp();
 * </pre>
 * 
 * <h3>Property Loading</h3>
 * 
 * We use convention to load the properties.
 * <ol>
 * <li>We search for all fields with upper case letters in their names. For example, <code>APP_NAME<code>.</li>
 * <li>We search for the corresponding field cache variable. The field name is converted to camel case and prefixed with
 * underscore. For example, <code>_appName</code></li>
 * <li>Next, we search for a load method to parse the entry in the property file. The field name is converted to camel
 * case and prefixed with "load". For example, <code>loadAppName</code></li>
 * <li>If the method is found, it is called and the result is used to set the cache variable identified in step #2.</li>
 * </ol>
 * 
 * 
 * @author vibul
 * @since 1.0
 */
public class BuildProperties
{
    private static Log4JLogger _logger = Log4JLogger.getLogger(BuildProperties.class);
    private static final String BUILDINFO_PROPERTY_FILE_NAME = "buildinfo.properties";

    /**
     * Returns the singleton instance for this class
     */
    public static BuildProperties getInstance()
    {
        return SingletonHolder.INSTANCE;
    }

    /**
     * SingletonHolder is loaded on the first execution of Singleton.getInstance() or the first access to
     * SingletonHolder.INSTANCE, not before.
     * 
     * @see http://en.wikipedia.org/wiki/Singleton_pattern
     */
    private static class SingletonHolder
    {
        public static final BuildProperties INSTANCE = new BuildProperties();
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
    private BuildProperties()
    {
        try
        {
            loadProperties();
        }
        catch (Exception e)
        {
            _logger.error(e, "Error loading application properties: " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * <p>
     * Loads the configuration information from the <code>app.properties</code> file and caches then as strongly typed
     * values. This method is NOT thread-safe and should only be called for unit-testing.
     * </p>
     * 
     * <p>
     * <code>LoadProperties</code> first loads the default settings form the <code>app.properties</code> file the root
     * classpath and then any overrides from the <code>app.properties</code> file located in the in directory specified
     * in the "chililog.config.dir" system property.
     * </p>
     * 
     * @throws Exception
     */
    public void loadProperties() throws Exception
    {
        Properties properties = readPropertiesFile();
        parseProperties(properties);
    }

    /**
     * <p>
     * Loads the configuration information from the <code>app.properties</code> file.
     * </p>
     * 
     * <p>
     * <code>LoadProperties</code> loads the settings form the <code>buildinfo.properties</code> file found in the
     * classpath
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

            // Load default from class path
            InputStream is = BuildProperties.class.getClassLoader().getResourceAsStream(BUILDINFO_PROPERTY_FILE_NAME);
            if (is == null)
            {
                throw new FileNotFoundException("'buildinfo.properties' file not found in classpath");
            }
            properties.load(is);
            is.close();

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
     * <p>
     * Parses the properties into strongly typed class fields.
     * </p>
     * 
     * <p>
     * Use reflection to simulate the likes of: <code>_appName = loadAppName(properties);</code>
     * </p>
     * 
     * @param properties
     *            Properties to parse
     * @throws Exception
     */
    private void parseProperties(Properties properties) throws Exception
    {
        Class<BuildProperties> cls = BuildProperties.class;
        Field[] ff = cls.getDeclaredFields();
        for (Field f : ff)
        {
            // Look for field names like APP_NAME
            String propertyNameFieldName = f.getName();
            if (!propertyNameFieldName.matches("^[A-Z0-9_]+$"))
            {
                continue;
            }

            // Build cache field (_appName) and method (loadAppName) methods
            String baseName = WordUtils.capitalizeFully(propertyNameFieldName, new char[]
            { '_' });
            baseName = baseName.replace("_", "");
            String cacheMethodName = "load" + baseName;
            String cacheFieldName = "_" + StringUtils.uncapitalize(baseName);

            // If field not exist, then skip
            Field cacheField = null;
            try
            {
                cacheField = cls.getDeclaredField(cacheFieldName);
            }
            catch (NoSuchFieldException e)
            {
                continue;
            }

            // Get and set the value
            Method m = cls.getDeclaredMethod(cacheMethodName, Properties.class);
            Object cacheValue = m.invoke(null, properties);
            cacheField.set(this, cacheValue);
        }

        return;
    }

    /**
     * Returns this application's name - ChiliLog Server.
     */
    public String getAppName()
    {
        return _appName;
    }

    static final String APP_NAME = "application.name";

    private String _appName = null;

    static String loadAppName(Properties properties)
    {
        return loadString(properties, APP_NAME, "ChiliLog Server");
    }

    /**
     * Returns this application's version
     */
    public String getAppVersion()
    {
        return _appVersion;
    }

    static final String APP_VERSION = "application.version";

    private String _appVersion = null;

    static String loadAppVersion(Properties properties)
    {
        return loadString(properties, APP_VERSION);
    }

    /**
     * Returns the date and time when this application build was performed
     */
    public String getBuildTimestamp()
    {
        return _buildTimestamp;
    }

    static final String BUILD_TIMESTAMP = "build.timestamp";

    private String _buildTimestamp = null;

    static String loadBuildTimestamp(Properties properties)
    {
        return loadString(properties, BUILD_TIMESTAMP);
    }

    /**
     * Returns the name of machine on which this application build was performed
     */
    public String getBuildMachineName()
    {
        return _buildMachineName;
    }

    static final String BUILD_MACHINE_NAME = "build.machinename";

    private String _buildMachineName = null;

    static String loadBuildMachineName(Properties properties)
    {
        return loadString(properties, BUILD_MACHINE_NAME);
    }

    /**
     * Returns the user account with which this application build was performed
     */
    public String getBuildUserName()
    {
        return _buildUserName;
    }

    static final String BUILD_USER_NAME = "build.username";

    private String _buildUserName = null;

    static String loadBuildUserName(Properties properties)
    {
        return loadString(properties, BUILD_USER_NAME);
    }

    // *************************************************************************************************************
    // LOAD METHODS
    // *************************************************************************************************************

    /**
     * Loads a string. If it is blank (whitespace, empty or null), then exception is thrown.
     * 
     * @param properties
     *            Properties to lookup
     * @param name
     *            Name of the property
     * 
     * @return Value of the property named <code>name</code>.
     * @throws IllegalArgumentException
     *             if the value of the named properties is blank
     */
    private static String loadString(Properties properties, String name)
    {
        String s = properties.getProperty(name);
        if (StringUtils.isBlank(s))
        {
            throw new IllegalArgumentException(String.format("The property '%s' in '%s' is blank.'", name,
                    BUILDINFO_PROPERTY_FILE_NAME));
        }
        return s;
    }

    /**
     * Loads a string. If it is blank (whitespace, empty or null), then return the <code>defaultValue</code>
     * 
     * @param properties
     *            Properties to lookup
     * @param name
     *            Name of the property
     * @param defaultValue
     *            Value to return if property value is blank.
     * @return Value of the property named <code>name</code>. If whitespace, empty or null, then return the
     *         <code>defaultValue</code>
     */
    private static String loadString(Properties properties, String name, String defaultValue)
    {
        String s = properties.getProperty(name);
        if (StringUtils.isBlank(s))
        {
            return defaultValue;
        }
        return s;
    }

    /**
     * Returns a string representation of the parsed properties
     */
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        Class<BuildProperties> cls = BuildProperties.class;
        for (Field f : cls.getDeclaredFields())
        {
            // Look for field names like APP_NAME
            String propertyNameFieldName = f.getName();
            if (!propertyNameFieldName.matches("^[A-Z0-9_]+$"))
            {
                continue;
            }

            // Build cache field (_appName) and method (loadAppName) methods
            String baseName = WordUtils.capitalizeFully(propertyNameFieldName, new char[]
            { '_' });
            baseName = baseName.replace("_", "");
            String cacheFieldName = "_" + StringUtils.uncapitalize(baseName);

            // If field not exist, then skip
            Field cacheField = null;
            try
            {
                cacheField = cls.getDeclaredField(cacheFieldName);
            }
            catch (NoSuchFieldException e)
            {
                continue;
            }

            // Get the value
            try
            {
                Object o = cacheField.get(this);
                sb.append(f.get(null));
                sb.append(" = ");
                sb.append(o == null ? "<not set>" : o.toString());
                sb.append("\n");
            }
            catch (Exception e)
            {
                sb.append("ERROR: Cannot load value for: " + propertyNameFieldName);
            }

        }

        return sb.toString();
    }
}
