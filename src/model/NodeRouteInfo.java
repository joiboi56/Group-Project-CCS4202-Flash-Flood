package model;

import java.util.List;

/**
 * Result of Dijkstra's algorithm for a single destination node:
 * its shortest ETA, path, and reachability status.
 */
public class NodeRouteInfo {

    private final String nodeId;
    private final double eta;          // dist[v]; Double.POSITIVE_INFINITY if unreachable
    private final List<String> path;   // sequence of node ids, source -> ... -> nodeId
    private final boolean reachable;   // false if DISABLED or no path exists

    public NodeRouteInfo(String nodeId, double eta, List<String> path, boolean reachable) {
        this.nodeId = nodeId;
        this.eta = eta;
        this.path = path;
        this.reachable = reachable;
    }

    public String getNodeId() {
        return nodeId;
    }

    public double getEta() {
        return eta;
    }

    public List<String> getPath() {
        return path;
    }

    public boolean isReachable() {
        return reachable;
    }

    /** e.g. "UNI -> UPM -> SKS" rendered with the arrow character used in the UI. */
    public String getPathString() {
        return String.join(" \u2192 ", path);
    }
}
