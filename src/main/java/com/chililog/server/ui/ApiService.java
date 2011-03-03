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

import static org.jboss.netty.handler.codec.http.HttpHeaders.*;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.*;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.*;
import static org.jboss.netty.handler.codec.http.HttpVersion.*;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang.reflect.ConstructorUtils;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferOutputStream;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpChunk;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

import com.chililog.server.common.JsonTranslator;
import com.chililog.server.common.Log4JLogger;
import com.chililog.server.ui.api.ErrorAO;
import com.chililog.server.ui.api.Worker;
import com.chililog.server.ui.api.Worker.ContentIOStyle;
import com.chililog.server.ui.api.ApiResult;

/**
 * <p>
 * Dispatches an API request to an API object for it to be processed.
 * </p>
 * <p>
 * The expected format of the URI is <code>/api/[Object]</code> where <code>[Object]</code> is the name of the API
 * object to which the caller wishes to perform an action.
 * </p>
 */
public class ApiService extends Service
{
    private static Log4JLogger _logger = Log4JLogger.getLogger(ApiService.class);
    private HttpRequest _request;
    private Worker _apiProcessor;

    private boolean _readingChunks = false;

    private File _requestContentFile = null;

    /**
     * Buffer that stores chunked response content
     * */
    private OutputStream _requestContentStream = null;

    /**
     * Process the message
     */
    @Override
    public void processMessage(ChannelHandlerContext ctx, MessageEvent e) throws Exception
    {
        try
        {
            ApiResult result;

            // Collect HTTP information - whether chunk or not
            if (!_readingChunks)
            {
                // Initialize
                _request = (HttpRequest) e.getMessage();
                instanceApiProcessor();
                result = _apiProcessor.initialize(_request);
                if (!result.isSuccess())
                {
                    writeResponse(e, result);
                    return;
                }

                // Send 100 continue to tell browser that there is no validation errors, send content now.
                if (is100ContinueExpected(_request))
                {
                    send100Continue(e);
                }

                // Get request content
                if (_request.isChunked())
                {
                    // Read chunks
                    _readingChunks = true;

                    // Setup buffer to store chunk
                    if (_apiProcessor.getRequestContentIOStyle() == ContentIOStyle.ByteArray)
                    {
                        _requestContentStream = new ByteArrayOutputStream();
                    }
                    else if (_apiProcessor.getRequestContentIOStyle() == ContentIOStyle.ByteArray)
                    {
                        File _requestContentFile = File.createTempFile("ApiService_", ".dat");
                        _requestContentStream = new BufferedOutputStream(new FileOutputStream(_requestContentFile));
                    }
                    else
                    {
                        throw new NotImplementedException("ContentIOStyle "
                                + _apiProcessor.getRequestContentIOStyle().toString());
                    }

                    return;
                }
                else
                {
                    // No chunks. Process it.
                    writeResponse(e, invokeApiProcessorNoChunks());
                    return;
                }
            }
            else
            {
                HttpChunk chunk = (HttpChunk) e.getMessage();
                if (chunk.isLast())
                {
                    // No more chunks. Process it.
                    _readingChunks = false;
                    writeResponse(e, invokeApiProcessorWithChunks());
                    return;
                }
                else
                {
                    _requestContentStream.write(chunk.getContent().array());
                    return;
                }
            }

            // Don't code here. Unreachable code
        }
        catch (Exception ex)
        {
            writeResponse(e, ex);
        }
        finally
        {
            cleanup();
        }
    }

    /**
     * <p>
     * Instance our API class using the name passed in on the URI.
     * </p>
     * <p>
     * If <code>/api/session</code> is passed in, the class <code>com.chililog.server.ui.api.Session</code> will be
     * instanced.
     * </p>
     */
    private void instanceApiProcessor() throws Exception
    {
        String uri = _request.getUri();
        String[] segments = uri.split("/");
        String apiName = segments[1];

        apiName = WordUtils.capitalizeFully(apiName, new char[]
        { '_' });

        String className = "com.chililog.server.ui.api." + apiName;
        Class<?> apiClass = ClassUtils.getClass(className);
        _apiProcessor = (Worker) ConstructorUtils.invokeConstructor(apiClass, null);
    }

