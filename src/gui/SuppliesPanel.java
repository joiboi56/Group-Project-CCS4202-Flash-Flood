package gui;

import controller.ReliefPlannerController;
import database.FloodDatabase;
import model.SupplyItem;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;

/**
 * Tab 2 — Supplies panel.
 * User edits relief items (weight, priority, stock) and truck capacity W.
 * These values are passed to the knapsack algorithms when Calculate runs.
 */
public class SuppliesPanel extends JPanel {

    private final ReliefPlannerController controller;
    private final Runnable onPlanReady;
    private final DefaultTableModel tableModel;
    private final JTextField truckField;

    public SuppliesPanel(ReliefPlannerController controller, Runnable onPlanReady) {
        this.controller = controller;
        this.onPlanReady = onPlanReady;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        add(new JLabel("List the relief items available and how much stock you have."), BorderLayout.NORTH);

        // Table columns match SupplyItem fields used in knapsack
        tableModel = new DefaultTableModel(
                new String[]{"Supply Item", "Weight per unit (kg)", "Priority Score", "Available (kg)"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column > 0; // name column not editable in table (use Add Item)
            }
        };
        JTable table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addItem = new JButton("Add Item");
        JButton removeItem = new JButton("Remove Selected");
        addItem.addActionListener(e -> addItem());
        removeItem.addActionListener(e -> removeItem(table));
        left.add(addItem);
        left.add(removeItem);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        truckField = new JTextField(6);
        truckField.setText(String.valueOf((int) controller.getDatabase().getTruckCapacityKg()));
        right.add(new JLabel("Truck capacity (kg):"));
        right.add(truckField);

        bottom.add(left, BorderLayout.WEST);
        bottom.add(right, BorderLayout.EAST);
        add(bottom, BorderLayout.SOUTH);

        reloadTable();
    }

    /** Fills the table from the database supply list. */
    private void reloadTable() {
        tableModel.setRowCount(0);
        for (SupplyItem item : controller.getSupplyItems()) {
            tableModel.addRow(new Object[]{
                    item.getName(),
                    item.getWeightPerUnit(),
                    item.getPriorityScore(),
                    item.getAvailableKg()
            });
        }
    }

    /** Prompts user for a new supply item and adds it to the database. */
    private void addItem() {
        String name = JOptionPane.showInputDialog(this, "Item name:");
        if (name == null || name.isBlank()) {
            return;
        }
        try {
            double weight = Double.parseDouble(JOptionPane.showInputDialog(this, "Weight per unit (kg):", "5"));
            double priority = Double.parseDouble(JOptionPane.showInputDialog(this, "Priority score:", "5"));
            double available = Double.parseDouble(JOptionPane.showInputDialog(this, "Available (kg):", "50"));
            controller.getDatabase().addSupplyItem(name.trim(), weight, priority, available);
            controller.save();
            reloadTable();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers.");
        }
    }

    /** Deletes the highlighted row from the supply list. */
    private void removeItem(JTable table) {
        int row = table.getSelectedRow();
        if (row < 0) {
            return;
        }
        controller.getDatabase().removeSupplyItem(row);
        controller.save();
        reloadTable();
    }

    /**
     * Copies table values into SupplyItem objects before Calculate runs.
     * Also updates truck capacity — this becomes W in the knapsack problem.
     */
    public void applyEdits() {
        FloodDatabase db = controller.getDatabase();
        for (int i = 0; i < tableModel.getRowCount() && i < db.getSupplyItems().size(); i++) {
            SupplyItem item = db.getSupplyItems().get(i);
            item.setName(tableModel.getValueAt(i, 0).toString());
            try {
                item.setWeightPerUnit(Double.parseDouble(tableModel.getValueAt(i, 1).toString()));
                item.setPriorityScore(Double.parseDouble(tableModel.getValueAt(i, 2).toString()));
                item.setAvailableKg(Double.parseDouble(tableModel.getValueAt(i, 3).toString()));
            } catch (NumberFormatException ignored) {
            }
        }
        try {
            db.setTruckCapacityKg(Double.parseDouble(truckField.getText().trim()));
        } catch (NumberFormatException ignored) {
        }
        controller.save();
    }

    /** Optional shortcut — apply edits then calculate (not used by main button). */
    public void calculatePlan() {
        applyEdits();
        controller.calculateDeliveryPlan();
        onPlanReady.run();
    }
}
