package net.czpilar.vet.analyzer.starter.tcp.codec

import io.netty.buffer.Unpooled
import io.netty.channel.embedded.EmbeddedChannel
import net.czpilar.vet.analyzer.core.protocol.ControlCharacters
import spock.lang.Specification

import java.nio.charset.StandardCharsets

class StxEtxDecoderTest extends Specification {

    private static final byte STX = ControlCharacters.STX
    private static final byte ETX = ControlCharacters.ETX
    private static final byte BCC = (byte) 0x42  // any byte value, decoder ignores BCC content

    def "framed STX...ETX BCC payload is decoded as ISO-8859-1 string"() {
        given:
        def channel = new EmbeddedChannel(new StxEtxDecoder())
        def frame = concat(bytes(STX), "HELLO".getBytes(StandardCharsets.ISO_8859_1), bytes(ETX, BCC))

        when:
        channel.writeInbound(Unpooled.wrappedBuffer(frame))

        then:
        channel.readInbound() == "HELLO"
        channel.readInbound() == null
        !channel.finish()
    }

    def "incomplete frame (no ETX) waits for more data"() {
        given:
        def channel = new EmbeddedChannel(new StxEtxDecoder())
        def partial = concat(bytes(STX), "HELL".getBytes(StandardCharsets.ISO_8859_1))

        when:
        channel.writeInbound(Unpooled.wrappedBuffer(partial))

        then:
        channel.readInbound() == null
        !channel.finish()
    }

    def "incomplete frame (no BCC after ETX) waits for one more byte"() {
        given:
        def channel = new EmbeddedChannel(new StxEtxDecoder())
        def partial = concat(bytes(STX), "HI".getBytes(StandardCharsets.ISO_8859_1), bytes(ETX))

        when:
        channel.writeInbound(Unpooled.wrappedBuffer(partial))

        then:
        channel.readInbound() == null

        when:
        channel.writeInbound(Unpooled.wrappedBuffer(bytes(BCC)))

        then:
        channel.readInbound() == "HI"
        !channel.finish()
    }

    def "data without leading STX is decoded as unframed message (trimmed)"() {
        given:
        def channel = new EmbeddedChannel(new StxEtxDecoder())
        def data = "  PLAIN  ".getBytes(StandardCharsets.ISO_8859_1)

        when:
        channel.writeInbound(Unpooled.wrappedBuffer(data))

        then:
        channel.readInbound() == "PLAIN"
        !channel.finish()
    }

    def "less than 2 bytes is buffered until more data arrives"() {
        given:
        def channel = new EmbeddedChannel(new StxEtxDecoder())

        when:
        channel.writeInbound(Unpooled.wrappedBuffer(bytes(STX)))

        then:
        channel.readInbound() == null
        !channel.finish()
    }

    private static byte[] bytes(byte... values) {
        return values
    }

    private static byte[] concat(byte[]... arrays) {
        int total = 0
        arrays.each { total += it.length }
        def result = new byte[total]
        int pos = 0
        arrays.each { arr ->
            System.arraycopy(arr, 0, result, pos, arr.length)
            pos += arr.length
        }
        return result
    }
}
