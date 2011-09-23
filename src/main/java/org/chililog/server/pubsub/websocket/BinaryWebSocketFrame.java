
//
// Copyright 2011 Cinch Logic Pty Ltd.
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

package org.chililog.server.pubsub.websocket;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

/**
 * Web Socket frame containing binary data
 * 
 * @author vibul
 */
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
