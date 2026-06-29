package gui;

import controller.ReliefPlannerController;
import database.FloodDatabase;

public class Main {
    public static void main(String[] args) {
        FloodDatabase database = new FloodDatabase();
        ReliefPlannerController controller = new ReliefPlannerController(database);
        MainFrame.launch(controller);
    }
}
