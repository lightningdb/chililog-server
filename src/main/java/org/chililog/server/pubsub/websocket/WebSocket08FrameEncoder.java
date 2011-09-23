// (BSD License: http://www.opensource.org/licenses/bsd-license)
//
// Copyright (c) 2011, Joe Walnes and contributors
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or
// without modification, are permitted provided that the
// following conditions are met:
//
// * Redistributions of source code must retain the above
// copyright notice, this list of conditions and the
// following disclaimer.
//
// * Redistributions in binary form must reproduce the above
// copyright notice, this list of conditions and the
// following disclaimer in the documentation and/or other
// materials provided with the distribution.
//
// * Neither the name of the Webbit nor the names of
// its contributors may be used to endorse or promote products
// derived from this software without specific prior written
// permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
// CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
// INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
// MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
// DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
// CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
// INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
// (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
// GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
// BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
// LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
// OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
// POSSIBILITY OF SUCH DAMAGE.

package org.chililog.server.pubsub.websocket;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;

/**
 * This code was originally taken from webbit and modified.
 * 
 * @author https://github.com/joewalnes/webbit
 */
public class WebSocket08FrameEncoder extends OneToOneEncoder {

    private static final byte OPCODE_TEXT = 0x1;
    private static final byte OPCODE_BINARY = 0x2;
    private static final byte OPCODE_CLOSE = 0x8;
    private static final byte OPCODE_PING = 0x9;
    private static final byte OPCODE_PONG = 0xA;

    @Override
    protected Object encode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
        if (msg instanceof WebSocketFrame) {
            WebSocketFrame frame = (WebSocketFrame) msg;
            ChannelBuffer data = frame.getBinaryData();
            ChannelBuffer encoded = channel.getConfig().getBufferFactory()
                    .getBuffer(data.order(), data.readableBytes() + 6);

            byte opcode;
            if (frame instanceof TextWebSocketFrame) {
                opcode = OPCODE_TEXT;
            } else if (frame instanceof PingWebSocketFrame) {
                opcode = OPCODE_PING;
            } else if (frame instanceof PongWebSocketFrame) {
                opcode = OPCODE_PONG;
            } else if (frame instanceof CloseWebSocketFrame) {
                opcode = OPCODE_CLOSE;
            } else if (frame instanceof BinaryWebSocketFrame) {
                opcode = OPCODE_BINARY;
            } else {
                throw new UnsupportedOperationException("Cannot encode frame of type: " + frame.getClass().getName());
            }
            encoded.writeByte(0x80 | opcode);

            int length = data.readableBytes();
            if (length < 126) {
                encoded.writeByte(length);
            } else if (length < 65535) {
                encoded.writeByte(126);
                encoded.writeShort(length);
            } else {
                encoded.writeByte(127);
                encoded.writeInt(length);
            }

            encoded.writeBytes(data, data.readerIndex(), data.readableBytes());
            encoded = encoded.slice(0, encoded.writerIndex());
            return encoded;
        }
        return msg;
    }
}