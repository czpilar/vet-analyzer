package net.czpilar.vet.analyzer.core.parser.hl7;

import net.czpilar.vet.analyzer.core.model.AnalyzerType;
import net.czpilar.vet.analyzer.core.model.hl7.Hl7Message;
import net.czpilar.vet.analyzer.core.model.hl7.Hl7Observation;
import net.czpilar.vet.analyzer.core.parser.MessageParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Base class for HL7 v2 message parsers. Implements the shared HL7 v2 grammar
 * (segment splitting, MSH/OBR/OBX/NTE field extraction, datetime parsing) used by
 * all HL7-speaking veterinary analyzers.
 *
 * <p>Concrete subclasses pin the parser to a specific device + protocol version
 * via {@link #matches(String, String)} and provide the {@link AnalyzerType}
 * embedded in the resulting {@link Hl7Message}.
 *
 * <p>Messages that do not match the strict gate fall through to
 * {@code MessageParserRegistry.parse()} returning {@code null}, and the channel
 * handler logs them as raw/unknown without sending an HL7 ACK.
 */
public abstract class AbstractHl7MessageParser implements MessageParser<Hl7Message> {

    private static final Logger log = LoggerFactory.getLogger(AbstractHl7MessageParser.class);
    private static final DateTimeFormatter HL7_DATETIME_14 = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final DateTimeFormatter HL7_DATETIME_12 = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

    /**
     * Returns the analyzer type associated with this parser; embedded into the
     * resulting {@link Hl7Message} so listeners can route by device.
     */
    protected abstract AnalyzerType analyzerType();

    /**
     * Strict gate based on HL7 MSH-3 (sending application) and MSH-12 (version).
     * Return {@code true} only when the message comes from the expected device
     * with the expected protocol version.
     */
    protected abstract boolean matches(String sendingApplication, String hl7Version);

    @Override
    public final boolean canParse(String rawData) {
        if (rawData == null || !rawData.startsWith("MSH|")) {
            return false;
        }
        String[] mshFields = extractMshFields(rawData);
        String sendingApp = mshFields.length > 2 ? mshFields[2] : null;
        String version = mshFields.length > 11 ? mshFields[11] : null;
        return matches(sendingApp, version);
    }

    @Override
    public final Hl7Message parse(String rawData) {
        String[] segments = rawData.split("[\\r\\n]");

        String messageControlId = "";
        String messageType = "";
        String hl7Version = "";
        String sendingApplication = "";
        LocalDateTime messageDateTime = null;
        String sampleId = "";
        String universalServiceId = "";
        LocalDateTime observationDateTime = null;
        String comment = "";
        List<Hl7Observation> observations = new ArrayList<>();

        for (String segment : segments) {
            String trimmed = segment.trim();
            if (trimmed.isEmpty()) {
                continue;
            }

            String[] fields = trimmed.split("\\|", -1);
            String segmentType = fields[0];

            switch (segmentType) {
                case "MSH" -> {
                    if (fields.length > 2) sendingApplication = fields[2];
                    if (fields.length > 7) messageDateTime = parseHl7DateTime(fields[7]);
                    if (fields.length > 8) messageType = fields[8];
                    if (fields.length > 9) messageControlId = fields[9];
                    if (fields.length > 11) hl7Version = fields[11];
                }
                case "OBR" -> {
                    if (fields.length > 3) sampleId = fields[3];
                    if (fields.length > 4) universalServiceId = fields[4];
                    if (fields.length > 22) observationDateTime = parseHl7DateTime(fields[22]);
                }
                case "NTE" -> {
                    if (fields.length > 3) comment = cleanValue(fields[3]);
                }
                case "OBX" -> observations.add(parseObx(fields));
                default -> log.trace("Ignoring segment: {}", segmentType);
            }
        }

        return new Hl7Message(
                analyzerType(),
                messageControlId, messageType, hl7Version, sendingApplication,
                messageDateTime, sampleId, universalServiceId, observationDateTime,
                comment, observations, rawData, Instant.now()
        );
    }

    private static String[] extractMshFields(String rawData) {
        int end = rawData.length();
        for (int i = 0; i < rawData.length(); i++) {
            char c = rawData.charAt(i);
            if (c == '\r' || c == '\n') {
                end = i;
                break;
            }
        }
        return rawData.substring(0, end).split("\\|", -1);
    }

    private static Hl7Observation parseObx(String[] fields) {
        int setId = fields.length > 1 ? parseIntSafe(fields[1]) : 0;
        String valueType = fields.length > 2 ? fields[2] : "";
        String observationId = fields.length > 3 ? fields[3] : "";
        String value = fields.length > 5 ? cleanValue(fields[5]) : "";
        String unit = fields.length > 6 ? fields[6] : "";
        String referenceRange = fields.length > 7 ? fields[7] : "";
        String abnormalFlag = fields.length > 8 ? fields[8] : "";
        String status = fields.length > 11 ? fields[11] : "";

        return new Hl7Observation(setId, valueType, observationId, value, unit, referenceRange, abnormalFlag, status);
    }

    private static String cleanValue(String value) {
        if (value == null) return "";
        if ("\"\"".equals(value)) return "";
        return value;
    }

    private static LocalDateTime parseHl7DateTime(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            if (value.length() >= 14) {
                return LocalDateTime.parse(value.substring(0, 14), HL7_DATETIME_14);
            } else if (value.length() >= 12) {
                return LocalDateTime.parse(value.substring(0, 12), HL7_DATETIME_12);
            }
        } catch (DateTimeParseException e) {
            log.warn("Failed to parse HL7 datetime: {}", value);
        }
        return null;
    }

    private static int parseIntSafe(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
