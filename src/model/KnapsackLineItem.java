package model;

/**
 * One row of the cargo manifest produced by the Fractional Knapsack
 * Algorithm: how much of a given supply item was loaded, the survival
 * score it contributed, and what fraction (x_i) of the item was used.
 */
public class KnapsackLineItem {

    private final SupplyItem item;
    private final double weightLoaded; // kg actually placed on the vehicle
    private final double scoreAdded;   // contribution to total survival score
    private final double fraction;     // x_i in [0,1]; 1.0 = whole item loaded

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

    public double getFraction() {
        return fraction;
    }
}
