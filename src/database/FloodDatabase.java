package database;

import model.Edge;
import model.Graph;
import model.Node;
import model.PlaceType;
import model.SupplyItem;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * DATABASE PACKAGE — stores all project data in memory and in flood_data.txt.
 *
 * Holds three things the algorithms need:
 *   1) The road graph (places + roads) for Dijkstra
 *   2) The supply list for knapsack algorithms
 *   3) Settings: truck capacity W and flood limit Dmax
 */
public class FloodDatabase {

    private static final String DATA_FILE = "flood_data.txt";

    private final Graph graph = new Graph();
    private final List<SupplyItem> supplyItems = new ArrayList<>();
    private double truckCapacityKg = 500;  // W — max truck payload in kg
    private double dMaxMm = 400;           // deepest flood (mm) the vehicle can drive through

    /**
     * Constructor runs when the app starts.
     * First loads the built-in Selangor sample, then overwrites with flood_data.txt if it exists.
     */
    public FloodDatabase() {
        loadSelangorSample();
        load();
    }

    /** Returns the map graph so Dijkstra and the GUI can use it. */
    public Graph getGraph() {
        return graph;
    }

    /** Returns the list of relief items for knapsack packing. */
    public List<SupplyItem> getSupplyItems() {
        return supplyItems;
    }

    public double getTruckCapacityKg() {
        return truckCapacityKg;
    }

    public void setTruckCapacityKg(double truckCapacityKg) {
        this.truckCapacityKg = truckCapacityKg;
    }

    public double getDMaxMm() {
        return dMaxMm;
    }

    public void setDMaxMm(double dMaxMm) {
        this.dMaxMm = dMaxMm;
    }

    /** Search supply list by ID — used when reading flood_data.txt. */
    public SupplyItem findSupplyItem(String id) {
        for (SupplyItem s : supplyItems) {
            if (s.getId().equals(id)) {
                return s;
            }
        }
        return null;
    }

    /**
     * Loads the default NADMA Sector 4 scenario from our project report.
     * 2 hubs (UPM, UNITEN), 6 affected areas, 19 roads, 6 supply types.
     * Triggered by "Load Selangor Sample" button or on first app launch.
     */
    public void loadSelangorSample() {
        graph.clear();
        supplyItems.clear();
        truckCapacityKg = 500;
        dMaxMm = 400;

        // Add all places with map position (layoutX, layoutY) for the GUI circle layout
        addPlace("UPM", "UPM", PlaceType.RELIEF_HUB, 100, 0.50, 0.18);
        addPlace("UNIT", "UNITEN", PlaceType.RELIEF_HUB, 120, 0.50, 0.42);
        addPlace("MERAB", "SK Sungai Merab", PlaceType.AFFECTED_AREA, 350, 0.28, 0.70);
        addPlace("RAMAL", "SMK Sungai Ramal", PlaceType.AFFECTED_AREA, 360, 0.72, 0.70);
        addPlace("SKSS", "SK Sri Serdang", PlaceType.AFFECTED_AREA, 260, 0.18, 0.32);
        addPlace("SMKSS", "SMK Sri Serdang", PlaceType.AFFECTED_AREA, 340, 0.82, 0.32);
        addPlace("U360", "Univ 360", PlaceType.AFFECTED_AREA, 320, 0.22, 0.52);
        addPlace("KTMB", "KTMB", PlaceType.AFFECTED_AREA, 300, 0.78, 0.52);

        // Add roads: travel time (min), weight limit (kg), flooded flag, flood depth (mm)
        addRoad("UPM", "UNIT", 9, 500, false, 120);
        addRoad("UNIT", "MERAB", 11, 500, true, 760);
        addRoad("UNIT", "RAMAL", 10, 500, false, 680);
        addRoad("UPM", "SKSS", 6, 500, false, 260);
        addRoad("UPM", "SMKSS", 8, 500, false, 340);
        addRoad("UPM", "U360", 7, 500, false, 420);
        addRoad("UPM", "KTMB", 9, 500, false, 300);
        addRoad("SKSS", "SMKSS", 5, 500, false, 340);
        addRoad("SKSS", "U360", 4, 500, false, 420);
        addRoad("SKSS", "KTMB", 7, 500, false, 300);
        addRoad("SMKSS", "SKSS", 5, 500, false, 260);
        addRoad("SMKSS", "U360", 6, 500, false, 420);
        addRoad("SMKSS", "KTMB", 4, 500, false, 300);
        addRoad("U360", "SKSS", 4, 500, false, 260);
        addRoad("U360", "SMKSS", 6, 500, false, 340);
        addRoad("U360", "KTMB", 5, 500, false, 300);
        addRoad("KTMB", "SKSS", 7, 500, false, 260);
        addRoad("KTMB", "SMKSS", 4, 500, false, 340);
        addRoad("KTMB", "U360", 5, 500, false, 420);

        // Default relief supplies with weight, priority score, and stock (kg)
        supplyItems.add(new SupplyItem("medical", "Medical Kit", 20, 10, 60));
        supplyItems.add(new SupplyItem("water", "Clean Water", 10, 8, 100));
        supplyItems.add(new SupplyItem("formula", "Infant Formula", 5, 9, 40));
        supplyItems.add(new SupplyItem("rice", "Rice Ration", 15, 7, 200));
        supplyItems.add(new SupplyItem("torch", "Torch+Battery", 2, 6, 50));
        supplyItems.add(new SupplyItem("blanket", "Blanket", 3, 5, 80));
    }

