package gui;

import model.Edge;
import model.Graph;
import model.Node;
import model.PlaceType;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.function.Supplier;

/**
 * The interactive map canvas — draws circles (places) and lines (roads).
 * Dotted lines = flooded/blocked roads (matches our project proposal sketch).
 * Users can drag places and click to add roads.
 */
public class MapGraphPanel extends JPanel {

    private static final int NODE_R = 22; // radius of each place circle
    private static final double BLOCKED_FLOOD_DEPTH_MM = 700.0;

    private Supplier<Graph> graphSupplier; // gives us the current graph from controller
    private String selectedId;             // which place is clicked
    private String roadStartId;            // first click when adding a road
    private boolean addRoadMode;           // true when user clicked "Add Road"

    private Node dragNode;    // place being dragged with mouse
    private int dragOffsetX;
    private int dragOffsetY;

    private NodeSelectionListener selectionListener;
    private Runnable graphChangeListener;
    private RoadAddedListener roadAddedListener;

    /** Sets up mouse listeners for click, drag, and road drawing. */
    public MapGraphPanel() {
        setBackground(new Color(30, 34, 42));
        setPreferredSize(new Dimension(700, 420));
        setMinimumSize(new Dimension(400, 300));

        MouseAdapter mouse = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                handlePress(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (dragNode != null) {
                    dragNode = null;
                    if (graphChangeListener != null) {
                        graphChangeListener.run(); // save position after drag
                    }
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (dragNode != null) {
                    moveNode(dragNode, e.getX(), e.getY());
                    repaint();
                }
            }
        };
        addMouseListener(mouse);
        addMouseMotionListener(mouse);
    }

    public void setGraphSupplier(Supplier<Graph> graphSupplier) {
        this.graphSupplier = graphSupplier;
    }

    /** Called when user clicks a place — updates the editor fields below map. */
    public void setSelectionListener(NodeSelectionListener listener) {
        this.selectionListener = listener;
    }

    public void setGraphChangeListener(Runnable listener) {
        this.graphChangeListener = listener;
    }

    /** Called when user finishes drawing a road (second click). */
    public void setOnRoadAdded(RoadAddedListener listener) {
        this.roadAddedListener = listener;
    }

    public String getSelectedNodeId() {
        return selectedId;
    }

    public void clearSelection() {
        selectedId = null;
        repaint();
    }

    /** Turns on crosshair cursor — user clicks two places to connect them. */
    public void startAddRoadMode() {
        addRoadMode = true;
        roadStartId = null;
        setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
    }

    /** Redraw the map (e.g. after data changes). */
    public void refresh() {
        repaint();
    }

    /** Centres a newly added place on the map. */
    public void placeNewNode(String id) {
        Graph graph = graphSupplier.get();
        Node node = graph.getNode(id);
        if (node != null) {
            node.setLayoutX(0.5);
            node.setLayoutY(0.5);
        }
        selectedId = id;
        repaint();
        notifySelection();
    }

    /**
     * "Re-arrange Map" button — spreads all places in a circle so the map looks tidy.
     * Only changes screen positions, not routes or flood data.
     */
    public void rearrangeCircular() {
        Graph graph = graphSupplier.get();
        int count = graph.getAllNodes().size();
        if (count == 0) {
            return;
        }
        int i = 0;
        for (Node n : graph.getAllNodes()) {
            double angle = (2 * Math.PI * i) / count - Math.PI / 2;
            n.setLayoutX(0.5 + 0.42 * Math.cos(angle));
            n.setLayoutY(0.5 + 0.42 * Math.sin(angle));
            i++;
        }
        repaint();
    }

    /** Handles mouse click — either select/drag a node or add-road second step. */
    private void handlePress(MouseEvent e) {
        Graph graph = graphSupplier.get();
        Node hit = findNodeAt(graph, e.getX(), e.getY());
        if (addRoadMode) {
            if (hit == null) {
                return;
            }
            if (roadStartId == null) {
                roadStartId = hit.getId(); // first click — remember start
            } else if (!roadStartId.equals(hit.getId())) {
                if (roadAddedListener != null) {
                    roadAddedListener.onRoadAdded(roadStartId, hit.getId()); // second click — create road
                }
                addRoadMode = false;
                roadStartId = null;
                setCursor(Cursor.getDefaultCursor());
            }
            return;
        }

        if (hit != null) {
            selectedId = hit.getId();
            dragNode = hit;
            Point2D p = toScreen(hit);
            dragOffsetX = (int) (e.getX() - p.getX());
            dragOffsetY = (int) (e.getY() - p.getY());
            notifySelection();
            repaint();
        } else {
            selectedId = null;
            notifySelection();
            repaint();
        }
    }

    /** Tells MapRoadsPanel which place was selected so editor fields update. */
    private void notifySelection() {
        if (selectionListener == null) {
            return;
        }
        Graph graph = graphSupplier.get();
        selectionListener.onNodeSelected(selectedId != null ? graph.getNode(selectedId) : null);
    }

