package model;

import java.util.List;


public class KnapsackResult {

    private final List<KnapsackLineItem> manifest; // item-by-item loading list
    private final double totalWeight;            // sum of kg loaded (must be <= capacity)
    private final double totalScore;             // total help score achieved
    private final double capacity;               // truck limit W used for this run
    //like a receipt that contatin all those 4 info
    public KnapsackResult(List<KnapsackLineItem> manifest, double totalWeight, double totalScore, double capacity) {
        this.manifest = manifest;
        this.totalWeight = totalWeight;
        this.totalScore = totalScore;
        this.capacity = capacity;
    }

    // List of every supply item and how much was loaded. 
    public List<KnapsackLineItem> getManifest() {
        return manifest;
    }
    //get total weight loaded of the truck
    public double getTotalWeight() {
        return totalWeight;
    }
    //compare 2 packing methods, which one is better, greedy or fractional knapsack
    public double getTotalScore() {
        return totalScore;
    }
    //get the truck capacity used
    public double getCapacity() {
        return capacity;
    }
}
