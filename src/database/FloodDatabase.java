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
 * Stores the road network, flood readings and relief supplies.
 */
public class FloodDatabase {

    private static final String DATA_FILE = "flood_data.txt";

    private final Graph graph = new Graph();
    private final List<SupplyItem> supplyItems = new ArrayList<>();
    private double truckCapacityKg = 500;
    private double dMaxMm = 400;

    public FloodDatabase() {
        loadSelangorSample();
        load();
    }

    public Graph getGraph() {
        return graph;
    }

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

    public SupplyItem findSupplyItem(String id) {
        for (SupplyItem s : supplyItems) {
            if (s.getId().equals(id)) {
                return s;
            }
        }
        return null;
    }

    public void loadSelangorSample() {
        graph.clear();
        supplyItems.clear();
        truckCapacityKg = 500;
        dMaxMm = 400;

        addPlace("SERD", "Serdang Hospital", PlaceType.RELIEF_HUB, 100, 0.50, 0.35);
        addPlace("UPM", "UPM Sports Complex", PlaceType.RELIEF_HUB, 100, 0.50, 0.65);
        addPlace("RAYA", "Taman Serdang Raya", PlaceType.AFFECTED_AREA, 300, 0.62, 0.42);
        addPlace("BAND", "Bandar Baru Bangi", PlaceType.AFFECTED_AREA, 390, 0.72, 0.55);
        addPlace("KAJA", "Kajang Town Centre", PlaceType.AFFECTED_AREA, 350, 0.78, 0.48);
        addPlace("BALA", "Balakong", PlaceType.AFFECTED_AREA, 450, 0.85, 0.38);
        addPlace("TAMA", "Taman Connaught", PlaceType.AFFECTED_AREA, 280, 0.38, 0.52);
        addPlace("ONN", "Bandar Tun Hussein Onn", PlaceType.AFFECTED_AREA, 420, 0.82, 0.62);
        addPlace("SEME", "Seri Kembangan", PlaceType.AFFECTED_AREA, 320, 0.58, 0.58);
        addPlace("PUCH", "Puchong", PlaceType.AFFECTED_AREA, 500, 0.22, 0.72);
        addPlace("CHER", "Cheras", PlaceType.AFFECTED_AREA, 400, 0.30, 0.45);
        addPlace("SEMPA", "Semenyih", PlaceType.AFFECTED_AREA, 410, 0.88, 0.30);
        addPlace("AMPA", "Ampang", PlaceType.AFFECTED_AREA, 410, 0.18, 0.35);
        addPlace("PUTRA", "Putra Heights", PlaceType.AFFECTED_AREA, 200, 0.28, 0.78);

        addRoad("SERD", "RAYA", 12, 500, false);
        addRoad("SERD", "UPM", 8, 500, false);
        addRoad("SERD", "TAMA", 10, 500, false);
        addRoad("SERD", "SEME", 15, 500, false);
        addRoad("UPM", "RAYA", 5, 500, false);
        addRoad("UPM", "BAND", 15, 500, false);
        addRoad("UPM", "KAJA", 10, 500, false);
        addRoad("UPM", "CHER", 12, 500, false);
        addRoad("UPM", "PUTRA", 8, 500, true);
        addRoad("RAYA", "BAND", 8, 400, false);
        addRoad("RAYA", "KAJA", 12, 500, false);
        addRoad("BAND", "KAJA", 5, 500, false);
        addRoad("KAJA", "ONN", 6, 500, true);
        addRoad("KAJA", "BALA", 8, 300, false);
        addRoad("PUTRA", "PUCH", 9, 500, true);
        addRoad("BAND", "SEMPA", 14, 500, true);
        addRoad("CHER", "AMPA", 7, 500, true);
        addRoad("SEME", "TAMA", 4, 500, false);
        addRoad("AMPA", "ONN", 10, 500, false);
        addRoad("SEME", "KAJA", 11, 500, false);
        addRoad("TAMA", "CHER", 13, 500, false);

        supplyItems.add(new SupplyItem("medical", "Medical Kit", 20, 10, 60));
        supplyItems.add(new SupplyItem("water", "Clean Water", 10, 8, 100));
        supplyItems.add(new SupplyItem("formula", "Infant Formula", 5, 9, 40));
        supplyItems.add(new SupplyItem("rice", "Rice Ration", 15, 7, 200));
        supplyItems.add(new SupplyItem("torch", "Torch+Battery", 2, 6, 50));
        supplyItems.add(new SupplyItem("blanket", "Blanket", 3, 5, 80));
    }

