package net.czpilar.vet.analyzer.starter.tcp.codec

import io.netty.buffer.ByteBuf
import io.netty.channel.embedded.EmbeddedChannel
import net.czpilar.vet.analyzer.core.protocol.ControlCharacters
import spock.lang.Specification

import java.nio.charset.StandardCharsets

class MllpEncoderTest extends Specification {

    def "string message is encoded with MLLP framing (VT prefix, FS+CR suffix)"() {
        given:
        def channel = new EmbeddedChannel(new MllpEncoder())
        def msg = "MSH|^~\\&|SENDER||||"

        when:
        channel.writeOutbound(msg)
        ByteBuf buf = channel.readOutbound()

        then:
        buf != null
        def bytes = new byte[buf.readableBytes()]
        buf.readBytes(bytes)
        bytes[0] == ControlCharacters.MLLP_START
        bytes[bytes.length - 2] == ControlCharacters.MLLP_END
        bytes[bytes.length - 1] == ControlCharacters.CR
        // The middle is the original payload bytes
        new String(bytes, 1, bytes.length - 3, StandardCharsets.UTF_8) == msg

        cleanup:
        buf.release()
        channel.finish()
    }
}
