package model;

/**
 * One line on the truck loading list (cargo manifest).
 * Shows how much of one supply item was packed and how much help score it gives.
 */
public class KnapsackLineItem {

    private final SupplyItem item;
    private final double weightLoaded; // kg actually put on the truck
    private final double scoreAdded;   // help score from this line
    private final double fraction;     // 1.0 = full unit, 0.5 = half (fractional knapsack)

    public KnapsackLineItem(SupplyItem item, double weightLoaded, double scoreAdded, double fraction) {
        this.item = item;
        this.weightLoaded = weightLoaded;
        this.scoreAdded = scoreAdded;
        this.fraction = fraction;
    }

    public SupplyItem getItem() {
        return item;
    }

    public double getWeightLoaded() {
        return weightLoaded;
    }

    public double getScoreAdded() {
        return scoreAdded;
    }

    /** How many whole units this represents (can be a decimal for fractional loading). */
    public double getFraction() {
        return fraction;
    }
}
