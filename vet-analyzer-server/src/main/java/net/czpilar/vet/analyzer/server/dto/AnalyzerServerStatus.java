package net.czpilar.vet.analyzer.server.dto;

public record AnalyzerServerStatus(boolean running, String message) {

    public static AnalyzerServerStatus of(boolean running) {
        return new AnalyzerServerStatus(running, null);
    }
}
