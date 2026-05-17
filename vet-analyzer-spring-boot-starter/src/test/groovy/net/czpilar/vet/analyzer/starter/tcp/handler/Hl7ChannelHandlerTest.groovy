package net.czpilar.vet.analyzer.starter.tcp.handler

import io.netty.channel.embedded.EmbeddedChannel
import net.czpilar.vet.analyzer.core.listener.AnalyzerMessageListener
import net.czpilar.vet.analyzer.core.listener.SessionContext
import net.czpilar.vet.analyzer.core.model.AnalyzerType
import net.czpilar.vet.analyzer.core.model.hl7.Hl7Message
import net.czpilar.vet.analyzer.core.parser.MessageParserRegistry
import spock.lang.Specification

import java.time.Instant
import java.time.LocalDateTime

class Hl7ChannelHandlerTest extends Specification {

    private static final String SESSION_ID = "session-1"
    private static final String REMOTE = "192.168.1.10:54321"
    private static final SessionContext CTX = new SessionContext(SESSION_ID, REMOTE)

    private MessageParserRegistry registry
    private AnalyzerMessageListener listener
    private Hl7ChannelHandler handler
    private EmbeddedChannel channel

    def setup() {
        registry = Mock(MessageParserRegistry)
        listener = Mock(AnalyzerMessageListener)
        handler = new Hl7ChannelHandler(CTX, registry, [listener])
        channel = new EmbeddedChannel(handler)
    }

    def "HL7 message is parsed, listener is notified, and ACK is sent back"() {
        given:
        def msg = "MSH|..."
        def parsed = hl7Message("MSGCTL-123")

        when:
        channel.writeInbound(msg)

        then:
        1 * registry.parse(msg) >> parsed
        1 * listener.onMessage(parsed, msg, CTX)
        0 * listener._

        and:
        def ack = channel.readOutbound() as String
        ack != null
        ack.contains("MSGCTL-123")

        cleanup:
        channel.finish()
    }

    def "unparseable HL7 message goes to onRawMessage and no ACK is sent"() {
        given:
        def msg = "junk"

        when:
        channel.writeInbound(msg)

        then:
        1 * registry.parse(msg) >> null
        1 * listener.onRawMessage(msg, CTX)
        0 * listener._

        and:
        channel.readOutbound() == null

        cleanup:
        channel.finish()
    }

    def "channelInactive notifies listener of session end"() {
        when:
        channel.close()

        then:
        1 * listener.onSessionEnd(SESSION_ID)
    }

    private static Hl7Message hl7Message(String controlId) {
        return new Hl7Message(AnalyzerType.BM850_EXIGO, controlId, "ORU^R01", "2.7", "BM850",
                LocalDateTime.of(2026, 5, 3, 10, 0), "S1", "USI", LocalDateTime.of(2026, 5, 3, 10, 0),
                "comment", [], "raw", Instant.parse("2026-05-03T10:00:00Z"))
    }
}
