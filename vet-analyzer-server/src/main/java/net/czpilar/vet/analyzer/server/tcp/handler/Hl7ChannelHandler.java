package net.czpilar.vet.analyzer.server.tcp.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.czpilar.vet.analyzer.core.model.AnalyzerType;
import net.czpilar.vet.analyzer.core.model.hl7.Hl7Message;
import net.czpilar.vet.analyzer.core.parser.MessageParserRegistry;
import net.czpilar.vet.analyzer.core.protocol.hl7.Hl7Protocol;
import net.czpilar.vet.analyzer.server.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Hl7ChannelHandler extends SimpleChannelInboundHandler<String> {

    private static final Logger log = LoggerFactory.getLogger(Hl7ChannelHandler.class);

    private final Session session;
    private final MessageParserRegistry parserRegistry;
    private boolean typeDetected = false;

    public Hl7ChannelHandler(Session session, MessageParserRegistry parserRegistry) {
        this.session = session;
        this.parserRegistry = parserRegistry;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        if (!typeDetected) {
            session.updateAnalyzerType(AnalyzerType.BM850_EXIGO);
            typeDetected = true;
        }

        log.info("Received HL7 message ({} bytes) in session {}", msg.length(), session.getSessionId());

        var parsed = parserRegistry.parse(msg);
        session.writeMessage(msg, parsed);

        // Send ACK
        if (parsed instanceof Hl7Message hl7) {
            String ack = Hl7Protocol.createAck(hl7.messageControlId());
            ctx.writeAndFlush(ack);
            log.debug("Sent ACK for message control ID: {}", hl7.messageControlId());
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        session.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Error in HL7 handler for session {}", session.getSessionId(), cause);
        session.close();
        ctx.close();
    }
}
