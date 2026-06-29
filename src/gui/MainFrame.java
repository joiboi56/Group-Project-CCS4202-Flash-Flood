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

/**
 * GUI PACKAGE — MainFrame is the root window (View layer in MVC).
 *
 * Contains 3 tabs: Map & Roads, Supplies, Delivery Plan.
 * The "Calculate Delivery Plan" button at the bottom triggers all algorithms.
 */
public class MainFrame extends JFrame {

    private final ReliefPlannerController controller;
    private final MapRoadsPanel mapPanel;
    private final SuppliesPanel suppliesPanel;
    private final DeliveryPlanPanel planPanel;
    private final JTabbedPane tabs = new JTabbedPane();

    /**
     * Builds the main window and wires up all three tabs.
     * @param controller the MVC controller that runs Dijkstra and knapsack
     */
    public MainFrame(ReliefPlannerController controller) {
        super("Flash Flood Relief Planner - Selangor");
        this.controller = controller;

        // Create each tab panel — pass controller so they can read/write data
        mapPanel = new MapRoadsPanel(controller, this::refreshPlan);
        suppliesPanel = new SuppliesPanel(controller, this::refreshPlan);
        planPanel = new DeliveryPlanPanel(controller);

        tabs.addTab("1. Map & Roads", mapPanel);
        tabs.addTab("2. Supplies", suppliesPanel);
        tabs.addTab("3. Delivery Plan", planPanel);

        // Main action button — runs the full planning process
        JButton calculate = new JButton("Calculate Delivery Plan");
        calculate.addActionListener(e -> runCalculation());

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(calculate);

        setLayout(new BorderLayout());
        add(tabs, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(980, 720);
        setLocationRelativeTo(null); // centre on screen
    }

    /**
     * Called when Calculate is clicked.
     * 1) Save any edits from map and supplies tabs
     * 2) Run controller.calculateDeliveryPlan() (Dijkstra + knapsack)
     * 3) Show results on Delivery Plan tab
     */
    private void runCalculation() {
        mapPanel.applyRoadTableEdits();
        suppliesPanel.applyEdits();
        planPanel.refreshDeliveryRequestControls();
        DeliveryPlan plan = controller.calculateDeliveryPlan();
        planPanel.showPlan(plan);
        tabs.setSelectedIndex(2); // jump to Delivery Plan tab
        JOptionPane.showMessageDialog(this,
                "Plan ready! " + plan.getReachableDestinations() + " places can receive help. "
                        + plan.getBlockedDestinations() + " places are blocked.");
    }

    /** Refreshes Delivery Plan tab when map or supplies change. */
    private void refreshPlan() {
        planPanel.refresh();
    }

    /**
     * Entry point called from Main.java — creates and shows the window.
     * Uses system look-and-feel so buttons match the OS style.
     */
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