    private void addPlace(String id, String name, PlaceType type, double floodMm,
                          double layoutX, double layoutY) {
        Node node = new Node(id, name, type, floodMm);
        node.setLayoutX(layoutX);
        node.setLayoutY(layoutY);
        graph.addNode(node);
    }

    public void addPlace(String id, String name, PlaceType type, double floodMm) {
        Node node = new Node(id, name, type, floodMm);
        node.setLayoutX(0.5);
        node.setLayoutY(0.5);
        graph.addNode(node);
    }

    public void addRoad(String from, String to, double minutes, double limitKg, boolean flooded) {
        if (graph.getNode(from) == null || graph.getNode(to) == null) {
            return;
        }
        if (graph.findEdge(from, to) != null) {
            return;
        }
        // NEW LOGIC: Calculate average depth
        Node nodeFrom = graph.getNode(from);
        Node nodeTo = graph.getNode(to);
        double avgFloodDepth = (nodeFrom.getFloodDepthMm() + nodeTo.getFloodDepthMm()) / 2.0;

        graph.addEdge(new Edge(from, to, minutes, limitKg, flooded, avgFloodDepth)); // <--- Pass depth
    }
    public void addSupplyItem(String name, double weight, double priority, double available) {
        String id = "item" + (supplyItems.size() + 1);
        supplyItems.add(new SupplyItem(id, name, weight, priority, available));
    }

    public void removeSupplyItem(int index) {
        if (index >= 0 && index < supplyItems.size()) {
            supplyItems.remove(index);
        }
    }

    public synchronized void save() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(DATA_FILE))) {
            pw.printf("CONFIG,%.2f,%.2f%n", truckCapacityKg, dMaxMm);
            for (Node n : graph.getAllNodes()) {
                pw.printf("NODE,%s,%s,%s,%.2f,%.4f,%.4f%n",
                        n.getId(), n.getName(), n.getPlaceType().name(),
                        n.getFloodDepthMm(), n.getLayoutX(), n.getLayoutY());
            }
            for (Edge e : graph.getEdges()) {
                pw.printf("EDGE,%s,%s,%.2f,%.2f,%s,%.2f%n", // <--- Added extra placeholder for depth
                        e.getFrom(), e.getTo(), e.getTravelMinutes(),
                        e.getWeightLimitKg(), e.isFlooded(), e.getFloodDepthMm()); // <--- Added depth
            }
            for (SupplyItem s : supplyItems) {
                pw.printf("ITEM,%s,%s,%.2f,%.2f,%.2f%n",
                        s.getId(), s.getName(), s.getWeightPerUnit(),
                        s.getPriorityScore(), s.getAvailableKg());
            }
        } catch (IOException ex) {
            System.err.println("Could not save data: " + ex.getMessage());
        }
    }

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

    private void applyEdgeLine(String[] p) {
        if (p.length < 4) {
            return;
        }
        Edge e = graph.findEdge(p[1], p[2]);
        double minutes = Double.parseDouble(p[3]);
        double limit = p.length > 4 ? Double.parseDouble(p[4]) : 500;
        boolean flooded = p.length > 5 && Boolean.parseBoolean(p[5]);
        double floodDepth = p.length > 6 ? Double.parseDouble(p[6]) : 0.0; // <--- NEW LINE

        if (e != null) {
            e.setTravelMinutes(minutes);
            e.setWeightLimitKg(limit);
            e.setFlooded(flooded);
            e.setFloodDepthMm(floodDepth); // <--- NEW LINE
        } else if (graph.getNode(p[1]) != null && graph.getNode(p[2]) != null) {
            graph.addEdge(new Edge(p[1], p[2], minutes, limit, flooded, floodDepth)); // <--- Pass depth
        }
    }

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