    /** Updates layoutX/layoutY while user drags a place on the map. */
    private void moveNode(Node node, int mouseX, int mouseY) {
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) {
            return;
        }
        double x = (mouseX - dragOffsetX - NODE_R) / (double) (w - 2 * NODE_R);
        double y = (mouseY - dragOffsetY - NODE_R) / (double) (h - 2 * NODE_R);
        node.setLayoutX(clamp(x));
        node.setLayoutY(clamp(y));
    }

    /** Keeps nodes inside the map area (not off the edge). */
    private double clamp(double v) {
        return Math.max(0.05, Math.min(0.95, v));
    }

    /** Returns which place (if any) is under the mouse coordinates. */
    private Node findNodeAt(Graph graph, int x, int y) {
        for (Node n : graph.getAllNodes()) {
            Point2D p = toScreen(n);
            double dx = x - p.getX();
            double dy = y - p.getY();
            if (dx * dx + dy * dy <= NODE_R * NODE_R) {
                return n;
            }
        }
        return null;
    }

    /** Converts stored layout position (0-1) to pixel coordinates on screen. */
    private Point2D toScreen(Node n) {
        int w = Math.max(getWidth(), 1);
        int h = Math.max(getHeight(), 1);
        double x = NODE_R + n.getLayoutX() * (w - 2 * NODE_R);
        double y = NODE_R + n.getLayoutY() * (h - 2 * NODE_R);
        return new Point2D.Double(x, y);
    }

    /**
     * Main drawing method — paints roads then places on top.
     * Travel time and flood depth labels are drawn on each road.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (graphSupplier == null) {
            return;
        }
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Graph graph = graphSupplier.get();

        // --- Draw all roads ---
        for (Edge edge : graph.getEdges()) {
            Node from = graph.getNode(edge.getFrom());
            Node to = graph.getNode(edge.getTo());
            if (from == null || to == null) {
                continue;
            }
            Point2D p1 = toScreen(from);
            Point2D p2 = toScreen(to);
            // Flooded or >= 700mm -> dashed grey line (blocked)
            if (edge.isFlooded() || edge.getFloodDepthMm() >= BLOCKED_FLOOD_DEPTH_MM) {
                g2.setColor(new Color(120, 120, 120));
                g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                        0, new float[]{6, 6}, 0));
            } else {
                g2.setColor(new Color(170, 175, 185));
                g2.setStroke(new BasicStroke(1.8f));
            }
            g2.drawLine((int) p1.getX(), (int) p1.getY(), (int) p2.getX(), (int) p2.getY());

            // Show travel time and flood depth on the road
            int mx = (int) ((p1.getX() + p2.getX()) / 2);
            int my = (int) ((p1.getY() + p2.getY()) / 2);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
            g2.setColor(new Color(200, 205, 215));
            g2.drawString((int) edge.getTravelMinutes() + "m", mx - 8, my - 6);
            if (edge.getFloodDepthMm() > 0) {
                g2.setColor(new Color(160, 175, 195));
                g2.drawString((int) edge.getFloodDepthMm() + "mm", mx - 12, my + 8);
            }
        }

        // --- Draw all places as coloured circles ---
        for (Node node : graph.getAllNodes()) {
            Point2D p = toScreen(node);
            int cx = (int) p.getX();
            int cy = (int) p.getY();

            boolean selected = node.getId().equals(selectedId);
            Color fill = nodeFillColor(node);
            Color border = selected ? Color.WHITE : outlineColor(node);
            if (node.getPlaceType() == PlaceType.RELIEF_HUB) {
                g2.setColor(new Color(230, 190, 40));
                g2.setFont(new Font("SansSerif", Font.BOLD, 10));
                g2.drawString("HUB", cx - 12, cy - NODE_R - 4);
            }

            g2.setColor(fill);
            g2.fillOval(cx - NODE_R, cy - NODE_R, NODE_R * 2, NODE_R * 2);
            g2.setColor(border);
            g2.setStroke(new BasicStroke(selected ? 3f : 2f));
            g2.drawOval(cx - NODE_R, cy - NODE_R, NODE_R * 2, NODE_R * 2);

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.BOLD, 11));
            FontMetrics fm = g2.getFontMetrics();
            String label = node.shortLabel();
            g2.drawString(label, cx - fm.stringWidth(label) / 2, cy + 4);
        }

        g2.dispose();
    }

    /** Circle colour based on flood depth — redder = more dangerous. */
    private Color nodeFillColor(Node node) {
        if (node.getPlaceType() == PlaceType.RELIEF_HUB) {
            return new Color(45, 110, 70);
        }
        double d = node.getFloodDepthMm();
        if (d >= 450) {
            return new Color(170, 55, 55);
        }
        if (d >= 400) {
            return new Color(195, 95, 45);
        }
        if (d >= 300) {
            return new Color(210, 165, 45);
        }
        if (d >= 200) {
            return new Color(210, 205, 55);
        }
        return new Color(55, 120, 185);
    }

    /** Border colour around each place circle. */
    private Color outlineColor(Node node) {
        double d = node.getFloodDepthMm();
        if (d >= 400) {
            return new Color(200, 70, 70);
        }
        if (d >= 300) {
            return new Color(220, 140, 50);
        }
        return new Color(70, 140, 210);
    }

    /** Callback when user clicks a place — used by MapRoadsPanel. */
    public interface NodeSelectionListener {
        void onNodeSelected(Node node);
    }

    /** Callback when user finishes drawing a road between two places. */
    public interface RoadAddedListener {
        void onRoadAdded(String fromId, String toId);
    }
}
