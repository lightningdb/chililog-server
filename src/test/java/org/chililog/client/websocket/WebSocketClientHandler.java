
package org.chililog.client.websocket;

import java.net.InetSocketAddress;
import java.net.URI;

import org.chililog.server.pubsub.websocket.WebSocketClientHandshaker;
import org.chililog.server.pubsub.websocket.WebSocketClientHandshakerFactory;
import org.chililog.server.pubsub.websocket.WebSocketFrame;
import org.chililog.server.pubsub.websocket.WebSocketSpecificationVersion;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.util.CharsetUtil;

/**
 * Copied from https://github.com/cgbystrom/netty-tools
 * 
 * Handles socket communication for a connected WebSocket client Not intended for end-users. Please use
 * {@link WebSocketClient} or {@link WebSocketCallback} for controlling your client.
 * 
 * @author <a href="http://www.pedantique.org/">Carl Bystr&ouml;m</a>
 */
public class WebSocketClientHandler extends SimpleChannelUpstreamHandler implements WebSocketClient {

    private ClientBootstrap bootstrap;
    private URI url;
    private WebSocketCallback callback;
    private Channel channel;
    private WebSocketClientHandshaker handshaker = null;
    private WebSocketSpecificationVersion version;

    public WebSocketClientHandler(ClientBootstrap bootstrap, URI url, WebSocketSpecificationVersion version, WebSocketCallback callback) {
        this.bootstrap = bootstrap;
        this.url = url;
        this.version = version;
        this.callback = callback;
    }

    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        channel = e.getChannel();
        this.handshaker = new WebSocketClientHandshakerFactory().newHandshaker(url, version, null);
        handshaker.beginOpeningHandshake(ctx, channel);
    }

    @Override
    public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        callback.onDisconnect(this);
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        if (!handshaker.isOpeningHandshakeCompleted()) {
            handshaker.endOpeningHandshake(ctx, (HttpResponse) e.getMessage());
            callback.onConnect(this);
            return;
        }

        if (e.getMessage() instanceof HttpResponse) {
            HttpResponse response = (HttpResponse) e.getMessage();
            throw new WebSocketException("Unexpected HttpResponse (status=" + response.getStatus() + ", content="
                    + response.getContent().toString(CharsetUtil.UTF_8) + ")");
        }

        WebSocketFrame frame = (WebSocketFrame) e.getMessage();
        callback.onMessage(this, frame);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        final Throwable t = e.getCause();
        callback.onError(t);
        e.getChannel().close();
    }

    public ChannelFuture connect() {
        return bootstrap.connect(new InetSocketAddress(url.getHost(), url.getPort()));
    }

    public ChannelFuture disconnect() {
        return channel.close();
    }

    public ChannelFuture send(WebSocketFrame frame) {
        return channel.write(frame);
    }

    public URI getUrl() {
        return url;
    }

    public void setUrl(URI url) {
        this.url = url;
    }
}