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

    def "generateSessionId returns mostly unique values across many calls"() {
        when:
        // Session id = timestamp(yyyyMMdd-HHmmss) + 4-hex random = 65536 buckets.
        // Calls within the same second share a timestamp; only the 4-hex random part disambiguates.
        // Birthday paradox for n samples in 65536 buckets:
        //   expected collisions ~ n^2 / (2 * 65536)
        //   200 samples -> ~0.3 expected; 500 -> ~1.9; 1000 -> ~7.6 (and previously flaky at >=990)
        def ids = (1..200).collect { ProtocolDetector.generateSessionId() } as Set

        then:
        // Tolerance generous enough to be statistically stable.
        ids.size() >= 195
    }
}
