package net.czpilar.vet.analyzer.core.protocol.fujifilm;

public enum FujifilmCommand {

    I("Worklist index query", true),
    W("Sample info query", true),
    S("Measurement start", false),
    R("Measurement results", false),
    T("Measurement results (AU20V)", false),
    E("Error information", false),
    X("Order index query (AU20V)", true),
    Y("Order index query with ref range (AU20V)", true);

    private final String description;
    private final boolean requiresResponse;

    FujifilmCommand(String description, boolean requiresResponse) {
        this.description = description;
        this.requiresResponse = requiresResponse;
    }

    public String description() {
        return description;
    }

    public boolean requiresResponse() {
        return requiresResponse;
    }

    public static FujifilmCommand fromCode(String code) {
        if (code == null || code.isEmpty()) {
            throw new IllegalArgumentException("Command code must not be null or empty");
        }
        try {
            return valueOf(code.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown Fujifilm command code: " + code);
        }
    }

    /**
     * Returns true if this command is specific to AU20V (not used by NX600).
     */
    public boolean isAu20vSpecific() {
        return this == T || this == X || this == Y;
    }
}
