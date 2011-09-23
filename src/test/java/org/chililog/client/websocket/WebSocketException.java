
package org.chililog.client.websocket;

import java.io.IOException;

/**
 * Copied from https://github.com/cgbystrom/netty-tools
 * 
 * A WebSocket related exception
 * 
 * @author <a href="http://www.pedantique.org/">Carl Bystr&ouml;m</a>
 */
public class WebSocketException extends IOException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public WebSocketException(String s) {
        super(s);
    }

    public WebSocketException(String s, Throwable throwable) {
        super(s, throwable);
    }
}