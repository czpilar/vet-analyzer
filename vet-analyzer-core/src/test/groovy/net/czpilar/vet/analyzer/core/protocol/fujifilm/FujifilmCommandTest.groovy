package net.czpilar.vet.analyzer.core.protocol.fujifilm

import net.czpilar.vet.analyzer.core.exception.UnknownFujifilmCommandException
import net.czpilar.vet.analyzer.core.exception.VetAnalyzerException
import spock.lang.Specification

class FujifilmCommandTest extends Specification {

    def "fromCode '#code' returns #expected"() {
        expect:
        FujifilmCommand.fromCode(code) == expected

        where:
        code || expected
        "R"  || FujifilmCommand.R
        "T"  || FujifilmCommand.T
        "I"  || FujifilmCommand.I
        "W"  || FujifilmCommand.W
        "S"  || FujifilmCommand.S
        "E"  || FujifilmCommand.E
        "X"  || FujifilmCommand.X
        "Y"  || FujifilmCommand.Y
    }

    def "fromCode throws UnknownFujifilmCommandException for invalid code '#code'"() {
        when:
        FujifilmCommand.fromCode(code)

        then:
        UnknownFujifilmCommandException ex = thrown()
        // Catch-all via VetAnalyzerException base also matches
        ex instanceof VetAnalyzerException

        where:
        code << ["Z", "ABC", "", null]
    }

    def "isAu20vSpecific returns #expected for #command"() {
        expect:
        command.isAu20vSpecific() == expected

        where:
        command             || expected
        FujifilmCommand.T   || true
        FujifilmCommand.X   || true
        FujifilmCommand.Y   || true
        FujifilmCommand.R   || false
        FujifilmCommand.I   || false
        FujifilmCommand.W   || false
        FujifilmCommand.S   || false
        FujifilmCommand.E   || false
    }

    def "requiresResponse returns #expected for #command"() {
        expect:
        command.requiresResponse() == expected

        where:
        command             || expected
        FujifilmCommand.I   || true
        FujifilmCommand.W   || true
        FujifilmCommand.X   || true
        FujifilmCommand.Y   || true
        FujifilmCommand.R   || false
        FujifilmCommand.T   || false
        FujifilmCommand.S   || false
        FujifilmCommand.E   || false
    }
}
