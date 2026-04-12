package net.czpilar.vet.analyzer.starter.tcp.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import net.czpilar.vet.analyzer.core.protocol.hl7.Hl7Protocol;

/**
 * Encodes HL7 messages with MLLP framing for transmission.
 */
public class MllpEncoder extends MessageToByteEncoder<String> {

    @Override
    protected void encode(ChannelHandlerContext ctx, String msg, ByteBuf out) {
        byte[] mllpFrame = Hl7Protocol.wrapMllp(msg);
        out.writeBytes(mllpFrame);
    }
}
