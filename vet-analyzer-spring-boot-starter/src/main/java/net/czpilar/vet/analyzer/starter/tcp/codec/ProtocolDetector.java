package net.czpilar.vet.analyzer.starter.tcp.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import net.czpilar.vet.analyzer.core.listener.AnalyzerMessageListener;
import net.czpilar.vet.analyzer.core.listener.SessionContext;
import net.czpilar.vet.analyzer.core.parser.MessageParserRegistry;
import net.czpilar.vet.analyzer.core.protocol.ControlCharacters;
import net.czpilar.vet.analyzer.core.protocol.hl7.Hl7Protocol;
import net.czpilar.vet.analyzer.starter.tcp.handler.FujifilmChannelHandler;
import net.czpilar.vet.analyzer.starter.tcp.handler.Hl7ChannelHandler;
import net.czpilar.vet.analyzer.starter.tcp.handler.IdleDisconnectHandler;
import net.czpilar.vet.analyzer.starter.tcp.handler.RawChannelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class ProtocolDetector extends ByteToMessageDecoder {

    private static final Logger log = LoggerFactory.getLogger(ProtocolDetector.class);
    private static final DateTimeFormatter SESSION_ID_TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private final MessageParserRegistry parserRegistry;
    private final List<AnalyzerMessageListener> listeners;
    private final int idleTimeoutSeconds;

    private String sessionId;
    private String remoteAddress;
    private SessionContext sessionContext;

    public ProtocolDetector(MessageParserRegistry parserRegistry,
                            List<AnalyzerMessageListener> listeners,
                            int idleTimeoutSeconds) {
        this.parserRegistry = parserRegistry;
        this.listeners = listeners;
        this.idleTimeoutSeconds = idleTimeoutSeconds;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.sessionId = generateSessionId();
        this.remoteAddress = ((InetSocketAddress) ctx.channel().remoteAddress()).getAddress().getHostAddress();
        this.sessionContext = new SessionContext(sessionId, remoteAddress);

        log.info("Client connected: {} (session {})", remoteAddress, sessionId);
        for (AnalyzerMessageListener listener : listeners) {
            listener.onSessionStart(sessionContext);
        }

        // Idle timeout - close connection if no data received (even if no protocol detected yet)
        if (idleTimeoutSeconds > 0) {
            ctx.pipeline().addFirst("idleState", new IdleStateHandler(idleTimeoutSeconds, 0, 0, TimeUnit.SECONDS));
            ctx.pipeline().addAfter("idleState", "idleDisconnect", new IdleDisconnectHandler());
        }

        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // If no protocol was detected (client disconnected without sending data), end session here
        if (sessionId != null && ctx.pipeline().get("handler") == null) {
            log.info("Client disconnected before sending data: {} (session {})", remoteAddress, sessionId);
            for (AnalyzerMessageListener listener : listeners) {
                listener.onSessionEnd(sessionId);
            }
        }
        super.channelInactive(ctx);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        if (in.readableBytes() < 4) {
            return;
        }

        byte[] peek = new byte[Math.min(4, in.readableBytes())];
        in.getBytes(in.readerIndex(), peek);

        ChannelPipeline pipeline = ctx.pipeline();

        if (Hl7Protocol.isHl7(peek)) {
            log.info("Detected HL7/MLLP protocol from {}", remoteAddress);
            pipeline.addLast("mllpDecoder", new MllpDecoder());
            pipeline.addLast("mllpEncoder", new MllpEncoder());
            pipeline.addLast("handler", new Hl7ChannelHandler(sessionContext, parserRegistry, listeners));
        } else if (isFujifilm(peek)) {
            log.info("Detected Fujifilm protocol from {}", remoteAddress);
            pipeline.addLast("stxEtxDecoder", new StxEtxDecoder());
            pipeline.addLast("handler", new FujifilmChannelHandler(sessionContext, parserRegistry, listeners));
        } else {
            log.warn("Unknown protocol from {}, logging raw data", remoteAddress);
            pipeline.addLast("rawDecoder", new RawDecoder());
            pipeline.addLast("handler", new RawChannelHandler(sessionContext, listeners));
        }

        pipeline.remove(this);
    }

    /**
     * Session id format: {@code yyyyMMdd-HHmmss-xxxx} (20 chars).
     * Timestamp prefix is human-readable; 4-hex random suffix avoids collisions
     * when multiple connections arrive within the same second.
     */
    static String generateSessionId() {
        String timestamp = LocalDateTime.now().format(SESSION_ID_TIMESTAMP_FORMAT);
        int random = ThreadLocalRandom.current().nextInt(0x10000);
        return timestamp + "-" + String.format("%04x", random);
    }

    private static boolean isFujifilm(byte[] peek) {
        if (peek[0] == ControlCharacters.STX) {
            return true;
        }
        if (peek.length >= 2 && peek[1] == ',') {
            char cmd = (char) peek[0];
            return cmd == 'R' || cmd == 'I' || cmd == 'W' || cmd == 'S' || cmd == 'E'
                    || cmd == 'T' || cmd == 'X' || cmd == 'Y';
        }
        return false;
    }
}