    /** Helper used by sample loader — creates a node with a fixed map position. */
    private void addPlace(String id, String name, PlaceType type, double floodMm,
                          double layoutX, double layoutY) {
        Node node = new Node(id, name, type, floodMm);
        node.setLayoutX(layoutX);
        node.setLayoutY(layoutY);
        graph.addNode(node);
    }

    /** Called when user adds a new place from the Map tab — starts at map centre. */
    public void addPlace(String id, String name, PlaceType type, double floodMm) {
        Node node = new Node(id, name, type, floodMm);
        node.setLayoutX(0.5);
        node.setLayoutY(0.5);
        graph.addNode(node);
    }

    /**
     * Adds a road when user draws it on the map.
     * Flood depth on the road = average of flood at both places.
     */
    public void addRoad(String from, String to, double minutes, double limitKg, boolean flooded) {
        if (graph.getNode(from) == null || graph.getNode(to) == null) {
            return;
        }
        if (graph.findEdge(from, to) != null) {
            return; // road already exists
        }
        Node nodeFrom = graph.getNode(from);
        Node nodeTo = graph.getNode(to);
        double avgFloodDepth = (nodeFrom.getFloodDepthMm() + nodeTo.getFloodDepthMm()) / 2.0;

        graph.addEdge(new Edge(from, to, minutes, limitKg, flooded, avgFloodDepth));
    }

    /** Helper for sample data — lets us set exact flood depth per road. */
    private void addRoad(String from, String to, double minutes, double limitKg,
                         boolean flooded, double floodDepthMm) {
        if (graph.getNode(from) == null || graph.getNode(to) == null) {
            return;
        }
        if (graph.findEdge(from, to) != null) {
            return;
        }
        graph.addEdge(new Edge(from, to, minutes, limitKg, flooded, floodDepthMm));
    }

    /** Adds a new supply row from the Supplies tab. */
    public void addSupplyItem(String name, double weight, double priority, double available) {
        String id = "item" + (supplyItems.size() + 1);
        supplyItems.add(new SupplyItem(id, name, weight, priority, available));
    }

    /** Removes one supply item by table row index. */
    public void removeSupplyItem(int index) {
        if (index >= 0 && index < supplyItems.size()) {
            supplyItems.remove(index);
        }
    }

