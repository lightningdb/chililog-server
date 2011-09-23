//
// Copyright 2011 Cinch Logic Pty Ltd.
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

package org.chililog.server.pubsub.websocket;

import java.net.URI;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import org.chililog.server.common.Log4JLogger;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.HttpHeaders.Names;
import org.jboss.netty.handler.codec.http.HttpHeaders.Values;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.util.CharsetUtil;

/**
 * Performs client side opening and closing handshakes
 * 
 * @author vibul
 */
public class WebSocketClientHandshaker {

    private static Log4JLogger _logger = Log4JLogger.getLogger(WebSocketClientHandshaker.class);

    public static final String SEC_WEBSOCKET_VERSION = "Sec-WebSocket-Version";
    public static final String SEC_WEBSOCKET_KEY = "Sec-WebSocket-Key";
    public static final String SEC_WEBSOCKET_ACCEPT = "Sec-WebSocket-Accept";
    public static final String SEC_WEBSOCKET_08_ACCEPT_GUID = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";

    private URI webSocketURL;

    private WebSocketVersion version = WebSocketVersion.UNKNOWN;

    private boolean openningHandshakeCompleted = false;

    private String expectedChallengeResponseString = null;
    private byte[] expectedChallengeResponseBytes = null;

    private String protocol = null;

    /**
     * Constructor specifying the destination web socket location and version to initiate
     * 
     * @param webSocketURL
     *            URL for web socket communications. e.g "ws://myhost.com/mypath". Subsequent web socket frames will be
     *            sent to this URL.
     * @param version
     *            Version of web socket specification to use to connect to the server
     */
    public WebSocketClientHandshaker(URI webSocketURL, WebSocketVersion version) {
        this.webSocketURL = webSocketURL;
        this.version = version;
        return;
    }

    /**
     * Constructor specifying the destination web socket location and version to initiate
     * 
     * @param webSocketURL
     *            URL for web socket communications. e.g "ws://myhost.com/mypath". Subsequent web socket frames will be
     *            sent to this URL.
     * @param version
     *            Version of web socket specification to use to connect to the server
     * @param protocol
     *            Protocol to use. e.g "chat"
     */
    public WebSocketClientHandshaker(URI webSocketURL, WebSocketVersion version, String protocol) {
        this.webSocketURL = webSocketURL;
        this.version = version;
        return;
    }

    /**
     * Performs the opening handshake
     * 
     * @param ctx
     *            Context
     * @param channel
     *            Channel into which we can write our request
     */
    public void beginOpeningHandshake(ChannelHandlerContext ctx, Channel channel) {
        if (this.version == WebSocketVersion.HYBI08) {
            beginOpeningHandshake08(ctx, channel);
        } else {
            beginOpeningHandshake00(ctx, channel);
        }
    }

    /**
     * Processes the response from the openning handshake
     * 
     * @param ctx
     *            Context
     * @param response
     *            HTTP response
     * @throws WebSocketHandshakeException
     */
    public void endOpeningHandshake(ChannelHandlerContext ctx, HttpResponse response)
            throws WebSocketHandshakeException {
        if (this.version == WebSocketVersion.HYBI08) {
            endOpeningHandshake08(ctx, response);
        } else {
            endOpeningHandshake00(ctx, response);
        }
        openningHandshakeCompleted = true;
    }

    /**
     * The web socket version that is required by the client
     */
    public WebSocketVersion getVersion() {
        return this.version;
    }

    /**
     * Indicates if we have completed our opening handshake or not
     */
    public boolean isOpeningHandshakeCompleted() {
        return openningHandshakeCompleted;
    }

    /**
     * <p>
     * Sends the opening request to the server:
     * </p>
     * 
     * <pre>
     * GET /chat HTTP/1.1
     * Host: server.example.com
     * Upgrade: websocket
     * Connection: Upgrade
     * Sec-WebSocket-Key: dGhlIHNhbXBsZSBub25jZQ==
     * Sec-WebSocket-Origin: http://example.com
     * Sec-WebSocket-Protocol: chat, superchat
     * Sec-WebSocket-Version: 8
     * </pre>
     * 
     * @param ctx
     *            Channel context
     * @param channel
     *            Channel into which we can write our request
     */
    private void beginOpeningHandshake08(ChannelHandlerContext ctx, Channel channel) {
        // Get path
        String path = this.webSocketURL.getPath();
        if (this.webSocketURL.getQuery() != null && this.webSocketURL.getQuery().length() > 0) {
            path = this.webSocketURL.getPath() + "?" + this.webSocketURL.getQuery();
        }

        // Get 16 bit nonce and base 64 encode it
        byte[] nonce = createRandomBytes(16);
        String key = Base64.encode(nonce);

        String acceptSeed = key + SEC_WEBSOCKET_08_ACCEPT_GUID;
        byte[] sha1 = sha1(acceptSeed.getBytes(CharsetUtil.US_ASCII));
        this.expectedChallengeResponseString = Base64.encode(sha1);

        _logger.debug("HyBi08 Client Handshake key: %s. Expected response: %s.", key, this.expectedChallengeResponseString);
        
        // Format request
        HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, path);
        request.addHeader(Names.UPGRADE, Values.WEBSOCKET.toLowerCase());
        request.addHeader(Names.CONNECTION, Values.UPGRADE);
        request.addHeader(SEC_WEBSOCKET_KEY, key);
        request.addHeader(Names.HOST, this.webSocketURL.getHost());
        request.addHeader(Names.ORIGIN, "http://" + this.webSocketURL.getHost());
        if (protocol != null && !protocol.equals("")) {
            request.addHeader(Names.SEC_WEBSOCKET_PROTOCOL, protocol);
        }
        request.addHeader(SEC_WEBSOCKET_VERSION, "8");

