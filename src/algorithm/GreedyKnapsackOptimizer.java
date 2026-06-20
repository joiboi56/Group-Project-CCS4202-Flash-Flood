package algorithm;

import model.KnapsackLineItem;
import model.KnapsackResult;
import model.SupplyItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Greedy 0/1 knapsack: items sorted by priority/weight ratio; each item is
 * taken whole or skipped (no fractional loading).
 */
public class GreedyKnapsackOptimizer {

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
            double unitWeight = item.getWeightPerUnit();

            if (remainingCapacity <= 0 || maxFromStock <= 0 || unitWeight <= 0) {
                manifest.add(new KnapsackLineItem(item, 0.0, 0.0, 0.0));
                continue;
            }

            double weightLoaded = 0.0;
            double scoreAdded = 0.0;
            double stock = maxFromStock;

            while (unitWeight > 0 && unitWeight <= remainingCapacity && stock >= unitWeight) {
                weightLoaded += unitWeight;
                scoreAdded += item.getPriorityScore();
                remainingCapacity -= unitWeight;
                stock -= unitWeight;
                totalScore += item.getPriorityScore();
            }

            double fraction = unitWeight > 0 ? weightLoaded / unitWeight : 0.0;
            stockLeft.put(item.getId(), stock);
            manifest.add(new KnapsackLineItem(item, weightLoaded, scoreAdded, fraction));
        }

        double totalWeight = capacityW - remainingCapacity;
        return new KnapsackResult(manifest, totalWeight, totalScore, capacityW);
    }
}
