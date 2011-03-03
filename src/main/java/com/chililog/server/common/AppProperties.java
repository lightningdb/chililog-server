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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Hashtable;
import java.util.Properties;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.log4j.Logger;

/**
 * <p>
 * AppProperties provides strongly typed access configuration information in the <code>app.properties</code> file.
 * </p>
 * 
 * <p>
 * The <code>app.properties</code> file in the root classpath contains the default configuration.
 * </p>
 * 
 * <p>
 * To override the default configuration, create your own <code>app.properties</code> file and set your JVM system
 * property option "chililog.config.dir" to the directory where your file is located.
 * </p>
 * 
 * <p>
 * For example, if you created the file <code>/usr/local/chililog/config/app.properties</code>, then set the following
 * JVM option. <code>-Dchililog.config.dir=/usr/local/chililog/config</code>.
 * </p>
 * 
 * <h3>Example</h3>
 * 
 * <pre>
 * AppProperties.getInstance().getBuildTimestamp();
 * </pre>
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
 * 
 * @author vibul
 * @since 1.0
 */
public class AppProperties
{
    private static Logger _logger = Logger.getLogger(AppProperties.class);
    private static final String APP_PROPERTY_FILE_NAME = "app.properties";

    /**
     * Returns the singleton instance for this class
     */
    public static AppProperties getInstance()
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
        public static final AppProperties INSTANCE = new AppProperties();
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
    private AppProperties()
    {
        try
        {
            loadProperties();
        }
        catch (Exception e)
        {
            _logger.error("Error loading application properties: " + e.getMessage(), e);
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

            // Load default from class path
            InputStream is = AppProperties.class.getClassLoader().getResourceAsStream(APP_PROPERTY_FILE_NAME);
            if (is == null)
            {
                throw new FileNotFoundException("Default app.properties file inside JAR not found");
            }
            properties.load(is);
            is.close();

            // Load overrides
            File configDirectory = SystemProperties.getInstance().getChiliLogConfigDirectory();
            if (configDirectory != null)
            {
                File configFile = new File(configDirectory, APP_PROPERTY_FILE_NAME);
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
        Class<AppProperties> cls = AppProperties.class;
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

    /**
     * If true, JSON serialization is to be human readable. If false, white spaces will be eliminated.
     */
    public boolean getJsonPetty()
    {
        return _jsonPretty;
    }

    static final String JSON_PRETTY = "json.pretty";

    private boolean _jsonPretty = false;

    static boolean loadJsonPetty(Properties properties)
    {
        return loadBoolean(properties, JSON_PRETTY, false);
    }

    /**
     * Returns the IP address of the mongoDB Database Server
     */
    public String getDbIpAddress()
    {
        return _dbIpAddress;
    }

    static final String DB_IP_ADDRESS = "db.ip_address";

    private String _dbIpAddress = null;

    static String loadDbIpAddress(Properties properties)
    {
        return loadString(properties, DB_IP_ADDRESS);
    }

    /**
     * Returns the IP port that the mongoDB Database Server is listening on. Defaults to 27017 if not set.
     */
    public int getDbIpPort()
    {
        return _dbIpPort;
    }

    static final String DB_IP_PORT = "db.ip_port";

    private int _dbIpPort = 0;

    static int loadDbIpPort(Properties properties)
    {
        return loadInt(properties, DB_IP_PORT, 27017);
    }

    /**
     * Returns the name of the database within the mongoDB server to use
     */
    public String getDbName()
    {
        return _dbName;
    }

    static final String DB_NAME = "db.name";

    private String _dbName = null;

    static String loadDbName(Properties properties)
    {
        return loadString(properties, DB_NAME);
    }

    /**
     * Returns the usename to use for authenticating of the mongoDB database
     */
    public String getDbUserName()
    {
        return _dbUserName;
    }

    static final String DB_USER_NAME = "db.username";

    private String _dbUserName = null;

    static String loadDbUserName(Properties properties)
    {
        return loadString(properties, DB_USER_NAME);
    }

    /**
     * Returns the password to use for authenticating of the mongoDB database
     */
    public String getDbPassword()
    {
        return _dbPassword;
    }

    static final String DB_PASSWORD = "db.password";

    private String _dbPassword = null;

    static String loadDbPassword(Properties properties)
    {
        return loadString(properties, DB_PASSWORD);
    }

    /**
     * Returns The full class name to use as the JAAS login module
     */
    public String getJaasLoginModuleClassName()
    {
        return _jaasLoginModuleClassName;
    }

    static final String JAAS_LOGIN_MODULE_CLASS_NAME = "jaas.login_module_class_name";

    private String _jaasLoginModuleClassName = null;

    static String loadJaasLoginModuleClassName(Properties properties)
    {
        return loadString(properties, JAAS_LOGIN_MODULE_CLASS_NAME);
    }

    /**
     * Returns The full class name to use as the JAAS configuration
     */
    public String getJaasConfigurationClassName()
    {
        return _jaasConfigurationClassName;
    }

    static final String JAAS_CONFIGURATION_CLASS_NAME = "jaas.configuration_class_name";

    private String _jaasConfigurationClassName = null;

    static String loadJaasConfigurationClassName(Properties properties)
    {
        return loadString(properties, JAAS_CONFIGURATION_CLASS_NAME);
    }

    /**
     * Returns The name of the configuration stored within the JAAS configuration class to use for login
     */
    public String getJaasConfigurationName()
    {
        return _jaasConfigurationName;
    }

    static final String JAAS_CONFIGURATION_NAME = "jaas.configuration_name";

    private String _jaasConfigurationName = null;

    static String loadJaasConfigurationName(Properties properties)
    {
        return loadString(properties, JAAS_CONFIGURATION_NAME);
    }

    /**
     * Returns The full class name to use as the JAAS callback handler
     */
    public String getJaasCallbackHandlerClassName()
    {
        return _jaasCallbackHandlerClassName;
    }

    static final String JAAS_CALLBACK_HANDLER_CLASS_NAME = "jaas.callback_handler_class_name";

    private String _jaasCallbackHandlerClassName = null;

    static String loadJaasCallbackHandlerClassName(Properties properties)
    {
        return loadString(properties, JAAS_CALLBACK_HANDLER_CLASS_NAME);
    }

    /**
     * Returns The name of the ChiliLog system user. This auto-create user will have permission to manage all aspects of
     * ChiliLog. If it is not set, then we generate a random one. It should be set for load-balanced installations.
     */
    public String getJaasSystemUsername()
    {
        return _jaasSystemUsername;
    }

    static final String JAAS_SYSTEM_USERNAME = "jaas.system_username";

    private String _jaasSystemUsername = null;

    static String loadJaasSystemUsername(Properties properties)
    {
        String s = loadString(properties, JAAS_SYSTEM_USERNAME, StringUtils.EMPTY);
        if (StringUtils.isBlank(s))
        {
            s = "systemuser_" + UUID.randomUUID().toString();
        }
        return s;
    }

    /**
     * Returns The password of the ChiliLog system user. This auto-create user will have permission to manage all
     * aspects of ChiliLog. If it is not set, then we generate a random one.
     */
    public String getJaasSystemPassword()
    {
        return _jaasSystemPassword;
    }

    static final String JAAS_SYSTEM_PASSWORD = "jaas.system_password";

    private String _jaasSystemPassword = null;

    static String loadJaasSystemPassword(Properties properties)
    {
        String s = loadString(properties, JAAS_SYSTEM_PASSWORD, StringUtils.EMPTY);
        if (StringUtils.isBlank(s))
        {
            s = UUID.randomUUID().toString();
        }
        return s;
    }

    /**
     * Returns The role of the ChiliLog system user.
     */
    public String getJaasSystemRole()
    {
        return _jaasSystemRole;
    }

    static final String JAAS_SYSTEM_ROLE = "jaas.system_role";

    private String _jaasSystemRole = null;

    static String loadJaasSystemRole(Properties properties)
    {
        return loadString(properties, JAAS_SYSTEM_ROLE);
    }

    /**
     * Returns Flag to indicate if persistent message queues are to be used. Default is false.
     */
    public boolean getMqPersistenceEnabled()
    {
        return _mqPersistenceEnabled;
    }

    static final String MQ_PERSISTENCE_ENABLED = "mq.persistence_enabled";

    private boolean _mqPersistenceEnabled = false;

    static boolean loadMqPersistenceEnabled(Properties properties)
    {
        return loadBoolean(properties, MQ_PERSISTENCE_ENABLED, false);
    }

    /**
     * Returns Flag to indicate if message queue clustering is to be used. Default is false.
     */
    public boolean getMqClusteredEnabled()
    {
        return _mqClusteredEnabled;
    }

    static final String MQ_CLUSTERED_ENABLED = "mq.clustered_enabled";

    private boolean _mqClusteredEnabled = false;

    static boolean loadMqClusteredEnabled(Properties properties)
    {
        return loadBoolean(properties, MQ_CLUSTERED_ENABLED, false);
    }

    /**
     * Returns Flag to indicate if the message queue HornetQ and JMS protocols are to be enabled.
     */
    public boolean getMqCoreProtocolEnabled()
    {
        return _mqCoreProtocolEnabled;
    }

    static final String MQ_CORE_PROTOCOL_ENABLED = "mq.protocol.core.enabled";

    private boolean _mqCoreProtocolEnabled = false;

    static boolean loadMqCoreProtocolEnabled(Properties properties)
    {
        return loadBoolean(properties, MQ_CORE_PROTOCOL_ENABLED, false);
    }

    /**
     * Returns configuration settings for the message queue HornetQ and JMS protocols
     */
    public Hashtable<String, Object> getMqCoreProtocolConfig()
    {
        return _mqCoreProtocolConfig;
    }

    static final String MQ_CORE_PROTOCOL_CONFIG = "mq.protocol.core.";

    private Hashtable<String, Object> _mqCoreProtocolConfig = null;

    static Hashtable<String, Object> loadMqCoreProtocolConfig(Properties properties)
    {
        Hashtable<String, Object> m = new Hashtable<String, Object>();
        for (Object key : properties.keySet())
        {
            String keyAsString = (String) key;
            if (keyAsString.startsWith(MQ_CORE_PROTOCOL_CONFIG)
                    && !keyAsString.equalsIgnoreCase("mq.protocol.core.enabled"))
            {
                String value = properties.getProperty(keyAsString);
                if (!StringUtils.isBlank(value))
                {
                    m.put(keyAsString.substring(MQ_CORE_PROTOCOL_CONFIG.length()), value);
                }
            }
        }
        return m;
    }

    /**
     * Returns Flag to indicate if the STOMP protocol is to be enabled for the message queue
     */
    public boolean getMqStompProtocolEnabled()
    {
        return _mqStompProtocolEnabled;
    }

    static final String MQ_STOMP_PROTOCOL_ENABLED = "mq.protocol.stomp.enabled";

    private boolean _mqStompProtocolEnabled = false;

    static boolean loadMqStompProtocolEnabled(Properties properties)
    {
        return loadBoolean(properties, MQ_STOMP_PROTOCOL_ENABLED, false);
    }

    /**
     * Returns configuration settings for the message queue STOMP protocol
     */
    public Hashtable<String, Object> getMqStompProtocolConfig()
    {
        return _mqStompProtocolConfig;
    }

    static final String MQ_STOMP_PROTOCOL_CONFIG = "mq.protocol.stomp.";

    private Hashtable<String, Object> _mqStompProtocolConfig = null;

    static Hashtable<String, Object> loadMqStompProtocolConfig(Properties properties)
    {
        Hashtable<String, Object> m = new Hashtable<String, Object>();
        for (Object key : properties.keySet())
        {
            String keyAsString = (String) key;
            if (keyAsString.startsWith(MQ_STOMP_PROTOCOL_CONFIG)
                    && !keyAsString.equalsIgnoreCase("mq.protocol.stomp.enabled"))
            {
                String value = properties.getProperty(keyAsString);
                if (!StringUtils.isBlank(value))
                {
                    m.put(keyAsString.substring(MQ_STOMP_PROTOCOL_CONFIG.length()), value);
                }
            }
        }
        m.put("protocol", "stomp");
        return m;
    }

    /**
     * Returns Flag to indicate if the STOMP WEB SOCKET protocol is to be enabled for the message queue
     */
    public boolean getMqStompWebSocketProtocolEnabled()
    {
        return _mqStompWebSocketProtocolEnabled;
    }

    static final String MQ_STOMP_WEB_SOCKET_PROTOCOL_ENABLED = "mq.protocol.stomp-ws.enabled";

    private boolean _mqStompWebSocketProtocolEnabled = false;

    static boolean loadMqStompWebSocketProtocolEnabled(Properties properties)
    {
        return loadBoolean(properties, MQ_STOMP_WEB_SOCKET_PROTOCOL_ENABLED, false);
    }

    /**
     * Returns configuration settings for the message queue STOMP WEB SOCKET protocol
     */
    public Hashtable<String, Object> getMqStompWebSocketProtocolConfig()
    {
        return _mqStompWebSocketProtocolConfig;
    }

    static final String MQ_STOMP_WEB_SOCKET_PROTOCOL_CONFIG = "mq.protocol.stomp-ws.";

    private Hashtable<String, Object> _mqStompWebSocketProtocolConfig = null;

    static Hashtable<String, Object> loadMqStompWebSocketProtocolConfig(Properties properties)
    {
        Hashtable<String, Object> m = new Hashtable<String, Object>();
        for (Object key : properties.keySet())
        {
            String keyAsString = (String) key;
            if (keyAsString.startsWith(MQ_STOMP_WEB_SOCKET_PROTOCOL_CONFIG)
                    && !keyAsString.equalsIgnoreCase("mq.protocol.stomp-ws.enabled"))
            {
                String value = properties.getProperty(keyAsString);
                if (!StringUtils.isBlank(value))
                {
                    m.put(keyAsString.substring(MQ_STOMP_WEB_SOCKET_PROTOCOL_CONFIG.length()), value);
                }
            }
        }
        m.put("protocol", "stomp_ws");
        return m;
    }

    /**
     * Returns the maximum number of delivery attempts that will be made before a message is deleted or placed on the
     * dead letter queue. A message is catergorised as failed if it has been acknowledge AND its transactional session
     * is rolled back.
     */
    public int getMqRedeliveryMaxAttempts()
    {
        return _mqRedeliveryMaxAttempts;
    }

    static final String MQ_REDELIVERY_MAX_ATTEMPTS = "mq.redelivery.max_attempts";

    private int _mqRedeliveryMaxAttempts = -1;

    static int loadMqRedeliveryMaxAttempts(Properties properties)
    {
        return loadInt(properties, MQ_REDELIVERY_MAX_ATTEMPTS);
    }

    /**
     * Returns the number of milliseconds before a re-delivery of a failed message is made.
     */
    public int getMqRedeliveryDelayMilliseconds()
    {
        return _mqRedeliveryDelayMilliseconds;
    }

    static final String MQ_REDELIVERY_DELAY_MILLISECONDS = "mq.redelivery.delay_milliseconds";

    private int _mqRedeliveryDelayMilliseconds = -1;

    static int loadMqRedeliveryDelayMilliseconds(Properties properties)
    {
        return loadInt(properties, MQ_REDELIVERY_DELAY_MILLISECONDS);
    }

    /**
     * Returns the IP address to use for binding our web server
     */
    public String getWebIpAddress()
    {
        return _webIpAddress;
    }

    static final String WEB_IP_ADDRESS = "web.ip_address";

    private String _webIpAddress = null;

    static String loadWebIpAddress(Properties properties)
    {
        return loadString(properties, WEB_IP_ADDRESS);
    }

    /**
     * Returns the IP port to use for binding our web server
     */
    public int getWebIpPort()
    {
        return _webIpPort;
    }

    static final String WEB_IP_PORT = "web.ip_port";

    private int _webIpPort = 0;

    static int loadWebIpPort(Properties properties)
    {
        return loadInt(properties, WEB_IP_PORT, 9898);
    }

    /**
     * Returns Flag to indicate if the SSL is to be supported
     */
    public boolean getWebSslEnabled()
    {
        return _webSslEnabled;
    }

    static final String WEB_SSL_ENABLED = "web.ssl_enabled";

    private boolean _webSslEnabled = false;

    static boolean loadWebSslEnabled(Properties properties)
    {
        return loadBoolean(properties, WEB_SSL_ENABLED, false);
    }

    /**
     * Returns the path to the key store to use for SSL
     */
    public String getWebKeyStorePath()
    {
        return _webKeyStorePath;
    }

    static final String WEB_KEY_STORE_PATH = "web.key_store_path";

    private String _webKeyStorePath = null;

    static String loadWebKeyStorePath(Properties properties)
    {
        return loadString(properties, WEB_KEY_STORE_PATH, null);
    }

    /**
     * Returns the password to the key store to use for SSL
     */
    public String getWebKeyStorePassword()
    {
        return _webKeyStorePassword;
    }

    static final String WEB_KEY_STORE_PASSWORD = "web.key_store_password";

    private String _webKeyStorePassword = null;

    static String loadWebKeyStorePassword(Properties properties)
    {
        return loadString(properties, WEB_KEY_STORE_PASSWORD, null);
    }

    /**
     * Returns the password to the key inside to the key store to use for SSL
     */
    public String getWebKeyStoreKeyPassword()
    {
        return _webKeyStoreKeyPassword;
    }

    static final String WEB_KEY_STORE_KEY_PASSWORD = "web.key_store_key_password";

    private String _webKeyStoreKeyPassword = null;

    static String loadWebKeyStoreKeyPassword(Properties properties)
    {
        return loadString(properties, WEB_KEY_STORE_KEY_PASSWORD, null);
    }

    /**
     * Returns the path to the trust store to use for SSL
     */
    public String getWebTrustStorePath()
    {
        return _webTrustStorePath;
    }

    static final String WEB_TRUST_STORE_PATH = "web.trust_store_path";

    private String _webTrustStorePath = null;

    static String loadWebTrustStorePath(Properties properties)
    {
        return loadString(properties, WEB_TRUST_STORE_PATH, null);
    }

    /**
     * Returns the password to the trust store to use for SSL
     */
    public String getWebTrustStorePassword()
    {
        return _webTrustStorePassword;
    }

    static final String WEB_TRUST_STORE_PASSWORD = "web.trust_store_password";

    private String _webTrustStorePassword = null;

    static String loadWebTrustStorePassword(Properties properties)
    {
        return loadString(properties, WEB_TRUST_STORE_PASSWORD, null);
    }

    /**
     * Returns the password to the trust store to use for SSL
     */
    public String getWebStaticFilesDirectory()
    {
        return _webStaticFilesDirectory;
    }

    static final String WEB_STATIC_FILES_DIRECTORY = "web.static_files.directory";

    private String _webStaticFilesDirectory = null;

    static String loadWebStaticFilesDirectory(Properties properties)
    {
        return loadString(properties, WEB_STATIC_FILES_DIRECTORY, ".");
    }

    /**
     * Returns the IP port to use for binding our web server
     */
    public int getWebStaticFilesCacheSeconds()
    {
        return _webStaticFilesCacheSeconds;
    }

    static final String WEB_STATIC_FILES_CACHE_SECONDS = "web.static_files.cache_seconds";

    private int _webStaticFilesCacheSeconds = 0;

    static int loadWebStaticFilesCacheSeconds(Properties properties)
    {
        return loadInt(properties, WEB_STATIC_FILES_CACHE_SECONDS, 0);
    }

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
                    APP_PROPERTY_FILE_NAME));
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
     * Loads an int value. If not set, an exception is thrown
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
    private static int loadInt(Properties properties, String name)
    {
        String s = loadString(properties, name);
        return Integer.parseInt(s);
    }

    /**
     * Loads an int value. If it is blank (whitespace, empty or null), then return the <code>defaultValue</code>
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
    private static int loadInt(Properties properties, String name, int defaultValue)
    {
        String s = loadString(properties, name, null);
        if (s == null)
        {
            return defaultValue;
        }
        return Integer.parseInt(s);
    }

    /**
     * Loads an boolean value. If not set, an exception is thrown
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
    @SuppressWarnings("unused")
    private static boolean loadBoolean(Properties properties, String name)
    {
        String s = loadString(properties, name);
        return Boolean.parseBoolean(s);
    }

    /**
     * Loads a boolean value. If it is blank (whitespace, empty or null), then return the <code>defaultValue</code>
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
    private static boolean loadBoolean(Properties properties, String name, boolean defaultValue)
    {
        String s = loadString(properties, name, null);
        if (s == null)
        {
            return defaultValue;
        }
        return Boolean.parseBoolean(s);
    }

    /**
     * Returns a string representation of the parsed properties
     */
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        Class<AppProperties> cls = AppProperties.class;
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
