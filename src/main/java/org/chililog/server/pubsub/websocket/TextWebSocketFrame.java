
package org.chililog.server.pubsub.websocket;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.util.CharsetUtil;

/**
 * Web Socket text frame with assumed UTF-8 encoding
 * 
 * @author vibul
 * 
 */
public class TextWebSocketFrame extends WebSocketFrame {
    @Override
    public WebSocketFrameType getType() {
        return WebSocketFrameType.TEXT;
    }

    /**
     * Creates a new empty text frame.
     */
    public TextWebSocketFrame() {
        this.setBinaryData(ChannelBuffers.EMPTY_BUFFER);
    }

    /**
     * Creates a new text frame with the specified text string.
     * 
     * @param text
     *            String to put in the frame
     */
    public TextWebSocketFrame(String text) {
        this.setBinaryData(ChannelBuffers.copiedBuffer(text, CharsetUtil.UTF_8));
    }

    /**
     * Creates a new frame with the specified binary data.
     * 
     * @param binaryData
     *            the content of the frame. Must be UTF-8 encoded
     */
    public TextWebSocketFrame(ChannelBuffer binaryData) {
        this.setBinaryData(binaryData);
    }

    /**
     * Returns the text data in this frame
     */
    public String getText() {
        if (this.getBinaryData() == null) {
            return null;
        }
        return this.getBinaryData().toString(CharsetUtil.UTF_8);
    }

    /**
     * Sets the string for this frame
     * 
     * @param text
     *            text to store
     */
    public void setText(String text) {
        if (text == null) {
            throw new NullPointerException("text");
        }
        this.setBinaryData(ChannelBuffers.copiedBuffer(text, CharsetUtil.UTF_8));
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(text: " + getText() + ')';
    }
}
