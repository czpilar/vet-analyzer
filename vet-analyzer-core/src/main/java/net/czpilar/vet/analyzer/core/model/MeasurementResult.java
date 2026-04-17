package net.czpilar.vet.analyzer.core.model;

public record MeasurementResult(
        String testCode,
        String relation,
        String value,
        String unit,
        int dilutionFactor,
        String rangeLow,
        String rangeHigh,
        String warnings
) {
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(testCode).append(relation).append(value).append(" ").append(unit);
        if (rangeLow != null && rangeHigh != null && !rangeLow.isBlank() && !rangeHigh.isBlank()) {
            sb.append(" [").append(rangeLow).append("-").append(rangeHigh).append("]");
        }
        if (warnings != null && !warnings.isBlank()) {
            sb.append(" ").append(warnings);
        }
        return sb.toString();
    }
}
