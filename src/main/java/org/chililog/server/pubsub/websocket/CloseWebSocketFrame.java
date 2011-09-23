
package org.chililog.server.pubsub.websocket;

import org.jboss.netty.buffer.ChannelBuffers;

public class CloseWebSocketFrame extends WebSocketFrame {

    @Override
    public WebSocketFrameType getType() {
        return WebSocketFrameType.CLOSE;
    }

    /**
     * Creates a new empty binary frame.
     */
    public CloseWebSocketFrame() {
        this.setBinaryData(ChannelBuffers.EMPTY_BUFFER);
    }
}
