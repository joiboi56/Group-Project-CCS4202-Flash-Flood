package model;

//One relief supply item used by the knapsack algorithms.
 //weightPerUnit = w(i), priorityScore = v(i), availableKg = warehouse stock.
 
public class SupplyItem {

    private final String id;//id for item
    private String name;//readable name for item
    private double weightPerUnit;  // kg per one unit (e.g. one medical kit = 20 kg)
    private double priorityScore;  // how important this item is in an emergency
    private double availableKg;    // how much stock we have in the warehouse
    //method to call brand new supply item
    public SupplyItem(String id, String name, double weightPerUnit, double priorityScore, double availableKg) {
        this.id = id;
        this.name = name;
        this.weightPerUnit = weightPerUnit;
        this.priorityScore = priorityScore;
        this.availableKg = availableKg;
    }
    //read item
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

    //Value-to-weight ratio v(i)/w(i). Both knapsack algorithms sort by this.
     //Higher density = pack first (e.g. torch = 3.0, rice = 0.47).
     
     //method that calc the crucial ratio
    public double density() {
        return weightPerUnit <= 0 ? 0 : priorityScore / weightPerUnit;
    }
}
