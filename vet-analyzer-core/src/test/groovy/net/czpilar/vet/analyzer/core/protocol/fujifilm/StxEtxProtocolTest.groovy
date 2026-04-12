package net.czpilar.vet.analyzer.core.protocol.fujifilm

import net.czpilar.vet.analyzer.core.protocol.ControlCharacters
import spock.lang.Specification

class StxEtxProtocolTest extends Specification {

    def "calculateBcc computes XOR correctly for '#input'"() {
        given:
        byte[] data = input as byte[]

        expect:
        StxEtxProtocol.calculateBcc(data, 0, data.length - 1) == (byte) expected

        where:
        input              || expected
        [0x41]             || 0x41
        [0x41, 0x42]       || 0x41 ^ 0x42
        [0x41, 0x42, 0x43] || 0x41 ^ 0x42 ^ 0x43
        [0x52, 0x2C, 0x4E] || 0x52 ^ 0x2C ^ 0x4E
    }

    def "validateBcc returns #expected for #description"() {
        expect:
        StxEtxProtocol.validateBcc(input as byte[]) == expected

        where:
        description        | input                                                     || expected
        "valid frame"      | StxEtxProtocol.frame("R,TEST".getBytes())                 || true
        "valid long frame" | StxEtxProtocol.frame("T,NORMAL ,15-09".getBytes())        || true
        "corrupted BCC"    | corruptBcc(StxEtxProtocol.frame("R,TEST".getBytes()))      || false
        "too short"        | [0x02, 0x03]                                              || false
        "empty"            | []                                                        || false
        "no STX"           | [0x41, 0x42, 0x03, 0x00]                                  || false
    }

    def "frame and unframe are inverse for '#original'"() {
        expect:
        StxEtxProtocol.unframe(StxEtxProtocol.frame(original.getBytes())) == original

        where:
        original << [
            "R,NORMAL",
            "T,NORMAL ,15-09-2025,15:14,1",
            "I,061201,3",
            "W,2006061202",
            "E,001,Sample tray error",
            "S,NORMAL ,12-06-2006,10:50,1"
        ]
    }

    def "isFramed returns #expected for #description"() {
        expect:
        StxEtxProtocol.isFramed(input as byte[]) == expected

        where:
        description  | input                             || expected
        "STX prefix" | [ControlCharacters.STX, 0x52]     || true
        "plain R"    | [0x52, 0x2C]                      || false
        "empty"      | []                                || false
    }

    private static byte[] corruptBcc(byte[] frame) {
        frame[frame.length - 1] = (byte) 0xFF
        return frame
    }
}
