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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang.reflect.ConstructorUtils;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferOutputStream;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpChunk;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

import com.chililog.server.common.ChiliLogException;
import com.chililog.server.common.JsonTranslator;
import com.chililog.server.common.Log4JLogger;
import com.chililog.server.ui.api.ErrorAO;
import com.chililog.server.ui.api.AuthenticationWorker;
import com.chililog.server.ui.api.Worker;
import com.chililog.server.ui.api.Worker.ContentIOStyle;
import com.chililog.server.ui.api.ApiResult;

/**
 * <p>
 * Routes the request to an API worker for processing
 * </p>
 * <p>
 * The expected format of the URI is <code>/api/{WorkerName}</code> where <code>{WorkerName}</code> is the name of the
 * API worker class to invoke.
 * </p>
 * <p>
 * For example, <code>/api/Authentication</code> will invoke the {@link AuthenticationWorker} worker.
 * </p>
 * <p>
 * If processing is successful, a response status of <code>200 OK</code> is returned.
 * </p>
 * <p>
 * Common errors include:
 * <ul>
 * <li><code>404 Not Found</code> - if the requested authentication work is not found</li>
 * <li><code>405 Method Not Allowed</code> - if the requested HTTP method is not supported</li>
 * <li><code>500 Internal Server Error</code> - if there is an unexpected exception caught during processing</li>
 * </p>
 * <p>
 * The following is an example of the error description in JSON format.
 * </p>
 * 
 * <pre>
 * {
 *    "Message": "Cannot find API class 'com.chililog.server.ui.api.Notfound' for URI: '/api/notfound.'",
 *    "StackTrace": "com.chililog.server.common.ChiliLogException: Cannot find ..."
 * }
 * </pre>
 */
public class ApiService extends Service
{
    private static Log4JLogger _logger = Log4JLogger.getLogger(ApiService.class);

    /**
     * Http request
     */
    private HttpRequest _request;

    /**
     * API work to call to process request
     */
    private Worker _apiWorker;

    /**
     * Flag to indicate if we ware processing incoming HTTP chunks
     */
    private boolean _readingChunks = false;

