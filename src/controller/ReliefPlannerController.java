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
 * MVC controller — runs Dijkstra routing and fractional knapsack loading.
 */
public class ReliefPlannerController {

    private final FloodDatabase database;
    private final DijkstraRouter router = new DijkstraRouter();
    private final FractionalKnapsackOptimizer knapsack = new FractionalKnapsackOptimizer();
    private final GreedyKnapsackOptimizer greedyKnapsack = new GreedyKnapsackOptimizer();
    private final List<DeliveryRequest> deliveryRequests = new ArrayList<>();
    private DeliveryPlan lastPlan;

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

    public DeliveryPlan getLastPlan() {
        return lastPlan;
    }

    public List<DeliveryRequest> getDeliveryRequests() {
        return Collections.unmodifiableList(deliveryRequests);
    }

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
        lastPlan = null;
    }

    public void removeDeliveryRequest(int index) {
        if (index >= 0 && index < deliveryRequests.size()) {
            deliveryRequests.remove(index);
            lastPlan = null;
        }
    }

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

    public DeliveryPlan calculateDeliveryPlan() {
        double dMax = database.getDMaxMm();
        double truckKg = database.getTruckCapacityKg();
        Graph graph = database.getGraph();

        DeliveryPlan plan = new DeliveryPlan();
        plan.setDMax(dMax);
        plan.setTruckCapacity(truckKg);

        int reachable = 0;
        int blocked = 0;

        for (DeliveryRequest request : deliveryRequests) {
            Node hub = graph.getNode(request.getHubId());
            Node dest = graph.getNode(request.getDestinationId());
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

        List<SupplyItem> supplies = database.getSupplyItems();
        plan.setKnapsackResult(knapsack.optimize(supplies, truckKg));
        plan.setGreedyResult(greedyKnapsack.optimize(supplies, truckKg));

        lastPlan = plan;
        database.save();
        return plan;
    }

    public void loadSample() {
        database.loadSelangorSample();
        resetDeliveryRequestsToSample();
        database.save();
        lastPlan = null;
    }

    public void save() {
        database.save();
    }
}
