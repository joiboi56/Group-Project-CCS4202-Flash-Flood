package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds the complete answer after pressing "Calculate Delivery Plan".
 * Combines Dijkstra route results + both knapsack loading results.
 */
public class DeliveryPlan {

    // Contain the list of route
    private final List<DeliveryRoute> routes = new ArrayList<>();
    // fractional knapsack output
    private KnapsackResult knapsackResult;
    // greedy knapsack output   
    private KnapsackResult greedyResult; 
    // counting how many flood-affected area can a truckreach  
    private int reachableDestinations;
    // count how manay blocked area
    private int blockedDestinations;
    //max flood depth a truck can cross
    private double dMax;
    // max truck weight
    private double truckCapacity;
    
    // show full list of route
    public List<DeliveryRoute> getRoutes() {
        return routes;
    }

    //add one more route to the list
    public void addRoute(DeliveryRoute route) {
        routes.add(route);
    }

    //read the packing result
    public KnapsackResult getKnapsackResult() {
        return knapsackResult;
    }
    //save the packing result
    // same for below line
    public void setKnapsackResult(KnapsackResult knapsackResult) {
        this.knapsackResult = knapsackResult;
    }
    //simple packing result
    public KnapsackResult getGreedyResult() {
        return greedyResult;
    }
    //simple packing result
    public void setGreedyResult(KnapsackResult greedyResult) {
        this.greedyResult = greedyResult;
    }
    //reachableDestinations
    public int getReachableDestinations() {
        return reachableDestinations;
    }
    //reachableDestinations
    public void setReachableDestinations(int reachableDestinations) {
        this.reachableDestinations = reachableDestinations;
    }
    //blockedDestinations
    public int getBlockedDestinations() {
        return blockedDestinations;
    }
    //blockedDestinations
    public void setBlockedDestinations(int blockedDestinations) {
        this.blockedDestinations = blockedDestinations;
    }
    //max flood depth a truck can cross
    public double getDMax() {
        return dMax;
    }
    //max flood depth a truck can cross
    public void setDMax(double dMax) {
        this.dMax = dMax;
    }
    // max truck weight
    public double getTruckCapacity() {
        return truckCapacity;
    }
    // max truck weight
    public void setTruckCapacity(double truckCapacity) {
        this.truckCapacity = truckCapacity;
    }
    //like a summary, takes all numbers and turn them into snetences that people can read
    public List<String> buildAdvice() {
        List<String> advice = new ArrayList<>();
        advice.add("Vehicle flood limit (Dmax): " + (int) dMax + " mm");
        advice.add("Truck capacity: " + truckCapacity + " kg");
        advice.add(reachableDestinations + " affected areas can receive help right now.");
        //warn if block are blocked
        if (blockedDestinations > 0) {
            advice.add(blockedDestinations + " routes are blocked — try the other relief hub or wait for water to drop.");
        }
        // List fractional knapsack loading
        if (knapsackResult != null) {
            advice.add("Fractional knapsack: load " + String.format("%.1f", knapsackResult.getTotalWeight())
                    + " kg (help score " + String.format("%.1f", knapsackResult.getTotalScore()) + ").");
            for (KnapsackLineItem line : knapsackResult.getManifest()) {
                if (line.getWeightLoaded() > 0) {
                    advice.add("  • " + line.getItem().getName() + ": "
                            + String.format("%.1f", line.getWeightLoaded()) + " kg");
                }
            }
        }
        // List greedy knapsack loading for comparison
        if (greedyResult != null) {
            advice.add("Greedy (whole items): load " + String.format("%.1f", greedyResult.getTotalWeight())
                    + " kg (help score " + String.format("%.1f", greedyResult.getTotalScore()) + ").");
            for (KnapsackLineItem line : greedyResult.getManifest()) {
                if (line.getWeightLoaded() > 0) {
                    advice.add("  • " + line.getItem().getName() + ": "
                            + String.format("%.1f", line.getWeightLoaded()) + " kg");
                }
            }
        }
        return advice;
    }
}
