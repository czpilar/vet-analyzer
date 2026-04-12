package net.czpilar.vet.analyzer.core.model.fujifilm;

import net.czpilar.vet.analyzer.core.model.AnalyzerType;
import net.czpilar.vet.analyzer.core.protocol.fujifilm.FujifilmCommand;

import java.time.Instant;

public record FujifilmErrorMessage(
        AnalyzerType analyzerType,
        FujifilmCommand command,
        String errorData,
        String rawData,
        Instant receivedAt
) implements FujifilmMessage {
}
