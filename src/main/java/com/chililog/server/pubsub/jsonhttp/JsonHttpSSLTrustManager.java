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
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import com.chililog.server.common.AppProperties;
import com.chililog.server.common.Log4JLogger;

/**
 * <p>
 * Manager to check client side certificates.
 * </p>
 * <p>
 * See http://download.oracle.com/javase/1.5.0/docs/guide/security/jsse/JSSERefGuide.html#TrustManager and
 * http://www.coderanch.com/t/207318/sockets/java/do-hold-Java-default-SSL
 * </p>
 * 
 * @author vibul
 * 
 */
public class JsonHttpSSLTrustManager
{
    private static Log4JLogger _logger = Log4JLogger.getLogger(JsonHttpSSLTrustManager.class);

    /*
     * The default X509TrustManager returned by SunX509. We'll delegate decisions to it, and fall back to the logic in
     * this class if the default X509TrustManager doesn't trust it.
     */
    private X509TrustManager _sunJSSEX509TrustManager;

    private TrustManager[] _trustManagers = null;

    /**
     * Returns the singleton instance for this class
     */
    public static JsonHttpSSLTrustManager getInstance()
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
        public static final JsonHttpSSLTrustManager INSTANCE = new JsonHttpSSLTrustManager();
    }

    /**
     * Constructor
     */
    private JsonHttpSSLTrustManager()
    {
        try
        {
            // create a "default" JSSE X509TrustManager.
            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(new FileInputStream(AppProperties.getInstance().getPubSubJsonHttpProtocolTrustStorePath()),
                    AppProperties.getInstance().getPubSubJsonHttpProtocolTrustStorePassword().toCharArray());

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509", "SunJSSE");
            trustManagerFactory.init(ks);

            _trustManagers = trustManagerFactory.getTrustManagers();

            /*
             * Iterate over the returned trust managers, look for an instance of X509TrustManager. If found, use that as
             * our "default" trust manager.
             */
            for (int i = 0; i < _trustManagers.length; i++)
            {
                if (_trustManagers[i] instanceof X509TrustManager)
                {
                    _sunJSSEX509TrustManager = (X509TrustManager) _trustManagers[i];
                    return;
                }
            }

            /*
             * Find some other way to initialize, or else we have to fail the constructor.
             */
            throw new Exception("X509TrustManager not found.");
        }
        catch (Exception ex)
        {
            _logger.error("Error initializing SSLTrustManager. " + ex.getMessage(), ex);
            System.exit(1);

        }
    }

    /*
     * Delegate to the default trust manager.
     */
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException
    {
        _sunJSSEX509TrustManager.checkClientTrusted(chain, authType);
    }

    /*
     * Delegate to the default trust manager.
     */
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException
    {
        _sunJSSEX509TrustManager.checkServerTrusted(chain, authType);
    }

    /*
     * Merely pass this through.
     */
    public X509Certificate[] getAcceptedIssuers()
    {
        return _sunJSSEX509TrustManager.getAcceptedIssuers();
    }

    public TrustManager[] getTrustManagers()
    {
        return _trustManagers;
    }
}
