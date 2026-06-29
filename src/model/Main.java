package model;

import controller.ReliefPlannerController;
import database.FloodDatabase;
import gui.MainFrame;

/**
 * PROGRAM ENTRY POINT — starts the Flash Flood Relief Planner application.
 *
 * MVC startup order:
 *   1) FloodDatabase  — loads map and supplies (Model data)
 *   2) ReliefPlannerController — connects data to algorithms (Controller)
 *   3) MainFrame — shows the GUI window (View)
 */
public class Main {
    public static void main(String[] args) {
        FloodDatabase database = new FloodDatabase();
        ReliefPlannerController controller = new ReliefPlannerController(database);
        MainFrame.launch(controller);
    }
}
