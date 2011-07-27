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

package com.chililog.server.pubsub;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;

import org.jboss.netty.handler.codec.http.HttpMethod;

import com.chililog.server.common.JsonTranslator;

/**
 * Collection of utility methods
 * 
 * @author vibul
 * 
 */
public class TestUtils
{
    /**
     * Builds a HTTP connection ready for sending to server
     * 
     * @param urlString
     *            URL to send to
     * @param method
     *            HTTP method
     * @param authtoken
     *            Authentication token
     * @return HttpURLConnection
     * @throws Exception
     */
    public static HttpURLConnection getHttpURLConnection(String urlString, HttpMethod method) throws Exception
    {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        if (method == HttpMethod.DELETE)
        {
            conn.setRequestMethod(method.getName());
        }
        if (method == HttpMethod.POST || method == HttpMethod.PUT)
        {
            conn.setDoOutput(true);
            conn.setRequestMethod(method.getName());
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        }

        return conn;
    }

    /**
     * Sends the request data as JSON
     * 
     * @param httpConn
     *            HTTP connection
     * @param jsonObject
     *            Object to jsonify
     * @throws IOException
     */
    public static void postJSON(HttpURLConnection httpConn, Object objectToJsonify) throws IOException
    {
        OutputStreamWriter out = new OutputStreamWriter(httpConn.getOutputStream());
        JsonTranslator.getInstance().toJson(objectToJsonify, out);
        out.close();
    }

    /**
     * Get the response content and headers as string
     * 
     * @param httpConn
     * @return
     * @throws IOException
     */
    public static void getResponse(HttpURLConnection httpConn,
                                   StringBuilder responseContent,
                                   StringBuilder responseCode,
                                   HashMap<String, String> headers) throws IOException
    {
        String s = getResponseContent(httpConn);
        responseContent.setLength(0);
        responseContent.append(s);

        s = getResponseHeaders(httpConn, headers);
        responseCode.setLength(0);
        responseCode.append(s);

        return;
    }

    /**
     * Get the response (or error response) as a string
     * 
     * @param httpConn
     * @return
     * @throws IOException
     */
    public static String getResponseContent(HttpURLConnection httpConn) throws IOException
    {
        try
        {
            if (httpConn.getInputStream() == null)
            {
                return null;
            }
            else
            {
                StringBuilder sb = new StringBuilder();
                BufferedReader in = new BufferedReader(new InputStreamReader(httpConn.getInputStream()));
                String str;
                while ((str = in.readLine()) != null)
                {
                    sb.append(str + "\n");
                }
                in.close();

                return sb.toString();
            }
        }
        catch (Exception ex)
        {
            return getResponseErrorContent(httpConn);
        }
    }

    /**
     * Gets the error response as a string
     * 
     * @param httpConn
     * @return
     * @throws IOException
     */
    public static String getResponseErrorContent(HttpURLConnection httpConn) throws IOException
    {
        if (httpConn.getErrorStream() == null)
        {
            return null;
        }
        else
        {
            StringBuilder sb = new StringBuilder();
            BufferedReader in = new BufferedReader(new InputStreamReader(httpConn.getErrorStream()));
            String str;
            while ((str = in.readLine()) != null)
            {
                sb.append(str + "\n");
            }
            in.close();

            return sb.toString();
        }
    }

    /**
     * Gets the headers
     * 
     * @param conn
     * @param headers
     * @return 1st response line
     */
    public static String getResponseHeaders(URLConnection conn, HashMap<String, String> headers)
    {
        headers.clear();
        String responseCode = "";
        for (int i = 0;; i++)
        {
            String name = conn.getHeaderFieldKey(i);
            String value = conn.getHeaderField(i);
            if (name == null && value == null)
            {
                break;
            }
            if (name == null)
            {
                responseCode = value;
            }
            else
            {
                headers.put(name, value);
            }
        }
        return responseCode;
    }

    /**
     * Check for a 200 OK response
     * 
     * @param responseCode
     * @param headers
     */
    public static void check200OKResponse(String responseCode, HashMap<String, String> headers)
    {
        assertEquals("HTTP/1.1 200 OK", responseCode);
        assertEquals("application/json; charset=UTF-8", headers.get("Content-Type"));
    }

    /**
     * Check for a 400 Bad Request response
     * 
     * @param responseCode
     * @param headers
     */
    public static void check400BadRequestResponse(String responseCode, HashMap<String, String> headers)
    {
        assertEquals("HTTP/1.1 400 Bad Request", responseCode);

        // Should have ErrorAO to describe error
        assertEquals("application/json; charset=UTF-8", headers.get("Content-Type"));
    }
}
