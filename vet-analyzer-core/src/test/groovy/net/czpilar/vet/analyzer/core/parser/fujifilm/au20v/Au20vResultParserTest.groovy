package net.czpilar.vet.analyzer.core.parser.fujifilm.au20v

import net.czpilar.vet.analyzer.core.model.AnalyzerType
import net.czpilar.vet.analyzer.core.protocol.fujifilm.FujifilmCommand
import spock.lang.Specification

class Au20vResultParserTest extends Specification {

    def parser = new Au20vResultParser()

    // Real data from AU20V
    static final String REAL_AU20V_DATA = "T,NORMAL ,15-09-2025,15:14,1            ,111          ,             ,49,9,999,01,01,v-PRG   ,<, 0.20    ng/mL ,01,0.00 ,0.00 ,  #        ,0]"

    def "canParse detects T command"() {
        expect:
        parser.canParse(REAL_AU20V_DATA)
        parser.canParse("T,test")
        !parser.canParse("R,test")
        !parser.canParse(null)
    }

    def "parse real AU20V result data"() {
        when:
        def message = parser.parse(REAL_AU20V_DATA)

        then:
        message.analyzerType() == AnalyzerType.AU20V
        message.command() == FujifilmCommand.T
        message.status() == "NORMAL"
        message.date().dayOfMonth == 15
        message.date().monthValue == 9
        message.date().year == 2025
        message.time().hour == 15
        message.time().minute == 14
        message.sampleNumber() == "1"
        message.patientId() == "111"
        message.speciesCode() == 49
        message.sex() == 9
        message.age() == 999
        message.samplePosition() == 1
        message.numberOfTests() == 1
    }

    def "parse test result from AU20V data"() {
        when:
        def message = parser.parse(REAL_AU20V_DATA)
        def results = message.testResults()

        then:
        results.size() == 1
        results[0].testCode() == "v-PRG"
        results[0].relation() == "<"
        results[0].value() == "0.20"
        results[0].unit() == "ng/mL"
        results[0].dilutionFactor() == 1
        results[0].rangeLow() == "0.00"
        results[0].rangeHigh() == "0.00"
    }
}
