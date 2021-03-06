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

package org.chililog.server.pubsub.jsonhttp;

import static org.jboss.netty.channel.Channels.*;

import java.util.concurrent.Executor;

import javax.net.ssl.SSLEngine;

import org.chililog.server.common.AppProperties;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.jboss.netty.handler.ssl.SslHandler;

/**
 * <p>
 * Sets up the pipeline of handlers for incoming JSON log requests over HTTP and HTTP web sockets
 * </p>
 */
public class JsonHttpServerPipelineFactory implements ChannelPipelineFactory {

    private Executor _executor = null;

    /**
     * Constructor
     * 
     * @param executor
     *            ThreadPool to use for processing requests
     */
    public JsonHttpServerPipelineFactory(Executor executor) {
        _executor = executor;
    }

    /**
     * Creates an HTTP Pipeline for our server
     */
    public ChannelPipeline getPipeline() throws Exception {
        AppProperties appProperties = AppProperties.getInstance();

        // Create a default pipeline implementation.
        ChannelPipeline pipeline = pipeline();

        // SSL handling
        if (appProperties.getPubSubJsonHttpSslEnabled()) {
            SSLEngine engine = JsonHttpSslContextManager.getInstance().getServerContext().createSSLEngine();
            engine.setUseClientMode(false);
            pipeline.addLast("ssl", new SslHandler(engine));
        }

        // Decodes ChannelBuffer into HTTP Request message
        pipeline.addLast("decoder", new HttpRequestDecoder());

        // Aggregate HTTP Chunks so we don't have to do it in our code. Allow 1MB aggregation buffer
        pipeline.addLast("aggregator", new HttpChunkAggregator(1048576));

        // Encodes HTTTPRequest message to ChannelBuffer
        pipeline.addLast("encoder", new HttpResponseEncoder());

        // Execute the handler in a new thread via OrderedMemoryAwareThreadPoolExecutor
        // Removed because we get funny errors like handshaking not completed and
        // "Message of type 'org.jboss.netty.buffer.BigEndianHeapChannelBuffer' is not supported."
        // Maybe OrderedMemoryAwareThreadPoolExecutor does not work well with duplex channel???
        //
        // pipeline.addLast("pipelineExecutor", new ExecutionHandler(_pipelineExecutor));

        // Handler to dispatch processing to our services
        pipeline.addLast("handler", new JsonHttpRequestHandler(_executor));

        return pipeline;
    }
}
