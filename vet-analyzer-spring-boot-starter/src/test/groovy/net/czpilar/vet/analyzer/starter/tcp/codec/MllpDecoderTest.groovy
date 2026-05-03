package net.czpilar.vet.analyzer.starter.tcp.codec

import io.netty.buffer.Unpooled
import io.netty.channel.embedded.EmbeddedChannel
import net.czpilar.vet.analyzer.core.protocol.ControlCharacters
import spock.lang.Specification

import java.nio.charset.StandardCharsets

class MllpDecoderTest extends Specification {

    private static final byte VT = ControlCharacters.MLLP_START   // 0x0B
    private static final byte FS = ControlCharacters.MLLP_END     // 0x1C
    private static final byte CR = ControlCharacters.CR           // 0x0D

    def "MLLP-framed message (VT...FS CR) is decoded"() {
        given:
        def channel = new EmbeddedChannel(new MllpDecoder())
        def payload = "MSH|^~\\&|SENDER|||||||MSG^A01||P|2.7".getBytes(StandardCharsets.UTF_8)
        def frame = concat(bytes(VT), payload, bytes(FS, CR))

        when:
        channel.writeInbound(Unpooled.wrappedBuffer(frame))

        then:
        channel.readInbound() == new String(payload, StandardCharsets.UTF_8)
        !channel.finish()
    }

    def "MLLP-framed message (VT...FS) without trailing CR is still decoded"() {
        given:
        def channel = new EmbeddedChannel(new MllpDecoder())
        def payload = "MSH|^~\\&|".getBytes(StandardCharsets.UTF_8)
        def frame = concat(bytes(VT), payload, bytes(FS))
        // pad with one extra byte so writerIndex - 1 > position of FS, exposing it to the loop
        def withTail = concat(frame, bytes((byte) 0x00))

        when:
        channel.writeInbound(Unpooled.wrappedBuffer(withTail))

        then:
        channel.readInbound() == new String(payload, StandardCharsets.UTF_8)
        !channel.finish()
    }

    def "data without MLLP framing but containing 'MSH|' is decoded as plain HL7"() {
        given:
        def channel = new EmbeddedChannel(new MllpDecoder())
        def hl7 = "MSH|^~\\&|SENDER|||||||MSG^A01||P|2.7\r".getBytes(StandardCharsets.UTF_8)

        when:
        channel.writeInbound(Unpooled.wrappedBuffer(hl7))

        then:
        channel.readInbound() == "MSH|^~\\&|SENDER|||||||MSG^A01||P|2.7"
        !channel.finish()
    }

    def "tiny buffer (<=4 bytes) without 'MSH|' is buffered"() {
        given:
        def channel = new EmbeddedChannel(new MllpDecoder())

        when:
        channel.writeInbound(Unpooled.wrappedBuffer("abc".getBytes(StandardCharsets.UTF_8)))

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
