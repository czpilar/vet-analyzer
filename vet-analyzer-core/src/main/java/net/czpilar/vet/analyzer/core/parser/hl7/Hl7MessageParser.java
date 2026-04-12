package net.czpilar.vet.analyzer.core.parser.hl7;

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

public class Hl7MessageParser implements MessageParser<Hl7Message> {

    private static final Logger log = LoggerFactory.getLogger(Hl7MessageParser.class);
    private static final DateTimeFormatter HL7_DATETIME_14 = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final DateTimeFormatter HL7_DATETIME_12 = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

    @Override
    public boolean canParse(String rawData) {
        return rawData != null && rawData.startsWith("MSH|");
    }

    @Override
    public Hl7Message parse(String rawData) {
        String[] segments = rawData.split("\\r|\\n");

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
                    // MSH|^~\&| splits as: [0]=MSH [1]=enc [2]=sendApp [3]=sendFac [4]=recvApp [5]=recvFac [6]=empty [7]=dateTime
                    // 5 pipes between sendingApp and dateTime = 4 empty fields (MSH-4,5,6 + extra empty)
                    if (fields.length > 2) sendingApplication = fields[2];  // MSH-3
                    if (fields.length > 7) messageDateTime = parseHl7DateTime(fields[7]);  // MSH-7
                    if (fields.length > 8) messageType = fields[8];  // MSH-9
                    if (fields.length > 9) messageControlId = fields[9];  // MSH-10
                    if (fields.length > 11) hl7Version = fields[11];  // MSH-12
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
                messageControlId, messageType, hl7Version, sendingApplication,
                messageDateTime, sampleId, universalServiceId, observationDateTime,
                comment, observations, rawData, Instant.now()
        );
    }

    private Hl7Observation parseObx(String[] fields) {
        int setId = fields.length > 1 ? parseIntSafe(fields[1]) : 0;
        String valueType = fields.length > 2 ? fields[2] : "";
        String observationId = fields.length > 3 ? fields[3] : "";
        // field 4 is sub-id (empty)
        String value = fields.length > 5 ? cleanValue(fields[5]) : "";
        String unit = fields.length > 6 ? fields[6] : "";
        String referenceRange = fields.length > 7 ? fields[7] : "";
        String abnormalFlag = fields.length > 8 ? fields[8] : "";
        String status = fields.length > 11 ? fields[11] : "";

        return new Hl7Observation(setId, valueType, observationId, value, unit, referenceRange, abnormalFlag, status);
    }

    private static String cleanValue(String value) {
        if (value == null) return "";
        // Remove HL7 empty markers
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
