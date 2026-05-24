package net.czpilar.vet.analyzer.server.web

import net.czpilar.vet.analyzer.server.config.SessionProperties
import org.springframework.http.HttpStatus
import spock.lang.Specification
import spock.lang.TempDir
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.json.JsonMapper

import java.nio.file.Files
import java.nio.file.Path

class SessionsControllerTest extends Specification {

    @TempDir
    Path tempDir

    private ObjectMapper mapper = JsonMapper.builder().build()

    private SessionsController controller() {
        return new SessionsController(new SessionProperties(directory: tempDir.toString()), mapper)
    }

    def "list returns empty when session directory does not exist"() {
        given:
        def missing = tempDir.resolve("missing")
        def ctrl = new SessionsController(new SessionProperties(directory: missing.toString()), mapper)

        when:
        def result = ctrl.list()

        then:
        result.isEmpty()
    }

    def "list returns empty when directory has no .json files"() {
        given:
        Files.writeString(tempDir.resolve("session_x.log"), "not json")

        when:
        def result = controller().list()

        then:
        result.isEmpty()
    }

    def "list returns only summary nodes for each .json file"() {
        given:
        writeSessionFile("session_aaa", 3, null)
        writeSessionFile("session_bbb", 1, "2026-05-25 00:01:00")
        Files.writeString(tempDir.resolve("session_zzz.log"), "ignored")

        when:
        def result = controller().list()

        then:
        result.size() == 2
        result.every { it.has("sessionId") && it.has("messageCount") }
        result.every { !it.has("messages") }
    }

    def "list returns sessions sorted newest-first by filename"() {
        given:
        writeSessionFile("session_20260524-235747-520a_127-0-0-1", 1, null)
        writeSessionFile("session_20260525-000252-9e8f_127-0-0-1", 1, null)
        writeSessionFile("session_20260525-000127-29c1_127-0-0-1", 1, null)

        when:
        def result = controller().list()

        then:
        result*.get("sessionId")*.asString() == [
                "20260525-000252-9e8f",
                "20260525-000127-29c1",
                "20260524-235747-520a"
        ]
    }

    def "list skips files that cannot be parsed as JSON"() {
        given:
        writeSessionFile("session_valid", 1, null)
        Files.writeString(tempDir.resolve("session_broken.json"), "{not valid json")

        when:
        def result = controller().list()

        then:
        result.size() == 1
        result[0].path("sessionId").asString() == "valid"
    }

    def "detail returns full JSON tree for existing session"() {
        given:
        writeSessionFile("session_abc", 2, "2026-05-25 00:01:00")

        when:
        def response = controller().detail("session_abc")

        then:
        response.statusCode == HttpStatus.OK
        response.body.path("summary").path("sessionId").asString() == "abc"
        response.body.path("messages").size() == 2
    }

    def "detail returns 404 when session file does not exist"() {
        when:
        def response = controller().detail("does-not-exist")

        then:
        response.statusCode == HttpStatus.NOT_FOUND
    }

    def "detail rejects path traversal attempts"() {
        given:
        def outside = tempDir.resolveSibling("secret.json")
        Files.writeString(outside, '{"summary":{}}')

        when:
        def response = controller().detail("../" + outside.fileName.toString().replace(".json", ""))

        then:
        response.statusCode == HttpStatus.NOT_FOUND

        cleanup:
        Files.deleteIfExists(outside)
    }

    private void writeSessionFile(String id, int messageCount, String endedAt) {
        def messages = (1..messageCount).collect {
            [type: "X - test", timestamp: "2026-05-25 00:00:0$it", raw: "raw-$it", parsed: null]
        }
        def shortId = id.startsWith("session_") ? id.substring("session_".length()).split("_")[0] : id
        def detail = [
                summary : [
                        id          : id,
                        sessionId   : shortId,
                        remote      : "127.0.0.1",
                        startedAt   : "2026-05-25 00:00:00",
                        endedAt     : endedAt,
                        analyzer    : "Fujifilm NX600 (Biochemistry)",
                        messageCount: messageCount,
                ],
                messages: messages,
        ]
        mapper.writeValue(tempDir.resolve(id + ".json").toFile(), detail)
    }
}
