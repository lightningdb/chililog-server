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
import static org.jboss.netty.handler.codec.http.HttpHeaders.Values.*;
import static org.jboss.netty.handler.codec.http.HttpMethod.*;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.*;
import static org.jboss.netty.handler.codec.http.HttpVersion.*;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.lang.StringUtils;
import org.chililog.server.common.Log4JLogger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpHeaders.Names;
import org.jboss.netty.handler.codec.http.HttpHeaders.Values;
import org.jboss.netty.handler.codec.http.websocket.DefaultWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocket.WebSocketFrame;
import org.jboss.netty.handler.codec.http.websocket.WebSocketFrameDecoder;
import org.jboss.netty.handler.codec.http.websocket.WebSocketFrameEncoder;
import org.jboss.netty.util.CharsetUtil;

/**
 * Handler for JSON log entries send over HTTP request and web socket.
 * 
 * @author vibul
 */
public class JsonHttpRequestHandler extends SimpleChannelUpstreamHandler
{
    private static Log4JLogger _logger = Log4JLogger.getLogger(JsonHttpRequestHandler.class);

    private static final String PUBLISH_PATH = "/publish";

    private static final String WEBSOCKET_PATH = "/websocket";

    private static final Charset UTF_8_CHARSET = Charset.forName("UTF-8");

    private SubscriptionWorker _subscriptionWorker = null;

