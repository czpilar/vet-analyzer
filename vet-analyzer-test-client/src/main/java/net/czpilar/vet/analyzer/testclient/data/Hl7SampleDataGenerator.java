package net.czpilar.vet.analyzer.testclient.data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Hl7SampleDataGenerator {

    private static final DateTimeFormatter HL7_DATETIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final DateTimeFormatter HL7_DATETIME_SHORT = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

    public String generateResultMessage(String sampleId, String name) {
        String now = LocalDateTime.now().format(HL7_DATETIME);
        String nowShort = LocalDateTime.now().format(HL7_DATETIME_SHORT);
        return "MSH|^~\\&|BM850^HL7MW|||||" + now + "|ORU^R01|BM_1|P|2.7||||||UNICODE UTF-8\r"
                + "SFT|Boule Medical AB|r28391 branches/rel-2.3.11|E|r28391 branches/rel-2.3.11\r"
                + "OBR|1||" + sampleId + "|" + name + "||||||||||||||||||" + nowShort + "\r"
                + "NTE|1||\"\"\r"
                + "OBX|1|ST|ID2||\"\"||||||P\r"
                + "OBX|2|ST|PROF||PES (3POP)||||||P\r"
                + "OBX|3|ST|METH||OT||||||P\r"
                + "OBX|4|ST|OPID||\"\"||||||P\r"
                + "OBX|5|NM|RBC||5.04|10*12/L|5.50-8.50|\"\"|||P\r"
                + "OBX|6|NM|MCV||68.5|fL|60.0-72.0|\"\"|||P\r"
                + "OBX|7|NM|HCT||34.5|%|37.0-55.0|\"\"|||P\r"
                + "OBX|8|NM|MCH||24.6|pg|19.5-25.5|\"\"|||P\r"
                + "OBX|9|NM|MCHC||35.9|g/dL|32.0-38.5|\"\"|||P\r"
                + "OBX|10|NM|RDWR||15.2|%|12.0-17.5|\"\"|||P\r"
                + "OBX|11|NM|RDWA||46.7|fL|35.0-65.0|\"\"|||P\r"
                + "OBX|12|NM|PLT||375|10*9/L|200-500|\"\"|||P\r"
                + "OBX|13|NM|MPV||7.7|fL|5.5-10.5|\"\"|||P\r"
                + "OBX|14|NM|HGB||12.4|g/dL|12.0-18.0|\"\"|||P\r"
                + "OBX|15|NM|WBC||12.9|10*9/L|6.0-17.0|\"\"|||P\r"
                + "OBX|16|NM|LYMA||1.1|10*9/L|0.9-5.0|\"\"|||P\r"
                + "OBX|17|NM|MONA||0.6|10*9/L|0.3-1.5|\"\"|||P\r"
                + "OBX|18|NM|GRNA||11.2|10*9/L|3.5-12.0|\"\"|||P\r"
                + "OBX|19|NM|NEUA||9.8|10*9/L|3.0-12.0|\"\"|||P\r"
                + "OBX|20|NM|LYMR||8.7|%|0.0-99.9|\"\"|||P\r"
                + "OBX|21|NM|MONR||4.9|%|0.0-99.9|\"\"|||P\r"
                + "OBX|22|NM|GRNR||86.4|%|0.0-99.9|\"\"|||P\r"
                + "OBX|23|NM|NEUR||76.0|%|0.0-99.9|\"\"|||P\r"
                + "OBX|24|NM|EOSA||1.4|10*9/L|0.1-1.5|\"\"|||P\r"
                + "OBX|25|NM|EOSR||10.4|%|0.1-99.9|\"\"|||P";
    }
}
