package net.czpilar.vet.analyzer.starter.tcp.handler

import io.netty.channel.embedded.EmbeddedChannel
import net.czpilar.vet.analyzer.core.listener.AnalyzerMessageListener
import spock.lang.Specification

class RawChannelHandlerTest extends Specification {

    private static final String SESSION_ID = "raw-session"
    private static final String REMOTE = "10.0.0.5:1234"

    private AnalyzerMessageListener listener
    private RawChannelHandler handler
    private EmbeddedChannel channel

    def setup() {
        listener = Mock(AnalyzerMessageListener)
        handler = new RawChannelHandler(SESSION_ID, REMOTE, [listener])
        channel = new EmbeddedChannel(handler)
    }

    def "any inbound message is delivered as raw"() {
        when:
        channel.writeInbound("anything")

        then:
        1 * listener.onRawMessage("anything", REMOTE)
        0 * listener._

        cleanup:
        channel.finish()
    }

    def "channelInactive notifies listener of session end"() {
        when:
        channel.close()

        then:
        1 * listener.onSessionEnd(SESSION_ID)
    }

    def "all listeners receive raw message"() {
        given:
        def listener2 = Mock(AnalyzerMessageListener)
        def multiHandler = new RawChannelHandler(SESSION_ID, REMOTE, [listener, listener2])
        def multiChannel = new EmbeddedChannel(multiHandler)

        when:
        multiChannel.writeInbound("msg")

        then:
        1 * listener.onRawMessage("msg", REMOTE)
        1 * listener2.onRawMessage("msg", REMOTE)

        cleanup:
        multiChannel.finish()
    }
}
