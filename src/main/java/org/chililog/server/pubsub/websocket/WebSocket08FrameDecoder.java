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
import org.jboss.netty.handler.codec.frame.CorruptedFrameException;
import org.jboss.netty.handler.codec.frame.TooLongFrameException;
import org.jboss.netty.handler.codec.replay.ReplayingDecoder;

import java.util.ArrayList;
import java.util.List;

/**
 * This code was originally taken from webbit and modified.
 * 
 * @author https://github.com/joewalnes/webbit
 */
public class WebSocket08FrameDecoder extends ReplayingDecoder<WebSocket08FrameDecoder.State> {

    private static final byte OPCODE_CONT = 0x0;
    private static final byte OPCODE_TEXT = 0x1;
    private static final byte OPCODE_BINARY = 0x2;
    private static final byte OPCODE_CLOSE = 0x8;
    private static final byte OPCODE_PING = 0x9;
    private static final byte OPCODE_PONG = 0xA;

    public static final int MAX_LENGTH = 16384;

    private Byte fragmentOpcode;
    private Byte opcode = null;
    private int currentFrameLength;
    private ChannelBuffer maskingKey;
    private List<ChannelBuffer> frames = new ArrayList<ChannelBuffer>();

    public static enum State {
        FRAME_START, PARSING_LENGTH, MASKING_KEY, PARSING_LENGTH_2, PARSING_LENGTH_3, PAYLOAD
    }

    public WebSocket08FrameDecoder() {
        super(State.FRAME_START);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer, State state)
            throws Exception {
        switch (state) {
            case FRAME_START:
                byte b = buffer.readByte();
                byte fin = (byte) (b & 0x80);
                byte reserved = (byte) (b & 0x70);
                byte opcode = (byte) (b & 0x0F);

                if (reserved != 0) {
                    throw new CorruptedFrameException("Reserved bits set: " + bits(reserved));
                }
                if (!isOpcode(opcode)) {
                    throw new CorruptedFrameException("Invalid opcode " + hex(opcode));
                }

                if (fin == 0) {
                    if (fragmentOpcode == null) {
                        if (!isDataOpcode(opcode)) {
                            throw new CorruptedFrameException("Fragmented frame with invalid opcode " + hex(opcode));
                        }
                        fragmentOpcode = opcode;
                    } else if (opcode != OPCODE_CONT) {
                        throw new CorruptedFrameException("Continuation frame with invalid opcode " + hex(opcode));
                    }
                } else {
                    if (fragmentOpcode != null) {
                        if (!isControlOpcode(opcode) && opcode != OPCODE_CONT) {
                            throw new CorruptedFrameException("Final frame with invalid opcode " + hex(opcode));
                        }
                    } else if (opcode == OPCODE_CONT) {
                        throw new CorruptedFrameException("Final frame with invalid opcode " + hex(opcode));
                    }
                    this.opcode = opcode;
                }

                checkpoint(State.PARSING_LENGTH);
            case PARSING_LENGTH:
                b = buffer.readByte();
                byte masked = (byte) (b & 0x80);
                if (masked == 0) {
                    throw new CorruptedFrameException("Unmasked frame received");
                }

                int length = (byte) (b & 0x7F);

                if (length < 126) {
                    currentFrameLength = length;
                    checkpoint(State.MASKING_KEY);
                } else if (length == 126) {
                    checkpoint(State.PARSING_LENGTH_2);
                } else if (length == 127) {
                    checkpoint(State.PARSING_LENGTH_3);
                }
                return null;
            case PARSING_LENGTH_2:
                currentFrameLength = buffer.readShort();
                checkpoint(State.MASKING_KEY);
                return null;
            case PARSING_LENGTH_3:
                currentFrameLength = buffer.readInt();
                checkpoint(State.MASKING_KEY);
                return null;
            case MASKING_KEY:
                maskingKey = buffer.readBytes(4);
                checkpoint(State.PAYLOAD);
            case PAYLOAD:
                checkpoint(State.FRAME_START);
                ChannelBuffer payload = buffer.readBytes(currentFrameLength);
                unmask(payload);

                if (this.opcode == OPCODE_CONT) {
                    this.opcode = fragmentOpcode;
                    frames.add(payload);

                    payload = channel.getConfig().getBufferFactory().getBuffer(0);
                    for (ChannelBuffer channelBuffer : frames) {
                        payload.ensureWritableBytes(channelBuffer.readableBytes());
                        payload.writeBytes(channelBuffer);
                    }

                    this.fragmentOpcode = null;
                    frames.clear();
                }

                if (this.opcode == OPCODE_TEXT) {
                    if (payload.readableBytes() > MAX_LENGTH) {
                        throw new TooLongFrameException();
                    }
                    return new TextWebSocketFrame(payload);
                } else if (this.opcode == OPCODE_BINARY) {
                    return new BinaryWebSocketFrame(payload);
                } else if (this.opcode == OPCODE_PING) {
                    return new PingWebSocketFrame(payload);
                } else if (this.opcode == OPCODE_PONG) {
                    return new PongWebSocketFrame(payload);
                } else if (this.opcode == OPCODE_CLOSE) {
                    return new CloseWebSocketFrame();
                }else {
                    throw new UnsupportedOperationException("Cannot decode opcode: " + this.opcode);
                }
            default:
                throw new Error("Shouldn't reach here.");
        }
    }

    private void unmask(ChannelBuffer frame) {
        byte[] bytes = frame.array();
        for (int i = 0; i < bytes.length; i++) {
            frame.setByte(i, frame.getByte(i) ^ maskingKey.getByte(i % 4));
        }
    }

    private String bits(byte b) {
        return Integer.toBinaryString(b).substring(24);
    }

    private String hex(byte b) {
        return Integer.toHexString(b);
    }

    private boolean isOpcode(int opcode) {
        return opcode == OPCODE_CONT || opcode == OPCODE_TEXT || opcode == OPCODE_BINARY || opcode == OPCODE_CLOSE
                || opcode == OPCODE_PING || opcode == OPCODE_PONG;
    }

    private boolean isControlOpcode(int opcode) {
        return opcode == OPCODE_CLOSE || opcode == OPCODE_PING || opcode == OPCODE_PONG;
    }

    private boolean isDataOpcode(int opcode) {
        return opcode == OPCODE_TEXT || opcode == OPCODE_BINARY;
    }
}