    /**
     * File used for storing chunked input
     */
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
                result = instanceApiWorker();
                if (!result.isSuccess())
                {
                    writeResponse(ctx, e, result);
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
                    if (_apiWorker.getRequestContentIOStyle() == ContentIOStyle.ByteArray)
                    {
                        // Store in memory
                        _requestContentStream = new ByteArrayOutputStream();
                    }
                    else if (_apiWorker.getRequestContentIOStyle() == ContentIOStyle.File)
                    {
                        // Store as file on the file system
                        File _requestContentFile = File.createTempFile("ApiService_", ".dat");
                        _requestContentStream = new BufferedOutputStream(new FileOutputStream(_requestContentFile));
                    }
                    else
                    {
                        throw new UnsupportedOperationException("ContentIOStyle "
                                + _apiWorker.getRequestContentIOStyle().toString());
                    }

                    return;
                }
                else
                {
                    // No chunks. Process it.
                    writeResponse(ctx, e, invokeApiWorker(false));
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
                    writeResponse(ctx, e, invokeApiWorker(true));
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
            writeResponse(ctx, e, ex);
        }
        finally
        {
            cleanup();
        }
    }

    /**
     * <p>
     * Instance our API worker class using the name passed in on the URI.
     * </p>
     * <p>
     * If <code>/api/Authentication</code> is passed in, the class
     * <code>com.chililog.server.ui.api.AuthenticationWorker</code> will be instanced.
     * </p>
     */
    private ApiResult instanceApiWorker() throws Exception
    {
        String className = null;
        try
        {
            String uri = _request.getUri();
            String[] segments = uri.split("/");
            String apiName = segments[2];

            apiName = WordUtils.capitalizeFully(apiName, new char[]
            { '_' });

            className = "com.chililog.server.ui.api." + apiName + "Worker";
            _logger.debug("Instancing ApiWorker: %s", className);

            Class<?> apiClass = ClassUtils.getClass(className);
            _apiWorker = (Worker) ConstructorUtils.invokeConstructor(apiClass, _request);

            return _apiWorker.validate();
        }
        catch (ClassNotFoundException ex)
        {
            return new ApiResult(HttpResponseStatus.NOT_FOUND, new ChiliLogException(ex, Strings.API_NOT_FOUND_ERROR,
                    className, _request.getUri()));
        }
    }

    /**
     * Invoke an API worker object to process the request
     * 
     * @param isChunked
     *            Flag indicating if this HTTP request is chunked or not
     * @return {@link ApiResult} indicating the success or failure of the operation
     * @throws IOException
     */
    private ApiResult invokeApiWorker(boolean isChunked) throws Exception
    {
        Object requestContent = null;
        ContentIOStyle requestContentIOStyle = _apiWorker.getRequestContentIOStyle();

        if (isChunked)
        {
            // Chunked so requeste data is stored in streams
            if (requestContentIOStyle == ContentIOStyle.ByteArray)
            {
                // byte[]
                requestContent = ((ByteArrayOutputStream) _requestContentStream).toByteArray();
            }
            else if (requestContentIOStyle == ContentIOStyle.File)
            {
                // File
                _requestContentStream.close();
                requestContent = _requestContentFile;
            }
            else
            {
                throw new UnsupportedOperationException("ContentIOStyle " + requestContentIOStyle.toString());
            }
        }
        else
        {
            // Not chunked so our request data is stored in the request content
            ChannelBuffer content = _request.getContent();
            if (content.readable())
            {
                if (requestContentIOStyle == ContentIOStyle.ByteArray)
                {
                    // byte[]
                    requestContent = content.array();
                }
                else if (requestContentIOStyle == ContentIOStyle.File)
                {
                    // File
                    _requestContentFile = File.createTempFile("ApiService_", ".dat");
                    _requestContentStream = new FileOutputStream(_requestContentFile);
                    _requestContentStream.write(content.array());
                    _requestContentStream.close();

                    requestContent = _requestContentFile;
                }
                else
                {
                    throw new UnsupportedOperationException("ContentIOStyle " + requestContentIOStyle.toString());
                }
            }
        }

        // If debugging, we want to output our request
        if (_logger.isDebugEnabled())
        {
            logHttpRequest(requestContent);
        }

        // Dispatch
        HttpMethod requestMethod = _request.getMethod();
        if (requestMethod == HttpMethod.GET)
        {
            return _apiWorker.processGet();
        }
        else if (requestMethod == HttpMethod.DELETE)
        {
            return _apiWorker.processDelete();
        }
        else if (requestMethod == HttpMethod.POST)
        {
            return _apiWorker.processPost(requestContent);
        }
        else if (requestMethod == HttpMethod.PUT)
        {
            return _apiWorker.processPut(requestContent);
        }
        else
        {
            throw new UnsupportedOperationException("HTTP method " + requestMethod.toString() + " not supproted.");
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
    private void writeResponse(ChannelHandlerContext ctx, MessageEvent e, ApiResult result)
    {
        // Decide whether to close the connection or not.
        boolean keepAlive = isKeepAlive(_request);

        // Build the response object.
        HttpResponse response = new DefaultHttpResponse(HTTP_1_1, result.getResponseStatus());

        // Headers
        setDateHeader(response);
        for (Entry<String, String> header : result.getHeaders().entrySet())
        {
            response.setHeader(header.getKey(), header.getValue());
        }

        // Content
        if (result.getResponseContent() != null)
        {
            response.setHeader(CONTENT_TYPE, result.getResponseContentType());
            if (result.getResponseContentIOStyle() == ContentIOStyle.ByteArray)
            {
                byte[] content = (byte[]) result.getResponseContent();
                toogleCompression(ctx, content.length > 4096); // Compress if > 4K
                response.setContent(ChannelBuffers.copiedBuffer(content));
            }
            else
            {
                throw new NotImplementedException("ContentIOStyle " + result.getResponseContentIOStyle().toString());
            }
        }

        // If debugging, we want to output our response
        if (_logger.isDebugEnabled())
        {
            logHttpResponse(response, result.getResponseContent());
        }

        // Add 'Content-Length' header only for a keep-alive connection.
        if (keepAlive)
        {
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
    private void writeResponse(ChannelHandlerContext ctx, MessageEvent e, Exception ex) throws Exception
    {
        // Decide whether to close the connection or not.
        boolean keepAlive = isKeepAlive(_request);

        // No need to compress errors. Will be small in size
        toogleCompression(ctx, false);

        // Build the response object.
        HttpResponse response = new DefaultHttpResponse(HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR);
        setDateHeader(response);
        response.setHeader(CONTENT_TYPE, Worker.JSON_CONTENT_TYPE);

        ErrorAO errorAO = new ErrorAO(ex);

        ChannelBuffer buffer = ChannelBuffers.dynamicBuffer(4096);
        ChannelBufferOutputStream os = new ChannelBufferOutputStream(buffer);
        PrintStream ps = new PrintStream(os, true, Worker.JSON_CHARSET);
        JsonTranslator.getInstance().toJson(errorAO, ps);
        ps.close();
        os.close();

        response.setContent(buffer);

        // If debugging, we want to output our response
        if (_logger.isDebugEnabled())
        {
            logHttpResponse(response, response.getContent().array());
        }

        // Add 'Content-Length' header only for a keep-alive connection.
        if (keepAlive)
        {
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

    /**
     * Sets the Date header for the HTTP response
     * 
     * @param response
     *            HTTP response
     * @param file
     *            file to extract content type
     */
    private void setDateHeader(HttpResponse response)
    {
        SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT);
        dateFormatter.setTimeZone(TimeZone.getTimeZone(HTTP_DATE_GMT_TIMEZONE));

        Calendar time = new GregorianCalendar();
        response.setHeader(HttpHeaders.Names.DATE, dateFormatter.format(time.getTime()));
    }

    /**
     * Turn on/off compression
     * 
     * @param ctx
     *            context
     * @param doCompression
     *            True to turn compression on, False to turn it off
     */
    private void toogleCompression(ChannelHandlerContext ctx, boolean doCompression)
    {
        ChannelHandler deflater = ctx.getPipeline().get("deflater");
        if (deflater instanceof ConditionalHttpContentCompressor)
        {
            ((ConditionalHttpContentCompressor) deflater).setDoCompression(doCompression);
        }
    }

    /**
     * Write a log entry about the request
     * 
     * @param requestContent
     *            HTTP request body
     */
    private void logHttpRequest(Object requestContent)
    {
        try
        {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("HTTP Request %s '%s'", _request.getMethod().toString(), _request.getUri()));

            for (Map.Entry<String, String> h : _request.getHeaders())
            {
                sb.append("\r\nHEADER: " + h.getKey() + " = " + h.getValue());
            }

            if (requestContent != null)
            {
                if (requestContent instanceof byte[])
                {
                    sb.append("\r\nCONTENT: " + new String((byte[]) requestContent, "UTF-8"));
                }
                else if (requestContent instanceof File)
                {
                    sb.append("\r\nCONTENT: stored in file");
                }
            }

            _logger.debug(sb.toString());
        }
        catch (Throwable ex)
        {
            // Ignore
            ex.toString();
        }
    }

    /**
     * Log the HTTP response
     * 
     * @param response
     *            HTTP response
     * @param responseContent
     *            HTTP response content
     */
    private void logHttpResponse(HttpResponse response, Object responseContent)
    {
        try
        {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("HTTP Response to %s '%s'. %s", _request.getMethod().toString(), _request.getUri(),
                    response.getStatus().toString()));

            String contentType = StringUtils.EMPTY;
            for (Map.Entry<String, String> h : response.getHeaders())
            {
                sb.append("\r\nHEADER: " + h.getKey() + " = " + h.getValue());
                if (h.getKey().equals(HttpHeaders.Names.CONTENT_TYPE))
                {
                    contentType = h.getValue();
                }
            }

            if (responseContent != null)
            {
                if (responseContent instanceof byte[] && contentType.contains("UTF-8"))
                {
                    sb.append("\r\nCONTENT: " + new String((byte[]) responseContent, "UTF-8"));
                }
                else if (responseContent instanceof File)
                {
                    sb.append("\r\nCONTENT: stored in file");
                }
            }

            _logger.debug(sb.toString());
        }
        catch (Throwable ex)
        {
            // Ignore
            ex.toString();
        }
    }

}
