package model;

/**
 * Priority classification for a crisis node (v in V).
 * Ordered from least to most urgent so it can be used for
 * tie-breaking / sorting in the dispatcher UI as described
 * in the "Vulnerability Mitigation" section of the project brief.
 */
public enum PriorityLevel {
    NONE("NONE", 0),
    MODERATE("MODERATE", 1),
    HIGH("HIGH", 2),
    CRITICAL("CRITICAL", 3);

    private final String label;
    private final int rank;

    PriorityLevel(String label, int rank) {
        this.label = label;
        this.rank = rank;
    }

    public String getLabel() {
        return label;
    }

    /** Higher rank = more urgent. Useful for sorting overlapping routes. */
    public int getRank() {
        return rank;
    }
}
