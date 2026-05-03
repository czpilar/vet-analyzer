package net.czpilar.vet.analyzer.starter.config

import net.czpilar.vet.analyzer.core.listener.AnalyzerMessageListener
import net.czpilar.vet.analyzer.core.parser.MessageParserRegistry
import spock.lang.Specification

import java.net.ServerSocket

class VetAnalyzerServerLifecycleTest extends Specification {

    private VetAnalyzerProperties properties
    private MessageParserRegistry registry
    private List<AnalyzerMessageListener> listeners
    private VetAnalyzerServerLifecycle lifecycle

    def setup() {
        properties = new VetAnalyzerProperties(
                port: pickFreePort(),
                idleTimeoutSeconds: 300,
                autoStart: false
        )
        registry = new MessageParserRegistry([])
        listeners = []
        lifecycle = new VetAnalyzerServerLifecycle(properties, registry, listeners)
    }

    def cleanup() {
        if (lifecycle.isRunning()) {
            lifecycle.stop()
        }
    }

    def "lifecycle is not running before start"() {
        expect:
        !lifecycle.isRunning()
    }

    def "start binds to configured port and isRunning becomes true"() {
        when:
        lifecycle.start()

        then:
        lifecycle.isRunning()
    }

    def "stop releases the port and isRunning becomes false"() {
        given:
        lifecycle.start()
        assert lifecycle.isRunning()

        when:
        lifecycle.stop()

        then:
        !lifecycle.isRunning()
    }

    def "isAutoStartup follows properties.autoStart"() {
        given:
        properties.autoStart = autoStart

        expect:
        lifecycle.isAutoStartup() == autoStart

        where:
        autoStart << [true, false]
    }

    def "getPhase returns Integer.MAX_VALUE - 1 (start late, stop early)"() {
        expect:
        lifecycle.getPhase() == Integer.MAX_VALUE - 1
    }

    def "start with already-bound port throws RuntimeException and lifecycle stays not running"() {
        given:
        // Pre-bind the port to provoke a bind failure
        def hog = new ServerSocket(properties.port)

        when:
        lifecycle.start()

        then:
        thrown(RuntimeException)
        !lifecycle.isRunning()

        cleanup:
        hog.close()
    }

    private static int pickFreePort() {
        new ServerSocket(0).withCloseable { socket ->
            return socket.getLocalPort()
        }
    }
}
