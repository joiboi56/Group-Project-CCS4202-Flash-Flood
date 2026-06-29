package gui;

import controller.ReliefPlannerController;
import model.DeliveryPlan;
import model.DeliveryRequest;
import model.DeliveryRoute;
import model.Graph;
import model.KnapsackLineItem;
import model.KnapsackResult;
import model.Node;
import model.PlaceType;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;

/**
 * Tab 3 — Delivery Plan.
 * Shows Dijkstra route results, knapsack loading comparison, and simple advice text.
 * Three sub-tabs: Where to Send Help | What to Load on Truck | Simple Advice
 */
public class DeliveryPlanPanel extends JPanel {

    private final ReliefPlannerController controller;

    private final JLabel summaryLabel = new JLabel("Run Calculate Delivery Plan to see results.");
    private final JLabel routeStats = new JLabel(" ");
    private final JLabel loadStats = new JLabel(" ");
    private final JProgressBar truckBar = new JProgressBar(0, 100);
    private final DefaultTableModel planRequestModel;
    private final DefaultTableModel routeModel;
    private final DefaultTableModel loadModel;
    private final JTable planRequestTable;
    private final JTextArea adviceArea = new JTextArea();
    private final JComboBox<NodeChoice> hubBox = new JComboBox<>();
    private final JComboBox<NodeChoice> destinationBox = new JComboBox<>();
    private final JButton fractionalBtn = new JButton("Fractional Knapsack");
    private final JButton greedyBtn = new JButton("Greedy Algorithm");

    private DeliveryPlan currentPlan;
    private boolean showGreedy; // false = show fractional, true = show greedy

