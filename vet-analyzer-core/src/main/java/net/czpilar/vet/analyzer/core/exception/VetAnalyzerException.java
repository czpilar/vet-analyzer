package net.czpilar.vet.analyzer.core.exception;

/**
 * Base unchecked exception for all errors raised by the {@code vet-analyzer} project.
 * Specific failure modes (e.g. {@code VetAnalyzerServerStartException}) extend this class
 * so callers can catch the whole family with a single {@code catch (VetAnalyzerException ...)}
 * block.
 */
public class VetAnalyzerException extends RuntimeException {

    public VetAnalyzerException(String message) {
        super(message);
    }

    public VetAnalyzerException(String message, Throwable cause) {
        super(message, cause);
    }

    public VetAnalyzerException(Throwable cause) {
        super(cause);
    }
}
