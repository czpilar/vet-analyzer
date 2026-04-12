package net.czpilar.vet.analyzer.core.model.fujifilm;

import net.czpilar.vet.analyzer.core.model.MeasurementResult;

public record FujifilmTestResult(
        String testCode,
        String relation,
        String value,
        String unit,
        int dilutionFactor,
        String rangeLow,
        String rangeHigh,
        String flag
) {
    public MeasurementResult toMeasurementResult() {
        return new MeasurementResult(
                testCode, relation, value, unit, dilutionFactor, rangeLow, rangeHigh, flag
        );
    }
}
