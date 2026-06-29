package model;

/**
 * Tells the system what kind of place a node is.
 * RELIEF_HUB = where trucks start (UPM, UNITEN).
 * AFFECTED_AREA = flooded location that needs help.
 */
public enum PlaceType {
    RELIEF_HUB("Relief Hub"),
    AFFECTED_AREA("Affected Area");

    private final String label;

    PlaceType(String label) {
        this.label = label;
    }

    /** Name shown in dropdown menus on the Map tab. */
    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
