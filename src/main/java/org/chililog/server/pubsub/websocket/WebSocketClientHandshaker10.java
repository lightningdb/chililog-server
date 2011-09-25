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

import org.chililog.server.common.Log4JLogger;
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
 * <p>
 * Performs client side opening and closing handshakes for web socket specification version <a
 * href="http://tools.ietf.org/html/draft-ietf-hybi-thewebsocketprotocol-10">draft-ietf-hybi-thewebsocketprotocol-
 * 10</a>
 * </p>
 * 
 * @author vibul
 */
public class WebSocketClientHandshaker10 extends WebSocketClientHandshaker {

    private static Log4JLogger _logger = Log4JLogger.getLogger(WebSocketClientHandshaker10.class);

    public static final String SEC_WEBSOCKET_VERSION = "Sec-WebSocket-Version";
    public static final String SEC_WEBSOCKET_KEY = "Sec-WebSocket-Key";
    public static final String SEC_WEBSOCKET_ACCEPT = "Sec-WebSocket-Accept";
    public static final String MAGIC_GUID = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";

    private String expectedChallengeResponseString = null;

    private String protocol = null;

    /**
     * Constructor specifying the destination web socket location and version to initiate
     * 
     * @param webSocketURL
     *            URL for web socket communications. e.g "ws://myhost.com/mypath". Subsequent web socket frames will be
     *            sent to this URL.
     * @param version
     *            Version of web socket specification to use to connect to the server
     * @param subProtocol
     *            Sub protocol request sent to the server.
     */
    public WebSocketClientHandshaker10(URI webSocketURL, WebSocketSpecificationVersion version, String subProtocol) {
        super(webSocketURL, version, subProtocol);
        return;
    }

    /**
     * /**
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
    @Override
    public void beginOpeningHandshake(ChannelHandlerContext ctx, Channel channel) {
        // Get path
        URI wsURL = this.getWebSocketURL();
        String path = wsURL.getPath();
        if (wsURL.getQuery() != null && wsURL.getQuery().length() > 0) {
            path = wsURL.getPath() + "?" + wsURL.getQuery();
        }

        // Get 16 bit nonce and base 64 encode it
        byte[] nonce = createRandomBytes(16);
        String key = Base64.encode(nonce);

        String acceptSeed = key + MAGIC_GUID;
        byte[] sha1 = sha1(acceptSeed.getBytes(CharsetUtil.US_ASCII));
        this.expectedChallengeResponseString = Base64.encode(sha1);

        _logger.debug("HyBi10 Client Handshake key: %s. Expected response: %s.", key,
                this.expectedChallengeResponseString);

        // Format request
        HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, path);
        request.addHeader(Names.UPGRADE, Values.WEBSOCKET.toLowerCase());
        request.addHeader(Names.CONNECTION, Values.UPGRADE);
        request.addHeader(SEC_WEBSOCKET_KEY, key);
        request.addHeader(Names.HOST, wsURL.getHost());
        request.addHeader(Names.ORIGIN, "http://" + wsURL.getHost());
        if (protocol != null && !protocol.equals("")) {
            request.addHeader(Names.SEC_WEBSOCKET_PROTOCOL, protocol);
        }
        request.addHeader(SEC_WEBSOCKET_VERSION, "8");

        channel.write(request);

        ctx.getPipeline().replace("encoder", "ws-encoder", new WebSocket08FrameEncoder(true));
        return;
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
    @Override
    public void endOpeningHandshake(ChannelHandlerContext ctx, HttpResponse response)
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
        
        this.setOpenningHandshakeCompleted(true);
        return;
    }

}
