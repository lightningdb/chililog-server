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

package com.chililog.server.management;

import static org.jboss.netty.handler.codec.http.HttpHeaders.*;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.*;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.*;
import static org.jboss.netty.handler.codec.http.HttpVersion.*;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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

/**
 * <p>
 * The Echo Service sends back what ever is sent by the caller. This service is intended to be used for testing.
 * </p>
 * <p>
 * This code is based on the Netty HTTP Snoop sample (http://www.jboss.org/netty/documentation.html).
 * </p>
 */
public class EchoService extends Service
{
    private HttpRequest _request;

    private boolean _readingChunks = false;

    /** Buffer that stores the response content */
    private final StringBuilder buf = new StringBuilder();

    /**
     * Process the message
     */
    @Override
    public void processMessage(ChannelHandlerContext ctx, MessageEvent e) throws Exception
    {
        // First time called, _readingChunks will be false
        if (!_readingChunks)
        {
            HttpRequest request = this._request = (HttpRequest) e.getMessage();

            if (is100ContinueExpected(request))
            {
                send100Continue(e);
            }

            buf.setLength(0);
            buf.append("WELCOME TO THE CHILILOG WEB SERVER\r\n");
            buf.append("==================================\r\n");

            buf.append("SERVER TIME: " + new Date().toString() + "\r\n");
            buf.append("HTTP VERSION: " + request.getProtocolVersion() + "\r\n");
            buf.append("HOSTNAME: " + getHost(request, "unknown") + "\r\n");
            buf.append("REQUEST_URI: " + request.getUri() + "\r\n\r\n");

            for (Map.Entry<String, String> h : request.getHeaders())
            {
                buf.append("HEADER: " + h.getKey() + " = " + h.getValue() + "\r\n");
            }
            buf.append("\r\n");

            QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.getUri());
            Map<String, List<String>> params = queryStringDecoder.getParameters();
            if (!params.isEmpty())
            {
                for (Entry<String, List<String>> p : params.entrySet())
                {
                    String key = p.getKey();
                    List<String> vals = p.getValue();
                    for (String val : vals)
                    {
                        buf.append("PARAM: " + key + " = " + val + "\r\n");
                    }
                }
                buf.append("\r\n");
            }

            if (request.isChunked())
            {
                _readingChunks = true;
            }
            else
            {
                ChannelBuffer content = request.getContent();
                if (content.readable())
                {
                    buf.append("CONTENT: " + content.toString(CharsetUtil.UTF_8) + "\r\n");
                }
                writeResponse(e);
            }
        }
        else
        {
            HttpChunk chunk = (HttpChunk) e.getMessage();
            if (chunk.isLast())
            {
                _readingChunks = false;
                buf.append("END OF CONTENT\r\n");

                HttpChunkTrailer trailer = (HttpChunkTrailer) chunk;
                if (!trailer.getHeaderNames().isEmpty())
                {
                    buf.append("\r\n");
                    for (String name : trailer.getHeaderNames())
                    {
                        for (String value : trailer.getHeaders(name))
                        {
                            buf.append("TRAILING HEADER: " + name + " = " + value + "\r\n");
                        }
                    }
                    buf.append("\r\n");
                }

                writeResponse(e);
            }
            else
            {
                buf.append("CHUNK: " + chunk.getContent().toString(CharsetUtil.UTF_8) + "\r\n");
            }
        }
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
        response.setContent(ChannelBuffers.copiedBuffer(buf.toString(), CharsetUtil.UTF_8));
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
