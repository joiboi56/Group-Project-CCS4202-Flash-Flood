package model;

import java.util.List;

//best path result from one hub to one affected area, including the route and the time taken
public class DeliveryRoute {
    
    //starting hub name
    private final String hubName;
    private final String destinationName;
    private final boolean canDeliver;       // true if a safe path exists
    private final double travelMinutes;     // ETA from Dijkstra
    private final List<String> pathNames;   // human-readable route steps

    //like when the system finish calculate the route, all these info will be written
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

    //Whether the truck can reach this place right now.
    public boolean canDeliver() {
        return canDeliver;
    }

    //Total driving time in minutes along the shortest safe route.
    public double getTravelMinutes() {
        return travelMinutes;
    }
    //full list of road stops
    public List<String> getPathNames() {
        return pathNames;
    }

   //convert the route into a human-readable string, e.g. "UPM → SK Sri Serdang → SK Seri Kembangan"
    public String routeText() {
        //if truck isblocked, or no road steps recorded
        if (!canDeliver || pathNames.isEmpty()) {
            return "Try another relief hub";
        }
        // join all the stops name together
        return String.join(" → ", pathNames);
    }
}
