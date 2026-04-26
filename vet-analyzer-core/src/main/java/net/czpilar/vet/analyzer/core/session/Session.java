package net.czpilar.vet.analyzer.core.session;

import net.czpilar.vet.analyzer.core.model.AnalyzerMessage;
import net.czpilar.vet.analyzer.core.model.AnalyzerType;
import net.czpilar.vet.analyzer.core.model.fujifilm.FujifilmResultMessage;
import net.czpilar.vet.analyzer.core.model.hl7.Hl7Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class Session {

    private static final Logger log = LoggerFactory.getLogger(Session.class);
    private static final String NL = System.lineSeparator();
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    private final String sessionId;
    private final String remoteAddress;
    private final Instant startedAt;
    private final Path sessionFile;
    private final BufferedWriter writer;
    private AnalyzerType detectedType;

    public Session(String sessionId, String remoteAddress, Path sessionDirectory) throws IOException {
        this.sessionId = sessionId;
        this.remoteAddress = remoteAddress;
        this.startedAt = Instant.now();

        String safeAddress = remoteAddress.replaceAll("[:/.]", "-");
        this.sessionFile = sessionDirectory.resolve("session_" + sessionId + "_" + safeAddress + ".log");

        Files.createDirectories(sessionDirectory);
        this.writer = Files.newBufferedWriter(sessionFile, StandardCharsets.UTF_8);

        writeHeader();
    }

    private void writeHeader() throws IOException {
        writer.write("=== SESSION START ===" + NL);
        writer.write("Session ID: " + sessionId + NL);
        writer.write("Remote: " + remoteAddress + NL);
        writer.write("Started: " + TIMESTAMP_FORMAT.format(startedAt) + NL);
        writer.write(NL);
        writer.flush();
    }

    public void updateAnalyzerType(AnalyzerType type) throws IOException {
        this.detectedType = type;
        writer.write("Analyzer: " + type.displayName() + " (" + type.category() + ") [auto-detected]" + NL);
        writer.write(NL);
        writer.flush();
    }

    public void writeMessage(String rawData, AnalyzerMessage parsedMessage) throws IOException {
        String timestamp = TIMESTAMP_FORMAT.format(Instant.now());

        String msgType = parsedMessage != null ? parsedMessage.messageDescription() : "UNKNOWN";

        writer.write("--- MESSAGE [" + msgType + "] @ " + timestamp + " ---" + NL);
        writer.write(rawData);
        if (!rawData.endsWith(NL)) {
            writer.write(NL);
        }

        if (parsedMessage != null) {
            writer.write("--- PARSED ---" + NL);
            writeParsedSummary(parsedMessage);
        }

        writer.write("--- END MESSAGE ---" + NL + NL);
        writer.flush();
    }

    private void writeParsedSummary(AnalyzerMessage message) throws IOException {
        switch (message) {
            case Hl7Message hl7 -> {
                writer.write("Type: " + hl7.messageType() + " (HL7 " + hl7.hl7Version() + ")" + NL);
                writer.write("Sample: " + hl7.sampleId() + NL);
                writer.write("Tests: ");
                List<String> results = hl7.observations().stream()
                        .filter(o -> "NM".equals(o.valueType()))
                        .map(o -> o.observationId() + "=" + o.value() + " " + o.unit())
                        .toList();
                writer.write(String.join(", ", results));
                writer.write(NL);
            }
            case FujifilmResultMessage result -> {
                writer.write("Type: " + result.command().name() + " (" + result.command().description() + ")" + NL);
                writer.write("Sample: " + result.sampleNumber() + ", Patient: " + result.patientId()
                        + ", Species: " + result.speciesCode() + NL);
                writer.write("Tests: ");
                List<String> results = result.testResults().stream()
                        .map(t -> t.toMeasurementResult().toString())
                        .toList();
                writer.write(String.join(", ", results));
                writer.write(NL);
            }
            default -> writer.write("Type: " + message.messageDescription() + NL);
        }
    }

    public void close() {
        try {
            writer.write("=== SESSION END === " + TIMESTAMP_FORMAT.format(Instant.now()) + NL);
            writer.close();
            log.info("Session {} closed, file: {}", sessionId, sessionFile);
        } catch (IOException e) {
            log.error("Error closing session {}", sessionId, e);
        }
    }

    public String getSessionId() {
        return sessionId;
    }

    public AnalyzerType getDetectedType() {
        return detectedType;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public Path getSessionFile() {
        return sessionFile;
    }
}
