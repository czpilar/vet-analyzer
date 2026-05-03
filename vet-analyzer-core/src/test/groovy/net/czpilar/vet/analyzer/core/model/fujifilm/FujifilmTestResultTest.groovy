package net.czpilar.vet.analyzer.core.model.fujifilm

import net.czpilar.vet.analyzer.core.model.MeasurementResult
import spock.lang.Specification

class FujifilmTestResultTest extends Specification {

    def "toMeasurementResult copies all fields one-to-one"() {
        given:
        def r = new FujifilmTestResult("BUN-PS", "=", "7.50", "mmol/l", 1, "3.0", "10.0", "")

        when:
        MeasurementResult m = r.toMeasurementResult()

        then:
        m.testCode() == "BUN-PS"
        m.relation() == "="
        m.value() == "7.50"
        m.unit() == "mmol/l"
        m.dilutionFactor() == 1
        m.rangeLow() == "3.0"
        m.rangeHigh() == "10.0"
        m.warnings() == ""
    }

    def "toMeasurementResult preserves limit-of-detection relations"() {
        given:
        def r = new FujifilmTestResult("v-TSH", "<", "0.20", "ng/mL", 2, "", "", "L")

        when:
        def m = r.toMeasurementResult()

        then:
        m.relation() == "<"
        m.value() == "0.20"
        m.dilutionFactor() == 2
        m.warnings() == "L"
    }
}
