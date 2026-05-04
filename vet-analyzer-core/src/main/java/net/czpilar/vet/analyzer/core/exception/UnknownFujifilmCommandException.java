package net.czpilar.vet.analyzer.core.exception;

/**
 * Thrown by {@code FujifilmCommand.fromCode(...)} when the supplied command code is
 * {@code null}, empty, or does not correspond to any known Fujifilm command letter.
 */
public class UnknownFujifilmCommandException extends VetAnalyzerException {

    public UnknownFujifilmCommandException(String message) {
        super(message);
    }
}
