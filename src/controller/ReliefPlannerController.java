package controller;

import algorithm.DijkstraRouter;
import algorithm.FractionalKnapsackOptimizer;
import algorithm.GreedyKnapsackOptimizer;
import database.FloodDatabase;
import model.DeliveryPlan;
import model.DeliveryRequest;
import model.DeliveryRoute;
import model.Graph;
import model.KnapsackResult;
import model.Node;
import model.NodeRouteInfo;
import model.PlaceType;
import model.RouteResult;
import model.SupplyItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * CONTROLLER PACKAGE — middle layer in MVC (Model-View-Controller).
 *
 * The GUI (View) never talks to algorithms directly. It calls this class instead.
 * This class reads data from FloodDatabase (Model), runs the algorithms, and
 * returns a DeliveryPlan for the GUI to display.
 */
public class ReliefPlannerController {

    private final FloodDatabase database;
    private final DijkstraRouter router = new DijkstraRouter();
    private final FractionalKnapsackOptimizer knapsack = new FractionalKnapsackOptimizer();
    private final GreedyKnapsackOptimizer greedyKnapsack = new GreedyKnapsackOptimizer();
    private final List<DeliveryRequest> deliveryRequests = new ArrayList<>();
    private DeliveryPlan lastPlan; // cached result from last Calculate click

    /** Sets up database and loads the default delivery list. */
    public ReliefPlannerController(FloodDatabase database) {
        this.database = database;
        resetDeliveryRequestsToSample();
    }

    public FloodDatabase getDatabase() {
        return database;
    }

    public Graph getGraph() {
        return database.getGraph();
    }

    public List<SupplyItem> getSupplyItems() {
        return database.getSupplyItems();
    }

    //Returns the last plan so tabs can refresh without recalculating.

    public DeliveryPlan getLastPlan() {
        return lastPlan;
    }

    /** Read-only list of hub-to-destination trips the user configured. */
    public List<DeliveryRequest> getDeliveryRequests() {
        return Collections.unmodifiableList(deliveryRequests);
    }

    /**
     * Adds one delivery trip to the plan (e.g. UPM -> SK Sri Serdang).
     * Ignores duplicates and invalid pairs.
     */
    public void addDeliveryRequest(String hubId, String destinationId) {
        if (hubId == null || destinationId == null || hubId.equals(destinationId)) {
            return;
        }
        for (DeliveryRequest request : deliveryRequests) {
            if (request.getHubId().equals(hubId) && request.getDestinationId().equals(destinationId)) {
                return;
            }
        }
        deliveryRequests.add(new DeliveryRequest(hubId, destinationId));
        lastPlan = null; // old results no longer valid
    }

    /** Removes one delivery row by index from the Delivery Plan tab. */
    public void removeDeliveryRequest(int index) {
        if (index >= 0 && index < deliveryRequests.size()) {
            deliveryRequests.remove(index);
            lastPlan = null;
        }
    }

    /**
     * Restores the 6 default routes from our NADMA Sector 4 scenario:
     * 4 from UPM, 2 from UNITEN.
     */
    public void resetDeliveryRequestsToSample() {
        deliveryRequests.clear();
        deliveryRequests.add(new DeliveryRequest("UPM", "SKSS"));
        deliveryRequests.add(new DeliveryRequest("UPM", "SMKSS"));
        deliveryRequests.add(new DeliveryRequest("UPM", "U360"));
        deliveryRequests.add(new DeliveryRequest("UPM", "KTMB"));
        deliveryRequests.add(new DeliveryRequest("UNIT", "MERAB"));
        deliveryRequests.add(new DeliveryRequest("UNIT", "RAMAL"));
        lastPlan = null;
    }

    /**
     * MAIN PLANNING METHOD — runs when user clicks "Calculate Delivery Plan".
     *
     * Step 1: For each delivery request, run Dijkstra to find shortest safe route.
     * Step 2: Run Fractional Knapsack and Greedy Knapsack on the supply list.
     * Step 3: Package everything into a DeliveryPlan and save data to file.
     */
    public DeliveryPlan calculateDeliveryPlan() {
        double dMax = database.getDMaxMm();
        double truckKg = database.getTruckCapacityKg();
        Graph graph = database.getGraph();

        DeliveryPlan plan = new DeliveryPlan();
        plan.setDMax(dMax);
        plan.setTruckCapacity(truckKg);

        int reachable = 0;
        int blocked = 0;

        // --- ROUTING: Dijkstra for each hub -> destination pair ---
        for (DeliveryRequest request : deliveryRequests) {
            Node hub = graph.getNode(request.getHubId());
            Node dest = graph.getNode(request.getDestinationId());
            // Must start at hub and end at affected area
            if (hub == null || dest == null || !hub.isHub()
                    || dest.getPlaceType() != PlaceType.AFFECTED_AREA) {
                continue;
            }
            RouteResult result = router.computeShortestPaths(graph, hub.getId(), dMax, truckKg);
            NodeRouteInfo info = result.get(dest.getId());
            boolean ok = info != null && info.isReachable();
            if (ok) {
                reachable++;
            } else {
                blocked++;
            }
            // Turn node IDs into readable place names for the GUI table
            List<String> pathNames = new ArrayList<>();
            if (ok) {
                for (String stepId : info.getPath()) {
                    Node step = graph.getNode(stepId);
                    pathNames.add(step != null ? step.getName() : stepId);
                }
            }
            double eta = info != null ? info.getEta() : Double.POSITIVE_INFINITY;
            plan.addRoute(new DeliveryRoute(
                    hub.getName(),
                    dest.getName(),
                    ok,
                    eta,
                    pathNames
            ));
        }

        plan.setReachableDestinations(reachable);
        plan.setBlockedDestinations(blocked);

        // --- LOADING: both knapsack algorithms for comparison ---
        List<SupplyItem> supplies = database.getSupplyItems();
        plan.setKnapsackResult(knapsack.optimize(supplies, truckKg));
        plan.setGreedyResult(greedyKnapsack.optimize(supplies, truckKg));

        lastPlan = plan;
        database.save();
        return plan;
    }

    /** Resets map and supplies to Selangor sample — "Load Selangor Sample" button. */
    public void loadSample() {
        database.loadSelangorSample();
        resetDeliveryRequestsToSample();
        database.save();
        lastPlan = null;
    }

    /** Tells database to write flood_data.txt (called after map/supply edits). */
    public void save() {
        database.save();
    }
}
