package net.czpilar.vet.analyzer.core.model;

import java.time.Instant;

public interface AnalyzerMessage {

    AnalyzerType analyzerType();

    Instant receivedAt();

    String rawData();

    /**
     * Human-readable description of the message type.
     */
    String messageDescription();
}
