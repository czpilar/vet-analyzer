package net.czpilar.vet.analyzer.core.parser

import net.czpilar.vet.analyzer.core.model.AnalyzerMessage
import net.czpilar.vet.analyzer.core.model.AnalyzerType
import net.czpilar.vet.analyzer.core.model.fujifilm.FujifilmResultMessage
import net.czpilar.vet.analyzer.core.model.hl7.Hl7Message
import spock.lang.Specification

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

/**
 * Data-driven parser test. Drop a session log file into
 * {@code src/test/resources/sessions/} and every MESSAGE block in it is
 * re-parsed through {@link MessageParserRegistry} and cross-checked against
 * the PARSED summary captured in the same log.
 */
class SessionLogParserTest extends Specification {

    static final String FIXTURES_RESOURCE = "/sessions"
    static final String MESSAGE_PREFIX = "--- MESSAGE ["
    static final String PARSED_MARKER = "--- PARSED ---"
    static final String END_MARKER = "--- END MESSAGE ---"
    static final String ANALYZER_PREFIX = "Analyzer: "
    static final String SAMPLE_PREFIX = "Sample: "
    static final String TESTS_PREFIX = "Tests: "

    def registry = MessageParserRegistry.createDefault()

    def "parser handles #label from #fixture"() {
        when:
        AnalyzerMessage message = registry.parse(rawData)

        then:
        message != null
        message.analyzerType() == expectedAnalyzer
        actualSampleId(message) == expectedSampleId
        actualTestCount(message) == expectedTestCount

        where:
        [fixture, label, rawData, expectedAnalyzer, expectedSampleId, expectedTestCount] << loadFixtures()
    }

    static String actualSampleId(AnalyzerMessage m) {
        if (m instanceof Hl7Message) {
            return m.sampleId()
        }
        if (m instanceof FujifilmResultMessage) {
            return m.sampleNumber()
        }
        return ""
    }

    static int actualTestCount(AnalyzerMessage m) {
        if (m instanceof Hl7Message) {
            return m.observations().findAll { it.valueType() == "NM" }.size()
        }
        if (m instanceof FujifilmResultMessage) {
            return m.testResults().size()
        }
        return 0
    }

    static List<List<Object>> loadFixtures() {
        URL url = SessionLogParserTest.getResource(FIXTURES_RESOURCE)
        if (url == null) {
            return []
        }
        Path dir = Path.of(url.toURI())
        List<List<Object>> rows = []
        Files.list(dir).sorted().each { Path file ->
            if (!file.fileName.toString().endsWith(".log")) {
                return
            }
            String content = Files.readString(file, StandardCharsets.UTF_8)
            AnalyzerType analyzer = detectAnalyzer(content)
            extractMessages(content).each { msg ->
                rows << [file.fileName.toString(), msg.label, msg.raw, analyzer, msg.sampleId, msg.testCount]
            }
        }
        return rows
    }

    static AnalyzerType detectAnalyzer(String content) {
        String line = content.split("\r\n").find { it.startsWith(ANALYZER_PREFIX) }
        if (line == null) {
            return null
        }
        if (line.contains("NX600")) {
            return AnalyzerType.NX600
        }
        if (line.contains("AU20V")) {
            return AnalyzerType.AU20V
        }
        if (line.contains("BM850") || line.contains("EXIGO")) {
            return AnalyzerType.BM850_EXIGO
        }
        return null
    }

    /**
     * Parses the session-log structure. The session writer uses {@code \r\n}
     * as its line separator, while HL7 messages keep their own {@code \r}
     * segment terminators inside the raw block — splitting on {@code \r\n}
     * preserves them.
     */
    static List<Map<String, Object>> extractMessages(String content) {
        List<Map<String, Object>> messages = []
        String[] lines = content.split("\r\n", -1)
        int i = 0
        while (i < lines.length) {
            String line = lines[i]
            if (!line.startsWith(MESSAGE_PREFIX) || !line.endsWith("---")) {
                i++
                continue
            }
            int closeBracket = line.indexOf(']')
            String label = line
            if (closeBracket > 0) {
                label = line.substring(MESSAGE_PREFIX.length(), closeBracket)
            }
            int parsedIdx = -1
            int endIdx = -1
            for (int j = i + 1; j < lines.length; j++) {
                if (lines[j] == PARSED_MARKER) {
                    parsedIdx = j
                    break
                }
                if (lines[j] == END_MARKER) {
                    endIdx = j
                    break
                }
            }
            int rawEnd = parsedIdx >= 0 ? parsedIdx : endIdx
            if (rawEnd < 0) {
                i++
                continue
            }
            String rawData = (i + 1..<rawEnd).collect { lines[it] }.join("\r\n")
            String sampleId = ""
            int testCount = 0
            if (parsedIdx >= 0) {
                for (int k = parsedIdx + 1; k < lines.length && lines[k] != END_MARKER; k++) {
                    if (lines[k].startsWith(SAMPLE_PREFIX)) {
                        sampleId = lines[k].substring(SAMPLE_PREFIX.length()).split(",")[0].trim()
                    }
                    if (lines[k].startsWith(TESTS_PREFIX)) {
                        String rest = lines[k].substring(TESTS_PREFIX.length())
                        if (rest.trim().isEmpty()) {
                            testCount = 0
                        } else {
                            testCount = rest.split(", ").length
                        }
                    }
                }
            }
            messages << [label: label, raw: rawData, sampleId: sampleId, testCount: testCount]
            i = rawEnd + 1
        }
        return messages
    }
}
