package net.czpilar.vet.analyzer.core.model

import spock.lang.Specification

class AnalyzerTypeTest extends Specification {

    def "all analyzer types have non-blank display name and category"() {
        expect:
        type.displayName() != null && !type.displayName().isBlank()
        type.category() != null && !type.category().isBlank()

        where:
        type << AnalyzerType.values()
    }

    def "displayName and category match expected"() {
        expect:
        type.displayName() == displayName
        type.category() == category

        where:
        type                     || displayName            | category
        AnalyzerType.BM850_EXIGO || "BM850/EXIGO H400"     | "Hematology"
        AnalyzerType.NX600       || "Fujifilm NX600"       | "Biochemistry"
        AnalyzerType.AU20V       || "Fujifilm AU20V"       | "Immunoassay"
    }

    def "values returns 3 entries"() {
        expect:
        AnalyzerType.values().length == 3
    }
}
