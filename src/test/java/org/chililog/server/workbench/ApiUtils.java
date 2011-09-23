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

package org.chililog.server.workbench;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.chililog.server.common.JsonTranslator;
import org.chililog.server.workbench.workers.AuthenticationAO;
import org.chililog.server.workbench.workers.Worker;
import org.chililog.server.workbench.workers.AuthenticationAO.ExpiryType;
import org.jboss.netty.handler.codec.http.HttpMethod;

public class ApiUtils {

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
    public static HttpURLConnection getHttpURLConnection(String urlString, HttpMethod method, String authtoken)
            throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        if (method == HttpMethod.DELETE) {
            conn.setRequestMethod(method.getName());
        }
        if (method == HttpMethod.POST || method == HttpMethod.PUT) {
            conn.setDoOutput(true);
            conn.setRequestMethod(method.getName());
            conn.setRequestProperty("Content-Type", Worker.JSON_CONTENT_TYPE);
        }
        if (!StringUtils.isEmpty(authtoken)) {
            conn.setRequestProperty(Worker.AUTHENTICATION_TOKEN_HEADER, authtoken);
        }
        return conn;
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
                                   HashMap<String, String> headers) throws IOException {
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
    public static String getResponseContent(HttpURLConnection httpConn) throws IOException {
        try {
            if (httpConn.getInputStream() == null) {
                return null;
            } else {
                StringBuilder sb = new StringBuilder();
                BufferedReader in = new BufferedReader(new InputStreamReader(httpConn.getInputStream()));
                String str;
                while ((str = in.readLine()) != null) {
                    sb.append(str + "\n");
                }
                in.close();

                return sb.toString();
            }
        } catch (Exception ex) {
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
    public static String getResponseErrorContent(HttpURLConnection httpConn) throws IOException {
        if (httpConn.getErrorStream() == null) {
            return null;
        } else {
            StringBuilder sb = new StringBuilder();
            BufferedReader in = new BufferedReader(new InputStreamReader(httpConn.getErrorStream()));
            String str;
            while ((str = in.readLine()) != null) {
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
    public static String getResponseHeaders(URLConnection conn, HashMap<String, String> headers) {
        headers.clear();
        String responseCode = "";
        for (int i = 0;; i++) {
            String name = conn.getHeaderFieldKey(i);
            String value = conn.getHeaderField(i);
            if (name == null && value == null) {
                break;
            }
            if (name == null) {
                responseCode = value;
            } else {
                headers.put(name, value);
            }
        }
        return responseCode;
    }

    /**
     * Format response for logging
     * 
     * @param responseCode
     * @param headers
     * @param content
     * @return
     */
    public static String formatResponseForLogging(String responseCode, HashMap<String, String> headers, String content) {
        StringBuilder sb = new StringBuilder();
        sb.append("HTTP Response: ");
        sb.append(responseCode);
        sb.append("\n");
        for (Entry<String, String> e : headers.entrySet()) {
            sb.append(String.format("Header %s = %s", e.getKey(), e.getValue()));
        }
        sb.append("\nCONTENT: ");
        sb.append(content == null ? "{No Content}" : content);
        sb.append("\n");
        return sb.toString();
    }

    /**
     * Perform a successful login and return the authentication token for sliding 1 hour expiry
     * 
     * @param username
     *            username of the user to login
     * @param password
     *            password of the user to login
     * @return Authentication Token
     * @throws IOException
     */
    public static String login(String username, String password) throws IOException {
        return login(username, password, ExpiryType.Sliding, 3600);
    }

    /**
     * Perform a successful login and return the authentication token
     * 
     * @param username
     *            username of the user to login
     * @param password
     *            password of the user to login
     * @param expiryType
     *            type of expiry
     * @param expirySeconds
     *            seconds until expiry
     * @return Authentication Token
     * @throws IOException
     */
    public static String login(String username, String password, ExpiryType expiryType, int expirySeconds)
            throws IOException {
        URL url = new URL("http://localhost:8989/api/Authentication");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", Worker.JSON_CONTENT_TYPE);

        AuthenticationAO requestContent = new AuthenticationAO();
        requestContent.setUsername(username);
        requestContent.setPassword(password);
        requestContent.setExpiryType(expiryType);
        requestContent.setExpirySeconds(expirySeconds);

        OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
        JsonTranslator.getInstance().toJson(requestContent, out);
        out.close();

        HashMap<String, String> headers = new HashMap<String, String>();
        String responseCode = ApiUtils.getResponseHeaders(conn, headers);
        String authenticationCode = headers.get(Worker.AUTHENTICATION_TOKEN_HEADER);

        assertEquals("HTTP/1.1 200 OK", responseCode);
        assertNotNull(authenticationCode);
        assertNotNull(headers.get("Date"));

        return authenticationCode;
    }

    /**
     * Check for a 200 OK response
     * 
     * @param responseCode
     * @param headers
     */
    public static void check200OKResponse(String responseCode, HashMap<String, String> headers) {
        assertEquals("HTTP/1.1 200 OK", responseCode);
        assertNotNull(headers.get("Date"));
        assertEquals(Worker.JSON_CONTENT_TYPE, headers.get("Content-Type"));
    }

    /**
     * Check for a 204 No Content response
     * 
     * @param responseCode
     * @param headers
     */
    public static void check204NoContentResponse(String responseCode, HashMap<String, String> headers) {
        assertEquals("HTTP/1.1 204 No Content", responseCode);
        assertNotNull(headers.get("Date"));
        assertNull(headers.get("Content-Type"));
    }

    /**
     * Check for a 400 Bad Request response
     * 
     * @param responseCode
     * @param headers
     */
    public static void check400BadRequestResponse(String responseCode, HashMap<String, String> headers) {
        assertEquals("HTTP/1.1 400 Bad Request", responseCode);
        assertNotNull(headers.get("Date"));

        // Should have ErrorAO to describe error
        assertEquals(Worker.JSON_CONTENT_TYPE, headers.get("Content-Type"));
    }

    /**
     * Check for a 401 Unauthorized response
     * 
     * @param responseCode
     * @param headers
     */
    public static void check401UnauthorizedResponse(String responseCode, HashMap<String, String> headers) {
        assertEquals("HTTP/1.1 401 Unauthorized", responseCode);
        assertNotNull(headers.get("Date"));

        // Should have ErrorAO to describe error
        assertEquals(Worker.JSON_CONTENT_TYPE, headers.get("Content-Type"));
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
    public static void sendJSON(HttpURLConnection httpConn, Object objectToJsonify) throws IOException {
        OutputStreamWriter out = new OutputStreamWriter(httpConn.getOutputStream());
        JsonTranslator.getInstance().toJson(objectToJsonify, out);
        out.close();
    }
}
