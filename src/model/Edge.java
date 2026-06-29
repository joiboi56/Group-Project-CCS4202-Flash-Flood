package model;

public class Edge {

    private final String from;       // starting place ID
    private final String to;           // ending place ID
    private double travelMinutes;    // edge weight for shortest path
    private double weightLimitKg;    // road weight capacity
    private boolean flooded;         // true if marked flooded in the table
    private double floodDepthMm;     // water depth in millimetres

    // Constructor for an edge with no flood depth specified (defaults to 0.0)
    public Edge(String from, String to, double travelMinutes, double weightLimitKg, boolean flooded) {
        this(from, to, travelMinutes, weightLimitKg, flooded, 0.0);
    }

    public Edge(String from, String to, double travelMinutes, double weightLimitKg, boolean flooded, double floodDepthMm) {
        this.from = from;
        this.to = to;
        this.travelMinutes = travelMinutes;
        this.weightLimitKg = weightLimitKg;
        this.flooded = flooded;
        this.floodDepthMm = floodDepthMm;
    }
    //where the road starts
    public String getFrom() {
        return from;
    }
    //where the road ends
    public String getTo() {
        return to;
    }

    public double getTravelMinutes() {
        return travelMinutes;
    }
    //update the travel time for this road
    public void setTravelMinutes(double travelMinutes) {
        this.travelMinutes = travelMinutes;
    }

    public double getWeightLimitKg() {
        return weightLimitKg;
    }
    //update the weight limit for this road 
    public void setWeightLimitKg(double weightLimitKg) {
        this.weightLimitKg = weightLimitKg;
    }
    //update flood status for this road
    public boolean isFlooded() {
        return flooded;
    }
    //update flood depth
    public void setFlooded(boolean flooded) {
        this.flooded = flooded;
    }

    public double getFloodDepthMm() {
        return floodDepthMm;
    }

    public void setFloodDepthMm(double floodDepthMm) {
        this.floodDepthMm = floodDepthMm;
    }

    //Returns travel time — this is the "cost" Dijkstra minimises. like comparing 2 routes
    public double effectiveWeight() {
        return travelMinutes;
    }
}
