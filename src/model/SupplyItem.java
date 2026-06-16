package model;

/**
 * A single critical resource item i &isin; I, e.g. "Insulin & medical kits".
 *
 * w(i) = weightKg      -> explicit weight of the item
 * v(i) = priorityValue -> priority / survival score of the item
 *
 * density(i) = v(i) / w(i) is the value-to-weight ratio used to sort
 * items for the Fractional Knapsack Algorithm.
 */
public class SupplyItem {

    private final String id;     // short key, e.g. "medical", "rations"
    private final String name;   // display name, e.g. "Insulin & medical kits"
    private double weightKg;      // w(i)
    private double priorityValue; // v(i)

    public SupplyItem(String id, String name, double weightKg, double priorityValue) {
        this.id = id;
        this.name = name;
        this.weightKg = weightKg;
        this.priorityValue = priorityValue;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(double weightKg) {
        this.weightKg = weightKg;
    }

    public double getPriorityValue() {
        return priorityValue;
    }

    public void setPriorityValue(double priorityValue) {
        this.priorityValue = priorityValue;
    }

    /** density(i) = v(i) / w(i) -- value-to-weight ratio. */
    public double density() {
        return weightKg <= 0 ? 0 : priorityValue / weightKg;
    }
}
