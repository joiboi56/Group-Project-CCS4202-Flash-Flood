package model;

/**
 * Vertex in the road network graph.
 * d(n) = flood depth in mm; node is impassable when d(n) >= Dmax.
 */
public class Node {

    private final String id;
    private String name;
    private PlaceType placeType;
    private double floodDepthMm;
    private double layoutX;
    private double layoutY;

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

    public boolean isHub() {
        return placeType == PlaceType.RELIEF_HUB;
    }

    public boolean isDisabled(double dMax) {
        return floodDepthMm >= dMax;
    }

    public String shortLabel() {
        if (id.length() <= 4) {
            return id;
        }
        return id.substring(0, 4).toUpperCase();
    }
}
