package algorithm;

import model.KnapsackLineItem;
import model.KnapsackResult;
import model.SupplyItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Module 02: Emergency Payload Optimisation -- Fractional Knapsack Algorithm.
 *
 * This implementation follows, step for step, the 7-step flowchart
 * "Fractional Knapsack Algorithm -- Emergency Payload Optimisation"
 * from the group project sketch:
 *
 *  1. INPUT: Supply item set I
 *  2. Read w(i) and v(i) for each item i
 *  3. Compute value-to-weight density(i) = v(i) / w(i) for each item
 *  4. Sort items by density (descending order) -- O(n log n)
 *  5. Initialize vehicle variables: remaining_capacity = W, total_score = 0
 *  6. Loop: while items remain in the sorted list AND remaining_capacity > 0
 *       - take next item i (highest density first)
 *       - if w(i) <= remaining_capacity -> load full item i
 *       - else -> load fractional portion: fraction = remaining_capacity / w(i),
 *                 score_added = fraction * v(i)
 *       - update running totals: total_score += score_added,
 *                                 remaining_capacity -= weight_taken
 *  7. OUTPUT: Cargo manifest + total survival score
 */
public class FractionalKnapsackOptimizer {

    /**
     * Computes the optimal (fractional) loading plan for a vehicle with
     * payload capacity W.
     *
     * @param items      the critical resource item set I
     * @param capacityW  vehicle payload capacity W (kg)
     * @return a KnapsackResult containing the cargo manifest, total
     *         weight loaded and total survival score achieved
     */
    public KnapsackResult optimize(List<SupplyItem> items, double capacityW) {

        // --- Steps 1 & 2: INPUT supply item set I; w(i), v(i) already on each SupplyItem ---

        // --- Step 3: density(i) = v(i) / w(i) -- computed lazily via item.density() ---

        // --- Step 4: sort items by density, descending order, O(n log n) ---
        List<SupplyItem> sorted = new ArrayList<>(items);
        sorted.sort((a, b) -> Double.compare(b.density(), a.density()));

        // --- Step 5: initialise vehicle variables ---
        double remainingCapacity = capacityW;
        double totalScore = 0.0;
        List<KnapsackLineItem> manifest = new ArrayList<>();

        // --- Step 6: main loop over the sorted item list ---
        for (SupplyItem item : sorted) {

            // "Items remaining in sorted list?" -> always true here (for-each)
            // "remaining_capacity == 0? Vehicle fully loaded?" -> if so, item is skipped (x_i = 0)
            if (remainingCapacity <= 0) {
                manifest.add(new KnapsackLineItem(item, 0.0, 0.0, 0.0));
                continue;
            }

            // "Take next item i (highest density first)" -- already in order

            if (item.getWeightKg() <= remainingCapacity) {
                // "w(i) <= remaining_capacity? YES -> Load full item i"
                double weightLoaded = item.getWeightKg();
                double scoreAdded = item.getPriorityValue();

                manifest.add(new KnapsackLineItem(item, weightLoaded, scoreAdded, 1.0));

                // update running totals
                totalScore += scoreAdded;
                remainingCapacity -= weightLoaded;
            } else {
                // "NO -> Load fractional portion of item i:
                //  fraction = remaining_capacity / w(i)
                //  score_added = fraction * v(i)"
                double fraction = remainingCapacity / item.getWeightKg();
                double scoreAdded = fraction * item.getPriorityValue();
                double weightLoaded = remainingCapacity; // fills the vehicle exactly

                manifest.add(new KnapsackLineItem(item, weightLoaded, scoreAdded, fraction));

                // update running totals
                totalScore += scoreAdded;
                remainingCapacity = 0.0;
            }
        }

        // --- Step 7: OUTPUT cargo manifest + total survival score ---
        double totalWeight = capacityW - remainingCapacity;
        return new KnapsackResult(manifest, totalWeight, totalScore, capacityW);
    }
}
