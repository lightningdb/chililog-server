/*
 * Copyright 2009 Red Hat, Inc. Red Hat licenses this file to you under the Apache License, version 2.0 (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the License at:
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 */

package com.chililog.server.ui;

import static org.jboss.netty.channel.Channels.*;

import javax.net.ssl.SSLEngine;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.jboss.netty.handler.ssl.SslHandler;
import org.jboss.netty.handler.stream.ChunkedWriteHandler;

import com.chililog.server.common.AppProperties;

/**
 * @author <a href="http://www.jboss.org/netty/">The Netty Project</a>
 * @author Andy Taylor (andy.taylor@jboss.org)
 * @author <a href="http://gleamynode.net/">Trustin Lee</a>
 * 
 * @version $Rev: 2080 $, $Date: 2010-01-26 18:04:19 +0900 (Tue, 26 Jan 2010) $
 */
public class HttpServerPipelineFactory implements ChannelPipelineFactory
{
    /**
     * Creates an HTTP Pipeline for our server
     */
    public ChannelPipeline getPipeline() throws Exception
    {
        AppProperties appProperties = AppProperties.getInstance();

        // Create a default pipeline implementation.
        ChannelPipeline pipeline = pipeline();

        // SSL handling
        if (appProperties.getWebSslEnabled())
        {
            SSLEngine engine = SslContextManager.getInstance().getServerContext().createSSLEngine();
            engine.setUseClientMode(false);
            pipeline.addLast("ssl", new SslHandler(engine));
        }

        // Decodes ChannelBuffer into HTTP Request message
        pipeline.addLast("decoder", new HttpRequestDecoder());

        // Uncomment the following line if you don't want to handle HttpChunks.
        // pipeline.addLast("aggregator", new HttpChunkAggregator(1048576));

        // Encodes HTTTPRequest message to ChannelBuffer
        pipeline.addLast("encoder", new HttpResponseEncoder());

        // Chunked handler for SSL large static file downloads
        if (appProperties.getWebSslEnabled())
        {
            pipeline.addLast("chunkedWriter", new ChunkedWriteHandler());
        }

        // Compress
        pipeline.addLast("deflater", new ConditionalHttpContentCompressor());

        // Handler to dispatch processing to our services
        pipeline.addLast("handler", new HttpRequestHandler());

        return pipeline;
    }
}
