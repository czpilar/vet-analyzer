package net.czpilar.vet.analyzer.starter.tcp.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.czpilar.vet.analyzer.core.listener.AnalyzerMessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class RawChannelHandler extends SimpleChannelInboundHandler<String> {

    private static final Logger log = LoggerFactory.getLogger(RawChannelHandler.class);

    private final String sessionId;
    private final String remoteAddress;
    private final List<AnalyzerMessageListener> listeners;

    public RawChannelHandler(String sessionId, String remoteAddress,
                              List<AnalyzerMessageListener> listeners) {
        this.sessionId = sessionId;
        this.remoteAddress = remoteAddress;
        this.listeners = listeners;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) {
        log.warn("Received unknown message ({} bytes) in session {}", msg.length(), sessionId);
        for (var listener : listeners) {
            listener.onRawMessage(msg, remoteAddress);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        for (var listener : listeners) {
            listener.onSessionEnd(sessionId);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Error in raw handler for session {}", sessionId, cause);
        ctx.close();
    }
}
