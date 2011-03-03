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
 * Route messages to specialised services based on the request URI.
 * <ul>
 * <li><code>/echo/*</code> - echo service for testing</li>
 * <li><code>/api/*</code> - api service exposes a RESTful interface for ChiliLog management and query</li>
 * <li><code>/workbench/*</code> - workbench service provides a browser based tool for ChiliLog management and query</li>
 * <li><code>/static/*</code> - static file service serves up static files</li>
 * </ul>
 * </p>
 */
public class HttpRequestHandler extends SimpleChannelUpstreamHandler
{
    private static Log4JLogger _logger = Log4JLogger.getLogger(HttpRequestHandler.class);

    private Service _service = null;

    /**
     * Handles incoming messages by routing to services
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
                send404NotFound(e);
                return;
            }

            // Route
            uri = uri.toLowerCase();
            if (uri.startsWith("/api/"))
            {
                _service = new ApiService();
            }
            else if (uri.startsWith("/static/"))
            {
                _service = new StaticFileService();
            }
            else if (uri.startsWith("/echo/"))
            {
                _service = new EchoService();
            }
            else
            {
                send404NotFound(e);
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
     * <p>
     * Upon exception, send an HTTP Response with a status of <code>500 Internal Server Error</code> back to the caller.
     * </p>
     * <p>
     * The error details is returned as text in the response content. For example:
     * </p>
     * 
     * <pre>
     * ERROR: error message
     * 
     * STACK TRACE:  Test Error
     * at com.chililog.server.common.ChiliLogExceptionTest.testWrapping(ChiliLogExceptionTest.java:68)
     * at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
     * at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39)
     * at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)
     * at java.lang.reflect.Method.invoke(Method.java:597)
     * at org.junit.runners.model.FrameworkMethod$1.runReflectiveCall(FrameworkMethod.java:44)
     * </pre>
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception
    {
        boolean _willBeClosed = false;
        try
        {
            Throwable cause = e.getCause();
            HttpResponseStatus responseStatus = HttpResponseStatus.INTERNAL_SERVER_ERROR;

            _logger.error(cause, "ERROR handling request. %1", cause.getMessage());

            HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, responseStatus);

            response.setHeader(CONTENT_TYPE, "text/plain; charset=UTF-8");

            StringBuffer sb = new StringBuffer();
            sb.append("ERROR: " + e.getCause().getMessage() + "\n\n");
            sb.append("STACK TRACE: " + e.getCause().toString());
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
     * Sends an HTTP Response with a status of <code>404 Not Found</code> back to the caller
     * 
     * @param e
     *            Message event that we are processing
     */
    private void send404NotFound(MessageEvent e)
    {
        HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND);
        ChannelFuture future = e.getChannel().write(response);
        future.addListener(ChannelFutureListener.CLOSE);
    }

}
