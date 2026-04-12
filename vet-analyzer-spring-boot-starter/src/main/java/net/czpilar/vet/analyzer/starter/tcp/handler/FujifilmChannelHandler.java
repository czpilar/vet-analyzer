package net.czpilar.vet.analyzer.starter.tcp.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.czpilar.vet.analyzer.core.listener.AnalyzerMessageListener;
import net.czpilar.vet.analyzer.core.parser.MessageParserRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class FujifilmChannelHandler extends SimpleChannelInboundHandler<String> {

    private static final Logger log = LoggerFactory.getLogger(FujifilmChannelHandler.class);

    private final String sessionId;
    private final String remoteAddress;
    private final MessageParserRegistry parserRegistry;
    private final List<AnalyzerMessageListener> listeners;

    public FujifilmChannelHandler(String sessionId, String remoteAddress,
                                   MessageParserRegistry parserRegistry,
                                   List<AnalyzerMessageListener> listeners) {
        this.sessionId = sessionId;
        this.remoteAddress = remoteAddress;
        this.parserRegistry = parserRegistry;
        this.listeners = listeners;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) {
        log.info("Received Fujifilm message ({} bytes) in session {}", msg.length(), sessionId);

        var parsed = parserRegistry.parse(msg);

        if (parsed != null) {
            for (var listener : listeners) {
                listener.onMessage(parsed, msg, remoteAddress);
            }
        } else {
            for (var listener : listeners) {
                listener.onRawMessage(msg, remoteAddress);
            }
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
        log.error("Error in Fujifilm handler for session {}", sessionId, cause);
        ctx.close();
    }
}
