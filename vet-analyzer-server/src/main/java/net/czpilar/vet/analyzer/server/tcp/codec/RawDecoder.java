package net.czpilar.vet.analyzer.server.tcp.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Decoder for unknown protocols - passes through all received data as strings.
 */
public class RawDecoder extends ByteToMessageDecoder {

    private static final Logger log = LoggerFactory.getLogger(RawDecoder.class);

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        if (in.readableBytes() == 0) {
            return;
        }

        byte[] data = new byte[in.readableBytes()];
        in.readBytes(data);

        String message = new String(data, StandardCharsets.ISO_8859_1);
        log.debug("Decoded raw message ({} bytes)", data.length);
        out.add(message);
    }
}