    /**
     * Handles incoming HTTP data
     * 
     * @param ctx
     *            Channel Handler Context
     * @param e
     *            Message event
     */
    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception
    {
        Object msg = e.getMessage();
        if (msg instanceof HttpRequest)
        {
            handleHttpRequest(ctx, e, (HttpRequest) msg);
        }
        else if (msg instanceof WebSocketFrame)
        {
            handleWebSocketFrame(ctx, (WebSocketFrame) msg);
        }
        else
        {
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
    private void handleHttpRequest(ChannelHandlerContext ctx, MessageEvent e, HttpRequest req) throws Exception
    {
        if (req.getMethod() == GET)
        {
            // Websocket handshake
            if (req.getUri().equals(WEBSOCKET_PATH))
            {
                handleWebSocketHandShakeRequest(ctx, req);
                return;
            }
        }
        else if (req.getMethod() == POST && req.getUri().equals(PUBLISH_PATH))
        {
            // Get request content
            ChannelBuffer content = req.getContent();
            if (!content.readable())
            {
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
    private String bytesToString(byte[] bytes) throws UnsupportedEncodingException
    {
        if (bytes == null)
        {
            return null;
        }

        return new String(bytes, UTF_8_CHARSET);
    }

    /**
     * <p>
     * Handle the web socket handshake for the older web socket specification <a
     * href="http://tools.ietf.org/html/draft-hixie-thewebsocketprotocol-75">hixie-75</a> and <a
     * href="http://tools.ietf.org/html/draft-hixie-thewebsocketprotocol-76">hixie-76</a>.
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
    private void handleWebSocketHandShakeRequest(ChannelHandlerContext ctx, HttpRequest req)
            throws NoSuchAlgorithmException
    {
        _logger.debug("Channel %s web socket handshake", ctx.getChannel().getId());

        // Serve the WebSocket handshake request.
        if (!Values.UPGRADE.equalsIgnoreCase(req.getHeader(CONNECTION))
                || !WEBSOCKET.equalsIgnoreCase(req.getHeader(Names.UPGRADE)))
        {
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
        if (isHixie76)
        {
            // New handshake method with a challenge:
            res.addHeader(SEC_WEBSOCKET_ORIGIN, req.getHeader(ORIGIN));
            res.addHeader(SEC_WEBSOCKET_LOCATION, getWebSocketLocation(req));
            String protocol = req.getHeader(SEC_WEBSOCKET_PROTOCOL);
            if (protocol != null)
            {
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
        }
        else
        {
            // Old Hixie 75 handshake method with no challenge:
            res.addHeader(WEBSOCKET_ORIGIN, req.getHeader(ORIGIN));
            res.addHeader(WEBSOCKET_LOCATION, getWebSocketLocation(req));
            String protocol = req.getHeader(WEBSOCKET_PROTOCOL);
            if (protocol != null)
            {
                res.addHeader(WEBSOCKET_PROTOCOL, protocol);
            }
        }

        // Upgrade the connection and send the handshake response.
        ChannelPipeline p = ctx.getChannel().getPipeline();
        p.remove("aggregator");
        p.replace("decoder", "wsdecoder", new WebSocketFrameDecoder());

        ctx.getChannel().write(res);

        p.replace("encoder", "wsencoder", new WebSocketFrameEncoder());
        return;
    }

    /**
     * Returns the URL to use for sending and receiving web socket frames
     * 
     * @param req
     *            The current HTTP request
     */
    private String getWebSocketLocation(HttpRequest req)
    {
        return "ws://" + req.getHeader(HttpHeaders.Names.HOST) + WEBSOCKET_PATH;
    }

    /**
     * <p>
     * Figure out if this is a publish or subscribe request and do it.
     * </p>
     * 
     * @param ctx
     * @param frame
     */
    private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame)
    {
        //_logger.debug("Channel %s got frame type %s %s %s.", ctx.getChannel().getId(), frame.getType(), frame.isBinary(), frame.isText());

        // Check for closing frame - according to the standard, we just return the same frame
        if (((frame.getType() & 0xFF) == 0xFF) && frame.isBinary())
        {
            _logger.debug("Channel %s got web socket closing frame. Echoing it back.", ctx.getChannel().getId());
            ctx.getChannel().write(frame);
            return;
        }

        String requestJson = frame.getTextData();
        String responseJson = null;

        if (StringUtils.isBlank(requestJson))
        {
            return;
        }

        _logger.debug("Channel %s Request JSON: %s", ctx.getChannel().getId(), requestJson);

        // Process according to request type
        // We do a quick peek in the json in order to dispatch to the required worker
        String first50Characters = requestJson.length() > 50 ? requestJson.substring(0, 50) : requestJson;
        if (first50Characters.indexOf("\"PublicationRequest\"") > 0)
        {
            PublicationWorker worker = new PublicationWorker(JsonHttpService.getInstance().getMqProducerSessionPool());

            StringBuilder sb = new StringBuilder();
            worker.process(requestJson, sb);
            responseJson = sb.toString();
        }
        else if (first50Characters.indexOf("\"SubscriptionRequest\"") > 0)
        {
            // If existing subscription exists, stop it first
            if (_subscriptionWorker != null)
            {
                _subscriptionWorker.stop();
            }

            _subscriptionWorker = new SubscriptionWorker(ctx.getChannel());

            StringBuilder sb = new StringBuilder();
            _subscriptionWorker.process(requestJson, sb);
            responseJson = sb.toString();
        }
        else
        {
            throw new UnsupportedOperationException("Unsupported request: " + requestJson);
        }

        _logger.debug("Response JSON: %s", responseJson);
        ctx.getChannel().write(new DefaultWebSocketFrame(responseJson));
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
    private void sendHttpResponse(ChannelHandlerContext ctx, HttpRequest req, HttpResponse res)
    {
        // Decide whether to close the connection or not.
        boolean isKeepAlive = isKeepAlive(req);

        // Set content if one is not set
        if (res.getContent() == null || res.getContent().readableBytes() == 0)
        {
            res.setContent(ChannelBuffers.copiedBuffer(res.getStatus().toString(), CharsetUtil.UTF_8));
            setContentLength(res, res.getContent().readableBytes());
        }

        // Add 'Content-Length' header only for a keep-alive connection.
        if (isKeepAlive)
        {
            res.setHeader(CONTENT_LENGTH, res.getContent().readableBytes());
        }

        // Send the response
        ChannelFuture f = ctx.getChannel().write(res);

        // Close the connection if necessary.
        if (!isKeepAlive || res.getStatus().getCode() != 200)
        {
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }

    /**
     * Handle exception by printing it and closing socket
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception
    {
        try
        {
            _logger.error(e.getCause(), "Error handling PubSub JSON HTTP Request");
            e.getChannel().close();
        }
        catch (Exception ex)
        {
            _logger.debug(ex, "Error closing channel in exception");
        }
    }

    /**
     * Add channel to channel group to disconnect when shutting down. Channel group automatically removes closed
     * channels.
     */
    @Override
    public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e)
    {
        JsonHttpService.getInstance().getAllChannels().add(e.getChannel());
    }

    /**
     * If disconnected, stop subscription if one is present
     */
    @Override
    public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception
    {
        _logger.debug("Channel %s disconnected", ctx.getChannel().getId());

        if (_subscriptionWorker != null)
        {
            _subscriptionWorker.stop();
        }

        super.channelDisconnected(ctx, e);
    }
}
