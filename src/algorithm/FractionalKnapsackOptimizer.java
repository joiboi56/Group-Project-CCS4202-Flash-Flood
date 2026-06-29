package algorithm;

import model.KnapsackLineItem;
import model.KnapsackResult;
import model.SupplyItem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//Fractional Knapsack — decides what supplies to put on the rescue truck.
 // The truck only can fit 500kg(customizable). Then, we have a load of items to be load in the truck
//The Fractional knapsack will decide based on the "help score" per kilogram. Thus, it will give the best combination
//and produced the highest help score .
public class FractionalKnapsackOptimizer {

    // Pick the best mix of items for one trip

    //The parameter included are items(list of available relief items wit weight,priority and stock), capacityW(max truck weight in kg)
    //return(showing what to load, total weight and help score
    public KnapsackResult optimize(List<SupplyItem> items, double capacityW) {

        // sorting the items first by following the (priority / weight) rule and choose the highest
        // e.g. torch has density 3.0, medical kit only 0.5, so torch goes first
        List<SupplyItem> sorted = new ArrayList<>(items);

        // compare the density(priority/weight0
        sorted.sort((a, b) -> Double.compare(b.density(), a.density()));

        // Track how much stock is left for each item while we pack
        Map<String, Double> stockLeft = new HashMap<>();
        for (SupplyItem item : items) {
            stockLeft.put(item.getId(), item.getAvailableKg());
        }

        //shows the remaining capacity of the truck left
        double remainingCapacity = capacityW;
        //to store the total priority score
        double totalScore = 0.0;

        // list of the final selected items and their quantities
        List<KnapsackLineItem> manifest = new ArrayList<>();

        //Go through all the sorted items by density
        for (SupplyItem item : sorted) {
            double maxFromStock = stockLeft.getOrDefault(item.getId(), 0.0);
            if (remainingCapacity <= 0 || maxFromStock <= 0) {
                manifest.add(new KnapsackLineItem(item, 0.0, 0.0, 0.0));
                continue;
            }

            double unitWeight = item.getWeightPerUnit();
            if (unitWeight <= 0) {
                manifest.add(new KnapsackLineItem(item, 0.0, 0.0, 0.0));
                continue;
            }

            // Load as much as we can — either until truck is full or stock runs out
            double weightLoaded = Math.min(remainingCapacity, maxFromStock);
            // Fractional part: we can take half a bag of rice if only half fits
            double fraction = weightLoaded / unitWeight;
            double scoreAdded = fraction * item.getPriorityScore();

            manifest.add(new KnapsackLineItem(item, weightLoaded, scoreAdded, fraction));
            totalScore += scoreAdded;
            remainingCapacity -= weightLoaded;
            stockLeft.put(item.getId(), maxFromStock - weightLoaded);
        }

        double totalWeight = capacityW - remainingCapacity;
        return new KnapsackResult(manifest, totalWeight, totalScore, capacityW);
    }
}
