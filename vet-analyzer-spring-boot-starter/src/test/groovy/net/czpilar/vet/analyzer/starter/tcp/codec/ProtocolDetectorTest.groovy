package net.czpilar.vet.analyzer.starter.tcp.codec

import spock.lang.Specification

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ProtocolDetectorTest extends Specification {

    def "generateSessionId has format yyyyMMdd-HHmmss-xxxx"() {
        when:
        def id = ProtocolDetector.generateSessionId()

        then:
        id ==~ /\d{8}-\d{6}-[0-9a-f]{4}/
    }

    def "generateSessionId timestamp matches current time"() {
        given:
        def before = LocalDateTime.now()

        when:
        def id = ProtocolDetector.generateSessionId()
        def after = LocalDateTime.now()

        then:
        def timestampPart = id.substring(0, 15)
        def parsed = LocalDateTime.parse(timestampPart, DateTimeFormatter.ofPattern('yyyyMMdd-HHmmss'))
        !parsed.isBefore(before.withNano(0))
        !parsed.isAfter(after.withNano(0).plusSeconds(1))
    }

    def "generateSessionId returns unique values across many calls"() {
        when:
        def ids = (1..1000).collect { ProtocolDetector.generateSessionId() } as Set

        then:
        // 4-hex random part = 65536 variants, 1000 calls → collision probability ~0.7%
        // expect at least 990 unique (very generous, prevents flaky test)
        ids.size() >= 990
    }
}
