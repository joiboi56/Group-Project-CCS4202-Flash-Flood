package model;

import java.util.List;

/**
 * Full output after running a knapsack algorithm (fractional or greedy).
 * Tells the dispatcher what to load, total weight, and total help score.
 */
public class KnapsackResult {

    private final List<KnapsackLineItem> manifest; // item-by-item loading list
    private final double totalWeight;            // sum of kg loaded (must be <= capacity)
    private final double totalScore;             // total help score achieved
    private final double capacity;               // truck limit W used for this run

    public KnapsackResult(List<KnapsackLineItem> manifest, double totalWeight, double totalScore, double capacity) {
        this.manifest = manifest;
        this.totalWeight = totalWeight;
        this.totalScore = totalScore;
        this.capacity = capacity;
    }

    /** List of every supply item and how much was loaded. */
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
