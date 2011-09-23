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

import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.ORIGIN;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.SEC_WEBSOCKET_KEY1;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.SEC_WEBSOCKET_KEY2;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.SEC_WEBSOCKET_LOCATION;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.SEC_WEBSOCKET_ORIGIN;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.SEC_WEBSOCKET_PROTOCOL;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.WEBSOCKET_LOCATION;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.WEBSOCKET_ORIGIN;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.WEBSOCKET_PROTOCOL;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Values.WEBSOCKET;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.chililog.server.common.Log4JLogger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpHeaders.Names;
import org.jboss.netty.handler.codec.http.HttpHeaders.Values;

/**
 * Opening and closing handshake for servers
 * 
 * @author vibul
 */
public class WebSocketServerHandshaker {

    private static Log4JLogger _logger = Log4JLogger.getLogger(WebSocketServerHandshaker.class);

    private String webSocketURL;

    private WebSocketVersion version = WebSocketVersion.UNKNOWN;

    /**
     * Constructor specifying the destination web socket location
     * 
     * @param webSocketURL
     *            URL for web socket communications. e.g "ws://myhost.com/mypath". Subsequent web socket frames will be
     *            sent to this URL.
     */
    public WebSocketServerHandshaker(String webSocketURL) {
        this.webSocketURL = webSocketURL;
        return;
    }

    /**
     * Performs the opening handshake to establish ws location and version
     * 
     * @param ctx
     *            Context
     * @param req
     *            HTTP Request
     * @throws NoSuchAlgorithmException
     */
    public void executeOpeningHandshake(ChannelHandlerContext ctx, HttpRequest req) throws NoSuchAlgorithmException {
        if (isHybi10WebSocketRequest(req)) {
            // upgradeResponse08(req, res);
        } else {
            executeOpeningHandshake00(ctx, req);
        }
    }

    public void executeClosingHandshake(ChannelHandlerContext ctx, WebSocketFrame frame) {
        if (this.version == WebSocketVersion.HYBI08) {
            // upgradeResponse08(req, res);
        } else {
            executeClosingHandshake00(ctx, frame);
        }
    }

    private boolean isHybi10WebSocketRequest(HttpRequest req) {
        return req.containsHeader("Sec-WebSocket-Version");
    }

    /**
     * The web socket version that is required by the client
     */
    public WebSocketVersion getVersion() {
        return this.version;
    }

    /**
     * <p>
     * Handle the web socket handshake for the web socket specification <a
     * href="http://tools.ietf.org/html/draft-ietf-hybi-thewebsocketprotocol-00">HyBi version 0</a> and lower. This
     * standard is really a rehash of <a
     * href="http://tools.ietf.org/html/draft-hixie-thewebsocketprotocol-76">hixie-76</a> and <a
     * href="http://tools.ietf.org/html/draft-hixie-thewebsocketprotocol-75">hixie-75</a>.
     * </p>
     * 
     * <p>
     * Browser request to the server:
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
     * <p>
     * Server response:
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
     * @param req
     *            HTTP request
     * @throws NoSuchAlgorithmException
     */
    private void executeOpeningHandshake00(ChannelHandlerContext ctx, HttpRequest req) throws NoSuchAlgorithmException {
        _logger.debug("Channel %s web socket version 00 handshake", ctx.getChannel().getId());

        this.version = WebSocketVersion.HYBI00;

        // Serve the WebSocket handshake request.
        if (!Values.UPGRADE.equalsIgnoreCase(req.getHeader(CONNECTION))
                || !WEBSOCKET.equalsIgnoreCase(req.getHeader(Names.UPGRADE))) {
            return;
        }

        // Hixie 75 does not contain these headers while Hixie 76 does
        boolean isHixie76 = req.containsHeader(SEC_WEBSOCKET_KEY1) && req.containsHeader(SEC_WEBSOCKET_KEY2);

        // Create the WebSocket handshake response.
        HttpResponse res = new DefaultHttpResponse(HTTP_1_1, new HttpResponseStatus(101,
                isHixie76 ? "WebSocket Protocol Handshake" : "Web Socket Protocol Handshake"));
        res.addHeader(Names.UPGRADE, WEBSOCKET);
        res.addHeader(CONNECTION, Values.UPGRADE);

        // Fill in the headers and contents depending on handshake method.
        if (isHixie76) {
            // New handshake method with a challenge:
            res.addHeader(SEC_WEBSOCKET_ORIGIN, req.getHeader(ORIGIN));
            res.addHeader(SEC_WEBSOCKET_LOCATION, this.webSocketURL);
            String protocol = req.getHeader(SEC_WEBSOCKET_PROTOCOL);
            if (protocol != null) {
                res.addHeader(SEC_WEBSOCKET_PROTOCOL, protocol);
            }

            // Calculate the answer of the challenge.
            String key1 = req.getHeader(SEC_WEBSOCKET_KEY1);
            String key2 = req.getHeader(SEC_WEBSOCKET_KEY2);
            int a = (int) (Long.parseLong(key1.replaceAll("[^0-9]", "")) / key1.replaceAll("[^ ]", "").length());
            int b = (int) (Long.parseLong(key2.replaceAll("[^0-9]", "")) / key2.replaceAll("[^ ]", "").length());
            long c = req.getContent().readLong();
            ChannelBuffer input = ChannelBuffers.buffer(16);
            input.writeInt(a);
            input.writeInt(b);
            input.writeLong(c);
            ChannelBuffer output = ChannelBuffers.wrappedBuffer(MessageDigest.getInstance("MD5").digest(input.array()));
            res.setContent(output);
        } else {
            // Old Hixie 75 handshake method with no challenge:
            res.addHeader(WEBSOCKET_ORIGIN, req.getHeader(ORIGIN));
            res.addHeader(WEBSOCKET_LOCATION, this.webSocketURL);
            String protocol = req.getHeader(WEBSOCKET_PROTOCOL);
            if (protocol != null) {
                res.addHeader(WEBSOCKET_PROTOCOL, protocol);
            }
        }

        // Upgrade the connection and send the handshake response.
        ChannelPipeline p = ctx.getChannel().getPipeline();
        p.remove("aggregator");
        p.replace("decoder", "wsdecoder", new WebSocket00FrameDecoder());

        ctx.getChannel().write(res);

        p.replace("encoder", "wsencoder", new WebSocket00FrameEncoder());
        return;
    }

    /**
     * Echo back the closing frame
     * 
     * @param ctx
     *            Channel context
     * @param frame
     *            Web Socket frame that was received
     */
    private void executeClosingHandshake00(ChannelHandlerContext ctx, WebSocketFrame frame) {
        _logger.debug("Channel %s got web socket closing frame. Echoing it back.", ctx.getChannel().getId());
        ctx.getChannel().write(frame);
    }


}
