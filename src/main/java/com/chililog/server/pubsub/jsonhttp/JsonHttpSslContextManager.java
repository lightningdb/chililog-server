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

package com.chililog.server.pubsub.jsonhttp;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.Security;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

import com.chililog.server.common.AppProperties;
import com.chililog.server.common.Log4JLogger;

/**
 * <p>
 * SSL Context manager for handling SSL traffic.
 * </p>
 */
public class JsonHttpSslContextManager
{
    private static Log4JLogger _logger = Log4JLogger.getLogger(JsonHttpSslContextManager.class);
    private static final String PROTOCOL = "TLS";
    private SSLContext _serverContext;
    private SSLContext _clientContext;

    /**
     * Returns the singleton instance for this class
     */
    public static JsonHttpSslContextManager getInstance()
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
        public static final JsonHttpSslContextManager INSTANCE = new JsonHttpSslContextManager();
    }

    /**
     * Constructor for singleton
     */
    private JsonHttpSslContextManager()
    {
        try
        {
            // Key store (Server side certificate)
            String algorithm = Security.getProperty("ssl.KeyManagerFactory.algorithm");
            if (algorithm == null)
            {
                algorithm = "SunX509";
            }

            SSLContext serverContext = null;
            try
            {
                KeyStore ks = KeyStore.getInstance("JKS");
                FileInputStream fin = new FileInputStream(AppProperties.getInstance()
                        .getPubSubJsonHttpProtocolKeyStorePath());
                ks.load(fin, AppProperties.getInstance().getPubSubJsonHttpProtocolKeyStorePassword().toCharArray());

                // Set up key manager factory to use our key store
                KeyManagerFactory kmf = KeyManagerFactory.getInstance(algorithm);
                kmf.init(ks, AppProperties.getInstance().getPubSubJsonHttpProtocolKeyStoreKeyPassword().toCharArray());

                // Initialise the SSLContext to work with our key managers.
                serverContext = SSLContext.getInstance(PROTOCOL);
                serverContext.init(kmf.getKeyManagers(), null, null);
            }
            catch (Exception e)
            {
                throw new Error("Failed to initialize the server-side SSLContext", e);
            }
            _serverContext = serverContext;

            // Trust store (client side certificate)
            SSLContext clientContext = null;
            try
            {
                clientContext = SSLContext.getInstance(PROTOCOL);
                clientContext.init(null, JsonHttpSSLTrustManager.getInstance().getTrustManagers(), null);
            }
            catch (Exception e)
            {
                throw new Error("Failed to initialize the client-side SSLContext", e);
            }
            _clientContext = clientContext;
            return;
        }
        catch (Exception ex)
        {
            _logger.error("Error initializing SslContextManager. " + ex.getMessage(), ex);
            System.exit(1);

        }
    }

    /**
     * Returns the server context with server side key store
     */
    public SSLContext getServerContext()
    {
        return _serverContext;
    }

    /**
     * Returns the client context with the client side trust store
     */
    public SSLContext getClientContext()
    {
        return _clientContext;
    }
}
