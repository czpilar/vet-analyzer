package net.czpilar.vet.analyzer.starter.config

import spock.lang.Specification

class VetAnalyzerPropertiesTest extends Specification {

    def "default values"() {
        when:
        def props = new VetAnalyzerProperties()

        then:
        props.enabled
        props.autoStart
        props.port == 9012
        props.idleTimeoutSeconds == 300
    }

    def "getters and setters"() {
        given:
        def props = new VetAnalyzerProperties()

        when:
        props.enabled = enabled
        props.autoStart = autoStart
        props.port = port
        props.idleTimeoutSeconds = idleTimeoutSeconds

        then:
        props.enabled == enabled
        props.autoStart == autoStart
        props.port == port
        props.idleTimeoutSeconds == idleTimeoutSeconds

        where:
        enabled | autoStart | port  | idleTimeoutSeconds
        false   | false     | 0     | 0
        true    | true      | 9012  | 300
        false   | true      | 8087  | 600
    }
}
