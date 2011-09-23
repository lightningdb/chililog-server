
package org.chililog.server.pubsub.websocket;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

public class BinaryWebSocketFrame extends WebSocketFrame {

    @Override
    public WebSocketFrameType getType() {
        return WebSocketFrameType.BINARY;
    }

    /**
     * Creates a new empty binary frame.
     */
    public BinaryWebSocketFrame() {
        this.setBinaryData(ChannelBuffers.EMPTY_BUFFER);
    }

    /**
     * Creates a new frame with the specified binary data.
     * 
     * @param binaryData
     *            the content of the frame.
     */
    public BinaryWebSocketFrame(ChannelBuffer binaryData) {
        this.setBinaryData(binaryData);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(type: " + getType() + ", " + "data: " + getBinaryData() + ')';
    }

}
