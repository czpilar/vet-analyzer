package net.czpilar.vet.analyzer.starter.tcp.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import net.czpilar.vet.analyzer.core.protocol.ControlCharacters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Decodes Fujifilm STX/ETX framed messages and unframed (Type 3) messages.
 */
public class StxEtxDecoder extends ByteToMessageDecoder {

    private static final Logger log = LoggerFactory.getLogger(StxEtxDecoder.class);

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        if (in.readableBytes() < 2) {
            return;
        }

        byte firstByte = in.getByte(in.readerIndex());

        if (firstByte == ControlCharacters.STX) {
            // Framed message: STX ... ETX BCC
            decodeFramed(in, out);
        } else {
            // Unframed message (Type 3 or plain text)
            decodeUnframed(in, out);
        }
    }

    private void decodeFramed(ByteBuf in, List<Object> out) {
        // Find ETX
        int startIndex = in.readerIndex();
        for (int i = startIndex + 1; i < in.writerIndex(); i++) {
            if (in.getByte(i) == ControlCharacters.ETX) {
                // Need one more byte for BCC
                if (i + 1 >= in.writerIndex()) {
                    return; // Wait for BCC byte
                }

                // Extract payload between STX and ETX
                int payloadLength = i - startIndex - 1;
                byte[] payload = new byte[payloadLength];
                in.getBytes(startIndex + 1, payload);

                // Skip STX + payload + ETX + BCC
                in.readerIndex(i + 2);

                String message = new String(payload, StandardCharsets.ISO_8859_1);
                log.debug("Decoded STX/ETX framed message ({} bytes)", payloadLength);
                out.add(message);
                return;
            }
        }
        // ETX not found yet, wait for more data
    }

    private void decodeUnframed(ByteBuf in, List<Object> out) {
        // Read all available data as a single message
        // Unframed messages (Type 3) are terminated by connection close
        byte[] data = new byte[in.readableBytes()];
        in.readBytes(data);

        String message = new String(data, StandardCharsets.ISO_8859_1).trim();
        if (!message.isEmpty()) {
            log.debug("Decoded unframed message ({} bytes)", data.length);
            out.add(message);
        }
    }
}
