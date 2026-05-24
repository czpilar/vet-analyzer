package net.czpilar.vet.analyzer.server.dto;

public record SessionSummary(
        String id,
        String sessionId,
        String remote,
        String startedAt,
        String endedAt,
        String analyzer,
        int messageCount
) {
}
