package model;

/**
 * Directed road link (u, v) with travel time w(u,v) in minutes.
 */
public class Edge {

    private final String from;
    private final String to;
    private double travelMinutes;
    private double weightLimitKg;
    private boolean flooded;
    private double floodDepthMm; // <--- NEW LINE

    public Edge(String from, String to, double travelMinutes, double weightLimitKg, boolean flooded) {
        this(from, to, travelMinutes, weightLimitKg, flooded, 0.0); // <--- Change to call the new constructor
    }

    // <--- NEW CONSTRUCTOR
    public Edge(String from, String to, double travelMinutes, double weightLimitKg, boolean flooded, double floodDepthMm) {
        this.from = from;
        this.to = to;
        this.travelMinutes = travelMinutes;
        this.weightLimitKg = weightLimitKg;
        this.flooded = flooded;
        this.floodDepthMm = floodDepthMm;
    }
    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public double getTravelMinutes() {
        return travelMinutes;
    }

    public void setTravelMinutes(double travelMinutes) {
        this.travelMinutes = travelMinutes;
    }

    public double getWeightLimitKg() {
        return weightLimitKg;
    }

    public void setWeightLimitKg(double weightLimitKg) {
        this.weightLimitKg = weightLimitKg;
    }

    public boolean isFlooded() {
        return flooded;
    }

    public void setFlooded(boolean flooded) {
        this.flooded = flooded;
    }

    public double getFloodDepthMm() { return floodDepthMm; } // <--- NEW
    public void setFloodDepthMm(double floodDepthMm) { this.floodDepthMm = floodDepthMm; } // <--- NEW

    public double effectiveWeight() {
        return travelMinutes;
    }
}
