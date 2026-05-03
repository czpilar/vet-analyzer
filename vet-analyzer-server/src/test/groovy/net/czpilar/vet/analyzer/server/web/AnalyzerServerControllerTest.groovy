package net.czpilar.vet.analyzer.server.web

import net.czpilar.vet.analyzer.starter.config.VetAnalyzerServerLifecycle
import spock.lang.Specification

class AnalyzerServerControllerTest extends Specification {

    private VetAnalyzerServerLifecycle lifecycle = Mock(VetAnalyzerServerLifecycle)
    private AnalyzerServerController controller = new AnalyzerServerController(lifecycle)

    def "status returns running flag"() {
        when:
        def result = controller.status()

        then:
        result == [running: running]
        1 * lifecycle.isRunning() >> running

        where:
        running << [true, false]
    }

    def "start triggers lifecycle when not running"() {
        when:
        def result = controller.start()

        then:
        result == [running: true, message: "Started"]
        1 * lifecycle.isRunning() >> false
        1 * lifecycle.start()
        0 * lifecycle._
    }

    def "start does nothing when already running"() {
        when:
        def result = controller.start()

        then:
        result == [running: true, message: "Already running"]
        1 * lifecycle.isRunning() >> true
        0 * lifecycle.start()
        0 * lifecycle._
    }

    def "stop triggers lifecycle when running"() {
        when:
        def result = controller.stop()

        then:
        result == [running: false, message: "Stopped"]
        1 * lifecycle.isRunning() >> true
        1 * lifecycle.stop()
        0 * lifecycle._
    }

    def "stop does nothing when already stopped"() {
        when:
        def result = controller.stop()

        then:
        result == [running: false, message: "Already stopped"]
        1 * lifecycle.isRunning() >> false
        0 * lifecycle.stop()
        0 * lifecycle._
    }
}
