package net.czpilar.vet.analyzer.core.session

import net.czpilar.vet.analyzer.core.model.AnalyzerMessage
import net.czpilar.vet.analyzer.core.model.AnalyzerType
import net.czpilar.vet.analyzer.core.model.fujifilm.FujifilmResultMessage
import net.czpilar.vet.analyzer.core.model.fujifilm.FujifilmTestResult
import net.czpilar.vet.analyzer.core.model.hl7.Hl7Message
import net.czpilar.vet.analyzer.core.model.hl7.Hl7Observation
import net.czpilar.vet.analyzer.core.protocol.fujifilm.FujifilmCommand
import spock.lang.Specification
import spock.lang.TempDir

import java.nio.file.Files
import java.nio.file.Path
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class SessionTest extends Specification {

    @TempDir
    Path tempDir

    def "session file is created with safe filename and contains header"() {
        when:
        def session = new Session("abc123", "192.168.1.10:54321", tempDir)
        session.close()

        then:
        // Filename has dashes substituted for : and . in remote address
        def file = session.sessionFile
        file.fileName.toString() == "session_abc123_192-168-1-10-54321.log"
        Files.exists(file)

        and:
        def content = Files.readString(file)
        content.contains("=== SESSION START ===")
        content.contains("Session ID: abc123")
        content.contains("Remote: 192.168.1.10:54321")
        content.contains("=== SESSION END ===")
    }

    def "session creates the directory if it does not exist"() {
        given:
        def nestedDir = tempDir.resolve("nested/subdir")
        assert !Files.exists(nestedDir)

        when:
        def session = new Session("s1", "host", nestedDir)
        session.close()

        then:
        Files.exists(nestedDir)
        Files.exists(session.sessionFile)
    }

    def "updateAnalyzerType writes detected analyzer to file and stores type"() {
        given:
        def session = new Session("s1", "10.0.0.1", tempDir)

        when:
        session.updateAnalyzerType(AnalyzerType.NX600)
        session.close()

        then:
        session.detectedType == AnalyzerType.NX600
        def content = Files.readString(session.sessionFile)
        content.contains("Analyzer: Fujifilm NX600 (Biochemistry) [auto-detected]")
    }

    def "writeMessage with null parsed message writes UNKNOWN type"() {
        given:
        def session = new Session("s1", "10.0.0.1", tempDir)
        def raw = "raw data line"

        when:
        session.writeMessage(raw, null)
        session.close()

        then:
        def content = Files.readString(session.sessionFile)
        content.contains("--- MESSAGE [UNKNOWN]")
        content.contains(raw)
        content.contains("--- END MESSAGE ---")
        // Without parsed message, no PARSED block
        !content.contains("--- PARSED ---")
    }

    def "writeMessage with HL7 message writes parsed summary including test observations"() {
        given:
        def session = new Session("s1", "10.0.0.1", tempDir)
        def hl7 = new Hl7Message(
                AnalyzerType.BM850_EXIGO,
                "MSGCTL-1", "ORU^R01", "2.7", "BM850",
                LocalDateTime.of(2026, 5, 3, 10, 0),
                "SAMPLE-42", "USI",
                LocalDateTime.of(2026, 5, 3, 10, 0), "comment",
                [
                        new Hl7Observation(1, "NM", "HGB", "14.1", "g/dL", "12-16", "N", "F"),
                        new Hl7Observation(2, "ST", "TEXT", "skip-text", "", "", "", "F")
                ],
                "MSH|...", Instant.parse("2026-05-03T10:00:00Z"))

        when:
        session.writeMessage("MSH|...", hl7)
        session.close()

        then:
        def content = Files.readString(session.sessionFile)
        content.contains("--- MESSAGE [ORU^R01 (HL7 2.7)]")
        content.contains("Type: ORU^R01 (HL7 2.7)")
        content.contains("Sample: SAMPLE-42")
        content.contains("HGB=14.1 g/dL")
        // Non-NM observations are filtered out from the summary
        !content.contains("TEXT=skip-text")
    }

    def "writeMessage with Fujifilm result message writes parsed summary"() {
        given:
        def session = new Session("s1", "10.0.0.1", tempDir)
        def result = new FujifilmResultMessage(
                AnalyzerType.NX600, FujifilmCommand.R, "00",
                LocalDate.of(2026, 5, 3), LocalTime.of(10, 0),
                "S001", "P001", "Rex", 16, 1, 5, 1, 1,
                [new FujifilmTestResult("BUN-PS", "=", "7.50", "mmol/l", 1, "3.0", "10.0", "")],
                "raw", Instant.parse("2026-05-03T10:00:00Z"))

        when:
        session.writeMessage("raw", result)
        session.close()

        then:
        def content = Files.readString(session.sessionFile)
        content.contains("Sample: S001, Patient: P001, Species: 16")
        content.contains("BUN-PS=7.50 mmol/l")
    }

    def "writeMessage with other AnalyzerMessage falls through to default summary"() {
        given:
        def session = new Session("s1", "10.0.0.1", tempDir)
        def custom = new AnalyzerMessage() {
            @Override AnalyzerType analyzerType() { AnalyzerType.AU20V }
            @Override Instant receivedAt() { Instant.parse("2026-05-03T10:00:00Z") }
            @Override String rawData() { "custom" }
            @Override String messageDescription() { "CUSTOM-DESC" }
        }

        when:
        session.writeMessage("custom raw", custom)
        session.close()

        then:
        def content = Files.readString(session.sessionFile)
        content.contains("--- MESSAGE [CUSTOM-DESC]")
        content.contains("Type: CUSTOM-DESC")
    }

    def "session getters expose configuration"() {
        when:
        def session = new Session("session-id", "10.0.0.5:8080", tempDir)

        then:
        session.sessionId == "session-id"
        session.remoteAddress == "10.0.0.5:8080"
        session.detectedType == null
        session.sessionFile.parent == tempDir

        cleanup:
        session.close()
    }
}
