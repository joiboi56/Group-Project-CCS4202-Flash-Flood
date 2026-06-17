import controller.ReliefPlannerController;
import database.FloodDatabase;

public class QuickTest {
    public static void main(String[] args) {
        ReliefPlannerController c = new ReliefPlannerController(new FloodDatabase());
        var p = c.calculateDeliveryPlan();
        System.out.println("Reachable: " + p.getReachableDestinations());
        System.out.println("Blocked: " + p.getBlockedDestinations());
        System.out.println("Loaded: " + p.getKnapsackResult().getTotalWeight());
        System.out.println("Score: " + p.getKnapsackResult().getTotalScore());
    }
}
