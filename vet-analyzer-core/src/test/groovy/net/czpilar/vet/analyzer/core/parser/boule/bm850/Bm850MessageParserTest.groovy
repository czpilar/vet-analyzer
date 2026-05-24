package net.czpilar.vet.analyzer.core.parser.boule.bm850

import net.czpilar.vet.analyzer.core.model.AnalyzerType
import spock.lang.Specification

class Bm850MessageParserTest extends Specification {

    def parser = new Bm850MessageParser()

    // Real data from Exigo H400 / BM850
    static final String REAL_HL7_DATA = """\
MSH|^~\\&|BM850^HL7MW|||||20190902135341|ORU^R01|BM_1|P|2.7||||||UNICODE UTF-8
SFT|Boule Medical AB|r14892 branches/rel-2.2|E|r14892 branches/rel-2.2
OBR|1||68|12345||||||||||||||||||201909021206
NTE|1||""
OBX|1|ST|ID2||""||||||P
OBX|2|ST|PROF||PES (3 POP)||||||P
OBX|3|ST|METH||OT||||||P
OBX|4|ST|OPID||""||||||P
OBX|5|NM|RBC||3.74|10*12/L|5.50-8.50|ER|||P
OBX|6|NM|MCV||89.7|fL|60.0-72.0|ER|||P
OBX|7|NM|HCT||33.5|%|37.0-55.0|ER|||P
OBX|8|NM|MCH||30.5|pg|19.5-25.5|ER|||P
OBX|9|NM|MCHC||34.0|g/dL|32.0-38.5|ER|||P
OBX|10|NM|RDWR||19.7|%|12.0-17.5|ER|||P
OBX|11|NM|RDWA||85.8|fL|35.0-65.0|ER|||P
OBX|12|NM|PLT||266|10*9/L|200-500|ER|||P
OBX|13|NM|MPV||11.3|fL|5.5-10.5|ER|||P
OBX|14|NM|HGB||11.4|g/dL|12.0-18.0|ER|||P
OBX|15|NM|WBC||7.8|10*9/L|6.0-17.0|ER|||P
OBX|16|NM|LYMA||3.8|10*9/L|0.9-5.0|ER|||P
OBX|17|NM|MONA||0.5|10*9/L|0.3-1.5|ER|||P
OBX|18|NM|GRNA||3.5|10*9/L|3.5-12.0|ER|||P
OBX|19|NM|LYMR||47.9|%|0.0-99.9|ER|||P
OBX|20|NM|MONR||7.2|%|0.0-99.9|ER|||P
OBX|21|NM|GRNR||44.9|%|0.0-99.9|ER|||P"""

    def "canParse accepts BM850 HL7 v2.7 message"() {
        expect:
        parser.canParse(REAL_HL7_DATA)
    }

    def "canParse accepts EXIGO sending application"() {
        expect:
        parser.canParse("MSH|^~\\&|EXIGO^HL7MW|||||20190902135341|ORU^R01|E_1|P|2.7\rOBR|1||1")
    }

    def "canParse rejects non-HL7 input"() {
        expect:
        !parser.canParse("R,NORMAL ,14-06-2019")
        !parser.canParse(null)
        !parser.canParse("")
        !parser.canParse("not an HL7 message")
    }

    def "canParse rejects HL7 message from a different device"() {
        given:
        // VCheck V200 announces itself as "Vcheck" or "BIONOTE" in MSH-3
        def vcheckMessage = "MSH|^~\\&|Vcheck^BIONOTE|||||20210101120000|ORU^R01|V_1|P|2.6\rOBR|1||1"

        expect:
        !parser.canParse(vcheckMessage)
    }

    def "canParse rejects BM850 with wrong HL7 version"() {
        given:
        def wrongVersion = "MSH|^~\\&|BM850^HL7MW|||||20190902135341|ORU^R01|BM_1|P|2.5\rOBR|1||1"

        expect:
        !parser.canParse(wrongVersion)
    }

    def "canParse rejects HL7 v2.7 with unknown sending application"() {
        given:
        def fooLab = "MSH|^~\\&|FooLab|||||20190902135341|ORU^R01|F_1|P|2.7\rOBR|1||1"

        expect:
        !parser.canParse(fooLab)
    }

    def "parse real BM850 HL7 data"() {
        when:
        def message = parser.parse(REAL_HL7_DATA)

        then:
        message.analyzerType() == AnalyzerType.BM850_EXIGO
        message.messageType() == "ORU^R01"
        message.hl7Version() == "2.7"
        message.sendingApplication() == "BM850^HL7MW"
        message.messageControlId() == "BM_1"
        message.sampleId() == "68"
        message.universalServiceId() == "12345"
        message.observations().size() == 21
    }

    def "parse HL7 OBX observations correctly"() {
        when:
        def message = parser.parse(REAL_HL7_DATA)
        def rbc = message.observations().find { it.observationId() == "RBC" }

        then:
        rbc != null
        rbc.valueType() == "NM"
        rbc.value() == "3.74"
        rbc.unit() == "10*12/L"
        rbc.referenceRange() == "5.50-8.50"
        rbc.abnormalFlag() == "ER"
    }

    def "parse OBX unwraps HL7 null marker across all cleaned text fields"() {
        given:
        // OBX-22 is real BM850 output with the explicit-null marker (HL7 v2 `""`)
        // in value (OBX-5) and abnormalFlag (OBX-8). OBX-23 pushes the same marker
        // through every other OBX text field the parser cleans (valueType,
        // observationId, unit, referenceRange, observationStatus) to verify HL7's
        // null-encoding rule is honoured for all ST/ID/IS fields.
        def message = """\
MSH|^~\\&|BM850^HL7MW|||||20190902135341|ORU^R01|BM_1|P|2.7
OBR|1||68|12345
OBX|22|NM|EOSA||""|10*9/L|0.1-1.5|""|||P
OBX|23|""|""||""|""|""|""|||""
"""

        when:
        def parsed = parser.parse(message)
        def eosa = parsed.observations().find { it.observationId() == "EOSA" }
        def allNull = parsed.observations().find { it.setId() == 23 }

        then:
        eosa.value() == ""
        eosa.abnormalFlag() == ""
        eosa.unit() == "10*9/L"
        eosa.referenceRange() == "0.1-1.5"
        eosa.observationStatus() == "P"

        and:
        allNull.valueType() == ""
        allNull.observationId() == ""
        allNull.value() == ""
        allNull.unit() == ""
        allNull.referenceRange() == ""
        allNull.abnormalFlag() == ""
        allNull.observationStatus() == ""
    }

    def "parse HL7 datetime correctly"() {
        when:
        def message = parser.parse(REAL_HL7_DATA)

        then:
        message.messageDateTime() != null
        message.messageDateTime().year == 2019
        message.messageDateTime().monthValue == 9
        message.messageDateTime().dayOfMonth == 2
        message.messageDateTime().hour == 13
        message.messageDateTime().minute == 53
    }

    def "toMeasurementResult converts observation"() {
        when:
        def message = parser.parse(REAL_HL7_DATA)
        def wbc = message.observations().find { it.observationId() == "WBC" }
        def result = wbc.toMeasurementResult()

        then:
        result.testCode() == "WBC"
        result.value() == "7.8"
        result.unit() == "10*9/L"
        result.rangeLow() == "6.0"
        result.rangeHigh() == "17.0"
    }
}
