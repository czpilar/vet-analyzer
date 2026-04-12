package net.czpilar.vet.analyzer.core.parser.fujifilm.nx600

import net.czpilar.vet.analyzer.core.model.AnalyzerType
import net.czpilar.vet.analyzer.core.model.fujifilm.FujifilmErrorMessage
import net.czpilar.vet.analyzer.core.model.fujifilm.FujifilmSampleInfoMessage
import net.czpilar.vet.analyzer.core.model.fujifilm.FujifilmStartMessage
import net.czpilar.vet.analyzer.core.model.fujifilm.FujifilmWorklistQueryMessage
import net.czpilar.vet.analyzer.core.protocol.fujifilm.FujifilmCommand
import spock.lang.Specification

class Nx600ParsersTest extends Specification {

    def "canParse returns true only for correct command for '#input'"() {
        expect:
        parser.canParse(input) == expected

        where:
        parser                      | input         || expected
        new Nx600StartParser()      | "S,NORMAL"    || true
        new Nx600StartParser()      | "R,NORMAL"    || false
        new Nx600StartParser()      | null          || false
        new Nx600ErrorParser()      | "E,001,error" || true
        new Nx600ErrorParser()      | "R,NORMAL"    || false
        new Nx600ErrorParser()      | null          || false
        new Nx600WorklistParser()   | "I,061201,3"  || true
        new Nx600WorklistParser()   | "W,test"      || false
        new Nx600WorklistParser()   | null          || false
        new Nx600SampleInfoParser() | "W,061201"    || true
        new Nx600SampleInfoParser() | "I,test"      || false
        new Nx600SampleInfoParser() | null          || false
    }

    def "Nx600StartParser parses start message"() {
        given:
        def parser = new Nx600StartParser()
        def data = "S,NORMAL ,12-06-2006,10:50,2006061201   ,ABCDEFGHIJKLM,Taro Fuji    ,01"

        when:
        FujifilmStartMessage msg = parser.parse(data)

        then:
        msg.analyzerType() == AnalyzerType.NX600
        msg.command() == FujifilmCommand.S
        msg.testCondition() == "NORMAL"
        msg.date().dayOfMonth == 12
        msg.date().monthValue == 6
        msg.date().year == 2006
        msg.time().hour == 10
        msg.time().minute == 50
        msg.sampleNumber() == "2006061201"
        msg.patientId() == "ABCDEFGHIJKLM"
        msg.patientName() == "Taro Fuji"
        msg.samplePosition() == 1
    }

    def "Nx600ErrorParser parses error message '#input'"() {
        given:
        def parser = new Nx600ErrorParser()

        when:
        FujifilmErrorMessage msg = parser.parse(input)

        then:
        msg.analyzerType() == AnalyzerType.NX600
        msg.command() == FujifilmCommand.E
        msg.errorData() == expectedError

        where:
        input                     || expectedError
        "E,001,Sample tray error" || "001,Sample tray error"
        "E,999,Unknown"           || "999,Unknown"
        "E,"                      || ""
    }

    def "Nx600WorklistParser parses worklist query"() {
        given:
        def parser = new Nx600WorklistParser()

        when:
        FujifilmWorklistQueryMessage msg = parser.parse(input)

        then:
        msg.analyzerType() == AnalyzerType.NX600
        msg.command() == FujifilmCommand.I
        msg.sampleNumber() == expectedSample
        msg.numberOfRequests() == expectedCount

        where:
        input               || expectedSample | expectedCount
        "I,061201       ,3" || "061201"       | 3
        "I,             ,5" || ""             | 5
        "I,,99"             || ""             | 99
    }

    def "Nx600SampleInfoParser parses sample info query"() {
        given:
        def parser = new Nx600SampleInfoParser()

        when:
        FujifilmSampleInfoMessage msg = parser.parse(input)

        then:
        msg.analyzerType() == AnalyzerType.NX600
        msg.command() == FujifilmCommand.W
        msg.sampleNumber() == expectedSample

        where:
        input          || expectedSample
        "W,2006061202" || "2006061202"
        "W,1"          || "1"
    }
}
