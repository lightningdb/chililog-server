/*
 * Copyright 2009 Red Hat, Inc. Red Hat licenses this file to you under the Apache License, version 2.0 (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the License at:
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 */

package com.chililog.server.ui;

import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpChunk;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.handler.codec.http.websocket.WebSocketFrame;
import org.jboss.netty.util.CharsetUtil;

import com.chililog.server.common.Log4JLogger;

/**
 * <p>
 * Routes handling of the request to other specialised handlers
 * </p>
 */
public class HttpRequestHandler extends SimpleChannelUpstreamHandler
{
    static Log4JLogger _logger = Log4JLogger.getLogger(HttpRequestHandler.class);

    private BaseService _service = null;

    /**
     * <p>
     * Route messages to specialised handlers based on the request URI.
     * <ul>
     * <li>/echo/ - echo handler</li>
     * <li>/api/ - api handler</li>
     * <li>/static/ - static file handler</li>
     * </ul>
     * </p>
     * <p>
     * Note that we cannot change the existing pipeline so we have to dispatch it to our own handler.
     * </p>
     * 
     * @param ctx
     *            Context
     * @param e
     *            Message event
     */
    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception
    {
        Object msg = e.getMessage();

        if (msg instanceof HttpRequest)
        {
            // New request so let's figure our the service to call
            HttpRequest request = (HttpRequest) msg;
            String uri = request.getUri();
            if (StringUtils.isBlank(uri))
            {
                writeNotFoundResponse(e);
                return;
            }

            uri = uri.toLowerCase();
            if (uri.startsWith("/echo/"))
            {
                _service = new EchoService();
            }
            else
            {
                writeNotFoundResponse(e);
                return;
            }
        }
        else if (msg instanceof HttpChunk || msg instanceof WebSocketFrame)
        {
            // If this is HTTP chunk or web socket frame, then use existing _service
        }
        else
        {
            throw new NotImplementedException("Message Type " + msg.getClass().getName());
        }

        _service.processMessage(ctx, e);
        return;

    }

    /**
     * Upon exception, send back a 500 - internal server error
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception
    {
        boolean _willBeClosed = false;
        try
        {
            _logger.error(e.getCause(), "ERROR handling request. %1", e.getCause().getMessage());

            HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1,
                    HttpResponseStatus.INTERNAL_SERVER_ERROR);

            response.setHeader(CONTENT_TYPE, "text/plain; charset=UTF-8");

            StringBuffer sb = new StringBuffer();
            sb.append(e.getCause().getMessage() + "\n");
            sb.append(e.getCause().toString());
            response.setContent(ChannelBuffers.copiedBuffer(sb.toString(), CharsetUtil.UTF_8));

            ChannelFuture future = e.getChannel().write(response);
            future.addListener(ChannelFutureListener.CLOSE);
            _willBeClosed = true;
        }
        catch (Exception ex)
        {
            _logger.error(ex, "ERROR while trying to send 500 - Internal Server Error. %1", ex.getMessage());
        }

        // Close the channel
        if (!_willBeClosed)
        {
            e.getChannel().close();
        }
    }

    /**
     * Send back a 404 not found
     * 
     * @param e
     *            Message event that we are processing
     */
    private void writeNotFoundResponse(MessageEvent e)
    {
        HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND);
        ChannelFuture future = e.getChannel().write(response);
        future.addListener(ChannelFutureListener.CLOSE);
    }

}
