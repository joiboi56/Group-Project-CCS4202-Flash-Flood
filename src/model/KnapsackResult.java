package model;

import java.util.List;

/**
 * Output of the Fractional Knapsack Algorithm: the cargo manifest plus
 * the resulting total payload weight and total survival score, matching
 * the OUTPUT step of the knapsack flowchart
 * ("Cargo manifest + total survival score").
 */
public class KnapsackResult {

    private final List<KnapsackLineItem> manifest;
    private final double totalWeight; // sum of w(i) * x_i, <= capacity
    private final double totalScore;  // sum of v(i) * x_i, the maximised objective
    private final double capacity;    // W used for this run

    public KnapsackResult(List<KnapsackLineItem> manifest, double totalWeight, double totalScore, double capacity) {
        this.manifest = manifest;
        this.totalWeight = totalWeight;
        this.totalScore = totalScore;
        this.capacity = capacity;
    }

    public List<KnapsackLineItem> getManifest() {
        return manifest;
    }

    public double getTotalWeight() {
        return totalWeight;
    }

    public double getTotalScore() {
        return totalScore;
    }

    public double getCapacity() {
        return capacity;
    }
}
