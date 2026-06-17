package algorithm;

import model.KnapsackLineItem;
import model.KnapsackResult;
import model.SupplyItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Module 02: Fractional knapsack for emergency payload loading.
 */
public class FractionalKnapsackOptimizer {

    public KnapsackResult optimize(List<SupplyItem> items, double capacityW) {
        List<SupplyItem> sorted = new ArrayList<>(items);
        sorted.sort((a, b) -> Double.compare(b.density(), a.density()));

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

            double weightLoaded = Math.min(remainingCapacity, maxFromStock);
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
