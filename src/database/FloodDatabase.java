package database;

import model.Edge;
import model.Graph;
import model.Node;
import model.PriorityLevel;
import model.SupplyItem;
import model.VehicleProfile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DATABASE layer of the MVC architecture (see "Sketch / Framework"):
 *   - Road network data (the Graph: V, E, w(u,v), d(n))
 *   - Flood status (d(n) per node)
 *   - Relief items & priority (the supply item set I: w(i), v(i))
 *   - Vehicle capacity presets (W, Dmax)
 *   - Read / write data: {@link #save()} and {@link #load()}
 *
 * On startup the database is seeded with the exact scenario values from
 * the Project Initial Plan (NADMA Sector 4 flash flood scenario), then
 * any previously-saved field reports are layered on top from
 * flood_data.txt so changes survive a restart.
 */
public class FloodDatabase {

    private static final String DATA_FILE = "flood_data.txt";

    private final Graph graph = new Graph();
    private final List<SupplyItem> supplyItems = new ArrayList<>();
    private final List<VehicleProfile> vehicleProfiles = new ArrayList<>();

    /**
     * Maps a crisis node id to the (from -> to) hub edge whose baseline
     * travel cost w(u,v) is updated when a field report is filed for
     * that node. This mirrors the "Baseline Travel Cost w(u,v)" field
     * on the "I want to give information" form, which is keyed only by
     * the target crisis node.
     */
    private static final Map<String, String[]> PRIMARY_EDGE = new HashMap<>();
    static {
        PRIMARY_EDGE.put("SKS", new String[]{"UPM", "SKS"});
        PRIMARY_EDGE.put("SMK", new String[]{"UPM", "SMK"});
        PRIMARY_EDGE.put("U360", new String[]{"UPM", "U360"});
        PRIMARY_EDGE.put("KTMB", new String[]{"UPM", "KTMB"});
        PRIMARY_EDGE.put("SGR", new String[]{"UNI", "SGR"});
        PRIMARY_EDGE.put("MRB", new String[]{"UNI", "MRB"});
    }

    public FloodDatabase() {
        seedDefaults();
        load();
    }

    /** Seed V, E, I and vehicle profiles with the values from the project sketch. */
    private void seedDefaults() {

        // ---- V: command hubs and affected target zones ----
        graph.addNode(new Node("UPM", "UPM Main Base", PriorityLevel.NONE, 100));
        graph.addNode(new Node("UNI", "UNITEN Sub-base", PriorityLevel.NONE, 100));
        graph.addNode(new Node("SKS", "SK Sri Serdang", PriorityLevel.CRITICAL, 450));
        graph.addNode(new Node("SMK", "SMK Sri Serdang", PriorityLevel.HIGH, 400));
        graph.addNode(new Node("U360", "Univ 360", PriorityLevel.MODERATE, 300));
        graph.addNode(new Node("KTMB", "KTMB Serdang Station", PriorityLevel.HIGH, 420));
        graph.addNode(new Node("SGR", "SMK Sungai Ramal", PriorityLevel.CRITICAL, 480));
        graph.addNode(new Node("MRB", "SK Sg Merab", PriorityLevel.HIGH, 350));

        // ---- E: directed weighted road links, w(u,v) in minutes ----
        // Hub <-> hub link
        graph.addEdge(new Edge("UNI", "UPM", 7));
        graph.addEdge(new Edge("UPM", "UNI", 7));

        // UPM (western/central corridor) -> crisis nodes
        graph.addEdge(new Edge("UPM", "SKS", 16));   // UNI->UPM->SKS = 23
        graph.addEdge(new Edge("UPM", "SMK", 14));   // UNI->UPM->SMK = 21
        graph.addEdge(new Edge("UPM", "U360", 12));  // UNI->UPM->U360 = 19
        graph.addEdge(new Edge("UPM", "KTMB", 17));  // UNI->UPM->KTMB = 24

        // UNI (eastern corridor) -> crisis nodes
        graph.addEdge(new Edge("UNI", "SGR", 8));    // UNI->SGR = 8
        graph.addEdge(new Edge("UNI", "MRB", 11));   // UNI->MRB = 11

        // Cross-links between crisis nodes (creates a richer, more realistic graph)
        graph.addEdge(new Edge("SKS", "SMK", 3));
        graph.addEdge(new Edge("SMK", "U360", 4));
        graph.addEdge(new Edge("U360", "KTMB", 6));
        graph.addEdge(new Edge("SGR", "MRB", 6));
        graph.addEdge(new Edge("KTMB", "MRB", 9));
        graph.addEdge(new Edge("SGR", "KTMB", 20));

        // ---- I: critical resource item set, w(i) in kg, v(i) = priority/survival score ----
        supplyItems.add(new SupplyItem("medical", "Insulin & medical kits", 30.0, 600.0));
        supplyItems.add(new SupplyItem("formula", "Infant formula & diapers", 144.0, 1080.0));
        supplyItems.add(new SupplyItem("hygiene", "Hygiene kits", 80.0, 600.0));
        supplyItems.add(new SupplyItem("rations", "Food rations (MRE)", 450.0, 2100.0));
        supplyItems.add(new SupplyItem("clothing", "Blankets & dry clothing", 296.0, 740.0));

        // ---- Vehicle presets: W (payload capacity) and Dmax (safe flood depth) ----
        vehicleProfiles.add(new VehicleProfile("Rescue Boat", 550, 1200));
        vehicleProfiles.add(new VehicleProfile("Relief Truck", 1000, 400));
    }

    public Graph getGraph() {
        return graph;
    }

    public List<SupplyItem> getSupplyItems() {
        return supplyItems;
    }

    public List<VehicleProfile> getVehicleProfiles() {
        return vehicleProfiles;
    }

    public SupplyItem findSupplyItem(String id) {
        for (SupplyItem s : supplyItems) {
            if (s.getId().equals(id)) {
                return s;
            }
        }
        return null;
    }

    /* =======================================================
       Field-report write operations (Controller -> Database)
       ======================================================= */

    /** Update d(n) for a given node. */
    public boolean updateNodeFloodDepth(String nodeId, double depth) {
        Node n = graph.getNode(nodeId);
        if (n == null) {
            return false;
        }
        n.setFloodDepthMm(depth);
        return true;
    }

    /** Update w(u,v) (baseline travel cost) of the primary hub edge feeding this node. */
    public boolean updateEdgeBaseWeight(String nodeId, double baseWeight) {
        String[] pair = PRIMARY_EDGE.get(nodeId);
        if (pair == null) {
            return false;
        }
        Edge e = graph.findEdge(pair[0], pair[1]);
        if (e == null) {
            return false;
        }
        e.setBaseWeight(baseWeight);
        return true;
    }

    /** Update w(i) and/or v(i) for a supply item. */
    public boolean updateSupplyItem(String itemId, Double weight, Double priority) {
        SupplyItem item = findSupplyItem(itemId);
        if (item == null) {
            return false;
        }
        if (weight != null) {
            item.setWeightKg(weight);
        }
        if (priority != null) {
            item.setPriorityValue(priority);
        }
        return true;
    }

    /** Directly update the baseline weight of any edge by its from/to pair. */
    public boolean updateEdgeDirect(String from, String to, double weight) {
        Edge e = graph.findEdge(from, to);
        if (e == null) return false;
        e.setBaseWeight(weight);
        return true;
    }

    /** Add a brand-new node to V at runtime. */
    public void addNode(String id, String name, PriorityLevel priority, double floodDepth) {
        graph.addNode(new Node(id, name, priority, floodDepth));
    }

    /** Add a directed edge to E at runtime. */
    public void addEdge(String from, String to, double weight) {
        graph.addEdge(new Edge(from, to, weight));
    }

    /* =======================================================
       Simple file-based persistence (read/write data)
       ======================================================= */

    /** Persist current node flood depths, edge weights and item values. */
    public synchronized void save() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(DATA_FILE))) {
            for (Node n : graph.getAllNodes()) {
                pw.printf("NODE,%s,%s,%.2f%n",
                        n.getId(),
                        n.getPriority().name(),
                        n.getFloodDepthMm());
            }
            for (Edge e : graph.getEdges()) {
                pw.printf("EDGE,%s,%s,%.2f%n", e.getFrom(), e.getTo(), e.getBaseWeight());
            }
            for (SupplyItem s : supplyItems) {
                pw.printf("ITEM,%s,%.2f,%.2f%n", s.getId(), s.getWeightKg(), s.getPriorityValue());
            }
        } catch (IOException ex) {
            System.err.println("[FloodDatabase] could not save " + DATA_FILE + ": " + ex.getMessage());
        }
    }

    /** Load previously-saved field reports on top of the seeded defaults, if present. */
    public synchronized void load() {
        File f = new File(DATA_FILE);
        if (!f.exists()) {
            return;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                String[] parts = line.split(",");
                switch (parts[0]) {
                    case "NODE": {
                        if (parts.length == 3) {
                            // old format: NODE,id,floodDepth
                            Node n = graph.getNode(parts[1]);
                            if (n != null) {
                                n.setFloodDepthMm(Double.parseDouble(parts[2]));
                            }
                        } else if (parts.length >= 4) {
                            // new format: NODE,id,PRIORITY,floodDepth
                            // if node already exists just update depth;
                            // if it doesn't (user added at runtime), create it
                            Node existing = graph.getNode(parts[1]);
                            double depth = Double.parseDouble(parts[3]);
                            if (existing != null) {
                                existing.setFloodDepthMm(depth);
                            } else {
                                PriorityLevel pl;
                                try { pl = PriorityLevel.valueOf(parts[2]); }
                                catch (IllegalArgumentException e) { pl = PriorityLevel.MODERATE; }
                                graph.addNode(new Node(parts[1], parts[1], pl, depth));
                            }
                        }
                        break;
                    }
                    case "EDGE": {
                        Edge e = graph.findEdge(parts[1], parts[2]);
                        if (e != null) {
                            e.setBaseWeight(Double.parseDouble(parts[3]));
                        } else {
                            // edge was added at runtime — restore it
                            if (graph.getNode(parts[1]) != null && graph.getNode(parts[2]) != null) {
                                graph.addEdge(new Edge(parts[1], parts[2], Double.parseDouble(parts[3])));
                            }
                        }
                        break;
                    }
                    case "ITEM": {
                        SupplyItem s = findSupplyItem(parts[1]);
                        if (s != null) {
                            s.setWeightKg(Double.parseDouble(parts[2]));
                            s.setPriorityValue(Double.parseDouble(parts[3]));
                        }
                        break;
                    }
                    default:
                        // ignore unknown lines
                }
            }
        } catch (IOException ex) {
            System.err.println("[FloodDatabase] could not load " + DATA_FILE + ": " + ex.getMessage());
        }
    }
}