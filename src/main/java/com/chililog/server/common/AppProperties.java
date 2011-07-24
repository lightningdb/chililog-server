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
    public boolean getJsonPretty()
    {
        return _jsonPretty;
    }

    static final String JSON_PRETTY = "json.pretty";

    private boolean _jsonPretty = false;

    static boolean loadJsonPretty(Properties properties)
    {
        return loadBoolean(properties, JSON_PRETTY, false);
    }

    // *****************************************************************************************************************
    // *****************************************************************************************************************
    // Database
    // *****************************************************************************************************************
    // *****************************************************************************************************************

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
     * Returns the number of connections per host. The default is 10.
     */
    public int getDbConnectionsPerHost()
    {
        return _dbConnectionsPerHost;
    }

    static final String DB_CONNECTIONS_PER_HOST = "db.connections_per_host";

    private int _dbConnectionsPerHost = 0;

    static int loadDbConnectionsPerHost(Properties properties)
    {
        return loadInt(properties, DB_CONNECTIONS_PER_HOST, 10);
    }

    // *****************************************************************************************************************
    // *****************************************************************************************************************
    // Message Queue
    // *****************************************************************************************************************
    // *****************************************************************************************************************

    /**
     * Returns The name of the ChiliLog system user. This auto-create user will have permission to manage all aspects of
     * ChiliLog. If it is not set, then we generate a random one. It should be set for load-balanced installations.
     */
    public String getMqSystemUsername()
    {
        return _mqSystemUsername;
    }

    static final String MQ_SYSTEM_USERNAME = "mq.system_username";

    private String _mqSystemUsername = null;

    static String loadMqSystemUsername(Properties properties)
    {
        String s = loadString(properties, MQ_SYSTEM_USERNAME, StringUtils.EMPTY);
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
    public String getMqSystemPassword()
    {
        return _mqSystemPassword;
    }

    static final String MQ_SYSTEM_PASSWORD = "mq.system_password";

    private String _mqSystemPassword = null;

    static String loadMqSystemPassword(Properties properties)
    {
        String s = loadString(properties, MQ_SYSTEM_PASSWORD, StringUtils.EMPTY);
        if (StringUtils.isBlank(s))
        {
            s = UUID.randomUUID().toString();
        }
        return s;
    }

    /**
     * Returns Flag to indicate if journalling is enabled or not. If so, then messages from message queues flagged as
     * durable will be persisted (or journalled). Default is false.
     */
    public boolean getMqJournallingEnabled()
    {
        return _mqJournallingEnabled;
    }

    static final String MQ_JOURNALLING_ENABLED = "mq.journalling_enabled";

    private boolean _mqJournallingEnabled = false;

    static boolean loadMqJournallingEnabled(Properties properties)
    {
        return loadBoolean(properties, MQ_JOURNALLING_ENABLED, false);
    }

    /**
     * Returns the directory to store journal files
     */
    public String getMqJournalDirectory()
    {
        return _mqJournalDirectory;
    }

    static final String MQ_JOURNAL_DIRECTORY = "mq.journal_directory";

    private String _mqJournalDirectory = null;

    static String loadMqJournalDirectory(Properties properties)
    {
        String s = loadString(properties, MQ_JOURNAL_DIRECTORY);
        return s;
    }

    /**
     * Returns the directory to store paging files.
     */
    public String getMqPagingDirectory()
    {
        return _mqPagingDirectory;
    }

    static final String MQ_PAGING_DIRECTORY = "mq.paging_directory";

    private String _mqPagingDirectory = null;

    static String loadMqPagingDirectory(Properties properties)
    {
        String s = loadString(properties, MQ_PAGING_DIRECTORY);
        return s;
    }

    /**
     * Returns the time period in milliseconds during which an authenticated user is valid and credentials will not be
     * validated by calling JAAS. Defaults to 10000 (10 seconds).
     */
    public int getMqSecurityInvalidationInterval()
    {
        return _mqSecurityInvalidationInterval;
    }

    static final String MQ_SECURITY_INVALIDATION_INTERVAL = "mq.security_invalidation_interval";

    private int _mqSecurityInvalidationInterval = 10000;

    static int loadMqSecurityInvalidationInterval(Properties properties)
    {
        int i = loadInt(properties, MQ_SECURITY_INVALIDATION_INTERVAL, 10000);
        return i;
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
     * Returns the address to send for undelivered messages
     */
    public String getMqDeadLetterAddress()
    {
        return _mqDeadLetterAddress;
    }

    static final String MQ_DEAD_LETTER_ADDRESS = "mq.dead_letter_address";

    private String _mqDeadLetterAddress = null;

    static String loadMqDeadLetterAddress(Properties properties)
    {
        return loadString(properties, MQ_DEAD_LETTER_ADDRESS, null);
    }

    // *****************************************************************************************************************
    // *****************************************************************************************************************
    // PUB SUB
    // *****************************************************************************************************************
    // *****************************************************************************************************************
    
    /**
     * Returns Flag to indicate if the message queue HornetQ and JMS protocols are to be enabled for pubsub use
     */
    public boolean getPubSubCoreProtocolEnabled()
    {
        return _pubSubCoreProtocolEnabled;
    }

    static final String PUB_SUB_CORE_PROTOCOL_ENABLED = "pubsub.core.enabled";

    private boolean _pubSubCoreProtocolEnabled = false;

    static boolean loadPubSubCoreProtocolEnabled(Properties properties)
    {
        return loadBoolean(properties, PUB_SUB_CORE_PROTOCOL_ENABLED, false);
    }

    /**
     * Returns configuration settings for the message queue HornetQ and JMS protocols
     */
    public Hashtable<String, Object> getPubSubCoreProtocolConfig()
    {
        return _pubSubCoreProtocolConfig;
    }

    static final String PUB_SUB_CORE_PROTOCOL_CONFIG = "pubsub.core.";

    private Hashtable<String, Object> _pubSubCoreProtocolConfig = null;

    static Hashtable<String, Object> loadPubSubCoreProtocolConfig(Properties properties)
    {
        Hashtable<String, Object> m = new Hashtable<String, Object>();
        for (Object key : properties.keySet())
        {
            String keyAsString = (String) key;
            if (keyAsString.startsWith(PUB_SUB_CORE_PROTOCOL_CONFIG)
                    && !keyAsString.equalsIgnoreCase("pubsub.core.enabled"))
            {
                String value = properties.getProperty(keyAsString);
                if (!StringUtils.isBlank(value))
                {
                    m.put(keyAsString.substring(PUB_SUB_CORE_PROTOCOL_CONFIG.length()), value);
                }
            }
        }
        return m;
    }

    /**
     * Returns Flag to indicate if the STOMP protocol is to be enabled for pubsub use
     */
    public boolean getPubSubStompProtocolEnabled()
    {
        return _pubSubStompProtocolEnabled;
    }

    static final String PUB_SUB_STOMP_PROTOCOL_ENABLED = "pubsub.stomp.enabled";

    private boolean _pubSubStompProtocolEnabled = false;

    static boolean loadPubSubStompProtocolEnabled(Properties properties)
    {
        return loadBoolean(properties, PUB_SUB_STOMP_PROTOCOL_ENABLED, false);
    }

    /**
     * Returns configuration settings for the message queue STOMP protocol
     */
    public Hashtable<String, Object> getPubSubStompProtocolConfig()
    {
        return _pubSubStompProtocolConfig;
    }

    static final String PUB_SUB_STOMP_PROTOCOL_CONFIG = "pubsub.stomp.";

    private Hashtable<String, Object> _pubSubStompProtocolConfig = null;

    static Hashtable<String, Object> loadPubSubStompProtocolConfig(Properties properties)
    {
        Hashtable<String, Object> m = new Hashtable<String, Object>();
        for (Object key : properties.keySet())
        {
            String keyAsString = (String) key;
            if (keyAsString.startsWith(PUB_SUB_STOMP_PROTOCOL_CONFIG)
                    && !keyAsString.equalsIgnoreCase("pubsub.stomp.enabled"))
            {
                String value = properties.getProperty(keyAsString);
                if (!StringUtils.isBlank(value))
                {
                    m.put(keyAsString.substring(PUB_SUB_STOMP_PROTOCOL_CONFIG.length()), value);
                }
            }
        }
        m.put("protocol", "stomp");
        return m;
    }

    /**
     * Returns Flag to indicate if the STOMP WEB SOCKET protocol is to be enabled for pubsub use
     */
    public boolean getPubSubStompWebSocketProtocolEnabled()
    {
        return _pubSubStompWebSocketProtocolEnabled;
    }

    static final String PUB_SUB_STOMP_WEB_SOCKET_PROTOCOL_ENABLED = "pubsub.stomp-ws.enabled";

    private boolean _pubSubStompWebSocketProtocolEnabled = false;

    static boolean loadPubSubStompWebSocketProtocolEnabled(Properties properties)
    {
        return loadBoolean(properties, PUB_SUB_STOMP_WEB_SOCKET_PROTOCOL_ENABLED, false);
    }

    /**
     * Returns configuration settings for the message queue STOMP WEB SOCKET protocol
     */
    public Hashtable<String, Object> getPubSubStompWebSocketProtocolConfig()
    {
        return _pubSubStompWebSocketProtocolConfig;
    }

    static final String PUB_SUB_STOMP_WEB_SOCKET_PROTOCOL_CONFIG = "pubsub.stomp-ws.";

    private Hashtable<String, Object> _pubSubStompWebSocketProtocolConfig = null;

    static Hashtable<String, Object> loadPubSubStompWebSocketProtocolConfig(Properties properties)
    {
        Hashtable<String, Object> m = new Hashtable<String, Object>();
        for (Object key : properties.keySet())
        {
            String keyAsString = (String) key;
            if (keyAsString.startsWith(PUB_SUB_STOMP_WEB_SOCKET_PROTOCOL_CONFIG)
                    && !keyAsString.equalsIgnoreCase("pubsub.stomp-ws.enabled"))
            {
                String value = properties.getProperty(keyAsString);
                if (!StringUtils.isBlank(value))
                {
                    m.put(keyAsString.substring(PUB_SUB_STOMP_WEB_SOCKET_PROTOCOL_CONFIG.length()), value);
                }
            }
        }
        m.put("protocol", "stomp_ws");
        return m;
    }

    /**
     * Returns Flag to indicate if the JSON HTTP protocol is to be enabled for pubsub use
     */
    public boolean getPubSubJsonHttpProtocolEnabled()
    {
        return _pubSubJsonHttpProtocolEnabled;
    }

    static final String PUB_SUB_JSON_HTTP_PROTOCOL_ENABLED = "pubsub.json-http.enabled";

    private boolean _pubSubJsonHttpProtocolEnabled = false;

    static boolean loadPubSubJsonHttpProtocolEnabled(Properties properties)
    {
        return loadBoolean(properties, PUB_SUB_JSON_HTTP_PROTOCOL_ENABLED, false);
    }
    
    /**
     * Returns the IP address to use for binding our UI web server
     */
    public String getPubSubJsonHttpProtocolHost()
    {
        return _pubSubJsonHttpProtocolHost;
    }

    static final String PUB_SUB_JSON_HTTP_PROTOCOL_HOST = "pubsub.json-http.host";

    private String _pubSubJsonHttpProtocolHost = null;

    static String loadPubSubJsonHttpProtocolHost(Properties properties)
    {
        return loadString(properties, PUB_SUB_JSON_HTTP_PROTOCOL_HOST);
    }

    /**
     * Returns the IP port to use for binding our UI web server
     */
    public int getPubSubJsonHttpProtocolPort()
    {
        return _pubSubJsonHttpProtocolPort;
    }

    static final String PUB_SUB_JSON_HTTP_PROTOCOL_PORT = "pubsub.json-http.port";

    private int _pubSubJsonHttpProtocolPort = 0;

    static int loadPubSubJsonHttpProtocolPort(Properties properties)
    {
        return loadInt(properties, PUB_SUB_JSON_HTTP_PROTOCOL_PORT, 61615);
    }

    /**
     * Returns the maximum number of active threads to execute tasks
     */
    public int getPubSubJsonHttpProtocolTaskThreadPoolSize()
    {
        return _pubSubJsonHttpProtocolTaskThreadPoolSize;
    }

    static final String PUB_SUB_JSON_HTTP_PROTOCOL_TASK_THREAD_POOL_SIZE = "pubsub.json-http.task_thread_pool.size";

    private int _pubSubJsonHttpProtocolTaskThreadPoolSize = 0;

    static int loadPubSubJsonHttpProtocolTaskThreadPoolSize(Properties properties)
    {
        return loadInt(properties, PUB_SUB_JSON_HTTP_PROTOCOL_TASK_THREAD_POOL_SIZE, 16);
    }
    
    /**
     * Returns maximum total size of the queued events per channel (0 to disable).
     */
    public long getPubSubJsonHttpProtocolTaskThreadPoolMaxChannelMemorySize()
    {
        return _pubSubJsonHttpProtocolTaskThreadPoolMaxChannelMemorySize;
    }

    static final String PUB_SUB_JSON_HTTP_PROTOCOL_TASK_THREAD_POOL_MAX_CHANNEL_MEMORY_SIZE = "pubsub.json-http.task_thread_pool.max_channel_memory_size";

    private long _pubSubJsonHttpProtocolTaskThreadPoolMaxChannelMemorySize = 0;

    static long loadPubSubJsonHttpProtocolTaskThreadPoolMaxChannelMemorySize(Properties properties)
    {
        return loadLong(properties, PUB_SUB_JSON_HTTP_PROTOCOL_TASK_THREAD_POOL_MAX_CHANNEL_MEMORY_SIZE, 1048576);
    }
    
    /**
     * Returns maximum total size of the queued events for this pool (0 to disable).
     */
    public long getPubSubJsonHttpProtocolTaskThreadPoolMaxThreadMemorySize()
    {
        return _pubSubJsonHttpProtocolTaskThreadPoolMaxThreadMemorySize;
    }

    static final String PUB_SUB_JSON_HTTP_PROTOCOL_TASK_THREAD_POOL_MAX_THREAD_MEMORY_SIZE = "pubsub.json-http.task_thread_pool.max_thread_memory_size";

    private long _pubSubJsonHttpProtocolTaskThreadPoolMaxThreadMemorySize = 0;

    static long loadPubSubJsonHttpProtocolTaskThreadPoolMaxThreadMemorySize(Properties properties)
    {
        return loadLong(properties, PUB_SUB_JSON_HTTP_PROTOCOL_TASK_THREAD_POOL_MAX_THREAD_MEMORY_SIZE, 1048576);
    }
    
    /**
     * Returns the amount of time for an inactive thread to shut itself down
     */
    public int getPubSubJsonHttpProtocolTaskThreadPoolKeepAliveSeconds()
    {
        return _pubSubJsonHttpProtocolTaskThreadPoolKeepAliveSeconds;
    }

    static final String PUB_SUB_JSON_HTTP_PROTOCOL_TASK_THREAD_POOL_KEEP_ALIVE_SECONDS = "pubsub.json-http.task_thread_pool.keep_alive_seconds";

    private int _pubSubJsonHttpProtocolTaskThreadPoolKeepAliveSeconds = 0;

    static int loadPubSubJsonHttpProtocolTaskThreadPoolKeepAliveSeconds(Properties properties)
    {
        return loadInt(properties, PUB_SUB_JSON_HTTP_PROTOCOL_TASK_THREAD_POOL_KEEP_ALIVE_SECONDS, 30);
    }
    
    /**
     * Returns Flag to indicate if the SSL is to be supported
     */
    public boolean getPubSubJsonHttpProtocolSslEnabled()
    {
        return _pubSubJsonHttpProtocolSslEnabled;
    }

    static final String PUB_SUB_JSON_HTTP_PROTOCOL_SSL_ENABLED = "pubsub.json-http.ssl_enabled";

    private boolean _pubSubJsonHttpProtocolSslEnabled = false;

    static boolean loadPubSubJsonHttpProtocolSslEnabled(Properties properties)
    {
        return loadBoolean(properties, PUB_SUB_JSON_HTTP_PROTOCOL_SSL_ENABLED, false);
    }

    /**
     * Returns the path to the key store to use for SSL
     */
    public String getPubSubJsonHttpProtocolKeyStorePath()
    {
        return _pubSubJsonHttpProtocolKeyStorePath;
    }

    static final String PUB_SUB_JSON_HTTP_PROTOCOL_KEY_STORE_PATH = "pubsub.json-http.key_store_path";

    private String _pubSubJsonHttpProtocolKeyStorePath = null;

    static String loadPubSubJsonHttpProtocolKeyStorePath(Properties properties)
    {
        return loadString(properties, PUB_SUB_JSON_HTTP_PROTOCOL_KEY_STORE_PATH, null);
    }

    /**
     * Returns the password to the key store to use for SSL
     */
    public String getPubSubJsonHttpProtocolKeyStorePassword()
    {
        return _pubSubJsonHttpProtocolKeyStorePassword;
    }

    static final String PUB_SUB_JSON_HTTP_PROTOCOL_KEY_STORE_PASSWORD = "pubsub.json-http.key_store_password";

    private String _pubSubJsonHttpProtocolKeyStorePassword = null;

    static String loadPubSubJsonHttpProtocolKeyStorePassword(Properties properties)
    {
        return loadString(properties, PUB_SUB_JSON_HTTP_PROTOCOL_KEY_STORE_PASSWORD, null);
    }

    /**
     * Returns the password to the key inside to the key store to use for SSL
     */
    public String getPubSubJsonHttpProtocolKeyStoreKeyPassword()
    {
        return _pubSubJsonHttpProtocolKeyStoreKeyPassword;
    }

    static final String PUB_SUB_JSON_HTTP_PROTOCOL_KEY_STORE_KEY_PASSWORD = "pubsub.json-http.key_store_key_password";

    private String _pubSubJsonHttpProtocolKeyStoreKeyPassword = null;

    static String loadPubSubJsonHttpProtocolKeyStoreKeyPassword(Properties properties)
    {
        return loadString(properties, PUB_SUB_JSON_HTTP_PROTOCOL_KEY_STORE_KEY_PASSWORD, null);
    }

    /**
     * Returns the path to the trust store to use for SSL
     */
    public String getPubSubJsonHttpProtocolTrustStorePath()
    {
        return _pubSubJsonHttpProtocolTrustStorePath;
    }

    static final String PUB_SUB_JSON_HTTP_PROTOCOL_TRUST_STORE_PATH = "pubsub.json-http.trust_store_path";

    private String _pubSubJsonHttpProtocolTrustStorePath = null;

    static String loadPubSubJsonHttpProtocolTrustStorePath(Properties properties)
    {
        return loadString(properties, PUB_SUB_JSON_HTTP_PROTOCOL_TRUST_STORE_PATH, null);
    }

    /**
     * Returns the password to the trust store to use for SSL
     */
    public String getPubSubJsonHttpProtocolTrustStorePassword()
    {
        return _pubSubJsonHttpProtocolTrustStorePassword;
    }

    static final String PUB_SUB_JSON_HTTP_PROTOCOL_TRUST_STORE_PASSWORD = "pubsub.json-http.trust_store_password";

    private String _pubSubJsonHttpProtocolTrustStorePassword = null;

    static String loadPubSubJsonHttpProtocolTrustStorePassword(Properties properties)
    {
        return loadString(properties, PUB_SUB_JSON_HTTP_PROTOCOL_TRUST_STORE_PASSWORD, null);
    }

    // *****************************************************************************************************************
    // *****************************************************************************************************************
    // WORKBENCH
    // *****************************************************************************************************************
    // *****************************************************************************************************************
    /**
     * Returns the IP address to use for binding our WorkBench web server
     */
    public String getWorkbenchHost()
    {
        return _workbenchHost;
    }

    static final String WORKBENCH_HOST = "workbench.host";

    private String _workbenchHost = null;

    static String loadWorkbenchHost(Properties properties)
    {
        return loadString(properties, WORKBENCH_HOST);
    }

    /**
     * Returns the IP port to use for binding our WorkBench web server
     */
    public int getWorkbenchPort()
    {
        return _workbenchPort;
    }

    static final String WORKBENCH_PORT = "workbench.port";

    private int _workbenchPort = 0;

    static int loadWorkbenchPort(Properties properties)
    {
        return loadInt(properties, WORKBENCH_PORT, 8989);
    }

    /**
     * Returns the maximum number of active threads to execute tasks
     */
    public int getWorkbenchTaskThreadPoolSize()
    {
        return _workbenchTaskThreadPoolSize;
    }

    static final String WORKBENCH_TASK_THREAD_POOL_SIZE = "workbench.task_thread_pool.size";

    private int _workbenchTaskThreadPoolSize = 0;

    static int loadWorkbenchTaskThreadPoolSize(Properties properties)
    {
        return loadInt(properties, WORKBENCH_TASK_THREAD_POOL_SIZE, 16);
    }
    
    /**
     * Returns maximum total size of the queued events per channel (0 to disable).
     */
    public long getWorkbenchTaskThreadPoolMaxChannelMemorySize()
    {
        return _workbenchTaskThreadPoolMaxChannelMemorySize;
    }

    static final String WORKBENCH_TASK_THREAD_POOL_MAX_CHANNEL_MEMORY_SIZE = "workbench.task_thread_pool.max_channel_memory_size";

    private long _workbenchTaskThreadPoolMaxChannelMemorySize = 0;

    static long loadWorkbenchTaskThreadPoolMaxChannelMemorySize(Properties properties)
    {
        return loadLong(properties, WORKBENCH_TASK_THREAD_POOL_MAX_CHANNEL_MEMORY_SIZE, 1048576);
    }
    
    /**
     * Returns maximum total size of the queued events for this pool (0 to disable).
     */
    public long getWorkbenchTaskThreadPoolMaxThreadMemorySize()
    {
        return _workbenchTaskThreadPoolMaxThreadMemorySize;
    }

    static final String WORKBENCH_TASK_THREAD_POOL_MAX_THREAD_MEMORY_SIZE = "workbench.task_thread_pool.max_thread_memory_size";

    private long _workbenchTaskThreadPoolMaxThreadMemorySize = 0;

    static long loadWorkbenchTaskThreadPoolMaxThreadMemorySize(Properties properties)
    {
        return loadLong(properties, WORKBENCH_TASK_THREAD_POOL_MAX_THREAD_MEMORY_SIZE, 1048576);
    }
    
    /**
     * Returns the amount of time for an inactive thread to shut itself down
     */
    public int getWorkbenchTaskThreadPoolKeepAliveSeconds()
    {
        return _workbenchTaskThreadPoolKeepAliveSeconds;
    }

    static final String WORKBENCH_TASK_THREAD_POOL_KEEP_ALIVE_SECONDS = "workbench.task_thread_pool.keep_alive_seconds";

    private int _workbenchTaskThreadPoolKeepAliveSeconds = 0;

    static int loadWorkbenchTaskThreadPoolKeepAliveSeconds(Properties properties)
    {
        return loadInt(properties, WORKBENCH_TASK_THREAD_POOL_KEEP_ALIVE_SECONDS, 30);
    }
    
    /**
     * Returns Flag to indicate if the SSL is to be supported
     */
    public boolean getWorkbenchSslEnabled()
    {
        return _workbenchSslEnabled;
    }

    static final String WORKBENCH_SSL_ENABLED = "workbench.ssl_enabled";

    private boolean _workbenchSslEnabled = false;

    static boolean loadWorkbenchSslEnabled(Properties properties)
    {
        return loadBoolean(properties, WORKBENCH_SSL_ENABLED, false);
    }

    /**
     * Returns the path to the key store to use for SSL
     */
    public String getWorkbenchKeyStorePath()
    {
        return _workbenchKeyStorePath;
    }

    static final String WORKBENCH_KEY_STORE_PATH = "workbench.key_store_path";

    private String _workbenchKeyStorePath = null;

    static String loadWorkbenchKeyStorePath(Properties properties)
    {
        return loadString(properties, WORKBENCH_KEY_STORE_PATH, null);
    }

    /**
     * Returns the password to the key store to use for SSL
     */
    public String getWorkbenchKeyStorePassword()
    {
        return _workbenchKeyStorePassword;
    }

    static final String WORKBENCH_KEY_STORE_PASSWORD = "workbench.key_store_password";

    private String _workbenchKeyStorePassword = null;

    static String loadWorkbenchKeyStorePassword(Properties properties)
    {
        return loadString(properties, WORKBENCH_KEY_STORE_PASSWORD, null);
    }

    /**
     * Returns the password to the key inside to the key store to use for SSL
     */
    public String getWorkbenchKeyStoreKeyPassword()
    {
        return _workbenchKeyStoreKeyPassword;
    }

    static final String WORKBENCH_KEY_STORE_KEY_PASSWORD = "workbench.key_store_key_password";

    private String _workbenchKeyStoreKeyPassword = null;

    static String loadWorkbenchKeyStoreKeyPassword(Properties properties)
    {
        return loadString(properties, WORKBENCH_KEY_STORE_KEY_PASSWORD, null);
    }

    /**
     * Returns the path to the trust store to use for SSL
     */
    public String getWorkbenchTrustStorePath()
    {
        return _workbenchTrustStorePath;
    }

    static final String WORKBENCH_TRUST_STORE_PATH = "workbench.trust_store_path";

    private String _workbenchTrustStorePath = null;

    static String loadWorkbenchTrustStorePath(Properties properties)
    {
        return loadString(properties, WORKBENCH_TRUST_STORE_PATH, null);
    }

    /**
     * Returns the password to the trust store to use for SSL
     */
    public String getWorkbenchTrustStorePassword()
    {
        return _workbenchTrustStorePassword;
    }

    static final String WORKBENCH_TRUST_STORE_PASSWORD = "workbench.trust_store_password";

    private String _workbenchTrustStorePassword = null;

    static String loadWorkbenchTrustStorePassword(Properties properties)
    {
        return loadString(properties, WORKBENCH_TRUST_STORE_PASSWORD, null);
    }

    /**
     * Returns the password to the trust store to use for SSL
     */
    public String getWorkbenchStaticFilesDirectory()
    {
        return _workbenchStaticFilesDirectory;
    }

    static final String WORKBENCH_STATIC_FILES_DIRECTORY = "workbench.static_files.directory";

    private String _workbenchStaticFilesDirectory = null;

    static String loadWorkbenchStaticFilesDirectory(Properties properties)
    {
        return loadString(properties, WORKBENCH_STATIC_FILES_DIRECTORY, ".");
    }

    /**
     * Returns the number of seconds static files are cached in the browser
     */
    public int getWorkbenchStaticFilesCacheSeconds()
    {
        return _workbenchStaticFilesCacheSeconds;
    }

    static final String WORKBENCH_STATIC_FILES_CACHE_SECONDS = "workbench.static_files.cache_seconds";

    private int _workbenchStaticFilesCacheSeconds = 0;

    static int loadWorkbenchStaticFilesCacheSeconds(Properties properties)
    {
        return loadInt(properties, WORKBENCH_STATIC_FILES_CACHE_SECONDS, 0);
    }

    /**
     * Returns the salt to use for hashing of the authentication token
     */
    public byte[] getWorkbenchApiAuthenticationHashSalt()
    {
        return _workbenchApiAuthenticationHashSalt;
    }

    static final String WORKBENCH_API_AUTHENTICATION_HASH_SALT = "workbench.api.authentication.hash_salt";

    private byte[] _workbenchApiAuthenticationHashSalt = null;

    static byte[] loadWorkbenchApiAuthenticationHashSalt(Properties properties)
    {
        try
        {
            return loadString(properties, WORKBENCH_API_AUTHENTICATION_HASH_SALT).getBytes("UTF-8");
        }
        catch (Exception ex)
        {
            return loadString(properties, WORKBENCH_API_AUTHENTICATION_HASH_SALT).getBytes();
        }
    }

    /**
     * Returns the password to use for authentication token encryption
     */
    public byte[] getWorkbenchApiAuthenticationEncryptionPassword()
    {
        return _workbenchApiAuthenticationEncryptionPassword;
    }

    static final String WORKBENCH_API_AUTHENTICATION_ENCRYPTION_PASSWORD = "workbench.api.authentication.encyrption_password";

    private byte[] _workbenchApiAuthenticationEncryptionPassword = null;

    static byte[] loadWorkbenchApiAuthenticationEncryptionPassword(Properties properties)
    {
        try
        {
            return loadString(properties, WORKBENCH_API_AUTHENTICATION_ENCRYPTION_PASSWORD).getBytes("UTF-8");
        }
        catch (Exception ex)
        {
            return loadString(properties, WORKBENCH_API_AUTHENTICATION_ENCRYPTION_PASSWORD).getBytes();
        }
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
     * Loads a Long value. If it is blank (whitespace, empty or null), then return the <code>defaultValue</code>
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
    private static long loadLong(Properties properties, String name, int defaultValue)
    {
        String s = loadString(properties, name, null);
        if (s == null)
        {
            return defaultValue;
        }
        return Long.parseLong(s);
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
