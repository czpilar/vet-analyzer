package net.czpilar.vet.analyzer.core.protocol.hl7;

import net.czpilar.vet.analyzer.core.protocol.ControlCharacters;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class Hl7Protocol {

    private static final DateTimeFormatter HL7_DATETIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private Hl7Protocol() {
    }

    /**
     * Wraps an HL7 message in MLLP framing: VT + message + FS + CR
     */
    public static byte[] wrapMllp(String hl7Message) {
        byte[] messageBytes = hl7Message.getBytes(StandardCharsets.UTF_8);
        byte[] result = new byte[messageBytes.length + 3];
        result[0] = ControlCharacters.MLLP_START;
        System.arraycopy(messageBytes, 0, result, 1, messageBytes.length);
        result[result.length - 2] = ControlCharacters.MLLP_END;
        result[result.length - 1] = ControlCharacters.CR;
        return result;
    }

    /**
     * Unwraps an MLLP-framed message, returning the HL7 content.
     */
    public static String unwrapMllp(byte[] data) {
        int start = 0;
        int end = data.length;

        if (data.length > 0 && data[0] == ControlCharacters.MLLP_START) {
            start = 1;
        }
        if (data.length > 1 && data[data.length - 1] == ControlCharacters.CR) {
            end--;
        }
        if (end > start && data[end - 1] == ControlCharacters.MLLP_END) {
            end--;
        }

        return new String(data, start, end - start, StandardCharsets.UTF_8);
    }

    /**
     * Creates an HL7 ACK message for the given message control ID.
     */
    public static String createAck(String messageControlId) {
        String timestamp = LocalDateTime.now().format(HL7_DATETIME);
        return "MSH|^~\\&|VetAnalyzer||||" + timestamp + "||ACK^R01|ACK_" + messageControlId + "|P|2.7\r"
                + "MSA|AA|" + messageControlId + "\r";
    }

    /**
     * Detects if raw data starts with MLLP framing or HL7 MSH header.
     */
    public static boolean isHl7(byte[] data) {
        if (data.length == 0) {
            return false;
        }
        if (data[0] == ControlCharacters.MLLP_START) {
            return true;
        }
        // Check for "MSH|" prefix
        if (data.length >= 4) {
            return data[0] == 'M' && data[1] == 'S' && data[2] == 'H' && data[3] == '|';
        }
        return false;
    }
}
