package algorithm;

import model.KnapsackLineItem;
import model.KnapsackResult;
import model.SupplyItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fractional Knapsack — decides what supplies to put on the rescue truck.
 *
 * Real-life idea: the truck can only carry 500 kg, but we have many items
 * (water, blankets, medical kits). We want to pack items that give the most
 * "help score" per kilogram. Items like rice or water CAN be split into
 * smaller portions, so we fill every last kg of space.
 */
public class FractionalKnapsackOptimizer {

    /**
     * Picks the best mix of supplies for one trip.
     *
     * @param items      list of available relief items with weight, priority, stock
     * @param capacityW  maximum truck weight in kg (W from our project report)
     * @return           manifest showing what to load, total weight and help score
     */
    public KnapsackResult optimize(List<SupplyItem> items, double capacityW) {
        // Step 1: sort items by density (priority / weight) — highest first
        // e.g. torch has density 3.0, medical kit only 0.5, so torch goes first
        List<SupplyItem> sorted = new ArrayList<>(items);
        sorted.sort((a, b) -> Double.compare(b.density(), a.density()));

        // Track how much stock is left for each item while we pack
        Map<String, Double> stockLeft = new HashMap<>();
        for (SupplyItem item : items) {
            stockLeft.put(item.getId(), item.getAvailableKg());
        }

        double remainingCapacity = capacityW;
        double totalScore = 0.0;
        List<KnapsackLineItem> manifest = new ArrayList<>();

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
