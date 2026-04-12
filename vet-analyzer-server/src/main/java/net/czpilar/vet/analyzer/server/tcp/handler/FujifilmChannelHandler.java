package net.czpilar.vet.analyzer.server.tcp.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.czpilar.vet.analyzer.core.model.AnalyzerMessage;
import net.czpilar.vet.analyzer.core.model.AnalyzerType;
import net.czpilar.vet.analyzer.core.parser.MessageParserRegistry;
import net.czpilar.vet.analyzer.server.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FujifilmChannelHandler extends SimpleChannelInboundHandler<String> {

    private static final Logger log = LoggerFactory.getLogger(FujifilmChannelHandler.class);

    private final Session session;
    private final MessageParserRegistry parserRegistry;
    private boolean typeDetected = false;

    public FujifilmChannelHandler(Session session, MessageParserRegistry parserRegistry) {
        this.session = session;
        this.parserRegistry = parserRegistry;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        log.info("Received Fujifilm message ({} bytes) in session {}", msg.length(), session.getSessionId());

        var parsed = parserRegistry.parse(msg);

        if (!typeDetected && parsed != null) {
            AnalyzerType type = parsed.analyzerType();
            session.updateAnalyzerType(type);
            typeDetected = true;
            log.info("Auto-detected analyzer type: {}", type.displayName());
        }

        session.writeMessage(msg, parsed);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        session.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Error in Fujifilm handler for session {}", session.getSessionId(), cause);
        session.close();
        ctx.close();
    }
}
