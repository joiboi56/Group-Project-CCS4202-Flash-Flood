package controller;

import algorithm.DijkstraRouter;
import algorithm.FractionalKnapsackOptimizer;
import algorithm.GreedyKnapsackOptimizer;
import database.FloodDatabase;
import model.DeliveryPlan;
import model.DeliveryRoute;
import model.Graph;
import model.KnapsackResult;
import model.Node;
import model.NodeRouteInfo;
import model.PlaceType;
import model.RouteResult;
import model.SupplyItem;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * MVC controller — runs Dijkstra routing and fractional knapsack loading.
 */
public class ReliefPlannerController {

    private final FloodDatabase database;
    private final DijkstraRouter router = new DijkstraRouter();
    private final FractionalKnapsackOptimizer knapsack = new FractionalKnapsackOptimizer();
    private final GreedyKnapsackOptimizer greedyKnapsack = new GreedyKnapsackOptimizer();
    private DeliveryPlan lastPlan;

    public ReliefPlannerController(FloodDatabase database) {
        this.database = database;
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

    public DeliveryPlan calculateDeliveryPlan() {
        double dMax = database.getDMaxMm();
        double truckKg = database.getTruckCapacityKg();
        Graph graph = database.getGraph();

        List<Node> hubs = graph.getAllNodes().stream()
                .filter(Node::isHub)
                .collect(Collectors.toList());
        List<Node> affected = graph.getAllNodes().stream()
                .filter(n -> n.getPlaceType() == PlaceType.AFFECTED_AREA)
                .collect(Collectors.toList());

        DeliveryPlan plan = new DeliveryPlan();
        plan.setDMax(dMax);
        plan.setTruckCapacity(truckKg);

        int reachable = 0;
        int blocked = 0;

        for (Node hub : hubs) {
            RouteResult result = router.computeShortestPaths(graph, hub.getId(), dMax, truckKg);
            for (Node dest : affected) {
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
        database.save();
        lastPlan = null;
    }

    public void save() {
        database.save();
    }
}
