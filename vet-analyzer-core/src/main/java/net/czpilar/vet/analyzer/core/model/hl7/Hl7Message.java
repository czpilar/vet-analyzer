package net.czpilar.vet.analyzer.core.model.hl7;

import net.czpilar.vet.analyzer.core.model.AnalyzerMessage;
import net.czpilar.vet.analyzer.core.model.AnalyzerType;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

public record Hl7Message(
        AnalyzerType analyzerType,
        String messageControlId,
        String messageType,
        String hl7Version,
        String sendingApplication,
        LocalDateTime messageDateTime,
        String sampleId,
        String universalServiceId,
        LocalDateTime observationDateTime,
        String comment,
        List<Hl7Observation> observations,
        String rawData,
        Instant receivedAt
) implements AnalyzerMessage {

    @Override
    public String messageDescription() {
        return messageType + " (HL7 " + hl7Version + ")";
    }
}
