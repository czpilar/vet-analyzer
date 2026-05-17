package net.czpilar.vet.analyzer.core.model.hl7

import net.czpilar.vet.analyzer.core.model.AnalyzerType
import spock.lang.Specification

import java.time.Instant
import java.time.LocalDateTime

class Hl7MessageTest extends Specification {

    def "analyzerType is taken from constructor"() {
        given:
        def msg = new Hl7Message(AnalyzerType.BM850_EXIGO, "ID", "ORU^R01", "2.7", "BM850",
                LocalDateTime.now(), "S1", "USI",
                LocalDateTime.now(), "comment", [], "raw", Instant.now())

        expect:
        msg.analyzerType() == AnalyzerType.BM850_EXIGO
    }

    def "messageDescription combines messageType and HL7 version"() {
        given:
        def msg = new Hl7Message(AnalyzerType.BM850_EXIGO, "ID", "ORU^R01", "2.7", "BM850",
                LocalDateTime.now(), "S1", "USI",
                LocalDateTime.now(), "comment", [], "raw", Instant.now())

        expect:
        msg.messageDescription() == "ORU^R01 (HL7 2.7)"
    }

    def "record components are exposed via accessors"() {
        given:
        def now = LocalDateTime.of(2026, 5, 3, 10, 0)
        def received = Instant.parse("2026-05-03T10:00:00Z")
        def obs = new Hl7Observation(1, "NM", "HGB", "14.1", "g/dL", "12-16", "N", "F")

        when:
        def msg = new Hl7Message(AnalyzerType.BM850_EXIGO, "MSGCTL-1", "ORU^R01", "2.7", "BM850", now,
                "SAMPLE-42", "USI", now, "test comment", [obs], "raw-data", received)

        then:
        msg.analyzerType() == AnalyzerType.BM850_EXIGO
        msg.messageControlId() == "MSGCTL-1"
        msg.messageType() == "ORU^R01"
        msg.hl7Version() == "2.7"
        msg.sendingApplication() == "BM850"
        msg.messageDateTime() == now
        msg.sampleId() == "SAMPLE-42"
        msg.universalServiceId() == "USI"
        msg.observationDateTime() == now
        msg.comment() == "test comment"
        msg.observations() == [obs]
        msg.rawData() == "raw-data"
        msg.receivedAt() == received
    }
}
