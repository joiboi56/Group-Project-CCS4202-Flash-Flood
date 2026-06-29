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

//Finds the shortest safe driving route during a flood.
//We use Dijkstra's Algorithm to choose tye shortest and safest path

public class DijkstraRouter {

    // Rescue teams should reach victims within 3 hours  if possible
    private static final double MAX_ACCEPTABLE_TIME = 60.0;

    // Roads at 700mm or deeper are treated as fully blocked
    private static final double BLOCKED_FLOOD_DEPTH_MM = 700.0;

    //Main routing function  works out the shortest safe path from one relief hub to every other place on the map.
    //the parameter graph(map =places+roads), sourceId(where the truck start), dMax(max flood depth(mm)), truckWeightKg(the truck weight)
    public RouteResult computeShortestPaths(Graph graph, String sourceId, double dMax, double truckWeightKg) {

        // the initial plan that show the shortest time to go at each places
        Map<String, Double> dist = new HashMap<>();
        // save the previous visited place to rebuild the route
        Map<String, String> prev = new HashMap<>();

        //Go through all the place on the map e
        for (Node n : graph.getAllNodes()) {

            //every place start with positive infinity since it haven't travelled there and no one knows the previous place
            dist.put(n.getId(), Double.POSITIVE_INFINITY);
            prev.put(n.getId(), null);
        }
        // The starting place start 0
        dist.put(sourceId, 0.0);

        // Priority queue always picks the place with the smallest travel time next
        PriorityQueue<String> pq = new PriorityQueue<>(Comparator.comparingDouble(dist::get));
        pq.add(sourceId);
        Set<String> visited = new HashSet<>();

        // Keep searching untill no more places left
        while (!pq.isEmpty()) {

            //always finding the nearest place
            String u = pq.poll();
            //Skip the place that already being visited
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

                // get a travel time and also include teh up travel time
                double edgeTravelTime = edge.getTravelMinutes();
                double candidate = dist.get(u) + edgeTravelTime;

                // Do not suggest routes that take more than 1 hours
                if (candidate > MAX_ACCEPTABLE_TIME) continue;

                // Found a faster way to reach v (update and queue it)
                if (candidate < dist.get(v)) {
                    //update the new shortest time
                    dist.put(v, candidate);
                    //remember the previous place
                    prev.put(v, u);
                    //add the place into the queue
                    pq.add(v);
                }
            }
        }

        // Build the final answer for every location on the map
        Map<String, NodeRouteInfo> results = new LinkedHashMap<>();
        for (Node n : graph.getAllNodes()) {
            String id = n.getId();
            double eta = dist.get(id);
            // Reachable only if we found a path and the destination is not abve Dmax
            boolean reachable = !Double.isInfinite(eta) && !n.isDisabled(dMax);
            List<String> path = reachable ? buildPath(prev, sourceId, id) : Collections.emptyList();
            if (reachable && path.isEmpty()) reachable = false;
            results.put(id, new NodeRouteInfo(id, eta, path, reachable));
        }

        return new RouteResult(sourceId, dMax, results);
    }


    private List<String> buildPath(Map<String, String> prev, String source, String target) {
        LinkedList<String> path = new LinkedList<>();
        //the current string is the target location(destination)
        String current = target;
        //saved at path if its not a null value
        while (current != null) {
            //insert the current node at the beginning of the list
            path.addFirst(current);
            // if the current node  reach the main base(UPM) then break
            if (current.equals(source)) break;
            current = prev.get(current);
        }

        // if the first path does nit equal to the main base(UPM), then it means no valid path
        if (path.isEmpty() || !path.getFirst().equals(source)) return Collections.emptyList();
        return path;
    }
}
