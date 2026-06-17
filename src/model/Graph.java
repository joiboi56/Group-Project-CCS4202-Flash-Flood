package model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    public void clear() {
        nodes.clear();
        adjacency.clear();
        edges.clear();
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

    public List<Edge> getOutgoingEdges(String nodeId) {
        return adjacency.getOrDefault(nodeId, Collections.emptyList());
    }

    public Edge findEdge(String from, String to) {
        for (Edge e : edges) {
            if (e.getFrom().equals(from) && e.getTo().equals(to)) {
                return e;
            }
        }
        return null;
    }

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
