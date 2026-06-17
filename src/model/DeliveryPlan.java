package model;

import java.util.ArrayList;
import java.util.List;

public class DeliveryPlan {

    private final List<DeliveryRoute> routes = new ArrayList<>();
    private KnapsackResult knapsackResult;
    private int reachableDestinations;
    private int blockedDestinations;
    private double dMax;
    private double truckCapacity;

    public List<DeliveryRoute> getRoutes() {
        return routes;
    }

    public void addRoute(DeliveryRoute route) {
        routes.add(route);
    }

    public KnapsackResult getKnapsackResult() {
        return knapsackResult;
    }

    public void setKnapsackResult(KnapsackResult knapsackResult) {
        this.knapsackResult = knapsackResult;
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

    public List<String> buildAdvice() {
        List<String> advice = new ArrayList<>();
        advice.add("Vehicle flood limit (Dmax): " + (int) dMax + " mm");
        advice.add("Truck capacity: " + truckCapacity + " kg");
        advice.add(reachableDestinations + " affected areas can receive help right now.");
        if (blockedDestinations > 0) {
            advice.add(blockedDestinations + " routes are blocked — try the other relief hub or wait for water to drop.");
        }
        if (knapsackResult != null) {
            advice.add("Load " + String.format("%.1f", knapsackResult.getTotalWeight()) + " kg for best help score.");
            for (KnapsackLineItem line : knapsackResult.getManifest()) {
                if (line.getWeightLoaded() > 0) {
                    advice.add("  • " + line.getItem().getName() + ": "
                            + String.format("%.1f", line.getWeightLoaded()) + " kg");
                }
            }
        }
        return advice;
    }
}
