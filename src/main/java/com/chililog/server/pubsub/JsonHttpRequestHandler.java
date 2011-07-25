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

import static org.jboss.netty.handler.codec.http.HttpHeaders.*;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.*;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Values.*;
import static org.jboss.netty.handler.codec.http.HttpMethod.*;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.*;
import static org.jboss.netty.handler.codec.http.HttpVersion.*;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
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
import org.jboss.netty.handler.codec.http.websocket.WebSocketFrame;
import org.jboss.netty.handler.codec.http.websocket.WebSocketFrameDecoder;
import org.jboss.netty.handler.codec.http.websocket.WebSocketFrameEncoder;
import org.jboss.netty.util.CharsetUtil;

import com.chililog.server.common.Log4JLogger;

/**
 * Handler for JSON log entries send over HTTP request and web socket.
 * 
 * @author vibul
 */
public class JsonHttpRequestHandler extends SimpleChannelUpstreamHandler
{
    private static Log4JLogger _logger = Log4JLogger.getLogger(JsonHttpRequestHandler.class);

    private static final String PUBLISH_PATH = "/publish";

    private static final String WEBSOCKET_HYBI_00_PATH = "/websocket-hybi-00";

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
            if (req.getUri().equals(WEBSOCKET_HYBI_00_PATH))
            {
                handleHttpHybi00HandShakeRequest(ctx, req);
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
            
            return;
        }

        // Only GET and POST are permitted
        sendHttpResponse(ctx, req, new DefaultHttpResponse(HTTP_1_1, FORBIDDEN));
        return;
    }


    /**
     * <p>
     * Handle the web socket handshake for web socket specification <a
     * href="http://en.wikipedia.org/wiki/WebSockets">hybi-00</a>.
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
    private void handleHttpHybi00HandShakeRequest(ChannelHandlerContext ctx, HttpRequest req)
            throws NoSuchAlgorithmException
    {
        // Serve the WebSocket handshake request.
        if (Values.UPGRADE.equalsIgnoreCase(req.getHeader(CONNECTION))
                && WEBSOCKET.equalsIgnoreCase(req.getHeader(Names.UPGRADE)))
        {

            // Create the WebSocket handshake response.
            HttpResponse res = new DefaultHttpResponse(HTTP_1_1, new HttpResponseStatus(101,
                    "Web Socket Protocol Handshake"));
            res.addHeader(Names.UPGRADE, WEBSOCKET);
            res.addHeader(CONNECTION, Values.UPGRADE);

            // Fill in the headers and contents depending on handshake method.
            if (req.containsHeader(SEC_WEBSOCKET_KEY1) && req.containsHeader(SEC_WEBSOCKET_KEY2))
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
                ChannelBuffer output = ChannelBuffers.wrappedBuffer(MessageDigest.getInstance("MD5").digest(
                        input.array()));
                res.setContent(output);
            }
            else
            {
                // Old handshake method with no challenge:
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
    }

    /**
     * Returns the URL to use for sending and receiving web socket frames
     * 
     * @param req
     *            The current HTTP request
     */
    private String getWebSocketLocation(HttpRequest req)
    {
        return "ws://" + req.getHeader(HttpHeaders.Names.HOST) + WEBSOCKET_HYBI_00_PATH;
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
        // Send the uppercased string back.
        // ctx.getChannel().write(new DefaultWebSocketFrame(frame.getTextData().toUpperCase()));
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
        // Generate an error page if response status code is not OK (200).
        if (res.getStatus().getCode() != 200)
        {
            res.setContent(ChannelBuffers.copiedBuffer(res.getStatus().toString(), CharsetUtil.UTF_8));
            setContentLength(res, res.getContent().readableBytes());
        }

        // Send the response and close the connection if necessary.
        ChannelFuture f = ctx.getChannel().write(res);
        if (!isKeepAlive(req) || res.getStatus().getCode() != 200)
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
        _logger.error(e.getCause(), "Error handling PubSub JSON HTTP Request");
        e.getChannel().close();
    }
    
    
    

}
