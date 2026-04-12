package net.czpilar.vet.analyzer.server.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;

public class SessionManager {

    private static final Logger log = LoggerFactory.getLogger(SessionManager.class);

    private final Path sessionDirectory;

    public SessionManager(String sessionDirectory) {
        this.sessionDirectory = Path.of(sessionDirectory);
    }

    public Session createSession(String remoteAddress) {
        try {
            var session = new Session(remoteAddress, sessionDirectory);
            log.info("Created session {} for {}", session.getSessionId(), remoteAddress);
            return session;
        } catch (IOException e) {
            log.error("Failed to create session for {}", remoteAddress, e);
            throw new RuntimeException("Failed to create session", e);
        }
    }
}
