package net.czpilar.vet.analyzer.core.protocol.fujifilm;

import net.czpilar.vet.analyzer.core.protocol.ControlCharacters;

import java.nio.charset.StandardCharsets;

public final class StxEtxProtocol {

    private StxEtxProtocol() {
    }

    /**
     * Calculates BCC (Block Check Character) using XOR from after STX to ETX inclusive.
     */
    public static byte calculateBcc(byte[] data, int fromInclusive, int toInclusive) {
        byte bcc = 0;
        for (int i = fromInclusive; i <= toInclusive; i++) {
            bcc ^= data[i];
        }
        return bcc;
    }

    /**
     * Validates BCC of a complete STX...ETX+BCC frame.
     */
    public static boolean validateBcc(byte[] frame) {
        if (frame.length < 4) {
            return false;
        }
        if (frame[0] != ControlCharacters.STX) {
            return false;
        }

        int etxIndex = -1;
        for (int i = frame.length - 2; i >= 1; i--) {
            if (frame[i] == ControlCharacters.ETX) {
                etxIndex = i;
                break;
            }
        }
        if (etxIndex < 0) {
            return false;
        }

        byte expectedBcc = calculateBcc(frame, 1, etxIndex);
        return expectedBcc == frame[etxIndex + 1];
    }

    /**
     * Wraps payload in STX...ETX+BCC frame.
     */
    public static byte[] frame(byte[] payload) {
        byte[] result = new byte[payload.length + 3]; // STX + payload + ETX + BCC
        result[0] = ControlCharacters.STX;
        System.arraycopy(payload, 0, result, 1, payload.length);
        result[payload.length + 1] = ControlCharacters.ETX;
        result[payload.length + 2] = calculateBcc(result, 1, payload.length + 1);
        return result;
    }

    /**
     * Strips STX, ETX and BCC from a frame, returning the payload.
     */
    public static String unframe(byte[] data) {
        if (data.length < 3) {
            return new String(data, StandardCharsets.ISO_8859_1);
        }

        int start = 0;
        int end = data.length;

        if (data[0] == ControlCharacters.STX) {
            start = 1;
        }

        // Find ETX from end
        for (int i = data.length - 1; i >= start; i--) {
            if (data[i] == ControlCharacters.ETX) {
                end = i;
                break;
            }
        }

        return new String(data, start, end - start, StandardCharsets.ISO_8859_1);
    }

    /**
     * Detects if data has STX/ETX framing.
     */
    public static boolean isFramed(byte[] data) {
        return data.length > 0 && data[0] == ControlCharacters.STX;
    }
}
