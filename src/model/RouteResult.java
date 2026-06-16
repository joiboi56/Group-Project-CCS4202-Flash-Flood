package model;

import java.util.Map;

/**
 * Output of a single run of Dijkstra's algorithm: the shortest, safe
 * route from a single source node to every other node in the graph,
 * matching the OUTPUT step of the Dijkstra flowchart
 * ("Shortest safe route, V'=[...] ").
 */
public class RouteResult {

    private final String source;
    private final double dMax;
    private final Map<String, NodeRouteInfo> results; // nodeId -> info

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

    public NodeRouteInfo get(String nodeId) {
        return results.get(nodeId);
    }
}
