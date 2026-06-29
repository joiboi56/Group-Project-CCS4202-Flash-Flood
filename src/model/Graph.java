package model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class Graph {
    //lookup table of all locations
    private final Map<String, Node> nodes = new LinkedHashMap<>();
    //lookup table of all roads leaving each location
    private final Map<String, List<Edge>> adjacency = new LinkedHashMap<>(); // roads leaving each node
    //complete route inventory
    private final List<Edge> edges = new ArrayList<>();

    //Registers a new place on the map. 
    public void addNode(Node node) {
        nodes.put(node.getId(), node);// adds the location to the directory
        adjacency.putIfAbsent(node.getId(), new ArrayList<>());// create empty road list
    }

    //Connects two places with a directed road. 
    public void addEdge(Edge edge) {
        edges.add(edge); //add road to master road inventory
        adjacency.computeIfAbsent(edge.getFrom(), k -> new ArrayList<>()).add(edge);//files the road under starting location road list
    }

    // Removes a place and all roads connected to it. 
    public boolean removeNode(String id) {
        if (!nodes.containsKey(id)) {
            return false;
        }
        //remove location from directory
        nodes.remove(id);
        //remove road list for this location
        adjacency.remove(id);
        //remove all nodes connecting to this route in master inventory
        edges.removeIf(e -> e.getFrom().equals(id) || e.getTo().equals(id));
        //goes to every other location road list and remove any road that was point towards the deleted location
        for (List<Edge> list : adjacency.values()) {
            list.removeIf(e -> e.getTo().equals(id));
        }
        return true;
    }

    //Removes one road between two places.
    public boolean removeEdge(String from, String to) {
        Edge found = findEdge(from, to);
        if (found == null) {
            return false;
        }
        //if found, remove the road from the master inventory and also from the starting location road list
        edges.remove(found);
        List<Edge> outgoing = adjacency.get(from);
        if (outgoing != null) {
            outgoing.remove(found);
        }
        return true;
    }

    //Wipes the whole map — used when loading the sample scenario. 
    public void clear() {
        nodes.clear();
        adjacency.clear();
        edges.clear();
    }

    // give detail of a specific location
    public Node getNode(String id) {
        return nodes.get(id);
    }

    //All places currently on the map.
    public Collection<Node> getAllNodes() {
        return nodes.values();
    }

    //Every road in the network.
    public List<Edge> getEdges() {
        return edges;
    }

    // All roads leaving one place — Dijkstra checks these next.
    public List<Edge> getOutgoingEdges(String nodeId) {
        return adjacency.getOrDefault(nodeId, Collections.emptyList());
    }

    //Find the direct road from A to B, if it exists. */
    public Edge findEdge(String from, String to) {
        for (Edge e : edges) {
            if (e.getFrom().equals(from) && e.getTo().equals(to)) {
                return e;
            }
        }
        return null;
    }

   //check if the road is safe for the truck to travel, based on flood status, flood depth, and weight limit.
    public boolean isEdgePassable(Edge edge, double dMax, double truckWeightKg) {
        if (edge.isFlooded()) {
            return false;
        }
        if (truckWeightKg > edge.getWeightLimitKg()) {
            return false;
        }
        //check whether the starting and ending locations are disabled due to flood depth
        Node u = nodes.get(edge.getFrom());
        Node v = nodes.get(edge.getTo());
        if (u == null || v == null) {
            return false;
        }
        //check whether the both loction are accessible
        return !u.isDisabled(dMax) && !v.isDisabled(dMax);
    }
}
// end of Graph.java