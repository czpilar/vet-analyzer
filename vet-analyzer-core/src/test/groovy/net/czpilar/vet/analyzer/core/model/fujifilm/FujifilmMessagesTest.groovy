package net.czpilar.vet.analyzer.core.model.fujifilm

import net.czpilar.vet.analyzer.core.model.AnalyzerType
import net.czpilar.vet.analyzer.core.protocol.fujifilm.FujifilmCommand
import spock.lang.Specification

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

class FujifilmMessagesTest extends Specification {

    private static final Instant NOW = Instant.parse("2026-05-03T10:00:00Z")

    def "FujifilmMessage default messageDescription combines command name and description"() {
        given:
        def msg = new FujifilmStartMessage(
                AnalyzerType.NX600, FujifilmCommand.S, "01",
                LocalDate.of(2026, 5, 3), LocalTime.of(10, 0),
                "S001", "P001", "Rex", 1, "raw", NOW)

        expect:
        msg.messageDescription() == "S - " + FujifilmCommand.S.description()
    }

    def "FujifilmStartMessage exposes record components"() {
        given:
        def date = LocalDate.of(2026, 5, 3)
        def time = LocalTime.of(10, 30)
        def msg = new FujifilmStartMessage(
                AnalyzerType.AU20V, FujifilmCommand.S, "00", date, time,
                "S99", "P99", "Charlie", 7, "raw", NOW)

        expect:
        msg.analyzerType() == AnalyzerType.AU20V
        msg.command() == FujifilmCommand.S
        msg.testCondition() == "00"
        msg.date() == date
        msg.time() == time
        msg.sampleNumber() == "S99"
        msg.patientId() == "P99"
        msg.patientName() == "Charlie"
        msg.samplePosition() == 7
        msg.rawData() == "raw"
        msg.receivedAt() == NOW
    }

    def "FujifilmErrorMessage exposes record components"() {
        given:
        def msg = new FujifilmErrorMessage(AnalyzerType.NX600, FujifilmCommand.E, "errcode", "raw", NOW)

        expect:
        msg.analyzerType() == AnalyzerType.NX600
        msg.command() == FujifilmCommand.E
        msg.errorData() == "errcode"
        msg.rawData() == "raw"
        msg.receivedAt() == NOW
    }

    def "FujifilmSampleInfoMessage exposes record components"() {
        given:
        def msg = new FujifilmSampleInfoMessage(AnalyzerType.NX600, FujifilmCommand.W, "S42", "raw", NOW)

        expect:
        msg.analyzerType() == AnalyzerType.NX600
        msg.command() == FujifilmCommand.W
        msg.sampleNumber() == "S42"
    }

    def "FujifilmWorklistQueryMessage exposes record components"() {
        given:
        def msg = new FujifilmWorklistQueryMessage(
                AnalyzerType.AU20V, FujifilmCommand.Y, "S1", "P1", "Patient", 3, "raw", NOW)

        expect:
        msg.analyzerType() == AnalyzerType.AU20V
        msg.command() == FujifilmCommand.Y
        msg.sampleNumber() == "S1"
        msg.patientId() == "P1"
        msg.patientName() == "Patient"
        msg.numberOfRequests() == 3
    }

    def "FujifilmResultMessage exposes record components"() {
        given:
        def date = LocalDate.of(2026, 5, 3)
        def time = LocalTime.of(10, 30)
        def tests = [new FujifilmTestResult("BUN-PS", "=", "7.5", "mmol/l", 1, "3", "10", "")]
        def msg = new FujifilmResultMessage(
                AnalyzerType.NX600, FujifilmCommand.R, "00", date, time,
                "S001", "P001", "Rex", 16, 1, 5, 1, 1, tests, "raw", NOW)

        expect:
        msg.analyzerType() == AnalyzerType.NX600
        msg.command() == FujifilmCommand.R
        msg.status() == "00"
        msg.date() == date
        msg.time() == time
        msg.sampleNumber() == "S001"
        msg.patientId() == "P001"
        msg.patientName() == "Rex"
        msg.speciesCode() == 16
        msg.sex() == 1
        msg.age() == 5
        msg.samplePosition() == 1
        msg.numberOfTests() == 1
        msg.testResults() == tests
        msg.rawData() == "raw"
        msg.receivedAt() == NOW
    }
}
