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

package org.chililog.server.pubsub.jsonhttp;

import static org.jboss.netty.handler.codec.http.HttpHeaders.*;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.*;
import static org.jboss.netty.handler.codec.http.HttpMethod.*;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.*;
import static org.jboss.netty.handler.codec.http.HttpVersion.*;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import org.apache.commons.lang.StringUtils;
import org.chililog.server.common.Log4JLogger;
import org.chililog.server.pubsub.websocket.CloseWebSocketFrame;
import org.chililog.server.pubsub.websocket.TextWebSocketFrame;
import org.chililog.server.pubsub.websocket.WebSocketFrame;
import org.chililog.server.pubsub.websocket.WebSocketServerHandshaker;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.util.CharsetUtil;

/**
 * Handler for JSON log entries send over HTTP request and web socket.
 * 
 * @author vibul
 */
public class JsonHttpRequestHandler extends SimpleChannelUpstreamHandler {

    private static Log4JLogger _logger = Log4JLogger.getLogger(JsonHttpRequestHandler.class);

    private static final String PUBLISH_PATH = "/publish";

    private static final String WEBSOCKET_PATH = "/websocket";

    private static final Charset UTF_8_CHARSET = Charset.forName("UTF-8");

    private SubscriptionWorker _subscriptionWorker = null;

    private WebSocketServerHandshaker _handshaker = null;

