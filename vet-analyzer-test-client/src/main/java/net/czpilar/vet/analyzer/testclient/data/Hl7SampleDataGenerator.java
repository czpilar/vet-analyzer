package net.czpilar.vet.analyzer.testclient.data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Hl7SampleDataGenerator {

    private static final DateTimeFormatter HL7_DATETIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final DateTimeFormatter HL7_DATETIME_SHORT = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

    public String generateResultMessage(String sampleId) {
        String now = LocalDateTime.now().format(HL7_DATETIME);
        String nowShort = LocalDateTime.now().format(HL7_DATETIME_SHORT);
        return "MSH|^~\\&|BM850^HL7MW|||||" + now + "|ORU^R01|BM_1|P|2.7||||||UNICODE UTF-8\r"
                + "SFT|Boule Medical AB|r14892 branches/rel-2.2|E|r14892 branches/rel-2.2\r"
                + "OBR|1||" + sampleId + "|" + sampleId + "||||||||||||||||||" + nowShort + "\r"
                + "NTE|1||\"\"\r"
                + "OBX|1|ST|ID2||\"\"||||||\u0050\r"
                + "OBX|2|ST|PROF||PES (3 POP)||||||\u0050\r"
                + "OBX|3|ST|METH||OT||||||\u0050\r"
                + "OBX|4|NM|RBC||6.52|10*12/L|5.50-8.50|||\u0050\r"
                + "OBX|5|NM|MCV||68.2|fL|60.0-72.0|||\u0050\r"
                + "OBX|6|NM|HCT||44.5|%|37.0-55.0|||\u0050\r"
                + "OBX|7|NM|MCH||22.1|pg|19.5-25.5|||\u0050\r"
                + "OBX|8|NM|MCHC||32.4|g/dL|32.0-38.5|||\u0050\r"
                + "OBX|9|NM|PLT||312|10*9/L|200-500|||\u0050\r"
                + "OBX|10|NM|HGB||14.4|g/dL|12.0-18.0|||\u0050\r"
                + "OBX|11|NM|WBC||9.2|10*9/L|6.0-17.0|||\u0050\r"
                + "OBX|12|NM|LYMA||2.4|10*9/L|0.9-5.0|||\u0050\r"
                + "OBX|13|NM|MONA||0.7|10*9/L|0.3-1.5|||\u0050\r"
                + "OBX|14|NM|GRNA||6.1|10*9/L|3.5-12.0|||\u0050";
    }
}