    /**
     * Saves everything to flood_data.txt in the project folder.
     * Called after edits and after Calculate so data is not lost when app closes.
     */
    public synchronized void save() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(DATA_FILE))) {
            // Line 1: truck capacity and Dmax
            pw.printf("CONFIG,%.2f,%.2f%n", truckCapacityKg, dMaxMm);
            // One line per place
            for (Node n : graph.getAllNodes()) {
                pw.printf("NODE,%s,%s,%s,%.2f,%.4f,%.4f%n",
                        n.getId(), n.getName(), n.getPlaceType().name(),
                        n.getFloodDepthMm(), n.getLayoutX(), n.getLayoutY());
            }
            // One line per road
            for (Edge e : graph.getEdges()) {
                pw.printf("EDGE,%s,%s,%.2f,%.2f,%s,%.2f%n",
                        e.getFrom(), e.getTo(), e.getTravelMinutes(),
                        e.getWeightLimitKg(), e.isFlooded(), e.getFloodDepthMm());
            }
            // One line per supply item
            for (SupplyItem s : supplyItems) {
                pw.printf("ITEM,%s,%s,%.2f,%.2f,%.2f%n",
                        s.getId(), s.getName(), s.getWeightPerUnit(),
                        s.getPriorityScore(), s.getAvailableKg());
            }
        } catch (IOException ex) {
            System.err.println("Could not save data: " + ex.getMessage());
        }
    }

    /**
     * Reads flood_data.txt on startup.
     * If the file does not exist yet, we keep the sample data from loadSelangorSample().
     */
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
                String[] p = line.split(",", -1);
                switch (p[0]) {
                    case "CONFIG":
                        truckCapacityKg = Double.parseDouble(p[1]);
                        dMaxMm = Double.parseDouble(p[2]);
                        break;
                    case "NODE":
                        applyNodeLine(p);
                        break;
                    case "EDGE":
                        applyEdgeLine(p);
                        break;
                    case "ITEM":
                        applyItemLine(p);
                        break;
                    default:
                        break;
                }
            }
        } catch (IOException ex) {
            System.err.println("Could not load data: " + ex.getMessage());
        }
    }

    /** Parses one NODE line from the file and updates or creates that place. */
    private void applyNodeLine(String[] p) {
        if (p.length < 5) {
            return;
        }
        String id = p[1];
        Node existing = graph.getNode(id);
        PlaceType type;
        try {
            type = PlaceType.valueOf(p[3]);
        } catch (IllegalArgumentException e) {
            type = PlaceType.AFFECTED_AREA;
        }
        double flood = Double.parseDouble(p[4]);
        double lx = p.length > 5 ? Double.parseDouble(p[5]) : 0.5;
        double ly = p.length > 6 ? Double.parseDouble(p[6]) : 0.5;
        if (existing != null) {
            existing.setName(p[2]);
            existing.setPlaceType(type);
            existing.setFloodDepthMm(flood);
            existing.setLayoutX(lx);
            existing.setLayoutY(ly);
        } else {
            Node node = new Node(id, p[2], type, flood);
            node.setLayoutX(lx);
            node.setLayoutY(ly);
            graph.addNode(node);
        }
    }

    /** Parses one EDGE line from the file and updates or creates that road. */
    private void applyEdgeLine(String[] p) {
        if (p.length < 4) {
            return;
        }
        Edge e = graph.findEdge(p[1], p[2]);
        double minutes = Double.parseDouble(p[3]);
        double limit = p.length > 4 ? Double.parseDouble(p[4]) : 500;
        boolean flooded = p.length > 5 && Boolean.parseBoolean(p[5]);
        double floodDepth = p.length > 6 ? Double.parseDouble(p[6]) : 0.0;

        if (e != null) {
            e.setTravelMinutes(minutes);
            e.setWeightLimitKg(limit);
            e.setFlooded(flooded);
            e.setFloodDepthMm(floodDepth);
        } else if (graph.getNode(p[1]) != null && graph.getNode(p[2]) != null) {
            graph.addEdge(new Edge(p[1], p[2], minutes, limit, flooded, floodDepth));
        }
    }

    /** Parses one ITEM line and updates an existing supply item's details. */
    private void applyItemLine(String[] p) {
        if (p.length < 5) {
            return;
        }
        SupplyItem item = findSupplyItem(p[1]);
        if (item != null) {
            item.setName(p[2]);
            item.setWeightPerUnit(Double.parseDouble(p[3]));
            item.setPriorityScore(Double.parseDouble(p[4]));
            if (p.length > 5) {
                item.setAvailableKg(Double.parseDouble(p[5]));
            }
        }
    }
}
