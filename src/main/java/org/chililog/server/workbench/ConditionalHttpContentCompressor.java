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

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.HttpContentCompressor;

/**
 * <p>
 * Extends the Netty HttpContentCompressor class to add a flag that allows compression to be turned on or off.
 * </p>
 * <p>
 * This class was introduced because the HttpContentCompressor does not handle zero-copy or ChunkedFile used by
 * {@link StaticFileRequestHandler}. HttpContentCompressor only seems to work when the HTTPResponse content is set to a
 * ChannelBuffer.
 * </p>
 * <p>
 * For example, to turn off compression from another handler:
 * </p>
 * 
 * <pre>
 * ChannelHandler deflater = ctx.getPipeline().get(&quot;deflater&quot;);
 * if (deflater instanceof ConditionalHttpContentCompressor) {
 *     ((ConditionalHttpContentCompressor) deflater).setDoCompression(false);
 * }
 * </pre>
 */
public class ConditionalHttpContentCompressor extends HttpContentCompressor {

    private boolean _doCompression = true;

    /**
     * Creates a new handler with the default compression level (<tt>6</tt>).
     */
    public ConditionalHttpContentCompressor() {
        super();
    }

    /**
     * Creates a new handler with the specified compression level.
     * 
     * @param compressionLevel
     *            {@code 1} yields the fastest compression and {@code 9} yields the best compression. {@code 0} means no
     *            compression. The default compression level is {@code 6}.
     */
    public ConditionalHttpContentCompressor(int compressionLevel) {
        super(compressionLevel);
    }

    /**
     * 
     */
    @Override
    public void writeRequested(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        if (_doCompression) {
            super.writeRequested(ctx, e);
        } else {
            ctx.sendDownstream(e);
        }
    }

    /**
     * Flag to indicate if compression is to be performed
     */
    public boolean isDoCompression() {
        return _doCompression;
    }

    /**
     * Set flag to indicate if compression is to be performed.
     */
    public void setDoCompression(boolean doCompression) {
        _doCompression = doCompression;
    }

}
