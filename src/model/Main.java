package model;

import controller.ReliefPlannerController;
import database.FloodDatabase;
import gui.MainFrame;

public class Main {
    public static void main(String[] args) {
        FloodDatabase database = new FloodDatabase();
        ReliefPlannerController controller = new ReliefPlannerController(database);
        MainFrame.launch(controller);
    }
}
