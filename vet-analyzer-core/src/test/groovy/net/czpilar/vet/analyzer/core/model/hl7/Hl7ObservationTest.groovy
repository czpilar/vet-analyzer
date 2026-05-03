package net.czpilar.vet.analyzer.core.model.hl7

import net.czpilar.vet.analyzer.core.model.MeasurementResult
import spock.lang.Specification

class Hl7ObservationTest extends Specification {

    def "toMeasurementResult parses 'low-high' reference range"() {
        given:
        def obs = new Hl7Observation(1, "NM", "HGB", "14.1", "g/dL", "12-16", "N", "F")

        when:
        MeasurementResult result = obs.toMeasurementResult()

        then:
        result.testCode() == "HGB"
        result.relation() == "="
        result.value() == "14.1"
        result.unit() == "g/dL"
        result.dilutionFactor() == 1
        result.rangeLow() == "12"
        result.rangeHigh() == "16"
        result.warnings() == "N"
    }

    def "toMeasurementResult trims whitespace around range parts"() {
        given:
        def obs = new Hl7Observation(1, "NM", "HGB", "14.1", "g/dL", "  12  -  16  ", "", "F")

        when:
        def result = obs.toMeasurementResult()

        then:
        result.rangeLow() == "12"
        result.rangeHigh() == "16"
    }

    def "toMeasurementResult leaves rangeLow/High empty when reference range has no dash"() {
        expect:
        def result = new Hl7Observation(1, "NM", "X", "1", "u", refRange, "", "F").toMeasurementResult()
        result.rangeLow() == ""
        result.rangeHigh() == ""

        where:
        refRange << [null, "", "no-dash-but-trick"].findAll { it != null && !it.contains("-") } + [null, ""]
    }

    def "toMeasurementResult uses '=' as relation regardless"() {
        expect:
        new Hl7Observation(1, "NM", "X", "1", "u", "0-100", "", "F").toMeasurementResult().relation() == "="
    }
}
