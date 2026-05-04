package net.czpilar.vet.analyzer.starter.tcp.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.czpilar.vet.analyzer.core.listener.AnalyzerMessageListener;
import net.czpilar.vet.analyzer.core.listener.SessionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class RawChannelHandler extends SimpleChannelInboundHandler<String> {

    private static final Logger log = LoggerFactory.getLogger(RawChannelHandler.class);

    private final SessionContext session;
    private final List<AnalyzerMessageListener> listeners;

    public RawChannelHandler(SessionContext session,
                             List<AnalyzerMessageListener> listeners) {
        this.session = session;
        this.listeners = listeners;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) {
        log.warn("Received unknown message ({} bytes) in session {}", msg.length(), session.sessionId());
        for (AnalyzerMessageListener listener : listeners) {
            listener.onRawMessage(msg, session);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        for (AnalyzerMessageListener listener : listeners) {
            listener.onSessionEnd(session.sessionId());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Error in raw handler for session {}", session.sessionId(), cause);
        ctx.close();
    }
}
