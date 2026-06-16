package model;

/**
 * Represents a single vertex v &isin; V of the directed weighted graph
 * G = (V, E) described in the Problem Specification.
 *
 * Holds d(n): the current flood water depth at node n (in millimetres),
 * which is checked against Dmax (vehicle safety depth) by the
 * Impassable Corridor Constraint:
 *
 *      d(n) >= Dmax  -->  Node status = DISABLED
 */
public class Node {

    private final String id;          // short code used in path strings, e.g. "UPM", "SKS"
    private final String name;        // human-readable name, e.g. "UPM Main Base"
    private final PriorityLevel priority;
    private double floodDepthMm;      // d(n)

    public Node(String id, String name, PriorityLevel priority, double floodDepthMm) {
        this.id = id;
        this.name = name;
        this.priority = priority;
        this.floodDepthMm = floodDepthMm;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public PriorityLevel getPriority() {
        return priority;
    }

    public double getFloodDepthMm() {
        return floodDepthMm;
    }

    public void setFloodDepthMm(double floodDepthMm) {
        this.floodDepthMm = floodDepthMm;
    }

    /**
     * Impassable Corridor Constraint (Problem Specification, Constraints):
     * d(n) >= Dmax  -->  Node status = DISABLED
     *
     * @param dMax the vehicle's safe flood depth threshold (mm)
     * @return true if this node must be treated as DISABLED for the given vehicle
     */
    public boolean isDisabled(double dMax) {
        return floodDepthMm >= dMax;
    }
}
