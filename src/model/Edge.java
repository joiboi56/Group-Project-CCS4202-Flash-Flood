package model;

/**
 * Represents a directed edge (u, v) &isin; E with weight w(u, v).
 *
 * Per the Problem Specification:
 *   "w(u, v): The weight of the edge connecting node u to node v,
 *    dynamically calculated based on baseline travel time adjusted
 *    for real-time traffic and flood depth."
 *
 * effectiveWeight() implements that dynamic recalculation:
 *      w(u,v) = baseWeight * trafficFactor + floodPenalty
 *
 * trafficFactor and floodPenalty default to neutral values (1.0 and 0.0)
 * so that, out of the box, w(u,v) == baseWeight (matching the example
 * ETAs shown in the project sketch). Field reports submitted through the
 * "I want to give information" console can adjust baseWeight at runtime.
 */
public class Edge {

    private final String from;
    private final String to;
    private double baseWeight;     // baseline travel time, minutes
    private double trafficFactor;  // multiplicative real-time traffic adjustment
    private double floodPenalty;   // additive flood-depth adjustment, minutes

    public Edge(String from, String to, double baseWeight) {
        this(from, to, baseWeight, 1.0, 0.0);
    }

    public Edge(String from, String to, double baseWeight, double trafficFactor, double floodPenalty) {
        this.from = from;
        this.to = to;
        this.baseWeight = baseWeight;
        this.trafficFactor = trafficFactor;
        this.floodPenalty = floodPenalty;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public double getBaseWeight() {
        return baseWeight;
    }

    public void setBaseWeight(double baseWeight) {
        this.baseWeight = baseWeight;
    }

    public double getTrafficFactor() {
        return trafficFactor;
    }

    public void setTrafficFactor(double trafficFactor) {
        this.trafficFactor = trafficFactor;
    }

    public double getFloodPenalty() {
        return floodPenalty;
    }

    public void setFloodPenalty(double floodPenalty) {
        this.floodPenalty = floodPenalty;
    }

    /**
     * w(u, v): dynamically calculated edge weight used by Dijkstra's algorithm.
     */
    public double effectiveWeight() {
        return baseWeight * trafficFactor + floodPenalty;
    }
}
