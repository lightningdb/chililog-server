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

import static org.jboss.netty.channel.Channels.*;

import javax.net.ssl.SSLEngine;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.jboss.netty.handler.execution.ExecutionHandler;
import org.jboss.netty.handler.ssl.SslHandler;
import org.jboss.netty.handler.stream.ChunkedWriteHandler;

import com.chililog.server.common.AppProperties;

/**
 * <p>
 * Sets up the pipeline of handlers for incoming HTTP requests.
 * </p>
 */
public class HttpServerPipelineFactory implements ChannelPipelineFactory
{
    private final ExecutionHandler _executionHandler;

    public HttpServerPipelineFactory(ExecutionHandler executionHandler)
    {
        _executionHandler = executionHandler;
    }

    /**
     * Creates an HTTP Pipeline for our server
     */
    public ChannelPipeline getPipeline() throws Exception
    {
        AppProperties appProperties = AppProperties.getInstance();

        // Create a default pipeline implementation.
        ChannelPipeline pipeline = pipeline();

        // SSL handling
        if (appProperties.getUiSslEnabled())
        {
            SSLEngine engine = SslContextManager.getInstance().getServerContext().createSSLEngine();
            engine.setUseClientMode(false);
            pipeline.addLast("ssl", new SslHandler(engine));
        }

        // Decodes ChannelBuffer into HTTP Request message
        pipeline.addLast("decoder", new HttpRequestDecoder());

        // Uncomment the following line if you don't want to handle HttpChunks.
        // Leave it off. We want to handle large file uploads efficiently by not aggregating and storing in memory
        // pipeline.addLast("aggregator", new HttpChunkAggregator(1048576));

        // Encodes HTTTPRequest message to ChannelBuffer
        pipeline.addLast("encoder", new HttpResponseEncoder());

        // Chunked handler for SSL large static file downloads
        if (appProperties.getUiSslEnabled())
        {
            pipeline.addLast("chunkedWriter", new ChunkedWriteHandler());
        }

        // Compress
        pipeline.addLast("deflater", new ConditionalHttpContentCompressor());

        // Execution handler to move blocking tasks into another thread pool
        pipeline.addLast("executionHandler", _executionHandler);
        
        // Handler to dispatch processing to our services
        pipeline.addLast("handler", new HttpRequestHandler());

        return pipeline;
    }
}
