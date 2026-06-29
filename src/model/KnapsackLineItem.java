package model;


public class KnapsackLineItem {
    //which supply item this line is about, e.g rice, water..
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
    //which item is this line about, e.g rice, water..
    public SupplyItem getItem() {
        return item;
    }
    //item weight
    public double getWeightLoaded() {
        return weightLoaded;
    }
    //add help score
    public double getScoreAdded() {
        return scoreAdded;
    }

    // How many whole units this represents (can be a decimal for fractional loading).
    public double getFraction() {
        return fraction;
    }
}
