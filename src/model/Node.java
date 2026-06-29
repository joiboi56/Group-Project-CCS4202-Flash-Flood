package model;


public class Node {

    private final String id;           // short code, e.g. "UPM", "SKSS"
    private String name;               // full name shown in tables
    private PlaceType placeType;       // hub or affected area
    private double floodDepthMm;       // d(n) — water depth at this place
    private double layoutX;            // horizontal position on map (0.0 to 1.0)
    private double layoutY;            // vertical position on map (0.0 to 1.0)

    //info needed to create a node
    public Node(String id, String name, PlaceType placeType, double floodDepthMm) {
        this.id = id;
        this.name = name;
        this.placeType = placeType;
        this.floodDepthMm = floodDepthMm;
    }
    //read the node ID
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
    //update the node name
    public void setName(String name) {
        this.name = name;
    }

    public PlaceType getPlaceType() {
        return placeType;
    }
    //Allows other code to read or update whether this is a hub or affected area.
    public void setPlaceType(PlaceType placeType) {
        this.placeType = placeType;
    }

    public double getFloodDepthMm() {
        return floodDepthMm;
    }
    //Allows other code to read or update the flood depth. If it rains more, the system can use setFloodDepthMm to increase the number.
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
    //Allows the map-drawing system to read or set exactly where this node should be drawn on the screen.
    public void setLayoutY(double layoutY) {
        this.layoutY = layoutY;
    }

    //True if this place is a relief base (UPM / UNITEN) where trucks start. 
    public boolean isHub() {
        return placeType == PlaceType.RELIEF_HUB;
    }

    //Checks if flood water is too high for the rescue vehicle to enter.
     //From our report: if d(n) >= Dmax, the node is blocked.
     
    public boolean isDisabled(double dMax) {
        return floodDepthMm >= dMax;
    }

    //Short text drawn inside the circle on the map.
    //If the ID is 4 characters or fewer, it uses it as-is. If it's longer, it chops off everything after the first 4 characters (substring(0, 4)) and ensures they are capitalized (toUpperCase()). 
    public String shortLabel() {
        if (id.length() <= 4) {
            return id;
        }
        return id.substring(0, 4).toUpperCase();
    }
}
