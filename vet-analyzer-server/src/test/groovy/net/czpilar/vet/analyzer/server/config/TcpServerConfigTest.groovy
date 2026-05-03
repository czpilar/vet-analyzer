package net.czpilar.vet.analyzer.server.config

import net.czpilar.vet.analyzer.core.listener.AnalyzerMessageListener
import net.czpilar.vet.analyzer.core.session.SessionFileListener
import spock.lang.Specification

class TcpServerConfigTest extends Specification {

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
}
