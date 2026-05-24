package net.czpilar.vet.analyzer.server.dto;

import net.czpilar.vet.analyzer.core.model.AnalyzerMessage;

/**
 * Single message captured during a session.
 *
 * @param type      human-readable description of the message (e.g. "ORU^R01 (HL7 2.7)")
 * @param timestamp formatted local timestamp the message was received at
 * @param raw       raw payload as received from the analyzer
 * @param parsed    the parsed {@link AnalyzerMessage} (or {@code null} for unparsed
 *                  raw messages); serialized as-is so all per-type fields are preserved
 */
public record MessageEntry(
        String type,
        String timestamp,
        String raw,
        AnalyzerMessage parsed
) {
}
