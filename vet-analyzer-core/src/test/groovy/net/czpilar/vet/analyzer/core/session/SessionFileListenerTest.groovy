package net.czpilar.vet.analyzer.core.session

import net.czpilar.vet.analyzer.core.model.AnalyzerType
import net.czpilar.vet.analyzer.core.model.fujifilm.FujifilmResultMessage
import net.czpilar.vet.analyzer.core.model.fujifilm.FujifilmTestResult
import net.czpilar.vet.analyzer.core.protocol.fujifilm.FujifilmCommand
import spock.lang.Specification
import spock.lang.TempDir

import java.nio.file.Files
import java.nio.file.Path
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

class SessionFileListenerTest extends Specification {

    @TempDir
    Path tempDir

    def "session lifecycle creates and closes file"() {
        given:
        def listener = new SessionFileListener(tempDir.toString())

        when:
        listener.onSessionStart("sess1", "192.168.1.10")
        listener.onSessionEnd("sess1")

        then:
        def files = Files.list(tempDir).toList()
        files.size() == 1
        def content = Files.readString(files[0])
        content.contains("SESSION START")
        content.contains("192.168.1.10")
        content.contains("SESSION END")
    }

    def "onMessage writes parsed message to session file"() {
        given:
        def listener = new SessionFileListener(tempDir.toString())
        def message = createResultMessage()

        when:
        listener.onSessionStart("sess1", "192.168.1.10")
        listener.onMessage(message, "R,NORMAL ,14-06-2019,09:28,8", "192.168.1.10")
        listener.onSessionEnd("sess1")

        then:
        def content = Files.readString(Files.list(tempDir).toList()[0])
        content.contains("MESSAGE")
        content.contains("PARSED")
        content.contains("R,NORMAL")
        content.contains("TP-PS")
    }

    def "onRawMessage writes unknown message to session file"() {
        given:
        def listener = new SessionFileListener(tempDir.toString())

        when:
        listener.onSessionStart("sess1", "192.168.1.10")
        listener.onRawMessage("some unknown data", "192.168.1.10")
        listener.onSessionEnd("sess1")

        then:
        def content = Files.readString(Files.list(tempDir).toList()[0])
        content.contains("UNKNOWN")
        content.contains("some unknown data")
    }

    def "multiple sessions create separate files"() {
        given:
        def listener = new SessionFileListener(tempDir.toString())

        when:
        listener.onSessionStart("sess1", "192.168.1.10")
        listener.onSessionStart("sess2", "192.168.1.20")
        listener.onSessionEnd("sess1")
        listener.onSessionEnd("sess2")

        then:
        Files.list(tempDir).toList().size() == 2
    }

    def "constructor creates directory if it does not exist"() {
        given:
        def newDir = tempDir.resolve("nested").resolve("subdir")
        assert !Files.exists(newDir)

        when:
        new SessionFileListener(newDir.toString())

        then:
        Files.exists(newDir)
        Files.isDirectory(newDir)
    }

    def "constructor accepts existing directory"() {
        given:
        def existingDir = Files.createDirectory(tempDir.resolve("existing"))

        when:
        def listener = new SessionFileListener(existingDir.toString())
        listener.onSessionStart("sess1", "192.168.1.10")
        listener.onSessionEnd("sess1")

        then:
        Files.list(existingDir).toList().size() == 1
    }

    private static FujifilmResultMessage createResultMessage() {
        return new FujifilmResultMessage(
                AnalyzerType.NX600, FujifilmCommand.R, "NORMAL",
                LocalDate.of(2019, 6, 14), LocalTime.of(9, 28),
                "8", "006532", "", 16, 9, 255, 1, 1,
                [new FujifilmTestResult("TP-PS", "=", "74", "g/l", 1, "55", "75", "")],
                "R,NORMAL ,14-06-2019,09:28,8", Instant.now()
        )
    }
}
