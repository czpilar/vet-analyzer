package net.czpilar.vet.analyzer.core.exception;

/**
 * Thrown when the {@code SessionFileListener} cannot create or access the configured
 * session directory (e.g. permission denied, disk full, invalid path).
 */
public class SessionDirectoryException extends VetAnalyzerException {

    public SessionDirectoryException(String message, Throwable cause) {
        super(message, cause);
    }
}
