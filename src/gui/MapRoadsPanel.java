package gui;

import controller.ReliefPlannerController;
import model.Edge;
import model.Graph;
import model.Node;
import model.PlaceType;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;

public class MapRoadsPanel extends JPanel {

    private static final double BLOCKED_FLOOD_DEPTH_MM = 700.0;

    private final ReliefPlannerController controller;
    private final Runnable onPlanReady;

    private final MapGraphPanel graphPanel = new MapGraphPanel();
    private final JTextField nameField = new JTextField(18);
    private final JTextField floodField = new JTextField(6);
    private final JComboBox<PlaceType> typeBox = new JComboBox<>(PlaceType.values());
    private final DefaultTableModel roadModel;
    private final JTable roadTable;

    public MapRoadsPanel(ReliefPlannerController controller, Runnable onPlanReady) {
        this.controller = controller;
        this.onPlanReady = onPlanReady;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JLabel hint = new JLabel(
                "Mark each location as Relief Hub (where supplies start) or Affected Area (where help is needed).");
        add(hint, BorderLayout.NORTH);

        JPanel mapArea = new JPanel(new BorderLayout(6, 6));
        mapArea.add(buildToolbar(), BorderLayout.NORTH);
        mapArea.add(graphPanel, BorderLayout.CENTER);
        mapArea.add(buildEditor(), BorderLayout.SOUTH);

        roadModel = new DefaultTableModel(
                new String[]{"From Place", "To Place", "Time (min)", "Limit (kg)", "Flooded?"}, 0) {
            @Override
            public Class<?> getColumnClass(int column) {
                return column == 4 ? Boolean.class : String.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column >= 2;
            }
        };
        roadTable = new JTable(roadModel);
        roadTable.getColumn("Flooded?").setCellEditor(roadTable.getDefaultEditor(Boolean.class));
        roadTable.getColumn("Flooded?").setCellRenderer(roadTable.getDefaultRenderer(Boolean.class));

        JPanel roadsPanel = new JPanel(new BorderLayout(4, 4));
        roadsPanel.setBorder(BorderFactory.createTitledBorder("Roads"));
        JScrollPane roadScroll = new JScrollPane(roadTable);
        roadScroll.setPreferredSize(new Dimension(0, 180));
        roadsPanel.add(roadScroll, BorderLayout.CENTER);
        JButton removeRoad = new JButton("Remove Selected Road");
        removeRoad.addActionListener(e -> removeSelectedRoad());
        roadsPanel.add(removeRoad, BorderLayout.SOUTH);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, mapArea, roadsPanel);
        split.setResizeWeight(0.72);
        split.setOneTouchExpandable(true);
        split.setContinuousLayout(true);
        split.setDividerSize(6);
        add(split, BorderLayout.CENTER);

        graphPanel.setGraphSupplier(controller::getGraph);
        graphPanel.setSelectionListener(this::showSelectedNode);
        graphPanel.setGraphChangeListener(() -> {
            controller.save();
            refreshRoadTable();
        });

