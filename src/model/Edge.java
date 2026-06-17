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

    public Edge(String from, String to, double travelMinutes, double weightLimitKg, boolean flooded) {
        this.from = from;
        this.to = to;
        this.travelMinutes = travelMinutes;
        this.weightLimitKg = weightLimitKg;
        this.flooded = flooded;
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

    public double effectiveWeight() {
        return travelMinutes;
    }
}
