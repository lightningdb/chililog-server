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

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;

/**
 * Instances the appropriate handshake class to use for clients
 * 
 * @author vibul
 */
public class WebSocketServerHandshakerFactory {

    private String webSocketURL;

    private String subProtocols;

    /**
     * Constructor specifying the destination web socket location
     * 
     * @param webSocketURL
     *            URL for web socket communications. e.g "ws://myhost.com/mypath". Subsequent web socket frames will be
     *            sent to this URL.
     * @param subProtocols
     *            CSV of supported protocols. Null if sub protocols not supported.
     */
    public WebSocketServerHandshakerFactory(String webSocketURL, String subProtocols) {
        this.webSocketURL = webSocketURL;
        this.subProtocols = subProtocols;
        return;
    }

    /**
     * Instances a new handshaker
     * 
     * @param webSocketURL
     *            URL for web socket communications. e.g "ws://myhost.com/mypath". Subsequent web socket frames will be
     *            sent to this URL.
     * @param version
     *            Version of web socket specification to use to connect to the server
     * @param subProtocol
     *            Sub protocol request sent to the server. Null if no sub-protocol support is required.
     * @throws WebSocketHandshakeException
     */
    public WebSocketServerHandshaker newHandshaker(ChannelHandlerContext ctx, HttpRequest req)
            throws WebSocketHandshakeException {

        String version = req.getHeader(WebSocketClientHandshaker10.SEC_WEBSOCKET_VERSION);
        if (version != null) {
            if (version.equals("8")) {
                // Version 8 of the wire protocol - assume version 10 of the specification.
                return new WebSocketServerHandshaker10(webSocketURL, subProtocols);
            } else {
                throw new WebSocketHandshakeException("");
            }
        } else {
            // Assume version 00 where version header was not specified
            return new WebSocketServerHandshaker00(webSocketURL, subProtocols);
        }
    }

    /**
     * Return that we need cannot not support the web socket version
     * 
     * @param ctx
     *            Context
     * @param req
     *            HTTP Request
     */
    public void sendUnsupportedWebSocketVersionResponse(ChannelHandlerContext ctx, HttpRequest req) {
        HttpResponse res = new DefaultHttpResponse(HttpVersion.HTTP_1_1, new HttpResponseStatus(101,
                "Switching Protocols"));
        res.setStatus(HttpResponseStatus.UPGRADE_REQUIRED);
        res.setHeader(WebSocketClientHandshaker10.SEC_WEBSOCKET_VERSION, "8");
        ctx.getChannel().write(res);
        return;
    }

}