        wireGraphPanel();
        refreshRoadTable();
        SwingUtilities.invokeLater(() -> split.setDividerLocation(0.72));
    }

    private JPanel buildToolbar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        JButton addPlace = new JButton("Add Place");
        JButton deleteSelected = new JButton("Delete Selected");
        JButton addRoad = new JButton("Add Road");
        JButton rearrange = new JButton("Re-arrange Map");
        JButton loadSample = new JButton("Load Selangor Sample");

        addPlace.addActionListener(e -> addPlace());
        deleteSelected.addActionListener(e -> deleteSelected());
        addRoad.addActionListener(e -> graphPanel.startAddRoadMode());
        rearrange.addActionListener(e -> {
            graphPanel.rearrangeCircular();
            controller.save();
        });
        loadSample.addActionListener(e -> {
            controller.loadSample();
            graphPanel.rearrangeCircular();
            controller.save();
            graphPanel.refresh();
            refreshRoadTable();
            clearEditor();
            onPlanReady.run();
        });

        bar.add(addPlace);
        bar.add(deleteSelected);
        bar.add(addRoad);
        bar.add(rearrange);
        bar.add(loadSample);
        return bar;
    }

    private JPanel buildEditor() {
        JPanel panel = new JPanel(new GridLayout(2, 1));
        panel.setBorder(BorderFactory.createTitledBorder("Place editor"));
        panel.add(new JLabel("Click a place on the map to edit it."));

        JPanel fields = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        fields.add(new JLabel("Place name:"));
        fields.add(nameField);
        fields.add(new JLabel("Flood level (mm):"));
        fields.add(floodField);
        fields.add(new JLabel("Type:"));
        fields.add(typeBox);
        JButton savePlace = new JButton("Save Place");
        savePlace.addActionListener(e -> saveSelectedPlace());
        fields.add(savePlace);
        panel.add(fields);
        return panel;
    }

    private void wireGraphPanel() {
        graphPanel.setOnRoadAdded((from, to) -> {
            String minutes = JOptionPane.showInputDialog(this, "Travel time (minutes):", "10");
            if (minutes == null) {
                return;
            }
            String limit = JOptionPane.showInputDialog(this, "Weight limit (kg):", "500");
            if (limit == null) {
                return;
            }
            try {
                controller.getGraph().addEdge(new Edge(from, to,
                        Double.parseDouble(minutes), Double.parseDouble(limit), false));
                controller.save();
                refreshRoadTable();
                graphPanel.refresh();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter valid numbers.");
            }
        });
    }

    private void addPlace() {
        String name = JOptionPane.showInputDialog(this, "Place name:");
        if (name == null || name.isBlank()) {
            return;
        }
        String id = name.trim().replaceAll("\\s+", "").substring(0, Math.min(4, name.trim().length())).toUpperCase();
        if (controller.getGraph().getNode(id) != null) {
            id = id + (controller.getGraph().getAllNodes().size() + 1);
        }
        String floodStr = JOptionPane.showInputDialog(this, "Flood level (mm):", "200");
        if (floodStr == null) {
            return;
        }
        try {
            double flood = Double.parseDouble(floodStr);
            controller.getDatabase().addPlace(id, name.trim(), PlaceType.AFFECTED_AREA, flood);
            graphPanel.placeNewNode(id);
            controller.save();
            refreshRoadTable();
            onPlanReady.run();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid flood level.");
        }
    }

    private void deleteSelected() {
        String id = graphPanel.getSelectedNodeId();
        if (id == null) {
            JOptionPane.showMessageDialog(this, "Select a place on the map first.");
            return;
        }
        int ok = JOptionPane.showConfirmDialog(this, "Delete " + id + " and its roads?");
        if (ok != JOptionPane.YES_OPTION) {
            return;
        }
        controller.getGraph().removeNode(id);
        graphPanel.clearSelection();
        controller.save();
        refreshRoadTable();
        graphPanel.refresh();
        clearEditor();
        onPlanReady.run();
    }

    private void showSelectedNode(Node node) {
        if (node == null) {
            clearEditor();
            return;
        }
        nameField.setText(node.getName());
        floodField.setText(String.valueOf((int) node.getFloodDepthMm()));
        typeBox.setSelectedItem(node.getPlaceType());
    }

    private void saveSelectedPlace() {
        String id = graphPanel.getSelectedNodeId();
        if (id == null) {
            JOptionPane.showMessageDialog(this, "Select a place on the map first.");
            return;
        }
        Node node = controller.getGraph().getNode(id);
        try {
            node.setName(nameField.getText().trim());
            node.setFloodDepthMm(Double.parseDouble(floodField.getText().trim()));
            node.setPlaceType((PlaceType) typeBox.getSelectedItem());
            controller.save();
            graphPanel.refresh();
            refreshRoadTable();
            onPlanReady.run();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Flood level must be a number.");
        }
    }

    private void clearEditor() {
        nameField.setText("");
        floodField.setText("");
        typeBox.setSelectedIndex(0);
    }

    public void refreshRoadTable() {
        roadModel.setRowCount(0);
        Graph graph = controller.getGraph();
        for (Edge e : graph.getEdges()) {
            Node from = graph.getNode(e.getFrom());
            Node to = graph.getNode(e.getTo());
            roadModel.addRow(new Object[]{
                    from != null ? from.getName() : e.getFrom(),
                    to != null ? to.getName() : e.getTo(),
                    String.valueOf(e.getTravelMinutes()),
                    String.valueOf(e.getWeightLimitKg()),
                    e.isFlooded() || e.getFloodDepthMm() >= BLOCKED_FLOOD_DEPTH_MM
            });
        }
    }

    private void removeSelectedRoad() {
        int row = roadTable.getSelectedRow();
        if (row < 0) {
            return;
        }
        Graph graph = controller.getGraph();
        String fromName = (String) roadModel.getValueAt(row, 0);
        String toName = (String) roadModel.getValueAt(row, 1);
        String fromId = null;
        String toId = null;
        for (Node n : graph.getAllNodes()) {
            if (n.getName().equals(fromName)) {
                fromId = n.getId();
            }
            if (n.getName().equals(toName)) {
                toId = n.getId();
            }
        }
        if (fromId != null && toId != null) {
            graph.removeEdge(fromId, toId);
            controller.save();
            refreshRoadTable();
            graphPanel.refresh();
            onPlanReady.run();
        }
    }

    public void applyRoadTableEdits() {
        Graph graph = controller.getGraph();
        for (int i = 0; i < roadModel.getRowCount(); i++) {
            String fromName = (String) roadModel.getValueAt(i, 0);
            String toName = (String) roadModel.getValueAt(i, 1);
            String fromId = null;
            String toId = null;
            for (Node n : graph.getAllNodes()) {
                if (n.getName().equals(fromName)) {
                    fromId = n.getId();
                }
                if (n.getName().equals(toName)) {
                    toId = n.getId();
                }
            }
            if (fromId == null || toId == null) {
                continue;
            }
            Edge edge = graph.findEdge(fromId, toId);
            if (edge == null) {
                continue;
            }
            try {
                edge.setTravelMinutes(Double.parseDouble(roadModel.getValueAt(i, 2).toString()));
                edge.setWeightLimitKg(Double.parseDouble(roadModel.getValueAt(i, 3).toString()));
                boolean blocked = (Boolean) roadModel.getValueAt(i, 4);
                edge.setFlooded(blocked);
                if (blocked) {
                    edge.setFloodDepthMm(Math.max(edge.getFloodDepthMm(), BLOCKED_FLOOD_DEPTH_MM));
                } else if (edge.getFloodDepthMm() >= BLOCKED_FLOOD_DEPTH_MM) {
                    edge.setFloodDepthMm(0);
                }
            } catch (NumberFormatException ignored) {
            }
        }
        controller.save();
        graphPanel.refresh();
        onPlanReady.run();
    }
}
