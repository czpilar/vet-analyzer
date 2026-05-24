package net.czpilar.vet.analyzer.server.dto;

import java.util.List;

public record SessionDetail(
        SessionSummary summary,
        List<MessageEntry> messages
) {
}
