package net.czpilar.vet.analyzer.core.model.fujifilm;

import net.czpilar.vet.analyzer.core.model.AnalyzerType;
import net.czpilar.vet.analyzer.core.protocol.fujifilm.FujifilmCommand;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

public record FujifilmStartMessage(
        AnalyzerType analyzerType,
        FujifilmCommand command,
        String testCondition,
        LocalDate date,
        LocalTime time,
        String sampleNumber,
        String patientId,
        String patientName,
        int samplePosition,
        String rawData,
        Instant receivedAt
) implements FujifilmMessage {
}
