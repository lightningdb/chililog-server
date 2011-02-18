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

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.log4j.Logger;

/**
 * <p>
 * SystemProperties provides strongly typed access to our system properties.
 * </p>
 * 
 * <h3>Example</h3>
 * 
 * <code>
 * SystemProperties.getInstance().getChiliLogConfigDirectory();
 * </code>
 * 
 * <h3>Property Loading</h3>
 * 
 * We use convention to load the properties.
 * <ol>
 * <li>We search for all fields with upper case letters in their names. For example, <code>APP_NAME<code>.</li>
 * <li>We search for the corresponding field cache variable. The field name is converted to camel case and prefixed with
 * underscore. For example, <code>_appName</code>li>
 * <li>Next, we search for a load method to parse the entry in the property file. The field name is converted to camel
 * case and prefixed with "load". For example, <code>loadAppName</code></li>
 * <li>If the method is found, it is called and the result is used to set the cache variable identified in step #2.</li>
 * </ol>
 * 
 * <h3>Setting a System Property</h3>
 * 
 * Start your JVM with -D command line flag. For example: <code>-Dchililog.config.directory=/home/chililog/config</code>.
 * 
 * @author vibul
 * @since 1.0
 */
public class SystemProperties
{
    private static Logger _logger = Logger.getLogger(SystemProperties.class);

    /**
     * Returns the singleton instance for this class
     */
    public static SystemProperties getInstance()
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
        public static final SystemProperties INSTANCE = new SystemProperties();
    }

    /**
     * <p>
     * Singleton constructor that parses and loads the required system properties.
     * </p>
     * 
     * <p>
     * If there are any errors, the JVM is terminated. Without valid system properties, we will fall over elsewhere so
     * might as well terminate here.
     * </p>
     */
    private SystemProperties()
    {
        try
        {
            loadProperties();
        }
        catch (Exception e)
        {
            _logger.error("Error loading system properties: " + e.getMessage(), e);
            System.exit(1);
        }
    }

    /**
     * <p>
     * Loads/Reloads the properties. This method is NOT thread-safe and should only be called for unit-testing.
     * </p>
     * 
     * <p>
     * Use reflection to simulate the likes of:
     * <code>_chiliLogConfigDirectory = loadChiliLogConfigDirectory(properties);</code>
     * </p>
     * 
     * @throws Exception
     */
    public void loadProperties() throws Exception
    {
        Class<SystemProperties> cls = SystemProperties.class;
        for (Field f : cls.getDeclaredFields())
        {
            // Look for field names like CHILILOG_CONFIG_DIRECTORY
            String propertyNameFieldName = f.getName();
            if (!propertyNameFieldName.matches("^[A-Z0-9_]+$"))
            {
                continue;
            }

            // Build cache field (_chiliLogConfigDirectory) and method (loadChiliLogConfigDirectory) names
            String baseName = WordUtils.capitalizeFully(propertyNameFieldName, new char[]
            { '_' });
            baseName = baseName.replace("Chililog", "ChiliLog").replace("_", "");
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
            Method m = cls.getDeclaredMethod(cacheMethodName, (Class[]) null);
            Object cacheValue = m.invoke(null, (Object[]) null);
            cacheField.set(this, cacheValue);
        }

        return;
    }

    /**
     * <p>
     * Returns the directory containing the application configuration files that overrides the default configuration
     * files as stored in the classpath.
     * </p>
     * 
     * @return The directory encapsulated as a <code>File</code> object; or null if not set.
     */
    public File getChiliLogConfigDirectory()
    {
        return _chiliLogConfigDirectory;
    }

    public static final String CHILILOG_CONFIG_DIRECTORY = "chililog.config.directory";

    private File _chiliLogConfigDirectory = null;

    /**
     * Loads the application configuration directory encapsulated as a <code>File</code> object; or null if not set.
     * 
     * @return The directory encapsulated as a <code>File</code> object; or null if not set.
     * @throws FileNotFoundException
     *             if a directory is specified and it is not found or it is not a directory.
     */
    static File loadChiliLogConfigDirectory() throws FileNotFoundException
    {
        String s = System.getProperty(CHILILOG_CONFIG_DIRECTORY);
        if (StringUtils.isBlank(s))
        {
            return null;
        }

        s = s.trim();
        File f = new File(s);
        if (!f.exists())
        {
            throw new FileNotFoundException(s);
        }

        if (!f.isDirectory())
        {
            throw new FileNotFoundException(s + " is not a directory");
        }

        return f;
    }

    /**
     * String representation of the values are have parsed
     */
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        Class<SystemProperties> cls = SystemProperties.class;
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
            baseName = baseName.replace("Chililog", "ChiliLog").replace("_", "");
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