        channel.write(request);

        ctx.getPipeline().replace("encoder", "ws-encoder", new WebSocket08FrameEncoder(true));
    }

    /**
     * <p>
     * Process server response:
     * </p>
     * 
     * <pre>
     * HTTP/1.1 101 Switching Protocols
     * Upgrade: websocket
     * Connection: Upgrade
     * Sec-WebSocket-Accept: s3pPLMBiTxaQ9kYGzzhZRbK+xOo=
     * Sec-WebSocket-Protocol: chat
     * </pre>
     * 
     * @param ctx
     *            Channel context
     * @param response
     *            HTTP response returned from the server for the request sent by beginOpeningHandshake00().
     * @throws WebSocketHandshakeException
     */
    private void endOpeningHandshake08(ChannelHandlerContext ctx, HttpResponse response)
            throws WebSocketHandshakeException {
        final HttpResponseStatus status = new HttpResponseStatus(101, "Switching Protocols");

        if (!response.getStatus().equals(status)) {
            throw new WebSocketHandshakeException("Invalid handshake response status: " + response.getStatus());
        }

        String upgrade = response.getHeader(Names.UPGRADE);
        if (upgrade == null || !upgrade.equals(Values.WEBSOCKET.toLowerCase())) {
            throw new WebSocketHandshakeException("Invalid handshake response upgrade: "
                    + response.getHeader(Names.UPGRADE));
        }

        String connection = response.getHeader(Names.CONNECTION);
        if (connection == null || !connection.equals(Values.UPGRADE)) {
            throw new WebSocketHandshakeException("Invalid handshake response connection: "
                    + response.getHeader(Names.CONNECTION));
        }

        String accept = response.getHeader(SEC_WEBSOCKET_ACCEPT);
        if (accept == null || !accept.equals(this.expectedChallengeResponseString)) {
            throw new WebSocketHandshakeException(String.format("Invalid challenge. Actual: %s. Expected: %s", accept,
                    this.expectedChallengeResponseString));
        }

        ctx.getPipeline().replace("decoder", "ws-decoder", new WebSocket08FrameDecoder(false));
        return;
    }

    /**
     * <p>
     * Sends the opening request to the server:
     * </p>
     * 
     * <pre>
     * GET /demo HTTP/1.1
     * Upgrade: WebSocket
     * Connection: Upgrade
     * Host: example.com
     * Origin: http://example.com
     * Sec-WebSocket-Key1: 4 @1  46546xW%0l 1 5
     * Sec-WebSocket-Key2: 12998 5 Y3 1  .P00
     * 
     * ^n:ds[4U
     * </pre>
     * 
     * @param ctx
     *            Channel context
     * @param channel
     *            Channel into which we can write our request
     */
    private void beginOpeningHandshake00(ChannelHandlerContext ctx, Channel channel) {
        // Make keys
        int spaces1 = rand(1, 12);
        int spaces2 = rand(1, 12);

        int max1 = Integer.MAX_VALUE / spaces1;
        int max2 = Integer.MAX_VALUE / spaces2;

        int number1 = rand(0, max1);
        int number2 = rand(0, max2);

        int product1 = number1 * spaces1;
        int product2 = number2 * spaces2;

        String key1 = Integer.toString(product1);
        String key2 = Integer.toString(product2);

        key1 = insertRandomCharacters(key1);
        key2 = insertRandomCharacters(key2);

        key1 = insertSpaces(key1, spaces1);
        key2 = insertSpaces(key2, spaces2);

        byte[] key3 = createRandomBytes(8);

        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(number1);
        byte[] number1Array = buffer.array();
        buffer = ByteBuffer.allocate(4);
        buffer.putInt(number2);
        byte[] number2Array = buffer.array();

        byte[] challenge = new byte[16];
        System.arraycopy(number1Array, 0, challenge, 0, 4);
        System.arraycopy(number2Array, 0, challenge, 4, 4);
        System.arraycopy(key3, 0, challenge, 8, 8);
        this.expectedChallengeResponseBytes = md5(challenge);

        // Get path
        String path = this.webSocketURL.getPath();
        if (this.webSocketURL.getQuery() != null && this.webSocketURL.getQuery().length() > 0) {
            path = this.webSocketURL.getPath() + "?" + this.webSocketURL.getQuery();
        }

        // Format request
        HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, path);
        request.addHeader(Names.UPGRADE, Values.WEBSOCKET);
        request.addHeader(Names.CONNECTION, Values.UPGRADE);
        request.addHeader(Names.HOST, this.webSocketURL.getHost());
        request.addHeader(Names.ORIGIN, "http://" + this.webSocketURL.getHost());
        request.addHeader(Names.SEC_WEBSOCKET_KEY1, key1);
        request.addHeader(Names.SEC_WEBSOCKET_KEY2, key2);
        if (protocol != null && !protocol.equals("")) {
            request.addHeader(Names.SEC_WEBSOCKET_PROTOCOL, protocol);
        }
        request.setContent(ChannelBuffers.copiedBuffer(key3));

        channel.write(request);

        ctx.getPipeline().replace("encoder", "ws-encoder", new WebSocket00FrameEncoder());
    }

    /**
     * <p>
     * Process server response:
     * </p>
     * 
     * <pre>
     * HTTP/1.1 101 WebSocket Protocol Handshake
     * Upgrade: WebSocket
     * Connection: Upgrade
     * Sec-WebSocket-Origin: http://example.com
     * Sec-WebSocket-Location: ws://example.com/demo
     * Sec-WebSocket-Protocol: sample
     * 
     * 8jKS'y:G*Co,Wxa-
     * </pre>
     * 
     * @param ctx
     *            Channel context
     * @param response
     *            HTTP response returned from the server for the request sent by beginOpeningHandshake00().
     * @throws WebSocketHandshakeException
     */
    private void endOpeningHandshake00(ChannelHandlerContext ctx, HttpResponse response)
            throws WebSocketHandshakeException {
        final HttpResponseStatus status = new HttpResponseStatus(101, "WebSocket Protocol Handshake");

        if (!response.getStatus().equals(status)) {
            throw new WebSocketHandshakeException("Invalid handshake response status: " + response.getStatus());
        }

        String upgrade = response.getHeader(Names.UPGRADE);
        if (upgrade == null || !upgrade.equals(Values.WEBSOCKET)) {
            throw new WebSocketHandshakeException("Invalid handshake response upgrade: "
                    + response.getHeader(Names.UPGRADE));
        }

        String connection = response.getHeader(Names.CONNECTION);
        if (connection == null || !connection.equals(Values.UPGRADE)) {
            throw new WebSocketHandshakeException("Invalid handshake response connection: "
                    + response.getHeader(Names.CONNECTION));
        }

        byte[] challenge = response.getContent().array();
        if (!Arrays.equals(challenge, expectedChallengeResponseBytes)) {
            throw new WebSocketHandshakeException("Invalid challenge");
        }

        ctx.getPipeline().replace("decoder", "ws-decoder", new WebSocket00FrameDecoder());
        return;
    }

    private String insertRandomCharacters(String key) {
        int count = rand(1, 12);

        char[] randomChars = new char[count];
        int randCount = 0;
        while (randCount < count) {
            int rand = (int) (Math.random() * 0x7e + 0x21);
            if (((0x21 < rand) && (rand < 0x2f)) || ((0x3a < rand) && (rand < 0x7e))) {
                randomChars[randCount] = (char) rand;
                randCount += 1;
            }
        }

        for (int i = 0; i < count; i++) {
            int split = rand(0, key.length());
            String part1 = key.substring(0, split);
            String part2 = key.substring(split);
            key = part1 + randomChars[i] + part2;
        }

        return key;
    }

    private String insertSpaces(String key, int spaces) {
        for (int i = 0; i < spaces; i++) {
            int split = rand(1, key.length() - 1);
            String part1 = key.substring(0, split);
            String part2 = key.substring(split);
            key = part1 + " " + part2;
        }

        return key;
    }

    private byte[] createRandomBytes(int size) {
        byte[] bytes = new byte[size];

        for (int i = 0; i < size; i++) {
            bytes[i] = (byte) rand(0, 255);
        }

        return bytes;
    }

    private byte[] md5(byte[] bytes) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            return md.digest(bytes);
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    private byte[] sha1(byte[] bytes) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA1");
            return md.digest(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new InternalError("SHA-1 not supported on this platform");
        }
    }

    private int rand(int min, int max) {
        int rand = (int) (Math.random() * max + min);
        return rand;
    }

}
