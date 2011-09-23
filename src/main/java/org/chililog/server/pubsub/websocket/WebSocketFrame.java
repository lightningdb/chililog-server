package org.chililog.server.pubsub.websocket;

import org.jboss.netty.buffer.ChannelBuffer;

public abstract class WebSocketFrame {

    /**
     * Contents of this frame
     */
    private ChannelBuffer binaryData;

    /**
     * Returns the type of this frame.
     */
    public abstract WebSocketFrameType getType();

    /**
     * Returns binary data
     */
    public ChannelBuffer getBinaryData() {
        return binaryData;
    }

    /**
     * Sets the binary data for this frame
     */
    public void setBinaryData(ChannelBuffer binaryData) {
        this.binaryData = binaryData;
    }

}
