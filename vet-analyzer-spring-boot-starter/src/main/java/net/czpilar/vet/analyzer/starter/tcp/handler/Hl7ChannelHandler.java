package net.czpilar.vet.analyzer.starter.tcp.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.czpilar.vet.analyzer.core.listener.AnalyzerMessageListener;
import net.czpilar.vet.analyzer.core.listener.SessionContext;
import net.czpilar.vet.analyzer.core.model.AnalyzerMessage;
import net.czpilar.vet.analyzer.core.model.hl7.Hl7Message;
import net.czpilar.vet.analyzer.core.parser.MessageParserRegistry;
import net.czpilar.vet.analyzer.core.protocol.hl7.Hl7Protocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class Hl7ChannelHandler extends SimpleChannelInboundHandler<String> {

    private static final Logger log = LoggerFactory.getLogger(Hl7ChannelHandler.class);

    private final SessionContext session;
    private final MessageParserRegistry parserRegistry;
    private final List<AnalyzerMessageListener> listeners;

    public Hl7ChannelHandler(SessionContext session,
                             MessageParserRegistry parserRegistry,
                             List<AnalyzerMessageListener> listeners) {
        this.session = session;
        this.parserRegistry = parserRegistry;
        this.listeners = listeners;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) {
        log.info("Received HL7 message ({} bytes) in session {}", msg.length(), session.sessionId());

        AnalyzerMessage parsed = parserRegistry.parse(msg);

        if (parsed != null) {
            for (AnalyzerMessageListener listener : listeners) {
                listener.onMessage(parsed, msg, session);
            }
        } else {
            for (AnalyzerMessageListener listener : listeners) {
                listener.onRawMessage(msg, session);
            }
        }

        // Send ACK
        if (parsed instanceof Hl7Message hl7) {
            String ack = Hl7Protocol.createAck(hl7.messageControlId());
            ctx.writeAndFlush(ack).addListener(future -> {
                if (future.isSuccess()) {
                    log.debug("Sent ACK for message control ID: {}", hl7.messageControlId());
                } else {
                    log.warn("Failed to send ACK for message control ID: {}", hl7.messageControlId(), future.cause());
                }
            });
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
        log.error("Error in HL7 handler for session {}", session.sessionId(), cause);
        ctx.close();
    }
}
