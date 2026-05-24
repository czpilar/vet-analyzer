package net.czpilar.vet.analyzer.server.web

import net.czpilar.vet.analyzer.server.dto.AnalyzerServerStatus
import net.czpilar.vet.analyzer.starter.config.VetAnalyzerServerLifecycle
import spock.lang.Specification

class AnalyzerServerControllerTest extends Specification {

    private VetAnalyzerServerLifecycle lifecycle = Mock(VetAnalyzerServerLifecycle)
    private AnalyzerServerController controller = new AnalyzerServerController(lifecycle)

    def "status returns running flag"() {
        when:
        def result = controller.status()

        then:
        result == new AnalyzerServerStatus(running, null)
        1 * lifecycle.isRunning() >> running

        where:
        running << [true, false]
    }

    def "start triggers lifecycle when not running"() {
        when:
        def result = controller.start()

        then:
        result == new AnalyzerServerStatus(true, "Started")
        1 * lifecycle.isRunning() >> false
        1 * lifecycle.start()
        0 * lifecycle._
    }

    def "start does nothing when already running"() {
        when:
        def result = controller.start()

        then:
        result == new AnalyzerServerStatus(true, "Already running")
        1 * lifecycle.isRunning() >> true
        0 * lifecycle.start()
        0 * lifecycle._
    }

    def "stop triggers lifecycle when running"() {
        when:
        def result = controller.stop()

        then:
        result == new AnalyzerServerStatus(false, "Stopped")
        1 * lifecycle.isRunning() >> true
        1 * lifecycle.stop()
        0 * lifecycle._
    }

    def "stop does nothing when already stopped"() {
        when:
        def result = controller.stop()

        then:
        result == new AnalyzerServerStatus(false, "Already stopped")
        1 * lifecycle.isRunning() >> false
        0 * lifecycle.stop()
        0 * lifecycle._
    }

    def "restart stops then starts when running"() {
        when:
        def result = controller.restart()

        then:
        result == new AnalyzerServerStatus(true, "Restarted")
        1 * lifecycle.isRunning() >> true
        then:
        1 * lifecycle.stop()
        then:
        1 * lifecycle.start()
        0 * lifecycle._
    }

    def "restart only starts when not running"() {
        when:
        def result = controller.restart()

        then:
        result == new AnalyzerServerStatus(true, "Restarted")
        1 * lifecycle.isRunning() >> false
        1 * lifecycle.start()
        0 * lifecycle._
    }
}
