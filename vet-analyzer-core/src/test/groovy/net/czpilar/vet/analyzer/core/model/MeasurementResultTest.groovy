package net.czpilar.vet.analyzer.core.model

import spock.lang.Specification

class MeasurementResultTest extends Specification {

    def "toString includes test code, relation, value, unit"() {
        given:
        def m = new MeasurementResult("HGB", "=", "14.1", "g/dL", 1, "12", "16", "")

        expect:
        m.toString() == "HGB=14.1 g/dL [12-16]"
    }

    def "toString omits range section when ranges are blank or null"() {
        expect:
        new MeasurementResult("X", "=", "1", "u", 1, low, high, "").toString() == "X=1 u"

        where:
        low   | high
        null  | null
        ""    | ""
        "  "  | "  "
        "1"   | null
        null  | "1"
        ""    | "1"
    }

    def "toString appends warnings when present"() {
        expect:
        new MeasurementResult("X", "<", "0.2", "u", 1, "", "", "H").toString() == "X<0.2 u H"
    }

    def "toString omits empty warnings"() {
        expect:
        new MeasurementResult("X", "=", "1", "u", 1, "", "", warnings).toString() == "X=1 u"

        where:
        warnings << [null, "", "  "]
    }
}
