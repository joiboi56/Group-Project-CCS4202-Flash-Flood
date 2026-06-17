package model;

import java.util.List;

public class DeliveryRoute {

    private final String hubName;
    private final String destinationName;
    private final boolean canDeliver;
    private final double travelMinutes;
    private final List<String> pathNames;

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

    public boolean canDeliver() {
        return canDeliver;
    }

    public double getTravelMinutes() {
        return travelMinutes;
    }

    public List<String> getPathNames() {
        return pathNames;
    }

    public String routeText() {
        if (!canDeliver || pathNames.isEmpty()) {
            return "Try another relief hub";
        }
        return String.join(" → ", pathNames);
    }
}
