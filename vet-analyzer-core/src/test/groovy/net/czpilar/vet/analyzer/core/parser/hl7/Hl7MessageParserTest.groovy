package net.czpilar.vet.analyzer.core.parser.hl7

import net.czpilar.vet.analyzer.core.model.AnalyzerType
import spock.lang.Specification

class Hl7MessageParserTest extends Specification {

    def parser = new Hl7MessageParser()

    // Real data from Exigo H400 / BM850
    static final String REAL_HL7_DATA = """\
MSH|^~\\&|BM850^HL7MW|||||20190902135341|ORU^R01|BM_1|P|2.7||||||UNICODE UTF-8
SFT|Boule Medical AB|r14892 branches/rel-2.2|E|r14892 branches/rel-2.2
OBR|1||68|12345||||||||||||||||||201909021206
NTE|1||""
OBX|1|ST|ID2||""||||||\u0050
OBX|2|ST|PROF||PES (3 POP)||||||\u0050
OBX|3|ST|METH||OT||||||\u0050
OBX|4|ST|OPID||""||||||\u0050
OBX|5|NM|RBC||3.74|10*12/L|5.50-8.50|ER|||\u0050
OBX|6|NM|MCV||89.7|fL|60.0-72.0|ER|||\u0050
OBX|7|NM|HCT||33.5|%|37.0-55.0|ER|||\u0050
OBX|8|NM|MCH||30.5|pg|19.5-25.5|ER|||\u0050
OBX|9|NM|MCHC||34.0|g/dL|32.0-38.5|ER|||\u0050
OBX|10|NM|RDWR||19.7|%|12.0-17.5|ER|||\u0050
OBX|11|NM|RDWA||85.8|fL|35.0-65.0|ER|||\u0050
OBX|12|NM|PLT||266|10*9/L|200-500|ER|||\u0050
OBX|13|NM|MPV||11.3|fL|5.5-10.5|ER|||\u0050
OBX|14|NM|HGB||11.4|g/dL|12.0-18.0|ER|||\u0050
OBX|15|NM|WBC||7.8|10*9/L|6.0-17.0|ER|||\u0050
OBX|16|NM|LYMA||3.8|10*9/L|0.9-5.0|ER|||\u0050
OBX|17|NM|MONA||0.5|10*9/L|0.3-1.5|ER|||\u0050
OBX|18|NM|GRNA||3.5|10*9/L|3.5-12.0|ER|||\u0050
OBX|19|NM|LYMR||47.9|%|0.0-99.9|ER|||\u0050
OBX|20|NM|MONR||7.2|%|0.0-99.9|ER|||\u0050
OBX|21|NM|GRNR||44.9|%|0.0-99.9|ER|||\u0050"""

    def "canParse detects HL7 message"() {
        expect:
        parser.canParse(REAL_HL7_DATA)
        parser.canParse("MSH|^~\\&|test")
        !parser.canParse("R,NORMAL ,14-06-2019")
        !parser.canParse(null)
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
