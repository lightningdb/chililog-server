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

import static org.jboss.netty.handler.codec.http.HttpHeaders.*;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.*;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.*;
import static org.jboss.netty.handler.codec.http.HttpVersion.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import javax.activation.MimetypesFileTypeMap;

import org.apache.commons.lang.StringUtils;
import org.chililog.server.common.AppProperties;
import org.chililog.server.common.Log4JLogger;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.stream.ChunkedFile;
import org.jboss.netty.util.CharsetUtil;

/**
 * <p>
 * Static file service serves static files stored on the file system.
 * </p>
 * <p>
 * The root directory under which to search for file is specified by the <code>web.static_files.directory</code> in the
 * <code>app.properties</code> file.
 * </p>
 * <p>
 * The number of seconds that browsers are expected to cache all files is specified by the
 * <code>web.static_files.cache_seconds</code> in the <code>app.properties</code> file. This is how caching works
 * </p>
 * 
 * <pre>
 * Request #1 Headers
 * ===================
 * GET /static/file1.txt HTTP/1.1
 * 
 * Response #1 Headers
 * ===================
 * HTTP/1.1 200 OK
 * Date:               Tue, 01 Mar 2011 22:44:26 GMT
 * Last-Modified:      Wed, 30 Jun 2010 21:36:48 GMT
 * Expires:            Tue, 01 Mar 2012 22:44:26 GMT
 * Cache-Control:      private, max-age=31536000
 * 
 * Request #2 Headers
 * ===================
 * GET /static/file1.txt HTTP/1.1
 * If-Modified-Since:  Wed, 30 Jun 2010 21:36:48 GMT
 * 
 * Response #2 Headers
 * ===================
 * HTTP/1.1 304 Not Modified
 * Date:               Tue, 01 Mar 2011 22:44:28 GMT
 * 
 * </pre>
 * 
 * <p>
 * Compression is turned off for all files except those that:
 * <ul>
 * <li>have an extension of ".html", ".txt", ".json", ".js", ".xml" or ".css", and</li>
 * <li>are between 4K and 1MB in size.</li>
 * </ul>
 * File extension restrictions are put in as these types of text files are the most common and compress well. We don't
 * want to compress files that are too small because the result can be bigger than the original. Also, we don't want to
 * compress files that are too big because it can waste CPU.
 * </p>
 * <p>
 * This code is based on the Netty HTTP File Server sample (http://www.jboss.org/netty/documentation.html).
 * </p>
 */
public class StaticFileRequestHandler extends WorkbenchRequestHandler {

    private static Log4JLogger _logger = Log4JLogger.getLogger(StaticFileRequestHandler.class);

    /**
     * Process the message
     */
    @Override
    public void processMessage(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        HttpRequest request = (HttpRequest) e.getMessage();

        // We don't handle 100 Continue because we only allow GET method.
        if (request.getMethod() != HttpMethod.GET) {
            sendError(ctx, e, METHOD_NOT_ALLOWED, null);
            return;
        }

        // Check
        final String filePath = convertUriToPhysicalFilePath(request.getUri());
        if (filePath == null) {
            sendError(ctx, e, FORBIDDEN, null);
            return;
        }
        File file = new File(filePath);
        if (file.isHidden() || !file.exists()) {
            sendError(ctx, e, NOT_FOUND, String.format("%s not exist", file.getCanonicalPath()));
            return;
        }
        if (!file.isFile()) {
            sendError(ctx, e, FORBIDDEN, String.format("%s not a file", file.getCanonicalPath()));
            return;
        }

        // Cache Validation
        String ifModifiedSince = request.getHeader(HttpHeaders.Names.IF_MODIFIED_SINCE);
        if (!StringUtils.isBlank(ifModifiedSince)) {
            SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
            Date ifModifiedSinceDate = dateFormatter.parse(ifModifiedSince);
            if (ifModifiedSinceDate.getTime() == file.lastModified()) {
                sendNotModified(ctx, e);
                return;
            }
        }

        // Open file for sending back
        RandomAccessFile raf;
        try {
            raf = new RandomAccessFile(file, "r");
        } catch (FileNotFoundException fnfe) {
            sendError(ctx, e, NOT_FOUND, null);
            return;
        }
        long fileLength = raf.length();

        // Log
        writeLogEntry(e, OK, null);

        // Create the response
        HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);
        setContentLength(response, fileLength);
        setContentTypeHeader(response, file);
        setDateAndCacheHeaders(response, file);

