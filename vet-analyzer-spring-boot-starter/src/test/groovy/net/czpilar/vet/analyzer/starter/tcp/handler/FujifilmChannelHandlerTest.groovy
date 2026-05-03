package net.czpilar.vet.analyzer.starter.tcp.handler

import io.netty.channel.embedded.EmbeddedChannel
import net.czpilar.vet.analyzer.core.listener.AnalyzerMessageListener
import net.czpilar.vet.analyzer.core.model.AnalyzerMessage
import net.czpilar.vet.analyzer.core.model.AnalyzerType
import net.czpilar.vet.analyzer.core.model.fujifilm.FujifilmErrorMessage
import net.czpilar.vet.analyzer.core.model.fujifilm.FujifilmStartMessage
import net.czpilar.vet.analyzer.core.parser.MessageParserRegistry
import net.czpilar.vet.analyzer.core.protocol.fujifilm.FujifilmCommand
import spock.lang.Specification

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

class FujifilmChannelHandlerTest extends Specification {

    private static final String SESSION_ID = "session-1"
    private static final String REMOTE = "192.168.1.10:54321"

    private MessageParserRegistry registry
    private AnalyzerMessageListener listener
    private FujifilmChannelHandler handler
    private EmbeddedChannel channel

    def setup() {
        registry = Mock(MessageParserRegistry)
        listener = Mock(AnalyzerMessageListener)
        handler = new FujifilmChannelHandler(SESSION_ID, REMOTE, registry, [listener])
        channel = new EmbeddedChannel(handler)
    }

    def "parsed message is delivered to listener via onMessage"() {
        given:
        def msg = "RAW"
        def parsed = startMsg(AnalyzerType.NX600, FujifilmCommand.R)

        when:
        channel.writeInbound(msg)

        then:
        1 * registry.parse(msg) >> parsed
        1 * listener.onMessage(parsed, msg, REMOTE)
        0 * listener._

        cleanup:
        channel.finish()
    }

    def "unparseable message is delivered as raw"() {
        given:
        def msg = "garbage"

        when:
        channel.writeInbound(msg)

        then:
        1 * registry.parse(msg) >> null
        1 * listener.onRawMessage(msg, REMOTE)
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

    def "AU20V-specific command (T/X/Y) sets detected type and re-tags later S parsed as NX600"() {
        given:
        // Shared S happens to be picked up by Nx600StartParser first -> tagged NX600
        // Then specific T/X/Y arrives -> detection kicks in
        // Then a third S still tagged NX600 -> should be re-tagged to AU20V
        def s1 = startMsg(AnalyzerType.NX600, FujifilmCommand.S)
        def specific = startMsg(AnalyzerType.AU20V, command)
        def s2 = startMsg(AnalyzerType.NX600, FujifilmCommand.S)

        when:
        channel.writeInbound("s1")
        channel.writeInbound("spec")
        channel.writeInbound("s2")

        then:
        1 * registry.parse("s1") >> s1
        1 * registry.parse("spec") >> specific
        1 * registry.parse("s2") >> s2
        // s1 keeps original type (no detection yet)
        1 * listener.onMessage({ it.analyzerType() == AnalyzerType.NX600 }, "s1", REMOTE)
        // specific message keeps its own type (matches detected after update)
        1 * listener.onMessage(specific, "spec", REMOTE)
        // s2 gets re-tagged AU20V
        1 * listener.onMessage({ it ->
            it instanceof FujifilmStartMessage && it.analyzerType() == AnalyzerType.AU20V
        }, "s2", REMOTE)

        cleanup:
        channel.finish()

        where:
        command << [FujifilmCommand.T, FujifilmCommand.X, FujifilmCommand.Y]
    }

    def "NX600-specific command (R/I/W) sets detected type and re-tags later E parsed as AU20V"() {
        given:
        def specific = startMsg(AnalyzerType.NX600, command)
        def err = errorMsg(AnalyzerType.AU20V, FujifilmCommand.E)

        when:
        channel.writeInbound("spec")
        channel.writeInbound("err")

        then:
        1 * registry.parse("spec") >> specific
        1 * registry.parse("err") >> err
        1 * listener.onMessage(specific, "spec", REMOTE)
        // E re-tagged from AU20V -> NX600
        1 * listener.onMessage({ it ->
            it instanceof FujifilmErrorMessage && it.analyzerType() == AnalyzerType.NX600
        }, "err", REMOTE)

        cleanup:
        channel.finish()

        where:
        command << [FujifilmCommand.R, FujifilmCommand.I, FujifilmCommand.W]
    }

    def "without prior detection, message keeps its original analyzerType"() {
        given:
        def shared = startMsg(AnalyzerType.NX600, FujifilmCommand.S)

        when:
        channel.writeInbound("only")

        then:
        1 * registry.parse("only") >> shared
        1 * listener.onMessage({ it.analyzerType() == AnalyzerType.NX600 }, "only", REMOTE)

        cleanup:
        channel.finish()
    }

    def "shared S/E commands alone do NOT update detectedType (no re-tagging)"() {
        given:
        def s = startMsg(AnalyzerType.NX600, FujifilmCommand.S)
        def err = errorMsg(AnalyzerType.AU20V, FujifilmCommand.E)

        when:
        channel.writeInbound("a")
        channel.writeInbound("b")

        then:
        1 * registry.parse("a") >> s
        1 * registry.parse("b") >> err
        1 * listener.onMessage(s, "a", REMOTE)
        // 'err' keeps its own AU20V type since no specific command set detectedType
        1 * listener.onMessage({ it.analyzerType() == AnalyzerType.AU20V }, "b", REMOTE)

        cleanup:
        channel.finish()
    }

    private static FujifilmStartMessage startMsg(AnalyzerType type, FujifilmCommand cmd) {
        return new FujifilmStartMessage(
                type, cmd, "01", LocalDate.of(2026, 5, 3), LocalTime.of(10, 0),
                "S001", "P001", "Rex", 1, "raw", Instant.parse("2026-05-03T10:00:00Z"))
    }

    private static FujifilmErrorMessage errorMsg(AnalyzerType type, FujifilmCommand cmd) {
        return new FujifilmErrorMessage(type, cmd, "data", "raw", Instant.parse("2026-05-03T10:00:00Z"))
    }
}
