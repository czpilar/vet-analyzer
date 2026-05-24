package net.czpilar.vet.analyzer.server.config

import net.czpilar.vet.analyzer.core.listener.AnalyzerMessageListener
import net.czpilar.vet.analyzer.core.session.SessionFileListener
import net.czpilar.vet.analyzer.server.listener.SessionJsonListener
import spock.lang.Specification
import spock.lang.TempDir
import tools.jackson.databind.json.JsonMapper

import java.nio.file.Path

class TcpServerConfigTest extends Specification {

    @TempDir
    Path tempDir

    def "sessionFileListener bean is created from session directory property"() {
        given:
        def config = new TcpServerConfig()
        def properties = new SessionProperties(directory: "./test-sessions")

        when:
        def listener = config.sessionFileListener(properties)

        then:
        listener != null
        listener instanceof AnalyzerMessageListener
        listener instanceof SessionFileListener
    }

    def "sessionJsonListener bean is created from session directory property"() {
        given:
        def config = new TcpServerConfig()
        def properties = new SessionProperties(directory: tempDir.toString())
        def mapper = JsonMapper.builder().build()

        when:
        def listener = config.sessionJsonListener(properties, mapper)

        then:
        listener != null
        listener instanceof AnalyzerMessageListener
        listener instanceof SessionJsonListener
    }
}
