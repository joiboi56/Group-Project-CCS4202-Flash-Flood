package model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The directed weighted graph G = (V, E) representing the regional
 * road network described in the Problem Specification.
 *
 * V: command hubs and affected target zones (Node objects)
 * E: connecting road links (Edge objects, with dynamic weight w(u,v))
 */
public class Graph {

    private final Map<String, Node> nodes = new LinkedHashMap<>();
    private final Map<String, List<Edge>> adjacency = new LinkedHashMap<>();
    private final List<Edge> edges = new ArrayList<>();

    public void addNode(Node node) {
        nodes.put(node.getId(), node);
        adjacency.putIfAbsent(node.getId(), new ArrayList<>());
    }

    public void addEdge(Edge edge) {
        edges.add(edge);
        adjacency.computeIfAbsent(edge.getFrom(), k -> new ArrayList<>()).add(edge);
    }

    public Node getNode(String id) {
        return nodes.get(id);
    }

    public Collection<Node> getAllNodes() {
        return nodes.values();
    }

    public List<Edge> getEdges() {
        return edges;
    }

    /** All edges (u, v) leaving node u, i.e. adjacency list of u. */
    public List<Edge> getOutgoingEdges(String nodeId) {
        return adjacency.getOrDefault(nodeId, Collections.emptyList());
    }

    /** Find the specific directed edge u -> v, or null if it does not exist. */
    public Edge findEdge(String from, String to) {
        for (Edge e : edges) {
            if (e.getFrom().equals(from) && e.getTo().equals(to)) {
                return e;
            }
        }
        return null;
    }

    /**
     * Edge (u,v) passability check used by Dijkstra's algorithm.
     * An edge is impassable if either endpoint is flooded beyond Dmax,
     * per the Impassable Corridor Constraint.
     */
    public boolean isEdgePassable(Edge edge, double dMax) {
        Node u = nodes.get(edge.getFrom());
        Node v = nodes.get(edge.getTo());
        if (u == null || v == null) {
            return false;
        }
        return !u.isDisabled(dMax) && !v.isDisabled(dMax);
    }
}