    /**
     * 
     * @return
     * @throws Exception
     */
    private ApiResult invokeApiProcessorNoChunks() throws Exception
    {
        byte[] requestContent = null;
        ChannelBuffer content = _request.getContent();
        if (content.readable())
        {
            requestContent = content.array();
        }

        if (_apiProcessor.getRequestContentIOStyle() == ContentIOStyle.ByteArray)
        {
            return _apiProcessor.process(requestContent);
        }
        else if (_apiProcessor.getRequestContentIOStyle() == ContentIOStyle.File)
        {
            File _requestContentFile = File.createTempFile("ApiService_", ".dat");
            _requestContentStream = new FileOutputStream(_requestContentFile);
            _requestContentStream.write(requestContent);
            _requestContentStream.close();

            return _apiProcessor.process(_requestContentFile);
        }
        else
        {
            throw new NotImplementedException("ContentIOStyle " + _apiProcessor.getRequestContentIOStyle().toString());
        }
    }

    /**
     * 
     * @return
     * @throws IOException
     */
    private ApiResult invokeApiProcessorWithChunks() throws IOException
    {
        if (_apiProcessor.getRequestContentIOStyle() == ContentIOStyle.ByteArray)
        {
            byte[] requestContent = ((ByteArrayOutputStream) _requestContentStream).toByteArray();
            return _apiProcessor.process(requestContent);
        }
        else if (_apiProcessor.getRequestContentIOStyle() == ContentIOStyle.File)
        {
            _requestContentStream.close();
            return _apiProcessor.process(_requestContentFile);
        }
        else
        {
            throw new NotImplementedException("ContentIOStyle " + _apiProcessor.getRequestContentIOStyle().toString());
        }
    }

    /**
     * Write the HTTP response
     * 
     * @param e
     *            Message event
     * @param result
     *            {@link ApiResult} API processing result
     */
    private void writeResponse(MessageEvent e, ApiResult result)
    {
        // Decide whether to close the connection or not.
        boolean keepAlive = isKeepAlive(_request);

        // Build the response object.
        HttpResponse response = new DefaultHttpResponse(HTTP_1_1, result.getResponseStatus());
        response.setHeader(CONTENT_TYPE, result.getResponseContentType());

        if (result.getResponseContentIOStyle() == ContentIOStyle.ByteArray)
        {
            response.setContent(ChannelBuffers.copiedBuffer((byte[]) result.getResponseContent()));
        }
        else
        {
            throw new NotImplementedException("ContentIOStyle " + result.getResponseContentIOStyle().toString());
        }

        if (keepAlive)
        {
            // Add 'Content-Length' header only for a keep-alive connection.
            response.setHeader(CONTENT_LENGTH, response.getContent().readableBytes());
        }

        // Write the response.
        ChannelFuture future = e.getChannel().write(response);

        // Close the non-keep-alive connection after the write operation is done.
        if (!keepAlive)
        {
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }

    /**
     * Write response for when there is an exception
     * 
     * @param e
     *            Message event
     * @param ex
     *            Exception that was thrown
     */
    private void writeResponse(MessageEvent e, Exception ex) throws Exception
    {
        // Decide whether to close the connection or not.
        boolean keepAlive = isKeepAlive(_request);

        // Build the response object.
        HttpResponse response = new DefaultHttpResponse(HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR);
        response.setHeader(CONTENT_TYPE, "text/json; charset=UTF-8");

        ErrorAO errorAO = new ErrorAO(ex);
        
        ChannelBuffer buffer = ChannelBuffers.dynamicBuffer(4096);
        ChannelBufferOutputStream os = new ChannelBufferOutputStream(buffer);
        PrintStream ps = new PrintStream(os, true, "UTF-8");
        JsonTranslator.getInstance().toJson(errorAO, ps);
        ps.close();
        os.close();       
        
        response.setContent(buffer);
        
        if (keepAlive)
        {
            // Add 'Content-Length' header only for a keep-alive connection.
            response.setHeader(CONTENT_LENGTH, response.getContent().readableBytes());
        }

        // Write the response.
        ChannelFuture future = e.getChannel().write(response);

        // Close the non-keep-alive connection after the write operation is done.
        if (!keepAlive)
        {
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }

    /**
     * Cleanup temp files, etc.
     */
    private void cleanup()
    {
        try
        {
            if (_requestContentFile != null && _requestContentFile.exists())
            {
                _requestContentFile.delete();
            }
        }
        catch (Exception ex)
        {
            _logger.error(ex, "Error cleaning up.");
        }
    }

    /**
     * 100 Continue tells the browser to start sending the content
     * 
     * @param e
     */
    private void send100Continue(MessageEvent e)
    {
        HttpResponse response = new DefaultHttpResponse(HTTP_1_1, CONTINUE);
        e.getChannel().write(response);
    }

}
