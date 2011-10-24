/*
 * Copyright 2010 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.chililog.server.pubsub.websocket;

import static org.jboss.netty.handler.codec.http.HttpHeaders.Values.WEBSOCKET;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.security.NoSuchAlgorithmException;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpHeaders.Names;
import org.jboss.netty.logging.InternalLogger;
import org.jboss.netty.logging.InternalLoggerFactory;
import org.jboss.netty.util.CharsetUtil;

/**
 * <p>
 * Performs server side opening and closing handshakes for web socket
 * specification version <a
 * href="http://tools.ietf.org/html/draft-ietf-hybi-thewebsocketprotocol-10"
 * >draft-ietf-hybi-thewebsocketprotocol- 10</a>
 * </p>
 * 
 * @author <a href="http://www.jboss.org/netty/">The Netty Project</a>
 */
public class WebSocketServerHandshaker10 extends WebSocketServerHandshaker {

	private static final InternalLogger logger = InternalLoggerFactory.getInstance(WebSocketServerHandshaker10.class);

	public static final String WEBSOCKET_08_ACCEPT_GUID = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";

	private boolean allowExtensions = false;

	/**
	 * Constructor specifying the destination web socket location
	 * 
	 * @param webSocketURL
	 *            URL for web socket communications. e.g
	 *            "ws://myhost.com/mypath". Subsequent web socket frames will be
	 *            sent to this URL.
	 * @param subProtocols
	 *            CSV of supported protocols
	 * @param allowExtensions
	 *            Allow extensions to be used in the reserved bits of the web
	 *            socket frame
	 */
	public WebSocketServerHandshaker10(String webSocketURL, String subProtocols, boolean allowExtensions) {
		super(webSocketURL, subProtocols);
		this.allowExtensions = allowExtensions;
		return;
	}

	/**
	 * <p>
	 * Handle the web socket handshake for the web socket specification <a href=
	 * "http://tools.ietf.org/html/draft-ietf-hybi-thewebsocketprotocol-08">HyBi
	 * version 8 to 10</a>. Version 8, 9 and 10 share the same wire protocol.
	 * </p>
	 * 
	 * <p>
	 * Browser request to the server:
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
	 * <p>
	 * Server response:
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
	 * @param req
	 *            HTTP request
	 * @throws NoSuchAlgorithmException
	 */
	@Override
	public void executeOpeningHandshake(ChannelHandlerContext ctx, HttpRequest req) {

		if (logger.isDebugEnabled()) {
			logger.debug(String.format("Channel %s web socket spec version 10 handshake", ctx.getChannel().getId()));
		}

		HttpResponse res = new DefaultHttpResponse(HTTP_1_1, new HttpResponseStatus(101, "Switching Protocols"));
		this.setVersion(WebSocketSpecificationVersion.V10);

		String key = req.getHeader(WebSocketServerHandshakerFactory.SEC_WEBSOCKET_KEY);
		if (key == null) {
			res.setStatus(HttpResponseStatus.BAD_REQUEST);
			return;
		}
		String acceptSeed = key + WEBSOCKET_08_ACCEPT_GUID;
		byte[] sha1 = sha1(acceptSeed.getBytes(CharsetUtil.US_ASCII));
		String accept = base64Encode(sha1);

		if (logger.isDebugEnabled()) {
			logger.debug(String.format("HyBi10 Server Handshake key: %s. Response: %s.", key, accept));
		}

		res.setStatus(new HttpResponseStatus(101, "Switching Protocols"));
		res.addHeader(Names.UPGRADE, WEBSOCKET.toLowerCase());
		res.addHeader(Names.CONNECTION, Names.UPGRADE);
		res.addHeader(WebSocketServerHandshakerFactory.SEC_WEBSOCKET_ACCEPT, accept);
		String protocol = req.getHeader(Names.SEC_WEBSOCKET_PROTOCOL);
		if (protocol != null) {
			res.addHeader(Names.SEC_WEBSOCKET_PROTOCOL, this.selectSubProtocol(protocol));
		}

		ctx.getChannel().write(res);

		// Upgrade the connection and send the handshake response.
		ChannelPipeline p = ctx.getChannel().getPipeline();
		p.remove("aggregator");
		p.replace("decoder", "wsdecoder", new WebSocket08FrameDecoder(true, this.allowExtensions));
		p.replace("encoder", "wsencoder", new WebSocket08FrameEncoder(false));

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
	@Override
	public void executeClosingHandshake(ChannelHandlerContext ctx, CloseWebSocketFrame frame) {
		ctx.getChannel().write(frame);
	}

}