        // Write the content.
        Channel ch = e.getChannel();
        ChannelFuture writeFuture;
        if (AppProperties.getInstance().getWorkbenchSslEnabled()) {
            // Cannot use zero-copy with HTTPS

            // Write the initial line and the header.
            ch.write(response);

            // Write chunks
            writeFuture = ch.write(new ChunkedFile(raf, 0, fileLength, 8192));
        } else {
            // Now that we are using Execution Handlers, we cannot do zero-copy.
            // Do as per with compression (which is what most browser will ask for)
            byte[] buffer = new byte[(int) fileLength];
            raf.readFully(buffer);
            raf.close();

            response.setContent(ChannelBuffers.copiedBuffer(buffer));
            writeFuture = ch.write(response);

            /*
             * // No encryption - use zero-copy. // However zero-copy does not seem to work with compression // Only use
             * zero-copy for large files like movies and music // Write the initial line and the header.
             * ch.write(response); // Zero-copy final FileRegion region = new DefaultFileRegion(raf.getChannel(), 0,
             * fileLength); writeFuture = ch.write(region); writeFuture.addListener(new ChannelFutureProgressListener()
             * { public void operationComplete(ChannelFuture future) { region.releaseExternalResources(); } public void
             * operationProgressed(ChannelFuture future, long amount, long current, long total) {
             * _logger.debug("Zero-Coping file %s: %d / %d (+%d) bytes", filePath, current, total, amount); } });
             */
        }

