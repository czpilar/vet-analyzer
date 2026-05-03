package net.czpilar.vet.analyzer.server.config

import spock.lang.Specification

class SessionPropertiesTest extends Specification {

    def "default directory is ./sessions"() {
        when:
        def props = new SessionProperties()

        then:
        props.directory == "./sessions"
    }

    def "directory getter and setter"() {
        given:
        def props = new SessionProperties()

        when:
        props.directory = directory

        then:
        props.directory == directory

        where:
        directory << [null, "", "/var/log/sessions", "C:\\sessions"]
    }
}
