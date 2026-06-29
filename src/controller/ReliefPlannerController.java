package controller;

//import the algorithm classes and model classes
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

// for dynamic list
import java.util.ArrayList;
//utility methods
import java.util.Collections;
//interface
import java.util.List;

// controller act as a middle layer in MVC
//The gui talk with model using contoller as a middle layer
public class ReliefPlannerController {

    //store the database object
    private final FloodDatabase database;
    //create the routing algorithm
    private final DijkstraRouter router = new DijkstraRouter();
    //create the Fractional knapsack Optimizer object
    private final FractionalKnapsackOptimizer knapsack = new FractionalKnapsackOptimizer();
    //create a greedy optimizer  object
    private final GreedyKnapsackOptimizer greedyKnapsack = new GreedyKnapsackOptimizer();
    //Array to store all delivery requests entered by the user
    private final List<DeliveryRequest> deliveryRequests = new ArrayList<>();
    // stores the most recent calculated plan
    private DeliveryPlan lastPlan;


    public ReliefPlannerController(FloodDatabase database) {
        //stores the database references
        this.database = database;
        //function that can loads the default six delivery request
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

   //Returns the delivery list
    public List<DeliveryRequest> getDeliveryRequests() {

        //Gui can read the value but cannot mofified it directly
        return Collections.unmodifiableList(deliveryRequests);
    }

    //method for create or add a new delivery request
    public void addDeliveryRequest(String hubId, String destinationId) {
        // the input must be valid(cannot null or same input)
        if (hubId == null || destinationId == null || hubId.equals(destinationId)) {
            return;
        }

        //check every existing delivery request
        for (DeliveryRequest request : deliveryRequests) {

            //check is there is a duplication
            if (request.getHubId().equals(hubId) && request.getDestinationId().equals(destinationId)) {
                return;
            }
        }
        //add new delivery request
        deliveryRequests.add(new DeliveryRequest(hubId, destinationId));
        // old results no longer valid
        lastPlan = null;
    }

    //method to remove a delivery request
    public void removeDeliveryRequest(int index) {
        //make sure all the delivery request index exist
        if (index >= 0 && index < deliveryRequests.size()) {
            //deletes it
            deliveryRequests.remove(index);
            lastPlan = null;
        }
    }

   //reset all the delivery request and set to default sample
    public void resetDeliveryRequestsToSample() {
        //clear/remove all requests
        deliveryRequests.clear();

        //add six sample routes
        deliveryRequests.add(new DeliveryRequest("UPM", "SKSS"));
        deliveryRequests.add(new DeliveryRequest("UPM", "SMKSS"));
        deliveryRequests.add(new DeliveryRequest("UPM", "U360"));
        deliveryRequests.add(new DeliveryRequest("UPM", "KTMB"));
        deliveryRequests.add(new DeliveryRequest("UNIT", "MERAB"));
        deliveryRequests.add(new DeliveryRequest("UNIT", "RAMAL"));
        lastPlan = null;
    }

   //create a calculating method
    public DeliveryPlan calculateDeliveryPlan() {

        //get the maximum safe flood depth
        double dMax = database.getDMaxMm();
        //get the maximum truck capacity
        double truckKg = database.getTruckCapacityKg();
        //get the road networks
        Graph graph = database.getGraph();

        //create a new plan
        DeliveryPlan plan = new DeliveryPlan();
        //stores all setting
        plan.setDMax(dMax);
        plan.setTruckCapacity(truckKg);

        // to count the reachable and blocked hub
        int reachable = 0;
        int blocked = 0;

        // the loop function is to process every delivery request
        for (DeliveryRequest request : deliveryRequests) {

            //get the starting hub(UPM)
            Node hub = graph.getNode(request.getHubId());
            //get the destination(affected areas)
            Node dest = graph.getNode(request.getDestinationId());
            // Must start at hub and end at affected area
            if (hub == null || dest == null || !hub.isHub() || dest.getPlaceType() != PlaceType.AFFECTED_AREA) {
                continue;
            }
            //runs the Dijkstra's algorithm
            RouteResult result = router.computeShortestPaths(graph, hub.getId(), dMax, truckKg);
            //get route information(reachable or blocked)
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
            //get the estimated travel time
            double eta = info != null ? info.getEta() : Double.POSITIVE_INFINITY;
            //stores one complete route
            plan.addRoute(new DeliveryRoute(hub.getName(),dest.getName(), ok, eta, pathNames));
        }
        //stores the reacable count and blocked count
        plan.setReachableDestinations(reachable);
        plan.setBlockedDestinations(blocked);

        //  Load Optimization: both knapsack algorithms for comparison
        //get all the supply items
        List<SupplyItem> supplies = database.getSupplyItems();
        //runs the fractional knapsack
        plan.setKnapsackResult(knapsack.optimize(supplies, truckKg));
        //runs greedy algorithm(0/1 knapsack)
        plan.setGreedyResult(greedyKnapsack.optimize(supplies, truckKg));

        //saved the last plan
        lastPlan = plan;
        //save the database
        database.save();
        //return the completed delivery plan
        return plan;
    }

    // load the sample dataset
    public void loadSample() {
        database.loadSelangorSample();
        resetDeliveryRequestsToSample();
        database.save();
        lastPlan = null;
    }
    //save the current state to database
    public void save() {
        database.save();
    }
}
