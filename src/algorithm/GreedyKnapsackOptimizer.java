package algorithm;

import model.KnapsackLineItem;
import model.KnapsackResult;
import model.SupplyItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//Greedy (0/1 knapsack) have same goals with as fractional but its a special designed for items that
//can't be split like first aid kit
//Still take the high priority item first but only whole unit fits.
public class GreedyKnapsackOptimizer {

    // the parameter included is items(items form the supplies list), capacityW(truck weight limit in kg)

    public KnapsackResult optimize(List<SupplyItem> items, double capacityW) {
        //create a copy of supply list to avoid altering the original list
        List<SupplyItem> sorted = new ArrayList<>(items);
        //items are being sorted by descending order based onn their ratio value
        sorted.sort((a, b) -> Double.compare(b.density(), a.density()));

        //stored the remaining stock available for each items
        Map<String, Double> stockLeft = new HashMap<>();

        for (SupplyItem item : items) {
            stockLeft.put(item.getId(), item.getAvailableKg());
        }

        //Store the remaining truck capacity
        double remainingCapacity = capacityW;
        //store the total score of the selected items
        double totalScore = 0.0;
        //list to store the selected items and loading details
        List<KnapsackLineItem> manifest = new ArrayList<>();

        //Sorted the item based on the highest value-to-weight ratio
        for (SupplyItem item : sorted) {

            //get the remaining stock for the current item, if not found defaul to 0
            double maxFromStock = stockLeft.getOrDefault(item.getId(), 0.0);
            //get the weight on one unit of the curent items
            double unitWeight = item.getWeightPerUnit();

            //skip the item if the truck full, no stock remains or the weight unit is invalid
            if (remainingCapacity <= 0 || maxFromStock <= 0 || unitWeight <= 0) {
                manifest.add(new KnapsackLineItem(item, 0.0, 0.0, 0.0));
                continue;
            }

            // Store the total weight loaded, priority score and available stock
            double weightLoaded = 0.0;
            double scoreAdded = 0.0;
            double stock = maxFromStock;

            // Keep adding whole units while there is room and stock left
            // e.g. each medical kit is 20 kg — if only 12 kg left, we skip it entirely
            while (unitWeight > 0 && unitWeight <= remainingCapacity && stock >= unitWeight) {

                // Add one unit's weight to the vehicle
                weightLoaded += unitWeight;
                //Increase the score based on the loaded unit
                scoreAdded += item.getPriorityScore();

                //reduced the remaining vehicle capacity
                remainingCapacity -= unitWeight;

                //Reduce the available
                stock -= unitWeight;

                //Update the total priority score
                totalScore += item.getPriorityScore();
            }
            //calculate how many complete units were loaded
            double fraction = unitWeight > 0 ? weightLoaded / unitWeight : 0.0;

            // update the remaining stock left after loading the items
            stockLeft.put(item.getId(), stock);
            //save the loading details for the current item
            manifest.add(new KnapsackLineItem(item, weightLoaded, scoreAdded, fraction));
        }
        //calculate the total weight loaded in the truck
        double totalWeight = capacityW - remainingCapacity;
        //return all the values
        return new KnapsackResult(manifest, totalWeight, totalScore, capacityW);
    }
}
