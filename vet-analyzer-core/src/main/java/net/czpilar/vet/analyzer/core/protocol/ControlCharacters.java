package net.czpilar.vet.analyzer.core.protocol;

public final class ControlCharacters {

    public static final byte STX = 0x02;
    public static final byte ETX = 0x03;
    public static final byte EOT = 0x04;
    public static final byte ENQ = 0x05;
    public static final byte ACK = 0x06;
    public static final byte NAK = 0x15;
    public static final byte ETB = 0x17;
    public static final byte CR = 0x0D;
    public static final byte LF = 0x0A;

    // MLLP (Minimum Lower Layer Protocol) for HL7
    public static final byte MLLP_START = 0x0B; // VT (Vertical Tab)
    public static final byte MLLP_END = 0x1C;   // FS (File Separator)

    private ControlCharacters() {
    }
}
