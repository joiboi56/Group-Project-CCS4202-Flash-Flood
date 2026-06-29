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

/**
 * Finds the shortest safe driving route during a flood.
 * We use Dijkstra's Algorithm from CCS4202 — it picks the fastest path
 * while skipping roads that are too flooded or too dangerous for the truck.
 */
public class DijkstraRouter {

    // Rescue teams should reach victims within 3 hours (180 minutes) if possible
    private static final double MAX_ACCEPTABLE_TIME = 180.0;

    // Roads at 700mm or deeper are treated as fully blocked (truck cannot pass)
    private static final double BLOCKED_FLOOD_DEPTH_MM = 700.0;

    /**
     * Main routing function — works out the shortest safe path from one relief hub
     * to every other place on the map.
     *
     * In simple terms: imagine UPM is the starting point and we want the fastest
     * route to each school or station, but only using roads that are still passable.
     *
     * @param graph          the map (places + roads) stored as a graph
     * @param sourceId       where the truck starts, e.g. "UPM" or "UNIT"
     * @param dMax           max flood depth (mm) the vehicle can handle at a location
     * @param truckWeightKg  how heavy the loaded truck is — some bridges have weight limits
     */
    public RouteResult computeShortestPaths(Graph graph, String sourceId, double dMax, double truckWeightKg) {
        // dist = shortest travel time found so far to each place
        Map<String, Double> dist = new HashMap<>();
        // prev = which place we came from — used later to rebuild the route
        Map<String, String> prev = new HashMap<>();
        for (Node n : graph.getAllNodes()) {
            dist.put(n.getId(), Double.POSITIVE_INFINITY);
            prev.put(n.getId(), null);
        }
        dist.put(sourceId, 0.0);

        // Priority queue always picks the place with the smallest travel time next
        PriorityQueue<String> pq = new PriorityQueue<>(Comparator.comparingDouble(dist::get));
        pq.add(sourceId);
        Set<String> visited = new HashSet<>();

        while (!pq.isEmpty()) {
            String u = pq.poll();
            if (visited.contains(u)) continue;
            visited.add(u);

            Node nodeU = graph.getNode(u);
            if (nodeU == null) continue;

            // Look at every road leaving the current place
            for (Edge edge : graph.getOutgoingEdges(u)) {
                String v = edge.getTo();
                if (visited.contains(v)) continue;

                // Skip if truck is too heavy for this road
                if (truckWeightKg > edge.getWeightLimitKg()) continue;
                // Skip flooded roads or roads where water is too deep
                if (edge.isFlooded() || edge.getFloodDepthMm() >= BLOCKED_FLOOD_DEPTH_MM) continue;

                double edgeTravelTime = edge.getTravelMinutes();
                double candidate = dist.get(u) + edgeTravelTime;

                // Do not suggest routes that take more than 3 hours
                if (candidate > MAX_ACCEPTABLE_TIME) continue;

                // Found a faster way to reach v — update and queue it
                if (candidate < dist.get(v)) {
                    dist.put(v, candidate);
                    prev.put(v, u);
                    pq.add(v);
                }
            }
        }

        // Build the final answer for every location on the map
        Map<String, NodeRouteInfo> results = new LinkedHashMap<>();
        for (Node n : graph.getAllNodes()) {
            String id = n.getId();
            double eta = dist.get(id);
            // Reachable only if we found a path AND the destination is not underwater (>= Dmax)
            boolean reachable = !Double.isInfinite(eta) && !n.isDisabled(dMax);
            List<String> path = reachable ? buildPath(prev, sourceId, id) : Collections.emptyList();
            if (reachable && path.isEmpty()) reachable = false;
            results.put(id, new NodeRouteInfo(id, eta, path, reachable));
        }

        return new RouteResult(sourceId, dMax, results);
    }

    /**
     * Traces back from the destination to the start using the prev map.
     * Example output path: [UPM, U360, SKSS] meaning UPM -> Univ 360 -> SK Sri Serdang
     */
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
