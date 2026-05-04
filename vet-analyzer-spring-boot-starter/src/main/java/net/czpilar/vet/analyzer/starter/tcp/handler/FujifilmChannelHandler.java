package net.czpilar.vet.analyzer.starter.tcp.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.czpilar.vet.analyzer.core.listener.AnalyzerMessageListener;
import net.czpilar.vet.analyzer.core.listener.SessionContext;
import net.czpilar.vet.analyzer.core.model.AnalyzerMessage;
import net.czpilar.vet.analyzer.core.model.AnalyzerType;
import net.czpilar.vet.analyzer.core.model.fujifilm.FujifilmErrorMessage;
import net.czpilar.vet.analyzer.core.model.fujifilm.FujifilmMessage;
import net.czpilar.vet.analyzer.core.model.fujifilm.FujifilmStartMessage;
import net.czpilar.vet.analyzer.core.parser.MessageParserRegistry;
import net.czpilar.vet.analyzer.core.protocol.fujifilm.FujifilmCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class FujifilmChannelHandler extends SimpleChannelInboundHandler<String> {

    private static final Logger log = LoggerFactory.getLogger(FujifilmChannelHandler.class);

    private final SessionContext session;
    private final MessageParserRegistry parserRegistry;
    private final List<AnalyzerMessageListener> listeners;

    private AnalyzerType detectedType;

    public FujifilmChannelHandler(SessionContext session,
                                  MessageParserRegistry parserRegistry,
                                  List<AnalyzerMessageListener> listeners) {
        this.session = session;
        this.parserRegistry = parserRegistry;
        this.listeners = listeners;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) {
        log.info("Received Fujifilm message ({} bytes) in session {}", msg.length(), session.sessionId());

        AnalyzerMessage parsed = parserRegistry.parse(msg);

        if (parsed != null) {
            updateDetectedType(parsed);
            parsed = applyDetectedType(parsed);
            for (AnalyzerMessageListener listener : listeners) {
                listener.onMessage(parsed, msg, session);
            }
        } else {
            for (AnalyzerMessageListener listener : listeners) {
                listener.onRawMessage(msg, session);
            }
        }
    }

    /**
     * Updates the detected analyzer type based on commands that uniquely identify the device.
     * AU20V uses T, X, Y commands; NX600 uses R, I, W commands.
     * Shared commands (S, E) do not update the detected type.
     */
    private void updateDetectedType(AnalyzerMessage parsed) {
        if (!(parsed instanceof FujifilmMessage fujifilm)) {
            return;
        }
        FujifilmCommand cmd = fujifilm.command();
        if (cmd == FujifilmCommand.T || cmd == FujifilmCommand.X || cmd == FujifilmCommand.Y) {
            detectedType = AnalyzerType.AU20V;
        } else if (cmd == FujifilmCommand.R || cmd == FujifilmCommand.I || cmd == FujifilmCommand.W) {
            detectedType = AnalyzerType.NX600;
        }
    }

    /**
     * Re-wraps shared command messages (S, E) with the previously detected analyzer type.
     * Returns the original message if no type override is needed.
     */
    private AnalyzerMessage applyDetectedType(AnalyzerMessage parsed) {
        if (detectedType == null || parsed.analyzerType() == detectedType) {
            return parsed;
        }
        return switch (parsed) {
            case FujifilmStartMessage s -> new FujifilmStartMessage(
                    detectedType, s.command(), s.testCondition(), s.date(), s.time(),
                    s.sampleNumber(), s.patientId(), s.patientName(), s.samplePosition(),
                    s.rawData(), s.receivedAt());
            case FujifilmErrorMessage e -> new FujifilmErrorMessage(
                    detectedType, e.command(), e.errorData(), e.rawData(), e.receivedAt());
            default -> parsed;
        };
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        for (AnalyzerMessageListener listener : listeners) {
            listener.onSessionEnd(session.sessionId());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Error in Fujifilm handler for session {}", session.sessionId(), cause);
        ctx.close();
    }
}
