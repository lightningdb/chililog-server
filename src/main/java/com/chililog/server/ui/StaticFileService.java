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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.activation.MimetypesFileTypeMap;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelFutureProgressListener;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.DefaultFileRegion;
import org.jboss.netty.channel.FileRegion;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.stream.ChunkedFile;
import org.jboss.netty.util.CharsetUtil;

import com.chililog.server.common.AppProperties;
import com.chililog.server.common.Log4JLogger;

/**
 * <p>
 * Static file service just sends back static files
 * </p>
 * <p>
 * This is copied from the Netty File Server sample
 * </p>
 */
public class StaticFileService extends BaseService
{
    private static Log4JLogger _logger = Log4JLogger.getLogger(StaticFileService.class);

    /**
     * Process the message
     */
    @Override
    public void processMessage(ChannelHandlerContext ctx, MessageEvent e) throws Exception
    {
        HttpRequest request = (HttpRequest) e.getMessage();

        if (request.getMethod() != HttpMethod.GET)
        {
            sendError(ctx, METHOD_NOT_ALLOWED);
            return;
        }

        // Check
        final String filePath = convertUriToPhysicalFilePath(request.getUri());
        if (filePath == null)
        {
            sendError(ctx, FORBIDDEN);
            return;
        }
        File file = new File(filePath);
        if (file.isHidden() || !file.exists())
        {
            sendError(ctx, NOT_FOUND);
            return;
        }
        if (!file.isFile())
        {
            sendError(ctx, FORBIDDEN);
            return;
        }

        // Open file for sending back
        RandomAccessFile raf;
        try
        {
            raf = new RandomAccessFile(file, "r");
        }
        catch (FileNotFoundException fnfe)
        {
            sendError(ctx, NOT_FOUND);
            return;
        }
        long fileLength = raf.length();

        // Turn compression on/off
        boolean doCompression = checkDoCompression(filePath, fileLength);
        ChannelHandler deflater = ctx.getPipeline().get("deflater");
        if (deflater instanceof ConditionalHttpContentCompressor)
        {
            ((ConditionalHttpContentCompressor) deflater).setDoCompression(doCompression);
        }

        // Create the response
        HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);
        setContentLength(response, fileLength);
        response.setHeader(HttpHeaders.Names.CONTENT_TYPE, convertFileExtensionToMimeType(filePath));
        response.setHeader(HttpHeaders.Names.CACHE_CONTROL, "max-age="
                + AppProperties.getInstance().getWebStaticFilesCacheSeconds());

        // Write the content.
        Channel ch = e.getChannel();
        ChannelFuture writeFuture;
        if (doCompression)
        {
            // Cannot use ChunkedFile or zero-copy if we want to do compression
            // Must read file contents and set it as the contents
            byte[] buffer = new byte[(int) fileLength];
            raf.readFully(buffer);
            raf.close();

            response.setContent(ChannelBuffers.copiedBuffer(buffer));
            writeFuture = ch.write(response);
        }
        else if (AppProperties.getInstance().getWebSslEnabled())
        {
            // Cannot use zero-copy with HTTPS

            // Write the initial line and the header.
            ch.write(response);

            // Write chunks
            writeFuture = ch.write(new ChunkedFile(raf, 0, fileLength, 8192));
        }
        else
        {
            // No encryption - use zero-copy.
            // However zero-copy does not seem to work with compression
            // Only use zero-copy for large files like movies and music

            // Write the initial line and the header.
            ch.write(response);

            // Zero-copy
            final FileRegion region = new DefaultFileRegion(raf.getChannel(), 0, fileLength);
            writeFuture = ch.write(region);
            writeFuture.addListener(new ChannelFutureProgressListener()
            {
                public void operationComplete(ChannelFuture future)
                {
                    region.releaseExternalResources();
                }

                public void operationProgressed(ChannelFuture future, long amount, long current, long total)
                {
                    _logger.debug("%s: %d / %d (+%d)%n", filePath, current, total, amount);
                }
            });
        }

        // Decide whether to close the connection or not.
        if (!isKeepAlive(request))
        {
            // Close the connection when the whole content is written out.
            writeFuture.addListener(ChannelFutureListener.CLOSE);
        }
    }

    /**
     * Converts the request URI to a file path
     * 
     * @param uri
     * @return
     * @throws UnsupportedEncodingException
     */
    private String convertUriToPhysicalFilePath(String uri) throws UnsupportedEncodingException
    {
        // Decode the path.
        try
        {
            uri = URLDecoder.decode(uri, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            uri = URLDecoder.decode(uri, "ISO-8859-1");
        }

        // Remove /static prefix
        uri = uri.substring(7);

        // Convert file separators.
        uri = uri.replace('/', File.separatorChar);

        // Simplistic dumb security check.
        // You will have to do something serious in the production environment.
        if (uri.contains(File.separator + ".") || uri.contains("." + File.separator) || uri.startsWith(".")
                || uri.endsWith("."))
        {
            return null;
        }

        // Convert to absolute path.
        return AppProperties.getInstance().getWebStaticFilesDirectory() + File.separator + uri;
    }

    /**
     * Send error to client
     * 
     * @param ctx
     *            Context
     * @param status
     *            HTTP response status
     */
    private void sendError(ChannelHandlerContext ctx, HttpResponseStatus status)
    {
        HttpResponse response = new DefaultHttpResponse(HTTP_1_1, status);
        response.setHeader(CONTENT_TYPE, "text/plain; charset=UTF-8");
        response.setContent(ChannelBuffers.copiedBuffer("Failure: " + status.toString() + "\r\n", CharsetUtil.UTF_8));

        // Close the connection as soon as the error message is sent.
        ctx.getChannel().write(response).addListener(ChannelFutureListener.CLOSE);
    }

    /**
     * Tries to figure out the MIME type of a file based on the file name
     * 
     * @param filePath
     *            Path to file
     * @return MIME type. e.g. "text/html"
     */
    private String convertFileExtensionToMimeType(String filePath)
    {
        MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
        return mimeTypesMap.getContentType(filePath);
    }

    /**
     * <p>
     * Figure out if we should do try to do HTTP compression or not based on the file extension and file size
     * </p>
     * 
     * @param filePath
     *            Path to the file
     * @return true if compression on the file should be performed, false if not
     */
    private boolean checkDoCompression(String filePath, long fileLength)
    {
        // If < 4096 bytes, compression makes the file bigger and/or CPU used vs time saved is small
        // If > 1 MB, don't compress. Just chunk download because it takes too much memory to read everything in
        if (fileLength < 4096 || fileLength > 1048576)
        {
            return false;
        }

        String s = filePath.toLowerCase();
        if (s.endsWith(".html") || s.endsWith(".htm") || s.endsWith(".js") || s.endsWith(".css") || s.endsWith(".txt")
                || s.endsWith(".json") || s.endsWith(".xml"))
        {
            return true;
        }
        return false;
    }
}
