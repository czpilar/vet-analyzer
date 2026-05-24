package net.czpilar.vet.analyzer.server.listener;

import net.czpilar.vet.analyzer.core.model.AnalyzerMessage;
import net.czpilar.vet.analyzer.core.model.AnalyzerType;
import net.czpilar.vet.analyzer.server.dto.MessageEntry;
import net.czpilar.vet.analyzer.server.dto.SessionDetail;
import net.czpilar.vet.analyzer.server.dto.SessionSummary;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * In-memory state of a single TCP session, persisted as a structured JSON
 * sidecar file. The file is rewritten on each new message and on close so that
 * a UI can poll the latest state at any time.
 */
class JsonSession {

    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    private final String id;
    private final String sessionId;
    private final String remote;
    private final String startedAt;
    private final Path file;
    private final ObjectMapper mapper;
    private final List<MessageEntry> messages = new ArrayList<>();

    private String analyzer;
    private String endedAt;

    JsonSession(String sessionId, String remoteAddress, Path file, ObjectMapper mapper) {
        this.id = file.getFileName().toString().replaceFirst("\\.json$", "");
        this.sessionId = sessionId;
        this.remote = remoteAddress;
        this.startedAt = TIMESTAMP_FORMAT.format(Instant.now());
        this.file = file;
        this.mapper = mapper;
    }

    void updateAnalyzerType(AnalyzerType type) {
        this.analyzer = type.displayName() + " (" + type.category() + ")";
    }

    void appendMessage(String rawData, AnalyzerMessage parsed) {
        String type = parsed != null ? parsed.messageDescription() : "UNKNOWN";
        String timestamp = TIMESTAMP_FORMAT.format(Instant.now());
        messages.add(new MessageEntry(type, timestamp, rawData, parsed));
    }

    void markEnded() {
        this.endedAt = TIMESTAMP_FORMAT.format(Instant.now());
    }

    void writeToDisk() throws IOException {
        SessionSummary summary = new SessionSummary(
                id, sessionId, remote, startedAt, endedAt, analyzer, messages.size()
        );
        SessionDetail detail = new SessionDetail(summary, List.copyOf(messages));
        mapper.writerWithDefaultPrettyPrinter().writeValue(file.toFile(), detail);
    }

    Path getFile() {
        return file;
    }
}
