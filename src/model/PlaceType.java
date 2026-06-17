package model;

public enum PlaceType {
    RELIEF_HUB("Relief Hub"),
    AFFECTED_AREA("Affected Area");

    private final String label;

    PlaceType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
