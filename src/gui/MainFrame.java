package gui;

import controller.ReliefPlannerController;
import model.DeliveryPlan;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.BorderLayout;
import java.awt.FlowLayout;

public class MainFrame extends JFrame {

    private final ReliefPlannerController controller;
    private final MapRoadsPanel mapPanel;
    private final SuppliesPanel suppliesPanel;
    private final DeliveryPlanPanel planPanel;
    private final JTabbedPane tabs = new JTabbedPane();

    public MainFrame(ReliefPlannerController controller) {
        super("Flash Flood Relief Planner - Selangor");
        this.controller = controller;

        mapPanel = new MapRoadsPanel(controller, this::refreshPlan);
        suppliesPanel = new SuppliesPanel(controller, this::refreshPlan);
        planPanel = new DeliveryPlanPanel(controller);

        tabs.addTab("1. Map & Roads", mapPanel);
        tabs.addTab("2. Supplies", suppliesPanel);
        tabs.addTab("3. Delivery Plan", planPanel);

        JButton calculate = new JButton("Calculate Delivery Plan");
        calculate.addActionListener(e -> runCalculation());

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(calculate);

        setLayout(new BorderLayout());
        add(tabs, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(980, 720);
        setLocationRelativeTo(null);
    }

    private void runCalculation() {
        mapPanel.applyRoadTableEdits();
        suppliesPanel.applyEdits();
        planPanel.refreshDeliveryRequestControls();
        DeliveryPlan plan = controller.calculateDeliveryPlan();
        planPanel.showPlan(plan);
        tabs.setSelectedIndex(2);
        JOptionPane.showMessageDialog(this,
                "Plan ready! " + plan.getReachableDestinations() + " places can receive help. "
                        + plan.getBlockedDestinations() + " places are blocked.");
    }

    private void refreshPlan() {
        planPanel.refresh();
    }

    public static void launch(ReliefPlannerController controller) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame(controller);
            frame.setVisible(true);
        });
    }
}
