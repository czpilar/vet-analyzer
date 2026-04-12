package net.czpilar.vet.analyzer.starter.tcp.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import net.czpilar.vet.analyzer.core.listener.AnalyzerMessageListener;
import net.czpilar.vet.analyzer.core.parser.MessageParserRegistry;
import net.czpilar.vet.analyzer.core.protocol.ControlCharacters;
import net.czpilar.vet.analyzer.core.protocol.hl7.Hl7Protocol;
import net.czpilar.vet.analyzer.starter.tcp.handler.FujifilmChannelHandler;
import net.czpilar.vet.analyzer.starter.tcp.handler.Hl7ChannelHandler;
import net.czpilar.vet.analyzer.starter.tcp.handler.RawChannelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.UUID;

public class ProtocolDetector extends ByteToMessageDecoder {

    private static final Logger log = LoggerFactory.getLogger(ProtocolDetector.class);

    private final MessageParserRegistry parserRegistry;
    private final List<AnalyzerMessageListener> listeners;

    public ProtocolDetector(MessageParserRegistry parserRegistry, List<AnalyzerMessageListener> listeners) {
        this.parserRegistry = parserRegistry;
        this.listeners = listeners;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        if (in.readableBytes() < 4) {
            return;
        }

        String remoteAddress = ((InetSocketAddress) ctx.channel().remoteAddress()).getAddress().getHostAddress();
        String sessionId = UUID.randomUUID().toString().substring(0, 8);

        for (var listener : listeners) {
            listener.onSessionStart(sessionId, remoteAddress);
        }

        byte[] peek = new byte[Math.min(4, in.readableBytes())];
        in.getBytes(in.readerIndex(), peek);

        var pipeline = ctx.pipeline();

        if (Hl7Protocol.isHl7(peek)) {
            log.info("Detected HL7/MLLP protocol from {}", remoteAddress);
            pipeline.addLast("mllpDecoder", new MllpDecoder());
            pipeline.addLast("mllpEncoder", new MllpEncoder());
            pipeline.addLast("handler", new Hl7ChannelHandler(sessionId, remoteAddress, parserRegistry, listeners));
        } else if (isFujifilm(peek)) {
            log.info("Detected Fujifilm protocol from {}", remoteAddress);
            pipeline.addLast("stxEtxDecoder", new StxEtxDecoder());
            pipeline.addLast("handler", new FujifilmChannelHandler(sessionId, remoteAddress, parserRegistry, listeners));
        } else {
            log.warn("Unknown protocol from {}, logging raw data", remoteAddress);
            pipeline.addLast("rawDecoder", new RawDecoder());
            pipeline.addLast("handler", new RawChannelHandler(sessionId, remoteAddress, listeners));
        }

        pipeline.remove(this);
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
