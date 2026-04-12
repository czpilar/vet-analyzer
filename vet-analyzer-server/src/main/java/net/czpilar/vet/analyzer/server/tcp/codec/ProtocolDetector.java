package net.czpilar.vet.analyzer.server.tcp.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import net.czpilar.vet.analyzer.core.parser.MessageParserRegistry;
import net.czpilar.vet.analyzer.core.protocol.hl7.Hl7Protocol;
import net.czpilar.vet.analyzer.server.session.Session;
import net.czpilar.vet.analyzer.server.session.SessionManager;
import net.czpilar.vet.analyzer.server.tcp.handler.FujifilmChannelHandler;
import net.czpilar.vet.analyzer.server.tcp.handler.Hl7ChannelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * Detects the protocol from the first bytes of incoming data and dynamically
 * configures the Netty pipeline with the appropriate decoder and handler.
 */
public class ProtocolDetector extends ByteToMessageDecoder {

    private static final Logger log = LoggerFactory.getLogger(ProtocolDetector.class);

    private final SessionManager sessionManager;
    private final MessageParserRegistry parserRegistry;

    public ProtocolDetector(SessionManager sessionManager, MessageParserRegistry parserRegistry) {
        this.sessionManager = sessionManager;
        this.parserRegistry = parserRegistry;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        if (in.readableBytes() < 4) {
            return; // Wait for more data
        }

        String remoteAddress = ((InetSocketAddress) ctx.channel().remoteAddress()).getAddress().getHostAddress();
        Session session = sessionManager.createSession(remoteAddress);

        // Peek at first bytes without consuming them
        byte[] peek = new byte[Math.min(4, in.readableBytes())];
        in.getBytes(in.readerIndex(), peek);

        var pipeline = ctx.pipeline();

        if (Hl7Protocol.isHl7(peek)) {
            log.info("Detected HL7/MLLP protocol from {}", remoteAddress);
            pipeline.addLast("mllpDecoder", new MllpDecoder());
            pipeline.addLast("mllpEncoder", new MllpEncoder());
            pipeline.addLast("handler", new Hl7ChannelHandler(session, parserRegistry));
        } else {
            log.info("Detected Fujifilm protocol from {}", remoteAddress);
            pipeline.addLast("stxEtxDecoder", new StxEtxDecoder());
            pipeline.addLast("handler", new FujifilmChannelHandler(session, parserRegistry));
        }

        // Remove this detector and replay the buffered data
        pipeline.remove(this);
    }
}
