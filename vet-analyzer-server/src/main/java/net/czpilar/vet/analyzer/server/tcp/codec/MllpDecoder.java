package net.czpilar.vet.analyzer.server.tcp.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import net.czpilar.vet.analyzer.core.protocol.ControlCharacters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Decodes MLLP-framed HL7 messages.
 * MLLP frame: VT (0x0B) + HL7 message + FS (0x1C) + CR (0x0D)
 */
public class MllpDecoder extends ByteToMessageDecoder {

    private static final Logger log = LoggerFactory.getLogger(MllpDecoder.class);

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        // Look for MLLP_END (FS) followed by CR
        int startIndex = in.readerIndex();

        // Skip MLLP_START if present
        if (in.readableBytes() > 0 && in.getByte(startIndex) == ControlCharacters.MLLP_START) {
            startIndex++;
        }

        // Find MLLP_END
        for (int i = startIndex; i < in.writerIndex() - 1; i++) {
            if (in.getByte(i) == ControlCharacters.MLLP_END) {
                // Extract message between start and MLLP_END
                int messageStart = in.readerIndex();
                if (in.getByte(messageStart) == ControlCharacters.MLLP_START) {
                    messageStart++;
                }

                int messageLength = i - messageStart;
                byte[] messageBytes = new byte[messageLength];
                in.getBytes(messageStart, messageBytes);

                // Skip past MLLP_END + optional CR
                int endPos = i + 1;
                if (endPos < in.writerIndex() && in.getByte(endPos) == ControlCharacters.CR) {
                    endPos++;
                }
                in.readerIndex(endPos);

                String message = new String(messageBytes, StandardCharsets.UTF_8);
                log.debug("Decoded MLLP message ({} bytes)", messageLength);
                out.add(message);
                return;
            }
        }

        // If no MLLP framing found, try to read as plain HL7 (delimited by newlines)
        // This handles cases where MLLP framing is absent
        if (in.readableBytes() > 4) {
            byte[] allBytes = new byte[in.readableBytes()];
            in.getBytes(in.readerIndex(), allBytes);
            String content = new String(allBytes, StandardCharsets.UTF_8);
            if (content.contains("MSH|")) {
                in.readerIndex(in.writerIndex());
                log.debug("Decoded plain HL7 message ({} bytes)", allBytes.length);
                out.add(content.trim());
            }
        }
    }
}
