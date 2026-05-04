package net.czpilar.vet.analyzer.starter.exception;

import net.czpilar.vet.analyzer.core.exception.VetAnalyzerException;

/**
 * Thrown by {@code VetAnalyzerServerLifecycle.start()} when the embedded TCP server
 * fails to bind, initialize the Netty pipeline, or otherwise enter the running state.
 * The original cause (typically a {@link java.net.BindException} or other Netty error)
 * is attached as the exception cause.
 */
public class VetAnalyzerServerStartException extends VetAnalyzerException {

    public VetAnalyzerServerStartException(String message, Throwable cause) {
        super(message, cause);
    }
}
