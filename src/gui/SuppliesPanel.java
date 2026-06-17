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

        tableModel = new DefaultTableModel(
                new String[]{"Supply Item", "Weight per unit (kg)", "Priority Score", "Available (kg)"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column > 0;
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

    private void removeItem(JTable table) {
        int row = table.getSelectedRow();
        if (row < 0) {
            return;
        }
        controller.getDatabase().removeSupplyItem(row);
        controller.save();
        reloadTable();
    }

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

    public void calculatePlan() {
        applyEdits();
        controller.calculateDeliveryPlan();
        onPlanReady.run();
    }
}
