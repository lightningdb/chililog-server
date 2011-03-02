/*
 * Copyright 2009 Red Hat, Inc. Red Hat licenses this file to you under the Apache License, version 2.0 (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the License at:
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 */

package com.chililog.server.ui;

import static org.jboss.netty.handler.codec.http.HttpHeaders.*;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.*;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.*;
import static org.jboss.netty.handler.codec.http.HttpVersion.*;

import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.reflect.ConstructorUtils;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.Cookie;
import org.jboss.netty.handler.codec.http.CookieDecoder;
import org.jboss.netty.handler.codec.http.CookieEncoder;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpChunk;
import org.jboss.netty.handler.codec.http.HttpChunkTrailer;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;
import org.jboss.netty.util.CharsetUtil;

import com.chililog.server.data.RepositoryController;

/**
 * <p>
 * Dispatches an API request to an API controller.
 * </p>
 * <p>
 * Format of the URL is <code>/api/[Controller]/[Action]/[Identifer]</code> where:
 * <ul>
 * <li>[Controller] - is the name of the API class</li>
 * <li>[Action] - is the name of the method in the controller class</li>
 * <li>[Identifer] - is an optional parameter to identify the record on which the perform the action</li>
 * </ul>
 * </p>
 */
public class ApiService extends BaseService
{
    private HttpRequest _request;

    private boolean _readingChunks = false;

    /** Buffer that stores the response content */
    private final ByteArrayOutputStream _requestContent = new ByteArrayOutputStream();

    /**
     * Process the message
     */
    @Override
    public void processMessage(ChannelHandlerContext ctx, MessageEvent e) throws Exception
    {
//        // Collect HTTP information - whether chunk or not
//        if (!_readingChunks)
//        {
//            HttpRequest request = this._request = (HttpRequest) e.getMessage();
//
//            if (is100ContinueExpected(request))
//            {
//                send100Continue(e);
//            }
//
//            if (request.isChunked())
//            {
//                _readingChunks = true;
//            }
//            else
//            {
//                ChannelBuffer content = request.getContent();
//                if (content.readable())
//                {
//                    _requestContent.w content.array();
//                    buf.append(content.toString(CharsetUtil.UTF_8));
//                }
//                writeResponse(e);
//            }
//        }
//        else
//        {
//            HttpChunk chunk = (HttpChunk) e.getMessage();
//            if (chunk.isLast())
//            {
//                _readingChunks = false;
//                writeResponse(e);
//            }
//            else
//            {
//                buf.append(chunk.getContent().toString(CharsetUtil.UTF_8));
//            }
//        }
    }

    private void instanceController()
    {
    
    }
    
    private void invokeController()
    {
        String uri = _request.getUri();
        String[] params = uri.split("/");
        String controller = params[1];
        String action = params[2];
        String id = params[3];
        
       // Class<?> controllerClass = ClassUtils.getClass(repoInfo.getControllerClassName());
        //ConstructorUtils.invokeConstructor(controllerClass, repoInfo);
    }
    
    /**
     * Write the HTTP response
     * 
     * @param e
     */
    private void writeResponse(MessageEvent e)
    {
        // Decide whether to close the connection or not.
        boolean keepAlive = isKeepAlive(_request);

        // Build the response object.
        HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);
        //response.setContent(ChannelBuffers.copiedBuffer(buf.toString(), CharsetUtil.UTF_8));
        response.setHeader(CONTENT_TYPE, "text/plain; charset=UTF-8");

        if (keepAlive)
        {
            // Add 'Content-Length' header only for a keep-alive connection.
            response.setHeader(CONTENT_LENGTH, response.getContent().readableBytes());
        }

        // Encode the cookie.
        String cookieString = _request.getHeader(COOKIE);
        if (cookieString != null)
        {
            CookieDecoder cookieDecoder = new CookieDecoder();
            Set<Cookie> cookies = cookieDecoder.decode(cookieString);
            if (!cookies.isEmpty())
            {
                // Reset the cookies if necessary.
                CookieEncoder cookieEncoder = new CookieEncoder(true);
                for (Cookie cookie : cookies)
                {
                    cookieEncoder.addCookie(cookie);
                }
                response.addHeader(SET_COOKIE, cookieEncoder.encode());
            }
        }

        // Write the response.
        ChannelFuture future = e.getChannel().write(response);

        // Close the non-keep-alive connection after the write operation is done.
        if (!keepAlive)
        {
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }

    private void send100Continue(MessageEvent e)
    {
        HttpResponse response = new DefaultHttpResponse(HTTP_1_1, CONTINUE);
        e.getChannel().write(response);
    }

}
