package algorithm;

import model.Edge;
import model.Graph;
import model.Node;
import model.NodeRouteInfo;
import model.RouteResult;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

public class DijkstraRouter {

    // Hard cap: 3 hours (180 minutes) for emergency rescue
    private static final double MAX_ACCEPTABLE_TIME = 180.0;

    // Vehicle speeds (km/h)
    private static final double CAR_SPEED_KMPH = 50.0;
    private static final double BOAT_SPEED_KMPH = 10.0;

    // Depth thresholds (mm)
    private static final double CAR_LIMIT_MM = 700.0;

    public RouteResult computeShortestPaths(Graph graph, String sourceId, double dMax, double truckWeightKg) {
        Map<String, Double> dist = new HashMap<>();
        Map<String, String> prev = new HashMap<>();
        for (Node n : graph.getAllNodes()) {
            dist.put(n.getId(), Double.POSITIVE_INFINITY);
            prev.put(n.getId(), null);
        }
        dist.put(sourceId, 0.0);

        PriorityQueue<String> pq = new PriorityQueue<>(Comparator.comparingDouble(dist::get));
        pq.add(sourceId);
        Set<String> visited = new HashSet<>();

        while (!pq.isEmpty()) {
            String u = pq.poll();
            if (visited.contains(u)) continue;
            visited.add(u);

            Node nodeU = graph.getNode(u);
            if (nodeU == null) continue;

            for (Edge edge : graph.getOutgoingEdges(u)) {
                String v = edge.getTo();
                if (visited.contains(v)) continue;

                // --- VEHICLE LOGIC START ---
                double edgeTravelTime = Double.POSITIVE_INFINITY;

                // 1. Check Weight Limit (Always applies)
                if (truckWeightKg <= edge.getWeightLimitKg()) {

                    // 2. If not flooded or flood depth is LESS than 700mm -> USE CAR
                    if (!edge.isFlooded() || edge.getFloodDepthMm() < CAR_LIMIT_MM) {
                        // Calculate time based on CAR speed
                        double distanceKm = (edge.getTravelMinutes() / 60.0) * CAR_SPEED_KMPH;
                        edgeTravelTime = (distanceKm / CAR_SPEED_KMPH) * 60.0;
                    }
                    // 3. If flood depth is >= 700mm -> USE BOAT
                    else {
                        // Calculate time based on BOAT speed
                        double distanceKm = (edge.getTravelMinutes() / 60.0) * CAR_SPEED_KMPH;
                        edgeTravelTime = (distanceKm / BOAT_SPEED_KMPH) * 60.0;
                    }
                }
                // --- VEHICLE LOGIC END ---

                // Skip if no vehicle can access (or weight limit exceeded)
                if (Double.isInfinite(edgeTravelTime)) continue;

                double candidate = dist.get(u) + edgeTravelTime;

                // 4. 3-Hour Hard Cap check
                if (candidate > MAX_ACCEPTABLE_TIME) continue;

                if (candidate < dist.get(v)) {
                    dist.put(v, candidate);
                    prev.put(v, u);
                    pq.add(v);
                }
            }
        }

        Map<String, NodeRouteInfo> results = new LinkedHashMap<>();
        for (Node n : graph.getAllNodes()) {
            String id = n.getId();
            double eta = dist.get(id);
            boolean reachable = !Double.isInfinite(eta) && !n.isDisabled(dMax);
            List<String> path = reachable ? buildPath(prev, sourceId, id) : Collections.emptyList();
            if (reachable && path.isEmpty()) reachable = false;
            results.put(id, new NodeRouteInfo(id, eta, path, reachable));
        }

        return new RouteResult(sourceId, dMax, results);
    }

    private List<String> buildPath(Map<String, String> prev, String source, String target) {
        LinkedList<String> path = new LinkedList<>();
        String current = target;
        while (current != null) {
            path.addFirst(current);
            if (current.equals(source)) break;
            current = prev.get(current);
        }
        if (path.isEmpty() || !path.getFirst().equals(source)) return Collections.emptyList();
        return path;
    }
}