package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds the complete answer after pressing "Calculate Delivery Plan".
 * Combines Dijkstra route results + both knapsack loading results.
 */
public class DeliveryPlan {

    private final List<DeliveryRoute> routes = new ArrayList<>();
    private KnapsackResult knapsackResult;   // fractional knapsack output
    private KnapsackResult greedyResult;     // greedy knapsack output
    private int reachableDestinations;
    private int blockedDestinations;
    private double dMax;
    private double truckCapacity;

    public List<DeliveryRoute> getRoutes() {
        return routes;
    }

    /** Adds one row to the route results table. */
    public void addRoute(DeliveryRoute route) {
        routes.add(route);
    }

    public KnapsackResult getKnapsackResult() {
        return knapsackResult;
    }

    public void setKnapsackResult(KnapsackResult knapsackResult) {
        this.knapsackResult = knapsackResult;
    }

    public KnapsackResult getGreedyResult() {
        return greedyResult;
    }

    public void setGreedyResult(KnapsackResult greedyResult) {
        this.greedyResult = greedyResult;
    }

    public int getReachableDestinations() {
        return reachableDestinations;
    }

    public void setReachableDestinations(int reachableDestinations) {
        this.reachableDestinations = reachableDestinations;
    }

    public int getBlockedDestinations() {
        return blockedDestinations;
    }

    public void setBlockedDestinations(int blockedDestinations) {
        this.blockedDestinations = blockedDestinations;
    }

    public double getDMax() {
        return dMax;
    }

    public void setDMax(double dMax) {
        this.dMax = dMax;
    }

    public double getTruckCapacity() {
        return truckCapacity;
    }

    public void setTruckCapacity(double truckCapacity) {
        this.truckCapacity = truckCapacity;
    }

    /**
     * Plain-language summary for the "Simple Advice" tab.
     * Written so non-IT users (dispatchers) can understand the plan quickly.
     */
    public List<String> buildAdvice() {
        List<String> advice = new ArrayList<>();
        advice.add("Vehicle flood limit (Dmax): " + (int) dMax + " mm");
        advice.add("Truck capacity: " + truckCapacity + " kg");
        advice.add(reachableDestinations + " affected areas can receive help right now.");
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
