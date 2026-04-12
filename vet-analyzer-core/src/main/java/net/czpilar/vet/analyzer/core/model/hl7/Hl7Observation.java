package net.czpilar.vet.analyzer.core.model.hl7;

import net.czpilar.vet.analyzer.core.model.MeasurementResult;

public record Hl7Observation(
        int setId,
        String valueType,
        String observationId,
        String value,
        String unit,
        String referenceRange,
        String abnormalFlag,
        String observationStatus
) {
    public MeasurementResult toMeasurementResult() {
        String rangeLow = "";
        String rangeHigh = "";
        if (referenceRange != null && referenceRange.contains("-")) {
            String[] parts = referenceRange.split("-", 2);
            rangeLow = parts[0].trim();
            rangeHigh = parts[1].trim();
        }
        return new MeasurementResult(
                observationId, "=", value, unit, 1, rangeLow, rangeHigh, abnormalFlag
        );
    }
}
