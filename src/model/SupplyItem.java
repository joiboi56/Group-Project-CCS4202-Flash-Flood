package model;

/**
 * Relief supply item for the fractional knapsack module.
 * weightPerUnit = w(i), priorityScore = v(i), availableKg = stock on hand.
 */
public class SupplyItem {

    private final String id;
    private String name;
    private double weightPerUnit;
    private double priorityScore;
    private double availableKg;

    public SupplyItem(String id, String name, double weightPerUnit, double priorityScore, double availableKg) {
        this.id = id;
        this.name = name;
        this.weightPerUnit = weightPerUnit;
        this.priorityScore = priorityScore;
        this.availableKg = availableKg;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getWeightPerUnit() {
        return weightPerUnit;
    }

    public void setWeightPerUnit(double weightPerUnit) {
        this.weightPerUnit = weightPerUnit;
    }

    public double getPriorityScore() {
        return priorityScore;
    }

    public void setPriorityScore(double priorityScore) {
        this.priorityScore = priorityScore;
    }

    public double getAvailableKg() {
        return availableKg;
    }

    public void setAvailableKg(double availableKg) {
        this.availableKg = availableKg;
    }

    public double density() {
        return weightPerUnit <= 0 ? 0 : priorityScore / weightPerUnit;
    }
}