    /**
     * Handles incoming HTTP data
     * 
     * @param ctx
     *            Channel Handler Context
     * @param e
     *            Message event
     */
    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        Object msg = e.getMessage();
        if (msg instanceof HttpRequest) {
            handleHttpRequest(ctx, e, (HttpRequest) msg);
        } else if (msg instanceof WebSocketFrame) {
            handleWebSocketFrame(ctx, (WebSocketFrame) msg);
        } else {
            throw new UnsupportedOperationException("Message of type '" + msg.getClass().getName()
                    + "' is not supported.");
        }
    }

    /**
     * <p>
     * Handles HTTP requests.
     * </p>
     * <p>
     * HTTP requests can only be for web socket handshake or for JSON log publishing
     * </p>
     * 
     * @param ctx
     *            Channel Handler Context
     * @param e
     *            Message event
     * @param req
     *            HTTP request
     * @throws Exception
     */
    private void handleHttpRequest(ChannelHandlerContext ctx, MessageEvent e, HttpRequest req) throws Exception {
        if (req.getMethod() == GET) {
            // Web socket handshake
            if (req.getUri().equals(WEBSOCKET_PATH)) {
                String wsURL = "ws://" + req.getHeader(HttpHeaders.Names.HOST) + WEBSOCKET_PATH;
                _handshaker = new WebSocketServerHandshaker(wsURL);
                _handshaker.executeOpeningHandshake(ctx, req);
                return;
            }
        } else if (req.getMethod() == POST && req.getUri().equals(PUBLISH_PATH)) {
            // Get request content
            ChannelBuffer content = req.getContent();
            if (!content.readable()) {
                throw new IllegalStateException("HTTP request content is NOT readable");
            }

            byte[] requestContent = content.array();
            String requestJson = bytesToString(requestContent);
            PublicationWorker worker = new PublicationWorker(JsonHttpService.getInstance().getMqProducerSessionPool());
            StringBuilder responseJson = new StringBuilder();

            _logger.debug("Publication Worker Request:\n%s", requestJson);

            boolean success = worker.process(requestJson, responseJson);

            _logger.debug("Publication Worker Response:\n%s", responseJson);

            HttpResponse res = success ? new DefaultHttpResponse(HTTP_1_1, OK) : new DefaultHttpResponse(HTTP_1_1,
                    BAD_REQUEST);
            res.setHeader(CONTENT_TYPE, "application/json; charset=UTF-8");
            res.setContent(ChannelBuffers.copiedBuffer(responseJson.toString(), UTF_8_CHARSET));
            sendHttpResponse(ctx, req, res);
            return;
        }

        // Only GET and POST are permitted
        sendHttpResponse(ctx, req, new DefaultHttpResponse(HTTP_1_1, FORBIDDEN));
        return;
    }

    /**
     * Converts request bytes into a string using the default UTF-8 character set.
     * 
     * @param bytes
     *            Bytes to convert
     * @return String form the bytes. If bytes is null, null is returned.
     * @throws UnsupportedEncodingException
     */
    private String bytesToString(byte[] bytes) throws UnsupportedEncodingException {
        if (bytes == null) {
            return null;
        }

        return new String(bytes, UTF_8_CHARSET);
    }

    /**
     * <p>
     * Figure out if this is a publish or subscribe request and do it.
     * </p>
     * 
     * @param ctx
     * @param frame
     */
    private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
        _logger.debug("Channel %s got frame type %s.", ctx.getChannel().getId(), frame.getType());

        // Check for closing frame
        if (frame instanceof CloseWebSocketFrame) {
            _handshaker.executeClosingHandshake(ctx, frame);
            return;
        }

        // Assume we only get text frames
        TextWebSocketFrame textFrame = (TextWebSocketFrame) frame;
        String requestJson = textFrame.getText();
        String responseJson = null;

        if (StringUtils.isBlank(requestJson)) {
            return;
        }

        _logger.debug("Channel %s Request JSON: %s", ctx.getChannel().getId(), requestJson);

        // Process according to request type
        // We do a quick peek in the json in order to dispatch to the required worker
        String first50Characters = requestJson.length() > 50 ? requestJson.substring(0, 50) : requestJson;
        if (first50Characters.indexOf("\"PublicationRequest\"") > 0) {
            PublicationWorker worker = new PublicationWorker(JsonHttpService.getInstance().getMqProducerSessionPool());

            StringBuilder sb = new StringBuilder();
            worker.process(requestJson, sb);
            responseJson = sb.toString();
        } else if (first50Characters.indexOf("\"SubscriptionRequest\"") > 0) {
            // If existing subscription exists, stop it first
            if (_subscriptionWorker != null) {
                _subscriptionWorker.stop();
            }

            _subscriptionWorker = new SubscriptionWorker(ctx.getChannel());

            StringBuilder sb = new StringBuilder();
            _subscriptionWorker.process(requestJson, sb);
            responseJson = sb.toString();
        } else {
            throw new UnsupportedOperationException("Unsupported request: " + requestJson);
        }

        _logger.debug("Response JSON: %s", responseJson);
        ctx.getChannel().write(new TextWebSocketFrame(responseJson));
    }

    /**
     * Returns a HTTP response
     * 
     * @param ctx
     *            Content
     * @param req
     *            HTTP request
     * @param res
     *            HTTP response
     */
    private void sendHttpResponse(ChannelHandlerContext ctx, HttpRequest req, HttpResponse res) {
        // Decide whether to close the connection or not.
        boolean isKeepAlive = isKeepAlive(req);

        // Set content if one is not set
        if (res.getContent() == null || res.getContent().readableBytes() == 0) {
            res.setContent(ChannelBuffers.copiedBuffer(res.getStatus().toString(), CharsetUtil.UTF_8));
            setContentLength(res, res.getContent().readableBytes());
        }

        // Add 'Content-Length' header only for a keep-alive connection.
        if (isKeepAlive) {
            res.setHeader(CONTENT_LENGTH, res.getContent().readableBytes());
        }

        // Send the response
        ChannelFuture f = ctx.getChannel().write(res);

        // Close the connection if necessary.
        if (!isKeepAlive || res.getStatus().getCode() != 200) {
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }

    /**
     * Handle exception by printing it and closing socket
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        try {
            _logger.debug(e.getCause(), "Error handling PubSub JSON HTTP Request");
            e.getChannel().close();
        } catch (Exception ex) {
            _logger.debug(ex, "Error closing channel in exception");
        }
    }

    /**
     * Add channel to channel group to disconnect when shutting down. Channel group automatically removes closed
     * channels.
     */
    @Override
    public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) {
        JsonHttpService.getInstance().getAllChannels().add(e.getChannel());
    }

    /**
     * If disconnected, stop subscription if one is present
     */
    @Override
    public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        _logger.debug("Channel %s disconnected", ctx.getChannel().getId());

        if (_subscriptionWorker != null) {
            _subscriptionWorker.stop();
        }

        super.channelDisconnected(ctx, e);
    }
}
