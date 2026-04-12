package net.czpilar.vet.analyzer.core.parser.fujifilm.au20v

import net.czpilar.vet.analyzer.core.model.AnalyzerType
import net.czpilar.vet.analyzer.core.model.fujifilm.FujifilmErrorMessage
import net.czpilar.vet.analyzer.core.model.fujifilm.FujifilmWorklistQueryMessage
import net.czpilar.vet.analyzer.core.protocol.fujifilm.FujifilmCommand
import spock.lang.Specification

class Au20vParsersTest extends Specification {

    def "canParse returns true only for correct AU20V command for '#input'"() {
        expect:
        parser.canParse(input) == expected

        where:
        parser                    | input          || expected
        new Au20vWorklistParser() | "X,,,,5"       || true
        new Au20vWorklistParser() | "Y,061201,,,3" || true
        new Au20vWorklistParser() | "I,test"       || false
        new Au20vWorklistParser() | null           || false
        new Au20vResultParser()   | "T,NORMAL"     || true
        new Au20vResultParser()   | "R,NORMAL"     || false
        new Au20vResultParser()   | null           || false
    }

    def "Au20vWorklistParser parses X command"() {
        given:
        def parser = new Au20vWorklistParser()
        def data = "X,061201       ,12345ABCD    ,Tarou Fuji   ,5"

        when:
        FujifilmWorklistQueryMessage msg = parser.parse(data)

        then:
        msg.analyzerType() == AnalyzerType.AU20V
        msg.command() == FujifilmCommand.X
        msg.sampleNumber() == "061201"
        msg.patientId() == "12345ABCD"
        msg.patientName() == "Tarou Fuji"
        msg.numberOfRequests() == 5
    }

    def "Au20vWorklistParser parses Y command (ref range)"() {
        given:
        def parser = new Au20vWorklistParser()
        def data = "Y,061201       ,,,3"

        when:
        FujifilmWorklistQueryMessage msg = parser.parse(data)

        then:
        msg.analyzerType() == AnalyzerType.AU20V
        msg.command() == FujifilmCommand.Y
        msg.sampleNumber() == "061201"
        msg.numberOfRequests() == 3
    }

    def "Au20vWorklistParser parses empty query"() {
        given:
        def parser = new Au20vWorklistParser()
        def data = "X,,,,5"

        when:
        FujifilmWorklistQueryMessage msg = parser.parse(data)

        then:
        msg.sampleNumber() == ""
        msg.patientId() == ""
        msg.patientName() == ""
        msg.numberOfRequests() == 5
    }

    def "Au20vErrorParser parses error message"() {
        given:
        def parser = new Au20vErrorParser()

        when:
        FujifilmErrorMessage msg = parser.parse(input)

        then:
        msg.analyzerType() == AnalyzerType.NX600  // inherits from Nx600ErrorParser
        msg.command() == FujifilmCommand.E
        msg.errorData() == expectedError

        where:
        input                     || expectedError
        "E,002,Measurement error" || "002,Measurement error"
        "E,001,Disc error"        || "001,Disc error"
    }

    def "Au20vResultParser parses multiple test results"() {
        given:
        def parser = new Au20vResultParser()
        def data = "T,NORMAL ,15-09-2025,15:14,1            ,111          ,             ,49,9,999,01,01,v-PRG   ,<, 0.20    ng/mL ,01,0.00 ,0.00 ,  #        "

        when:
        def msg = parser.parse(data)

        then:
        msg.analyzerType() == AnalyzerType.AU20V
        msg.numberOfTests() == 1
        msg.testResults().size() == 1
        msg.testResults()[0].testCode() == "v-PRG"
        msg.testResults()[0].relation() == "<"
    }
}
