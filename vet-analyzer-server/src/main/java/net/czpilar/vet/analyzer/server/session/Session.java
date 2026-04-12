package net.czpilar.vet.analyzer.server.session;

import net.czpilar.vet.analyzer.core.model.AnalyzerMessage;
import net.czpilar.vet.analyzer.core.model.AnalyzerType;
import net.czpilar.vet.analyzer.core.model.MeasurementResult;
import net.czpilar.vet.analyzer.core.model.fujifilm.FujifilmResultMessage;
import net.czpilar.vet.analyzer.core.model.hl7.Hl7Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class Session {

    private static final Logger log = LoggerFactory.getLogger(Session.class);
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    private final String sessionId;
    private final String remoteAddress;
    private final Instant startedAt;
    private final Path sessionFile;
    private final BufferedWriter writer;
    private AnalyzerType detectedType;

    public Session(String remoteAddress, Path sessionDirectory) throws IOException {
        this.sessionId = UUID.randomUUID().toString().substring(0, 8);
        this.remoteAddress = remoteAddress;
        this.startedAt = Instant.now();

        String timestamp = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").withZone(ZoneId.systemDefault()).format(startedAt);
        String safeAddress = remoteAddress.replaceAll("[:/]", "_");
        this.sessionFile = sessionDirectory.resolve("session_" + timestamp + "_" + safeAddress + "_" + sessionId + ".log");

        Files.createDirectories(sessionDirectory);
        this.writer = Files.newBufferedWriter(sessionFile, StandardCharsets.UTF_8);

        writeHeader();
    }

    private void writeHeader() throws IOException {
        writer.write("=== SESSION START ===\n");
        writer.write("Session ID: " + sessionId + "\n");
        writer.write("Remote: " + remoteAddress + "\n");
        writer.write("Started: " + TIMESTAMP_FORMAT.format(startedAt) + "\n");
        writer.write("\n");
        writer.flush();
    }

    public void updateAnalyzerType(AnalyzerType type) throws IOException {
        this.detectedType = type;
        writer.write("Analyzer: " + type.displayName() + " (" + type.category() + ") [auto-detected]\n");
        writer.write("\n");
        writer.flush();
    }

    public void writeMessage(String rawData, AnalyzerMessage parsedMessage) throws IOException {
        String timestamp = TIMESTAMP_FORMAT.format(Instant.now());

        String msgType = parsedMessage != null ? parsedMessage.messageDescription() : "UNKNOWN";

        writer.write("--- MESSAGE [" + msgType + "] @ " + timestamp + " ---\n");
        writer.write(rawData);
        if (!rawData.endsWith("\n")) {
            writer.write("\n");
        }

        if (parsedMessage != null) {
            writer.write("--- PARSED ---\n");
            writeParsedSummary(parsedMessage);
        }

        writer.write("--- END MESSAGE ---\n\n");
        writer.flush();
    }

    private void writeParsedSummary(AnalyzerMessage message) throws IOException {
        switch (message) {
            case Hl7Message hl7 -> {
                writer.write("Type: " + hl7.messageType() + " (HL7 " + hl7.hl7Version() + ")\n");
                writer.write("Sample: " + hl7.sampleId() + "\n");
                writer.write("Tests: ");
                var results = hl7.observations().stream()
                        .filter(o -> "NM".equals(o.valueType()))
                        .map(o -> o.observationId() + "=" + o.value() + " " + o.unit())
                        .toList();
                writer.write(String.join(", ", results));
                writer.write("\n");
            }
            case FujifilmResultMessage result -> {
                writer.write("Type: " + result.command().name() + " (" + result.command().description() + ")\n");
                writer.write("Sample: " + result.sampleNumber() + ", Patient: " + result.patientId()
                        + ", Species: " + result.speciesCode() + "\n");
                writer.write("Tests: ");
                var results = result.testResults().stream()
                        .map(t -> t.toMeasurementResult().toString())
                        .toList();
                writer.write(String.join(", ", results));
                writer.write("\n");
            }
            default -> writer.write("Type: " + message.messageDescription() + "\n");
        }
    }

    public void close() {
        try {
            writer.write("=== SESSION END === " + TIMESTAMP_FORMAT.format(Instant.now()) + "\n");
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

    public Path getSessionFile() {
        return sessionFile;
    }
}
