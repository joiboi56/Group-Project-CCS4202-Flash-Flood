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
 * Module 01: Flood Route Optimisation -- Dijkstra's Algorithm.
 *
 * This implementation follows, step for step, the 9-step flowchart
 * "Dijkstra's Algorithm -- Flood Route Optimisation" from the group
 * project sketch:
 *
 *  1. INPUT: GRAPH G=(V,E)
 *  2. Set source node s; dist[s]=0, dist[all others]=infinity, prev[all]=null
 *  3. Initialize min-heap priority queue PQ; insert (s,0) into PQ; mark all nodes unvisited
 *  4. Is PQ empty? (loop while NO)
 *  5. Extract node u with minimum dist[u] from PQ; mark u as visited
 *  6. Is node u impassable? d(u) >= Dmax OR status = DISABLED -> skip
 *  7. For each unvisited neighbour v of u: is edge (u,v) passable?
 *     dist[u] + w(u,v) < dist[v]? -> relax edge, update dist[v], prev[v]=u, push (v,dist[v]) into PQ
 *  8. OUTPUT: Shortest safe route V'=[...]
 *  9. Display route with ETA for dispatcher, e.g. UPM -> UNITEN -> SK Sg Merab (11 min)
 */
public class DijkstraRouter {

    /**
     * Runs Dijkstra's algorithm from a single source node across the
     * whole graph, respecting the Impassable Corridor Constraint.
     *
     * @param graph    G = (V, E)
     * @param sourceId the source hub, e.g. "UPM" or "UNI"
     * @param dMax     the vehicle's safe flood depth threshold (mm)
     * @return a RouteResult containing the shortest, safe ETA and path
     *         from sourceId to every node in the graph
     */
    public RouteResult computeShortestPaths(Graph graph, String sourceId, double dMax) {

        // --- Step 2: initialise dist[] and prev[] tables ---
        Map<String, Double> dist = new HashMap<>();
        Map<String, String> prev = new HashMap<>();
        for (Node n : graph.getAllNodes()) {
            dist.put(n.getId(), Double.POSITIVE_INFINITY);
            prev.put(n.getId(), null);
        }
        dist.put(sourceId, 0.0);

        // --- Step 3: initialise min-heap PQ; insert (s,0); mark all unvisited ---
        PriorityQueue<String> pq = new PriorityQueue<>(Comparator.comparingDouble(dist::get));
        pq.add(sourceId);
        Set<String> visited = new HashSet<>();

        // --- Step 4: loop while PQ is not empty ---
        while (!pq.isEmpty()) {

            // --- Step 5: extract node u with minimum dist[u]; mark visited ---
            String u = pq.poll();
            if (visited.contains(u)) {
                continue; // stale entry from an earlier, larger dist[u]
            }
            visited.add(u);

            Node nodeU = graph.getNode(u);

            // --- Step 6: is node u impassable? d(u) >= Dmax OR status = DISABLED -> skip ---
            if (nodeU == null || nodeU.isDisabled(dMax)) {
                continue; // do not expand neighbours of a disabled node
            }

            // --- Step 7: for each unvisited neighbour v of u ---
            for (Edge edge : graph.getOutgoingEdges(u)) {
                String v = edge.getTo();
                if (visited.contains(v)) {
                    continue;
                }

                // is edge (u,v) passable? (flood depth check on both endpoints)
                if (!graph.isEdgePassable(edge, dMax)) {
                    continue; // NO -> skip
                }

                // dist[u] + w(u,v) < dist[v] ?
                double candidate = dist.get(u) + edge.effectiveWeight();
                if (candidate < dist.get(v)) {
                    // YES -> relax edge: update dist[v], prev[v]=u, push (v,dist[v])
                    dist.put(v, candidate);
                    prev.put(v, u);
                    pq.add(v);
                }
            }
        }

        // --- Step 8/9: build OUTPUT - shortest safe route + ETA for every node ---
        Map<String, NodeRouteInfo> results = new LinkedHashMap<>();
        for (Node n : graph.getAllNodes()) {
            String id = n.getId();
            double eta = dist.get(id);
            boolean reachable = !Double.isInfinite(eta) && !n.isDisabled(dMax);
            List<String> path = reachable ? buildPath(prev, sourceId, id) : Collections.emptyList();
            if (reachable && path.isEmpty()) {
                reachable = false; // safety net: no valid path could be reconstructed
            }
            results.put(id, new NodeRouteInfo(id, eta, path, reachable));
        }

        return new RouteResult(sourceId, dMax, results);
    }

    /** Walk prev[] backwards from target to source to reconstruct the path. */
    private List<String> buildPath(Map<String, String> prev, String source, String target) {
        LinkedList<String> path = new LinkedList<>();
        String current = target;
        while (current != null) {
            path.addFirst(current);
            if (current.equals(source)) {
                break;
            }
            current = prev.get(current);
        }
        if (path.isEmpty() || !path.getFirst().equals(source)) {
            return Collections.emptyList();
        }
        return path;
    }
}
