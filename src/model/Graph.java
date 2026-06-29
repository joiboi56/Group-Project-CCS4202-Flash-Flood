package model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The road network as a graph G = (V, E) from our project report.
 * V = set of places (nodes), E = set of roads (edges).
 * Dijkstra's algorithm uses this structure to find shortest paths.
 */
public class Graph {

    private final Map<String, Node> nodes = new LinkedHashMap<>();
    private final Map<String, List<Edge>> adjacency = new LinkedHashMap<>(); // roads leaving each node
    private final List<Edge> edges = new ArrayList<>();

    /** Registers a new place on the map. */
    public void addNode(Node node) {
        nodes.put(node.getId(), node);
        adjacency.putIfAbsent(node.getId(), new ArrayList<>());
    }

    /** Connects two places with a directed road. */
    public void addEdge(Edge edge) {
        edges.add(edge);
        adjacency.computeIfAbsent(edge.getFrom(), k -> new ArrayList<>()).add(edge);
    }

    /** Removes a place and all roads connected to it. */
    public boolean removeNode(String id) {
        if (!nodes.containsKey(id)) {
            return false;
        }
        nodes.remove(id);
        adjacency.remove(id);
        edges.removeIf(e -> e.getFrom().equals(id) || e.getTo().equals(id));
        for (List<Edge> list : adjacency.values()) {
            list.removeIf(e -> e.getTo().equals(id));
        }
        return true;
    }

    /** Removes one road between two places. */
    public boolean removeEdge(String from, String to) {
        Edge found = findEdge(from, to);
        if (found == null) {
            return false;
        }
        edges.remove(found);
        List<Edge> outgoing = adjacency.get(from);
        if (outgoing != null) {
            outgoing.remove(found);
        }
        return true;
    }

    /** Wipes the whole map — used when loading the sample scenario. */
    public void clear() {
        nodes.clear();
        adjacency.clear();
        edges.clear();
    }

    /** Look up one place by its ID code. */
    public Node getNode(String id) {
        return nodes.get(id);
    }

    /** All places currently on the map. */
    public Collection<Node> getAllNodes() {
        return nodes.values();
    }

    /** Every road in the network. */
    public List<Edge> getEdges() {
        return edges;
    }

    /** All roads leaving one place — Dijkstra checks these next. */
    public List<Edge> getOutgoingEdges(String nodeId) {
        return adjacency.getOrDefault(nodeId, Collections.emptyList());
    }

    /** Find the road from A to B, if it exists. */
    public Edge findEdge(String from, String to) {
        for (Edge e : edges) {
            if (e.getFrom().equals(from) && e.getTo().equals(to)) {
                return e;
            }
        }
        return null;
    }

    /**
     * Checks if a road is safe to use before running full Dijkstra.
     * Fails if flooded, overweight, or either end is underwater.
     */
    public boolean isEdgePassable(Edge edge, double dMax, double truckWeightKg) {
        if (edge.isFlooded()) {
            return false;
        }
        if (truckWeightKg > edge.getWeightLimitKg()) {
            return false;
        }
        Node u = nodes.get(edge.getFrom());
        Node v = nodes.get(edge.getTo());
        if (u == null || v == null) {
            return false;
        }
        return !u.isDisabled(dMax) && !v.isDisabled(dMax);
    }
}
