package net.czpilar.vet.analyzer.core.model;

public enum AnalyzerType {

    BM850_EXIGO("BM850/EXIGO H400", "Hematology"),
    NX600("Fujifilm NX600", "Biochemistry"),
    AU20V("Fujifilm AU20V", "Immunoassay");

    private final String displayName;
    private final String category;

    AnalyzerType(String displayName, String category) {
        this.displayName = displayName;
        this.category = category;
    }

    public String displayName() {
        return displayName;
    }

    public String category() {
        return category;
    }
}
