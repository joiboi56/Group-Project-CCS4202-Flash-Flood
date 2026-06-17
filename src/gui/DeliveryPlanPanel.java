package gui;

import controller.ReliefPlannerController;
import model.DeliveryPlan;
import model.DeliveryRoute;
import model.KnapsackLineItem;
import model.KnapsackResult;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;

public class DeliveryPlanPanel extends JPanel {

    private final ReliefPlannerController controller;

    private final JLabel summaryLabel = new JLabel("Run Calculate Delivery Plan to see results.");
    private final JLabel routeStats = new JLabel(" ");
    private final JLabel loadStats = new JLabel(" ");
    private final JProgressBar truckBar = new JProgressBar(0, 100);
    private final DefaultTableModel routeModel;
    private final DefaultTableModel loadModel;
    private final JTextArea adviceArea = new JTextArea();

    public DeliveryPlanPanel(ReliefPlannerController controller) {
        this.controller = controller;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

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

        routeModel = new DefaultTableModel(
                new String[]{"Relief Hub", "Destination", "Status", "Travel Time", "Suggested Route"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable routeTable = new JTable(routeModel);
        routeTable.getColumn("Status").setCellRenderer(new StatusRenderer());

        loadModel = new DefaultTableModel(
                new String[]{"Supply Item", "Loaded (kg)", "Help Score", "Fraction"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable loadTable = new JTable(loadModel);

        adviceArea.setEditable(false);
        adviceArea.setLineWrap(true);
        adviceArea.setWrapStyleWord(true);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Where to Send Help", new JScrollPane(routeTable));
        tabs.addTab("What to Load on Truck", new JScrollPane(loadTable));
        tabs.addTab("Simple Advice", new JScrollPane(adviceArea));
        add(tabs, BorderLayout.CENTER);
    }

    public void showPlan(DeliveryPlan plan) {
        if (plan == null) {
            return;
        }
        summaryLabel.setText("Summary: " + plan.getReachableDestinations()
                + " places can receive help, " + plan.getBlockedDestinations() + " places are blocked.");
        routeStats.setText(plan.getReachableDestinations() + " reachable | "
                + plan.getBlockedDestinations() + " blocked");

        KnapsackResult load = plan.getKnapsackResult();
        if (load != null) {
            loadStats.setText(String.format("%.1f kg loaded | Help score: %.1f",
                    load.getTotalWeight(), load.getTotalScore()));
            int pct = plan.getTruckCapacity() <= 0 ? 0
                    : (int) Math.round(100.0 * load.getTotalWeight() / plan.getTruckCapacity());
            truckBar.setValue(Math.min(100, pct));
            truckBar.setString(String.format("%.1f / %.1f kg (%d%%)",
                    load.getTotalWeight(), plan.getTruckCapacity(), pct));
        }

        routeModel.setRowCount(0);
        for (DeliveryRoute r : plan.getRoutes()) {
            routeModel.addRow(new Object[]{
                    r.getHubName(),
                    r.getDestinationName(),
                    r.canDeliver() ? "CAN DELIVER" : "BLOCKED",
                    r.canDeliver() ? ((int) r.getTravelMinutes()) + " min" : "-",
                    r.routeText()
            });
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

        adviceArea.setText("");
        for (String line : plan.buildAdvice()) {
            adviceArea.append(line + "\n");
        }
    }

    public void refresh() {
        showPlan(controller.getLastPlan());
    }

    private static class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public java.awt.Component getTableCellRendererComponent(JTable table, Object value,
                                                                boolean isSelected, boolean hasFocus,
                                                                int row, int column) {
            java.awt.Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (!isSelected && value != null) {
                if ("CAN DELIVER".equals(value.toString())) {
                    c.setBackground(new Color(200, 235, 200));
                } else if ("BLOCKED".equals(value.toString())) {
                    c.setBackground(new Color(245, 200, 200));
                } else {
                    c.setBackground(Color.WHITE);
                }
            }
            return c;
        }
    }
}
