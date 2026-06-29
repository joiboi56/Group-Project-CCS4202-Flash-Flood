package model;

import java.util.Map;

/**
 * Everything Dijkstra found when starting from one hub.
 * Contains route info for every place on the map, not just one destination.
 */
public class RouteResult {

    private final String source;  // hub ID where routing started
    private final double dMax;  // flood limit used during this run
    private final Map<String, NodeRouteInfo> results; // destination ID -> route info

    public RouteResult(String source, double dMax, Map<String, NodeRouteInfo> results) {
        this.source = source;
        this.dMax = dMax;
        this.results = results;
    }

    public String getSource() {
        return source;
    }

    public double getDMax() {
        return dMax;
    }

    public Map<String, NodeRouteInfo> getResults() {
        return results;
    }

    /** Shortcut to get route info for one specific place. */
    public NodeRouteInfo get(String nodeId) {
        return results.get(nodeId);
    }
}
