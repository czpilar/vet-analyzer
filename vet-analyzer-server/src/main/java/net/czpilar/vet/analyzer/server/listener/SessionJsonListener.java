package net.czpilar.vet.analyzer.server.listener;

import net.czpilar.vet.analyzer.core.exception.SessionDirectoryException;
import net.czpilar.vet.analyzer.core.listener.AnalyzerMessageListener;
import net.czpilar.vet.analyzer.core.listener.SessionContext;
import net.czpilar.vet.analyzer.core.model.AnalyzerMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AnalyzerMessageListener implementation that persists each TCP session into
 * its own structured JSON sidecar file alongside the text log produced by
 * {@link net.czpilar.vet.analyzer.core.session.SessionFileListener}. The JSON
 * file is rewritten on every event so a UI can poll mid-session.
 */
public class SessionJsonListener implements AnalyzerMessageListener {

    private static final Logger log = LoggerFactory.getLogger(SessionJsonListener.class);

    private final Path sessionDirectory;
    private final ObjectMapper mapper;
    private final Map<String, JsonSession> sessions = new ConcurrentHashMap<>();

    public SessionJsonListener(String directory, ObjectMapper mapper) {
        this.sessionDirectory = Path.of(directory);
        this.mapper = mapper;
        try {
            Files.createDirectories(this.sessionDirectory);
        } catch (IOException e) {
            throw new SessionDirectoryException("Failed to create session directory: " + this.sessionDirectory, e);
        }
    }

    @Override
    public void onSessionStart(SessionContext ctx) {
        String safeAddress = ctx.remoteAddress().replaceAll("[:/.]", "-");
        Path file = sessionDirectory.resolve("session_" + ctx.sessionId() + "_" + safeAddress + ".json");
        JsonSession session = new JsonSession(ctx.sessionId(), ctx.remoteAddress(), file, mapper);
        sessions.put(ctx.sessionId(), session);
        flush(session, ctx.sessionId());
    }

    @Override
    public void onMessage(AnalyzerMessage message, String rawData, SessionContext ctx) {
        JsonSession session = sessions.get(ctx.sessionId());
        if (session == null) {
            return;
        }
        session.updateAnalyzerType(message.analyzerType());
        session.appendMessage(rawData, message);
        flush(session, ctx.sessionId());
    }

    @Override
    public void onRawMessage(String rawData, SessionContext ctx) {
        JsonSession session = sessions.get(ctx.sessionId());
        if (session == null) {
            return;
        }
        session.appendMessage(rawData, null);
        flush(session, ctx.sessionId());
    }

    @Override
    public void onSessionEnd(String sessionId) {
        JsonSession session = sessions.remove(sessionId);
        if (session == null) {
            return;
        }
        session.markEnded();
        flush(session, sessionId);
    }

    private void flush(JsonSession session, String sessionId) {
        try {
            session.writeToDisk();
        } catch (IOException e) {
            log.error("Failed to write JSON session file for {}", sessionId, e);
        }
    }
}
