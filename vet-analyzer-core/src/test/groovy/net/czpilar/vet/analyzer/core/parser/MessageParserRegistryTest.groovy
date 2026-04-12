package net.czpilar.vet.analyzer.core.parser

import net.czpilar.vet.analyzer.core.model.AnalyzerType
import net.czpilar.vet.analyzer.core.model.fujifilm.FujifilmResultMessage
import net.czpilar.vet.analyzer.core.model.hl7.Hl7Message
import spock.lang.Specification

class MessageParserRegistryTest extends Specification {

    def registry = MessageParserRegistry.createDefault()

    def "registry detects HL7 message"() {
        given:
        def hl7Data = "MSH|^~\\&|BM850^HL7MW|||||20190902135341|ORU^R01|BM_1|P|2.7\rOBR|1||68|12345"

        when:
        def message = registry.parse(hl7Data)

        then:
        message instanceof Hl7Message
        message.analyzerType() == AnalyzerType.BM850_EXIGO
    }

    def "registry detects NX600 R message"() {
        given:
        def nx600Data = "R,NORMAL ,14-06-2019,09:28,8,006532,,16,9,255,01,1,TP-PS,=,74 g/l,1,55,75,"

        when:
        def message = registry.parse(nx600Data)

        then:
        message instanceof FujifilmResultMessage
        message.analyzerType() == AnalyzerType.NX600
    }

    def "registry detects AU20V T message"() {
        given:
        def au20vData = "T,NORMAL ,15-09-2025,15:14,1,111,,49,9,999,01,01,v-PRG,<,0.20 ng/mL,01,0.00,0.00,#"

        when:
        def message = registry.parse(au20vData)

        then:
        message instanceof FujifilmResultMessage
        message.analyzerType() == AnalyzerType.AU20V
    }

    def "registry returns null for unknown data"() {
        when:
        def message = registry.parse("some random data")

        then:
        message == null
    }
}