    public DeliveryPlanPanel(ReliefPlannerController controller) {
        this.controller = controller;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // Top summary area
        JPanel top = new JPanel(new BorderLayout(6, 6));
        top.add(summaryLabel, BorderLayout.NORTH);

        JPanel stats = new JPanel(new GridLayout(1, 2, 8, 0));
        routeStats.setBorder(BorderFactory.createTitledBorder("Delivery Routes"));
        loadStats.setBorder(BorderFactory.createTitledBorder("Supply Loading"));
        stats.add(routeStats);
        stats.add(loadStats);
        top.add(stats, BorderLayout.CENTER);

        truckBar.setStringPainted(true);
        JPanel barPanel = new JPanel(new BorderLayout());
        barPanel.add(new JLabel("Truck space used:"), BorderLayout.WEST);
        barPanel.add(truckBar, BorderLayout.CENTER);
        top.add(barPanel, BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);

        // Table of planned deliveries (hub -> destination)
        planRequestModel = new DefaultTableModel(new String[]{"Relief Hub", "Destination"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        planRequestTable = new JTable(planRequestModel);

        // Table of Dijkstra results after Calculate
        routeModel = new DefaultTableModel(
                new String[]{"Relief Hub", "Destination", "Status", "Travel Time", "Suggested Route"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable routeTable = new JTable(routeModel);
        routeTable.getColumn("Status").setCellRenderer(new StatusRenderer());

        // Table of knapsack loading manifest
        loadModel = new DefaultTableModel(
                new String[]{"Supply Item", "Loaded (kg)", "Help Score", "Fraction"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable loadTable = new JTable(loadModel);

        JPanel loadPanel = new JPanel(new BorderLayout(6, 6));
        JPanel algoBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        algoBar.setBorder(BorderFactory.createTitledBorder("Loading algorithm"));
        fractionalBtn.addActionListener(e -> switchAlgorithm(false));
        greedyBtn.addActionListener(e -> switchAlgorithm(true));
        algoBar.add(fractionalBtn);
        algoBar.add(greedyBtn);
        loadPanel.add(algoBar, BorderLayout.NORTH);
        loadPanel.add(new JScrollPane(loadTable), BorderLayout.CENTER);
        updateAlgorithmButtons();

        adviceArea.setEditable(false);
        adviceArea.setLineWrap(true);
        adviceArea.setWrapStyleWord(true);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Where to Send Help", buildRoutePanel(routeTable));
        tabs.addTab("What to Load on Truck", loadPanel);
        tabs.addTab("Simple Advice", new JScrollPane(adviceArea));
        add(tabs, BorderLayout.CENTER);

        refreshDeliveryRequestControls();
    }

    /** Builds the route sub-tab with delivery setup and results tables. */
    private JPanel buildRoutePanel(JTable routeTable) {
        JPanel routePanel = new JPanel(new BorderLayout(6, 6));
        JPanel editor = new JPanel(new BorderLayout(6, 6));
        editor.setBorder(BorderFactory.createTitledBorder("Delivery plan setup"));

        JPanel fields = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        fields.add(new JLabel("Hub:"));
        fields.add(hubBox);
        fields.add(new JLabel("Destination:"));
        fields.add(destinationBox);

        JButton addPlan = new JButton("Add Delivery");
        JButton removePlan = new JButton("Remove Selected");
        JButton resetPlan = new JButton("Reset Sample Plan");
        addPlan.addActionListener(e -> addDeliveryRequest());
        removePlan.addActionListener(e -> removeSelectedDeliveryRequest());
        resetPlan.addActionListener(e -> resetSampleDeliveryRequests());
        fields.add(addPlan);
        fields.add(removePlan);
        fields.add(resetPlan);

        editor.add(fields, BorderLayout.NORTH);
        JScrollPane requestScroll = new JScrollPane(planRequestTable);
        editor.add(requestScroll, BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, editor, new JScrollPane(routeTable));
        split.setResizeWeight(0.35);
        split.setOneTouchExpandable(true);
        split.setContinuousLayout(true);
        routePanel.add(split, BorderLayout.CENTER);
        return routePanel;
    }

    /** Refills hub and destination dropdowns from the current graph. */
    public void refreshDeliveryRequestControls() {
        hubBox.removeAllItems();
        destinationBox.removeAllItems();

        Graph graph = controller.getGraph();
        for (Node node : graph.getAllNodes()) {
            NodeChoice choice = new NodeChoice(node.getId(), node.getName());
            if (node.isHub()) {
                hubBox.addItem(choice);
            } else if (node.getPlaceType() == PlaceType.AFFECTED_AREA) {
                destinationBox.addItem(choice);
            }
        }

        refreshDeliveryRequestTable();
    }

    /** Updates the planned deliveries table from controller's request list. */
    private void refreshDeliveryRequestTable() {
        planRequestModel.setRowCount(0);
        Graph graph = controller.getGraph();
        for (DeliveryRequest request : controller.getDeliveryRequests()) {
            Node hub = graph.getNode(request.getHubId());
            Node destination = graph.getNode(request.getDestinationId());
            planRequestModel.addRow(new Object[]{
                    hub != null ? hub.getName() : request.getHubId(),
                    destination != null ? destination.getName() : request.getDestinationId()
            });
        }
    }

    /** Adds hub->destination from dropdowns to the delivery plan list. */
    private void addDeliveryRequest() {
        NodeChoice hub = (NodeChoice) hubBox.getSelectedItem();
        NodeChoice destination = (NodeChoice) destinationBox.getSelectedItem();
        if (hub == null || destination == null) {
            JOptionPane.showMessageDialog(this, "Select a hub and destination first.");
            return;
        }
        controller.addDeliveryRequest(hub.id, destination.id);
        refreshDeliveryRequestTable();
        currentPlan = null;
        routeModel.setRowCount(0);
        summaryLabel.setText("Run Calculate Delivery Plan to see results.");
        routeStats.setText(" ");
    }

    /** Removes selected row from delivery plan list. */
    private void removeSelectedDeliveryRequest() {
        int row = planRequestTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a delivery plan row first.");
            return;
        }
        controller.removeDeliveryRequest(planRequestTable.convertRowIndexToModel(row));
        refreshDeliveryRequestTable();
        currentPlan = null;
        routeModel.setRowCount(0);
        summaryLabel.setText("Run Calculate Delivery Plan to see results.");
        routeStats.setText(" ");
    }

    /** Restores the 6 default NADMA Sector 4 delivery routes. */
    private void resetSampleDeliveryRequests() {
        controller.resetDeliveryRequestsToSample();
        refreshDeliveryRequestControls();
        currentPlan = null;
        routeModel.setRowCount(0);
        summaryLabel.setText("Run Calculate Delivery Plan to see results.");
        routeStats.setText(" ");
    }

    /**
     * Displays a completed DeliveryPlan — called from MainFrame after Calculate.
     * Fills route table, loading table, progress bar, and advice text.
     */
    public void showPlan(DeliveryPlan plan) {
        if (plan == null) {
            return;
        }
        currentPlan = plan;
        summaryLabel.setText("Summary: " + plan.getReachableDestinations()
                + " places can receive help, " + plan.getBlockedDestinations() + " places are blocked.");
        routeStats.setText(plan.getReachableDestinations() + " reachable | "
                + plan.getBlockedDestinations() + " blocked");

        routeModel.setRowCount(0);
        for (DeliveryRoute r : plan.getRoutes()) {
            String statusText;
            String timeText;
            String routeText = r.routeText();

            if (r.canDeliver()) {
                statusText = "Can Deliver";
                timeText = ((int) r.getTravelMinutes()) + " min";
            } else {
                statusText = "Blocked";
                timeText = "-";
                routeText = "Need boat. Waiting for rescue vehicle.";
            }

            routeModel.addRow(new Object[]{
                    r.getHubName(),
                    r.getDestinationName(),
                    statusText,
                    timeText,
                    routeText
            });
        }

        refreshLoadView();

        adviceArea.setText("");
        for (String line : plan.buildAdvice()) {
            adviceArea.append(line + "\n");
        }
    }

    /** Switches between fractional and greedy knapsack result views. */
    private void switchAlgorithm(boolean greedy) {
        showGreedy = greedy;
        updateAlgorithmButtons();
        refreshLoadView();
    }

    /** Greys out the button for the algorithm currently being displayed. */
    private void updateAlgorithmButtons() {
        fractionalBtn.setEnabled(showGreedy);
        greedyBtn.setEnabled(!showGreedy);
    }

    /** Updates loading table and truck progress bar for the active algorithm. */
    private void refreshLoadView() {
        if (currentPlan == null) {
            loadStats.setText(" ");
            truckBar.setValue(0);
            truckBar.setString("");
            loadModel.setRowCount(0);
            return;
        }

        KnapsackResult load = showGreedy ? currentPlan.getGreedyResult() : currentPlan.getKnapsackResult();
        String algoLabel = showGreedy ? "Greedy (whole items)" : "Fractional knapsack";

        if (load != null) {
            loadStats.setText(String.format("%s: %.1f kg loaded | Help score: %.1f",
                    algoLabel, load.getTotalWeight(), load.getTotalScore()));
            int pct = currentPlan.getTruckCapacity() <= 0 ? 0
                    : (int) Math.round(100.0 * load.getTotalWeight() / currentPlan.getTruckCapacity());
            truckBar.setValue(Math.min(100, pct));
            truckBar.setString(String.format("%.1f / %.1f kg (%d%%)",
                    load.getTotalWeight(), currentPlan.getTruckCapacity(), pct));
        } else {
            loadStats.setText(algoLabel + ": no result");
            truckBar.setValue(0);
            truckBar.setString("");
        }

        loadModel.setRowCount(0);
        if (load != null) {
            for (KnapsackLineItem line : load.getManifest()) {
                if (line.getWeightLoaded() <= 0) {
                    continue;
                }
                loadModel.addRow(new Object[]{
                        line.getItem().getName(),
                        String.format("%.1f", line.getWeightLoaded()),
                        String.format("%.1f", line.getScoreAdded()),
                        String.format("%.2f", line.getFraction())
                });
            }
        }
    }

    /** Refreshes dropdowns and re-shows last plan (e.g. after map edit). */
    public void refresh() {
        refreshDeliveryRequestControls();
        showPlan(controller.getLastPlan());
    }

    /** Wrapper for combo box — stores node ID but displays place name. */
    private static class NodeChoice {
        private final String id;
        private final String name;

        private NodeChoice(String id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    /** Colours route status green (Can Deliver) or red (Blocked). */
    private static class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public java.awt.Component getTableCellRendererComponent(JTable table, Object value,
                                                                boolean isSelected, boolean hasFocus,
                                                                int row, int column) {
            java.awt.Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (!isSelected && value != null) {
                if ("Can Deliver".equalsIgnoreCase(value.toString())) {
                    c.setBackground(new Color(200, 235, 200));
                } else if ("Blocked".equalsIgnoreCase(value.toString())) {
                    c.setBackground(new Color(245, 200, 200));
                } else {
                    c.setBackground(Color.WHITE);
                }
            }
            return c;
        }
    }
}
