package controller;

import algorithm.DijkstraRouter;
import algorithm.FractionalKnapsackOptimizer;
import database.FloodDatabase;
import model.Graph;
import model.KnapsackResult;
import model.PriorityLevel;
import model.RouteResult;
import model.SupplyItem;
import model.VehicleProfile;

import java.util.List;

/**
 * CONTROLLER layer of the MVC architecture.
 * Calls Dijkstra and Fractional Knapsack, delegates all
 * data mutations to FloodDatabase.
 */
public class FloodController {

    private final FloodDatabase database;
    private final DijkstraRouter router = new DijkstraRouter();
    private final FractionalKnapsackOptimizer knapsackOptimizer = new FractionalKnapsackOptimizer();

    public FloodController(FloodDatabase database) {
        this.database = database;
    }

    public RouteResult getRouteAnalysis(String sourceId, double dMax) {
        return router.computeShortestPaths(database.getGraph(), sourceId, dMax);
    }

    public KnapsackResult getLoadOptimization(double capacityW) {
        return knapsackOptimizer.optimize(database.getSupplyItems(), capacityW);
    }

    public Graph getGraph() { return database.getGraph(); }
    public List<SupplyItem> getSupplyItems() { return database.getSupplyItems(); }
    public List<VehicleProfile> getVehicleProfiles() { return database.getVehicleProfiles(); }

    /**
     * Handles field reports: node flood depth, hub edge weight,
     * supply item changes, or a direct edge update by from/to pair.
     */
    public boolean submitFieldReport(String nodeId, Double floodDepth, Double baseWeight,
                                     String itemId, Double itemWeight, Double itemPriority,
                                     String edgeFrom, String edgeTo, Double edgeWeight) {
        boolean changed = false;

        if (nodeId != null && floodDepth != null)
            changed |= database.updateNodeFloodDepth(nodeId, floodDepth);

        if (nodeId != null && baseWeight != null)
            changed |= database.updateEdgeBaseWeight(nodeId, baseWeight);

        if (itemId != null && (itemWeight != null || itemPriority != null))
            changed |= database.updateSupplyItem(itemId, itemWeight, itemPriority);

        if (edgeFrom != null && edgeTo != null && edgeWeight != null)
            changed |= database.updateEdgeDirect(edgeFrom, edgeTo, edgeWeight);

        if (changed) database.save();
        return changed;
    }

    /** Add a brand-new node to V at runtime. Returns false if the id already exists. */
    public boolean addNode(String id, String name, String priority, double floodDepth) {
        if (database.getGraph().getNode(id) != null) return false;
        PriorityLevel pl;
        try { pl = PriorityLevel.valueOf(priority.toUpperCase()); }
        catch (IllegalArgumentException e) { pl = PriorityLevel.MODERATE; }
        database.addNode(id, name, pl, floodDepth);
        database.save();
        return true;
    }

    /** Add a directed edge u→v to E at runtime. Returns false if either node doesn't exist. */
    public boolean addEdge(String from, String to, double weight) {
        if (database.getGraph().getNode(from) == null) return false;
        if (database.getGraph().getNode(to)   == null) return false;
        database.addEdge(from, to, weight);
        database.save();
        return true;
    }
}