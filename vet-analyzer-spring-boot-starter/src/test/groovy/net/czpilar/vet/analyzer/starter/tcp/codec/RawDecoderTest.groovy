package net.czpilar.vet.analyzer.starter.tcp.codec

import io.netty.buffer.Unpooled
import io.netty.channel.embedded.EmbeddedChannel
import spock.lang.Specification

import java.nio.charset.StandardCharsets

class RawDecoderTest extends Specification {

    def "data is decoded as ISO-8859-1 string"() {
        given:
        def channel = new EmbeddedChannel(new RawDecoder())
        def payload = "anything goes".getBytes(StandardCharsets.ISO_8859_1)

        when:
        channel.writeInbound(Unpooled.wrappedBuffer(payload))

        then:
        channel.readInbound() == "anything goes"
        !channel.finish()
    }

    def "binary bytes are preserved via ISO-8859-1 (no replacement)"() {
        given:
        def channel = new EmbeddedChannel(new RawDecoder())
        def payload = [0x00, 0x7F, (byte) 0xFF, 0x41] as byte[]

        when:
        channel.writeInbound(Unpooled.wrappedBuffer(payload))

        then:
        def msg = channel.readInbound() as String
        msg.length() == 4
        msg.charAt(3) == 'A'
        !channel.finish()
    }

    def "empty buffer produces no output"() {
        given:
        def channel = new EmbeddedChannel(new RawDecoder())

        when:
        channel.writeInbound(Unpooled.EMPTY_BUFFER)

        then:
        channel.readInbound() == null
        !channel.finish()
    }
}
