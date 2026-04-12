package net.czpilar.vet.analyzer.server.tcp.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.czpilar.vet.analyzer.server.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for unknown/unrecognized protocols.
 * Logs all received data as raw messages without parsing.
 */
public class RawChannelHandler extends SimpleChannelInboundHandler<String> {

    private static final Logger log = LoggerFactory.getLogger(RawChannelHandler.class);

    private final Session session;

    public RawChannelHandler(Session session) {
        this.session = session;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        log.warn("Received unknown message ({} bytes) in session {}", msg.length(), session.getSessionId());
        session.writeMessage(msg, null);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        session.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Error in raw handler for session {}", session.getSessionId(), cause);
        session.close();
        ctx.close();
    }
}
