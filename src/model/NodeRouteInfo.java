package model;

import java.util.List;

//Dijkstra result for one destination: how long it takes, which roads to use,
 //and whether the place can be reached at all.
 
public class NodeRouteInfo {
    //destination node ID
    private final String nodeId;
    private final double eta;        // shortest travel time in minutes
    private final List<String> path; // list of node IDs from hub to here
    private final boolean reachable; // false if flooded or no path found

    //When the pathfinding math is finished, it calls this constructor and passes in the four calculated values.
    public NodeRouteInfo(String nodeId, double eta, List<String> path, boolean reachable) {
        this.nodeId = nodeId;
        this.eta = eta;
        this.path = path;
        this.reachable = reachable;
    }
    //return destinantion id
    public String getNodeId() {
        return nodeId;
    }
    //return travel time in minutes
    public double getEta() {
        return eta;
    }
    //Returns the raw list of steps to get there.
    public List<String> getPath() {
        return path;
    }
    //
    public boolean isReachable() {
        return reachable;
    }

    // Builds a readable path string with arrow symbols for display.
    public String getPathString() {
        return String.join(" \u2192 ", path);
    }
}