        // Decide whether to close the connection or not.
        if (!isKeepAlive(request)) {
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
    private String convertUriToPhysicalFilePath(String uri) throws UnsupportedEncodingException {
        // Decode the path.
        try {
            uri = URLDecoder.decode(uri, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            uri = URLDecoder.decode(uri, "ISO-8859-1");
        }

        // Remove the initial /static/ or /workbench/ prefix
        int idx = uri.indexOf('/', 1);
        if (idx < 0) {
            return null;
        }
        uri = uri.substring(idx);
        if (StringUtils.isBlank(uri)) {
            return null;
        }

        // Remove query string if any
        idx = uri.indexOf('?');
        if (idx > 0) {
            uri = uri.substring(0, idx);
        }

        // Convert file separators.
        uri = uri.replace('/', File.separatorChar);

        // Simplistic dumb security check.
        // You will have to do something serious in the production environment.
        if (uri.contains(File.separator + ".") || uri.contains("." + File.separator) || uri.startsWith(".")
                || uri.endsWith(".")) {
            return null;
        }

        // Convert to absolute path.
        return AppProperties.getInstance().getWorkbenchStaticFilesDirectory() + uri;
    }

    /**
     * Send error to client
     * 
     * @param ctx
     *            Context
     * @param e
     *            Message Event
     * @param status
     *            HTTP response status
     * @param moreInfo
     *            More details of the error to log
     */
    private void sendError(ChannelHandlerContext ctx, MessageEvent e, HttpResponseStatus status, String moreInfo) {
        writeLogEntry(e, status, moreInfo);

        HttpResponse response = new DefaultHttpResponse(HTTP_1_1, status);
        setDateHeader(response);

        // Send error back as plain text in the body
        response.setHeader(CONTENT_TYPE, "text/plain; charset=UTF-8");
        response.setContent(ChannelBuffers.copiedBuffer("Failure: " + status.toString() + "\r\n", CharsetUtil.UTF_8));

        ChannelFuture writeFuture = ctx.getChannel().write(response);
        
        // Decide whether to close the connection or not.
        if (!isKeepAlive((HttpRequest)e.getMessage())) {
            writeFuture.addListener(ChannelFutureListener.CLOSE);
        }
    }

    /**
     * When file timestamp is the same as what the browser is sending up, send a "304 Not Modified"
     * 
     * @param ctx
     *            Context
     * @param e
     *            Message Event
     */
    private void sendNotModified(ChannelHandlerContext ctx, MessageEvent e) {
        writeLogEntry(e, HttpResponseStatus.NOT_MODIFIED, null);

        HttpResponse response = new DefaultHttpResponse(HTTP_1_1, HttpResponseStatus.NOT_MODIFIED);
        setDateHeader(response);

        ChannelFuture writeFuture = ctx.getChannel().write(response);
        
        // Decide whether to close the connection or not.
        if (!isKeepAlive((HttpRequest)e.getMessage())) {
            writeFuture.addListener(ChannelFutureListener.CLOSE);
        }
    }

    /**
     * Sets the content type header for the HTTP Response
     * 
     * @param response
     *            HTTP response
     * @param file
     *            file to extract content type
     */
    private void setContentTypeHeader(HttpResponse response, File file) {
        String mimeType = null;
        String filePath = file.getPath();

        int idx = filePath.lastIndexOf('.');
        if (idx == -1) {
            mimeType = "application/octet-stream";
        } else {
            String fileExtension = filePath.substring(idx).toLowerCase();

            // Try common types first
            if (fileExtension.equals(".html")) {
                mimeType = "text/html";
            } else if (fileExtension.equals(".css")) {
                mimeType = "text/css";
            } else if (fileExtension.equals(".js")) {
                mimeType = "application/javascript";
            } else if (fileExtension.equals(".gif")) {
                mimeType = "image/gif";
            } else if (fileExtension.equals(".png")) {
                mimeType = "image/png";
            } else if (fileExtension.equals(".txt")) {
                mimeType = "text/plain";
            } else if (fileExtension.equals(".xml")) {
                mimeType = "application/xml";
            } else if (fileExtension.equals(".json")) {
                mimeType = "application/json";
            } else {
                MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
                mimeType = mimeTypesMap.getContentType(file.getPath());
            }
        }

        response.setHeader(HttpHeaders.Names.CONTENT_TYPE, mimeType);
    }

    /**
     * Sets the Date header for the HTTP response
     * 
     * @param response
     *            HTTP response
     * @param file
     *            file to extract content type
     */
    private void setDateHeader(HttpResponse response) {
        SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
        dateFormatter.setTimeZone(TimeZone.getTimeZone(HTTP_DATE_GMT_TIMEZONE));

        Calendar time = new GregorianCalendar();
        response.setHeader(HttpHeaders.Names.DATE, dateFormatter.format(time.getTime()));
    }

    /**
     * Sets the Date and Cache headers for the HTTP Response
     * 
     * @param response
     *            HTTP response
     * @param file
     *            file to extract content type
     */
    private void setDateAndCacheHeaders(HttpResponse response, File filetoCache) {
        SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
        dateFormatter.setTimeZone(TimeZone.getTimeZone(HTTP_DATE_GMT_TIMEZONE));

        // Date header
        Calendar time = new GregorianCalendar();
        response.setHeader(HttpHeaders.Names.DATE, dateFormatter.format(time.getTime()));

        // Add cache headers
        time.add(Calendar.SECOND, AppProperties.getInstance().getWorkbenchStaticFilesCacheSeconds());
        response.setHeader(HttpHeaders.Names.EXPIRES, dateFormatter.format(time.getTime()));

        response.setHeader(HttpHeaders.Names.CACHE_CONTROL, "private, max-age="
                + AppProperties.getInstance().getWorkbenchStaticFilesCacheSeconds());

        response.setHeader(HttpHeaders.Names.LAST_MODIFIED, dateFormatter.format(new Date(filetoCache.lastModified())));
    }


    /**
     * Write audit log entry
     * 
     * @param e
     *            Message Event
     */
    private void writeLogEntry(MessageEvent e, HttpResponseStatus status, String moreInfo) {
        HttpRequest request = (HttpRequest) e.getMessage();
        _logger.info("GET %s REMOTE_IP=%s STATUS=%s CHANNEL=%s %s", request.getUri(), e.getRemoteAddress().toString(),
                status, e.getChannel().getId(), moreInfo == null ? StringUtils.EMPTY : moreInfo);
        
        StringBuilder sb = new StringBuilder("Request Headers\n");
        List<java.util.Map.Entry<String, String>> headers = request.getHeaders();
        for (java.util.Map.Entry<String, String> entry : headers){
            sb.append(entry.getKey()).append("=").append(entry.getValue()).append("\n");
        }
        _logger.debug(sb.toString());
        
        return;
    }
}
