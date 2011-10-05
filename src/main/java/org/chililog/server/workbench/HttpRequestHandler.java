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

package org.chililog.server.workbench;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.chililog.server.common.Log4JLogger;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpChunk;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;

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
 * <p>
 * If a route cannot be found, 404 Not Found is returned.
 * </p>
 * <p>
 * If there an uncaught exception during processing, a 500 Internal Server Error is returned. The content of the
 * response is set to the error message. The content type is "text/plain".
 * 
 * </p>
 */
public class HttpRequestHandler extends SimpleChannelUpstreamHandler {

    private static Log4JLogger _logger = Log4JLogger.getLogger(HttpRequestHandler.class);

    private WorkbenchRequestHandler _workbenchRequestHandler = null;

    /**
     * Constructor
     */
    public HttpRequestHandler() {
        super();
    }

    /**
     * Handles incoming messages by routing to services
     */
    @Override
    public void messageReceived(final ChannelHandlerContext ctx, final MessageEvent e) throws Exception {
        Object msg = e.getMessage();

        if (msg instanceof HttpRequest) {
            // New request so let's figure our the service to call
            HttpRequest request = (HttpRequest) msg;
            _logger.debug("%s %s CHANNEL=%s", request.getMethod(), request.getUri(), e.getChannel().getId());

            String uri = request.getUri();
            if (StringUtils.isBlank(uri)) {
                send404NotFound(e);
                return;
            }

            // Route
            // Could have used reflection but since we have so few routes, it is quicker to hard code
            uri = uri.toLowerCase();
            if (uri.startsWith("/api/")) {
                _workbenchRequestHandler = new ApiRequestHandler();
            } else if (uri.equalsIgnoreCase("/workbench") || uri.equalsIgnoreCase("/workbench/")) {
                redirectToWorkBenchIndexHtml(e);
                return;
            } else if (uri.startsWith("/static") || uri.startsWith("/workbench")) {
                _workbenchRequestHandler = new StaticFileRequestHandler();
            } else if (uri.startsWith("/echo")) {
                _workbenchRequestHandler = new EchoRequestHandler();
            } else {
                send404NotFound(e);
                return;
            }
        } else if (msg instanceof HttpChunk) {
            // If this is HTTP chunk or web socket frame, then use existing _workbenchRequestHandler
        } else {
            throw new NotImplementedException("Message Type " + msg.getClass().getName());
        }

        _workbenchRequestHandler.processMessage(ctx, e);

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
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        try {
            _logger.debug(e.getCause(), "ERROR: Unhandled exception: " + e.getCause().getMessage()
                    + ". Closing channel " + ctx.getChannel().getId());
            e.getChannel().close();
        } catch (Exception ex) {
            _logger.debug(ex, "ERROR trying to close socket because we got an unhandled exception");
        }
    }

    /**
     * Sends an HTTP Response with a status of <code>404 Not Found</code> back to the caller
     * 
     * @param e
     *            Message event that we are processing
     */
    private void send404NotFound(MessageEvent e) {
        HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND);
        ChannelFuture future = e.getChannel().write(response);
        future.addListener(ChannelFutureListener.CLOSE);
    }

    /**
     * <p>
     * The /workbench URI path is a virtual path. We redirect to our Sproutcore UI HTML page.
     * </p>
     * <p>
     * We have to find the index.html file and redirect to it.
     * </p>
     * 
     * @param e
     */
    private void redirectToWorkBenchIndexHtml(MessageEvent e) {
        String indexHTML = "/workbench/index.html";

        HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.FOUND);
        response.addHeader("Location", indexHTML);
        e.getChannel().write(response);
        e.getChannel().close(); // future.addListener(ChannelFutureListener.CLOSE);

    }

    /**
     * Add channel to channel group to disconnect when shutting down Channel group automatically removes closed
     * channels.
     */
    @Override
    public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) {
        WorkbenchService.getInstance().getAllChannels().add(e.getChannel());
    }

}
