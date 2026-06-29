package model;

import java.util.List;

/**
 * Result of routing one hub-to-destination pair after Dijkstra runs.
 * Shown as one row in the "Where to Send Help" table.
 */
public class DeliveryRoute {

    private final String hubName;
    private final String destinationName;
    private final boolean canDeliver;       // true if a safe path exists
    private final double travelMinutes;     // ETA from Dijkstra
    private final List<String> pathNames;   // human-readable route steps

    public DeliveryRoute(String hubName, String destinationName, boolean canDeliver,
                         double travelMinutes, List<String> pathNames) {
        this.hubName = hubName;
        this.destinationName = destinationName;
        this.canDeliver = canDeliver;
        this.travelMinutes = travelMinutes;
        this.pathNames = pathNames;
    }

    public String getHubName() {
        return hubName;
    }

    public String getDestinationName() {
        return destinationName;
    }

    /** Whether the truck can reach this place right now. */
    public boolean canDeliver() {
        return canDeliver;
    }

    /** Total driving time in minutes along the shortest safe route. */
    public double getTravelMinutes() {
        return travelMinutes;
    }

    public List<String> getPathNames() {
        return pathNames;
    }

    /**
     * Formats the route for the table, e.g. "UPM → Univ 360 → SK Sri Serdang".
     * If blocked, suggests trying another hub.
     */
    public String routeText() {
        if (!canDeliver || pathNames.isEmpty()) {
            return "Try another relief hub";
        }
        return String.join(" → ", pathNames);
    }
}
