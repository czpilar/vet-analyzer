package net.czpilar.vet.analyzer.core.session;

import net.czpilar.vet.analyzer.core.exception.SessionDirectoryException;
import net.czpilar.vet.analyzer.core.listener.AnalyzerMessageListener;
import net.czpilar.vet.analyzer.core.listener.SessionContext;
import net.czpilar.vet.analyzer.core.model.AnalyzerMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AnalyzerMessageListener implementation that logs each TCP session into its own
 * file inside the configured directory. The directory is created on construction
 * if it does not yet exist.
 * <p>
 * Reusable as a generic logging tool - does not depend on Spring.
 */
public class SessionFileListener implements AnalyzerMessageListener {

    private static final Logger log = LoggerFactory.getLogger(SessionFileListener.class);

    private final Path sessionDirectory;
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();

    public SessionFileListener(String directory) {
        this.sessionDirectory = Path.of(directory);
        try {
            Files.createDirectories(this.sessionDirectory);
        } catch (IOException e) {
            throw new SessionDirectoryException("Failed to create session directory: " + this.sessionDirectory, e);
        }
    }

    @Override
    public void onSessionStart(SessionContext ctx) {
        try {
            Session session = new Session(ctx.sessionId(), ctx.remoteAddress(), sessionDirectory);
            sessions.put(ctx.sessionId(), session);
            log.info("Session file created: {} for {}", session.getSessionFile(), ctx.remoteAddress());
        } catch (IOException e) {
            log.error("Failed to create session file for {}", ctx.remoteAddress(), e);
        }
    }

    @Override
    public void onMessage(AnalyzerMessage message, String rawData, SessionContext ctx) {
        Session session = sessions.get(ctx.sessionId());
        if (session == null) {
            return;
        }
        try {
            if (session.getDetectedType() == null) {
                session.updateAnalyzerType(message.analyzerType());
            }
            session.writeMessage(rawData, message);
        } catch (IOException e) {
            log.error("Failed to write message to session file", e);
        }
    }

    @Override
    public void onRawMessage(String rawData, SessionContext ctx) {
        Session session = sessions.get(ctx.sessionId());
        if (session == null) {
            return;
        }
        try {
            session.writeMessage(rawData, null);
        } catch (IOException e) {
            log.error("Failed to write raw message to session file", e);
        }
    }

    @Override
    public void onSessionEnd(String sessionId) {
        Session session = sessions.remove(sessionId);
        if (session != null) {
            session.close();
        }
    }
}
