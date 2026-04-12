package net.czpilar.vet.analyzer.core.parser.fujifilm.nx600

import net.czpilar.vet.analyzer.core.model.AnalyzerType
import net.czpilar.vet.analyzer.core.protocol.fujifilm.FujifilmCommand
import spock.lang.Specification

class Nx600ResultParserTest extends Specification {

    def parser = new Nx600ResultParser()

    // Real data from NX600
    static final String REAL_NX600_DATA = "R,NORMAL ,14-06-2019,09:28,8            ,006532       ,             ,16,9,255,01,7 ,TP-PS   ,=,74       g/l   ,1 ,55   ,75   ,           ,ALP-PS  ,=,4.51     ukat/l,1 ,0.10 ,4.00 ,H          ,GLU-PS  ,=,5.5      mmol/l,1 ,3.1  ,6.7  ,           ,GPT-PS  ,=,9.53     ukat/l,1 ,0.10 ,1.00 ,H          ,CRE-PS  ,=,74       umol/l,1 ,35   ,110  ,           ,BUN-PS  ,=,8.86     mmol/l,1 ,3.30 ,8.30 ,H          ,BUN/CRE ,=,119.7    (SI)  ,1 ,50.3 ,128.1,           "

    def "canParse detects R command"() {
        expect:
        parser.canParse(REAL_NX600_DATA)
        parser.canParse("R,test")
        !parser.canParse("T,test")
        !parser.canParse("MSH|test")
        !parser.canParse(null)
    }

    def "parse real NX600 result data"() {
        when:
        def message = parser.parse(REAL_NX600_DATA)

        then:
        message.analyzerType() == AnalyzerType.NX600
        message.command() == FujifilmCommand.R
        message.status() == "NORMAL"
        message.date().dayOfMonth == 14
        message.date().monthValue == 6
        message.date().year == 2019
        message.time().hour == 9
        message.time().minute == 28
        message.sampleNumber() == "8"
        message.patientId() == "006532"
        message.speciesCode() == 16
        message.sex() == 9
        message.age() == 255
        message.samplePosition() == 1
        message.numberOfTests() == 7
    }

    def "parse test results from NX600 data"() {
        when:
        def message = parser.parse(REAL_NX600_DATA)
        def results = message.testResults()

        then:
        results.size() == 7

        and: "first test result TP-PS"
        results[0].testCode() == "TP-PS"
        results[0].relation() == "="
        results[0].value() == "74"
        results[0].unit() == "g/l"
        results[0].dilutionFactor() == 1
        results[0].rangeLow() == "55"
        results[0].rangeHigh() == "75"
        results[0].flag() == ""

        and: "ALP-PS has H flag"
        results[1].testCode() == "ALP-PS"
        results[1].value() == "4.51"
        results[1].unit() == "ukat/l"
        results[1].flag() == "H"

        and: "BUN/CRE is last"
        results[6].testCode() == "BUN/CRE"
        results[6].value() == "119.7"
        results[6].unit() == "(SI)"
    }
}
