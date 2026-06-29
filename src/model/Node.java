package model;

/**
 * One location on the flood map (a vertex in graph G).
 * Can be a relief hub or an affected area. Stores flood depth d(n) in mm.
 */
public class Node {

    private final String id;           // short code, e.g. "UPM", "SKSS"
    private String name;               // full name shown in tables
    private PlaceType placeType;       // hub or affected area
    private double floodDepthMm;       // d(n) — water depth at this place
    private double layoutX;            // horizontal position on map (0.0 to 1.0)
    private double layoutY;            // vertical position on map (0.0 to 1.0)

    public Node(String id, String name, PlaceType placeType, double floodDepthMm) {
        this.id = id;
        this.name = name;
        this.placeType = placeType;
        this.floodDepthMm = floodDepthMm;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PlaceType getPlaceType() {
        return placeType;
    }

    public void setPlaceType(PlaceType placeType) {
        this.placeType = placeType;
    }

    public double getFloodDepthMm() {
        return floodDepthMm;
    }

    public void setFloodDepthMm(double floodDepthMm) {
        this.floodDepthMm = floodDepthMm;
    }

    public double getLayoutX() {
        return layoutX;
    }

    public void setLayoutX(double layoutX) {
        this.layoutX = layoutX;
    }

    public double getLayoutY() {
        return layoutY;
    }

    public void setLayoutY(double layoutY) {
        this.layoutY = layoutY;
    }

    /** True if this place is a relief base (UPM / UNITEN) where trucks start. */
    public boolean isHub() {
        return placeType == PlaceType.RELIEF_HUB;
    }

    /**
     * Checks if flood water is too high for the rescue vehicle to enter.
     * From our report: if d(n) >= Dmax, the node is blocked.
     */
    public boolean isDisabled(double dMax) {
        return floodDepthMm >= dMax;
    }

    /** Short text drawn inside the circle on the map. */
    public String shortLabel() {
        if (id.length() <= 4) {
            return id;
        }
        return id.substring(0, 4).toUpperCase();
    }
}
