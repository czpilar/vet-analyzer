package net.czpilar.vet.analyzer.server.listener

import net.czpilar.vet.analyzer.core.listener.SessionContext
import net.czpilar.vet.analyzer.core.model.AnalyzerType
import net.czpilar.vet.analyzer.core.model.fujifilm.FujifilmResultMessage
import net.czpilar.vet.analyzer.core.model.fujifilm.FujifilmTestResult
import net.czpilar.vet.analyzer.core.model.hl7.Hl7Message
import net.czpilar.vet.analyzer.core.model.hl7.Hl7Observation
import net.czpilar.vet.analyzer.core.protocol.fujifilm.FujifilmCommand
import spock.lang.Specification
import spock.lang.TempDir
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.json.JsonMapper

import java.nio.file.Files
import java.nio.file.Path
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class SessionJsonListenerTest extends Specification {

    @TempDir
    Path tempDir

    private ObjectMapper mapper = JsonMapper.builder().build()

    def "session start writes initial JSON file with summary"() {
        given:
        def listener = new SessionJsonListener(tempDir.toString(), mapper)
        def ctx = new SessionContext("sess1", "192.168.1.10")

        when:
        listener.onSessionStart(ctx)

        then:
        def files = Files.list(tempDir).toList()
        files.size() == 1
        files[0].fileName.toString().endsWith(".json")
        def json = mapper.readTree(files[0].toFile())
        json.path("summary").path("sessionId").asString() == "sess1"
        json.path("summary").path("remote").asString() == "192.168.1.10"
        json.path("summary").path("messageCount").asInt() == 0
        json.path("summary").path("endedAt").isNull()
        json.path("messages").isArray()
        json.path("messages").size() == 0
    }

    def "session end stamps endedAt timestamp"() {
        given:
        def listener = new SessionJsonListener(tempDir.toString(), mapper)
        def ctx = new SessionContext("sess1", "192.168.1.10")

        when:
        listener.onSessionStart(ctx)
        listener.onSessionEnd("sess1")

        then:
        def file = Files.list(tempDir).toList()[0]
        def json = mapper.readTree(file.toFile())
        !json.path("summary").path("endedAt").isNull()
        json.path("summary").path("endedAt").asString() ==~ /\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}/
    }

    def "onMessage appends Fujifilm message with parsed payload"() {
        given:
        def listener = new SessionJsonListener(tempDir.toString(), mapper)
        def ctx = new SessionContext("sess1", "192.168.1.10")
        def message = createFujifilmResultMessage()

        when:
        listener.onSessionStart(ctx)
        listener.onMessage(message, "R,NORMAL ,14-06-2019,09:28,8", ctx)

        then:
        def file = Files.list(tempDir).toList()[0]
        def json = mapper.readTree(file.toFile())
        json.path("summary").path("messageCount").asInt() == 1
        json.path("summary").path("analyzer").asString() == "Fujifilm NX600 (Biochemistry)"

        def msg = json.path("messages").get(0)
        msg.path("raw").asString() == "R,NORMAL ,14-06-2019,09:28,8"
        msg.path("type").asString().contains("R")
        msg.path("parsed").path("command").asString() == "R"
        msg.path("parsed").path("sampleNumber").asString() == "8"
        msg.path("parsed").path("testResults").size() == 1
        msg.path("parsed").path("testResults").get(0).path("testCode").asString() == "TP-PS"
    }

    def "onMessage appends HL7 message with parsed observations"() {
        given:
        def listener = new SessionJsonListener(tempDir.toString(), mapper)
        def ctx = new SessionContext("sess1", "192.168.1.10")
        def message = createHl7Message()

        when:
        listener.onSessionStart(ctx)
        listener.onMessage(message, "MSH|...", ctx)

        then:
        def file = Files.list(tempDir).toList()[0]
        def json = mapper.readTree(file.toFile())
        json.path("summary").path("messageCount").asInt() == 1
        json.path("summary").path("analyzer").asString() == "BM850/EXIGO H400 (Hematology)"

        def msg = json.path("messages").get(0)
        msg.path("parsed").path("messageType").asString() == "ORU^R01"
        msg.path("parsed").path("hl7Version").asString() == "2.7"
        msg.path("parsed").path("sampleId").asString() == "68"
        msg.path("parsed").path("observations").size() == 1
        msg.path("parsed").path("observations").get(0).path("observationId").asString() == "RBC"
    }

    def "onRawMessage appends unknown message with null parsed payload"() {
        given:
        def listener = new SessionJsonListener(tempDir.toString(), mapper)
        def ctx = new SessionContext("sess1", "192.168.1.10")

        when:
        listener.onSessionStart(ctx)
        listener.onRawMessage("some unknown data", ctx)

        then:
        def file = Files.list(tempDir).toList()[0]
        def json = mapper.readTree(file.toFile())
        def msg = json.path("messages").get(0)
        msg.path("raw").asString() == "some unknown data"
        msg.path("type").asString() == "UNKNOWN"
        msg.path("parsed").isNull()
    }

    def "file is rewritten on each event so polling sees fresh state"() {
        given:
        def listener = new SessionJsonListener(tempDir.toString(), mapper)
        def ctx = new SessionContext("sess1", "192.168.1.10")

        when:
        listener.onSessionStart(ctx)
        def file = Files.list(tempDir).toList()[0]
        listener.onRawMessage("first", ctx)
        def afterOne = mapper.readTree(file.toFile())
        listener.onRawMessage("second", ctx)
        def afterTwo = mapper.readTree(file.toFile())

        then:
        afterOne.path("summary").path("messageCount").asInt() == 1
        afterTwo.path("summary").path("messageCount").asInt() == 2
        afterTwo.path("messages").size() == 2
    }

    def "concurrent sessions write to separate files keyed by sessionId"() {
        given:
        def listener = new SessionJsonListener(tempDir.toString(), mapper)
        def ctx1 = new SessionContext("sess1", "192.168.1.10")
        def ctx2 = new SessionContext("sess2", "192.168.1.10")

        when:
        listener.onSessionStart(ctx1)
        listener.onSessionStart(ctx2)
        listener.onRawMessage("for-sess1", ctx1)
        listener.onRawMessage("for-sess2", ctx2)

        then:
        def files = Files.list(tempDir).toList()
        files.size() == 2
        def sess1File = files.find { it.fileName.toString().contains("sess1") }
        def sess2File = files.find { it.fileName.toString().contains("sess2") }
        mapper.readTree(sess1File.toFile()).path("messages").get(0).path("raw").asString() == "for-sess1"
        mapper.readTree(sess2File.toFile()).path("messages").get(0).path("raw").asString() == "for-sess2"
    }

    def "events for unknown session id are ignored"() {
        given:
        def listener = new SessionJsonListener(tempDir.toString(), mapper)
        def ctx = new SessionContext("missing", "192.168.1.10")

        when:
        listener.onMessage(createFujifilmResultMessage(), "raw", ctx)
        listener.onRawMessage("raw", ctx)
        listener.onSessionEnd("missing")

        then:
        Files.list(tempDir).toList().isEmpty()
    }

    def "constructor creates the session directory when missing"() {
        given:
        def newDir = tempDir.resolve("nested").resolve("dir")
        assert !Files.exists(newDir)

        when:
        new SessionJsonListener(newDir.toString(), mapper)

        then:
        Files.isDirectory(newDir)
    }

    private static FujifilmResultMessage createFujifilmResultMessage() {
        return new FujifilmResultMessage(
                AnalyzerType.NX600, FujifilmCommand.R, "NORMAL",
                LocalDate.of(2019, 6, 14), LocalTime.of(9, 28),
                "8", "006532", "", 16, 9, 255, 1, 1,
                [new FujifilmTestResult("TP-PS", "=", "74", "g/l", 1, "55", "75", "")],
                "R,NORMAL ,14-06-2019,09:28,8", Instant.now()
        )
    }

    private static Hl7Message createHl7Message() {
        return new Hl7Message(
                AnalyzerType.BM850_EXIGO, "BM_1", "ORU^R01", "2.7", "BM850",
                LocalDateTime.of(2026, 4, 20, 17, 16, 54),
                "68", "", LocalDateTime.of(2026, 4, 20, 17, 16),
                null,
                [new Hl7Observation(4, "NM", "RBC", "6.52", "10*12/L", "5.50-8.50", null, "P")],
                "MSH|...", Instant.now()
        )
    }
}
