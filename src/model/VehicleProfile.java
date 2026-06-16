package model;

/**
 * A deployable rescue/relief vehicle profile.
 *
 * W  = payloadCapacityKg  -> used by the Fractional Knapsack (Load Score Maximization Engine)
 * Dmax = safeFloodDepthMm -> used by Dijkstra's Impassable Corridor Constraint
 *
 * The "Vehicle Threshold Cap" slider on the rescue console maps onto Dmax,
 * letting the dispatcher compare a light Rescue Boat (which can wade through
 * deeper water) against a heavy Relief Truck (limited to shallower corridors).
 */
public class VehicleProfile {

    private final String name;
    private final double payloadCapacityKg; // W
    private final double safeFloodDepthMm;  // Dmax

    public VehicleProfile(String name, double payloadCapacityKg, double safeFloodDepthMm) {
        this.name = name;
        this.payloadCapacityKg = payloadCapacityKg;
        this.safeFloodDepthMm = safeFloodDepthMm;
    }

    public String getName() {
        return name;
    }

    public double getPayloadCapacityKg() {
        return payloadCapacityKg;
    }

    public double getSafeFloodDepthMm() {
        return safeFloodDepthMm;
    }
}
