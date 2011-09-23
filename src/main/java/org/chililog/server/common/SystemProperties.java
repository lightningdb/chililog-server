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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;

/**
 * <p>
 * SystemProperties provides strongly typed access to our system properties.
 * </p>
 * 
 * <h3>Example</h3>
 * 
 * <code>
 * SystemProperties.getInstance().getJavaHome();
 * </code>
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
 * @author vibul
 * @since 1.0
 */
public class SystemProperties {

    private static Log4JLogger _logger = Log4JLogger.getLogger(SystemProperties.class);

    /**
     * Returns the singleton instance for this class
     */
    public static SystemProperties getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * SingletonHolder is loaded on the first execution of Singleton.getInstance() or the first access to
     * SingletonHolder.INSTANCE, not before.
     * 
     * @see http://en.wikipedia.org/wiki/Singleton_pattern
     */
    private static class SingletonHolder {

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
    private SystemProperties() {
        try {
            loadProperties();
        } catch (Exception e) {
            _logger.error(e, "Error loading system properties: " + e.getMessage());
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
    public void loadProperties() throws Exception {
        Class<SystemProperties> cls = SystemProperties.class;
        for (Field f : cls.getDeclaredFields()) {
            // Look for field names like CHILILOG_CONFIG_DIRECTORY
            String propertyNameFieldName = f.getName();
            if (!propertyNameFieldName.matches("^[A-Z0-9_]+$")) {
                continue;
            }

            // Build cache field (_chiliLogConfigDirectory) and method (loadChiliLogConfigDirectory) names
            String baseName = WordUtils.capitalizeFully(propertyNameFieldName, new char[] { '_' });
            baseName = baseName.replace("Chililog", "ChiliLog").replace("_", "");
            String cacheMethodName = "load" + baseName;
            String cacheFieldName = "_" + StringUtils.uncapitalize(baseName);

            // If field not exist, then skip
            Field cacheField = null;
            try {
                cacheField = cls.getDeclaredField(cacheFieldName);
            } catch (NoSuchFieldException e) {
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
     * @return The Java home directory
     */
    public String getJavaHome() {
        return _javaHome;
    }

    public static final String JAVA_HOME = "java.home";

    private String _javaHome = null;

    static String loadJavaHome() {
        return System.getProperty(JAVA_HOME);
    }

    /**
     * @return The Java vendor
     */
    public String getJavaVender() {
        return _javaVender;
    }

    public static final String JAVA_VENDER = "java.vender";

    private String _javaVender = null;

    static String loadJavaVender() {
        return System.getProperty(JAVA_VENDER);
    }

    /**
     * @return The Java version
     */
    public String getJavaVersion() {
        return _javaVersion;
    }

    public static final String JAVA_VERSION = "java.version";

    private String _javaVersion = null;

    static String loadJavaVersion() {
        return System.getProperty(JAVA_VERSION);
    }

    /**
     * @return The operating system name
     */
    public String getOsName() {
        return _osName;
    }

    public static final String OS_NAME = "os.name";

    private String _osName = null;

    static String loadOsName() {
        return System.getProperty(OS_NAME);
    }

    /**
     * @return The operating system architecture
     */
    public String getOsArchitecture() {
        return _osArchitecture;
    }

    public static final String OS_ARCHITECTURE = "os.arch";

    private String _osArchitecture = null;

    static String loadOsArchitecture() {
        return System.getProperty(OS_ARCHITECTURE);
    }

    /**
     * @return The operating system version
     */
    public String getOsVersion() {
        return _osVersion;
    }

    public static final String OS_VERSION = "os.version";

    private String _osVersion = null;

    static String loadOsVersion() {
        return System.getProperty(OS_VERSION);
    }

    /**
     * String representation of the values are have parsed
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();

        // Get all system properties
        Properties props = System.getProperties();

        // Enumerate all system properties
        Enumeration<?> e = props.propertyNames();
        for (; e.hasMoreElements();) {
            String name = (String) e.nextElement();
            String value = (String) props.get(name);

            sb.append(name);
            sb.append(" = ");
            sb.append(value);
            sb.append("\n");
        }

        return sb.toString();
    }
}
