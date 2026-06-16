package controller;

import algorithm.DijkstraRouter;
import algorithm.FractionalKnapsackOptimizer;
import database.FloodDatabase;
import model.Graph;
import model.KnapsackResult;
import model.RouteResult;
import model.SupplyItem;
import model.VehicleProfile;

import java.util.List;

/**
 * CONTROLLER layer of the MVC architecture (see "Sketch / Framework"):
 *   - Handles request flow
 *   - Acts as intermediary between Model (FloodDatabase) and View (the
 *     rescue-console / give-info-console HTML pages, via {@link ApiServer})
 *   - Calls the appropriate algorithm models (Dijkstra & Fractional Knapsack)
 *   - Never handles raw data-storage logic directly -- that is delegated
 *     to {@link FloodDatabase}.
 */
public class FloodController {

    private final FloodDatabase database;
    private final DijkstraRouter router = new DijkstraRouter();
    private final FractionalKnapsackOptimizer knapsackOptimizer = new FractionalKnapsackOptimizer();

    public FloodController(FloodDatabase database) {
        this.database = database;
    }

    /** Module 01: Route Minimization Engine (Dijkstra). */
    public RouteResult getRouteAnalysis(String sourceId, double dMax) {
        return router.computeShortestPaths(database.getGraph(), sourceId, dMax);
    }

    /** Module 02: Load Score Maximization Engine (Fractional Knapsack). */
    public KnapsackResult getLoadOptimization(double capacityW) {
        return knapsackOptimizer.optimize(database.getSupplyItems(), capacityW);
    }

    public Graph getGraph() {
        return database.getGraph();
    }

    public List<SupplyItem> getSupplyItems() {
        return database.getSupplyItems();
    }

    public List<VehicleProfile> getVehicleProfiles() {
        return database.getVehicleProfiles();
    }

    /**
     * Handles a field report submitted via the "I want to give information" form.
     *
     * @param nodeId       target crisis node v in V (nullable)
     * @param floodDepth   new d(n) value in mm (nullable)
     * @param baseWeight   new baseline w(u,v) for the node's primary hub edge (nullable)
     * @param itemId       supply item i in I (nullable)
     * @param itemWeight   new w(i) for that item (nullable)
     * @param itemPriority new v(i) for that item (nullable)
     * @return true if at least one field was updated
     */
    public boolean submitFieldReport(String nodeId, Double floodDepth, Double baseWeight,
                                      String itemId, Double itemWeight, Double itemPriority) {
        boolean changed = false;

        if (nodeId != null && floodDepth != null) {
            changed |= database.updateNodeFloodDepth(nodeId, floodDepth);
        }
        if (nodeId != null && baseWeight != null) {
            changed |= database.updateEdgeBaseWeight(nodeId, baseWeight);
        }
        if (itemId != null && (itemWeight != null || itemPriority != null)) {
            changed |= database.updateSupplyItem(itemId, itemWeight, itemPriority);
        }

        if (changed) {
            database.save();
        }
        return changed;
    }
}
