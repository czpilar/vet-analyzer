package net.czpilar.vet.analyzer.core.model.fujifilm;

import net.czpilar.vet.analyzer.core.model.AnalyzerType;
import net.czpilar.vet.analyzer.core.protocol.fujifilm.FujifilmCommand;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record FujifilmResultMessage(
        AnalyzerType analyzerType,
        FujifilmCommand command,
        String status,
        LocalDate date,
        LocalTime time,
        String sampleNumber,
        String patientId,
        String patientName,
        int speciesCode,
        int sex,
        int age,
        int samplePosition,
        int numberOfTests,
        List<FujifilmTestResult> testResults,
        String rawData,
        Instant receivedAt
) implements FujifilmMessage {
}
