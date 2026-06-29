package model;

/**
 * One directed road link between two places (an edge in graph E).
 *
 * travelMinutes = w(u,v) — how long the drive takes (used as weight in Dijkstra).
 * floodDepthMm = water depth on this road — if too high, Dijkstra skips it.
 * weightLimitKg = max truck weight allowed on this road/bridge.
 */
public class Edge {

    private final String from;       // starting place ID
    private final String to;           // ending place ID
    private double travelMinutes;    // edge weight for shortest path
    private double weightLimitKg;    // road weight capacity
    private boolean flooded;         // true if marked flooded in the table
    private double floodDepthMm;     // water depth in millimetres

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

    public double getFloodDepthMm() {
        return floodDepthMm;
    }

    public void setFloodDepthMm(double floodDepthMm) {
        this.floodDepthMm = floodDepthMm;
    }

    /** Returns travel time — this is the "cost" Dijkstra minimises. */
    public double effectiveWeight() {
        return travelMinutes;
    }
}
