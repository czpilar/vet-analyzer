package net.czpilar.vet.analyzer.core.protocol.hl7

import net.czpilar.vet.analyzer.core.protocol.ControlCharacters
import spock.lang.Specification

class Hl7ProtocolTest extends Specification {

    def "wrapMllp and unwrapMllp are inverse for '#message'"() {
        expect:
        Hl7Protocol.unwrapMllp(Hl7Protocol.wrapMllp(message)) == message

        where:
        message << [
            "MSH|^~\\&|test",
            "MSH|^~\\&|BM850^HL7MW|||||20190902135341|ORU^R01|BM_1|P|2.7",
            "MSA|AA|BM_1",
            ""
        ]
    }

    def "wrapMllp produces correct framing"() {
        when:
        byte[] wrapped = Hl7Protocol.wrapMllp("MSH|test")

        then:
        wrapped[0] == ControlCharacters.MLLP_START
        wrapped[wrapped.length - 2] == ControlCharacters.MLLP_END
        wrapped[wrapped.length - 1] == ControlCharacters.CR
        wrapped.length == "MSH|test".length() + 3
    }

    def "createAck for controlId '#controlId' contains expected segments"() {
        when:
        def ack = Hl7Protocol.createAck(controlId)

        then:
        ack.contains("MSH|^~\\&|VetAnalyzer")
        ack.contains("ACK^R01")
        ack.contains("ACK_" + controlId)
        ack.contains("MSA|AA|" + controlId)

        where:
        controlId << ["BM_1", "BM_999", "TEST_ID"]
    }

    def "isHl7 returns #expected for #description"() {
        expect:
        Hl7Protocol.isHl7(input as byte[]) == expected

        where:
        description  | input                                                    || expected
        "MLLP start" | [ControlCharacters.MLLP_START, 0x4D, 0x53, 0x48]        || true
        "MSH| prefix"| [0x4D, 0x53, 0x48, 0x7C]                                || true
        "R command"  | [0x52, 0x2C, 0x4E, 0x4F]                                || false
        "T command"  | [0x54, 0x2C, 0x4E, 0x4F]                                || false
        "STX framed" | [ControlCharacters.STX, 0x52, 0x2C, 0x4E]               || false
        "empty"      | []                                                      || false
        "short"      | [0x4D, 0x53]                                            || false
    }
}
